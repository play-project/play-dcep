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
	
	public double average(double[] data){
		double ret = 0;
		for(int i = 1; i < data.length; i++){
			data[0] += data[i];
		}
		
		if(data.length > 0){
			ret = data[0] / data.length;
		}
		
		System.out.println(ret);
		
		return ret;
	}
}
