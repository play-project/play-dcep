/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.util;

/**
 * Cast parameters from events to parameters for external functions
 * 
 * 
 * @author ningyuan 
 * 
 * Aug 8, 2014
 *
 */
public class FunctionParameterCastor {
	
	public static final int TYPE_INT = 0, TYPE_DOUBLE = 1, TYPE_BOOLEAN = 2, TYPE_STR = 3, 
			TYPE_ARRAY_INT = 10, TYPE_ARRAY_DOUBLE = 11, TYPE_ARRAY_STR = 12;
	
	public static Object cast(int type, Object o) throws FunctionParameterCastException{
		switch(type){
			case TYPE_ARRAY_DOUBLE:{
				try{
						//System.out.println("ParameterCastor: cast double array");
					return castDoubleArray((String[][][])o);
				}
				catch(Exception e){
					throw new FunctionParameterCastException(e.getMessage());
				}
			}
			case TYPE_ARRAY_INT:{
				try{
						//System.out.println("ParameterCastor: cast int array");
					return castIntArray((String[][][])o);
				}
				catch(Exception e){
					throw new FunctionParameterCastException(e.getMessage());
				}
			}
			case TYPE_ARRAY_STR:{
				try{
						//System.out.println("ParameterCastor: cast string array");
					return castStringArray((String[][][])o);
				}
				catch(Exception e){
					throw new FunctionParameterCastException(e.getMessage());
				}
			}
			case TYPE_DOUBLE:{
				try{
						//System.out.println("ParameterCastor: cast double");
					return Double.valueOf((String)o);
				}
				catch(Exception e){
					throw new FunctionParameterCastException(e.getMessage());
				}
			}
			case TYPE_INT:{
				try{
						//System.out.println("ParameterCastor: cast int");
					return Integer.valueOf((String)o);
				}
				catch(Exception e){
					throw new FunctionParameterCastException(e.getMessage());
				}
			}
			case TYPE_BOOLEAN:{
				try{
						//System.out.println("ParameterCastor: cast boolean");
					return Boolean.valueOf((String)o);
				}
				catch(Exception e){
					throw new FunctionParameterCastException(e.getMessage());
				}
			}
			case TYPE_STR:{
				return o;
			}
			default: {
				throw new FunctionParameterCastException("Not supported casting type");
			}
		}
	}
	
	/**
	 * 
	 * @param array (must not be null, must not be empty)
	 * @return (never be null)
	 */
	private static Object castDoubleArray(String[][][] array) throws FunctionParameterCastException{
	
		int dimension = array[0].length;
		
		if(array[0].length > 1){
			double[][] ret = new double[array.length][dimension];
			
			for(int i = 0; i < array.length; i++){
				for(int j = 0; j < dimension; j++){
					try{
						ret[i][j] = Double.valueOf(array[i][j][1]);
					}
					catch(NumberFormatException e){
						throw new FunctionParameterCastException("External function parameter casting exception.");
					}
				}
			}
			
			return ret;
		}
		else{
			double[]  ret = new double[array.length];
			
			for(int i = 0; i < array.length; i++){
				try{
					ret[i] = Double.valueOf(array[i][0][1]);
				}
				catch(NumberFormatException e){
					throw new FunctionParameterCastException("External function parameter casting exception.");
				}
			}
			
			return ret;
		}
		
	}
	
	/**
	 * 
	 * @param array (must not be null, must not be empty)
	 * @return (never be null)
	 */
	private static Object castStringArray(String[][][] array) throws FunctionParameterCastException{
		int dimension = array[0].length;
		
		if(array[0].length > 1){
			String[][] ret = new String[array.length][dimension];
			
			for(int i = 0; i < array.length; i++){
				for(int j = 0; j < dimension; j++){
					ret[i][j] = array[i][j][1];
				}
			}
			return ret;
		}
		else{
			String[] ret = new String[array.length];
			
			for(int i = 0; i < array.length; i++){
				ret[i] = array[i][0][1];
			}
			
			return ret;
		}
	}
	
	/**
	 * 
	 * @param array (must not be null, must not be empty)
	 * @return (never be null)
	 */
	private static Object castIntArray(String[][][] array) throws FunctionParameterCastException{
	
		int dimension = array[0].length;
		
		if(array[0].length > 1){
			int[][] ret = new int[array.length][dimension];
			
			for(int i = 0; i < array.length; i++){
				for(int j = 0; j < dimension; j++){
					try{
						ret[i][j] = Integer.valueOf(array[i][j][1]);
					}
					catch(NumberFormatException e){
						throw new FunctionParameterCastException("External function parameter casting exception.");
					}
				}
			}
			
			return ret;
		}
		else{
			int[]  ret = new int[array.length];
			
			for(int i = 0; i < array.length; i++){
				try{
					ret[i] = Integer.valueOf(array[i][0][1]);
				}
				catch(NumberFormatException e){
					throw new FunctionParameterCastException("External function parameter casting exception.");
				}
			}
			
			return ret;
		}
		
	}
}
