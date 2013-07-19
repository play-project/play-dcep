
package eu.play_project.dcep.api.measurement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LoadTimeSeries extends MeasurementResult implements Collection<Double>, List<Double>, Comparable<LoadTimeSeries>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 100L;
	private List<Double> datastructure;
	private String name;
	private int possitonInDatascructure;
	private int numberOfElementsInLoadTimeSeries =0;
	
	
	public LoadTimeSeries(String name, int numberOfElementsInLoadTieSeries){
		datastructure = new ArrayList<Double>();
		this.setName(name);
		this.numberOfElementsInLoadTimeSeries = numberOfElementsInLoadTieSeries;
		possitonInDatascructure = 0;
		
		//Init datastructure.
		for (int i = 0; i < numberOfElementsInLoadTieSeries; i++) {
			datastructure.add(0.0);
		}
	}


	@Override
	public int size() {
		return datastructure.size();
	}


	@Override
	public boolean isEmpty() {
		return datastructure.isEmpty();
	}


	@Override
	public boolean contains(Object o) {
		return datastructure.contains(o);
	}


	@Override
	public Iterator<Double> iterator() {
		return datastructure.iterator();
	}


	@Override
	public Object[] toArray() {
		return datastructure.toArray();
	}


	@Override
	public <T> T[] toArray(T[] a) {
		return datastructure.toArray(a);
	}


	@Override
	public boolean add(Double e) {
		datastructure.set(possitonInDatascructure, e);
		possitonInDatascructure++;
		// Only a fixed number of elements is possible.
		possitonInDatascructure = possitonInDatascructure%numberOfElementsInLoadTimeSeries;

		return true;
	}


	@Override
	public boolean remove(Object o) {
		return datastructure.remove(o);
	}


	@Override
	public boolean containsAll(Collection<?> c) {
		return datastructure.containsAll(c);
	}


	@Override
	public boolean addAll(Collection<? extends Double> c) {
		return datastructure.addAll(c);
	}


	@Override
	public boolean removeAll(Collection<?> c) {
		return datastructure.removeAll(c);
	}


	@Override
	public boolean retainAll(Collection<?> c) {
		return datastructure.retainAll(c);
	}


	@Override
	public void clear() {
		datastructure.clear();
	}


	@Override
	public boolean addAll(int index, Collection<? extends Double> c) {
		return datastructure.addAll(c);
	}


	@Override
	public Double get(int index) {
		return datastructure.get(index);
	}


	@Override
	public Double set(int index, Double element) {
		return datastructure.set(index, element);
	}


	@Override
	public void add(int index, Double element) {
		datastructure.add(index, element);
	}


	@Override
	public Double remove(int index) {
		return datastructure.remove(index);
	}


	@Override
	public int indexOf(Object o) {
		return datastructure.indexOf(o);
	}


	@Override
	public int lastIndexOf(Object o) {
		return datastructure.lastIndexOf(o);
	}


	@Override
	public ListIterator<Double> listIterator() {
		return datastructure.listIterator();
	}


	@Override
	public ListIterator<Double> listIterator(int index) {
		return datastructure.listIterator(index);
	}


	@Override
	public List<Double> subList(int fromIndex, int toIndex) {
		return subList(fromIndex, toIndex);
	}


	/**
	 * @return the patternName
	 */
	@Override
	public String getName() {
		return name;
	}


	/**
	 * @param patternName the patternName to set
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}


	@Override
	public int compareTo(LoadTimeSeries o) {
		
		if(this.calcualteAverageValue(this) < this.calcualteAverageValue(o)){
			return -1;
		}else if(this.calcualteAverageValue(this) == this.calcualteAverageValue(o)){
			return 0;
		}else{
			return 1;
		}
	}
	
	/**
	 * Calc average load.
	 * @param o Load time series.
	 * @return average load.
	 */
	private double calcualteAverageValue(LoadTimeSeries o){
		double average = 0;
		for (Double load : o) {
			average += load;
		}
		
		average = average/o.size();
		
		return average;
	}


	public int getNumberOfElementsInLoadTimeSeries() {
		return numberOfElementsInLoadTimeSeries;
	}

	


	
	
}
