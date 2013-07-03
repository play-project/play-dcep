package eu.play_project.dcep.distribution;


import java.io.FileWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;

public class SubscriberPerformanceTest implements PublishApi, Serializable {

	private static final long serialVersionUID = 100L;
	private long t1;
	ArrayList<CompoundEvent> complexEvents = new ArrayList<CompoundEvent>();
	long counter = -1;
	int numberOfExpectedComplexEvents = 0;
	int givenRepetitions = 0;
	boolean firstTime = true;
	long timeTmp = 0;
	static String filename;

	public void setGivenRepetitions(int givenRepetitions) {
		this.givenRepetitions = givenRepetitions;
	}

	public SubscriberPerformanceTest() {
	}

	public ArrayList<CompoundEvent> getComplexEvents() {
		return complexEvents;
	}

	@Override
	public void publish(CompoundEvent arg0) {
		//System.out.println("New event in " + this.getClass().getSimpleName() + arg0.toString());
		// complexEvents.add(arg0);
		counter++;
		if (counter % 1000 == 0) {
			System.out.println((counter) + " "
					+ (System.currentTimeMillis() - timeTmp));
			timeTmp = System.currentTimeMillis();
		}
		//System.out.println("Subscriber got complex event: " + counter);
		//System.out.println(arg0);
		//arg0.getGraph();
		//arg0 = null;
//		if (counter >= numberOfExpectedComplexEvents) {
//			long t2 = System.currentTimeMillis();
//			System.out.println(t2 - t1);
//			//appendToFile("" + (t2 - t1) + ";");
//			// System.out.println("Counter: " + counter);
//
//		}
	}

	@Override
	public void publish(Quadruple arg0) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void publish(Collection<CompoundEvent> arg0) {
		throw new UnsupportedOperationException("Not implemented");
	}


	public void setT1(long t1) {
		this.t1 = t1;
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

	@Override
	public void publish(URL arg0, SerializationFormat arg1) {
		throw new UnsupportedOperationException("not implemented");
	}
}