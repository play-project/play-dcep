package eu.play_project.dcep.node.persistence;

/**
 * @author Roland Stühmer
 */
public class PersistenceException extends Exception {

	private static final long serialVersionUID = 100L;

	public PersistenceException() {
		super();
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public PersistenceException(String message) {
		super(message);
	}

	public PersistenceException(Throwable cause) {
		super(cause);
	}

}
