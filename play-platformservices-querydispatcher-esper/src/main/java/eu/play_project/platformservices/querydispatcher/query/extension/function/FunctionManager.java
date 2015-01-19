/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import eu.play_project.platformservices.querydispatcher.query.extension.function.implement.ArrhythmiaRRClassifier;
import eu.play_project.platformservices.querydispatcher.query.extension.function.implement.SofThresholdingDB4;
import eu.play_project.platformservices.querydispatcher.query.extension.function.implement.DefaultExFunction;
import eu.play_project.platformservices.querydispatcher.query.extension.function.implement.JSONTOARRAY;
import eu.play_project.platformservices.querydispatcher.query.extension.function.implement.QRSDDerivate;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.ArrayFunction;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FilterFunction;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FilterFunctionLoadException;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FunctionParameterTypeException;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.FunctionTable;
import eu.play_project.platformservices.querydispatcher.query.extension.function.util.IFunction;

/**
 * @author ningyuan 
 * 
 * Aug 6, 2014
 *
 */
public class FunctionManager {
	
	//XXX 
	private static final String funPath = "D:/Neo/Materials/Codes/Java/EclipseWorkspace/play-dcep/play-platformservices-querydispatcher-esper/exFunctionLib";
	
	private static FunctionManager instance;
	
	private FunctionTable fTable = FunctionTable.getInstance();
	
	private FunctionClassLoader fLoader;
	
	
	private FunctionManager() {
		try{
			fLoader = new FunctionClassLoader(funPath);
		}
		catch(FileNotFoundException e){
			System.out.println(e.getMessage());
		}
	}
	
	public static FunctionManager getInstance(){
		if(instance == null){
			instance = new FunctionManager();
			return instance;
		}
		else{
			return instance;
		}
	}
	
	public void initiateTable() throws FunctionParameterTypeException{
		
		Class<DefaultExFunction> fc = DefaultExFunction.class;
		
		Method fm = null;
		for(Method m : fc.getMethods()){
			
			if(m.getName().equals(FilterFunction.METHOD_NAME)){
				fm = m;
				break;
			}
		}
		
		if(fm != null){
			FilterFunction ef = new FilterFunction(fc.getCanonicalName(), fc, fm.getParameterTypes(), fm.getReturnType());
			fTable.putFunction("http://events.event-processing.org/function/"+"average", ef);
		}
		
		/*
		 * function unmarshal json array
		 */
		Class<JSONTOARRAY> afc = JSONTOARRAY.class;
		
		fm = null;
		for(Method m : afc.getMethods()){
			
			if(m.getName().equals(ArrayFunction.METHOD_NAME)){
				fm = m;
				break;
			}
		}
		
		if(fm != null){
			ArrayFunction ef = new ArrayFunction(afc.getCanonicalName(), afc, fm.getParameterTypes(), fm.getReturnType());
			fTable.putFunction("http://events.event-processing.org/function/"+"unmarshal", ef);
		}
		
		/*
		 * function detect qrs
		 */
		Class<QRSDDerivate> afc1 = QRSDDerivate.class;
		
		fm = null;
		for(Method m : afc1.getMethods()){
			
			if(m.getName().equals(ArrayFunction.METHOD_NAME)){
				fm = m;
				break;
			}
		}
		
		if(fm != null){
			ArrayFunction ef = new ArrayFunction(afc1.getCanonicalName(), afc1, fm.getParameterTypes(), fm.getReturnType());
			fTable.putFunction("http://events.event-processing.org/function/"+"qrsde", ef);
		}
		
		/*
		 * function classify rr
		 */
		Class<ArrhythmiaRRClassifier> afc2 = ArrhythmiaRRClassifier.class;
		
		fm = null;
		for(Method m : afc2.getMethods()){
			
			if(m.getName().equals(ArrayFunction.METHOD_NAME)){
				fm = m;
				break;
			}
		}
		
		if(fm != null){
			ArrayFunction ef = new ArrayFunction(afc2.getCanonicalName(), afc2, fm.getParameterTypes(), fm.getReturnType());
			fTable.putFunction("http://events.event-processing.org/function/"+"arrclass", ef);
		}
		
		/*
		 * function DWT db4 wavelet
		 */
		Class<SofThresholdingDB4> afc3 = SofThresholdingDB4.class;
		
		fm = null;
		for(Method m : afc3.getMethods()){
			
			if(m.getName().equals(ArrayFunction.METHOD_NAME)){
				fm = m;
				break;
			}
		}
		
		if(fm != null){
			ArrayFunction ef = new ArrayFunction(afc3.getCanonicalName(), afc3, fm.getParameterTypes(), fm.getReturnType());
			fTable.putFunction("http://events.event-processing.org/function/"+"DWTdb4", ef);
		}
	}
	
	public String[][] listFunctions(){
		return fTable.list();
	}
	
	/**
	 * 
	 * @param fName the function signature for bdpl
	 * @param fcName the java class name of function
	 * @return
	 * @throws FilterFunctionLoadException 
	 * @throws  
	 */
	public boolean loadFunction(String fDecl, String fcName) throws FilterFunctionLoadException{
		//TODO array function
		
		if(fLoader == null){
			throw new FilterFunctionLoadException("Function class loader could not be found");
		}
		
		List<Boolean> fParaTypes = new ArrayList<Boolean>();
		String fName = parseFunctionDeclaration(fDecl, fParaTypes);
		
		Class fc = null;
		try {
			// XXX fc will be always loaded, no matter if fc has proper parameters.
			fc = fLoader.loadClass(fcName);
		} catch (ClassNotFoundException e) {
			throw new FilterFunctionLoadException(e.getMessage());
		}
		
		Method fm = null;
		for(Method m : fc.getMethods()){
			if(m.getName().equals(FilterFunction.METHOD_NAME)){
				fm = m;
				break;
			}
		}
		
		if(fm != null){
			Class [] fmParaTypes = fm.getParameterTypes();
			
			if(checkParaTypeCompatability(fParaTypes, fmParaTypes)){
				IFunction ef = fTable.getFunction(fName);
					System.out.println("fpara: "+fm.getParameterTypes());
					System.out.println("fret: "+fm.getReturnType());
				if(ef != null){
					if(ef.compareParameterTypes(fmParaTypes)){
						try {
							fTable.putFunction(fName, new FilterFunction(fcName, fc, fm.getParameterTypes(), fm.getReturnType()));
						} catch (FunctionParameterTypeException e) {
							throw new FilterFunctionLoadException(e.getMessage());
						}
					}
					else{
						throw new FilterFunctionLoadException("Function "+fName+" is loaded before and the new one is declared with incompatable parameter types");
					}
				}
				else{
					try {
						ef = new FilterFunction(fcName, fc, fm.getParameterTypes(), fm.getReturnType());
						fTable.putFunction(fName, ef);
					} catch (FunctionParameterTypeException e) {
						throw new FilterFunctionLoadException(e.getMessage());
					}
				}
			}
			else{
				throw new FilterFunctionLoadException("Function parameter types declared are not compatable with method defined in class "+fcName);
			}
		}
		else{
			throw new FilterFunctionLoadException("Could not find \'"+FilterFunction.METHOD_NAME+"\' method in "+fcName);
		}
		
		return false;
	}
	
	private boolean checkParaTypeCompatability(List<Boolean> fTypes, Class[] fmTypes){
		// is fmTypes null, when there is no parameter???
		if(fTypes.size() == fmTypes.length){
			for(int i = 0; i < fmTypes.length; i++){
				if(Boolean.valueOf(fTypes.get(i)) != fmTypes[i].isArray()){
					return false;
				}
			}
			return true;
		}
		else{
			return false;
		}
	}
	
	private String parseFunctionDeclaration(String s, List<Boolean> paraTypes) throws FilterFunctionLoadException{
		
		/*
		 * ( [] . ) 
		 */
		
		char c;
		int state = 0;
		StringBuffer fn = new StringBuffer();
		
		for(int i = 0; i < s.length(); i++){
			c = s.charAt(i);
			
			switch(state){
				// start
				case 0:{
					if(c == ' '){
						continue;
					}
					else if(Character.isLetter(c) || Character.isDigit(c)){
						fn.append(c);
						state = 1;
					}
					else{
						throw new FilterFunctionLoadException("Bad format of function declaration");
					}
					break;
				}
				// in function name
				case 1:{
					if(c == '('){
						state = 2;
					}
					else {
						fn.append(c);
					}
					
					break;
				}
				// in parameter types
				case 2:{
					if(c == ' '){
						continue;
					}
					else if(c == '['){
						state = 3;
					}
					else if(c == '.'){
						paraTypes.add(false);
					}
					else if(c == ')'){
						
						return fn.toString();
					}
					else{
						throw new FilterFunctionLoadException("Bad format of function declaration");
					}
					break;
				}
				// in array type
				case 3:{
					if(c == ' '){
						continue;
					}
					else if(c == ']'){
						paraTypes.add(true);
						state = 2;
					}
					else{
						throw new FilterFunctionLoadException("Bad format of function declaration");
					}
					break;
				}
			}
		}
		
		throw new FilterFunctionLoadException("Bad format of function declaration");
	}
	
	
}
