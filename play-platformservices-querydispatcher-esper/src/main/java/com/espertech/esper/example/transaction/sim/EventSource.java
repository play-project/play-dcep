/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.example.transaction.sim;


import java.util.Iterator;

/** An Iterable source of events.
 *
 * @author Hans Gilde
 *
 */
public abstract class EventSource implements Iterable<Object> {

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Object> iterator() {
        return new InternalIterator();
    }

    protected abstract boolean hasNext();
    protected abstract Object next();

    private class InternalIterator implements Iterator<Object> {

        public boolean hasNext() {
            return EventSource.this.hasNext();
        }

        public Object next() {
            return EventSource.this.next();
        }

        public void remove() {
            throw new UnsupportedOperationException("This iterator does not suppoer removal.");
        }

    }

}
