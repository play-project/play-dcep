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
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import com.google.gson.Gson;

import eu.play_project.platformservices.bdpl.parser.util.BDPLConstants;
import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameEventModel;

/**
 * @author ningyuan 
 * 
 * Dec 14, 2014
 *
 */
public class ECGEventCreator1 extends EventCreator{
	
	private File records;
	private BufferedReader in;
	private long count = 0;
	private DatatypeFactory dtf;
	private StringBuffer sb = new StringBuffer();
	
	private QRSDetector ecgFilter = new QRSDetector();
	
	public ECGEventCreator1(String fn){
		super("ECGEvent");
		
		records = new File("D:/Neo/Downloads/simdata/out/"+fn);

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
	}
	
	@Override
	public String getEventType(){
		return eventType;
	}
	
	@Override
	public MapEvent next(){
		String record = null;
		int num = 32;
		double [] ecg = new double[num];
		
		for(int i = 0; i < num; i++){
			try {
				record = in.readLine();
			} catch (IOException e) {
				close();
				return null;
			} 
			if(record == null){
				return null;
			}
			else{
				ecg[i] = parseRecord(record);
			}
		}
		
		Gson gson = new Gson();
		String json;
		
		json = gson.toJson(ecg);
		
		Model ret = new LinkedHashModel();
		
		ret.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/id"), new LiteralImpl(String.valueOf(count)));
		ret.add(new URIImpl(":"+count), RDF.TYPE, new LiteralImpl(eventType));
		ret.add(new URIImpl(":"+count), new URIImpl("http://events.event-processing.org/types/stream"), new LiteralImpl(eventType));
		ret.add(new URIImpl(":"+count), new URIImpl("http://events.event-processing.org/types/endTime"), new LiteralImpl(dtf.newDuration(System.currentTimeMillis()).toString()));
		ret.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/ecg"), new LiteralImpl(json, new URIImpl(BDPLConstants.URI_TYPE_JSON_ARRAY)));
		
		count++;
		return new MapEvent(new SesameEventModel(ret));
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
	
	private double parseRecord(String r){
		
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
					return 0;
				}
			}
			else{
				sb.append(c);
			}
		}
		ecg = sb.toString();
		sb.delete(0, sb.length());
		
		return Double.valueOf(ecg);
	}
}
