/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation.arrhythmia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameEventModel;
import eu.play_project.platformservices.querydispatcher.query.simulation.EventCreator;

/**
 * Read test rr interval data from MIT BIH database.
 * http://www.physionet.org/cgi-bin/atm/ATM
 * 
 * 
 * @author ningyuan 
 * 
 * Nov 20, 2014
 *
 */
public class RREventCreator extends EventCreator{
	
	
	private File records;
	private BufferedReader in;
	private long count = 0;
	private DatatypeFactory dtf;
	private StringBuffer sb = new StringBuffer();
	
	
	public RREventCreator(){
		super("RREvent");
	}
	
	@Override
	public String getEventType(){
		return eventType;
	}
	
	@Override
	public MapEvent next(){
		if(ready){
			String record = null;
			try {
				record = in.readLine();
			} catch (IOException e) {
				close();
			} 
			if(record == null){
				return null;
			}
			else{
				Model m = parseRecord(record);
				if(m == null){
					return null;
				}
				else{
					return new MapEvent(new SesameEventModel(m));
				}
			}
		}
		else{
			return null;
		}
	}
	
	@Override
	public void close(){
		if(in != null){
			try {
				in.close();
			} catch (IOException e) {
				
			}
		}
	}
	
	private Model parseRecord(String r){
		Model ret = null;
		String timestampe="", rr="";
		
		int state = 0;
		for(int i = 0; i < r.length(); i++){
			char c = r.charAt(i);
			
			switch(state){
			    // start timestamp
				case 0 :{
					if(c == ' ' || c == '\t'){
						continue;
					}
					else{
						sb.append(c);
						state = 1;
					}
					break;
				}
				// timestamp
				case 1 :{
					if(c == ' ' || c == '\t'){
						timestampe = sb.toString();
						sb.delete(0, sb.length());
						
						state = 2;
					}
					else{
						sb.append(c);
					}
					break;
				}
				// start beat 1 type
				case 2 :{
					if(c == ' ' || c == '\t'){
						continue;
					}
					else{
						
						state = 3;
					}
					break;
				}
				// beat 1 type
				case 3 :{
					if(c == ' ' || c == '\t'){
						state = 4;
					}
					else{
						continue;
					}
					break;
				}
				// start rr
				case 4 :{
					if(c == ' ' || c == '\t'){
						continue;
					}
					else{
						sb.append(c);
						state = 5;
					}
					break;
				}
				// rr
				case 5 :{
					if(c == ' ' || c == '\t'){
						rr = sb.toString();
						sb.delete(0, sb.length());
						
						state = 6;
					}
					else{
						sb.append(c);
					}
					break;
				}
				// start beat 2 type
				case 6 :{
					//XXX the rest data is useless
					break;
				}
			}
		}	
			
		double rrD = Double.valueOf(rr);
		sb.delete(0, sb.length());
		
		ret = new LinkedHashModel();
		ret.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/id"), new LiteralImpl(String.valueOf(count)));
		ret.add(new URIImpl(":"+count), RDF.TYPE, new LiteralImpl(eventType));
		ret.add(new URIImpl(":"+count), new URIImpl("http://events.event-processing.org/types/stream"), new LiteralImpl(eventType));
		ret.add(new URIImpl(":"+count), new URIImpl("http://events.event-processing.org/types/endTime"), new LiteralImpl(dtf.newDuration(System.currentTimeMillis()).toString()));
		ret.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/timestampe"), new LiteralImpl(timestampe));
		ret.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/rr"), new NumericLiteralImpl(rrD));
		
			//System.out.println(eventType+count+": "+timestampe+" "+rr);
		count++;
		return ret;
	}

	@Override
	public void initiate(Object... paras) {
		if(paras != null && paras.length > 0){
			URL location = RREventCreator.class.getResource("/simdata/rr/"+(String)paras[0]+".txt"); 
			
			records = new File(location.getPath());
	
			try {
				in = new BufferedReader(new FileReader(records));
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("Source file of ArrEvent dose not exist");
			}
			
			try {
				dtf = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				throw new IllegalArgumentException("DatatypeFactory exception");
			}
			
			ready = true;
		}
	}
}
