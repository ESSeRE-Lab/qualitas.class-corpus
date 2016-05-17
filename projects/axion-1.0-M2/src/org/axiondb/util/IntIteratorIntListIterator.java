/*
 * $Id: IntIteratorIntListIterator.java,v 1.1 2003/05/15 22:51:39 rwald Exp $
 * =======================================================================
 * Copyright (c) 2003 Axion Development Team.  All rights reserved.
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

package org.axiondb.util;

import java.util.NoSuchElementException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.IntListIterator;

/**
 * @version $Revision: 1.1 $ $Date: 2003/05/15 22:51:39 $
 * @author Rodney Waldhoff
 */
public class IntIteratorIntListIterator implements IntListIterator {
    public IntIteratorIntListIterator(IntIterator iterator) {
        _iterator = iterator;
    }
    
    public void add(int value) {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return _nextIndex < _history.size() || _iterator.hasNext(); 
    }

    public boolean hasPrevious() {
        return _nextIndex > 0; 
    }

    public int next() {
        if(_nextIndex < _history.size()) {
            return _history.get(_nextIndex++);
        } else {
            int val = _iterator.next();
            _history.add(val);
            _nextIndex++;
            return val;
        }
    }

    public int nextIndex() {
        return _nextIndex;
    }

    public int previous() {
        if(hasPrevious()) {
            return _history.get(--_nextIndex);
        } else {
            throw new NoSuchElementException();
        }
    }

    public int previousIndex() {
        return (_nextIndex - 1);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void set(int value) {
        throw new UnsupportedOperationException();
    }

    private IntIterator _iterator = null;
    private IntList _history = new ArrayIntList();
    private int _nextIndex = 0;

}
