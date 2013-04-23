package eu.play_project.dcep.distributedetalis;

/**
 * A new complex event has no matching historic part so it must not be fired but retracted.
 * 
 * @author stuehmer
 */
public class RetractEventException extends Exception {

	private static final long serialVersionUID = 100L;

}
