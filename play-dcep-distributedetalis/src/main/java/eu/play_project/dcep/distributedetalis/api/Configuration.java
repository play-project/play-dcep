package eu.play_project.dcep.distributedetalis.api;

import java.io.Serializable;

import eu.play_project.dcep.distributedetalis.DistributedEtalisException;


/**
 * A class which implements this interface represents a configuration of dEtalis.
 * @author Stefan Obermeier
 *
 */
public interface Configuration {
	
	public void configure(DEtalisConfigApi dEtalisConfigApi) throws DistributedEtalisException;

}
