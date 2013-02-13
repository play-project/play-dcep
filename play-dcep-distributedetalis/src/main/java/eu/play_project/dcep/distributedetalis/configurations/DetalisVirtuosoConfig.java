package eu.play_project.dcep.distributedetalis.configurations;

import java.io.Serializable;

import javax.naming.NamingException;

import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.EcConnectionManagerVirtuoso;
import eu.play_project.dcep.distributedetalis.JtalisInputProvider;
import eu.play_project.dcep.distributedetalis.JtalisOutputProvider;
import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
import eu.play_project.dcep.distributedetalis.api.Configuration;
import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;

public class DetalisVirtuosoConfig implements Configuration, Serializable{

	private static final long serialVersionUID = 5249777449637212881L;

	@Override
	public void configure(DEtalisConfigApi dEtalisConfigApi) throws DistributedEtalisException {
		
		try {		// Init ETALIS
			PlayJplEngineWrapper engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
			JtalisContextImpl etalis = null;
			
			try {
				etalis = new JtalisContextImpl(engine);
				dEtalisConfigApi.setEtalis(etalis);
			} catch (Exception e) {
				dEtalisConfigApi.getLogger().error("Error initializing ETALIS", e);
				throw new DistributedEtalisException("Error initializing ETALIS", e);
			}
			
			// Load Semantic Web Library
			PrologSemWebLib semWebLib = new PrologSemWebLib();
			dEtalisConfigApi.setSemWebLib(semWebLib);
			semWebLib.init(etalis);
			
			dEtalisConfigApi.setEventInputProvider(new JtalisInputProvider(semWebLib));
	
			dEtalisConfigApi.setEcConnectionManager(new EcConnectionManagerVirtuoso());
			dEtalisConfigApi.getEventSinks().add(dEtalisConfigApi.getEcConnectionManager());
			dEtalisConfigApi.setEventOutputProvider(new JtalisOutputProvider(
					dEtalisConfigApi.getEventSinks(), dEtalisConfigApi.getRegisteredQuerys(),
					dEtalisConfigApi.getEcConnectionManager()));
	
			dEtalisConfigApi.getEtalis().registerOutputProvider(dEtalisConfigApi.getEventOutputProvider());
			dEtalisConfigApi.getEtalis().registerInputProvider(dEtalisConfigApi.getEventInputProvider());
	
			// Set ETALIS properties.
			etalis.setEtalisFlags("save_ruleId", "on");
			etalis.addEventTrigger("complex/_");
			etalis.addEventTrigger("realtimeResult/2");
			// etalis.setEtalisFlags("event_consumption_policy",
			// "chronological");
			// etalis.setEtalisFlags("logging","off");
			// etalis.setEtalisFlags("java_notification","on");
	
	
			// Instatiate measurement unit.
			// this.measurementUnit = new MeasurementUnit(this,	
		} catch (NamingException e) {
			throw new DistributedEtalisException("Error configuring DistributesEtalis: " + e.getMessage(), e);
		}
	}
}
