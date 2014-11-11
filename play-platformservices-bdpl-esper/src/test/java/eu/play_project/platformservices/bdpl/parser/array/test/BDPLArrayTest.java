/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.array.test;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.play_project.platformservices.bdpl.parser.util.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayException;



/**
 * @author ningyuan 
 * 
 * Jun 26, 2014
 *
 */
public class BDPLArrayTest {
	
	
	@Test(expected = BDPLArrayException.class)
	public void testStaticArray1() throws BDPLArrayException {
		
		BDPLArray sArray = new BDPLArray(null);
		sArray.write(new String[][]{{"1", null}});
	}
	
	@Test
	public void testStaticArray2() {
		BDPLArray sArray = new BDPLArray(null);
		String[][][] result = sArray.read();
		String[][][] expected = new String[][][] {};
		assertArrayEquals(expected, result);
	}
	
	@Test
	public void testStaticArray3(){
		
		BDPLArray sArray = new BDPLArray(new String[][][]{{{"1", null}}});
		String[][][] result = sArray.read();
		String[][][] expected = new String[][][] {{{"1", null}}};
		assertArrayEquals(expected, result);
	}
	
	@Test
	public void testStaticArray4(){
		
		BDPLArray sArray = new BDPLArray(new String[][][]{{{"1", "\'1\'@en"}}, {{"2", "\'2\'@en"}}});
		String[][][] result = sArray.read();
		String[][][] expected = new String[][][]{{{"1", "\'1\'@en"}}, {{"2", "\'2\'@en"}}};
		assertArrayEquals(expected, result);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDynamicArray1() {
		
		new BDPLArray(0, new String[][][]{{{"1", null}}});
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDynamicArray2() {
		
		new BDPLArray(1, new String[][][]{{{"1", null}}, {{"2", null}}});
	}
	
	@Test(expected = BDPLArrayException.class)
	public void testDynamicArray3() throws BDPLArrayException {
		BDPLArray dArray = new BDPLArray(1, null);
		dArray.write(new String[][][]{{{"1", null}}, {{"2", null}}});
	}
	
	@Test
	public void testDynamicArray4() throws BDPLArrayException {
		BDPLArray dArray = new BDPLArray(1, null);
		dArray.write(new String[][]{{"1", null}});
		String[][][] result = dArray.read();
		String[][][] expected = new String[][][] {{{"1", null}}};
		assertArrayEquals(expected, result);
		
		dArray.write(new String[][]{{"2", null}});
		result = dArray.read();
		expected[0][0][0] = "2";
		assertArrayEquals(expected, result);
	}
	
	@Test
	public void testDynamicArray5() throws BDPLArrayException {
		BDPLArray dArray = new BDPLArray(2, null);
		dArray.write(new String[][]{{"1", null}});
		String[][][] result = dArray.read();
		String[][][] expected = new String[][][] {{{"1", null}}};
		assertArrayEquals(expected, result);
		
		dArray.write(new String[][]{{"2", null}});
		result = dArray.read();
		expected = new String[][][] {{{"1", null}}, {{"2", null}}};
		assertArrayEquals(expected, result);
		
		dArray.write(new String[][]{{"3", null}});
		result = dArray.read();
		expected = new String[][][] {{{"2", null}}, {{"3", null}}};
		assertArrayEquals(expected, result);
	}
	
	@Test
	public void testDynamicArray6() throws BDPLArrayException {
		BDPLArray dArray = new BDPLArray(4, null);
		dArray.write(new String[][][] {{{"1", null}}, {{"2", null}}});
		String[][][] result = dArray.read();
		String[][][] expected = new String[][][] {{{"1", null}}, {{"2", null}}};
		assertArrayEquals(expected, result);
		
		dArray.write(new String[][]{{"3", null}});
		result = dArray.read();
		expected = new String[][][] {{{"1", null}}, {{"2", null}}, {{"3", null}}};
		assertArrayEquals(expected, result);
		
	}
	
	@Test
	public void testDynamicArray7() throws BDPLArrayException {
		BDPLArray dArray = new BDPLArray(4, null);
		dArray.write(new String[][]{{"1", null}});
		String[][][] result = dArray.read();
		String[][][] expected = new String[][][] {{{"1", null}}};
		assertArrayEquals(expected, result);
		
		dArray.write(new String[][][] {{{"2", null}}, {{"3", null}}});
		result = dArray.read();
		expected = new String[][][] {{{"1", null}}, {{"2", null}}, {{"3", null}}};
		assertArrayEquals(expected, result);
		
	}
}
