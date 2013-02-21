package eu.play_project.play_platformservices;

public class ExceptionCatcher extends ThreadGroup{

	public ExceptionCatcher(String name) {
		super(name);
	}
	
	@Override
	 public void uncaughtException(Thread t, Throwable e) {
	   System.out.println("Error: " + e.getMessage());
	 }


}
