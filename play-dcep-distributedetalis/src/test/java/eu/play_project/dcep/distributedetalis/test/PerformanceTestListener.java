package eu.play_project.dcep.distributedetalis.test;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;

public class PerformanceTestListener implements PublishApi, Serializable{

	private static final long serialVersionUID = 1L;
	private long t1;
	ArrayList<CompoundEvent> complexEvents = new ArrayList<CompoundEvent>();
	long counter = -1;
	int numberOfExpectedComplexEvents = 20000;
	int givenRepetitions = 0;
	boolean firstTime = true;
	long timeTmp = 0;
	static String filename;
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public PerformanceTestListener(){}
	
	public void setGivenRepetitions(int givenRepetitions) {
		this.givenRepetitions = givenRepetitions;
	}

	
	public ArrayList<CompoundEvent> getComplexEvents() {
		return complexEvents;
	}

	@Override
	public void publish(CompoundEvent arg0) {
		// logger.info(arg0);
		counter++;
		if (((counter) % (1000) )== 0) {
			logger.info((counter)  + " " + (System.currentTimeMillis()-timeTmp));
			timeTmp = System.currentTimeMillis();
		}
	
		// logger.info("Subscriber got complex event: " + counter);
		arg0 = null;
		//if (counter >= numberOfExpectedComplexEvents) {
			//long t2 = System.currentTimeMillis();
			//logger.info(t2 - t1);
			//appendToFile("" + (t2 - t1) + ";");
			//logger.info("Counter: " + counter);
		//}
	}

	@Override
	public void publish(Quadruple arg0) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void publish(Collection<CompoundEvent> arg0) {
		throw new RuntimeException("Not implemented");
	}


	public void setT1(long t1) {
		this.t1 = t1;
	}

	public void setNumberofExpectedComplexEvents(int number) {
		numberOfExpectedComplexEvents = number;
		logger.info("NumberOfExpectedComplexEvents:" + number);
	}

	public void setOutputFile(String name) {
		filename = name;
	}

	public static void appendToFile(String text) {
		try {

			FileWriter writer = new FileWriter(filename,/* append */true);

			writer.write(text);

			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void publish(InputStream arg0, SerializationFormat arg1) {
		// TODO Auto-generated method stub
		
	}

}
