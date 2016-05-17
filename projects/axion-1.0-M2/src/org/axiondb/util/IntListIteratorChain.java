/*
 * $Id: IntListIteratorChain.java,v 1.5 2003/05/15 17:25:40 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002-2003 Axion Development Team.  All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.IntListIterator;

/**
 * Concatenates multiple {@link IntListIterator}s into
 * a single {@link IntListIterator}.
 * <p/>
 * @version $Revision: 1.5 $ $Date: 2003/05/15 17:25:40 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class IntListIteratorChain implements IntListIterator {
    public IntListIteratorChain() {
        _listOfIterators = new ArrayList();
    }

    public void addIterator(IntListIterator iter) {
        assertNotStarted();
        addTempListIfNeeded();
        _listOfIterators.add(iter);
    }

    public void addIterator(int value) {
        assertNotStarted();
        addToTempList(value);
    }

    public boolean hasNext() {
        ensureStarted();
        if (_currentIterator != null && _currentIterator.hasNext()) {
            return true;
        } else if (_iteratorOverIterators.hasNext()) {
            _currentIterator = (IntListIterator)_iteratorOverIterators.next();
            return hasNext();
        } else {
            return false;
        }
    }

    public boolean hasPrevious() {
        ensureStarted();
        if (_currentIterator != null && _currentIterator.hasPrevious()) {
            return true;
        } else if (_iteratorOverIterators.hasPrevious()) {
            _currentIterator = (IntListIterator)_iteratorOverIterators.previous();
            return hasPrevious();
        } else {
            return false;
        }
    }

    public int next() {
        if (hasNext()) {
            _nextIndex++;
            return _currentIterator.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    public int previous() {
        if (hasPrevious()) {
            _nextIndex--;
            return _currentIterator.previous();
        } else {
            throw new NoSuchElementException();
        }
    }

    public void add(int elt) {
        throw new UnsupportedOperationException();
    }

    public int nextIndex() {
        return _nextIndex;
    }

    public int previousIndex() {
        return _nextIndex - 1;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void set(int elt) {
        throw new UnsupportedOperationException();
    }

    private void assertNotStarted() throws IllegalStateException {
        if (null != _iteratorOverIterators) {
            throw new IllegalStateException("Already started iterating");
        }
    }

    private void ensureStarted() {
        if (null == _iteratorOverIterators) {
            addTempListIfNeeded();
            _iteratorOverIterators = _listOfIterators.listIterator();
        }
    }

    private final void addTempListIfNeeded() {
        if(null != _tempList) {
            _listOfIterators.add(_tempList.listIterator());
            _tempList = null;
        } else if(_tempValueSet) {
            _listOfIterators.add(new SingleElementIntListIterator(_tempValue));
            _tempValueSet = false;
        }
    }
    
    private final void addToTempList(int value) {
        if(null == _tempList) {
            if(!_tempValueSet) {
                _tempValue = value;
                _tempValueSet = true;
            } else {
                _tempList = new ArrayIntList(2);
                _tempList.add(_tempValue);
                _tempList.add(value);
                _tempValueSet = false;
            }
        } else {
            _tempList.add(value);
        }
    }

    private List _listOfIterators = null;
    private IntList _tempList = null;
    private boolean _tempValueSet = false;
    private int _tempValue;
    private IntListIterator _currentIterator = null;
    private ListIterator _iteratorOverIterators = null;
    private int _nextIndex = 0;

    private final class SingleElementIntListIterator implements IntListIterator {
        
        public SingleElementIntListIterator(int value) {
            _value = value;
        }
        
        public boolean hasNext() {
            return _before;
        }

        public boolean hasPrevious() {
            return !_before;
        }

        public int next() {
            if(_before) {
                _before = false;
                return _value;
            } else {
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            return _before ? 0 : 1;
        }

        public int previous() {
            if(!_before) {
                _before = true;
                return _value;
            } else {
                throw new NoSuchElementException();
            }
        }

        public int previousIndex() {
            return _before ? -1 : 0;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(int arg0) {
            throw new UnsupportedOperationException();
        }

        public void add(int arg0) {
            throw new UnsupportedOperationException();
        }
        
        private boolean _before = true;
        private int _value = 0;
    }
}
