 /*
 * $Id: BaseBTreeIndex.java,v 1.6 2003/03/27 19:14:03 rwald Exp $
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

import java.io.File;

import org.axiondb.AxionException;
import org.axiondb.Column;
import org.axiondb.ComparisonOperator;
import org.axiondb.Index;

/**
 * Abstract base implementation for B-Tree based {@link Index indices}.
 * 
 * @version $Revision: 1.6 $ $Date: 2003/03/27 19:14:03 $
 * @author Dave Pekarek Krohn
 */
public abstract class BaseBTreeIndex extends BaseIndex implements Index {

    public BaseBTreeIndex(String name, Column column, boolean unique) {
        super(name, column, unique);
    }

    public boolean supportsOperator(ComparisonOperator op) {
        if(ComparisonOperator.EQUAL.equals(op)) {
            if(isUnique()) {
                return true;
            } else {
                return getIndexedColumn().getDataType().supportsSuccessor();
            }
        } else if (ComparisonOperator.LESS_THAN.equals(op)) {
            return true;
        } else if(ComparisonOperator.LESS_THAN_OR_EQUAL.equals(op)) {
            return getIndexedColumn().getDataType().supportsSuccessor();
        } else if (ComparisonOperator.GREATER_THAN.equals(op)) {
            return true;
        } else if(ComparisonOperator.GREATER_THAN_OR_EQUAL.equals(op)) {
            return getIndexedColumn().getDataType().supportsSuccessor();
        } else {
            return false;
        }
    }

    public void save(File dataDirectory) throws AxionException {
        getIndexLoader().saveIndex(this,dataDirectory);
    }


}
