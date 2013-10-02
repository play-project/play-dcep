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
	 * Load SWI libraries.
	 */
	public void init(JtalisContextImpl ctx) throws DistributedEtalisException;
	
	/**
	 * Put data in RDF Triple Store.
	 * @param event Event with date for the Triple Store.
	 */
	public Boolean addEvent(CompoundEvent event) throws Exception;

	/**
	 * Takes all data from SWI-Prolog Semantic Web Library db and returns the as CompundEvent.
	 * @param complexEventID ID of database which contains needet data.
	 * @return All data from given db.
	 */
	public CompoundEvent getRdfData(String complexEventID);

}
