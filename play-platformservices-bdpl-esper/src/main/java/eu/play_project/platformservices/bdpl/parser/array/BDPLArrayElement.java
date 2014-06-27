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
	
	private String content;
	
	public BDPLArrayElement(String content){
		if(content == null){
			throw new IllegalArgumentException();
		}
		this.content = content;
	}
	
	public String getContent(){
		return content;
	}
	
	public void setContent(String content){
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
