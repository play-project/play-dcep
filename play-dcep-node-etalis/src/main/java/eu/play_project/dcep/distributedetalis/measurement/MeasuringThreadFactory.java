package eu.play_project.dcep.distributedetalis.measurement;

import java.util.concurrent.ThreadFactory;
/**
 * Generate a thread with high priority.
 * @author sobermeier
 *
 */
public class MeasuringThreadFactory implements ThreadFactory{

	@Override
	public Thread newThread(Runnable r) {
		Thread t =  new Thread(r);
		t.setPriority(java.lang.Thread.MAX_PRIORITY);
		return t;
	}


}
