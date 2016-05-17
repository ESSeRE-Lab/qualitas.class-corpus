/*
 * $Id: UnsignedShortType.java,v 1.6 2003/05/14 19:07:30 rwald Exp $
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
 * A {@link DataType} representing an unsigned short value as an int.
 *
 * @version $Revision: 1.6 $ $Date: 2003/05/14 19:07:30 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public class UnsignedShortType extends BaseNumberDataType {
    
    public UnsignedShortType() {
    }

    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    public String getPreferredValueClassName() {
        return "java.lang.Integer";
    }

    public String toString() {
        return "unsignedshort";
    }

    public int getPrecision() {
        return String.valueOf(Integer.MAX_VALUE).length();
    }

    public boolean accepts(Object value) {
        if(value instanceof Number) {
            int s = ((Number)value).intValue();
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
     * Returns an <tt>Integer</tt> converted from the given <i>value</i>,
     * or throws {@link IllegalArgumentException} if the given <i>value</i>
     * isn't {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws IllegalArgumentException {
        if(value instanceof Number) {
            int s = ((Number)value).intValue();
            assertValidUnsignedShort(s);
            return new Integer(s);
        } else if(value instanceof String) {
            try {
                Integer ival = new Integer((String)value);
                assertValidUnsignedShort(ival.intValue());
                return ival;
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Expected integer, found " + value);
            }
        } else {
            return super.convert(value);
        }
    }

    public Object successor(Object value) throws IllegalArgumentException {
        int v = ((Integer)value).intValue();
        assertValidUnsignedShort(v);
        if(v == MAX_VALUE) {
            return value;
        } else {
            return new Integer(++v);
        }
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        int value = toInt(in.readShort());
        if(MIN_VALUE == value) {
            if(!in.readBoolean()) {
                return null;
            }
        }
        return new Integer(value);
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
            out.writeShort(fromInt(MIN_VALUE));
            out.writeBoolean(false);
        } else {
            int val = ((Integer)(convert(value))).intValue();
            out.writeShort(fromInt(val));
            if(MIN_VALUE == val) {
                out.writeBoolean(true);
            }
        }
    }

    public DataType makeNewInstance() {
        return new UnsignedShortType();
    }

    public boolean isUnsigned() {
        return true;
    }

    private final int toInt(short value) { 
        return ((int)value)&MAX_VALUE;
    }

    private final short fromInt(int value) {
        return (short)(value&MAX_VALUE);
    }

    private final void assertValidUnsignedShort(int value) throws IllegalArgumentException {
        if(value > MAX_VALUE) {
            throw new IllegalArgumentException(value + " > " + MAX_VALUE);
        }
        if(value < MIN_VALUE) {
            throw new IllegalArgumentException(value + " < " + MIN_VALUE);
        }
    }

    private static final int MIN_VALUE = (int)0;
    private static final int MAX_VALUE = (int)0xFFFF;
}
