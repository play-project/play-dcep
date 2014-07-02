/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.array;


/**
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class BDPLArrayElement {
	
	private BDPLArrayElement next = null;
	
	private Object[] content;
	
	public BDPLArrayElement(Object[] content){
		if(content == null){
			throw new IllegalArgumentException();
		}
		this.content = content;
	}
	
	public Object[] getContent(){
		return content;
	}
	
	public void setContent(Object[] content){
		if(content == null){
			throw new IllegalArgumentException();
		}
		this.content = content;
	}

	public BDPLArrayElement getNext() {
		return this.next;
	}

	public void setNext(BDPLArrayElement next) {
		this.next = next;
	}
	
}
