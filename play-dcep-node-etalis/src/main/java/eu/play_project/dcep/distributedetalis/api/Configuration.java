package eu.play_project.dcep.distributedetalis.api;

import eu.play_project.dcep.node.api.DcepNodeConfiguration;
import eu.play_project.dcep.node.api.DcepNodeException;

/**
 * A class which implements this interface represents a configuration of dEtalis.
 * @author Stefan Obermeier
 *
 */
public interface Configuration extends DcepNodeConfiguration {
	
	public void configure(DetalisConfiguringApi detalisConfiguringApi) throws DcepNodeException;

}
