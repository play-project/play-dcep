/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.generation.construct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import com.google.gson.Gson;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTable;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.bdpl.parser.util.BDPLConstants;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.ConstructTemplate;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.ConstructTemplateVisitor;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.TripleObject;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.TriplePredicate;
import eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util.TripleSubject;

/**
 * @author ningyuan 
 * 
 * Oct 20, 2014
 *
 */
public class ConstructTemplateFiller extends ConstructTemplateVisitor{
	
	private Model model; 
	
	private List<Map<String, String[]>> varBindings;
	
	private Map<String, String[][][]> dArrays;
	
	private BDPLArrayTable arrayTable;
	
	private DatatypeFactory dtf;
	/**
	 * 
	 * @param vars (must not be null)
	 * @param arrays (must not be null)
	 */
	public ConstructTemplateFiller(List<Map<String, String[]>> vbs, Map<String, String[][][]> dArrays, BDPLArrayTable arrayTable){
		varBindings = vbs;
		this.dArrays = dArrays;
		this.arrayTable = arrayTable;
		
		try {
			dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void updateVariables(List<Map<String, String[]>> vbs, Map<String, String[][][]> dArrays){
		varBindings = vbs;
		this.dArrays = dArrays;
	}
	
	public Model getModel(){
		return model;
	}
	
	@Override
	public void visit(ConstructTemplate template){
		Iterator<TripleSubject> it = template.getSubjectsIterator();
		
		while(it.hasNext()){
			TripleSubject sub = it.next();
			
			switch(sub.getType()){
				case BDPLConstants.TYPE_IRI : {
					URI subject;
					
					String iri = sub.getContent().get(0);
					// subjects are different in a construct template
					if(iri.equals(BDPLConstants.URI_CONSTRUCT_SUBJECT)){
						subject = new URIImpl(template.getStreamName()+template.count);
						template.count++;
						
						if(model == null){
							model = new LinkedHashModel();
						}
						model.add(subject, new URIImpl(BDPLConstants.URI_ENDTIME), new LiteralImpl(dtf.newDuration(System.currentTimeMillis()).toString()));
						
					}
					else{
						subject = new URIImpl(iri);
					}
					
					List<TriplePredicate> pres = sub.getPredicates();
					for(int i = 0; i < pres.size(); i++){
						TriplePredicate pre = pres.get(i);
						
						List<URI> predicates = getPredicate(pre);
						for(URI predicate : predicates){
							List<TripleObject> objs = pre.getObjects();
							for(int j = 0; j < objs.size(); j++){
								TripleObject obj = objs.get(j);
								
								List<Value> objects = getObject(obj);
								for(Value object : objects){
									
									if(model == null){
										model = new LinkedHashModel();
									}
									model.add(subject, predicate, object);
									
								}
							}
						}
					}
					
					break;
				}
				case BDPLConstants.TYPE_VAR : {
					for(int m = 0; m < varBindings.size(); m++){
						Map<String, String[]> binding = varBindings.get(m);
						
						String [] var = binding.get(sub.getContent().get(0));
						if(var != null){
							int type = Integer.valueOf(var[0]);
							
							if(type == BDPLConstants.TYPE_IRI){
								URI subject = new URIImpl(var[1]);
								
								List<TriplePredicate> pres = sub.getPredicates();
								for(int i = 0; i < pres.size(); i++){
									TriplePredicate pre = pres.get(i);
									
									List<URI> predicates = getPredicate(pre);
									for(URI predicate : predicates){
										List<TripleObject> objs = pre.getObjects();
										for(int j = 0; j < objs.size(); j++){
											TripleObject obj = objs.get(j);
											
											List<Value> objects = getObject(obj);
											for(Value object : objects){
												
												if(model == null){
													model = new LinkedHashModel();
												}
												model.add(subject, predicate, object);
												
											}
										}
									}
								}
							}
							else if(type == BDPLConstants.TYPE_BN){
								//TODO
							}
						}
					}
					break;
				}
				case BDPLConstants.TYPE_BN : {
					//TODO
					break;
				}
				case BDPLConstants.TYPE_BNL : {
					//TODO
					break;
				}
				default : {
					
				}
			}
		}
	}
	
	private List<URI> getPredicate(TriplePredicate pre){
		List<URI> ret = new ArrayList<URI>();
		
		switch(pre.getType()){
			case BDPLConstants.TYPE_IRI :{
				ret.add(new URIImpl(pre.getContent().get(0)));
			}
			case BDPLConstants.TYPE_VAR :{
				for(int m = 0; m < varBindings.size(); m++){
					Map<String, String[]> binding = varBindings.get(m);
					
					String [] var = binding.get(pre.getContent().get(0));
					if(var != null){
						int type = Integer.valueOf(var[0]);
						
						if(type == BDPLConstants.TYPE_IRI){
							ret.add(new URIImpl(var[1]));
						}
					}
				}
				
				break;
			}
			default :{
				
			}
		}
		
		return ret;
	}
	
	private List<Value> getObject(TripleObject obj){
		List<Value> ret = new ArrayList<Value>();
		
		switch(obj.getType()){
			case BDPLConstants.TYPE_IRI :{
				ret.add(new URIImpl(obj.getContent().get(0)));
			}
			case BDPLConstants.TYPE_VAR :{
				for(int m = 0; m < varBindings.size(); m++){
					Map<String, String[]> binding = varBindings.get(m);
				
					String [] var = binding.get(obj.getContent().get(0));
					if(var != null){
						int type = Integer.valueOf(var[0]);
					
						if(type == BDPLConstants.TYPE_IRI){
							ret.add(new URIImpl(var[1]));
						}
						else if(type == BDPLConstants.TYPE_LITERAL){
						
							//TODO data type
							ret.add(new LiteralImpl(var[1]));
						}
						else if(type == BDPLConstants.TYPE_BN){
							//TODO
						}
					}
				}
			
				break;
			}
			case BDPLConstants.TYPE_LITERAL :{
				List<String> con = obj.getContent();
				if(con.get(1) != null){
					ret.add(new LiteralImpl(con.get(0), con.get(1)));
				}
				else if(con.get(2) != null){
					ret.add(new LiteralImpl(con.get(0), new URIImpl(con.get(2))));
				}
				else{
					ret.add(new LiteralImpl(con.get(0)));
				}
				
				break;
			}
			case BDPLConstants.TYPE_ARRAY :{
				List<String> con = obj.getContent();
				if(con.get(0) != null){
					//TODO function
				}
				else{
					String [][][] a = dArrays.get(con.get(1));
					if(a == null){
						BDPLArrayTableEntry ate = arrayTable.get(con.get(1));
						if(ate != null){
							a = ate.getArray().read();
						}
					}
					
					if(a != null && a.length > 0){
						Gson gson = new Gson();
						String json;
						
						int dimension = a[0].length;
						
						if(dimension == 1){
							String [] temp = new String[a.length];
							for(int i = 0; i < a.length; i++){
								if(Integer.valueOf(a[i][0][0]) == BDPLConstants.TYPE_LITERAL){
									temp[i] = a[i][0][2];
								}
								else{
									temp[i] = a[i][0][1];
								}
							}
							
							json = gson.toJson(temp);
						}
						else{
							String [][] temp = new String[a.length][a[0].length];
							for(int i = 0; i < a.length; i++){
								for(int j = 0; j < a[0].length; j++){
									if(Integer.valueOf(a[i][j][0]) == BDPLConstants.TYPE_LITERAL){
										temp[i][j] = a[i][j][2];
									}
									else{
										temp[i][j] = a[i][j][1];
									}
								}
							}
							
							json = gson.toJson(temp);
							
						}
						
						ret.add(new LiteralImpl(json, new URIImpl(BDPLConstants.URI_TYPE_JSON_ARRAY)));
					}
				}
				
				break;
			}
			case BDPLConstants.TYPE_BN :{
				//TODO
				break;
			}
			case BDPLConstants.TYPE_BNL :{
				//TODO
				break;
			}
			case BDPLConstants.TYPE_COLLECTION :{
				//TODO
				break;
			}
			default :{
				
			}
		}
		
		return ret;
	}
}
