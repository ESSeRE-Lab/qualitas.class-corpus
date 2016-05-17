/*
 * $Id: BooleanType.java,v 1.8 2003/05/14 19:07:30 rwald Exp $
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
import java.sql.SQLException;
import java.sql.Types;
import java.util.Comparator;

import org.axiondb.DataType;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link DataType} representing a Boolean value.
 *
 * @version $Revision: 1.8 $ $Date: 2003/05/14 19:07:30 $
 * @author Rodney Waldhoff
 */
public class BooleanType extends BaseDataType {
    
    public BooleanType() {
    }

    /** @return {@link Types#BIT} */
    public int getJdbcType() {
        return Types.BIT;
    }

    public String getPreferredValueClassName() {
        return "java.lang.Boolean";
    }

    public int getPrecision() {
        return 1;
    }

    public Comparator getComparator() {
        return COMPARATOR_INSTANCE;
    }
    
    /**
     * Returns <code>"boolean"</code>
     * @return <code>"boolean"</code>
     */
    public String toString() {
        return "boolean";
    }

    public boolean toBoolean(Object value) throws SQLException {
        try {
            return ((Boolean)(convert(value))).booleanValue();
        } catch(ClassCastException e) {
            throw ExceptionConverter.convert("Can't convert " + value + " to boolean.",e);            
        } catch(NullPointerException e) {
            throw ExceptionConverter.convert("Can't convert null to boolean.",e);
        } catch(IllegalArgumentException e) {
            throw ExceptionConverter.convert("Can't convert " + value + " to boolean.",e);
        }
    }

    /**
     * Returns <code>true</code> iff <i>value</i> is <code>null</code>,
     * or a <code>Boolean</code>.
     */
    public boolean accepts(Object value) {
        if(null == value) {
            return true;
        } else if(value instanceof Boolean) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a <code>Boolean</code> converted from the given <i>value</i>,
     * or throws {@link IllegalArgumentException} if the given <i>value</i>
     * isn't {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws IllegalArgumentException {
        if(null == value) {
            return null;
        } else if(value instanceof Boolean) {
            return value;
        } else {
            throw new IllegalArgumentException("Can't convert " + value.getClass().getName() + " " + value + " into a Boolean.");
        }
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        byte value = in.readByte();
        switch(value) {
            case -1: return null;
            case  0: return Boolean.FALSE;
            case  1: return Boolean.TRUE;
            default: throw new IOException("Expected -1, 0, or 1, found " + value);
        }
    }

    /**
     * Writes the given <i>value</i> to the given <code>DataOutput</code>.
     * <code>Null</code> values are written as <code>(byte)-1</code>, 
     * <code>false</code> values are written as <code>(byte)0</code>,
     * <code>true</code> values are written as <code>(byte)1</code>.
     *
     * @param value the value to write, which must be {@link #accepts acceptable}
     */
    public void write(Object value, DataOutput out) throws IOException {
        if(null == value) {
            out.writeByte(-1);
        } else {
            boolean val = ((Boolean)(convert(value))).booleanValue();
            if(val) {
                out.writeByte(1);
            } else {
                out.writeByte(0);
            }
        }
    }

    public DataType makeNewInstance() {
        return new BooleanType();
    }
    
    /**
     * A {@link Comparator} for Boolean values.
     * (This may be a good candidate to move to commons-collections.)
     */
    static class BooleanComparator implements Comparator {
        
        public int compare(Object left, Object right) {
            return compare(((Boolean)left).booleanValue(),((Boolean)right).booleanValue());
        }
        
        private final int compare(boolean left, boolean right) {
            // sorts false before true            
            return (left ? ( right ? 0 : 1 ) : ( right ? -1 : 0 ));
        }
        
    }

    private static final BooleanComparator COMPARATOR_INSTANCE = new BooleanComparator();
}


