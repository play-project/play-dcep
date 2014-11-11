package eu.play_project.play_platformservices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.serializer.PlaySerializer;

import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;
import eu.play_project.dcep.api.DcepManagmentApi;
import eu.play_project.play_platformservices.api.BdplQuery;
import eu.play_project.play_platformservices.api.QueryDetailsEtalis;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.historic.QueryTemplateGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.ComplexTypeFinder;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.StreamIdCollector;
import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.WindowVisitor;

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
public class PlayPlatformservices extends AbstractPlatformservices implements ComponentInitActive, ComponentEndActive, BindingController {

	private static final long serialVersionUID = 100L;
	private EleGenerator eleGenerator;
	private final Logger logger = LoggerFactory.getLogger(PlayPlatformservices.class);

	@Override
	public String[] listFc() {
		return new String[] { DcepManagmentApi.class.getSimpleName() };
	}

	@Override
	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
		if (DcepManagmentApi.class.getSimpleName().equals(clientItfName)) {
			return this.getDcepManagmentApi();
		} else {
			throw new NoSuchInterfaceException(DcepManagmentApi.class.getSimpleName());
		}
	}

	@Override
	public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if (clientItfName.equals(DcepManagmentApi.class.getSimpleName())) {
			this.setDcepManagmentApi((DcepManagmentApi) serverItf);
		}
		else {
			throw new NoSuchInterfaceException(String.format("Interface '%s' not available at '%s'.", clientItfName, this.getClass().getSimpleName()));
		}
	}

	@Override
	public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if (clientItfName.equals(DcepManagmentApi.class.getSimpleName())) {
			// do nothing, currently
		}
		else {
			throw new NoSuchInterfaceException(String.format("Interface '%s' not available at '%s'.", clientItfName, this.getClass().getSimpleName()));
		}
	}

	@Override
	public synchronized void initComponentActivity(Body body) {
		if (!isInitialized()) {
			
			this.eleGenerator = new EleGeneratorForConstructQuery();
	
			super.initialize();
		}
	}
	
	@Override
	public synchronized void endComponentActivity(Body arg0) {
		super.destroy();
	}
	

	@Override
	protected BdplQuery createQuery(String queryId, String query)
			throws QueryDispatchException {
		// Parse query
		Query q;
		try {
			q = QueryFactory.create(query, Syntax.syntaxBDPL);
		} catch (com.hp.hpl.jena.query.QueryException e) {
			throw new QueryDispatchException(e.getMessage());
		}

		// Generate CEP-language
		eleGenerator.setPatternId(queryId);
		eleGenerator.generateQuery(q);

		// Add queryDetails
		QueryDetailsEtalis qd = this.createQueryDetails(queryId, q);
		qd.setRdfDbQueries(eleGenerator.getRdfDbQueries());
		
		BdplQuery bdpl = BdplQuery.builder()
				.details(qd)
				.target(eleGenerator.getEle())
				.historicalQueries(PlaySerializer.serializeToMultipleSelectQueries(q))
				.constructTemplate(new QueryTemplateGenerator().createQueryTemplate(q))
				.bdpl(query)
				.build();

		return bdpl;
	}

	@Override
	public QueryDetailsEtalis analyseQuery(String queryId, String query) throws QueryDispatchException {
		if (!isInitialized()) {
			throw new IllegalStateException("Component not initialized: "
					+ this.getClass().getSimpleName());
		}
		
		// Parse query
		try {
			Query q = QueryFactory.create(query, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
			return createQueryDetails(queryId, q);
		}
		catch (QueryDispatchException e) {
			throw new QueryDispatchException(e.getMessage());
		}
		
	}
	
	protected QueryDetailsEtalis createQueryDetails(String queryId, Query query) throws QueryDispatchException {
		if (!isInitialized()) {
			throw new IllegalStateException("Component not initialized: " + this.getClass().getSimpleName());
		}
		
		logger.info("Analysing query with ID '{}'", queryId);
		
		QueryDetailsEtalis qd = new QueryDetailsEtalis(queryId);
	
		// Set properties for windows in QueryDetails
		ElementWindowVisitor windowVisitor = new WindowVisitor(qd);
		query.getWindow().accept(windowVisitor);
		
		// Check if id is alredy used.
		if (this.getDcepManagmentApi() != null && this.getDcepManagmentApi().getRegisteredEventPatterns().containsKey(queryId)) {
			throw new QueryDispatchException("Query ID is already used: " + queryId);
		}
	
		// Set stream ids in QueryDetails.
		StreamIdCollector streamIdCollector = new StreamIdCollector();
		streamIdCollector.getStreamIds(query, qd);
		
		// Set complex event type.
		qd.setComplexType((new ComplexTypeFinder()).visit(query.getConstructTemplate()));
	
		return qd;
	}

	@Override
	public List<eu.play_project.play_platformservices.jaxb.Query> getRegisteredQueries() {
		if (!isInitialized()) {
			throw new IllegalStateException("Component not initialized: "
					+ this.getClass().getSimpleName());
		}

		List<eu.play_project.play_platformservices.jaxb.Query> results = new ArrayList<eu.play_project.play_platformservices.jaxb.Query>();

		Map<String, BdplQuery> queries = this.getDcepManagmentApi().getRegisteredEventPatterns();

		for (String queryId : queries.keySet()) {
			results.add(new eu.play_project.play_platformservices.jaxb.Query(queries.get(queryId)));
		}

		return results;
	}

}
