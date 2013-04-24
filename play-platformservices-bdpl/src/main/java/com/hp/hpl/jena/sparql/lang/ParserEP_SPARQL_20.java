package com.hp.hpl.jena.sparql.lang;

import java.io.Reader;
import java.io.StringReader;

import org.openjena.atlas.logging.Log;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.lang.EPSPARQL_20.SPARQLParserEP_SPARQL_20;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.Template;

public class ParserEP_SPARQL_20 extends ParserSPARQL11{
	private interface Action { void exec(SPARQLParserEP_SPARQL_20 parser) throws Exception ; }

	    
	    @Override
	    protected Query parse$(final Query query, String queryString)
	    {
	    	query.setSyntax(Syntax.syntaxEPSPARQL_20);

	        Action action = new Action() {
	            @Override
	            public void exec(SPARQLParserEP_SPARQL_20 parser) throws Exception
	            {
	                parser.QueryUnit() ;
	            }
	        } ;

	        perform(query, queryString, action) ;
	        validateParsedQuery(query) ;
	        return query ;
	    }
	    
	    public static Element parseElement(String string)
	    {
	        final Query query = new Query () ;
	        Action action = new Action() {
	            @Override
	            public void exec(SPARQLParserEP_SPARQL_20 parser) throws Exception
	            {
	                Element el = parser.GroupGraphPattern() ;
	                query.setQueryPattern(el) ;
	            }
	        } ;
	        perform(query, string, action) ;
	        return query.getQueryPattern() ;
	    }
	    
	    public static Template parseTemplate(String string)
	    {
	        final Query query = new Query () ;
	        Action action = new Action() {
	            @Override
	            public void exec(SPARQLParserEP_SPARQL_20 parser) throws Exception
	            {
	                Template t = parser.ConstructTemplate() ;
	                query.setConstructTemplate(t) ;
	            }
	        } ;
	        perform(query, string, action) ;
	        return query.getConstructTemplate() ;
	    }
	    
	    
	    // All throwable handling.
	    private static void perform(Query query, String string, Action action)
	    {
	        Reader in = new StringReader(string) ;
	        SPARQLParserEP_SPARQL_20 parser = new SPARQLParserEP_SPARQL_20(in) ;

	        try {
	            query.setStrict(true) ;
	            parser.setQuery(query) ;
	            action.exec(parser) ;
	        }
	        catch (com.hp.hpl.jena.sparql.lang.sparql_11.ParseException ex)
	        { 
	            throw new QueryParseException(ex.getMessage(),
	                                          ex.currentToken.beginLine,
	                                          ex.currentToken.beginColumn
	                                          ) ; }
	        catch (com.hp.hpl.jena.sparql.lang.sparql_11.TokenMgrError tErr)
	        {
	            // Last valid token : not the same as token error message - but this should not happen
	            int col = parser.token.endColumn ;
	            int line = parser.token.endLine ;
	            throw new QueryParseException(tErr.getMessage(), line, col) ; }
	        
	        catch (QueryException ex) { throw ex ; }
	        catch (JenaException ex)  { throw new QueryException(ex.getMessage(), ex) ; }
	        catch (Error err)
	        {
	            // The token stream can throw errors.
	            throw new QueryParseException(err.getMessage(), err, -1, -1) ;
	        }
	        catch (Throwable th)
	        {
	            Log.warn(ParserSPARQL11.class, "Unexpected throwable: ",th) ;
	            throw new QueryException(th.getMessage(), th) ;
	        }
	    }
}
