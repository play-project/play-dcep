/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.implement;

/**
 * The implementation of algorithm of arrhythmia detection with RR interval.
 * 
 * 
 * @author ningyuan 
 * 
 * Nov 20, 2014
 *
 */
public class ArrhythmiaRRClassifier {
	
	public String execute(double [] rrs){
		int length = rrs.length;
		String ret = "normal";
		
		if(length < 6){
				System.out.println("ArrhythmiaRRClassifier [type]: insufficient Data");
			return "insufficient Data";
		}
		else{
			double rr1 = rrs[length - 6];
			double rr2 = rrs[length - 5];
			double rr3 = rrs[length - 4];
			
			if(rr2 < 0.6 && rr2 < rr3){
				ret = "ventricular flutterfibrillation beats";
				
				double trr1 = rr2;
				double trr2 = rr3;
				double trr3;
				int i = 3;
				for(; i > 0; i--){
					trr3 = rrs[length - i];
					if(!(trr1 < 0.8 && trr2 < 0.8 && trr3 <0.8) && !(trr1 + trr2 + trr3  < 1.8)){
						break;
					}
					
					trr1 = trr2;
					trr2 = trr3;
				}
				
				if(i > 0){
					ret = "normal";
				}
			}
			
			if((rr2 < 0.9 * rr1) && (rr1 < 0.9 * rr3)){
				if(rr2 + rr3 < 2 * rr1){
					ret = "atrial, nodal and supraventricular premature beats";
				}
				else{
					ret = "ventricular premature beats";
				}
			}
			
			if(rr2 > 1.5 * rr1){
				ret = "escape beats";
			}
				
				System.out.println("ArrhythmiaRRClassifier [type]: "+ret);
			return ret;
		}
	}
}
