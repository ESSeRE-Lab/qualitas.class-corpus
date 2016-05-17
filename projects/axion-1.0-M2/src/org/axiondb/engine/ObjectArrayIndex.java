/*
 * $Id: ObjectArrayIndex.java,v 1.7 2003/07/11 17:04:06 rwald Exp $
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.primitives.IntList;
import org.axiondb.AxionException;
import org.axiondb.Column;
import org.axiondb.IndexLoader;

/**
 * An {@link BaseArrayIndex index} over <code>Object</code> keys.
 * 
 * @version $Revision: 1.7 $ $Date: 2003/07/11 17:04:06 $
 * @author Rodney Waldhoff
 */
public class ObjectArrayIndex extends BaseArrayIndex {
    public ObjectArrayIndex(String name, Column column, boolean unique) {
        super(name,column,unique);
        _keys = new ArrayList();
    }

    public ObjectArrayIndex(String name, Column column, boolean unique, ArrayList keys, IntList values) {
        super(name,column,unique,values);
        _keys = keys;
    }

    protected int find(Object seeking, boolean required) {
        int high = _keys.size();
        int low = 0;
        int cur = 0;
        boolean found = false;
        while(low < high) {
            cur = (high+low)/2;
            int comp = getComparator().compare(seeking,_keys.get(cur));
            if(0 == comp) {
                found = true;
                break;
            } else if(comp < 0) {
                high = cur;
            } else { // if(comp > 0)
                if(low == cur) { cur++; }
                low = cur;
            }            
        }
        if(!isUnique()) {
            while(cur > 0 && getComparator().compare(seeking,_keys.get(cur-1)) == 0) {
                cur--;
            }
        }
        if(!found) {
            return required ? -1 : cur;
        } else {
            return cur;
        }
    }

    protected int removeKey(Object value) throws AxionException {
        return removeKey((Comparable)value);
    }
        
    protected int removeKey(Comparable seeking) throws AxionException {
        int index = find(seeking,true);
        if(-1 != index) {
            _keys.remove(index);
        }
        return index;
    }

    protected void removeKeyAt(int index) throws AxionException {
        _keys.remove(index);
    }

    protected int insertKey(Object seeking) throws AxionException {
        int high = _keys.size();
        int low = 0;
        int cur = 0;
        while(low < high) {
            cur = (high+low)/2;
            int comp = getComparator().compare(seeking,_keys.get(cur));
            if(0 == comp) {
                if(isUnique()) {
                    throw new AxionException("Expected " + getIndexedColumn() + " to be unique, found " + seeking + " already.");
                } else {
                    break;
                }
            } else if(comp < 0) {
                high = cur;
            } else { // if(comp > 0)
                if(low == cur) {
                    cur++;
                }
                low = cur;
            }            
        }
        _keys.add(cur,seeking);
        return cur;
    }

    public IndexLoader getIndexLoader() {
        return LOADER;
    }

    protected List getKeyList() {
        return _keys;
    }

    protected List getKeyList(int minIndex, int maxIndex) {
        return _keys.subList(minIndex,maxIndex);
    }
   
    private ArrayList _keys = null;
    private static final IndexLoader LOADER = new ObjectArrayIndexLoader();
}
