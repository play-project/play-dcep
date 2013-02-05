/**
 * 
 */
package eu.play_project.dcep.distributedetalis.join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Ningyuan Pan
 *
 */
public class Core {
	
	//private ResultComparator comp = new ResultComparator();
	
	public static void make(Map<String, SelectVariable> variables, List<ResultRegistry> results){
		NaturalJoiner cross = new NaturalJoiner();
		List<ResultRegistry> minp = new ArrayList<ResultRegistry>(2);
		getMinProduct(minp, variables);
		
		while(minp.size() > 1){
			ResultRegistry min1 = minp.get(0);
			SelectResults min2 = minp.get(1);
				//System.out.println("\nCross Chosen: ( "+min1.getNum()+" "+min2.getNum()+" ) ");
			min1.setResult(cross.naturalJoin(min1.getResult(), min1.getVariables(), min2.getResult(), min2.getVariables()));
			
			// remove min2 in all relevant variables and add min1
			List<String> min2v = min2.getVariables();
			for(int i = 0; i < min2v.size(); i++){
				SelectVariable sv = variables.get(min2v.get(i));
				boolean b1 = sv.removeRelResult(min2);
				
				boolean b2 = sv.addRelResult(min1);
					//System.out.print("\nVariable "+min2v.get(i)+" remove relevant result "+min2.getNum()+": "+b1);
					//System.out.print("\nVariable "+min2v.get(i)+" add relevant result "+min1.getNum()+": "+b2);
			}
			
			// remove min2 
			boolean b3 = results.remove(min2);
				//System.out.print("\nRemove result "+min2.getNum()+": "+b3+"\n");
			
			getMinProduct(minp, variables);
		}
		
		// nature product with all independent results
		ResultRegistry ret = results.get(0);
		if(results.size() > 1){
			for(int i = 1; i < results.size(); i++){
				SelectResults ir = results.get(i);
					//System.out.println("\nIndepCross Chosen: ( "+ret.getNum()+" "+ir.getNum()+" ) ");
				ret.setResult(cross.naturalJoin(ret.getResult(), ret.getVariables(), ir.getResult(), ir.getVariables()));
			}
		}
	}
	
	// O(SIGMA(select variables):log|relevant results|)
	private static void getMinProduct(List<ResultRegistry> ret, Map<String, SelectVariable> vars){
		int product = Integer.MAX_VALUE;
		ret.clear();
		
		for(String v : vars.keySet()){
			TreeSet<ResultRegistry> rr = vars.get(v).getRelResult();
			
			if(rr.size() > 1){
				ResultRegistry r1 = rr.pollFirst();
				ResultRegistry r2 = rr.first();
				rr.add(r1);
				int minProd = (r1.getSize())*(r2.getSize());
				if(minProd > 0 && minProd < product){
					product = minProd;
					ret.clear();
					ret.add(r1);
					ret.add(r2);
						//System.out.println("\nCross Cadidate: ( "+r1.getNum()+" "+r2.getNum()+" ) ");
				}
			}
		}
	}
	
	/*public static void make2(Map<String, SelectVariable> variables, List<ResultRegistry> results){
		NaturalJoiner cross = new NaturalJoiner();
		List<ResultRegistry> indepResults = new ArrayList<ResultRegistry>();
		
		while(results.size() > 1){
			Collections.sort(results, comp);
		
			// get the result with minimal size
			ResultRegistry minr = results.get(0);
			List<String> minrv = minr.getVariables();
			List<ResultRegistry> rr = new ArrayList<ResultRegistry>();
			// search through all variables in the minimal result
			for(int i = 0; i < minrv.size(); i++){
				SelectVariable sv = variables.get(minrv.get(i));
				Set<ResultRegistry> vr = sv.getRelResult();
				
				// search through all results that share same variable with minimal result
				for(ResultRegistry r : vr){
					if(r != minr){
						System.out.print("\nCross Candidate on "+minrv.get(i)+" : ( "+minr.getNum()+" "+r.getNum()+" ) ");
						if(!rr.contains(r))
							rr.add(r);
					}
				}
			}
			
			Collections.sort(rr, comp);
	
			if(!rr.isEmpty()){
				ResultRegistry minrr = rr.get(0);
				System.out.println("\nCross Chosen: ( "+minr.getNum()+" "+minrr.getNum()+" ) ");
				// nature product between minimal result and its minimal rel. result
				minr.setResult(cross.naturalJoin(minr.getResult(), minr.getVariables(), minrr.getResult(), minrr.getVariables()));
				System.gc();
				
				// remove minrr in all relevant variables and add minr
				List<String> minrrv = minrr.getVariables();
				for(int i = 0; i < minrrv.size(); i++){
					SelectVariable sv = variables.get(minrrv.get(i));
					
					boolean b1 = sv.removeRelResult(minrr);
					
					boolean b2 = sv.addRelResult(minr);
					System.out.print("\nVariable "+minrrv.get(i)+" remove relevant result "+minrr.getNum()+": "+b1);
					System.out.print("\nVariable "+minrrv.get(i)+" add relevant result "+minr.getNum()+": "+b2);
				}
				
				// remove minrr 
				boolean b3 = results.remove(minrr);
				System.out.print("\nRemove result "+minrr.getNum()+": "+b3+"\n");
			}
			else{
				boolean b4 = indepResults.add(minr);
				results.remove(minr);
				System.out.print("\nRemove independent result "+minr.getNum()+": "+b4+"\n");
			}
		}
		// nature product with all independent results
		ResultRegistry ret = results.get(0);
		for(int i = 0; i < indepResults.size(); i++){
			ResultRegistry ir = indepResults.get(i);
			System.out.println("\nIndepCross Chosen: ( "+ret.getNum()+" "+ir.getNum()+" ) ");
			ret.setResult(cross.naturalJoin(ret.getResult(), ret.getVariables(), ir.getResult(), ir.getVariables()));
			System.gc();
		}
	}*/
	
	/*class ResultComparator implements Comparator <ResultRegistry>{
		
		@Override
		public int compare(ResultRegistry r1, ResultRegistry r2){
			return r1.getSize() - r2.getSize();
		}
		
	}*/
}
