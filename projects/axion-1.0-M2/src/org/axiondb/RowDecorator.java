/*
 * $Id: RowDecorator.java,v 1.5 2003/03/27 19:14:04 rwald Exp $
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

package org.axiondb;

import java.util.Map;

/**
 * A {@link org.axiondb.Row} with meta-information.
 * (Note that we've intentionally not implemented 
 * <code>Row</code> here. <code>Row</code> and 
 * <code>RowDecorator</code> have different contracts. A
 * reference to a <code>Row</code> is somewhat 
 * persistent--it can be added to a Collection, for 
 * example. A {@link RowDecorator} changes all the time.)
 *
 * @version $Revision: 1.5 $ $Date: 2003/03/27 19:14:04 $
 * @author Rodney Waldhoff
 */
public class RowDecorator {
    public RowDecorator(Map selectableToFieldMap) {
        _fieldMap = selectableToFieldMap;
    }

    /** Sets the {@link Row} I'm currently decorating. */
    public void setRow(Row row) {
        setRow(-1,row);
    }
    /** Sets the {@link Row} I'm currently decorating. */
    public void setRow(int rowndx, Row row) {
        _rowndx = rowndx;
        _row = row;
    }

    /** Gets the {@link Row} I'm currently decorating. */
    public Row getRow() {
        return _row;
    }

    /** Returns the value of the specified column. */
    public Object get(ColumnIdentifier colid) {
        Object obj = _row.get(getFieldIndex(colid));
        DataType type = colid.getDataType();
        if(null != type) {
            return type.convert(obj);
        } else {
            return obj;
        }
    }

    /**
     * Sets the value of the specified column.
     * This operation is optional.
     */
    public void set(ColumnIdentifier colid, Object val) throws UnsupportedOperationException {
        _row.set(getFieldIndex(colid),val);
    }

    public int getRowIndex() throws AxionException {
        if(_rowndx == -1) {
            throw new AxionException("Row index not available.");
        } else {
            return _rowndx;
        }
    }

    private int getFieldIndex(Selectable sel) {
        Integer val = (Integer)(_fieldMap.get(sel));
        if(null == val) {
            throw new IllegalArgumentException("Field " + sel + " not found.");
        } else {
            return val.intValue();
        }
    }

    private Row _row = null;
    private Map _fieldMap = null;
    private int _rowndx = -1;
}

