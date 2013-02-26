package eu.play_project.play_platformservices.api;

public class QueryDispatchException extends Exception {

	private static final long serialVersionUID = -3053256787444130368L;

	public QueryDispatchException() {
		super();
	}

	public QueryDispatchException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public QueryDispatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public QueryDispatchException(String message) {
		super(message);
	}

	public QueryDispatchException(Throwable cause) {
		super(cause);
	}
}
