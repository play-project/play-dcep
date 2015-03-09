/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.realtime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.openrdf.query.IncompatibleOperationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.bdpl.BaseDeclProcessor;
import org.openrdf.query.parser.bdpl.BlankNodeVarProcessor;
import org.openrdf.query.parser.bdpl.PrefixDeclProcessor;
import org.openrdf.query.parser.bdpl.StringEscapesProcessor;
import org.openrdf.query.parser.bdpl.WildcardProjectionProcessor;
import org.openrdf.query.parser.bdpl.ast.ASTQueryContainer;
import org.openrdf.query.parser.bdpl.ast.ParseException;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.bdpl.ast.TokenMgrError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.platformservices.bdpl.parser.BDPLSyntaxCheckProcessor;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.util.TranslateException;


/**
 * @author ningyuan
 *
 */
public class EPLTranslator implements QueryParser{
	
	private static final Logger logger = LoggerFactory.getLogger(EPLTranslator.class);
	
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
				
				ParsedQuery query = null;
			
				String prologText = BDPLSyntaxCheckProcessor.process(qc);
				
				logger.debug(EPLTranslationProcessor.process(qc, prologText)+"\n");
				
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
		catch (TranslateException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}
	
	public static void main(String[] args)
			throws java.io.IOException
		{
		
		    EPLTranslator trans = new EPLTranslator();
		
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
