package eu.play_project.platformservices.querydispatcher.query.initiate.test;

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

import eu.play_project.platformservices.bdpl.parser.array.BDPLArrayType;
import eu.play_project.platformservices.bdpl.parser.util.ArrayTableEntry;
import eu.play_project.platformservices.querydispatcher.query.initiate.array.DefaultArrayMaker;
import eu.play_project.platformservices.querydispatcher.query.initiate.array.IArrayMaker;
import eu.play_project.platformservices.querydispatcher.query.initiate.util.InitiateException;

public class DefaultArrayMakerTest {
	
	private static IArrayMaker arrayMaker;
	private static Repository rep;
	private static RepositoryConnection conn;
	private ArrayTableEntry arrayTableEntry;
	
	@BeforeClass
	public static void setUp() throws RepositoryException, RDFParseException, IOException{
		arrayMaker = new DefaultArrayMaker();
		
		/*Repository rep = new SailRepository(new MemoryStore());
		rep.initialize();
		 
		
		conn = rep.getConnection();
		
		conn.add(DefaultArrayMakerTest.class.getResourceAsStream("/rdf/test_data.trig"), "", RDFFormat.TRIG);*/
		
	}
	
	@AfterClass
	public static void setDown() throws RepositoryException{
		if(conn != null)
			conn.close();
		if(rep != null)
			rep.shutDown();
	}
	
	@Before
	public void init(){
		arrayTableEntry = new ArrayTableEntry();
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
		arrayTableEntry.setSource("PREFIX : <http://events.event-processing.org/types/> SELECT ?x WHERE { ?id :name ?x }");
		
		try {
			arrayMaker.make(arrayTableEntry, null);
			
			String[][] result = arrayTableEntry.getArray().read();
			String[][] expected = new String[][] {{"tom"}, {"jack"}, {"jane"}};
			assertArrayEquals(expected, result);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testQStatic2() {
		arrayTableEntry.setType(BDPLArrayType.STATIC_QUERY);
		arrayTableEntry.setSource("PREFIX : <http://events.event-processing.org/types/> SELECT ?x ?y WHERE { ?z :name ?x. ?z :age ?y }");
		
		try {
			arrayMaker.make(arrayTableEntry, null);
			
			String[][] result = arrayTableEntry.getArray().read();
			String[][] expected = new String[][] {{"tom", "21"}, {"jack", "32"}, {"jane", "24"}};
			assertArrayEquals(expected, result);
		} catch (InitiateException e) {
			e.printStackTrace();
		}
	}
}
