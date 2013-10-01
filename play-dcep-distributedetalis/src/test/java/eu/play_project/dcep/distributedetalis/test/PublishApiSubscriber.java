package eu.play_project.dcep.distributedetalis.test;

import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
import fr.inria.eventcloud.api.CompoundEvent;

public class PublishApiSubscriber implements SimplePublishApi, Serializable {

	private static final long serialVersionUID = 100L;
	private static final ArrayList<CompoundEvent> complexEvents = new ArrayList<CompoundEvent>();
	private final Logger logger = LoggerFactory.getLogger(PublishApiSubscriber.class);
	long counter = -1;
	int numberOfExpectedComplexEvents = 0;
	int givenRepetitions = 0;
	boolean firstTime = true;
	long timeTmp = 0;
	static String filename;

	public void setGivenRepetitions(int givenRepetitions) {
		this.givenRepetitions = givenRepetitions;
	}

	public PublishApiSubscriber() {
	}

	public ArrayList<CompoundEvent> getComplexEvents() {
		return complexEvents;
	}

	@Override
	public void publish(CompoundEvent arg0) {
		complexEvents.add(arg0);
		logger.info("RECEIVED: ====================================\n" + arg0);
	}


	public void setNumberofExpectedComplexEvents(int number) {
		numberOfExpectedComplexEvents = number;
		System.out.println("NumberOfExpectedComplexEvents:" + number);
	}

	public void setOutputFile(String name){
		filename = name;
	}
	public static void appendToFile(String text) {
		try {

			FileWriter writer = new FileWriter(
					filename,/* append */true);

			writer.write(text);

			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}