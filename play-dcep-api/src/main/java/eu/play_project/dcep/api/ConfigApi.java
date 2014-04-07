package eu.play_project.dcep.api;

/**
 * Set a configuration for a dEtalis instance.
 * 
 * @author Stefan Obermeier
 */
public interface ConfigApi {
	
	public void setConfig(String middleware) throws DcepManagementException;
	public void setConfigLocal(String rdfFile) throws DcepManagementException;
}
