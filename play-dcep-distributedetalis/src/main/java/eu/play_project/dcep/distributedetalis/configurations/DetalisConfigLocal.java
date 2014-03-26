package eu.play_project.dcep.distributedetalis.configurations;


import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import jpl.PrologException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.DistributedEtalis;
import eu.play_project.dcep.distributedetalis.EcConnectionManagerLocal;
import eu.play_project.dcep.distributedetalis.JtalisInputProvider;
import eu.play_project.dcep.distributedetalis.JtalisOutputProvider;
import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
import eu.play_project.dcep.distributedetalis.api.Configuration;
import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.dcep.distributedetalis.configurations.helpers.LoadPrologCode;
import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;



public class DetalisConfigLocal implements Configuration, Serializable{

	private static final long serialVersionUID = 100L;
	private List<String> inputRdfModelFile;
	private Logger logger;
	private MeasurementUnit measurementUnit;
	private LoadPrologCode cl;
	
	public  DetalisConfigLocal(){}
	
	public DetalisConfigLocal(List<String> inputRdfModelFile){
		this.inputRdfModelFile = inputRdfModelFile;
		
		logger = LoggerFactory.getLogger(this.getClass());
		logger.info("Configuring DistributedEtalis using {}", this.getClass().getSimpleName());
		
		cl = new LoadPrologCode();
	}
	
	public DetalisConfigLocal(String inputRdfModelFile){
		this.inputRdfModelFile = new LinkedList<String>();
		this.inputRdfModelFile.add(inputRdfModelFile);
		
		logger = LoggerFactory.getLogger(this.getClass());
		logger.info("Configuring DistributedEtalis using {}", this.getClass().getSimpleName());
		
		cl = new LoadPrologCode();
	}

	@Override
	public void configure(DEtalisConfigApi dEtalisConfigApi) throws DistributedEtalisException {
		
		// Init ETALIS
		PlayJplEngineWrapper engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
		JtalisContextImpl etalis = null;
		
		try {
			etalis = new JtalisContextImpl(engine);
			dEtalisConfigApi.setEtalis(etalis);
		} catch (Exception e) {
			logger.error("Error initializing ETALIS", e);
		}
		
		// Load Semantic Web Library
		PrologSemWebLib semWebLib = new PrologSemWebLib();
		dEtalisConfigApi.setSemWebLib(semWebLib);
		semWebLib.init(etalis);
		
		// Use measurement unit.
		measurementUnit = new MeasurementUnit((DistributedEtalis)dEtalisConfigApi, engine, semWebLib);
		
		dEtalisConfigApi.setMeasurementUnit(measurementUnit);
		dEtalisConfigApi.setEventInputProvider(new JtalisInputProvider(semWebLib));
		dEtalisConfigApi.setEcConnectionManager(new EcConnectionManagerLocal(inputRdfModelFile));
		dEtalisConfigApi.getEventSinks().add(dEtalisConfigApi.getEcConnectionManager());
		dEtalisConfigApi.setEventOutputProvider(new JtalisOutputProvider(
				dEtalisConfigApi.getEventSinks(),
				dEtalisConfigApi.getRegisteredQueries(),
				dEtalisConfigApi.getEcConnectionManager(),
				measurementUnit));

		dEtalisConfigApi.getEtalis().registerOutputProvider(dEtalisConfigApi.getEventOutputProvider());
		dEtalisConfigApi.getEtalis().registerInputProvider(dEtalisConfigApi.getEventInputProvider());

		try {
			cl.loadCode("ReferenceCounting.pl", engine);
			cl.loadCode("Aggregatfunktions.pl", engine);
			cl.loadCode("ComplexEventData.pl", engine);
			cl.loadCode("Measurement.pl", engine);
			cl.loadCode("Statistics.pl", engine);
			cl.loadCode("Helpers.pl", engine);
			cl.loadCode("Windows.pl", engine);
			cl.loadCode("Math.pl", engine);
		} catch (IOException e) {
			throw new DistributedEtalisException("It is not possible to load prolog code. IOException. " + e.getMessage());
		}catch(PrologException e){
			throw new DistributedEtalisException("It is not possible to load prolog code. PrologException. " + e.getMessage());
		}

		try {
			// Set ETALIS properties.
			etalis.setEtalisFlags("save_ruleId", "on");
			etalis.addEventTrigger("complex/_");
			etalis.addEventTrigger("realtimeResult/2");
			etalis.setEtalisFlags("event_consumption_policy","chronological");
			//etalis.setEtalisFlags("logging","on");
			etalis.setEtalisFlags("store_fired_events_java", "off");
			etalis.setEtalisFlags("garbage_control", "garbage_control");
			etalis.setEtalisFlags("garbage_window", "1");
			etalis.setEtalisFlags("garbage_window_step", "1");
		} catch (Exception e) {

			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	
		// Register event pattern.
		//Set new ID, but no complex event will be produced.
		//etalis.addDynamicRuleWithId("GarbageCollectionPattern", "complex <- gc(ID) where (setLastInsertedEvent(ID),false)");
	
	}
	
	
}
