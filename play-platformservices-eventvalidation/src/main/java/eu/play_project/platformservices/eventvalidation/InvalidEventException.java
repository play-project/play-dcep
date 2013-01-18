package eu.play_project.platformservices.eventvalidation;

public class InvalidEventException extends Exception {

	private static final long serialVersionUID = -244237528077092007L;

	public InvalidEventException(String message) {
		super(message);
	}
}
