/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.implement;

import java.util.Arrays;

import math.jwave.Transform;
import math.jwave.transforms.FastWaveletTransform;
import math.jwave.transforms.wavelets.daubechies.Daubechies4;

/**
 * The implementation of algorithm of ECG denoising using soft thresholding with DWT.
 * 
 * 
 * @author ningyuan 
 * 
 * Dec 14, 2014
 *
 */
public class SofThresholdingDB4 {
	
	public double[] execute(double[] data){
		double [] ret = null;
		
		Transform t = new Transform(new FastWaveletTransform(new Daubechies4()));
		
		int length = data.length;
		
		//if(length % 16 == 0){
			double[][] matDeComp = t.decompose(data);
			
			int subLength = length;
			for(int i = 1; i < matDeComp.length; i++){
				double [] level = matDeComp[i];
				
				subLength = subLength/2;
				double [] dc = new double[subLength];
				
				for(int j = 0; j < subLength; j++){
					dc[j] = Math.abs(level[j]);
				}
				
				double [] pa = getMedianAndMax(dc);
				
				double threshold = (pa[0] / 0.6745) * Math.pow((2 * Math.log(length)), 0.5);
				
				for(int j = 0; j < subLength; j++){
					if(Math.abs(level[j]) < threshold){
						level[j] = 0;
					}
					// soft threshold
					else{
						level[j] = level[j] - (Math.signum(level[j]) * threshold);
					}
				}
				
			}
			
			ret = t.recompose(matDeComp);
			
		//}
		
		return ret;
	}
	
	private double[] getMedianAndMax(double [] data){
		Arrays.sort(data);
		double median;
		if (data.length % 2 == 0)
		    median = (data[data.length/2] + data[data.length/2 - 1])/2;
		else
		    median =  data[data.length/2];
		
		double [] ret = new double[2];
		ret[0] = median;
		ret[1] = data[data.length-1];
		return ret;
	}
}
