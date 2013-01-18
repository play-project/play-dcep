package eu.play_project.dcep.api.measurement;

import java.util.List;


public class Load extends MeasuringResult implements Comparable<Load> {
	
	private double totalLoad;
	private long numberOfEventsProcessed;
	private String name;
	
	private List<PatternMeasuringResult> loadPerPattern;
	
	public Load(String name, int period) {
		super(name, period);
	}
	
	public double getTotalLoad() {
		return totalLoad;
	}
	public void setTotalLoad(double totalLoad) {
		this.totalLoad = totalLoad;
	}
	public List<PatternMeasuringResult> getLoadPerPattern() {
		return loadPerPattern;
	}
	public void setLoadPerPattern(List<PatternMeasuringResult> loadPerPattern) {
		this.loadPerPattern = loadPerPattern;
	}
	
	@Override
	public int compareTo(Load o) {
		if(totalLoad > o.getTotalLoad()){
			return 1;
		}else if(totalLoad == o.getTotalLoad()){
			return 0;
		}else{ 
			return -1;
		}
	}

	public long getNumberOfEventsProcessed() {
		return numberOfEventsProcessed;
	}

	public void setNumberOfEventsProcessed(long numberOfEventsProcessed) {
		this.numberOfEventsProcessed = numberOfEventsProcessed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
