/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.implement;

/**
 * @author ningyuan 
 * 
 * Aug 6, 2014
 *
 */
public class DefaultExFunction {
	
	public double execute(double[] data){
		double ret = 0;
		for(int i = 1; i < data.length; i++){
			data[0] += data[i];
		}
		
		if(data.length > 0){
			ret = data[0] / data.length;
		}
		
		return ret;
	}

}
