package eu.play_project.dcep.distribution.tests;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

import eu.play_project.dcep.api.DcepTestApi;
import eu.play_project.dcep.constants.DcepConstants;
import eu.play_project.dcep.distributedetalis.utils.ProActiveHelpers;
import eu.play_project.dcep.distributedetalis.utils.PrologHelpers;
import eu.play_project.play_commons.constants.Namespace;
import eu.play_project.play_platformservices.api.QueryDispatchApi;
import eu.play_project.play_platformservices.api.QueryDispatchException;
import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;

public class RegisterQueriesTest {
	public static QueryDispatchApi queryDispatchApi;
	public static DcepTestApi testApi;
	boolean start = false;
	static Component root;
	public static boolean test;
	private final org.slf4j.Logger logger = LoggerFactory.getLogger(RegisterQueriesTest.class);
	
	/**
	 * Load pattern which are loaded at DCEP startup.
	 */
	@Ignore
	@Test
	public void registerCommonPatterns() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException{
		
		InstantiatePlayPlatform();
		for (String queryFileName : DcepConstants.getProperties().getProperty("dcep.startup.registerqueries").split(",")) {
			queryFileName = queryFileName.trim();
			String	queryString = getSparqlQueries(queryFileName);

			try {
				queryDispatchApi.registerQuery("urn:bdpl:" + queryFileName, queryString);
			} catch (QueryDispatchException e) {
				System.out.println(("Error registering query " + queryFileName + " on startup: " + e.getMessage()));
			}
		}
	}
	
	@Ignore
	@Test
	public void parsSRBenchQueries(){
		Query query = QueryFactory.create(getSparqlQueries("benchmarks/srbench/q2.eprq"), Syntax.syntaxBDPL);
		
		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
		
		query.setQueryId(PrologHelpers.quoteForProlog(Namespace.PATTERN.getUri() + "123456"));

		visitor1.generateQuery(query);
		String etalisPattern = visitor1.getEle();

		System.out.println(etalisPattern);
	}
	public static void InstantiatePlayPlatform()
			throws IllegalLifeCycleException, NoSuchInterfaceException,
			ADLException {

		root = ProActiveHelpers.newComponent("DcepPsTest");
		GCM.getGCMLifeCycleController(root).startFc();

		queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root.getFcInterface(QueryDispatchApi.class.getSimpleName()));
		testApi = (DcepTestApi) root.getFcInterface(DcepTestApi.class.getSimpleName());
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private String getSparqlQueries(String queryFile){
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(queryFile);
			BufferedReader br =new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String line;
			
			while (null != (line = br.readLine())) {
					sb.append(line);
					sb.append("\n");
			}
			//System.out.println(sb.toString());
			br.close();
			is.close();

			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	
	}
	
	private void delay(){
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
