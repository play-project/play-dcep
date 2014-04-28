/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.translate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;


import java.util.Set;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Dataset;
import org.openrdf.query.IncompatibleOperationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.sparql.BaseDeclProcessor;
import org.openrdf.query.parser.sparql.BlankNodeVarProcessor;
import org.openrdf.query.parser.sparql.DatasetDeclProcessor;
import org.openrdf.query.parser.sparql.PrefixDeclProcessor;
import org.openrdf.query.parser.sparql.StringEscapesProcessor;
import org.openrdf.query.parser.sparql.TupleExprBuilder;
import org.openrdf.query.parser.sparql.WildcardProjectionProcessor;
import org.openrdf.query.parser.sparql.ast.ASTAskQuery;
import org.openrdf.query.parser.sparql.ast.ASTConstructQuery;
import org.openrdf.query.parser.sparql.ast.ASTDescribeQuery;
import org.openrdf.query.parser.sparql.ast.ASTPrefixDecl;
import org.openrdf.query.parser.sparql.ast.ASTQuery;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTSelectQuery;
import org.openrdf.query.parser.sparql.ast.Node;
import org.openrdf.query.parser.sparql.ast.ParseException;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.query.parser.sparql.ast.VisitorException;


/**
 * @author ningyuan
 *
 */
public class BDPLTranslator implements QueryParser{
	
	/* (non-Javadoc)
	 * @see org.openrdf.query.parser.QueryParser#parseUpdate(java.lang.String, java.lang.String)
	 */
	@Override
	public ParsedUpdate parseUpdate(String updateStr, String baseURI)
			throws MalformedQueryException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openrdf.query.parser.QueryParser#parseQuery(java.lang.String, java.lang.String)
	 */
	@Override
	public ParsedQuery parseQuery(String queryStr, String baseURI)
			throws MalformedQueryException {
		try {
			ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(queryStr);
			StringEscapesProcessor.process(qc);
			BaseDeclProcessor.process(qc, baseURI);
			Map<String, String> prefixes = PrefixDeclProcessor.process(qc);
			WildcardProjectionProcessor.process(qc);
			BlankNodeVarProcessor.process(qc);

			if (qc.containsQuery()) {

				// handle query operation

				TupleExpr tupleExpr = buildQueryModel(qc);
				
				ParsedQuery query;
			
				ASTQuery queryNode = qc.getQuery();
				if (queryNode instanceof ASTSelectQuery) {
						
					query = new ParsedTupleQuery(queryStr, tupleExpr);
				}
				else if (queryNode instanceof ASTConstructQuery) {
					
					query = new ParsedGraphQuery(queryStr, tupleExpr, prefixes);
				}
				else if (queryNode instanceof ASTAskQuery) {
					
					query = new ParsedBooleanQuery(queryStr, tupleExpr);
				}
				else if (queryNode instanceof ASTDescribeQuery) {
					
					query = new ParsedGraphQuery(queryStr, tupleExpr, prefixes);
				}
				else {
					throw new RuntimeException("Unexpected query type: " + queryNode.getClass());
				}

				// Handle dataset declaration
				Dataset dataset = DatasetDeclProcessor.process(qc);
				if (dataset != null) {
					query.setDataset(dataset);
				}
				
				EPLProcessor.process(qc);
				
				//System.out.println("\nDataset "+query.getDataset().toString());
				//System.out.println("\nTuple "+query.getTupleExpr().toString());
				
				return query;
			}
			else {
				throw new IncompatibleOperationException("supplied string is not a query operation");
			}
		}
		catch (ParseException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
		catch (TokenMgrError e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}
	
	private TupleExpr buildQueryModel(Node qc)
			throws MalformedQueryException
		{
			TupleExprBuilder tupleExprBuilder = new TupleExprBuilder(new ValueFactoryImpl());
			try {
				return (TupleExpr)qc.jjtAccept(tupleExprBuilder, null);
			}
			catch (VisitorException e) {
				throw new MalformedQueryException(e.getMessage(), e);
			}
		}
	
	public static void main(String[] args)
			throws java.io.IOException
		{
		
		    BDPLTranslator trans = new BDPLTranslator();
		
			System.out.println("Your BDPL query:");

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			StringBuilder buf = new StringBuilder();
			String line = null;
			
			int emptyLineCount = 0;
			while ((line = in.readLine()) != null) {
				if (line.length() > 0) {
					emptyLineCount = 0;
					buf.append(' ').append(line).append('\n');
				}
				else {
					emptyLineCount++;
				}

				if (emptyLineCount == 2) {
					emptyLineCount = 0;
					String queryStr = buf.toString().trim();
					if (queryStr.length() > 0) {
						try {
							/*ParsedQuery parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, queryStr, null);*/
							
							
							/*QueryParserFactory factory = new BDPLParserFactory();
							ParsedQuery parsedQuery = factory.getParser().parseQuery(queryStr, null);*/
							
							ParsedQuery parsedQuery = trans.parseQuery(queryStr, null);
							
							System.out.println("\n\nTransformed query: ");
							System.out.println(parsedQuery.toString());
							System.out.println();

						}
						catch (Exception e) {
							System.err.println(e.getMessage());
							e.printStackTrace();
						}
					}
					buf.setLength(0);
				}
			}
		}
}
