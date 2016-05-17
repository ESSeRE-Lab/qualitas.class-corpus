/*
 * $Id: LimitingRowIterator.java,v 1.7 2003/05/12 22:22:35 rwald Exp $
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

package org.axiondb.engine.rowiterators;

import org.axiondb.AxionException;
import org.axiondb.Literal;
import org.axiondb.RowIterator;

/**
 * {@link RowIterator} implementing LIMIT and OFFSET.
  *
 * @version $Revision: 1.7 $ $Date: 2003/05/12 22:22:35 $
 * @author Rodney Waldhoff
 */
public class LimitingRowIterator extends AbstractFilteringRowIterator {
    public LimitingRowIterator(RowIterator iter, Literal limit, Literal offset) {
        super(iter);
        _limit = limit;
        _offset = offset;
    }
    
    protected boolean determineNextRow() throws AxionException {
        if(isPreviousAvailable()) {
            clearPreviousRow();
            if(!determineNextRow()) {
                return false;
            } else {
                clearNextRow();
            }
        }
        // skip to offset if needed and possible
        while(!overOffset(getDelegate().nextIndex())) {
            if(getDelegate().hasNext()) {
                getDelegate().next();
            } else {
                return false;
            }
        }
        // check within my limit
        if(underLimit(nextIndex()) && getDelegate().hasNext()) {
            setNext(getDelegate().next());
            return true;            
        } else {
            return false;
        }
    }

    protected boolean determinePreviousRow() throws AxionException {
        if(isNextAvailable()) {
            clearNextRow();
            if(!determinePreviousRow()) {
                return false;
            } else {
                clearPreviousRow();
            }
        }
        if(overOffset(getDelegate().previousIndex()) && getDelegate().hasPrevious()) {
            setPrevious(getDelegate().previous());
            return true;            
        } else {
            return false;
        }
    }
    
    private boolean underLimit(int index) throws AxionException {
        return null == _limit || (index < ((Number)(_limit.evaluate(null))).intValue());
    }

    private boolean overOffset(int index) throws AxionException {
        return null == _offset || (index >= ((Number)(_offset.evaluate(null))).intValue());
    }
        
    private Literal _limit;
    private Literal _offset;

}

