/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.query;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.io.Printable;
import org.apache.jena.atlas.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.algebra.table.TableData;
import com.hp.hpl.jena.sparql.core.DatasetDescription;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.QueryCompare;
import com.hp.hpl.jena.sparql.core.QueryHashCode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.serializer.Serializer;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.PatternVars;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import eu.play_platform.platformservices.bdpl.syntax.windows.Window;
import eu.play_platform.platformservices.bdpl.syntax.windows.types.DummyWindow;

/** The data structure for a query as presented externally.
 *  There are two ways of creating a query - use the parser to turn
 *  a string description of the query into the executable form, and
 *  the programmatic way (the parser is calling the programmatic
 *  operations driven by the quyery string).  The declarative approach
 *  of passing in a string is preferred.
 *
 * Once a query is built, it can be passed to the QueryFactory to produce a query execution engine.
 * @see QueryExecutionFactory
 * @see ResultSet
 */
public class Query extends Prologue implements Cloneable, Printable, Serializable //TODO is not serializable. sobermeier look at it.
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 100L;

	static { ARQ.init() ; /* Ensure everything has started properly */ }
    
    public static final int QueryTypeUnknown    = -123 ;
    public static final int QueryTypeSelect     = 111 ;
    public static final int QueryTypeConstruct  = 222 ;
    public static final int QueryTypeDescribe   = 333 ;
    public static final int QueryTypeAsk        = 444 ;
    int queryType = QueryTypeUnknown ;

    // Play extensions ----------------------------------
    // Event Data
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String queryId;
    private Element eventQuery;
    private Window window = new DummyWindow();


    public Element getEventQuery() {
		return eventQuery;
	}

	public void setEventQuery(Element element){
    	eventQuery = element;
    }

	public void clearCEPData(){
		eventQuery = null;
	}

	public Window getWindow(){
		return window;
	}
	
	public void setWindow(Window window){
		this.window = window;
	}
	
	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
    
//=================================================================================
    
    // If no model is provided explicitly, the query engine will load
    // a model from the URL.  Never a list of zero items.
    
    private List<String> graphURIs = new ArrayList<String>() ;
    private List<String> namedGraphURIs = new ArrayList<String>() ;
    
    // The WHERE clause
    private Element queryPattern = null ;
    
    // Query syntax
    private Syntax syntax = Syntax.syntaxSPARQL ; // Default
    
    // LIMIT/OFFSET
    public static final long  NOLIMIT = Long.MIN_VALUE ;
    private long resultLimit   = NOLIMIT ;
    private long resultOffset  = NOLIMIT ;
    
    // ORDER BY
    private List<SortCondition> orderBy       = null ;
    public static final int ORDER_ASCENDING           = 1 ;
    public static final int ORDER_DESCENDING          = -1 ;
    public static final int ORDER_DEFAULT             = -2 ;    // Not explicitly given.
    public static final int ORDER_UNKNOW              = -3 ;

    // VALUES trailing clause
    protected TableData valuesDataBlock = null ;
    
    protected boolean strictQuery = true ;
    
    // SELECT * seen
    protected boolean queryResultStar        = false ;
    
    protected boolean distinct               = false ;
    protected boolean reduced                = false ;
    
    // CONSTRUCT
    protected Template constructTemplate  = null ;
    
    // DESCRIBE
    // Any URIs/QNames in the DESCRIBE clause
    // Also uses resultVars
    protected List<Node> resultNodes               = new ArrayList<Node>() ;     // Type in list: Node
    
    /**
     * Creates a new empty query
     */
    public Query()
    {
        syntax = Syntax.syntaxSPARQL ;
    }
    
    /**
     * Creates a new empty query with the given prologue
     */
    public Query(Prologue prologue)
    {
        this() ;
        usePrologueFrom(prologue) ;
    }
    
    // Allocate variables that are unique to this query.
    private final VarAlloc varAlloc = new VarAlloc(ARQConstants.allocVarMarker) ;
    private Var allocInternVar() { return varAlloc.allocVar() ; }
    
    //private VarAlloc varAnonAlloc = new VarAlloc(ARQConstants.allocVarAnonMarker) ;
    //public Var allocVarAnon() { return varAnonAlloc.allocVar() ; }
    
    @Deprecated
    public void setQueryType(int qType)         { queryType = qType ; }
    
    public void setQuerySelectType()            { queryType = QueryTypeSelect ; }
    public void setQueryConstructType()         { queryType = QueryTypeConstruct ; queryResultStar = true ; }
    public void setQueryDescribeType()          { queryType = QueryTypeDescribe ; }
    public void setQueryAskType()               { queryType = QueryTypeAsk ; }
    
    public int getQueryType()                   { return queryType ; }
    
    public boolean isSelectType()               { return queryType == QueryTypeSelect ; }

    public boolean isConstructType()            { return queryType == QueryTypeConstruct ; }

    public boolean isDescribeType()             { return queryType == QueryTypeDescribe ; }

    public boolean isAskType()                  { return queryType == QueryTypeAsk ; }

    public boolean isUnknownType()              { return queryType == QueryTypeUnknown ; }

    public void setStrict(boolean isStrict)
    {
        strictQuery = isStrict ;
        
        if ( strictQuery )
            initStrict() ;
        else
            initLax() ;
    }
    
    public boolean isStrict()                { return strictQuery ; }
    
    private void initStrict()
    {
//        if ( prefixMap.getGlobalPrefixMapping() == globalPrefixMap )
//            prefixMap.setGlobalPrefixMapping(null) ;
    }

    private void initLax()
    {
//        if ( prefixMap.getGlobalPrefixMapping() == null )
//            prefixMap.setGlobalPrefixMapping(globalPrefixMap) ;
    }
    
    public void setDistinct(boolean b) { distinct = b ; }
    public boolean isDistinct()        { return distinct ; }
    
    public void setReduced(boolean b) { reduced = b ; }
    public boolean isReduced()        { return reduced ; }
    
    /** @return Returns the syntax. */
    public Syntax getSyntax()         { return syntax ; }

    /** @param syntax The syntax to set. */
    public void setSyntax(Syntax syntax)
    {
        this.syntax = syntax ;
        if ( syntax != Syntax.syntaxSPARQL )
            strictQuery = false ;
    }

    // ---- Limit/offset
    
    public long getLimit()             { return resultLimit ; }
    public void setLimit(long limit)   { resultLimit = limit ; }
    public boolean hasLimit()          { return resultLimit != NOLIMIT ; }
    
    public long getOffset()            { return resultOffset ; }
    public void setOffset(long offset) { resultOffset = offset ; }
    public boolean hasOffset()         { return resultOffset != NOLIMIT ; }
    
    // ---- Order By
    
    public boolean hasOrderBy()        { return orderBy != null && orderBy.size() > 0 ; }
    
    public boolean isOrdered()         { return hasOrderBy() ; }

    public void addOrderBy(SortCondition condition)
    {
        if ( orderBy == null )
            orderBy = new ArrayList<SortCondition>() ;

        orderBy.add(condition) ;
    }
    public void addOrderBy(Expr expr, int direction)
    {
        SortCondition sc = new SortCondition(expr, direction) ;
        addOrderBy(sc) ;
    }
    
    public void addOrderBy(Node var, int direction)
    {
        if ( ! var.isVariable() )
            throw new QueryException("Not a variable: "+var) ;
        SortCondition sc = new SortCondition(var, direction) ;
        addOrderBy(sc) ;
    }
    
    public void addOrderBy(String varName, int direction)
    {
        varName = Var.canonical(varName) ;
        SortCondition sc = new SortCondition(new ExprVar(varName), direction) ;
        addOrderBy(sc) ;
    }

    public List<SortCondition> getOrderBy()           { return orderBy ; }
    
    // ----
    
    /** Answer whether the query had SELECT/DESCRIBE/CONSTRUCT *
     * @return boolean as to whether a * result form was seen
     */
    public boolean isQueryResultStar() { return false;} //queryResultStar ; } //FIXME Solve it in an other way. (SELECT * not legal with GROUP BY)

    /** Set whether the query had SELECT/DESCRIBE *
     * Strictly, this just means whether the projection is
     * 
     * @param isQueryStar
     */
    public void setQueryResultStar(boolean isQueryStar)
    {
//        if ( isConstructType() )
//            throw new IllegalArgumentException("Query is a CONSTRUCT query") ;
//        if ( isAskType() )
//            throw new IllegalArgumentException("Query is an ASK query") ;
        queryResultStar = isQueryStar ;
    }
    
    public void setQueryPattern(Element elt)
    {
        queryPattern = elt ;
//        if ( queryBlock == null )
//            queryBlock = new ElementBlock(null, null) ;
//        queryBlock.setPatternElement(elt) ;
    }
    
    public Element getQueryPattern() { return queryPattern ; }
    
     /** Location of the source for the data.  If the model is not set,
     *  then the QueryEngine will attempt to load the data from these URIs
     *  into the default (unamed) graph.
     */
    public void addGraphURI(String s)
    {
        if ( graphURIs == null )
            graphURIs = new ArrayList<String>() ;
        graphURIs.add(s) ;
    }

    /** Location of the source for the data.  If the model is not set,
     *  then the QueryEngine will attempt to load the data from these URIs
     *  as named graphs in the dataset.
     */
    public void addNamedGraphURI(String uri)
    {
        if ( namedGraphURIs == null )
            namedGraphURIs = new ArrayList<String>() ;
        if ( namedGraphURIs.contains(uri) )
            throw new QueryException("URI already in named graph set: "+uri) ;
        else
            namedGraphURIs.add(uri) ;
    }
    
    /** Return the list of URIs (strings) for the unnamed graph
     * 
     * @return List of strings
     */
    
    public List<String> getGraphURIs() { return graphURIs ; }

    /** Test whether the query mentions a URI in forming the default graph (FROM clause)
     * 
     * @param uri
     * @return boolean  True if the URI used in a FROM clause
     */
    public boolean usesGraphURI(String uri) { return graphURIs.contains(uri) ; }
    
    /** Return the list of URIs (strings) for the named graphs (FROM NAMED clause)
     * 
     * @return List of strings
     */
    
    public List<String> getNamedGraphURIs() { return namedGraphURIs ; }

    /** Test whether the query mentions a URI for a named graph.
     * 
     * @param uri
     * @return True if the URI used in a FROM NAMED clause
     */
    public boolean usesNamedGraphURI(String uri) { return namedGraphURIs.contains(uri) ; }
    
    /** Return true if the query has either some graph
     * URIs or some named graph URIs in its description.
     * This does not mean these URIs will be used - just that
     * they are noted as part of the query.
     */
    
    public boolean hasDatasetDescription()
    {
        if ( getGraphURIs() != null && getGraphURIs().size() > 0 )
            return true ;
        if ( getNamedGraphURIs() != null && getNamedGraphURIs().size() > 0 )
            return true ;
        return false ;
    }
    
    /** Return a dataset description (FROM/FROM NAMED clauses) for the query. */
    public DatasetDescription getDatasetDescription()
    {
        DatasetDescription description = new DatasetDescription() ;
        if ( ! hasDatasetDescription() )
            return description ;
        
        description.addAllDefaultGraphURIs(getGraphURIs()) ;
        description.addAllNamedGraphURIs(getNamedGraphURIs()) ;
        return description ;
    }
    
    // ---- SELECT

    protected VarExprList projectVars = new VarExprList() ;

    /** Return a list of the variables requested (SELECT) */
    public List<String> getResultVars()
    {
        // Ensure "SELECT *" processed
        setResultVars() ;
        return Var.varNames(projectVars.getVars()) ;
    }
    
    /** Return a list of the variables requested (SELECT) */
    public List<Var> getProjectVars()
    {
        // Ensure "SELECT *" processed
        setResultVars() ;
        return projectVars.getVars() ;
    }
    
    public VarExprList getProject()
    {
        return projectVars ;
    }
    
    /** Add a collection of projection variables to a SELECT query */
    public void addProjectVars(Collection<?> vars)
    {
        for ( Iterator<?> iter = vars.iterator() ; iter.hasNext() ; )
        {
            Object obj = iter.next();
            if ( obj instanceof String )
            {
                this.addResultVar((String)obj) ;
                continue ;
            }
            if ( obj instanceof Var )
            {
                this.addResultVar((Var)obj) ;
                continue ;
            }
            throw new QueryException("Not a variable or variable name: "+obj) ;
        }
        resultVarsSet = true ;
    }

    
    /** Add a projection variable to a SELECT query */
    public void addResultVar(String varName)
    {
        varName = Var.canonical(varName) ;
        _addResultVar(varName) ;
    }

    public void addResultVar(Node v)
    {
        if ( !v.isVariable() )
            throw new QueryException("Not a variable: "+v) ;
        _addResultVar(v.getName()) ;
    }
    
    public void addResultVar(Node v, Expr expr)
    {
        Var var = null ;
        if ( v == null )
            var = allocInternVar() ;
        else
        {
            if ( !v.isVariable() )
                throw new QueryException("Not a variable: "+v) ;
            var = Var.alloc(v) ;
        }
        _addVarExpr(projectVars, var, expr) ;
    }
    
    /** Add an to a SELECT query (a name will be created for it) */
    public void addResultVar(Expr expr)
    {
        _addVarExpr(projectVars, allocInternVar(), expr) ;
    }

    /** Add a named expression to a SELECT query */
    public void addResultVar(String varName, Expr expr)
    {
        Var var = null ;
        if ( varName == null )
            var = allocInternVar() ;
        else
        {
            varName = Var.canonical(varName) ;
            var = Var.alloc(varName) ;
        }
        _addVarExpr(projectVars, var, expr) ;
    }

    // Add raw name.
    private void _addResultVar(String varName)
    {
        Var v = Var.alloc(varName) ;
        _addVar(projectVars, v) ;
        resultVarsSet = true ;
    }

    private static void _addVar(VarExprList varExprList, Var v)
    {
        if ( varExprList.contains(v) )
        {
            Expr expr = varExprList.getExpr(v) ;
            if ( expr != null )
                
                // SELECT (?a+?b AS ?x) ?x
                throw new QueryBuildException("Duplicate variable (had an expression) in result projection '"+v+"'") ;
            // SELECT ?x ?x
            if ( ! ARQ.allowDuplicateSelectColumns )
                return ;
            // else drop thorugh and have two variables of the same name.
        }
        varExprList.add(v) ;
    }

    private static void _addVarExpr(VarExprList varExprList, Var v, Expr expr)
    {
        if ( varExprList.contains(v) )
            // SELECT ?x (?a+?b AS ?x)
            // SELECT (2*?a AS ?x) (?a+?b AS ?x)
            throw new QueryBuildException("Duplicate variable in result projection '"+v+"'") ;
        varExprList.add(v, expr) ;
    }

    protected VarExprList groupVars = new VarExprList() ;
    protected List<Expr> havingExprs = new ArrayList<Expr>() ;  // Expressions : Make an ExprList?
    
    public boolean hasGroupBy()     { return ! groupVars.isEmpty() || getAggregators().size() > 0 ; }
    public boolean hasHaving()      { return havingExprs != null && havingExprs.size() > 0 ; }
    
    public VarExprList getGroupBy()      { return groupVars ; }
    
    public List<Expr> getHavingExprs()    { return havingExprs ; }
    
    public void addGroupBy(String varName)
    {
        varName = Var.canonical(varName) ;
        addGroupBy(Var.alloc(varName)) ;
    }

    public void addGroupBy(Node v)
    {
        _addVar(groupVars, Var.alloc(v)) ;
    }

    public void addGroupBy(Expr expr) { addGroupBy(null, expr) ; }
    
    public void addGroupBy(Var v, Expr expr)
    {
        if ( v == null )
            v = allocInternVar() ;
        
        if ( expr.isVariable() && v.isAllocVar() )
        {
            // It was (?x) with no AS - keep the name by adding by variable.
            addGroupBy(expr.asVar()) ;
            return ;
        }
        
        groupVars.add(v, expr) ;
    }

    public void addHavingCondition(Expr expr)
    {
        havingExprs.add(expr) ;
    }

    // ---- Aggregates

    // Record allocated aggregations.
    // Later: The same aggregation expression used in a query
    // will always lead to the same aggregator.
    // For now, allocate a fresh one each time (cause the calcutation
    // to be done multiple times but (1) it's unusual to have repeated
    // aggregators normally and (2) the actual calculation is cheap.
        
    // Unlike SELECT expressions, here the expression itself (E_Aggregator) knows its variable
    // Commonality?
    
    private final List<ExprAggregator> aggregators = new ArrayList<ExprAggregator>() ;
    // Using a LinkedhashMap does seem to give strong enugh hashCode equality.
    private final Map<Var, ExprAggregator> aggregatorsMap = new HashMap<Var, ExprAggregator>() ;
    
    // Note any E_Aggregator created for reuse.
    private final Map<String, Var> aggregatorsAllocated = new HashMap<String, Var>() ;
    
    public boolean hasAggregators() { return aggregators.size() != 0  ; }
    public List<ExprAggregator> getAggregators() { return aggregators ; }
    
    public Expr allocAggregate(Aggregator agg)
    {
        // We need to track the aggregators in case one aggregator is used twice, e.g. in HAVING and in SELECT expression
        // (is is that much harm to do twice?  Yes, if distinct.)
        String key = agg.key() ;
        
        Var v = aggregatorsAllocated.get(key);
        if ( v != null )
        {
            ExprAggregator eAgg = aggregatorsMap.get(v) ;
            if ( ! agg.equals(eAgg.getAggregator()) )
                Log.warn(Query.class, "Internal inconsistency: Aggregator: "+agg) ;
            return eAgg ;
        }
        // Allocate.
        v = allocInternVar() ;
        ExprAggregator aggExpr = new ExprAggregator(v, agg) ;
        aggregatorsAllocated.put(key, v) ;
        aggregatorsMap.put(v, aggExpr) ;
        aggregators.add(aggExpr) ;
        return aggExpr ;
    }
    
    // ---- VALUES
    
    /** Does the query have a VALUES trailing block? */
    public boolean hasValues()                { return valuesDataBlock != null ; }
    
    
    /**
     * @deprecated Use hasValues()
     */
    @Deprecated
    public boolean hasBindings()                { return hasValues() ; }
    
    /** Binding variables
     * @deprecated Use getValuesVariables()
     */
    @Deprecated
    public List<Var> getBindingVariables()      { return getValuesVariables() ; }

    /** @deprecated Use getValuesVariables() */
    @Deprecated
    public List<Var> getBindingsVariables()     { return getValuesVariables() ; }

    public List<Var> getValuesVariables()     { return valuesDataBlock==null ? null : valuesDataBlock.getVars() ; }
    
    /** Binding values - null for a Node means undef
     * @deprecated Use getBindingsData()
     */
    @Deprecated
    public List<Binding> getBindingValues()     { return getBindingsData() ; }

    /** @deprecated Use getValuesData() */
    @Deprecated
    public List<Binding> getBindingsData()      { return getValuesData() ; }

    /** VALUES data - null for a Node means undef */
    public List<Binding> getValuesData()      { return valuesDataBlock==null ? null : valuesDataBlock.getRows() ; }

    /** @deprecated Use setValuesDataBlock */
    @Deprecated
    public void setBindings(List<Var> variables, List<Binding> values)
    { setBindingsDataBlock(variables, values) ; }
    
    /** @deprecated Use setValuesDataBlock */
    @Deprecated
    public void setBindingsDataBlock(List<Var> variables, List<Binding> values)
    { setValuesDataBlock(variables, values) ; }
    
    public void setValuesDataBlock(List<Var> variables, List<Binding> values)
    {
        checkDataBlock(variables, values) ;
        valuesDataBlock = new TableData(variables, values) ;
    }
    
    private static void checkDataBlock(List<Var> variables, List<Binding> values)
    {
        // Check.
        int N = variables.size() ;
        for ( Binding valueRow : values )
        {
            Iterator<Var> iter= valueRow.vars() ;
            for ( ; iter.hasNext() ; )
            {
                Var v = iter.next() ;
                if ( ! variables.contains(v) )
                    throw new QueryBuildException("Variable "+v+" not found in "+variables) ;
            }
        }
    }
    
    // ---- CONSTRUCT
    
    /** Get the template pattern for a construct query */
    public Template getConstructTemplate()
    {
        return constructTemplate ;
    }
    
    /** Set triple patterns for a construct query */
    public void setConstructTemplate(Template templ)  { constructTemplate = templ ; }

    // ---- DESCRIBE
    
    public void addDescribeNode(Node node)
    {
        if ( node.isVariable() ) { addResultVar(node) ; return ; }
        if ( node.isURI() || node.isBlank() )
        {
            if ( !resultNodes.contains(node) )
                resultNodes.add(node);
            return ;
        }
        if ( node.isLiteral() )
            throw new QueryException("Result node is a literal: "+FmtUtils.stringForNode(node)) ;
        throw new QueryException("Result node not recognized: "+node) ;
    }

    
    /** Get the result list (things wanted - not the results themselves)
     *  of a DESCRIBE query. */
    public List<Node> getResultURIs() { return resultNodes ; }
    
    private boolean resultVarsSet = false ;
    /** Fix up when the query has "*" (when SELECT * or DESCRIBE *)
     *  and for a construct query.  This operation is idempotent.
     */
    public void setResultVars()
    {
        if ( resultVarsSet )
            return ;
        resultVarsSet = true ;
        
        if ( getQueryPattern() == null )
        {
            if ( ! this.isDescribeType() )
                Log.warn(this, "setResultVars(): no query pattern") ;
            return ;
        }
        
        // May have been added via addResultVar(,Expr)
//        if ( resultVars.size() != 0 )
//            return ;
        
        if ( isSelectType() )
        {
            if ( isQueryResultStar() )
                findAndAddNamedVars() ;
            return ;
        }
        
        if ( isConstructType() )
        {
            // All named variables are in-scope
            findAndAddNamedVars() ;
            return ;
        }
        
        if ( isDescribeType() )
        {
            if ( isQueryResultStar() )
                findAndAddNamedVars() ;
            return ;
        }

//        if ( isAskType() )
//        {}
    }
    
    private void findAndAddNamedVars()
    {
        Iterator<Var> varIter = null ;
        if ( hasGroupBy() )
            varIter = groupVars.getVars().iterator() ;
        else
        {
            // Binding variables -- in patterns, not in filters and not in EXISTS
            LinkedHashSet<Var> queryVars = new LinkedHashSet<Var>() ;
            PatternVars.vars(queryVars, this.getQueryPattern()) ;
            if ( this.hasValues() )
                queryVars.addAll(getValuesVariables()) ;
//            if ( this.hasValues() )
//                queryVars.addAll(getValuesVariables()) ;
            varIter = queryVars.iterator() ;
        }
        
        // All query variables, including ones from bNodes in the query.
        
        for ( ; varIter.hasNext() ; )
        {
            Object obj = varIter.next() ;
            //Var var = (Var)iter.next() ;
            Var var = (Var)obj ;
            if ( var.isNamedVar() )
                addResultVar(var) ;
        }
    }

    public void visit(QueryVisitor visitor)
    {
        visitor.startVisit(this) ;
        visitor.visitResultForm(this) ;
        visitor.visitPrologue(this) ;
        if ( this.isSelectType() )
            visitor.visitSelectResultForm(this) ;
        if ( this.isConstructType() )
            visitor.visitConstructResultForm(this) ;
        if ( this.isDescribeType() )
            visitor.visitDescribeResultForm(this) ;
        if ( this.isAskType() )
            visitor.visitAskResultForm(this) ;
        visitor.visitDatasetDecl(this) ;
        visitor.visitQueryPattern(this) ;
        visitor.visitGroupBy(this) ;
        visitor.visitHaving(this) ;
        visitor.visitOrderBy(this) ;
        visitor.visitOffset(this) ;
        visitor.visitLimit(this) ;
        visitor.visitValues(this) ;
        visitor.finishVisit(this) ;
    }

    @Override
    public Object clone() { return cloneQuery() ; }
    
    /**
     * Makes a copy of this query.  Copies by parsing a query from the serialized form of this query
     * @return Copy of this query
     */
    public Query cloneQuery()
    {
    	//By default clone from serialized form of this query
    	return cloneQuery(false);
    }
    
    /**
     * Makes a copy of this query.  May specify whether is cloned by parsing from original raw query or by parsing from serialized form of this query
     * @param useRawQuery Copy from raw query if present
     * @return Copy of this query
     */
    public Query cloneQuery(boolean useRawQuery)
    {
        // A little crude.
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        serialize(buff, getSyntax()) ;
        String qs = buff.toString() ;
        return QueryFactory.create(qs, getSyntax()) ;
    }
    
    // ---- Query canonical syntax
    
    // Reverse of parsing : should produce a string that parses to an equivalent query
    // "Equivalent" => gives the same results on any model
    @Override
    public String toString()
    { return serialize() ; }
    
    public String toString(Syntax syntax)
    { return serialize(syntax) ; }

    
    /** Must align with .equals */
    private int hashcode = -1 ;
    
    /** Perform some check on the query
     * @deprecated This call does do anything.
     */
    
    @Deprecated
    public void validate()
    {
        // This is mostly done now as part of parsing.
        // See SyntaxVarScope and Parser.validatePasredQuery.
        setResultVars() ;
    }

    @Override
    public int hashCode()
    {
        if ( hashcode == -1 )
        {
            hashcode = QueryHashCode.calc(this) ;
            if ( hashcode == -1 )
                hashcode = Integer.MIN_VALUE/2 ;
        }
        return hashcode ;
    }
    
    /** Are two queries equals - tests shape and details.
     * Equality means that the queries do the same thing, including
     * same variables, in the same places.  Being unequals does
     * <b>not</b> mean the queries do different things.
     * 
     * For example, reordering a group or union
     * means that that a query is different.
     * 
     * Two instances of a query parsed from the same string are equal.
     */
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof Query ) )
            return false ;
        if ( this == other ) return true ;
        return QueryCompare.equals(this, (Query)other) ;
    }
    
//    public static boolean sameAs(Query query1, Query query2)
//    { return query1.sameAs(query2) ; }

    @Override
    public void output(IndentedWriter out)
    {
        serialize(out) ;
    }

    /** Convert the query to a string */
    
    public String serialize()
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        serialize(buff) ;
        return buff.toString();
    }
    
    /** Convert the query to a string in the given syntax
     * @param syntax
     */
    
    public String serialize(Syntax syntax)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        serialize(buff, syntax) ;
        return buff.toString();
    }

    /** Output the query
     * @param out  OutputStream
     */
    public void serialize(OutputStream out) { Serializer.serialize(this, out) ; }
    
    /** Output the query
     * 
     * @param out     OutputStream
     * @param syntax  Syntax URI
     */
    
    public void serialize(OutputStream out, Syntax syntax) { Serializer.serialize(this, out, syntax) ; }

    /** Format the query into the buffer
     * 
     * @param buff    IndentedLineBuffer
     */
    
    public void serialize(IndentedLineBuffer buff) { Serializer.serialize(this, buff) ; }
    
    /** Format the query
     * 
     * @param buff       IndentedLineBuffer in which to place the unparsed query
     * @param outSyntax  Syntax URI
     */
    
    public void serialize(IndentedLineBuffer buff, Syntax outSyntax) { Serializer.serialize(this, buff, outSyntax) ; }

    /** Format the query
     * 
     * @param writer  IndentedWriter
     */
    
    public void serialize(IndentedWriter writer) { Serializer.serialize(this, writer) ; }

    /** Format the query
     * 
     * @param writer     IndentedWriter
     * @param outSyntax  Syntax URI
     */
    
    public void serialize(IndentedWriter writer, Syntax outSyntax)
    {
        Serializer.serialize(this, writer, outSyntax) ;
    }
}
