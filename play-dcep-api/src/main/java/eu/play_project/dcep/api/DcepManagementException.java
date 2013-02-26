package eu.play_project.dcep.api;

public class DcepManagementException extends Exception {

	private static final long serialVersionUID = 7735129831662271066L;

	public DcepManagementException() {
	}

	public DcepManagementException(String message) {
		super(message);
	}

	public DcepManagementException(Throwable cause) {
		super(cause);
	}

	public DcepManagementException(String message, Throwable cause) {
		super(message, cause);
	}

	public DcepManagementException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
