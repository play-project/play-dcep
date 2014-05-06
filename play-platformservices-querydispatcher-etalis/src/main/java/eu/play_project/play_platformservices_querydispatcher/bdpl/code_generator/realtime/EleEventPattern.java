package eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime;

import java.util.List;

/**
 * Represents ELE representation of a BDPL event pattern.
 * @author sobermeier
 *
 */
public class EleEventPattern {
	
	private String methodName;
	private String methodImpl;
	
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getMethodImpl() {
		return methodImpl;
	}
	public void setMethodImpl(String methodImpel) {
		this.methodImpl = methodImpel;
	}
}
