/*
 * $Id: BaseJoinedRowIterator.java,v 1.2 2003/05/13 23:42:59 rwald Exp $
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
import java.util.NoSuchElementException;

import org.axiondb.AxionException;
import org.axiondb.FromNode;
import org.axiondb.JoinedRowIterator;
import org.axiondb.Row;
import org.axiondb.RowDecorator;
import org.axiondb.RowIterator;
import org.axiondb.WhereNode;
import org.axiondb.util.ExceptionConverter;

/**
 * An abstract base implementation of {@link RowIterator}.
 *
 * @version $Revision: 1.2 $ $Date: 2003/05/13 23:42:59 $
 * @author Rodney Waldhoff
 * @author Amrish Lal
 */
public abstract class BaseJoinedRowIterator extends BaseRowIterator implements JoinedRowIterator {

    public void setJoinCondition(RowDecorator decorator, WhereNode condition) {
        _decorator = decorator;
        _condition = condition;
    }

    public void setJoinType(int type) {
        _type = type;
    }

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
        return _currentRowSet;
    }

    public boolean hasNext() {
        if(_nextRowSet) {
            return true;
        } else {
            try {
                return setNextRow();
            } catch(AxionException e) {
                throw ExceptionConverter.convertToRuntimeException(e);
            }
        }
    }


    public Row next() throws AxionException {
        if(!_nextRowSet) {
            if(!setNextRow()) {
                throw new NoSuchElementException();
            }
        }
        _currentIndex = _nextIndex;
        _nextIndex++;
        _currentRow = (Row)(_nextRow);
        _currentRowSet = true;
        _nextRow = null;
        _nextRowSet = false;
        return _currentRow;
    }

    public int nextIndex() {
        return _nextIndex;
    }

    public boolean hasPrevious() {
        return (_nextIndex > 0);
    }

    public Row previous() throws AxionException {
        if(!setPreviousRow()) {
            throw new NoSuchElementException();
        }
        _nextIndex--;
        _currentIndex = _nextIndex;
        _currentRow = (Row)(_previousRow);
        _currentRowSet = true;
        _previousRow = null;
        _previousRowSet = false;
        return _currentRow;
    }

    public int previousIndex() {
        return (_nextIndex-1);
    }

    public Row first() throws AxionException {
        if (!hasPrevious()) {
            next();
        }
        Row row = null;
        while (hasPrevious()) {
            row = previous();
        }
        return (row);
    }

    public Row last() throws AxionException {
        if(!hasNext()) { previous(); }
        Row row = null;
        while(hasNext()) {
            row = next();
        }        
        return row;
    }

    public void reset() throws AxionException {
        for(int i=0;i<_iterators.size();i++) {
            RowIterator iter = ((RowIterator)(_iterators.get(i)));
            iter.reset();
        }
        for(int i=0;i<_iterators.size()-1;i++) {
            RowIterator iter = ((RowIterator)(_iterators.get(i)));
            iter.next();
        }
        _previousRow = null;
        _previousRowSet = false;
        _nextRow = null;
        _nextRowSet = false;
        _currentRow = null;
        _currentIndex = -1;        
        _currentRowSet = false;
        _nextIndex = 0;
    }


    protected boolean setNextRow() throws AxionException {
        throw new AxionException("Implement in subclass.");
    }

    protected boolean setPreviousRow() throws AxionException {
        throw new AxionException("Implement in subclass.");
    }


    protected ArrayList _iterators = new ArrayList();
    protected RowDecorator _decorator = null;
    protected int _type = FromNode.TYPE_UNDEFINED;
    protected WhereNode _condition = null;
    //
    protected Row _previousRow = null;
    protected Row _nextRow     = null;
    protected Row _currentRow  = null;

    //
    protected boolean _previousRowSet = false;
    protected boolean _nextRowSet     = false;
    protected boolean _currentRowSet  = false;

    //
    protected int _currentIndex = -1;
    protected int _nextIndex    = 0;
}
