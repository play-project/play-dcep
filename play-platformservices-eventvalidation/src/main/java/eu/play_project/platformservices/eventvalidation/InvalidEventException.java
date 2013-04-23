package eu.play_project.platformservices.eventvalidation;

public class InvalidEventException extends Exception {

	private static final long serialVersionUID = 100L;

	public InvalidEventException(String message) {
		super(message);
	}
}
