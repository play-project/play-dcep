package eu.play_project.dcep.distributedetalis.api;

import com.jtalis.core.JtalisContextImpl;

import fr.inria.eventcloud.api.CompoundEvent;

/**
 * To use the prolog Semantic Web Library with PLAY events.
 * 
 * @author obermei
 */
public interface UsePrologSemWebLib {
	
	/**
	 * Load Semantic Web library.
	 */
	public void init(JtalisContextImpl ctx);
	
	/**
	 * Put data in RDF Triple Store.
	 * @param event Event with date for the Triple Store.
	 * @return 
	 * @throws Exception 
	 */
	public Boolean addEvent(CompoundEvent event) throws Exception;
	
	/**
	 * Remove triples form RDF Triple Store. 
	 * @param id Triple Store id (is equals to graph name).
	 */
	public void removeEvent(String id);

	/**
	 * Takes all data from SWI-Prolog Semantic Web Library db and returns the as CompundEvent.
	 * @param complexEventID ID of database which contains needet data.
	 * @return All data from given db.
	 */
	public CompoundEvent getRdfData(String complexEventID);

}
