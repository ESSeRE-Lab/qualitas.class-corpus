/*
 * $Id: Literal.java,v 1.4 2002/11/30 15:56:31 rwald Exp $
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

/**
 * A {@link DataType typed} literal value.
 *
 * @version $Revision: 1.4 $ $Date: 2002/11/30 15:56:31 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public class Literal implements Selectable {

    // public constructors
    //-------------------------------------------------------------------------
    
    public Literal(Object value) {
        _value = value;
    }

    public Literal(Object value, DataType type) {
        _value = value;
        _type = type;
    }

    // protected constructors
    //-------------------------------------------------------------------------
    
    protected Literal(DataType type) {
        _type = type;
    }

    // public methods
    //-------------------------------------------------------------------------
    
    public String getName() {
        return toString();
    }

    public String getLabel() {
        return toString();
    }

    /** 
     * @param row is ignored and may be null.
     * @see #evaluate
     */
    public final Object evaluate(RowDecorator row) throws AxionException {
        return evaluate();
    }

    public void setDataType(DataType type) {
        _type = type;
    }

    public DataType getDataType() {
        return _type;
    }

    public String toString() {
        return String.valueOf(_value);
    }

    // protected methods
    //-------------------------------------------------------------------------
    
    protected Object evaluate() throws AxionException {
        if(null == getValue() && null == getDataType()) {
            return null;
        } else if(null == getDataType()) {
            return getValue();
        } else {
            return getDataType().convert(getValue());
        }
    }

    protected Object getValue() throws AxionException {
        return _value;
    }

    protected void setValue(Object value) {
        _value = value;
    }

    // attributes 
    //-------------------------------------------------------------------------
    
    private Object _value = null;
    private DataType _type= null;
}
