//package eu.play_project.distribution.tests;
//
//import java.util.HashMap;
//
//import org.etsi.uri.gcm.util.GCM;
//import org.junit.Test;
//import org.objectweb.fractal.adl.ADLException;
//import org.objectweb.fractal.adl.Factory;
//import org.objectweb.fractal.api.Component;
//import org.objectweb.fractal.api.NoSuchInterfaceException;
//import org.objectweb.fractal.api.control.IllegalLifeCycleException;
//import org.objectweb.proactive.core.component.adl.FactoryFactory;
//import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
//
//import com.hp.hpl.jena.graph.Node;
//
//import eu.play_project.play_platformservices_querydispatcher.api.QueryDispatchApi;
//import fr.inria.eventcloud.api.Collection;
//import fr.inria.eventcloud.api.CompoundEvent;
//import fr.inria.eventcloud.api.Event;
//import fr.inria.eventcloud.api.PublishApi;
//import fr.inria.eventcloud.api.Quadruple;
//
//public class PlatformTest {
//
//	//@Test
//	public void instantiate() throws ADLException, IllegalLifeCycleException, NoSuchInterfaceException, InterruptedException{
//		 CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue("proactive.java.policy");
//			//CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue(System.getProperty("user.dir")+ "\\src\\main\\resources\\proactive.java.policy");
//			CentralPAPropertyRepository.GCM_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");
//			//setProAktiveHome();
//
//
//			Factory factory = FactoryFactory.getFactory();
//			HashMap<String, Object> context = new HashMap<String, Object>();
//			
//
//
//			Component root = (Component) factory.newComponent("PLAY-Platform", context);
//			GCM.getGCMLifeCycleController(root).startFc();
//			
//			// Get interfaces
//			PublishApi publishApi = (fr.inria.eventcloud.api.PublishApi) root.getFcInterface("PublishApi");
//			QueryDispatchApi queryDispatchApi = ((eu.play_project.play_platformservices_querydispatcher.api.QueryDispatchApi) root.getFcInterface("QueryDispatchApi"));
//			//DcepSubscribeApi ecSubscribeApi = (fr.inria.eventcloud.api.SubscribeApi)root.getFcInterface("DcepSubscribeApi");
//			// Debug2 db2 = (eu.play_project.dcep.Debug2)root.getFcInterface("Debug2");
//			
//			Thread.sleep(4000);
//			// System.out.println("Schreibe mich ein");
//			// db2.start();
//			// System.out.println("Habe mich eingeschrieben");
//			// Thread.sleep(40000);
//			//Define query for CEP.
//			String prefix = "PREFIX : <http://events.event-processing.org/ids/>" +
//							"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " ;
//			String queryString = prefix + 
//				"CONSTRUCT{ ?e1 ?s ?p }" + 
//				" WHERE " +
//						"EVENT ?ID1 {?e1 ?s ?p. }" +
//									
//						"SEQ " +
//						"EVENT ?ID2 {?S1 ?O2 ?ID2} ";
//			
//			// Compile Query
//			System.out.println(queryString);
//			//queryDispatchApi.registerQuery(queryString);
//			queryDispatchApi.registerQuery(queryString);
//			Thread.sleep(1000);
//						
//			
//	
//			//Generate events.
//			Collection<Quadruple> quadruple = new Collection<Quadruple>();
//			Quadruple q1 = new Quadruple(
//	                Node.createURI("http://play-project.eu/id_4711"),
//	                Node.createURI("http://play-project.eu/Nice"),
//	                Node.createURI("http://play-project.eu/avgTempEvent"),
//	                Node.createURI("http://play-project.eu/42"));
//					
//			Quadruple q2 = new Quadruple(
//	                Node.createURI("http://play-project.eu/id_4712"),
//	                Node.createURI("http://play-project.eu/default_prefix/e1"),
//	                Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
//	                Node.createURI("http://play-project.eu/default_prefix/myType"));
//			
//			
//			publishApi.publish(new CompoundEvent(q1));
//			publishApi.publish(new CompoundEvent (q2));
//			Thread.sleep(9000);
////			try{
////			ecSubscribeApi.subscribe("SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }", PAActiveObject.newActive(BindingNotificationListenerImpl.class, null));
////	} catch (ActiveObjectCreationException e) {
////		// TODO Auto-generated catch block
////		e.printStackTrace();
////	} catch (NodeException e) {
////		// TODO Auto-generated catch block
////		e.printStackTrace();
////	} //Copy
//	}
//}