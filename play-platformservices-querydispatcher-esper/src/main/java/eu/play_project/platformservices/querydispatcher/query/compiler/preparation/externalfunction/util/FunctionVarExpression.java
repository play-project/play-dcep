/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.util;

import eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IFunctionExpression;
import eu.play_project.platformservices.querydispatcher.query.compiler.util.BDPLFilterException;

/**
 * @author ningyuan 
 * 
 * Aug 11, 2014
 *
 */
public class FunctionVarExpression implements IFunctionExpression<VariableBinder> {
	
	private final String vn;
	
	private VariableBinder vb;
	
	public FunctionVarExpression(String varName){
		vn = varName;
	}
	
	@Override
	public IFunctionExpression<VariableBinder> copy() {
		return new FunctionVarExpression(vn);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#setDataObject(java.lang.Object)
	 */
	@Override
	public void setDataObject(VariableBinder data) {
		vb = data;
	}

	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValue()
	 */
	@Override
	public Object getValue() throws BDPLFilterException{
		Object ret = vb.getVarValue(vn);
		if(ret != null){
			return ret;
		}
		else{
			throw new BDPLFilterException("Value of ariable "+vn+" could not be binded during evaluating external function expression");
		}
	}

	/* (non-Javadoc)
	 * @see eu.play_project.platformservices.querydispatcher.query.compiler.preparation.externalfunction.IExternalFunctionExpression#getValueType()
	 */
	@Override
	public Class getValueType() {
		/*
		 * var expression's return type is null, because the type of var could not be determined at compile time
		 */
		return null;
	}

}
