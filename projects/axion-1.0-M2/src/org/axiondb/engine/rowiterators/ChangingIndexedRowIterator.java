/*
 * $Id: ChangingIndexedRowIterator.java,v 1.2 2003/04/21 15:55:06 rwald Exp $
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

import org.axiondb.AxionException;
import org.axiondb.ComparisonOperator;
import org.axiondb.Index;
import org.axiondb.Table;

/**
 * A {@link DelegatingRowIterator} that is wraps
 * a {@link org.axiondb.RowIterator} from some {@link Index},
 * and that can be {@link #reset reset} to recreate the iterator
 * for a new {@link BindVariable bound value}.
 * 
 * @see org.axiondb.engine.BaseTable#getIndexedRows
 * @see org.axiondb.Index
 * @see org.axiondb.BindVariable
 * 
 * @version $Revision: 1.2 $ $Date: 2003/04/21 15:55:06 $
 * @author Amrish Lal
 */
public class ChangingIndexedRowIterator extends DelegatingRowIterator {
    public ChangingIndexedRowIterator(Index index, Table table, ComparisonOperator op) throws AxionException {
        super(null);
        _index = index;
        _table = table;
        _op = op;
        reset();
    }

    public void reset() throws AxionException {
        if (_indexSet) {
            super.reset();
        }
    }

    public void removeIndexKey() throws AxionException {
        _indexSet = false;
        setDelegate(null);
        reset();        
    }

    public boolean isEmpty() {
        if (_indexSet) {
            return (super.isEmpty());
        }
        return (false);
    }

    public void setIndexKey(Object value) throws AxionException {
        _indexSet = true;
        setDelegate(_index.getRowIterator(_table, _op, value));
        reset();
    }

    public boolean indexSet() {
        return (_indexSet);
    }

    private Index _index = null;
    private Table _table = null;
    private ComparisonOperator _op = null;
    private boolean _indexSet = false;
}
