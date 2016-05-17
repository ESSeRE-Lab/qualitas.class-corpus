/*
 * $Id: CharacterType.java,v 1.4 2003/05/14 19:07:30 rwald Exp $
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
import java.sql.Types;

import org.axiondb.DataType;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link DataType} representing a single char value.
 *
 * @version $Revision: 1.4 $ $Date: 2003/05/14 19:07:30 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class CharacterType extends BaseDataType {
    
    public CharacterType() {
    }

    /** @return {@link Types#CHAR} */
    public int getJdbcType() {
        return Types.CHAR;
    }

    public String getPreferredValueClassName() {
        return "java.lang.Character";
    }

    public int getPrecision() {
        return 1;
    }

    public boolean isCaseSensitive() {
        return true;
    }

    /**
     * Returns <code>"character"</code>
     * @return <code>"character"</code>
     */
    public String toString() {
        return "character";
    }

    /**
     * Returns <code>true</code> iff <i>value</i> is <code>null</code>,
     * a <tt>Character</tt>, or a single character <tt>String</tt>.
     */
    public boolean accepts(Object value) {
        if(null == value) {
            return true;
        } else if(value instanceof Character) {
            return true;
        } else if(value instanceof String) {
            if(((String)value).length() == 1) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
   
    public boolean supportsSuccessor() {
        return true;
    }

    public Object successor(Object value) throws IllegalArgumentException {
        char c = ((Character)value).charValue();
        if(c == Character.MAX_VALUE) {
            return value;
        } else {
            return new Character((char)((int)c+1));
        }
    }

    /**
     * Returns an <tt>Character</tt> converted 
     * from the given <i>value</i>, or throws 
     * {@link IllegalArgumentException} if the 
     * given <i>value</i>
     * isn't {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws IllegalArgumentException {
        if(null == value) {
            return null;
        } else if(value instanceof Character) {
            return value;
        } else if(value instanceof String) {
            try {
                return new Character(((String)value).charAt(0));
            } catch(RuntimeException e) {
                throw ExceptionConverter.convertToIllegalArgumentException("Can't create a character from " + (String)value,e);                
            }
        } else {
            throw new IllegalArgumentException("Can't convert " + value.getClass().getName() + " " + value + ".");
        }
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        char value = in.readChar();
        if(Character.MIN_VALUE == value) {
            if(!in.readBoolean()) {
                return null;
            }
        }
        return new Character(value);
    }

    /**
     * Writes the given <i>value</i> to the given {@link DataOutput}.
     * <code>Null</code> values are written as <code>Character.MIN_VALUE</code>, 
     * <code>false</code>. <code>Character.MIN_VALUE</code> values are written as 
     * <code>Character.MIN_VALUE</code>, <code>true</code>. All other values are 
     * written directly.
     *
     * @param value the value to write, which must be {@link #accepts acceptable}
     */
    public void write(Object value, DataOutput out) throws IOException {
        if(null == value) {
            out.writeChar(Character.MIN_VALUE);
            out.writeBoolean(false);
        } else {
            char val = ((Character)(convert(value))).charValue();
            out.writeChar(val);
            if(Character.MIN_VALUE == val) {
                out.writeBoolean(true);
            }
        }
    }

    public DataType makeNewInstance() {
        return new CharacterType();
    }
}


