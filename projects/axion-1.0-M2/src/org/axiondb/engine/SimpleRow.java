/*
 * $Id: SimpleRow.java,v 1.5 2003/05/10 00:07:07 rwald Exp $
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

import java.io.Serializable;

import org.axiondb.Row;

/**
 * A simple implementation of {@link Row}.
 * 
 * @version $Revision: 1.5 $ $Date: 2003/05/10 00:07:07 $
 * @author Rodney Waldhoff
 */
public class SimpleRow extends BaseRow implements Serializable {

    public SimpleRow(Row that) {
        _fields = new Object[that.size()];
        for(int i=0;i<_fields.length;i++) {
            _fields[i] = that.get(i);
        }
        setIdentifier(that.getIdentifier());
    }

    public SimpleRow(Object[] values) {
        _fields = values;
    }

    public SimpleRow(int id, int size) {
        this(size);
        setIdentifier(id);
    }

    public SimpleRow(int size) {
        _fields = new Object[size];
    }

    /** 
     * Get the value of field <i>i</i>.
     * Note that the index is zero-based.
     */
    public Object get(int i) {
        return _fields[i];
    }

    /** 
     * Set the value of field <i>i</i> to
     * <i>val</i>.
     *
     * Note that the index is zero-based.
     */
    public void set(int i, Object val) {
        _fields[i] = val;
    }

    /**
     * Return the number of fields I contain.
     */
    public int size() {
        return _fields.length;
    }

    private Object[] _fields = null;

}

