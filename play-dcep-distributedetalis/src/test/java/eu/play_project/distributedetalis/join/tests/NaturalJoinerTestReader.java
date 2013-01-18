/**
 * 
 */
package eu.play_project.distributedetalis.join.tests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ningyuan Pan
 *
 */
public class NaturalJoinerTestReader {
	private BufferedReader in;
	List<List> r1, r2, r;
	List<String> v1, v2, v;
	
	NaturalJoinerTestReader(String f) throws UnsupportedEncodingException, FileNotFoundException{
		in = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(f)));
	}
	
	void read() throws IOException{
		
		try{
			StringBuilder item = new StringBuilder();
			
			v1 = new ArrayList<String>();
			int c = in.read();
			while(c != '\n'){
				if(c == ' '){
					v1.add(item.toString());
					item.delete(0, item.length());
				}
				else if(c == '\r'){}
				else{
					item.append((char)c);
				}
				c = in.read();
			}
			v1.add(item.toString());
			item.delete(0, item.length());
			
			r1 = new ArrayList<List>();
			List<String> data = new ArrayList<String>();
			c = in.read();
			while(c != ';'){
				if(c == ' '){
					data.add(item.toString());
					item.delete(0, item.length());
				}
				else if(c == '\n'){
					data.add(item.toString());
					item.delete(0, item.length());
					r1.add(data);
					data = new ArrayList<String>();
				}
				else if(c == '\r'){}
				else{
					item.append((char)c);
				}
				c = in.read();
			}
			item.delete(0, item.length());
			
			c = in.read();
			while(c != '\n'){
				c = in.read();
			}
			//*******************************************************
			
			v2 = new ArrayList<String>();
			c = in.read();
			while(c != '\n'){
				if(c == ' '){
					v2.add(item.toString());
					item.delete(0, item.length());
				}
				else if(c == '\r'){}
				else{
					item.append((char)c);
				}
				c = in.read();
			}
			v2.add(item.toString());
			item.delete(0, item.length());
			
			r2 = new ArrayList<List>();
			data = new ArrayList<String>();
			c = in.read();
			while(c != ';'){
				if(c == ' '){
					data.add(item.toString());
					item.delete(0, item.length());
				}
				else if(c == '\n'){
					data.add(item.toString());
					item.delete(0, item.length());
					r2.add(data);
					data = new ArrayList<String>();
				}
				else if(c == '\r'){}
				else{
					item.append((char)c);
				}
				c = in.read();
			}
			item.delete(0, item.length());
			
			c = in.read();
			while(c != '\n'){
				c = in.read();
			}
			//****************************************************
			
			v = new ArrayList<String>();
			c = in.read();
			while(c != '\n'){
				if(c == ' '){
					v.add(item.toString());
					item.delete(0, item.length());
				}
				else if(c == '\r'){}
				else{
					item.append((char)c);
				}
				c = in.read();
			}
			v.add(item.toString());
			item.delete(0, item.length());
			
			r = new ArrayList<List>();
			data = new ArrayList<String>();
			c = in.read();
			while(c != -1){
				if(c == ' '){
					data.add(item.toString());
					item.delete(0, item.length());
				}
				else if(c == '\n'){
					data.add(item.toString());
					item.delete(0, item.length());
					r.add(data);
					data = new ArrayList<String>();
				}
				else if(c == '\r'){}
				else{
					item.append((char)c);
				}
				c = in.read();
			}
			item.delete(0, item.length());
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		finally{
			in.close();
		}
		
		//for test
		System.out.println("\nRead Result: ");
		for(int i = 0; i < v1.size(); i++){
			System.out.print(v1.get(i)+" ");
		}
		System.out.println();
		for(int i = 0; i < r1.size(); i++){
			List<String> ll = r1.get(i);
			for(int j = 0; j < ll.size(); j++){
				System.out.print(ll.get(j)+" ");
			}
			System.out.println();
		}
		
		System.out.println();
		for(int i = 0; i < v2.size(); i++){
			System.out.print(v2.get(i)+" ");
		}
		System.out.println();
		for(int i = 0; i < r2.size(); i++){
			List<String> ll = r2.get(i);
			for(int j = 0; j < ll.size(); j++){
				System.out.print(ll.get(j)+" ");
			}
			System.out.println();
		}
		
		System.out.println();
		for(int i = 0; i < v.size(); i++){
			System.out.print(v.get(i)+" ");
		}
		System.out.println();
		for(int i = 0; i < r.size(); i++){
			List<String> ll = r.get(i);
			for(int j = 0; j < ll.size(); j++){
				System.out.print(ll.get(j)+" ");
			}
			System.out.println();
		}
	}
}
