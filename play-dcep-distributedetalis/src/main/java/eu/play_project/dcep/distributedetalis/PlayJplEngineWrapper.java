package eu.play_project.dcep.distributedetalis;

import java.util.Hashtable;

import jpl.Atom;
import jpl.Query;
import jpl.Term;

import com.jtalis.core.event.EtalisEventListener;
import com.jtalis.core.plengine.EngineOutputListener;
import com.jtalis.core.plengine.JPLEngineWrapper;
import com.jtalis.core.plengine.PrologEngineWrapper;

import eu.play_project.dcep.distributedetalis.api.PrologEngineWrapperPlayExtensions;

/**
 * To synchronize acces to JPL TODO synchronize it.
 * www.swi-prolog.org/packages/jpl/java_api/high-level_interface.html
 * 
 * Important: To execute goals related to SemWebLib use only
 * PrologEngineWrapperPlayExtensions.execute(String command). This method
 * guarantees exclusive access to db. (Only from Java side).
 * 
 * @author sobermeier
 * 
 */
public class PlayJplEngineWrapper implements PrologEngineWrapper, PrologEngineWrapperPlayExtensions {

	private JPLEngineWrapper engine;
	private static PlayJplEngineWrapper localEngine = new PlayJplEngineWrapper();
	
	private PlayJplEngineWrapper(){
		engine = new JPLEngineWrapper();
	}
	
	public static PlayJplEngineWrapper getPlayJplEngineWrapper(){
		return localEngine;
	}

	public synchronized Hashtable<String, Object>[] execute(String command) {
		Hashtable<String, Object>[] result;
		System.out.println("EngineWrapper Thread: " + Thread.currentThread().getId() + " Goal: " + command);
		// Get data from triplestore
		Query q = new Query(command);
		synchronized (this) {
			// Possibly faster and thread safe.
			result = q.allSolutions();
		}
		//q.close();
		return result;
	}
	
	@Override
	public boolean execute(com.jtalis.core.plengine.logic.Term term) {
		System.out.println("EngineWrapper Thread: " + Thread.currentThread().getId() + " Goal: " + term);
		synchronized(this){
			return engine.execute(term);
		}
	}

	@Override
	public boolean executeGoal(String goal) {
		System.out.println("EngineWrapper Thread: " + Thread.currentThread().getId() + " Goal: " + goal);
		//return engine.executeGoal(goal);
	synchronized(this){	
		Query q = new Query(goal);
		return q.hasSolution();
	}

		
	}

	@Override
	public Object registerPushNotification(EtalisEventListener listener) {
		synchronized (this) {
			return engine.registerPushNotification(listener);
		}	
	}

	@Override
	public void unregisterPushNotification(EtalisEventListener listener) {
		synchronized (this) {
			engine.unregisterPushNotification(listener);
		}
	}


	@Override
	public void shutdown() {
		engine.shutdown();
		
		//It is not possible to shutdown completly. We will clean up the database.
		//this.executeGoal("retractall(_)");
		this.executeGoal("rdf_retractall(_S,_P,_O,_DB)");
		this.executeGoal("reset_etalis");
	}

	@Override
	public void addOutputListener(EngineOutputListener listener) {
		synchronized (this) {
			engine.addOutputListener(listener);
		}
	}

	@Override
	public String getName() {
		return engine.getName();
	}

	@Override
	public Hashtable<String, Object>[] getTriplestoreData(String triplestoreID) {
		
		//Get data from triplestore
		Hashtable<String, Object>[] result = this.execute("rdfTest(S,P,O, " + triplestoreID + ")");
		
		// Free space
		this.executeGoal("retractall(rdfTest(_S,_P,_O, " + triplestoreID + "))");
		
		return result;
	}
	
	public Hashtable<String, Object>[] getVariableValues(String queryId){
		StringBuffer comand = new StringBuffer();

		comand.append("variableValues(");
		comand.append(queryId);
		comand.append(", _, Value)");
		
		// Get Variables and values
		Hashtable<String, Object>[] result = this.execute((comand.toString()));
		
		return result;
	}
	
	
	@Override
	public boolean consult(String file) {
	    Query consult_query =
	            new Query(
	                "consult",
	                new Term[] {new Atom(file)}
	            );
	    synchronized (this) {
	    	return consult_query.hasSolution();
		}
		
	}

	@Override
	public boolean assertFromFile(String file) {
		return false;
	}
}

	
