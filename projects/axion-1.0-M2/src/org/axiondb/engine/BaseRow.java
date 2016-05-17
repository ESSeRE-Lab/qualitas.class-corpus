/*
 * $Id: BaseRow.java,v 1.2 2002/07/22 23:34:29 rwald Exp $
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

package org.axiondb.engine;

import org.axiondb.Row;

/**
 * An abstract base implementation of {@link Row},
 * providing {@link #equals equals}, 
 * {@link #hashCode hashCode} and
 * {@link #toString toString} implementations.
 *
 * @version $Revision: 1.2 $ $Date: 2002/07/22 23:34:29 $
 * @author Rodney Waldhoff
 */
public abstract class BaseRow implements Row {
    /**
     * Return a hash code for me, in keeping
     * with the generic {@link Object#hashCode}
     * contract.
     */
    public int hashCode() {
        int hash = size();
        for(int i=0;i<size();i++) {
            if(null != get(i)) {
                hash ^= get(i).hashCode();
            } else {
                hash ^= i;
            }
        }
        return hash;
    }

    /**
     * Returns <code>true</code> iff <i>that</i>
     * is a {@link Row} with the same number of 
     * fields and each is equal to the corresponding
     * field in me.
     * <p>
     * Adheres to the generic {@link Object#equals}
     * contract.
     */
    public boolean equals(Object that) {
        if(that instanceof Row) {
            Row thatrow = (Row)that;
            if(size() == thatrow.size()) {
                for(int i=0,m=size();i<m;i++) {
                    Object a = get(i);
                    Object b = thatrow.get(i);
                    if(null == a) {
                        if(null != b) {
                            return false;
                        } else {
                            // they're both null, so keep going
                        }
                    } else if(!a.equals(b)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /** 
     * Returns a simple {@link String} representation 
     * of me, perhaps suitable for debugging purposes.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        for(int i=0;i<size();i++) {
            buf.append(get(i));
            if(i != size()-1) {
                buf.append(",");
            }
        }
        buf.append("}");
        return buf.toString();
    }

    public int getIdentifier() {
        return _id;
    }

    public void setIdentifier(int id) {
        _id = id;
    }

    private int _id = -1;

}

