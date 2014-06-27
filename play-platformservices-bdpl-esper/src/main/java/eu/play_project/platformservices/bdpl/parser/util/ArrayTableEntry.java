/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.util;

import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;

/**
 * @author ningyuan 
 * 
 * Jun 27, 2014
 *
 */
public class ArrayTableEntry {
	
	private BDPLArray array;
	
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
}
