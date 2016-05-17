/*
 * $Id: BaseNumberDataType.java,v 1.3 2003/07/07 23:36:12 rwald Exp $
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

package org.axiondb.types;

import java.sql.SQLException;

/**
 * An abstract base {@link org.axiondb.DataType} for {@link Number} types.
 *
 * @version $Revision: 1.3 $ $Date: 2003/07/07 23:36:12 $
 * @author Rodney Waldhoff
 */
public abstract class BaseNumberDataType extends BaseDataType {
    
    public BaseNumberDataType() {
    }

    /**
     * Returns <code>true</code> iff <i>value</i> is <code>String</code>
     * that can be {@link #convert converted} without exception, <code>null</code>,
     * or a {@link Number Number}.
     */
    public boolean accepts(Object value) {
        if(value instanceof Number) {
            return true;
        } else if(null == value) {
            return true;
        } else if(value instanceof String) {
            try {
                convert(value);
                return true;
            } catch(IllegalArgumentException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns <i>value</i> if <i>value</i> is <code>null</code>
     * and throws {@link IllegalArgumentException} otherwise.
     * Subclasses should override this method and call
     * <code>super.convert(value)</code> as their last case.
     */
    public Object convert(Object value) throws IllegalArgumentException {
        if(null == value) {
            return null;
        } else {
            throw new IllegalArgumentException(cantConvertMessage(value));
        }
    }

    /** This base implementation returns <code>true</code>. */
    public boolean supportsSuccessor() {
        return true;
    }

    protected Number toNumber(Object value) throws SQLException {
        try {
            return (Number)(convert(value));
        } catch(IllegalArgumentException e) {
            throw new SQLException(cantConvertMessage(value));
        }
    }
    
    private String cantConvertMessage(Object value) {
        return "Can't convert " + 
                value.getClass().getName() + 
                " " + 
                value + 
                " to " + 
                toString() + 
                ".";
    }
}


