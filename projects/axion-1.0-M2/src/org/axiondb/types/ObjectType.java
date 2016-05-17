/*
 * $Id: ObjectType.java,v 1.2 2002/07/02 21:04:57 rwald Exp $
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.axiondb.DataType;

/**
 * A {@link DataType} representing a {@link Serializable} object value.
 *
 * @version $Revision: 1.2 $ $Date: 2002/07/02 21:04:57 $
 * @author James Strachan
 */
public class ObjectType extends BaseDataType {

    public ObjectType() {
    }

    public int getJdbcType() {
        return java.sql.Types.JAVA_OBJECT;
    }

    public String getPreferredValueClassName() {
        return "java.lang.Object";
    }

    /**
     * Returns <code>"string"</code>
     * @return <code>"string"</code>
     */
    public String toString() {
        return "string";
    }

    /**
     * Returns <code>true</code> iff <i>value</i> is <code>null</code>
     * or a {@link Serializable}.object
     */
    public boolean accepts(Object value) {
        return value == null || value instanceof Serializable;
    }

    /**
     * Returns a {@link String} converted from the given <i>value</i>,
     * or throws {@link IllegalArgumentException} if the given <i>value</i>
     * isn't {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }
        else if (value instanceof Serializable) {
            return value;
        }
        else {
            throw new IllegalArgumentException("The object must implement Serializable: " + value );
        }
    }

    public boolean supportsSuccessor() {
        return true;
    }

    public Object successor(Object value) throws IllegalArgumentException {
        // @TODO: I'm not sure we really want to do this.  The comparator methods
        //        aren't going to rely upon a String comparision in general,
        //        so this could lead to unexpected results
        String v = ((String)value);
        return v + "\0";
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        try {
            ObjectInput objectInput = null;
            if (in instanceof ObjectInput) {
                objectInput = (ObjectInput) in;
            }
            else {
                int size = in.readInt();
                byte[] data = new byte[size];
                in.readFully(data);
                ByteArrayInputStream buffer = new ByteArrayInputStream(data);
                objectInput = new ObjectInputStream(buffer);
            }
            return objectInput.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException("ClassNotFound: " + e.getMessage());
        }
    }

    /**
     * Writes the given <i>value</i> to the given <code>DataOutput</code>.
     * <code>Null</code> values are written as <code>"null", false</code>,
     * the non-null String <code>"null"</code> is written as
     * <code>"null", true</code>, all other values are written directly.
     *
     * @param value the value to write, which must be {@link #accepts acceptable}
     */
    public void write(Object value, DataOutput out) throws IOException {
        if (out instanceof ObjectOutput) {
            ObjectOutput objectOutput = (ObjectOutput) out;
            objectOutput.writeObject( value );
        }
        else {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(buffer);
            objectOut.writeObject(value);
            objectOut.close();
            byte[] data = buffer.toByteArray();
            out.writeInt(data.length);
            out.write(data);
        }
    }

    public DataType makeNewInstance() {
        return new ObjectType();
    }
}


