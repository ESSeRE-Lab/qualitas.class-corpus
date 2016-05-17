/*
 * $Id: IndexJoinedRowIterator.java,v 1.2 2003/05/01 16:38:59 rwald Exp $
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

import org.axiondb.RowIterator;
import org.axiondb.Row;
import org.axiondb.AxionException;
import org.axiondb.FromNode;
import org.axiondb.engine.SimpleRow;


/**
 * An abstract base implementation of {@link RowIterator}.
 *
 * @version $Revision: 1.2 $ $Date: 2003/05/01 16:38:59 $
 * @author Amrish Lal
 */

public class IndexJoinedRowIterator extends BaseJoinedRowIterator {
    public IndexJoinedRowIterator(int keypos, int colcount) {
        _keypos = keypos;
        _colcount = colcount;
    }

    public void addRowIterator(RowIterator iterator) throws AxionException {
        if (_iterators.size() == 2) {
            throw new AxionException("IndexedJoinedRowIterator operates over two iterators only.");
        }

        if (_iterators.size() == 1 && (iterator instanceof ChangingIndexedRowIterator) == false) {
            throw new AxionException("Second (or right table iterator) must be of type ChangingIndexedRowIterator.");
        } 

        _iterators.add(iterator);
    }

    protected boolean setNextRow() throws AxionException {
        RowIterator liter = null;
        ChangingIndexedRowIterator riter = null;
        //_lrow = null;
        //_rrow = null;
        
        if (_nextRowSet) {
            return (true);
        }

        if (_previousRowSet) {
            _previousRow = null;
            _previousRowSet = false;
            setNextRow();
        }

        // check if iterators are empty
        Iterator itIt = _iterators.iterator();
        while (itIt.hasNext()) {
            RowIterator iter = (RowIterator) itIt.next();
            if (iter.isEmpty()) {
                return (false);
            }
        }

        liter = (RowIterator) _iterators.get(0);
        riter = (ChangingIndexedRowIterator) _iterators.get(1);
        
        // this is the first time setNextRow has been called || riter does not have any more rows to join.
        if (!riter.indexSet() || (riter.indexSet() && !riter.hasNext())) {
            if (liter.hasNext()) {
                _lrow = liter.next();
                Object value = _lrow.get(_keypos);
                riter.setIndexKey(value);

                // check if there are rows in riter;
                if (!riter.hasNext()) {
                    riter.removeIndexKey();
                    _rrow = null;
                    _nextRow = getJoinedRow();
                    if (_nextRow != null) {
                        _nextRowSet = true;
                        return (true);
                    }
                }
            } else {
                return (false);
            }
        }

        // there are still rows in the riter
        while (riter.hasNext()) {
            _rrow = riter.next();
            _nextRow = getJoinedRow();
            if (_nextRow != null) {
                _nextRowSet = true;
                return (true);
            }
        }

        // no more rows in the riter and we still don't have a joined row.
        return (setNextRow());
    }


    protected boolean setPreviousRow() throws AxionException {
        RowIterator liter = null;
        ChangingIndexedRowIterator riter = null;
        //_lrow = null;
        //_rrow = null;
        
        if (_previousRowSet) {
            return (true);
        }

        if (_nextRowSet) {
            _nextRow = null;
            _nextRowSet = false;
            setPreviousRow();
        }

        // check if iterators are empty
        Iterator itIt = _iterators.iterator();
        while (itIt.hasNext()) {
            RowIterator iter = (RowIterator) itIt.next();
            if (iter.isEmpty()) {
                return (false);
            }
        }

        liter = (RowIterator) _iterators.get(0);
        riter = (ChangingIndexedRowIterator) _iterators.get(1);
        
        // this is the irst time setNextRow has been called || riter does not have any more rows to join.
        if (!riter.indexSet() || (riter.indexSet() && !riter.hasPrevious())) {
            while (liter.hasPrevious()) {
                _lrow = liter.previous();
                Object value = _lrow.get(_keypos);
                riter.setIndexKey(value);

                // check if there are rows in riter;
                if (riter.isEmpty()) {
                    _rrow = null;
                    _previousRow = getJoinedRow();
                    if (_previousRow != null) {
                        _previousRowSet = true;
                        return (true);
                    }
                }
            }
            return (false);
        }


        // there are still rows in the riter
        while (riter.hasPrevious()) {
            _rrow = riter.next();
            _previousRow = getJoinedRow();
            if (_previousRow != null) {
                _previousRowSet = true;
                return (true);
            }
        }

        // no more rows in the riter and we still don't have a joined row.
        return (setPreviousRow());
    }


    private Row getJoinedRow() {
        if (_lrow == null) {
            return (null);
        }

        JoinedRow row = new JoinedRow();
        row.addRow(_lrow);

        if (_rrow != null) {
            row.addRow(_rrow);
        } else {
            if (_type == FromNode.TYPE_LEFT) {
                SimpleRow right = new SimpleRow(_colcount);
                for (int i = 0 ; i < _colcount; i++) {
                    right.set(i, null);
                }
                row.addRow(right);
            } else {
                return (null);
            }
        }

        return (row);        
    }

    private int _keypos = -1;
    private int _colcount = -1;
    private Row _lrow = null;
    private Row _rrow = null;
}
