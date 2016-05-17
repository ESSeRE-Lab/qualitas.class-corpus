/*
 * $Id: AbstractFilteringRowIterator.java,v 1.9 2003/05/13 23:42:58 rwald Exp $
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

import java.util.NoSuchElementException;

import org.axiondb.AxionException;
import org.axiondb.Row;
import org.axiondb.RowIterator;
import org.axiondb.util.ExceptionConverter;

/**
 * @version $Revision: 1.9 $ $Date: 2003/05/13 23:42:58 $
 * @author Rodney Waldhoff
 */
public abstract class AbstractFilteringRowIterator extends DelegatingRowIterator {

    public AbstractFilteringRowIterator(RowIterator iterator) {
        super(iterator);
    }

    protected abstract boolean determineNextRow() throws AxionException;
    protected abstract boolean determinePreviousRow() throws AxionException;


    public Row current() {
        if(hasCurrent()) {
            return _currentRow;
        } else {
            throw new NoSuchElementException("No current row has been set.");
        }
    }

    public int currentIndex() {
        return _currentIndex;
    }

    public boolean hasCurrent() {
        return _currentAvailable;
    }

    /** Not supported in this base implementation. */
    public void add(Row row) throws AxionException {
        throw new UnsupportedOperationException("AbstractAcceptingRowIterator.add(Object) is not supported.");
    }

    public boolean hasNext() {
        if(_nextAvailable) {
            return true;
        } else {
            try {
                return determineNextRow();
            } catch(AxionException e) {
                throw ExceptionConverter.convertToRuntimeException(e);   
            }
        }
    }

    public boolean hasPrevious() {
        if(_previousAvailable) {
            return true;
        } else {
            try {
                return determinePreviousRow();
            } catch(AxionException e) {
                throw ExceptionConverter.convertToRuntimeException(e);   
            }
        }
    }

    public Row next() throws AxionException {
        if(!_nextAvailable) {
            if(!determineNextRow()) {
                throw new NoSuchElementException();
            }
        }
        _currentIndex = _nextIndex;
        _nextIndex++;
        _currentRow = (Row)(_nextRow);
        _currentAvailable = true;
        clearNextRow();
        return _currentRow;
    }

    public int nextIndex() {
        return _nextIndex;
    }

    public Row previous() throws AxionException {
        if(!_previousAvailable) {
            if(!determinePreviousRow()) {
                throw new NoSuchElementException();
            }
        }
        _nextIndex--;
        _currentIndex = _nextIndex;
        _currentRow = (Row)(_previousRow);
        _currentAvailable = true;
        clearPreviousRow();
        return _currentRow;
    }

    public int previousIndex() {
        return (_nextIndex-1);
    }

    public void remove() throws AxionException {
        getDelegate().remove();
    }

    public void set(Row row) throws AxionException {
        getDelegate().set(row);
    }

    public void reset() throws AxionException {
        super.reset();
        _previousRow = null;
        _previousAvailable = false;
        _nextRow = null;
        _nextAvailable = false;
        _nextIndex = 0;
        _currentRow = null;
        _currentAvailable = false;
        _currentIndex = -1;
    }

    public Row first() throws AxionException {
        if(!hasPrevious()) { next(); }
        Row row = null;
        while(hasPrevious()) {
            row = previous();
        }
        return row;
    }

    public Row last() throws AxionException {
        if(!hasNext()) { previous(); }
        Row row = null;
        while(hasNext()) {
            row = next();
        }       
        return row;
    }

    public Row peekNext() throws AxionException {
        next();
        return previous();
    }

    public Row peekPrevious() throws AxionException {
        previous();
        return next();
    }

    protected void setNext(Row row) {
        _nextRow = row;
        _nextAvailable = true;
    }

    protected void clearNextRow() {
        _nextRow = null;
        _nextAvailable = false;
    }

    protected void setPrevious(Row row) {
        _previousRow = row;
        _previousAvailable = true;
    }

    protected void clearPreviousRow() {
        _previousRow = null;
        _previousAvailable = false;
    }

    protected boolean isNextAvailable() {
        return _nextAvailable;
    }

    protected boolean isPreviousAvailable() {
        return _previousAvailable;
    }
    
    private Row _previousRow = null;
    private boolean _previousAvailable = false;
    private Row _nextRow = null;
    private boolean _nextAvailable = false;
    private int _nextIndex = 0;
    private Row _currentRow = null;
    private boolean _currentAvailable = false;
    private int _currentIndex = -1;
}

