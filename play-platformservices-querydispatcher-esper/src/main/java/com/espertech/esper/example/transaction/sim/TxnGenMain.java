/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.example.transaction.sim;

import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.time.TimerControlEvent;
import com.espertech.esper.example.transaction.*;

import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;



/** Runs the generator.
 *
 * @author Hans Gilde
 *
 */
public class TxnGenMain implements Runnable {

    private static Map<String,Integer> BUCKET_SIZES = new LinkedHashMap<String,Integer>();

    static {
        BUCKET_SIZES.put("tiniest", 20);
        BUCKET_SIZES.put("tiny", 499);
        BUCKET_SIZES.put("small", 4999);
        BUCKET_SIZES.put("medium", 14983);
        BUCKET_SIZES.put("large", 49999);
        BUCKET_SIZES.put("larger", 1999993);
        BUCKET_SIZES.put("largerer", 9999991);
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Arguments are: <bucket_size> <num_transactions>");
            System.exit(-1);
        }

        int bucketSize;
        try {
            bucketSize = BUCKET_SIZES.get(args[0]);
        } catch (NullPointerException e) {
            System.out.println("Invalid bucket size:");
            for(String key:BUCKET_SIZES.keySet()) {
                System.out.println("\t"+key+" -> "+BUCKET_SIZES.get(key));
            }

            System.exit(-2);
            return;
        }

        int numTransactions;
        try {
            numTransactions = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid num transactions");
            System.exit(-2);
            return;
        }

        // Run the sample
        System.out.println("Using bucket size of " + bucketSize + " with " + numTransactions + " transactions");
        TxnGenMain txnGenMain = new TxnGenMain(bucketSize, numTransactions, "TransactionExample", false);
        txnGenMain.run();
    }

    private int bucketSize;
    private int numTransactions;
    private boolean continuousSimulation;
    private EPServiceProvider epService = null;
    private ShuffledBucketOutput output;
    
    public TxnGenMain(int bucketSize, int numTransactions, String engineURI, boolean continuousSimulation)
    {
        this.bucketSize = bucketSize;
        this.numTransactions = numTransactions;
        this.continuousSimulation = continuousSimulation;
    }
    
    public TxnGenMain(int bucketSize, int numTransactions, boolean continuousSimulation, EPServiceProvider epS)
    {
        this.bucketSize = bucketSize;
        this.numTransactions = numTransactions;
        this.continuousSimulation = continuousSimulation;
        this.epService = epS;
    }
    
    public void run()
    {   
        
        // We will be supplying timer events externally.
        // We will assume that each bucket arrives within a defined period of time.
        //epService.getEPRuntime().sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
       
        // The feeder to feed the engine
        FeederOutputStream feeder = new FeederOutputStream(epService);

        // Generate transactions
        TransactionEventSource source = new TransactionEventSource(numTransactions);
        output = new ShuffledBucketOutput(source, feeder, bucketSize);

        // Feed events
        try {
            if (continuousSimulation) {
                while(true) {
                    output.output();
                    Thread.sleep(5000); // Send a batch every 5 seconds
                }
            }
            else {
                output.output();
            }
        }
        catch(InterruptedException ex) {
            // no action
        	System.out.println("[Event Feeding is stoped]");
        }
        catch(IOException ex) {
            throw new RuntimeException("Error outputting events: " + ex.getMessage(), ex);
        }
    }
}
