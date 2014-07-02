/**
 * 
 */
package eu.play_project.platformservices.bdpl.parser.array.test;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.array.BDPLArrayElement;
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
		BDPLArrayElement head = new BDPLArrayElement(new Object[]{"1"});
		BDPLArray sArray = new BDPLArray(head);
		sArray.write(new Object[]{"1"});
	}
	
	@Test
	public void testStaticArray2() {
		BDPLArray sArray = new BDPLArray(null);
	}
	
	@Test
	public void testStaticArray3(){
		BDPLArrayElement head = new BDPLArrayElement(new Object[]{"1"});
		BDPLArray sArray = new BDPLArray(head);
		Object[][] result = sArray.read();
		Object[][] expected = new Object[][] {{"1"}};
		assertArrayEquals(expected, result);
	}
	
	@Test
	public void testStaticArray4(){
		BDPLArrayElement head = new BDPLArrayElement(new Object[]{"1"});
		BDPLArrayElement tail = new BDPLArrayElement(new Object[]{"2"});
		head.setNext(tail);
		BDPLArray sArray = new BDPLArray(head);
		Object[][] result = sArray.read();
		Object[][] expected = new Object[][] {{"1"}, {"2"}};
		assertArrayEquals(expected, result);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDynamicArray1() {
		BDPLArrayElement head = new BDPLArrayElement(new Object[]{"1"});
		BDPLArray dArray = new BDPLArray(0, head);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDynamicArray2() {
		BDPLArrayElement head = new BDPLArrayElement(new Object[]{"1"});
		BDPLArrayElement tail = new BDPLArrayElement(new Object[]{"2"});
		head.setNext(tail);
		BDPLArray dArray = new BDPLArray(1, head);
	}
	
	@Test(expected = BDPLArrayException.class)
	public void testDynamicArray3() throws BDPLArrayException {
		BDPLArray dArray = new BDPLArray(1, null);
		dArray.write(new Object[][]{{"1"}, {"2"}});
	}
	
	@Test
	public void testDynamicArray4() throws BDPLArrayException {
		BDPLArray dArray = new BDPLArray(1, null);
		dArray.write(new Object[]{"1"});
		Object[][] result = dArray.read();
		Object[][] expected = new Object[][] {{"1"}};
		assertArrayEquals(expected, result);
		
		dArray.write(new Object[]{"2"});
		result = dArray.read();
		expected[0][0] = "2";
		assertArrayEquals(expected, result);
	}
	
	@Test
	public void testDynamicArray5() throws BDPLArrayException {
		BDPLArray dArray = new BDPLArray(2, null);
		dArray.write(new Object[]{"1"});
		Object[][] result = dArray.read();
		Object[][] expected = new Object[][] {{"1"}};
		assertArrayEquals(expected, result);
		
		dArray.write(new Object[]{"2"});
		result = dArray.read();
		expected = new Object[][] {{"1"}, {"2"}};
		assertArrayEquals(expected, result);
		
		dArray.write(new Object[]{"3"});
		result = dArray.read();
		expected = new Object[][] {{"2"}, {"3"}};
		assertArrayEquals(expected, result);
	}
	
	@Test
	public void testDynamicArray6() throws BDPLArrayException {
		BDPLArray dArray = new BDPLArray(4, null);
		dArray.write(new Object[][] {{"1"}, {"2"}});
		Object[][] result = dArray.read();
		Object[][] expected = new Object[][] {{"1"}, {"2"}};
		assertArrayEquals(expected, result);
		
		dArray.write(new Object[]{"3"});
		result = dArray.read();
		expected = new Object[][] {{"1"}, {"2"}, {"3"}};
		assertArrayEquals(expected, result);
		
	}
	
	@Test
	public void testDynamicArray7() throws BDPLArrayException {
		BDPLArray dArray = new BDPLArray(4, null);
		dArray.write(new Object[]{"1"});
		Object[][] result = dArray.read();
		Object[][] expected = new Object[][] {{"1"}};
		assertArrayEquals(expected, result);
		
		dArray.write(new Object[][] {{"2"}, {"3"}});
		result = dArray.read();
		expected = new Object[][] {{"1"}, {"2"}, {"3"}};
		assertArrayEquals(expected, result);
		
	}
}
