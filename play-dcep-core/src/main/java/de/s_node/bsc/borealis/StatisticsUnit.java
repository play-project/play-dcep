package de.s_node.bsc.borealis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.api.measurement.Load;
import eu.play_project.dcep.api.measurement.LoadTimeSeries;
import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
import eu.play_project.dcep.api.measurement.PatternMeasuringResult;
import eu.play_project.dcep.api.measurement.RoutingInformation;

public class StatisticsUnit {
	private Logger logger;
	int kPeriods; //Number of stored measurements.
	double statisticsWindow; //Total time of k statistics measurements.
	List<NodeMeasuringResult> nodes; //Values of the k measurements.
	
	double epsilon =0; //Good mapping or little load.
	double delta =0;
	
	public StatisticsUnit(){
		logger = LoggerFactory.getLogger(StatisticsUnit.class);
		nodes = new LinkedList<NodeMeasuringResult>();
	}
	
	public double calcVariance(LoadTimeSeries loadTimeSerie) {
		double variance = 99;

		double sum1 = sum(loadTimeSerie, 2);
		double sum2 = sum(loadTimeSerie, 1);

		variance = (1.0 / kPeriods) * sum1 - Math.pow(((1.0 / kPeriods) * sum2), 2);

		return variance;
	}

	public void setkPeriods(int kPeriods) {
		this.kPeriods = kPeriods;
	}
	
	public double calcCovariance(LoadTimeSeries loadTimeSerie1, LoadTimeSeries loadTimeSerie2){
		double covariance = 99;
		//Check parameters
		if(loadTimeSerie1.size()!= loadTimeSerie2.size()){
			throw new RuntimeException("loadTimeSerie1 and loadTimeSerie2 have not the same size. LTS1: " + loadTimeSerie1.size() + " LTS2: " + loadTimeSerie2.size());
		}
		//Calc fist sum
		double sum1 = 0;
		for (int i = 0; i < loadTimeSerie1.size(); i++) {
			sum1 += (loadTimeSerie1.get(i) * loadTimeSerie2.get(i));
		}
		
		double sum2 = 0;
		sum2 = sum(loadTimeSerie1, 1);
		
		double sum3 = 0;
		sum3 = sum(loadTimeSerie2, 1);
		

		covariance = ((1.0/kPeriods)*sum1) - (((1.0/kPeriods)*sum2)*((1.0/kPeriods)*sum3));
		return covariance;
	}
	
	/**
	 * Correlation coefficient between the load time series of operator o and total sum of load time series of all operator on N except o.
	 * @param o Load time series of operator o.
	 * @param N All load time series of node N except load time series of operator o.
	 * @return 
	 */
	public double p(LoadTimeSeries o, LoadTimeSeries[] N){
		//Join LodTimeSeires
		LoadTimeSeries sumN = new LoadTimeSeries("P1", kPeriods);
		
		int seriesSize = N[0].size();
		Double sum = new Double(0);
		// List all elements in a load time series.
		for(int i=0; i<seriesSize; i++){
			// Get all load time series
			for (int j = 0; j < N.length; j++) {
				sum += N[j].get(i);
			}
			sumN.add(sum);
		}
		
		return calcCorrelationCoefficient(o, sumN);
	}
	
	public double calcCorrelationCoefficient(LoadTimeSeries loadTimeSerie1, LoadTimeSeries loadTimeSerie2){
		double correlationCoefficient = 99;
		correlationCoefficient = calcCovariance(loadTimeSerie1, loadTimeSerie2)/(Math.sqrt(calcVariance(loadTimeSerie1)) * Math.sqrt(calcVariance(loadTimeSerie2)));
		return correlationCoefficient;
	}
	
	public double sum(LoadTimeSeries values, int exp){
		double sum = 0;
		for (Double value : values) {
			sum += Math.pow(value, exp);
		}
		return sum;
	}

	/**
	 * Accumulate all values from the LoadTimeSeries in the list without the values of the LoadTimeSeries with the name given in except.
	 * @param loadTimeSeires
	 * @param exept Name of LoadTimeSeries with will not be part of the sum.
	 * @return Sum of all values \{except}.
	 */
	public LoadTimeSeries sum(Map<String, LoadTimeSeries> loadTimeSeires, String except){
		// To know how many elements are in a LoadTimeSeries.
		int windowTime = (loadTimeSeires.get(loadTimeSeires.keySet().iterator().next()).getNumberOfElementsInLoadTimeSeries());
		LoadTimeSeries result = new LoadTimeSeries("sum", windowTime);
		
		// Take first pattern name and add it to the values in result. Take second pattern name and andd it to the values in restult ...
		for (String lsKey : loadTimeSeires.keySet()) {
			if (!lsKey.equals(except)) { // Exlcude series o.
				for (int j = 0; j < windowTime; j++) {
					result.set(j, (result.get(j) + loadTimeSeires.get(lsKey).get(j)));
				}
			}
		}
		return result;
	}
	public void addLoadTimeSeries(String nodeName, LoadTimeSeries loadTimeSeries){
		for (NodeMeasuringResult node : nodes) {
			if(node.getName().equals(node)){
				if(loadTimeSeries.size()==kPeriods){
					node.setLoatTimeSeries(nodeName, loadTimeSeries);
				}else{
					throw new RuntimeException("Only k periods are allowed. K: " + kPeriods + ". loadTimeSeries.size(): " + loadTimeSeries.size() );
				}
			}
		}
	}
	
	/**
	 * Calculates the load for this node an all Patterns in this measurementResult object.
	 */
	public Load calcLoad(NodeMeasuringResult measuredValues){
		Load load = new Load(measuredValues.getName(), measuredValues.getMeasuringPeriod());
		List<PatternMeasuringResult> patternLoad = new LinkedList<PatternMeasuringResult>();
		int processedEvents=0;
		
		//Calculate total load of the node.
		load.setTotalLoad(measuredValues.getNumberOfComponentInputEvetns()*measuredValues.getProcessingTimeForOneEvent());
		
		//Get number of processed events.
		for (PatternMeasuringResult patternMeasuringResult : measuredValues.getMeasuredValues()) {
			processedEvents += patternMeasuringResult.getProcessedEvents();
		}

		//Calculate load for every pattern.
		for (PatternMeasuringResult patternMeasuringResult : measuredValues.getMeasuredValues()) {
			PatternMeasuringResult ab = new PatternMeasuringResult(patternMeasuringResult.getName(), patternMeasuringResult.getProcessedEvents());
			patternLoad.add(ab);
			double loadD = ((((patternMeasuringResult.getProcessedEvents()* 1.0) / processedEvents)*measuredValues.getNumberOfComponentInputEvetns())*measuredValues.getProcessingTimeForOneEvent());
			ab.setBorealisLoad(loadD);
		}
		
		// Set calculated values.
		load.setLoadPerPattern(patternLoad);
		
		return load;
	}
	
	// Algorithem of the paper
	public List<RoutingInformation> makeDecisions() {
		List<RoutingInformation> patternsToMove = new ArrayList<RoutingInformation>();
		RoutingInformation pattern = null;
		
		// Request statistics from all sites/instances

		// Calculate utilization.
		
		// Order all nodes by their average load.
		Collections.sort(nodes, Collections.reverseOrder());
		
		

		// Check if movement is needed. // Section 3.3 in paper. Pair-wise Algorithm.
		for (int i = 0; i < (nodes.size() / 2); i++) {
			NodeMeasuringResult donor = nodes.get(i);
			NodeMeasuringResult receiver = nodes.get((nodes.size()) - (i + 1));
 
		//	LoadTimeSeries operator = nodes.get(i); //FIXME change value.

			logger.debug("Donor is: " + donor.getName());
			logger.debug("Receiver is: " + receiver.getName());
			logger.debug("Donor totalLoad: " + donor.getTotalLoad());
			logger.debug("Receiver totalLoad: " + receiver.getTotalLoad());
			
			// Move only if difference is big.
			if ((donor.getTotalLoad() - receiver.getTotalLoad()) > epsilon) {
				logger.debug("Load difference is > epsilon. " + (donor.getTotalLoad() - receiver.getTotalLoad()));
				
				// 3.3.1 One-way Correlation Based Load Balancing.
				double maxLoadToMove = (donor.getTotalLoad()-receiver.getTotalLoad())/2;
				double loadOfSelectedOperators =0;
				List<BorealisScoreLoad> bLoad = new LinkedList<BorealisScoreLoad>();
				
				
				//Calculate load for all operators. To move from N1 to N2.
				for (LoadTimeSeries o : donor.getLoadTimeSeriesList()) {
					LoadTimeSeries N1 = this.sum(donor.getAllLoatTimeSeries(), o.getName());
					LoadTimeSeries N2 = this.sum(receiver.getAllLoatTimeSeries(), ""); //Exclude nothing.

					
					bLoad.add(new BorealisScoreLoad(o.getName(), (calcCorrelationCoefficient(o, N1) - calcCorrelationCoefficient(o, N2)) / 2));
				}

				//Sort operators
				Collections.sort(bLoad, Collections.reverseOrder());
				
				//Select operators having the greatest  score until the load of the selected operators exceed (L1-L2)/2
				for(int j=0; (loadOfSelectedOperators < maxLoadToMove && j<bLoad.size()); j++){
					loadOfSelectedOperators += bLoad.get(i).getLoad();
					patternsToMove.add(new RoutingInformation(donor.getName(), receiver.getName(), bLoad.get(j).getName()));
				}	

			} else {
				System.out.println("Nicht umziehen.");
				break; //List is sorted so it makes no sens to look for the next elements.
			}

			// Move operators with score > delta
//			if (calcOneWayCorrelationBasedScore(operator, donor, receiver) > delta) {
//				// Put operator to node receiver.
//			}
		}
		return patternsToMove;
	} 
		/**
		 * Calcualte scrore for moving operator o from node n1 to n2.
		 * @param o Load of o.
		 * @param n1 Load of node 1.
		 * @param n2 Load of node 2
		 * @return Score for moving o from n1 to n2.
		 */
		protected double calcOneWayCorrelationBasedScore(LoadTimeSeries o,LoadTimeSeries n1, LoadTimeSeries n2){
			double score =0;
			
			score = (calcCorrelationCoefficient(o, n1) - calcCorrelationCoefficient(o, n2)) / 2.0;

			return score;
			
		}
		
		public double getkPeriods() {
			return kPeriods;
		}
		
		/**
		 * Put measured data in StatisticsUnit.
		 * @param data
		 */
		public void addData(NodeMeasuringResult data){
			
			Load l = this.calcLoad(data);
			
			// Add data to LoadTimeSeries for this node.
			NodeMeasuringResult node = this.getNode(data.getName()); //Get Node
			
			// If node does not exists create one.
			if(node==null){
				nodes.add(data);
				node = this.getNode(data.getName()); //Get Node
			}
			
			//Get load form measuring results add put it to the load time series of the given node.
			for (PatternMeasuringResult patternLoad : l.getLoadPerPattern()) {
				node.getLoatTimeSeries(patternLoad.getName()).add(patternLoad.getBorealisLoad());
			}
		}
		
		/**
		 * Get node with the given name. If node does not exist return null.
		 * @param name Name of the node.
		 * @return
		 */
		private NodeMeasuringResult getNode(String name){
			
			NodeMeasuringResult result = null;
			for (NodeMeasuringResult node : nodes) {
				if(node.getName().equals(name)){
					result = node;
				}
			}
			return result;
		}
}
