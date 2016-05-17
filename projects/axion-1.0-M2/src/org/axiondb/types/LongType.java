/*
 * $Id: LongType.java,v 1.5 2003/07/07 23:36:12 rwald Exp $
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
 * A {@link DataType} representing a long value.
 *
 * @version $Revision: 1.5 $ $Date: 2003/07/07 23:36:12 $
 * @author Rodney Waldhoff
 */
public class LongType extends BaseNumberDataType {
    
    public LongType() {
    }

    public int getJdbcType() {
        return java.sql.Types.BIGINT;
    }

    public String getPreferredValueClassName() {
        return "java.lang.Long";
    }

    public int getPrecision() {
        return String.valueOf(Long.MAX_VALUE).length();
    }

    public String toString() {
        return "long";
    }

    /**
     * Returns an <tt>Long</tt> converted from the given <i>value</i>,
     * or throws {@link IllegalArgumentException} if the given <i>value</i>
     * isn't {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws IllegalArgumentException {
        if(value instanceof Long) {
            return value;
        } else if(value instanceof Number) {
            return new Long(((Number)value).longValue());
        } else if(value instanceof String) {
            try {
                return new Long((String)value);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Expected long, found " + value);
            }
        } else {
            return super.convert(value);
        }
    }

    public Object successor(Object value) throws IllegalArgumentException {
        long v = ((Long)value).longValue();
        if(v == Long.MAX_VALUE) {
            return value;
        } else {
            return new Long(++v);
        }
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        long value = in.readLong();
        if(Long.MIN_VALUE == value) {
            if(!in.readBoolean()) {
                return null;
            }
        }
        return new Long(value);
    }

    /**
     * Writes the given <i>value</i> to the given <code>DataOutput</code>.
     * <code>Null</code> values are written as <code>Long.MIN_VALUE</code>, 
     * <code>false</code>. <code>Long.MIN_VALUE</code> values are written as 
     * <code>Long.MIN_VALUE</code>, <code>true</code>. All other values are 
     * written directly.
     *
     * @param value the value to write, which must be {@link #accepts acceptable}
     */
    public void write(Object value, DataOutput out) throws IOException {
        if(null == value) {
            out.writeLong(Long.MIN_VALUE);
            out.writeBoolean(false);
        } else {
            long val = ((Long)(convert(value))).longValue();
            out.writeLong(val);
            if(Long.MIN_VALUE == val) {
                out.writeBoolean(true);
            }
        }
    }

    public DataType makeNewInstance() {
        return new LongType();
    }
}


