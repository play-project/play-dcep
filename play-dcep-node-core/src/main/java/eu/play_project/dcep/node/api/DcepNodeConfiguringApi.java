package eu.play_project.dcep.node.api;

import eu.play_project.dcep.api.DcepManagementException;


public interface DcepNodeConfiguringApi<EventType> {

	public EcConnectionManager<EventType> getEcConnectionManager();

	/**
	 * Complex events are delivered to ecConnectionManger. With this method it
	 * is possible to set a special version e.g. one which needs no internet
	 * connection.
	 * 
	 * @param ecConnectionManager
	 *            Implementation of EcConnectionManger interface.
	 */
	public void setEcConnectionManager(EcConnectionManager<EventType> ecConnectionManager);

	public void setConfig(String middleware) throws DcepManagementException;

}
