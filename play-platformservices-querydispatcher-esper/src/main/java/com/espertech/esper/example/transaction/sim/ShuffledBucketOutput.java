/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.example.transaction.sim;

import com.espertech.esper.example.transaction.TxnEventBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Outputs events using a bucket, whose contents are shuffled.
 *
 * @author Hans Gilde
 *
 */
public class ShuffledBucketOutput {
    private static final Random random = RandomUtil.getNewInstance();

    private EventSource eventSource;
    private OutputStream outputStream;
    private List<Object> bucket = new ArrayList<Object>();
    private int bucketSize;
    
   
    /**
     * @param eventSource
     * @param outputStream
     * @param bucketSize how many events should be in the bucket when it's shuffled?
     */
    public ShuffledBucketOutput(EventSource eventSource, OutputStream outputStream, int bucketSize) {
        this.eventSource = eventSource;
        this.outputStream = outputStream;
        this.bucketSize = bucketSize;
    }

    public void output() throws IOException {
        
        for (Object e:eventSource) {
            bucket.add(e);
            if (bucket.size() == bucketSize) {
                outputBucket(bucket);
            }
        }

        if (bucket.size() > 0) {
            outputBucket(bucket);
        }
    }
    
    /**
     * @param bucket
     * @throws IOException
     */
    private void outputBucket(List<Object> bucket) throws IOException {
        Collections.shuffle(bucket, random);
        outputStream.output(bucket);
        bucket.clear();
    }
}
