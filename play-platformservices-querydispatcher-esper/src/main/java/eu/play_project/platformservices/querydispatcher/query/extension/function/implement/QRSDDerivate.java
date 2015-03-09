/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.implement;

/**
 * The implementation of algorithm of QRS detection using derivation.
 * 
 * 
 * @author ningyuan 
 * 
 * Nov 11, 2014
 *
 */
public class QRSDDerivate {
	
	public int execute(int[] data){
		return AF1(data);
	}
	
	private int AF1(int[] data){
		int length = data.length;
		
		if(length < 30){
			return -1;
		}
		else{
			double y0 = (data[length - 28] - data[length - 30]);
			double y1 = (data[length - 27] - data[length - 29]);
			double y2 = (data[length - 26] - data[length - 28]);
			
			if(y0 > 0.5 && y1 > 0.5 && y2 > 0.5){
				double threshold = getThreshold(data);
				
				int i = 0;
				for(; i < 3 ; i ++){
					if(data[length - 29 + i] < threshold){
						return 400;
					}
				}
				
				for(; i < 27; i++){
					if(data[length - 29 + i] < threshold){
						return 400;
					}
					
					double t0 = data[length - 28 + i] - data[length - 30 + i];
					double t1 = data[length - 27 + i] - data[length - 29 + i];
					
					if(t0 < -0.3 && t1 < -0.3 && data[length - 28 + i] > threshold){
						return 500;
					}
				}
				
				return 400;
			}
			else{
				return 400;
			}
		}
	}
	
	private int DF1(int data[]){
		int length = data.length;
		
		if(length < 48){
			return -1;
		}
		else{
			
			double y05 = (data[length - 40] - data[length - 44]);
			double y04 = (data[length - 41] - data[length - 45]);
			double y03 = (data[length - 42] - data[length - 46]);
			double y02 = (data[length - 43] - data[length - 47]);
			double y01 = (data[length - 44] - data[length - 48]);
			
			double y15 = y05 + 4*y04 + 6*y03 + 4*y02 + y01;
			
			if(y15 > 21){
				boolean con1 = false, con2 = false, con3 = false;
				
				int i = 1;
				for(; i < 40; i++){
					y01 = y02;
					y02 = y03;
					y03 = y04;
					y04 = y05;
					y05 = (data[length - 40 + i] - data[length - 44 + i]);
					
					
					y15 = y05 + 4*y04 + 6*y03 + 4*y02 + y01;
					
					if(y15 < -21){
						if(con1){
							if(con2){
								if(con3){
									return 400;
								}
								else{
									con3 = true;
								}
							}
							else{
								return 400;
							}
						}
						else{
							con1 = true;
						}
					}
					else if(y15 > 21){
						if(con1){
							if(con2){
								return 400;
							}
							else{
								con2 = true;
							}
						}
						else{
							return 400;
						}
					}
				
				}
				
				return 500;
			}
			else{
				return 400;
			}
		}
	}
	
	/*private int FD1(int[] data){
		
	}*/
	
	private double getThreshold(int [] data){
		int max = Integer.MIN_VALUE;
		
		for(int i = 0; i < data.length; i++){
			if(data[i] > max){
				max = data[i];
			}
		}
		
		return (0.3*max);
	}
}
