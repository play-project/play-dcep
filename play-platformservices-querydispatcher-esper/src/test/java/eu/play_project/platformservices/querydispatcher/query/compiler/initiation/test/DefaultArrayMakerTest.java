package eu.play_project.platformservices.querydispatcher.query.compiler.initiation.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import eu.play_project.platformservices.bdpl.parser.array.BDPLArray;
import eu.play_project.platformservices.bdpl.parser.array.BDPLArrayType;
import eu.play_project.platformservices.bdpl.parser.util.BDPLArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array.DefaultArrayMaker;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.array.IArrayMaker;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.InitiateException;
import eu.play_project.platformservices.querydispatcher.query.compiler.initiation.util.SubQueryTable;

public class DefaultArrayMakerTest {
	
	private static IArrayMaker arrayMaker;
	
	private BDPLArrayTableEntry arrayTableEntry;
	
	@BeforeClass
	public static void setUp() throws RepositoryException, RDFParseException, IOException{
		arrayMaker = new DefaultArrayMaker();
	}
	
	
	@Before
	public void init(){
		arrayTableEntry = new BDPLArrayTableEntry();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testEXStatic1() {
		arrayTableEntry.setType(BDPLArrayType.STATIC_EXPLICITE);
		arrayTableEntry.setSource(null);
		
		try {
			arrayMaker.make(arrayTableEntry, null);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testEXStatic2() {
		arrayTableEntry.setType(BDPLArrayType.STATIC_EXPLICITE);
		arrayTableEntry.setSource("");
		
		try {
			arrayMaker.make(arrayTableEntry, null);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected = InitiateException.class)
	public void testEXStatic3() throws InitiateException {
		arrayTableEntry.setType(BDPLArrayType.STATIC_EXPLICITE);
		arrayTableEntry.setSource(" ; ");
		
		
		arrayMaker.make(arrayTableEntry, null);
		
	}
	
	@Test(expected = InitiateException.class)
	public void testEXStatic4() throws InitiateException {
		arrayTableEntry.setType(BDPLArrayType.STATIC_EXPLICITE);
		arrayTableEntry.setSource(" 1 2 ; 3 ");
		
		
		arrayMaker.make(arrayTableEntry, null);
		
	}
	
	@Test
	public void testEXStatic5() {
		arrayTableEntry.setType(BDPLArrayType.STATIC_EXPLICITE);
		arrayTableEntry.setSource(" 1 ");
		
		try {
			arrayMaker.make(arrayTableEntry, null);
			
			String[][] result = arrayTableEntry.getArray().read();
			String[][] expected = new String[][] {{"1"}};
			assertArrayEquals(expected, result);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEXStatic6() {
		arrayTableEntry.setType(BDPLArrayType.STATIC_EXPLICITE);
		arrayTableEntry.setSource(" 1; ");
		
		try {
			arrayMaker.make(arrayTableEntry, null);
			
			String[][] result = arrayTableEntry.getArray().read();
			String[][] expected = new String[][] {{"1"}};
			assertArrayEquals(expected, result);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEXStatic7() {
		arrayTableEntry.setType(BDPLArrayType.STATIC_EXPLICITE);
		arrayTableEntry.setSource(" 1 2 ");
		
		try {
			arrayMaker.make(arrayTableEntry, null);
			
			String[][] result = arrayTableEntry.getArray().read();
			String[][] expected = new String[][] {{"1", "2"}};
			assertArrayEquals(expected, result);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
	 
	@Test
	public void testEXStatic8() {
		arrayTableEntry.setType(BDPLArrayType.STATIC_EXPLICITE);
		arrayTableEntry.setSource(" 1 2 ; 3 4");
		
		try {
			arrayMaker.make(arrayTableEntry, null);
			
			String[][] result = arrayTableEntry.getArray().read();
			String[][] expected = new String[][] {{"1", "2"}, {"3", "4"}};
			assertArrayEquals(expected, result);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testQStatic1() {
		arrayTableEntry.setType(BDPLArrayType.STATIC_QUERY);
		arrayTableEntry.setSource("PREFIX : <http://events.event-processing.org/types/> PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> SELECT ?x WHERE { ?id :name ?x }");
		
		try {
			arrayMaker.make(arrayTableEntry, null);
			
			String[][] result = arrayTableEntry.getArray().read();
			String[][] expected = new String[][] {{"\"tom\"@en"}, {"\"jack\"@en"}, {"\"jane\"@en"}};
			assertArrayEquals(expected, result);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testQStatic2() {
		arrayTableEntry.setType(BDPLArrayType.STATIC_QUERY);
		arrayTableEntry.setSource("PREFIX : <http://events.event-processing.org/types/> PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> SELECT ?x ?y WHERE { ?z :name ?x. ?z :age ?y }");
		
		try {
			arrayMaker.make(arrayTableEntry, null);
			
			String[][] result = arrayTableEntry.getArray().read();
			String[][] expected = new String[][] {{"\"tom\"@en", "\"21\"^^<http://www.w3.org/2001/XMLSchema#integer>"}, {"\"jack\"@en", "\"32\"^^<http://www.w3.org/2001/XMLSchema#integer>"}, {"\"jane\"@en", "\"24\"^^<http://www.w3.org/2001/XMLSchema#integer>"}};
			assertArrayEquals(expected, result);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testQStatic3() {
		arrayTableEntry.setType(BDPLArrayType.STATIC_QUERY);
		arrayTableEntry.setSource("PREFIX : <http://events.event-processing.org/types/> PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> SELECT ?y ?x WHERE { ?z :name ?x. ?z :tall ?y }");
		
		try {
			arrayMaker.make(arrayTableEntry, null);
			
			String[][] result = arrayTableEntry.getArray().read();
			String[][] expected = new String[][] {{"\"1.74\"^^<http://double>", "\"tom\"@en"}, {"\"1.89\"^^<http://double>", "\"jack\"@en"}, {"\"1.67\"^^<http://double>", "\"jane\"@en"}};
			assertArrayEquals(expected, result);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected = InitiateException.class)
	public void testVDynamic1() throws InitiateException {
		arrayTableEntry.setType(BDPLArrayType.DYNAMIC_VAR);
		SubQueryTable table = new SubQueryTable();
		
		arrayMaker.make(arrayTableEntry, table);
	}
	
	@Test
	public void testVDynamic2() {
		arrayTableEntry.setType(BDPLArrayType.DYNAMIC_VAR);
		SubQueryTable table = new SubQueryTable();
		
		arrayTableEntry.setArray(new BDPLArray(2, null));
		arrayTableEntry.setSource(" x ");
		
		try {
			arrayMaker.make(arrayTableEntry, table);
			String[] result = table.getEntryToSelf().get(0).getSelectedVars();
			String[] expected = new String[] {"x"};
			assertArrayEquals(expected, result);
			
		} catch (InitiateException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testVDynamic3() {
		arrayTableEntry.setType(BDPLArrayType.DYNAMIC_VAR);
		SubQueryTable table = new SubQueryTable();
		
		arrayTableEntry.setArray(new BDPLArray(2, null));
		arrayTableEntry.setSource("x  y");
		
		try {
			arrayMaker.make(arrayTableEntry, table);
			String[] result = table.getEntryToSelf().get(0).getSelectedVars();
			String[] expected = new String[] {"x", "y"};
			assertArrayEquals(expected, result);
			
		} catch (InitiateException e) {
			e.printStackTrace();
		}

	}
}
