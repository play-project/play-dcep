/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.construct.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The class of the construct template. This is a neutral representation of
 * the construct clause of a bdpl query. According to the construct template,
 * response events can be build correctly.
 * 
 * 
 * @author ningyuan 
 * 
 * Oct 20, 2014
 *
 */
public class ConstructTemplate {
	
	// compulsory predicate of a construct template. The rdf type of the created event.
	private String rdfType;
	
	// compulsory predicate of a construct template. The stream name of the created event.
	private String streamName;
	
	// list of subjects of all triples in the construct template.
	private List<TripleSubject> subjects = new ArrayList<TripleSubject>();
	
	public volatile int count = 0;
	
	public void accept(ConstructTemplateVisitor visitor){
		visitor.visit(this);
	}
	
	public String getRdfType() {
		return this.rdfType;
	}

	public void setRdfType(String rdfType) {
		this.rdfType = rdfType;
	}
	
	public String getStreamName() {
		return this.streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}
	
	public Iterator<TripleSubject> getSubjectsIterator(){
		return subjects.iterator();
	}
	
	public void addSubject(TripleSubject sub){
		for(int i = 0; i < subjects.size(); i++){
			TripleSubject temp = subjects.get(i);
			if(isSubjectEqual(sub, temp)){
				List<TriplePredicate> pres = sub.getPredicates();
				for(int j = 0; j < pres.size(); j++){
					temp.getPredicates().add(pres.get(j));
				}
				return;
			}
		}
		
		subjects.add(sub);
	}
	
	private boolean isSubjectEqual(TripleSubject sub1, TripleSubject sub2){
		if(sub1.getType() == sub2.getType()){
			List<String> con1 = sub1.getContent();
			List<String> con2 = sub2.getContent();
			if(con1.size() == con2.size()){
				for(int i = 0; i < con1.size(); i++){
					String s1 = con1.get(i);
					String s2 = con2.get(i);
					if((s1 != null && s2 != null && s1.equals(s2)) || (s1 == null && s2 == null)){
						continue;
					}
					else{
						return false;
					}
				}
				
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
}
