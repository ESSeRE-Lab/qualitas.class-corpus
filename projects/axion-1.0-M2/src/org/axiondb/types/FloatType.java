/*
 * $Id: FloatType.java,v 1.5 2003/07/07 23:36:12 rwald Exp $
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
 * A {@link DataType} representing an float value.
 *
 * @version $Revision: 1.5 $ $Date: 2003/07/07 23:36:12 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class FloatType extends BaseNumberDataType {
    
    public FloatType() {
    }

    public int getJdbcType() {
        return java.sql.Types.REAL;
    }

    public String getPreferredValueClassName() {
        return "java.lang.Float";
    }

    public int getPrecision() {
        return String.valueOf(Float.MAX_VALUE).length();
    }

    public int getScale() {
        return String.valueOf(Float.MAX_VALUE).length();
    }

    /**
     * Returns <code>"float"</code>
     * @return <code>"float"</code>
     */
    public String toString() {
        return "float";
    }

    /**
     * Returns a <tt>Float</tt> converted from the given <i>value</i>,
     * or throws {@link IllegalArgumentException} if the given <i>value</i>
     * isn't {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws IllegalArgumentException {
        if(value instanceof Float) {
            return value;
        } else if(value instanceof Number) {
            return new Float(((Number)value).floatValue());
        } else if(value instanceof String) {
            try {
                return new Float((String)value);
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Expected float, found " + value);
            }
        } else {
            return super.convert(value);
        }
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        float value = in.readFloat();
        if(Float.MIN_VALUE == value) {
            if(!in.readBoolean()) {
                return null;
            }
        }
        return new Float(value);
    }

    /** <code>false</code> */
    public boolean supportsSuccessor() {
        return false;
    }

    public void write(Object value, DataOutput out) throws IOException {
        if(null == value) {
            out.writeFloat(Float.MIN_VALUE);
            out.writeBoolean(false);
        } else {
            float val = ((Float)(convert(value))).floatValue();
            out.writeFloat(val);
            if(Float.MIN_VALUE == val) {
                out.writeBoolean(true);
            }
        }
    }

    public DataType makeNewInstance() {
        return new FloatType();
    }
}


