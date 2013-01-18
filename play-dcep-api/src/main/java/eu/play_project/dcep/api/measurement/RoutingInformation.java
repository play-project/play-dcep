package eu.play_project.dcep.api.measurement;

public class RoutingInformation {
	String source;
	String destination;
	String pattern;
	
	public RoutingInformation(String source, String destination, String pattern) {
		super();
		this.source = source;
		this.destination = destination;
		this.pattern = pattern;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}
