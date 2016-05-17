/*
 * $Id: ListIteratorRowIterator.java,v 1.4 2003/05/14 17:00:30 rwald Exp $
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

import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.axiondb.Row;

/**
 * A {@link RowIterator} that simply wraps a {@link ListIterator}.
 *
 * @version $Revision: 1.4 $ $Date: 2003/05/14 17:00:30 $
 * @author Rodney Waldhoff
 */
public class ListIteratorRowIterator extends BaseRowIterator {
    public ListIteratorRowIterator(ListIterator iter) {
        _iterator = iter;
    }

    public Row current() {
        if(hasCurrent()) {
            return _currentRow;
        } else {
            throw new NoSuchElementException("No current row has been set.");
        }
    }

    public void add(Row row) {
        _iterator.add(row);
    }
    
    public boolean hasCurrent() {
        return _currentRowSet;
    }

    public boolean hasNext() {
        return _iterator.hasNext();
    }

    public boolean hasPrevious() {
        return _iterator.hasPrevious();
    }

    public Row next() {
        _currentIndex = _iterator.nextIndex();
        _currentRow = (Row)(_iterator.next());
        _currentRowSet = true;
        return _currentRow;
    }

    public Row previous() {
        _currentRow = (Row)(_iterator.previous());
        _currentRowSet = true;
        _currentIndex = _iterator.nextIndex();
        return _currentRow;
    }

    public int currentIndex() {
        return _currentIndex;
    }

    public int nextIndex() {
        return _iterator.nextIndex();
    }

    public int previousIndex() {
        return _iterator.previousIndex();
    }

    public void remove() {
        _iterator.remove();
    }

    public void set(Row row) {
        _iterator.set(row);
    }

    public void reset() {
        while(_iterator.hasPrevious()) {
            _iterator.previous();
        }
        _currentRow = null;
        _currentRowSet = false;
        _currentIndex = -1;
    }


    private ListIterator _iterator = null;
    private Row _currentRow = null;
    private boolean _currentRowSet = false;
    private int _currentIndex = -1;
}

