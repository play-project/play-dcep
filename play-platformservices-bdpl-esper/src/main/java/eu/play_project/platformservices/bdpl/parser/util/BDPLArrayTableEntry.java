/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.util;

import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.array.BDPLArrayType;

/**
 * @author ningyuan 
 * 
 * Jun 27, 2014
 *
 */
public class BDPLArrayTableEntry {
	
	private BDPLArray array;

	private BDPLArrayType type;
	
	private String source;
	
	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public BDPLArray getArray() {
		return this.array;
	}

	public void setArray(BDPLArray array) {
		this.array = array;
	}
	
	public BDPLArrayType getType() {
		return this.type;
	}
	
	public void setType(BDPLArrayType type) {
		this.type = type;
	}

}
