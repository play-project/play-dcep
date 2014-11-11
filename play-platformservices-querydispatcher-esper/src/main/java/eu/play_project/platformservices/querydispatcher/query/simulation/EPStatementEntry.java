/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.espertech.esper.client.EPStatement;

/**
 * @author ningyuan 
 * 
 * Apr 30, 2014
 *
 */
public class EPStatementEntry extends WindowAdapter{
	
	private final String epl;
	
	private final EPStatement statement;
	
	private JFrame frame = null;
	
	public EPStatementEntry(String e, EPStatement s){
		epl = e;
		statement = s;
	}
	
	public String getEpl() {
		return this.epl;
	}
	
	public void setFrame(JFrame f){
		frame = f;
		frame.addWindowListener(this);
	}
	
	@Override
	public void windowClosed(WindowEvent e) {
       frame = null;
    }
	
	public void start(){
		if(statement.isStopped())
			statement.start();
	}
	
	public void stop(){
		if(statement.isStarted())
			statement.stop();
	}
	
	public void destroy(){
		if(!statement.isDestroyed()){
			statement.destroy();
		}
		
		if(frame != null){
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		}
	}
}
