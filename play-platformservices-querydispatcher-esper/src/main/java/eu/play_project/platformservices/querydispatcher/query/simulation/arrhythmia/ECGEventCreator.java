/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation.arrhythmia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
 * @author ningyuan 
 * 
 * Oct 1, 2014
 *
 */
public class ECGEventCreator extends EventCreator{
	

	private File records;
	private BufferedReader in;
	private long count = 0;
	private DatatypeFactory dtf;
	private StringBuffer sb = new StringBuffer();
	
	private QRSDetector ecgFilter = new QRSDetector();
	
	public ECGEventCreator(){
		super("ECGEvent");
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
		String timestampe="", ecg;
		
		int num = 0;
		for(int i = 0; i < r.length(); i++){
			char c = r.charAt(i);
			if(c == ':'){
				num++;
				if(num < 4){
					sb.append(c);
				}
				else if(num == 4){
					timestampe = sb.toString();
					sb.delete(0, sb.length());
					
				}
				else{
					System.out.println("ArrEvent parse exception");
					return null;
				}
			}
			else{
				sb.append(c);
			}
		}
		ecg = sb.toString();
		//int ecgInt = ecgFilter.QRSFilter(Integer.valueOf(ecg), false);
		int ecgInt = ecgFilter.QRSDet(Integer.valueOf(ecg), false);
		sb.delete(0, sb.length());
		
		ret = new LinkedHashModel();
		ret.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/id"), new LiteralImpl(String.valueOf(count)));
		ret.add(new URIImpl(":"+count), RDF.TYPE, new LiteralImpl(eventType));
		ret.add(new URIImpl(":"+count), new URIImpl("http://events.event-processing.org/types/stream"), new LiteralImpl(eventType));
		ret.add(new URIImpl(":"+count), new URIImpl("http://events.event-processing.org/types/endTime"), new LiteralImpl(dtf.newDuration(System.currentTimeMillis()).toString()));
		ret.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/timestampe"), new LiteralImpl(timestampe));
		ret.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/ecg"), new NumericLiteralImpl(ecgInt));
		
			//System.out.println(eventType+count+": "+timestampe+" "+ecg);
		count++;
		return ret;
	}

	@Override
	public void initiate(Object... paras) {
		if(paras != null && paras.length > 0){
			records = new File("D:/Neo/Downloads/simdata/out/"+(String)paras[0]);
	
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
