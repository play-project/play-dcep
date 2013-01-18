/**
 * 
 */
package eu.play_project.dcep.distributedetalis.join;

import java.util.ArrayList;
import java.util.List;

/**
 * For each natural join operation, a new object should be created for thread safety.
 * @author Ningyuan Pan
 *
 */
public class NaturalJoiner {
	// the number of same variables in two variable lists
	private int size = 0;
	// vp1 []: positions of same variables in list 1
	// vp2 []: positions of same variables in list 2 and of other variables (in reverse sequence)
	private int [] vp1, vp2;
	
	/**
	 * Natural join of two result r1 and r2 with their variable lists v1 and v2.
	 * This method works on individual data and try to not affect higher data structure
	 * @param r1 First result.
	 * @param v1 Variable list of first result. After execution v1 becomes new 
	 * variable list of nature product.
	 * @param r2
	 * @param v2
	 * @return Nature join of r1 and r2.
	 */
	
	// O(MAX(|v1||v2|, |r1||r2|||v1|+|v2||))
	public List<List> naturalJoin(List<List> r1, List<String> v1, List<List>r2, List<String> v2){
		List<List> ret = new ArrayList<List>();
		joinVar(v1, v2);
		
		if(r1.size() == 0 || r2.size() == 0){
			System.out.println("\nCross Result: ");
			return ret;
		}
		
		for(int i = 0; i < r1.size(); i++){
			List<String> rd1 = r1.get(i);
			for(int j = 0; j < r2.size(); j++){
				boolean found = true;
				List<String> rd2 = r2.get(j);
				for(int k = 0; k < size; k++){
					if(!rd1.get(vp1[k]).equals(rd2.get(vp2[k]))){
						found = false;
						break;
					}
				}
				if(found){
					List<String> rd = new ArrayList<String>();
					for(int k = 0; k < rd1.size(); k++){
						rd.add(rd1.get(k));
					}
					for(int k = vp2.length-1; k >= size; k--){
						rd.add(rd2.get(vp2[k]));
					}
					ret.add(rd);
				}
			}
		}
		
		//for test
		System.out.println("\nCross Result: ");
		for(int i = 0; i < ret.size(); i++){
			List<String> l = ret.get(i);
			for(int j = 0; j < l.size(); j++){
				System.out.print(l.get(j)+" ");
			}
			System.out.println();
		}
		
		/*System.out.println("\nV1: ");
		for(int i = 0; i < v1.size(); i++){
			System.out.print(v1.get(i)+" ");
		}
		System.out.println("\nV2: ");
		for(int i = 0; i < v2.size(); i++){
			System.out.print(v2.get(i)+" ");
		}
		System.out.println();*/
		
		return ret;
	}
	
	// O(|v1||v2|)
	// combine two variable lists into one in v1, which keep the sequence of 
	// natural join variable list same with result column
	private void joinVar(List<String> v1, List<String> v2){
		size = 0;
		vp1 = new int [v1.size()];
		vp2 = new int [v2.size()];
		int v1s = v1.size(); 
		int v2s = v2.size()-1;
		
		boolean found = false;
		for(int i = 0; i < v2.size(); i++){
			found = false;
			String vn2 = v2.get(i);
			for(int j = 0; j < v1s; j++){
				String vn1 = v1.get(j);
				if(vn2.equals(vn1)){
					vp1[size] = j;
					vp2[size++] = i;
					found = true;
					break;
				}
			}
			if(!found){
				v1.add(vn2);
				vp2[v2s--] = i;
			}
		}
		
		//for test
		/*System.out.println("\n1. Position: ");
		for(int i = 0; i < size; i++){
			System.out.print(vp1[i]+" ");
		}
		System.out.println("\n2. Position: ");
		for(int i = 0; i < v2.size(); i++){
			System.out.print(vp2[i]+" ");
		}*/
		System.out.println("\nCombined Vars:");
		for(int i = 0; i < v1.size(); i++){
			System.out.print(v1.get(i)+" ");
		}
		System.out.println();
	}
}
