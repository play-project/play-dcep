/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.extension.function.implement;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.ntriples.NTriplesUtil;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author ningyuan 
 * 
 * Oct 15, 2014
 *
 */
public class JSONTOARRAY {
	
	public String[][][] execute(String json){
		String [][][] ret = null;
		Gson gson = new Gson();
		
		try{
			String [] array = gson.fromJson(json, String[].class);
			ValueFactory vf = new ValueFactoryImpl();
			//XXX only support one dimension
			ret = new String[array.length][1][2];
				System.out.println("Json to array:");
			for(int i = 0; i < array.length; i++){
				try{
					Value v = NTriplesUtil.parseValue(array[i], vf);
					
					if(v instanceof Literal){
						ret[i][0][0] = ((Literal) v).getLabel();
					}
					else{
						ret[i][0][0] = v.toString();
					}
						
					ret[i][0][1] = v.toString();
				}
				catch(IllegalArgumentException e){
					ret[i][0][0] = array[i];
					ret[i][0][1] = array[i];
				}
					System.out.print(ret[i][0][1]+"   "+ret[i][0][0]+";  ");
			}
				System.out.println();
		}
		catch(JsonSyntaxException e){
			return null;
		}
		return ret;
	}
	
}
