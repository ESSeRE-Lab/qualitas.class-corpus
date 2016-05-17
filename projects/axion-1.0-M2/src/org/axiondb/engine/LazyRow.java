/*
 * $Id: LazyRow.java,v 1.4 2003/05/13 23:42:59 rwald Exp $
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

package org.axiondb.engine;

import org.axiondb.AxionException;
import org.axiondb.Row;
import org.axiondb.RowSource;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link Row} which loads data from a {@link RowSource}
 * as needed.
 * 
 * @version $Revision: 1.4 $ $Date: 2003/05/13 23:42:59 $
 * @author Rodney Waldhoff 
 */
public class LazyRow extends BaseRow implements Row {
    public LazyRow(RowSource source, int id) {
        this(source,id,-1,null);
    }

    public LazyRow(RowSource source, int id, int col, Object value) {
        super.setIdentifier(id);
        _source = source;
        _knownColumnIndex = col;
        _knownValue = value;
    }

    public Object get(int i) {
        if(i == _knownColumnIndex) {
            return _knownValue;
        } else {
            return getRow().get(i);
        }
    }

    public void set(int i, Object val) throws UnsupportedOperationException {
        getRow().set(i,val);
    }

    public int size() {
        return _source.getColumnCount();
    }

    private Row getRow() {
        if(null == _row) {
            try {
                _row = _source.getRow(getIdentifier());
            } catch(AxionException e) {
                throw ExceptionConverter.convertToRuntimeException(e);
            }
        }
        return _row;
    }

    private Row _row = null;
    private RowSource _source = null;
    private int _knownColumnIndex = -1;
    private Object _knownValue = null;
}
