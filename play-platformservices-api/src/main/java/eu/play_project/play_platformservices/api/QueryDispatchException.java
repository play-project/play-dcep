package eu.play_project.play_platformservices.api;

public class QueryDispatchException extends Exception {

	public QueryDispatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public QueryDispatchException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 100L;

	public QueryDispatchException() {
		super();
	}

	public QueryDispatchException(String message) {
		super(message);
	}
}
