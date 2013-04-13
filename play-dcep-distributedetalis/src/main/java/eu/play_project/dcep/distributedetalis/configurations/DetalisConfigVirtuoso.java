package eu.play_project.dcep.distributedetalis.configurations;

import java.io.IOException;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.EcConnectionManagerVirtuoso;
import eu.play_project.dcep.distributedetalis.JtalisInputProvider;
import eu.play_project.dcep.distributedetalis.JtalisOutputProvider;
import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
import eu.play_project.dcep.distributedetalis.api.Configuration;
import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.configurations.helpers.LoadPrologCode;

public class DetalisConfigVirtuoso extends DetalisConfigNet implements Configuration, Serializable{

	private static final long serialVersionUID = 5249777449637212881L;
	private Logger logger;
	private static LoadPrologCode cl;

	@Override
	public void configure(DEtalisConfigApi dEtalisConfigApi) throws DistributedEtalisException {
		
		logger = LoggerFactory.getLogger(DetalisConfigVirtuoso.class);
		cl = new LoadPrologCode();
		
		try {
			// Init ETALIS
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
	
			dEtalisConfigApi.setEcConnectionManager(new EcConnectionManagerVirtuoso(dEtalisConfigApi.getDistributedEtalis()));
			dEtalisConfigApi.getEventSinks().add(dEtalisConfigApi.getEcConnectionManager());
			dEtalisConfigApi.setEventOutputProvider(new JtalisOutputProvider(
					dEtalisConfigApi.getEventSinks(), dEtalisConfigApi.getRegisteredQueries(),
					dEtalisConfigApi.getEcConnectionManager()));
	
			dEtalisConfigApi.getEtalis().registerOutputProvider(dEtalisConfigApi.getEventOutputProvider());
			dEtalisConfigApi.getEtalis().registerInputProvider(dEtalisConfigApi.getEventInputProvider());
			
			//Load prolog methods.
			try {
				cl.loadCode("ReferenceCounting.pl", engine);
				cl.loadCode("Aggregatfunktions.pl", engine);
				cl.loadCode("ComplexEventData.pl", engine);
				cl.loadCode("Measurement.pl", engine);
				cl.loadCode("Statistics.pl", engine);
				cl.loadCode("Math.pl", engine);
			} catch (IOException e) {
				logger.error("It is not possible to load prolog code. " + e.getMessage());
				e.printStackTrace();
			}catch(Exception e){
				logger.error("It is not possible to load prolog code. " + e.getMessage());
				e.printStackTrace();
			}

	
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
		} catch (DistributedEtalisException e) {
			throw new DistributedEtalisException("Error configuring DistributedEtalis: " + e.getMessage(), e);
		}
	}
}
