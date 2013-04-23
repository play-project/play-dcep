package eu.play_project.dcep;

public class DcepException extends Exception {

	private static final long serialVersionUID = 100L;

	public DcepException() {
	}

	public DcepException(String message) {
		super(message);
	}

	public DcepException(Throwable cause) {
		super(cause);
	}

	public DcepException(String message, Throwable cause) {
		super(message, cause);
	}

}
