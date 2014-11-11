package eu.play_project.dcep.distributedetalis.api;

import java.util.Map;
import java.util.Set;

import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.api.SimplePublishApi;
import eu.play_project.dcep.distributedetalis.DistributedEtalis;
import eu.play_project.dcep.distributedetalis.JtalisInputProvider;
import eu.play_project.dcep.distributedetalis.JtalisOutputProvider;
import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
import eu.play_project.dcep.node.api.DcepNodeConfiguringApi;
import eu.play_project.play_platformservices.api.BdplQuery;
import fr.inria.eventcloud.api.CompoundEvent;

/**
 * Set attributes used by a dEtalis instance. dEtalis can be configured with
 * different implementations of an attribute. E.g. different output methods.
 * 
 * @author Stefan Obermeier
 * 
 */
public interface DetalisConfiguringApi extends DcepNodeConfiguringApi<CompoundEvent> {
	
	/**
	 * Java interface for Etalis.
	 * @param etalis
	 */
	public void setEtalis(JtalisContextImpl etalis);
	public void setEventOutputProvider(JtalisOutputProvider eventOutputProvider);
	public void setEventInputProvider(JtalisInputProvider eventInputProvider);
	public void setMeasurementUnit(MeasurementUnit measurementUnit);
	public void setSemWebLib(PrologSemWebLib semWebLib);
	public DistributedEtalis getDistributedEtalis();
	public Map<String, BdplQuery> getRegisteredQueries();
	public Set<SimplePublishApi<CompoundEvent>> getEventSinks();
	public JtalisContextImpl getEtalis();
	public JtalisOutputProvider getEventOutputProvider();
	public JtalisInputProvider getEventInputProvider();

}
