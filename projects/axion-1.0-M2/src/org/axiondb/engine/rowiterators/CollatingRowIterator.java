/*
 * $Id: CollatingRowIterator.java,v 1.9 2003/05/20 17:59:10 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002 Axion Development Team.  All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above 
 *    copyright notice, this list of conditions and the following 
 *    disclaimer. 
 *   
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution. 
 *   
 * 3. The names "Tigris", "Axion", nor the names of its contributors may 
 *    not be used to endorse or promote products derived from this 
 *    software without specific prior written permission. 
 *  
 * 4. Products derived from this software may not be called "Axion", nor 
 *    may "Tigris" or "Axion" appear in their names without specific prior
 *    written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =======================================================================
 */

package org.axiondb.engine.rowiterators;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.ArrayUnsignedShortList;
import org.apache.commons.collections.primitives.IntList;
import org.axiondb.AxionException;
import org.axiondb.Row;
import org.axiondb.RowComparator;
import org.axiondb.RowIterator;

/**
 * Collates the results of two or more sorted {@link RowIterator}s 
 * according to the given {@link RowComparator}.
 * It is assumed that each iterator is already ordered (ascending) according
 * to the given {@link RowComparator}.
 * @version $Revision: 1.9 $ $Date: 2003/05/20 17:59:10 $
 * @author Rodney Waldhoff
 */
public class CollatingRowIterator extends BaseRowIterator {
// XXX TO DO XXX
// This isn't pretty but it seems to work.
// There is probably substantial room for optimization here.
// In particular, it's probably not strictly necessary to maintain
// the stack of returned values, but I don't see a better way right now.
// XXX TO DO XXX

    public CollatingRowIterator(RowComparator comparator) {
        _comparator = comparator;
        _iterators = new ArrayList();
    }
    
    public void addRowIterator(RowIterator iter) throws IllegalStateException {
        assertNotStarted();
        _iterators.add(iter);
    }
    
    public Row previous() throws AxionException {
        startIfNotStarted();
        if(_prevFrom.isEmpty()) {
            throw new NoSuchElementException("No previous row");
        } else {
            int prevIndex = _prevFrom.removeElementAt(_prevFrom.size()-1);
            RowIterator prevIter = (RowIterator)(_iterators.get(prevIndex));
            clearPeeked(prevIndex);
            _hasCurrent = true;
            _currentIndex = _nextIndex-1;
            _nextIndex--;
            _currentRow = (Row)(prevIter.previous());
            _lastReturnedFrom = prevIndex;
            return _currentRow;
        }
    }

    public int previousIndex() {
        startIfNotStarted();
        return _nextIndex-1;
    }

    public boolean hasPrevious() {
        startIfNotStarted();
        return (!_prevFrom.isEmpty());
    }

    public Row next() throws AxionException {
        startIfNotStarted();
        int minIndex = -1;
        Row minValue = null;
        for(int i=0,m=_iterators.size();i<m;i++) {
            if(!_nextSet.get(i)) {
                RowIterator iter = (RowIterator)(_iterators.get(i));
                if(iter.hasNext()) {
                    _nextSet.set(i);
                    // peek ahead to the next value
                    _nexts.set(i,iter.peekNext());
                } else {
                    continue;
                }
            }
            if(-1 == minIndex) {
                minIndex = i;
                minValue = (Row)(_nexts.get(i));
            } else if(_comparator.compare(minValue,_nexts.get(i)) > 0) {
                minIndex = i;
                minValue = (Row)(_nexts.get(i));
            }
        }
        if(-1 == minIndex) {
            throw new NoSuchElementException();
        } else {
            clearPeeked(minIndex);
            // step forward past the previously peeked value
            RowIterator iter = (RowIterator)(_iterators.get(minIndex));
            iter.next();
            _hasCurrent = true;
            _currentIndex = _nextIndex;
            _nextIndex++;
            _currentRow = minValue;
            _lastReturnedFrom = minIndex;
            _prevFrom.add(minIndex);
            return _currentRow;
        }
    }

    public int nextIndex() {
        startIfNotStarted();
        return _nextIndex;
    }

    public boolean hasNext() {
        startIfNotStarted();
        for(int i=0,m=_iterators.size();i<m;i++) {
            if(_nextSet.get(i)) {
                return true;
            }
        }
        for(Iterator iter = _iterators.iterator(); iter.hasNext();) {
            RowIterator rowiter = (RowIterator)(iter.next());
            if(rowiter.hasNext()) {
                return true;
            }
        }
        return false;
    }

    public Row current() {
        startIfNotStarted();
        if(!hasCurrent()) {
            throw new NoSuchElementException("No current row");
        } else {
            return _currentRow;
        }
    }

    public int currentIndex() {
        startIfNotStarted();
        return _currentIndex;
    }

    public boolean hasCurrent() {
        startIfNotStarted();
        return _hasCurrent;
    }

    public void reset() throws AxionException {
        startIfNotStarted();
        for(int i=0,m = _iterators.size();i<m;i++) {
            clearPeeked(i);
            RowIterator iter = (RowIterator)(_iterators.get(i));
            iter.reset();
        }
        _currentRow = null;
        _currentIndex = -1;
        _hasCurrent = false;
        _nextIndex = 0;
        _lastReturnedFrom = -1;
        _prevFrom.clear();
        if(_prevFrom instanceof ArrayIntList) {
            ((ArrayIntList)_prevFrom).trimToSize();
        }
    }

    public void set(Row row) throws AxionException {
        // XXX TO DO XXX - test me
        startIfNotStarted();
        RowIterator iter = getLastReturnedFrom();
        iter.set(row);
        _currentRow = null;
        _currentIndex = -1;
        _hasCurrent = false;
    }

    public void remove() throws AxionException {
        // XXX TO DO XXX - test me
        startIfNotStarted();
        RowIterator iter = getLastReturnedFrom();
        iter.remove();
        _currentRow = null;
        _currentIndex = -1;
        _hasCurrent = false;
    }

    private void start() {
        _nexts = new ArrayList(_iterators.size());
        for(int i=0,m=_iterators.size();i<m;i++) {
            _nexts.add(null);
        }
        _nextSet = new BitSet(_iterators.size());
        _prevFrom = new ArrayUnsignedShortList();
    }

    private void startIfNotStarted() {
        if(!started()) {
            start();
        }
    }
    
    private boolean started() {
        return null != _nexts;
    }

    private RowIterator getLastReturnedFrom() throws IllegalStateException {
        assertStarted();
        RowIterator iter = (RowIterator)(_iterators.get(_lastReturnedFrom));
        return iter;
    }
    
    private void assertNotStarted() throws IllegalStateException {
        if(started()) {
            throw new IllegalStateException("Already started");
        }
    }

    private void assertStarted() throws IllegalStateException {
        if(!started()) {
            throw new IllegalStateException("Not started");
        }
    }

    private void clearPeeked(int i) {
        _nextSet.clear(i);
        _nexts.set(i,null);
    }

    /** My {@link RowComparator} to use for collating. */
    private RowComparator _comparator = null;
    /** The list of {@link RowIterator}s to collate over. */
    private ArrayList _iterators = null;
    /** {@link Row} values peeked from my {@link #_iterators}. */
    private ArrayList _nexts = null;
    /** Whether or not I've peeked ahead. */
    private BitSet _nextSet = null;
    /** The last {@link Row} returned by {@link #next} or {@link #previous}. */
    private Row _currentRow = null;
    /** The index of {@link #_currentRow} within my iteration. */
    private int _currentIndex = -1;
    /** Whether or not {@link #_currentRow} has been set. */
    private boolean _hasCurrent = false;
    /** The {@link #_iterators iterator} I last returned from. */
    private int _lastReturnedFrom = -1;
    /** The next index within my iteration. */
    private int _nextIndex = 0;
    /** A stack of the {@link #_iterators} from which {@link #next} obtained values from. */
    private IntList _prevFrom = null;
}

