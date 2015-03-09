/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.implement;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.ntriples.NTriplesUtil;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import eu.play_project.platformservices.bdpl.parser.util.BDPLConstants;

/**
 * Transformer of JSON array syntaxes into variable array variables.
 * 
 * 
 * @author ningyuan 
 * 
 * Oct 15, 2014
 *
 */
public class JSONTOARRAY {
	
	public String[][][] execute(String json){
		String [][][] ret = null;
		Gson gson = new Gson();
		
		String [][] mArray = null;
		try{
			mArray = gson.fromJson(json, String[][].class);
		}catch(JsonSyntaxException e){}
		
		if(mArray != null){
			ValueFactory vf = new ValueFactoryImpl();
			
			if(mArray.length > 0){
				int dimension = mArray[0].length;
				ret = new String[mArray.length][dimension][3];
					
				for(int i = 0; i < mArray.length; i++){
					for(int j = 0; j < dimension; j++){
						try{
							Value v = NTriplesUtil.parseValue(mArray[i][j], vf);
							
							if(v instanceof Literal){
								ret[i][j][0] = String.valueOf(BDPLConstants.TYPE_LITERAL);
								ret[i][j][1] = ((Literal) v).getLabel();
								ret[i][j][2] = v.toString();
							}
							else if(v instanceof URI){
								ret[i][j][0] = String.valueOf(BDPLConstants.TYPE_IRI);
								ret[i][j][1] = v.toString();
							}
							else{
								ret[i][j][0] = String.valueOf(BDPLConstants.TYPE_UNKNOWN);
								ret[i][j][1] = v.toString();
							}
						}
						catch(IllegalArgumentException e){
							ret[i][j][0] = String.valueOf(BDPLConstants.TYPE_UNKNOWN);
							ret[i][j][1] = mArray[i][j];
						}
						
							//System.out.print(ret[i][j][1]+" ");
					}
						//System.out.println();
				}
					//System.out.println();
			}
		}
		else{
			String [] array = null;
			try{
				array = gson.fromJson(json, String[].class);
			}catch(JsonSyntaxException e){}
			
			if(array != null){
				ValueFactory vf = new ValueFactoryImpl();
				
				if(array.length > 0){
					
					ret = new String[array.length][1][3];
						
					for(int i = 0; i < array.length; i++){
						try{
							Value v = NTriplesUtil.parseValue(array[i], vf);
								
							if(v instanceof Literal){
								ret[i][0][0] = String.valueOf(BDPLConstants.TYPE_LITERAL);
								ret[i][0][1] = ((Literal) v).getLabel();
								ret[i][0][2] = v.toString();
							}
							else if(v instanceof URI){
								ret[i][0][0] = String.valueOf(BDPLConstants.TYPE_IRI);
								ret[i][0][1] = v.toString();
							}
							else{
								ret[i][0][0] = String.valueOf(BDPLConstants.TYPE_UNKNOWN);
								ret[i][0][1] = v.toString();
							}
						}
						catch(IllegalArgumentException e){
							ret[i][0][0] = String.valueOf(BDPLConstants.TYPE_UNKNOWN);
							ret[i][0][1] = array[i];
						}
							//System.out.print(ret[i][0][1]+" ");
					}
						//System.out.println();
				}
			}
		}
		
		return ret;
	}
	
}
