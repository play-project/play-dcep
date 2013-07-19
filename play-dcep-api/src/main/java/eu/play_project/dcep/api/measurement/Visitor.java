package eu.play_project.dcep.api.measurement;

public abstract class Visitor {

	public void visit(PatternMeasuringResult element){}
	public void visit(NodeMeasurementResult element){}
	public void visit(LoadTimeSeries element){}
	public void visit(MeasurementResult measurementResult) {}


}
