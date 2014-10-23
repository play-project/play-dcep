/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.util;


/**
 * Information of a BDPL array. It is the value of BDPLArrayTable.
 * 
 * @author ningyuan 
 * 
 * Jun 27, 2014
 *
 */
public class BDPLArrayTableEntry {
	
	/*
	 * 
	 */
	private String name;
	
	/*
	 * BDPL array object
	 */
	private BDPLArray array;
	
	/*
	 * BDPL array type
	 */
	private BDPLArrayType type;
	
	/*
	 * declaration text of this BDPL array in query
	 */
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
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
