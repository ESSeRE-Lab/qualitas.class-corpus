/*
 * $Id: BigDecimalType.java,v 1.4 2003/07/02 15:18:57 rwald Exp $
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

package org.axiondb.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.axiondb.DataType;

/**
 * A {@link DataType} representing an number value.
 *
 * @version $Revision: 1.4 $ $Date: 2003/07/02 15:18:57 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class BigDecimalType extends BaseNumberDataType {
    
    public BigDecimalType() {
        this(0);
    }

    public BigDecimalType(int scale) {
        _scale = scale;
    }

    public int getJdbcType() {
        return java.sql.Types.DECIMAL;
    }

    public String getPreferredValueClassName() {
        return "java.math.BigDecimal";
    }

    public int getScale() {
        return _scale;
    }
    
    /**
     * Returns <code>"BigDecimal"</code>
     * @return <code>"BigDecimal"</code>
     */
    public String toString() {
        return "BigDecimal";
    }

    public boolean accepts(Object value) {
        if(value instanceof String) {
            try {
                convert(value);
                return true;
            } catch(IllegalArgumentException e) {
                return false;
            }
        } else if(value instanceof BigDecimal) {
            return true;
        } else {
            return super.accepts(value);
        }
    }

    /**
     * Returns a <tt>Float</tt> converted from the given <i>value</i>,
     * or throws {@link IllegalArgumentException} if the given <i>value</i>
     * isn't {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws IllegalArgumentException {
        BigDecimal toreturn = null;
        if(value instanceof BigDecimal) {
            toreturn = (BigDecimal)(value);
        } else if(value instanceof BigInteger) {
            toreturn = new BigDecimal((BigInteger)value);
        } else if(value instanceof Double || value instanceof Float) {
            toreturn = new BigDecimal(String.valueOf(value));
        } else if(value instanceof Number) {
            toreturn = BigDecimal.valueOf(((Number)value).longValue());
        } else if(value instanceof String) {
            try {
                toreturn = new BigDecimal((String)value);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Can't parse BigDecimal from " + value);
            }
        } else {
            toreturn = (BigDecimal)(super.convert(value));
        }
        if(null != toreturn) {
            try {
                if(toreturn.scale() != 2) {
                    toreturn = toreturn.setScale(_scale);
                }
            } catch(ArithmeticException e) {
                throw new IllegalArgumentException("BigDecimal " + toreturn + " has scale " + toreturn.scale() + ", can't convert to scale " + _scale + ".");
            }
        }
        return toreturn;
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        String str = in.readUTF();
        if(NULL_BIGDEC.equals(str)) {
            return null;
        } else {
            BigInteger value = new BigInteger(str,TOSTRING_RADIX);
            int scale = in.readInt();
            return new BigDecimal(value,scale);
        }
    }

    /** <code>false</code> */
    public boolean supportsSuccessor() {
        return false;
    }

    public void write(Object value, DataOutput out) throws IOException {
        BigDecimal towrite = (BigDecimal)(convert(value));
        if(null == towrite) {
            out.writeUTF(NULL_BIGDEC);
        } else {
            out.writeUTF(towrite.unscaledValue().toString(TOSTRING_RADIX));
            out.writeInt(towrite.scale());
        }
    }

    public DataType makeNewInstance() {
        // XXX FIX ME XXX
        // this (defaulting to scale 2) is an ugly hack, 
        // but we need to restructure the metadata storage
        // and column creation first
        // XXX FIX ME XXX
        return makeNewInstance(2); 
    }
    
    public DataType makeNewInstance(int scale) {
        return new BigDecimalType(scale);
    }
    
    private int _scale = 0;
    private static final String NULL_BIGDEC = " ";
    private static final int TOSTRING_RADIX = Character.MAX_RADIX;
}


