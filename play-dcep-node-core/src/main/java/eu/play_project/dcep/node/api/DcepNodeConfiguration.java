package eu.play_project.dcep.node.api;



/**
 * A class which implements this interface represents a configuration for a DCEP node.
 * 
 * @author Stefan Obermeier
 *
 */
public interface DcepNodeConfiguration {
	
	public void configure(DcepNodeConfiguration node) throws DcepNodeException;

}
