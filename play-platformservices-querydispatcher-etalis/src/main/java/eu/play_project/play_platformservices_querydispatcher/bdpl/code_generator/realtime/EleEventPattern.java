package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import java.util.List;

/**
 * Represents ELE representation of a BDPL event pattern.
 * @author sobermeier
 *
 */
public class EleEventPattern {
	
	private String methodName;
	private List<String> methodImpl;
	
	public String getMethodName() {
		return methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public List<String> getMethodImpl() {
		return this.methodImpl;
	}
	
	public void setMethodImpl(List<String> methodImpl) {
		this.methodImpl = methodImpl;
	}

}
