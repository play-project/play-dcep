package eu.play_project.dcep.distributedetalis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.play_project.dcep.node.api.DcepNodeApi;
import eu.play_project.dcep.node.connections.AbstractConnectionManagerWsn;
import fr.inria.eventcloud.api.CompoundEvent;

/**
 * An abstract connection manager to get real-time events from the PLAY
 * Platform. Access to historic data, however, must be implemented by extending
 * classes.
 * 
 * @author Roland Stühmer
 */
public abstract class EcConnectionManagerWsn extends AbstractConnectionManagerWsn<CompoundEvent> {
	
	private final Logger logger = LoggerFactory.getLogger(EcConnectionManagerWsn.class);

	public EcConnectionManagerWsn(DcepNodeApi<CompoundEvent> dEtalis) {
		super(dEtalis);
	}
}
