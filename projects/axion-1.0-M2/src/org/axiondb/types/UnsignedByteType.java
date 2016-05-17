/*
 * $Id: UnsignedByteType.java,v 1.5 2003/05/14 19:07:30 rwald Exp $
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

import org.axiondb.DataType;

/**
 * A {@link DataType} representing an unsigned byte value.
 *
 * @version $Revision: 1.5 $ $Date: 2003/05/14 19:07:30 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public class UnsignedByteType extends BaseNumberDataType {
    
    public UnsignedByteType() {
    }

    public int getJdbcType() {
        return java.sql.Types.TINYINT;
    }

    public String getPreferredValueClassName() {
        return "java.lang.Short";
    }

    public int getPrecision() {
        return String.valueOf(Short.MAX_VALUE).length();
    }

    public String toString() {
        return "unsignedbyte";
    }

    /**
     * Returns <code>true</code> iff <i>value</i> is <code>null</code>,
     * a <tt>Number</tt>, within the unsigned byte range (0 to 255).
     */
    public boolean accepts(Object value) {
        if(value instanceof Number) {
            short s = ((Number)value).shortValue();
            if(s < MIN_VALUE || s > MAX_VALUE) {
                return false;
            } else {
                return true;
            }
        } else if(value instanceof String) {
            try {
                convert(value);
                return true;
            } catch(IllegalArgumentException e) {
                return false;
            }
        } else {
            return super.accepts(value);
        }
    }

    /**
     * Returns an <tt>Short</tt> converted from the given <i>value</i>,
     * or throws {@link IllegalArgumentException} if the given <i>value</i>
     * isn't {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws IllegalArgumentException {
        if(value instanceof Number) {
            short s = ((Number)value).shortValue();
            assertValidUnsignedByte(s);
            return new Short(s);
        } else if(value instanceof String) {
            try {
                Short sval = new Short((String)value);
                assertValidUnsignedByte(sval.shortValue());
                return sval;
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Expected short, found " + value);
            }
        } else {
            return super.convert(value);
        }
    }

    public Object successor(Object value) throws IllegalArgumentException {
        short v = ((Short)value).shortValue();
        assertValidUnsignedByte(v);
        if(v == MAX_VALUE) {
            return value;
        } else {
            return new Short(++v);
        }
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        short value = toShort(in.readByte());
        if(MIN_VALUE == value) {
            if(!in.readBoolean()) {
                return null;
            }
        }
        return new Short(value);
    }

    /**
     * Writes the given <i>value</i> to the given <code>DataOutput</code>.
     * <code>Null</code> values are written as <code>MIN_VALUE</code>, 
     * <code>false</code>. <code>MIN_VALUE</code> values are written as 
     * <code>MIN_VALUE</code>, <code>true</code>. All other values are 
     * written directly.
     *
     * @param value the value to write, which must be {@link #accepts acceptable}
     */
    public void write(Object value, DataOutput out) throws IOException {
        if(null == value) {
            out.writeByte(fromShort(MIN_VALUE));
            out.writeBoolean(false);
        } else {
            short val = ((Short)(convert(value))).shortValue();
            out.writeByte(fromShort(val));
            if(MIN_VALUE == val) {
                out.writeBoolean(true);
            }
        }
    }

    public DataType makeNewInstance() {
        return new UnsignedByteType();
    }

    public boolean isUnsigned() {
        return true;
    }

    private final short toShort(byte value) { 
        return (short)(((short)value)&MAX_VALUE);
    }

    private final byte fromShort(short value) {
        return (byte)(value&MAX_VALUE);
    }

    private final void assertValidUnsignedByte(short value) throws IllegalArgumentException {
        if(value > MAX_VALUE) {
            throw new IllegalArgumentException(value + " > " + MAX_VALUE);
        }
        if(value < MIN_VALUE) {
            throw new IllegalArgumentException(value + " < " + MIN_VALUE);
        }
    }

    private static final short MIN_VALUE = (short)0;
    private static final short MAX_VALUE = (short)255;
}
