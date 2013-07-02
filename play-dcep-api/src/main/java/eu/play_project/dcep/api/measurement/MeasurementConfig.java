package eu.play_project.dcep.api.measurement;

import java.io.Serializable;

import eu.play_project.play_platformservices.api.BdplQuery;

public class MeasurementConfig implements Serializable {
	int measurementPeriod;
	BdplQuery bdplquery;
	//TODO sobermeier Extends with event template.
	public int getMeasurementPeriod() {
		return measurementPeriod;
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
	
}
