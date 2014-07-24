/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.compiler.translation.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;

import eu.play_project.platformservices.querydispatcher.query.compiler.translation.EPLTranslator;


/**
 * @author ningyuan 
 * 
 * Jun 16, 2014
 *
 */
public class EPLTranslatorTest {
	
	public final String NL = System.getProperty("line.separator");
	 
	public static URL testsLocation;
	 
    @BeforeClass
    public static void setUp() throws Exception { 
  	  testsLocation = EPLTranslatorTest.class.getResource("/bdpl/"); 
    } 
   
    @Test
    public void testQueries(){
  	  assertNotNull("[ERROR] Test queries could not be found.", testsLocation);
  	 
  	  File testsDir = new File(testsLocation.getPath());
  	  assertTrue("[ERROR] Invalid directory of test queries.", testsDir.isDirectory());
  	  	System.out.println(testsLocation.getPath());
  	  File [] queries = testsDir.listFiles();
  	  for(File query : queries){
  		 assertTrue("[ERROR] Invalid file name of test queries.", query.isFile());
  		 
  		 testQuery(query);
  	  }
    }
   
   private void testQuery(File query){
  	 System.out.println("Testing query file: "+query.getName());
  	 
  	 String [] content;
  	 try{
  		 content = getQuery(query);
  	 }
  	 catch (IOException ioe){
  		 System.out.println("[ERROR] IOException during reading query file "+query.getName());
  		 return;
  	 }
  	 
  	 if(query.getName().startsWith("BDPL-BrokenTranslation")){
  		
  		 try {
  			EPLTranslator translator = new EPLTranslator();
  			translator.parseQuery(content[0], null);
			System.out.println("[FAILED] No exception was thrown.");
  		 }
  		 catch (MalformedQueryException e) {
  			 if (content[1] != null && !e.getMessage().contains(content[1])) { // Test if expected exception
  				 
  				 System.out.println("[PASSED] Not expected exception was thrown.\t" + content[1] + " is expected");
  			 }
  			 else{
  				 System.out.println("[PASSED]");
  			 }
  		 } 
  	 }
  	 else{
  		 try {
  			EPLTranslator translator = new EPLTranslator();
  			translator.parseQuery(content[0], null);
			System.out.println("[PASSED]");
        } catch (MalformedQueryException e) {
       	  	fail("Malformed query file with error: " + e.getMessage());
		} 
  	 }
   } 
   
   private String[] getQuery(File query) throws IOException{
  	 InputStream is = new FileInputStream(query);
       
       InputStreamReader isr = new InputStreamReader(is);
       BufferedReader br = new BufferedReader(isr);
       StringBuffer sb = new StringBuffer();
       String line;
       String[] exeptionName = null;
       
       try{
	         while ((line = br.readLine()) != null) {
	
	                 if (line.startsWith("#")) { // Line with comment
	                         sb.append(line);
	                         sb.append("\n"); // new Line after commentline
	                         // Extract exceptionname
	                         exeptionName = line.split(".*@expectedException<*| *\\\\>*");
	                 } else {
	                         sb.append(line);
	                         sb.append("\n");
	                 }
	         }
       }
       finally{
	         br.close();
	         isr.close();
	         is.close();
       }
       
       
       if (exeptionName != null && exeptionName.length > 1) {
               return new String[] { sb.toString(), exeptionName[1] };
       } else {
               return new String[] { sb.toString(), null };
       }
  	 
   }
}
