package eu.play_project.dcep.api.measurement;

public class PatternMeasuringResult extends MeasuringResult implements Comparable<PatternMeasuringResult>{
	//Pattern name.
	private String name; 
	private int processedEvents;
	private double borealisLoad;
	/**
	 * Contains measured values for one pattern.	
	 * @param name Name of the pattern.
	 * @param measuringID Id for this measuring period.
	 * @param processedEvents Number of processe events in this period.
	 */
	public PatternMeasuringResult(String name, int processedEvents) {
		super(name);
		this.name = name;
		this.processedEvents = processedEvents;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getProcessedEvents() {
		return processedEvents;
	}
	public void setProcessedEvents(int processedEvents) {
		this.processedEvents = processedEvents;
	}

	public double getBorealisLoad() {
		return borealisLoad;
	}

	public void setBorealisLoad(double borealisLoad) {
		this.borealisLoad = borealisLoad;
	}

	@Override
	public int compareTo(PatternMeasuringResult o) {
		if(processedEvents > o.getProcessedEvents()){
			return 1;
		}else if(processedEvents == o.getProcessedEvents()){
			return 0;
		}else{ 
			return -1;
		}
	}

}
