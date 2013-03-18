package eu.play_project.dcep.distributedetalis.configurations;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.regex.Pattern;

import com.jtalis.core.JtalisContextImpl;

import eu.play_project.dcep.distributedetalis.EcConnectionManagerNet;
import eu.play_project.dcep.distributedetalis.JtalisInputProvider;
import eu.play_project.dcep.distributedetalis.JtalisOutputProvider;
import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
import eu.play_project.dcep.distributedetalis.api.Configuration;
import eu.play_project.dcep.distributedetalis.api.DEtalisConfigApi;
import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
import eu.play_project.play_commons.constants.Constants;

public class DetalisConfigNet implements Configuration, Serializable{

	private static final long serialVersionUID = 2565049949514271475L;

	@Override
	public void configure(DEtalisConfigApi dEtalisConfigApi) throws DistributedEtalisException {
		
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
		dEtalisConfigApi.setEcConnectionManager(new EcConnectionManagerNet(Constants
				.getProperties().getProperty("eventcloud.registry"), dEtalisConfigApi.getDistributedEtalis()));
		dEtalisConfigApi.getEventSinks().add(dEtalisConfigApi.getEcConnectionManager());
		dEtalisConfigApi.setEventOutputProvider(new JtalisOutputProvider(
				dEtalisConfigApi.getEventSinks(), dEtalisConfigApi.getRegisteredQueries(),
				dEtalisConfigApi.getEcConnectionManager()));

		dEtalisConfigApi.getEtalis().registerOutputProvider(dEtalisConfigApi.getEventOutputProvider());
		dEtalisConfigApi.getEtalis().registerInputProvider(dEtalisConfigApi.getEventInputProvider());

		//Load prolog methods.
		String[] methods = getPrologMethods("constructQueryImp.pl");
		for (int i = 0; i < methods.length; i++) {
			engine.execute("assert(" + methods[i] + ")");
		}

		methods = getPrologMethods("ReferenceCounting.pl");
		for (int i = 0; i < methods.length; i++) {
			engine.execute("assert(" + methods[i] + ")");
		}

		methods = getPrologMethods("Measurement.pl");
		for (int i = 0; i < methods.length; i++) {
			engine.execute("assert(" + methods[i] + ")");
		}
		
		methods = getPrologMethods("Math.pl");
		for (int i = 0; i < methods.length; i++) {
			engine.execute("assert(" + methods[i] + ")");
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
	}
	
	public static String[] getPrologMethods(String methodFile){
		try {
			InputStream is = DetalisConfigNet.class.getClassLoader().getResourceAsStream(methodFile);
			BufferedReader br =new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String line;
			
			while (null != (line = br.readLine())) {
				if (!(line.equals(" "))) {
					if (!line.startsWith("%")) { // Ignore comments
						sb.append(line);
					}
				}
			}
			br.close();
			is.close();
			
			String[] methods = sb.toString().split(Pattern.quote( "." ) );
			return methods;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	
	}
}
