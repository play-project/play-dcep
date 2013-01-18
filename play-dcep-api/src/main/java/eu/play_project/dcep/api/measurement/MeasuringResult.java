package eu.play_project.dcep.api.measurement;

import java.io.Serializable;

public abstract class MeasuringResult implements Serializable{
	private String name;
	protected int measuringPeriod =0;
	private int numberOfComponentInputEvetns =0;
	private int numberOfEtalisInputEvents = 0;
	private int numberOfOutputEvents =0;
	private long processingTimeForOneEvent =0;
	private long numberOfEventsProcessedSinceStartUp=0;
	private long inputBuffer = 0;
	private long outputBuffer = 0;
	
	public MeasuringResult(){}
	public MeasuringResult( String name){
		this.name = name;
	}
	
	public MeasuringResult( String name, int period){
		this.name = name;
		this.measuringPeriod = period;
	}

	public void accept(Visitor v){
		v.visit(this);
	}

	public int getMeasuringPeriod() {
		return measuringPeriod;
	}

	
	public int getNumberOfComponentInputEvetns() {
		return numberOfComponentInputEvetns;
	}

	public void setNumberOfComponentInputEvetns(int numberOfInputEvetns) {
		this.numberOfComponentInputEvetns = numberOfInputEvetns;
	}

	public int getNumberOfOutputEvents() {
		return numberOfOutputEvents;
	}

	public void setNumberOfOutputEvents(int numberOfOutputEvents) {
		this.numberOfOutputEvents = numberOfOutputEvents;
	}

	public long getProcessingTimeForOneEvent() {
		return processingTimeForOneEvent;
	}

	public void setProcessingTimeForOneEvent(long processingTimeForOneEvent) {
		this.processingTimeForOneEvent = processingTimeForOneEvent;
	}

	public int getNumberOfEtalisInputEvents() {
		return numberOfEtalisInputEvents;
	}

	public void setNumberOfEtalisInputEvents(int numberOfEtalisInputEvents) {
		this.numberOfEtalisInputEvents = numberOfEtalisInputEvents;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the numberOfEventsProcessedSinceStartUp
	 */
	public long getNumberOfEventsProcessedSinceStartUp() {
		return numberOfEventsProcessedSinceStartUp;
	}
	/**
	 * @param numberOfEventsProcessedSinceStartUp the numberOfEventsProcessedSinceStartUp to set
	 */
	public void setNumberOfEventsProcessedSinceStartUp(long numberOfEventsProcessedSinceStartUp) {
		this.numberOfEventsProcessedSinceStartUp = numberOfEventsProcessedSinceStartUp;
	}
	public long getInputBufferUtilization() {
		return inputBuffer;
	}
	public void setInputBufferUtilization(long inputBuffer) {
		this.inputBuffer = inputBuffer;
	}
	public long getOutputBufferUtilization() {
		return outputBuffer;
	}
	public void setOutputBufferUtilization(long outputBuffer) {
		this.outputBuffer = outputBuffer;
	}
}
