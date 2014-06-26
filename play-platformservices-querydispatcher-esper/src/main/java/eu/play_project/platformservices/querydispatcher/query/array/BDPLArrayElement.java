/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.array;

/**
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class BDPLArrayElement {
	
	private BDPLArrayElement next = null;
	
	private String content;
	
	public BDPLArrayElement(String content){
		this.content = content;
	}
	
	public String getContent(){
		return content;
	}
	
	public void setContent(String content){
		this.content = content;
	}

	public BDPLArrayElement getNext() {
		return this.next;
	}

	public void setNext(BDPLArrayElement next) {
		this.next = next;
	}
	
}
