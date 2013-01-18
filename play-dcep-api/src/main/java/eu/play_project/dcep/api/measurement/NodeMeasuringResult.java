package eu.play_project.dcep.api.measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NodeMeasuringResult extends MeasuringResult implements Comparable<NodeMeasuringResult>{

	private static final long serialVersionUID = 1L;
	private List<PatternMeasuringResult> measuredValues; //PatternID number of processed events.
	private Map<String, LoadTimeSeries> ltsMap;
	private List<LoadTimeSeries> ltsList;
	private double totalLoad;
	private int statisticsWindows = 0;
	private int compontenQueue = 0;
	private int etalisInputQueue = 0;
	private int period;

	public NodeMeasuringResult(String nodeName) {
		super(nodeName);
		this.ltsMap = new HashMap<String, LoadTimeSeries>();
		this.ltsList = new LinkedList<LoadTimeSeries>();
	}
	

	public NodeMeasuringResult(String nodeName, int period, List<PatternMeasuringResult> values) {
		super(nodeName, period);
		this.measuredValues = values;
		this.ltsMap = new HashMap<String, LoadTimeSeries>();
		this.ltsList = new LinkedList<LoadTimeSeries>();
	}


	public List<PatternMeasuringResult> getMeasuredValues() {
		return measuredValues;
	}

	public void setMeasuredValues(List<PatternMeasuringResult> measuredValues) {
		this.measuredValues = measuredValues;
	}
	
	public void addMeasuredValue(PatternMeasuringResult measuredValues) {
		if(this.measuredValues == null){
			this.measuredValues = new ArrayList<PatternMeasuringResult>();
		}
		
		this.measuredValues.add(measuredValues);
	}


	public LoadTimeSeries getLoatTimeSeries(String name) {
		if(ltsMap.get(name)==null){
			if(statisticsWindows==0){
				throw new RuntimeException("statisticsWindow must be > 0.");
			}
			ltsMap.put(name, new LoadTimeSeries(name, statisticsWindows));
		}
		return ltsMap.get(name);
	}
	
	public Map<String, LoadTimeSeries> getAllLoatTimeSeries() {
		return ltsMap;
	}


	public void addElementToLoatTimeSeries(String name, Double vaue) {
		ltsMap.get(name).add(vaue);
	}
	
	public void setLoatTimeSeries(String name, LoadTimeSeries loadTimeSeries) {
		ltsMap.put(name, loadTimeSeries);
	}
	
	public double getTotalLoad(){
		return totalLoad;
	}

	public void setTotalLoad(double totalLoad) {
		this.totalLoad = totalLoad;
	}


	public int getStatisticsWindows() {
		return statisticsWindows;
	}


	public void setStatisticsWindows(int statisticsWindows) {
		this.statisticsWindows = statisticsWindows;
	}


	@Override
	public int compareTo(NodeMeasuringResult o) {
		if(this.totalLoad < o.getTotalLoad()){
			return -1;
		}else if(this.totalLoad == o.getTotalLoad()){
			return 0;
		}else{
			return 1;
		}
	}
	
	private void putLTSinList(){
		ltsList = new LinkedList<LoadTimeSeries>();
		
		for (String patternId : ltsMap.keySet()) {
			ltsList.add(ltsMap.get(patternId));
			
		}
	}
	
	public List<LoadTimeSeries> getLoadTimeSeriesList(){
		putLTSinList();
		return ltsList;
	}


	public int getCompontenQueue() {
		return compontenQueue;
	}


	public void setCompontenQueue(int compontenQueue) {
		this.compontenQueue = compontenQueue;
	}


	public int getEtalisInputQueue() {
		return etalisInputQueue;
	}


	public void setEtalisInputQueue(int etalisInputQueue) {
		this.etalisInputQueue = etalisInputQueue;
	}
}
