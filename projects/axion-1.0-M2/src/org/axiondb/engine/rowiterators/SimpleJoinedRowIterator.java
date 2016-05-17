/*
 * $Id: SimpleJoinedRowIterator.java,v 1.3 2003/04/21 15:55:06 rwald Exp $
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

import java.util.Iterator;

import org.axiondb.AxionException;
import org.axiondb.FromNode;
import org.axiondb.Row;
import org.axiondb.RowDecorator;
import org.axiondb.RowIterator;
import org.axiondb.engine.SimpleRow;

/**
 * A {@link RowIterator} that iterates over zero or
 * more <code>RowIterator</code>s, returning
 * {@link Row}s that are the cross product of
 * the <code>Row</code>s returned by the contained
 * iterators.
 *
 * This class implements the nested loop join algorithm
 * and can produce inner join, left outer join, and right
 * outer join of the contained iterators.
 *
 * @version $Revision: 1.3 $ $Date: 2003/04/21 15:55:06 $
 * @author Rodney Waldhoff
 * @author Amrish Lal
 */
public class SimpleJoinedRowIterator extends BaseJoinedRowIterator {
    public SimpleJoinedRowIterator() {
        this(FromNode.TYPE_UNDEFINED);
    }

    public SimpleJoinedRowIterator(int joinType) {
        _type = joinType;
    }

    public void addRowIterator(RowIterator iterator) throws AxionException {
        if(!_iterators.isEmpty()) {
            RowIterator last = (RowIterator)(_iterators.get(_iterators.size()-1));
            if(last.hasNext()) {
                last.next();
            }
        }
        _iterators.add(iterator);
    }

    private boolean iteratorsAreEmpty() {
        Iterator itIt = _iterators.iterator();
        while (itIt.hasNext()) {
            RowIterator iter = (RowIterator)itIt.next();
            if (iter.isEmpty()) {
                return (true);
            }
        }
        return (false);
    }

    private boolean iteratorsHasNext() {
        boolean hasNext = false;
        int count = _iterators.size() - 1;
        while (!hasNext && count >= 0) {
            RowIterator iter = ((RowIterator)(_iterators.get(count)));
            hasNext = iter.hasNext();
            count--;
        }
        return (hasNext);
    }

    private boolean iteratorsHasPrevious() {
        boolean hasPrevious = false;
        int count = _iterators.size() - 1;
        while (!hasPrevious && count >= 0) {
            RowIterator iter = ((RowIterator)(_iterators.get(count)));
            hasPrevious = iter.hasPrevious();
            count--;
        }
        return (hasPrevious);
    }

    private boolean iteratorsWillCycleToLast() {
        if (_type != FromNode.TYPE_LEFT || _type != FromNode.TYPE_RIGHT) {
            return (((RowIterator)(_iterators.get(1))).hasPrevious() == false);
        }
        return (false);
    }

    private boolean iteratorsWillCycleToFirst() {
        if (_type != FromNode.TYPE_LEFT || _type != FromNode.TYPE_RIGHT) {
            return (((RowIterator)(_iterators.get(1))).hasNext() == false);
        }
        return (false);
    }

    private boolean iteratorsNext() throws AxionException {
        for (int i = _iterators.size() - 1; i >= 0; i--) {
            RowIterator iter = ((RowIterator)(_iterators.get(i)));
            if (iter.hasNext()) {
                if (i != _iterators.size()-1 && iter.currentIndex() == iter.nextIndex()) {
                    iter.next();
                }
                iter.next();
                if (i < _iterators.size() - 1) {
                    for (int j = i + 1; j <= _iterators.size() - 1; j++) {
                        RowIterator subiter = ((RowIterator)(_iterators.get(j)));
                        subiter.first();
                        subiter.next();
                    }
                }
                //
                if (i == 0 && (_type == FromNode.TYPE_LEFT || _type == FromNode.TYPE_RIGHT) ) {
                    _acceptableStatus = false;
                }
                return (true);
            }
        }
        return (false);
    }
    
    private boolean iteratorsPrevious() throws AxionException {
        for (int i = _iterators.size() - 1; i >= 0; i--) {
            RowIterator iter = ((RowIterator)(_iterators.get(i)));
            if (iter.hasPrevious()) {
                if (i != _iterators.size()-1 && iter.currentIndex() == iter.previousIndex()) {
                    iter.previous();
                }
                iter.previous();
                if (i < _iterators.size() - 1) {
                    for (int j = i + 1; j <= _iterators.size() - 1; j++) {
                        RowIterator subiter = ((RowIterator)(_iterators.get(j)));
                        subiter.last();
                        subiter.previous();
                    }
                }
                //
                if (i == 0 && (_type == FromNode.TYPE_LEFT || _type == FromNode.TYPE_RIGHT) ) {
                    _acceptableStatus = false;
                }
                return (true);
            }
        }
        return (false);
    }


    protected boolean setNextRow() throws AxionException {
        if (_nextRowSet) {
            return (true);
        }
        
        if (_previousRowSet) {
            _previousRow = null;
            _previousRowSet = false;
            setNextRow();
        }

	if (iteratorsAreEmpty()) {
	    return (false);
	}

        // continue to loop until we can not increment anymore or until
        // a valid joined row is found.
        while(true) {
            if (iteratorsHasNext() == true) {
		iteratorsNext();
                // construct a joined row. If joined row matches join condition
                // return.
                Row row = joinCurrent();
                if (acceptable(nextIndex(), row)) {
                    _nextRow = row;
                    _nextRowSet = true;
                    _acceptableStatus = true;
                    return (true);
                }
            } else {
                return (false);
            }

            // If we have cycled through the inner table and still there is
            // no matching row then we need to carry out a left or right
            // outer join if specified.
            if (iteratorsWillCycleToFirst() && _acceptableStatus == false) {
                Row row = applyJoinType();
                if (row != null) {
                    _nextRow = row;
                    _nextRowSet = true;
                    return (true);
                }
            }
        }
    }

    protected boolean setPreviousRow() throws AxionException {
        if (_previousRowSet) {
            return (true);
        }
        
        if (_nextRowSet) {
            _nextRow = null;
            _nextRowSet = false;
            setPreviousRow();
        }

	if (iteratorsAreEmpty()) {
	    return (false);
	}

        // continue to loop until we cannot decrement anymore or until
        // a valid joined row is found.
        for (;;) {
            if (iteratorsHasPrevious() == true) {
                // construct a new joined row. If the joined row matches the
                // join condition then return
		iteratorsPrevious();
                Row row = joinCurrent();
                if (acceptable(previousIndex(), row)) {
                    _previousRow = row;
                    _previousRowSet = true;
                    _acceptableStatus = true;
                    return (true);
                }
            } else {
                return (false);
            }

            // If we have cycled through the inner table and still there is
            // no matching row then we need to carry out a left or right
            // outer join if specified.
            if (iteratorsWillCycleToLast() && _acceptableStatus == false) {
                Row row = applyJoinType();
                if (row != null) {
                    _previousRow = row;
                    _previousRowSet = true;
                    return (true);
                }
            }
        }
    }

    /*
    private void clearCurrentRow() {
        _currentRow = null;
        _currentRowSet = false;
    }
    */

    /**
     * Produces a cross product row from the current rows of the contained
     * iterators.
     *
     */
    private JoinedRow joinCurrent() throws AxionException {
        JoinedRow row = new JoinedRow();
        if (_type == FromNode.TYPE_RIGHT) {
            // For righter out join, the inner table is the right table and the
            // outer table is the left table (see SelectCommand.java), so we
            // need to add the inner table first and outer table next to create
            // a valid right outer joined row.
            row.addRow(((RowIterator)(_iterators.get(1))).current());
            row.addRow(((RowIterator)(_iterators.get(0))).current());
            if (_iterators.size() == 3) {
                row.addRow(((RowIterator)(_iterators.get(2))).current());
            }
        } else {
            for(int i=0;i<_iterators.size();i++) {
                RowIterator iter = ((RowIterator)(_iterators.get(i)));
                row.addRow(iter.current());
            }
        }

        return (row);
    }

    private JoinedRow applyJoinType() throws AxionException {
        if (_type != FromNode.TYPE_RIGHT
            && _type != FromNode.TYPE_LEFT) {
            // not supported yet.
            return (null);
        }
        
        JoinedRow row = new JoinedRow();

        // there are atleast two tables present in the join.
        if (_type == FromNode.TYPE_LEFT) {
            // number of columns in the right (or inner) table.
            int cardinality = (((RowIterator)(_iterators.get(1))).current()).size();
            SimpleRow right = new SimpleRow(cardinality);

            // add the left table row.
            row.addRow(((RowIterator)(_iterators.get(0))).current());            
            for (int i = 0; i < cardinality; i++) {
                right.set(i, null);
            }
            // add the null right table row.
            row.addRow(right);
        }

        if (_type == FromNode.TYPE_RIGHT) {
            // number of columns in the left (or inner) table.
            int cardinality = (((RowIterator)(_iterators.get(1))).current()).size();
            SimpleRow left = new SimpleRow(cardinality);
            for (int i = 0; i < cardinality; i++) {
                left.set(i, null);
            }
            // add the null left row to the join
            row.addRow(left);
            // add the right row to the join.
            row.addRow(((RowIterator)(_iterators.get(0))).current());
        }

        if (_iterators.size() == 3) {
            // literals exist
            row.addRow(((RowIterator)(_iterators.get(2))).current());
        }

        return (row);
    }

    protected boolean acceptable(int rowindex, Row row) throws AxionException {
        boolean result = true;
        if (_condition != null) {
            try {
                result = ((Boolean)_condition.evaluate(decorate(rowindex, row))).booleanValue();
            } catch(ClassCastException e) {
                throw new AxionException("Expected Boolean valued expression",e);
            }
        }
        return (result);
    }

    private RowDecorator decorate(int rowindex, Row row) {
        _decorator.setRow(rowindex, row);
        return (_decorator);
    }

    protected boolean _acceptableStatus = false;
}
