/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function;



/**
 * @author ningyuan 
 * 
 * Jun 11, 2014
 *
 */
public class ExFunction {
	
	private final String name;
	
	private final Class[] paras;
	
	public ExFunction(String n, Class[] p){
		if(n == null && p == null){
			throw new IllegalArgumentException("Function name or parameters could not be null.");
		}
		
		name = n;
		paras = p;
	}
	
	public String getName() {
		return this.name;
	}

	public Class[] getParas() {
		return this.paras;
	}
	
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

		ExFunction c = (ExFunction)o;
		
		
		if(name.equals(c.getName())){
				//System.out.println("ExFunction.equals(): "+name+"    "+c.getName());
			Class[] cp = c.getParas();
				
			if(paras.length == cp.length){
				for(int i = 0; i < paras.length; i++){
					if(!paras[i].getCanonicalName().equals(cp[i].getCanonicalName())){
						return false;
					}
						//System.out.println("ExFunction.equals(): "+paras[i].getCanonicalName()+"    "+cp[i].getCanonicalName());
				}
				return true;
			}
		}			
			
		return false;
	}
}
