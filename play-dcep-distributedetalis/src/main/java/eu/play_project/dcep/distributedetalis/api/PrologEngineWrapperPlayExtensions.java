package eu.play_project.dcep.distributedetalis.api;

import java.util.Hashtable;

public interface PrologEngineWrapperPlayExtensions {
	
	public Hashtable[] getTriplestoreData(String TriplestoreID);
	
	/**
	 * Load prolog program from file.
	 * @param file Filename
	 * @return true = success
	 */
	public boolean consult(String file);
	public boolean assertFromFile(String file);
	public Hashtable<String, Object>[] execute(String command) throws DistributedEtalisException;
}
