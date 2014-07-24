/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.example.transaction.sim;


import eu.play_project.platformservices.querydispatcher.query.event.MapEvent;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameEventModel;
import eu.play_project.platformservices.querydispatcher.query.event.implement.rdf.sesame.SesameMapEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

/** Generates events for a continuous stream of transactions.
 * Rules for generating events are coded in {@link #createNextTransaction()}.
 *
 * @author Hans Gilde
 *
 */
public class TransactionEventSource extends EventSource {
    protected String currentTransactionID;
    protected Random random = RandomUtil.getNewInstance();
    protected List<Object> transactionEvents;
    protected Iterator<Object> transactionIterator;

    protected int maxTrans;
    protected int numTrans;

    protected FieldGenerator fieldGenerator = new FieldGenerator();
    
    private boolean flag = true;
    private int type = -1;
    private long id = 0l;
    private long id2 = 0l;
    private long count = 0l;
    
    /**
     * @param howManyTransactions How many transactions should events be generated for?
     */
    public TransactionEventSource(int howManyTransactions) {
        maxTrans = howManyTransactions;
    }

    protected List<Object> createNextTransaction() {
    	MapEvent mapEvent;
    	
        List<Object> t = new ArrayList<Object>();

        long beginningStamp = System.currentTimeMillis();
        //skip event 1 with probability 1 in 5000
        if (random.nextInt(5000) < 4998) {
           /* TxnEventA txnEventA = new TxnEventA(null, beginningStamp, fieldGenerator.getRandomCustomer());
            t.add(txnEventA);*/
        	
        	type = type * -1;
        	id++;
        	Model m = new LinkedHashModel();
        	m.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/id"), new LiteralImpl(String.valueOf(id)));
        	m.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/type"), new LiteralImpl(String.valueOf(type)));
        	m.add(new URIImpl(":"+count), RDF.TYPE, new LiteralImpl("Event1"));
        	/*Event1 rdf1 = new Event1(m);*/
        	mapEvent = new SesameMapEvent(new SesameEventModel(m));
        	
        	MapEventWrapper rdf1 = new MapEventWrapper("rdf1", mapEvent);
        	
        	t.add(rdf1);
        }

        long e2Stamp = fieldGenerator.randomLatency(beginningStamp);
        //skip event 2 with probability 1 in 1000
        if (random.nextInt(1000) < 998) {
           /* TxnEventB txnEventB = new TxnEventB(null, e2Stamp);
            t.add(txnEventB);*/
        	
        	id2++;
        	count++;
        	Model m = new LinkedHashModel();
        	m.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/id"), new LiteralImpl(String.valueOf(id2)));
        	m.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/stamp"), new LiteralImpl(String.valueOf(e2Stamp)));
        	m.add(new URIImpl(":"+count), RDF.TYPE, new LiteralImpl("Event2"));
        	
        	/*Event2 rdf2 = new Event2(m);*/
        	mapEvent = new SesameMapEvent(new SesameEventModel(m));
        	
        	MapEventWrapper rdf2 = new MapEventWrapper("rdf2", mapEvent);
        	t.add(rdf2);
        }

        long e3Stamp = fieldGenerator.randomLatency(e2Stamp);
        //skip event 3 with probability 1 in 10000
        if (random.nextInt(10000) < 9998) {
            /*TxnEventC txnEventC = new TxnEventC(null, e3Stamp, fieldGenerator.getRandomSupplier());
            t.add(txnEventC);*/
            
        	count++;
            Model m = new LinkedHashModel();
        	m.add(new URIImpl(":"+count), new URIImpl("http://ningyuan.com/stamp"), new LiteralImpl(String.valueOf(e3Stamp)));
        	m.add(new URIImpl(":"+count), RDF.TYPE, new LiteralImpl("Event3"));
        	/*Event3 rdf3 = new Event3(m);*/
        	mapEvent = new SesameMapEvent(new SesameEventModel(m));
        	
        	MapEventWrapper rdf3 = new MapEventWrapper("rdf3", mapEvent);
        	t.add(rdf3);
        }
        else
        {
            log.debug(".createNextTransaction generated missing event");
        }

        return t;
    }

    /**
     * @return Returns the maxTrans.
     */
    public int getMaxTrans() {
        return maxTrans;
    }

    protected boolean hasNext()
    {
    	if (maxTrans > -1){
	        if (numTrans < maxTrans) {
	            return true;
	        } else {
	            return transactionIterator.hasNext();
	        }
    	}
    	else{
    		if(transactionIterator == null || flag == false){
    			flag = true;
    			return flag;
    		}
    		else{
    			flag = transactionIterator.hasNext();
    			return flag;
    		}
    	}
    }

    protected Object next()
    {
        if (transactionIterator!=null && transactionIterator.hasNext()) {
            Object m = transactionIterator.next();
            /*m.setTransactionId(currentTransactionID);*/
            return m;
        }

        if (numTrans == maxTrans) {
            throw new IllegalStateException("There is no next element.");
        }
        //create a new transaction, with ID.
        numTrans++;
        int id = random.nextInt();
        if (id < 0) {
            id = -1 * id;
        }
        currentTransactionID = new Integer(id).toString();
        transactionEvents = createNextTransaction();
        transactionIterator = transactionEvents.iterator();
        return this.next();
    }

    private static final Log log = LogFactory.getLog(TransactionEventSource.class);
}
