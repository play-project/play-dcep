package eu.play_project.play_platformservices;

import javax.jws.WebService;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.bdpl.BaseDeclProcessor;
import org.openrdf.query.parser.bdpl.BlankNodeVarProcessor;
import org.openrdf.query.parser.bdpl.StringEscapesProcessor;
import org.openrdf.query.parser.bdpl.WildcardProjectionProcessor;
import org.openrdf.query.parser.bdpl.ast.ASTConstructQuery;
import org.openrdf.query.parser.bdpl.ast.ASTQuery;
import org.openrdf.query.parser.bdpl.ast.ASTQueryContainer;
import org.openrdf.query.parser.bdpl.ast.ParseException;
import org.openrdf.query.parser.bdpl.ast.SyntaxTreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.platformservices.querydispatcher.query.translate.EPLTranslationProcessor;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.QueryDetails;
import eu.play_project.play_platformservices.api.QueryDispatchException;

/**
 * The PLAY SOAP Web Service to manage event patterns. See
 * {@linkplain PlayPlatformservicesRest} for the corresponding RESTful service.
 * 
 * @author Roland Stühmer
 */
@WebService(
		serviceName = "QueryDispatchApi",
		portName = "QueryDispatchApiPort",
		endpointInterface = "eu.play_project.play_platformservices.api.QueryDispatchApi")
public class PlayPlatformservices extends AbstractPlatformservices {

	private static final long serialVersionUID = 100L;
	private final Logger logger = LoggerFactory.getLogger(PlayPlatformservices.class);

	@Override
	public synchronized void initialize() {
		if (!isInitialized()) {
			super.initialize();
		}
	}
	
	@Override
	public synchronized void destroy() {
		super.destroy();
	}
	
	@Override
	protected BdplQuery createQuery(String queryId, String query)
			throws QueryDispatchException {
		
		ASTQueryContainer qc = parseQuery(query);
		
		try {
			String eplQuery = EPLTranslationProcessor.process(qc);
			
			BdplQuery bdpl = BdplQuery.builder()
					.details(createQueryDetails(queryId, qc))
					.target(eplQuery)
					.historicalQueries(null) // FIXME stuehmer
					.constructTemplate(null) // FIXME stuehmer
					.bdpl(query)
					.build();
			
			return bdpl;
			
		} catch (MalformedQueryException e) {
			throw new QueryDispatchException(e);
		}


	}

	protected ASTQueryContainer parseQuery(String query) throws QueryDispatchException {
	
		try {
			ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(query);
			StringEscapesProcessor.process(qc);
			BaseDeclProcessor.process(qc, null);
			WildcardProjectionProcessor.process(qc);
			BlankNodeVarProcessor.process(qc);

			if (qc.containsQuery()) {
				ASTQuery queryNode = qc.getQuery();
				if (!(qc.getQuery() instanceof ASTConstructQuery)) {
					throw new QueryDispatchException("Unexpected query type: " + queryNode.getClass().getSimpleName());
				}
				else {
					return qc;
				}
			}
			else {
				throw new QueryDispatchException("Supplied string is not a query operation");
			}
		}
		catch (ParseException e){
			throw new QueryDispatchException(e);
		}
		catch (MalformedQueryException e) {
			throw new QueryDispatchException(e);
		}
	}

	protected QueryDetails createQueryDetails(String queryId, ASTQueryContainer qc) {
		if (!isInitialized()) {
			throw new IllegalStateException("Component not initialized: " + this.getClass().getSimpleName());
		}
		
		logger.info("Analysing query with ID '{}'", queryId);
		
		return QueryDetails.builder().id(queryId).build(); // FIXME stuehmer: add other properties
	}

	@Override
	public QueryDetails analyseQuery(String queryId, String query) throws QueryDispatchException {
		if (!isInitialized()) {
			throw new IllegalStateException("Component not initialized: "
					+ this.getClass().getSimpleName());
		}
		
		return createQueryDetails(queryId, parseQuery(query));
		
	}

}
