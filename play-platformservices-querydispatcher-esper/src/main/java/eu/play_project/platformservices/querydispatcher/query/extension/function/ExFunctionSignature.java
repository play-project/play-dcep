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
public class ExFunctionSignature {
	
	private final String name;
	
	private final Class[] paraTypes;
	
	public ExFunctionSignature(String n, Class[] p){
		if(n == null && p == null){
			throw new IllegalArgumentException("Function name or parameters could not be null.");
		}
		
		name = n;
		paraTypes = p;
	}
	
	public String getName() {
		return this.name;
	}

	public Class[] getParaTypes() {
		return this.paraTypes;
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

		ExFunctionSignature c = (ExFunctionSignature)o;
		
		
		if(name.equals(c.getName())){
				//System.out.println("ExFunction.equals(): "+name+"    "+c.getName());
			Class[] cp = c.getParaTypes();
				
			if(paraTypes.length == cp.length){
				for(int i = 0; i < paraTypes.length; i++){
					if(!paraTypes[i].getCanonicalName().equals(cp[i].getCanonicalName())){
						return false;
					}
						//System.out.println("ExFunction.equals(): "+paraTypes[i].getCanonicalName()+"    "+cp[i].getCanonicalName());
				}
				return true;
			}
		}			
			
		return false;
	}
}
