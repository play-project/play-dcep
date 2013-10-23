package eu.play_project.dcep.distributedetalis.join.tests;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.play_project.dcep.distributedetalis.join.SelectVariable;



/**
 * @author Ningyuan Pan
 *
 */
public class SelectVariableTest {
	private SelectVariable<String> sv;
	
	@Before
	public void init(){
		sv = new SelectVariable<String>();
	}
	
	@Test
	public void testAddRelResult1() {
		List<String> values;
		List<String> add = null;
		sv.addValues(add);
		values = sv.getValues();
		assertNull("Union size error when empty value set union null value set", values);
	}
	
	@Test
	public void testAddResult2(){
		List<String> values;
		List<String> add = new ArrayList<String>();
		add.add("tom");
		add.add("jack");
		sv.addValues(add);
		values = sv.getValues();
		boolean pass = false;
		for(int i = 0; i < add.size(); i++){
			String s = add.get(i);
			pass = false;
			for(int j = 0; j < values.size(); j++){
				if(s.equals(values.get(j))){
					pass = true;
					break;
				}
			}
		}
		assertTrue("Union size error when empty value set union none empty value set", values.size()==2);
		assertTrue("Union element lost when empty value set union none empty value set", pass);
	}
	
	@Test
	public void testAddResult3(){
		String [] v = {"tom", "jack"};
		List<String> values;
		List<String> add = new ArrayList<String>();
		add.add("tom");
		add.add("jack");
		sv.addValues(add);
		add.add("john");
		sv.addValues(add);
		values = sv.getValues();
		boolean pass = false;
		for(int i = 0; i < v.length; i++){
			String s = v[i];
			pass = false;
			for(int j = 0; j < values.size(); j++){
				if(s.equals(values.get(j))){
					pass = true;
					break;
				}
			}
		}
		assertTrue("Union size error when one none empty value set union another none empty value set", values.size()==2);
		assertTrue("Union element lost when one none empty value set union another none empty value set", pass);
	
		v = new String []{"tom"};
		add.clear();
		add.add("tom");
		sv.addValues(add);
		values = sv.getValues();
		pass = false;
		for(int i = 0; i < v.length; i++){
			String s = v[i];
			pass = false;
			for(int j = 0; j < values.size(); j++){
				if(s.equals(values.get(j))){
					pass = true;
					break;
				}
			}
		}
		assertTrue("Union size error when one none empty value set union another none empty value set", values.size()==1);
		assertTrue("Union element lost when one none empty value set union another none empty value set", pass);
	}
	
	@Test
	public void testAddResult4(){
		List<String> values;
		List<String> add = new ArrayList<String>();
		add.add("tom");
		add.add("jack");
		sv.addValues(add);
		add.clear();
		add.add("Tom");
		add.add("Jack");
		sv.addValues(add);
		values = sv.getValues();
	
		assertTrue("Union size error when two disjoint value sets are unioned", values.size()==0);
	}
	
	@Test
	public void testAddResult5(){
		String [] v = {"tom", "jack"};
		List<String> values;
		List<String> add = new ArrayList<String>();
		add.add("tom");
		add.add("jack");
		sv.addValues(add);
		sv.addValues(null);
		values = sv.getValues();
		boolean pass = false;
		for(int i = 0; i < v.length; i++){
			String s = v[i];
			pass = false;
			for(int j = 0; j < values.size(); j++){
				if(s.equals(values.get(j))){
					pass = true;
					break;
				}
			}
		}
		assertTrue("Union size error when one none empty value set union null value set", values.size()==2);
		assertTrue("Union element lost when one none empty value set union null value set", pass);
	}

}
