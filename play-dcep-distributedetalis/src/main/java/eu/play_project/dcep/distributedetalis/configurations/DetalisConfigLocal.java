package eu.play_project.dcep.distributedetalis.configurations;


import java.io.IOException;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.DistributedEtalis;
import eu.play_project.dcep.distributedetalis.JtalisInputProvider;
import eu.play_project.dcep.distributedetalis.JtalisOutputProvider;
import eu.play_project.dcep.distributedetalis.EcConnectionManagerLocal;
import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
import eu.play_project.dcep.distributedetalis.api.Configuration;
import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;
import eu.play_project.dcep.distributedetalis.configurations.helpers.LoadPrologCode;



public class DetalisConfigLocal implements Configuration, Serializable{

	private static final long serialVersionUID = 1L;
	private String inputRdfModelFile;
	private Logger logger;
	private static transient LoadPrologCode cl;
	
	public  DetalisConfigLocal(){}
	
	public DetalisConfigLocal(String inputRdfModelFile){
		this.inputRdfModelFile = inputRdfModelFile;
		logger = LoggerFactory.getLogger(DetalisConfigLocal.class);
		cl = new LoadPrologCode();
	}

	@Override
	public void configure(DEtalisConfigApi dEtalisConfigApi) {
		
		// Init ETALIS
		PlayJplEngineWrapper engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
		JtalisContextImpl etalis = null;
		
		try {
			etalis = new JtalisContextImpl(engine);
			dEtalisConfigApi.setEtalis(etalis);
		} catch (Exception e) {
			dEtalisConfigApi.getLogger().error("Error initializing ETALIS", e);
		}
		
		// Load Semantic Web Library
		PrologSemWebLib semWebLib = new PrologSemWebLib();
		dEtalisConfigApi.setSemWebLib(semWebLib);
		semWebLib.init(etalis);
		
		dEtalisConfigApi.setEventInputProvider(new JtalisInputProvider(semWebLib));
		dEtalisConfigApi.setEcConnectionManager(new EcConnectionManagerLocal(inputRdfModelFile));
		dEtalisConfigApi.getEventSinks().add(dEtalisConfigApi.getEcConnectionManager());
		dEtalisConfigApi.setEventOutputProvider(new JtalisOutputProvider(
		dEtalisConfigApi.getEventSinks(), dEtalisConfigApi.getRegisteredQueries(),
		dEtalisConfigApi.getEcConnectionManager()));

		dEtalisConfigApi.getEtalis().registerOutputProvider(dEtalisConfigApi.getEventOutputProvider());
		dEtalisConfigApi.getEtalis().registerInputProvider(dEtalisConfigApi.getEventInputProvider());


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
		// etalis.setEtalisFlags("logging","on");
		// etalis.setEtalisFlags("java_notification","on");


		// Instatiate measurement unit.
		// this.measurementUnit = new MeasurementUnit(this);	
		
		// Register event pattern.
		//Set new ID, but no complex event will be produced.
		//etalis.addDynamicRuleWithId("GarbageCollectionPattern", "complex <- gc(ID) where (setLastInsertedEvent(ID),false)");
	}
	
	
}
