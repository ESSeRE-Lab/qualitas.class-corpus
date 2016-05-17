/*
 * $Id: ChainedRowIterator.java,v 1.7 2003/05/12 22:22:35 rwald Exp $
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
package org.axiondb.engine.rowiterators;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.axiondb.AxionException;
import org.axiondb.Row;
import org.axiondb.RowIterator;

/**
 * Chains together one or more {@link RowIterator}s to make
 * them look like one (similiar to a SQL UNION).
 *
 * @version $Revision: 1.7 $ $Date: 2003/05/12 22:22:35 $
 * @author Rodney Waldhoff
 */
public class ChainedRowIterator extends BaseRowIterator implements RowIterator {
    public ChainedRowIterator() {
    }

    public void addRowIterator(RowIterator iter) {
        _iterators.add(iter);
    }

    public Row current() {
        if(_currentRowSet) {
            return _currentRow;
        } else {
            throw new NoSuchElementException("No current row.");
        }
    }

    public boolean hasCurrent() {
        return _currentRowSet;
    }

    public int currentIndex() {
        return _currentIndex;
    }

    public void reset() throws AxionException {
        for(int i=0;i<_iterators.size();i++) {
            ((RowIterator)(_iterators.get(i))).reset();
        }
        _currentIndex = -1;
        _nextIndex = 0;
        _currentRowSet = false;
        _currentRow = null;
        _currentIterator = 0;
    }

    public Row next() throws AxionException {
        for(;_currentIterator<_iterators.size();_currentIterator++) {
            RowIterator iter = getCurrentRowIterator();
            if(iter.hasNext()) {
                _currentRow = iter.next();
                _currentRowSet = true;
                _currentIndex = _nextIndex;
                _nextIndex++;
                return _currentRow;
            }
        }
        throw new NoSuchElementException("No next row.");
    }

    public Row previous() throws AxionException {
        for(;_currentIterator>=0;_currentIterator--) {
            RowIterator iter = getCurrentRowIterator();
            if(iter.hasPrevious()) {
                _currentRow = iter.previous();
                _currentRowSet = true;
                _nextIndex--;
                _currentIndex = _nextIndex;
                return _currentRow;
            }
        }
        throw new NoSuchElementException("No previous row.");
    }

    public boolean hasNext() {
        for(int i=_currentIterator;i<_iterators.size();i++) {
            RowIterator iter = (RowIterator)_iterators.get(i);
            if(iter.hasNext()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPrevious() {
        return nextIndex() > 0;
    }

    public int nextIndex() {
        return _nextIndex;
    }

    public int previousIndex() {
        return _nextIndex-1;
    }

    public void remove() throws AxionException {
        getCurrentRowIterator().remove();
    }

    public void set(Row row) throws AxionException {
        getCurrentRowIterator().set(row);
    }

    private RowIterator getCurrentRowIterator() {
        RowIterator iter = (RowIterator)_iterators.get(_currentIterator);
        return iter;
    }

    private ArrayList _iterators = new ArrayList();
    private int _nextIndex = 0;
    private int _currentIndex = -1;
    private int _currentIterator = 0;
    private Row _currentRow = null;
    private boolean _currentRowSet = false;
}
