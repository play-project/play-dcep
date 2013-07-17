package eu.play_project.dcep.api.measurement;

import java.io.Serializable;

import eu.play_project.play_platformservices.api.BdplQuery;

public class MeasurementConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	int measurementPeriod;
	BdplQuery bdplquery;

	MeasurementConfig(){}
	/**
	 * Define measurement query and measurement period.
	 * @param measurementPeriod Time in seconds.
	 * @param bdplquery
	 */
	public MeasurementConfig(int measurementPeriod, BdplQuery bdplquery){
		this.measurementPeriod = measurementPeriod;
		this.bdplquery = bdplquery;
	}
	
	public void setMeasurementPeriod(int measurementPeriod) {
		this.measurementPeriod = measurementPeriod;
	}
	public BdplQuery getBdplquery() {
		return bdplquery;
	}
	public void setBdplquery(BdplQuery bdplquery) {
		this.bdplquery = bdplquery;
	}
	
	public int getMeasurementPeriod() {
		return measurementPeriod;
	}
	
}
