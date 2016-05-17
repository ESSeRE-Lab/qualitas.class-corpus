/*
 * $Id: Column.java,v 1.16 2002/12/12 19:12:09 rwald Exp $
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
 * Describes a column within a {@link Table}.
 *  
 * @version $Revision: 1.16 $ $Date: 2002/12/12 19:12:09 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class Column {
    /**
     * Create column with the given <i>name</i> and <i>type</i>.  
     * @param name the name of this column, which MUST NOT be <code>null</code>
     * @param type the {@link DataType} of this column, which MUST NOT be <code>null</code>
     * @throws NullPointerException if either parameter is <code>null</code>
     */
    public Column(String name, DataType type) throws NullPointerException {
        if(null == name) {
            throw new NullPointerException("name parameter must not be null");
        }
        if(null == type) {
            throw new NullPointerException("type parameter must not be null");
        }
        _name = name;
        _type = type;
    }
    
    /**
     * Get the name of this column.
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the {@link DataType} of this column.
     */
    public DataType getDataType() {
        return _type;
    }

    /** Two {@link Column}s are equal if they have the same name. */
    public boolean equals(Object that) {
        if(that instanceof Column) {
            Column col = (Column)that;
            return getName().equals(col.getName());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return getName().hashCode();
    }

    private String _name = null;
    private DataType _type = null;
}