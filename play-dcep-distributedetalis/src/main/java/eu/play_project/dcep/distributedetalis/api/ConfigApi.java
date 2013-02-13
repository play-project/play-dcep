package eu.play_project.dcep.distributedetalis.api;

import eu.play_project.dcep.distributedetalis.DistributedEtalisException;

/**
 * Set a configuration for a dEtalis instance.
 * 
 * @author Stefan Obermeier
 */
public interface ConfigApi {
	
	public void setConfig(Configuration configuration) throws DistributedEtalisException;
}
