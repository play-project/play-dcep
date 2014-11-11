package eu.play_project.dcep.node.api;

public class DcepNodeException extends Exception {

	private static final long serialVersionUID = 100L;

	public DcepNodeException() {
		super();
	}

	public DcepNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DcepNodeException(String message) {
		super(message);
	}

	public DcepNodeException(Throwable cause) {
		super(cause);
	}

}
