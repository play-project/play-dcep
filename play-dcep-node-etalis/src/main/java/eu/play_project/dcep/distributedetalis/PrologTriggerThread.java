package eu.play_project.dcep.distributedetalis;

import com.jtalis.core.JtalisContextImpl;

/**
 * Call a prolog method every n ms.
 * 
 * @author Stefan Obermeier
 *
 */
public class PrologTriggerThread extends Thread {

	private JtalisContextImpl ctx; // Prolog context
	private int delay; // ms between two calls.
	
	/**
	 * Instantiate method call thread with prolog context and delay.
	 * @param ctx Prolog context.
	 * @param delay Delay between two method calls in ms.
	 */
	public PrologTriggerThread(JtalisContextImpl ctx, int delay) {
		this.ctx = ctx;
		this.delay =  delay;
		
		start();
	}
	
	@Override
	public void run() {

		while(! isInterrupted()) {
			ctx.getEngineWrapper().executeGoal("doSomething");
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				interrupt();
			}
		}
		
	}

}
