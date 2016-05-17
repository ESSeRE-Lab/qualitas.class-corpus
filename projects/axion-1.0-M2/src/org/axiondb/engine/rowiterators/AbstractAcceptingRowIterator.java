/*
 * $Id: AbstractAcceptingRowIterator.java,v 1.1 2003/05/12 22:22:35 rwald Exp $
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

package org.axiondb.engine.rowiterators;

import org.axiondb.AxionException;
import org.axiondb.Row;
import org.axiondb.RowIterator;

/**
 * Abstract base {@link DelegatingRowIterator} that
 * excludes {@link Row}s that are not {@link #acceptable acceptable}.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/05/12 22:22:35 $
 * @author Rodney Waldhoff
 */
public abstract class AbstractAcceptingRowIterator extends AbstractFilteringRowIterator {
    /**
     * My filtering method.
     * Return <code>true</code> if the given {@link Row} should
     * be included in the iteration, <code>false</code> otherwise.
     */
    protected abstract boolean acceptable(int rowindex, Row row) throws AxionException;

    public AbstractAcceptingRowIterator(RowIterator iterator) {
        super(iterator);
    }

    protected boolean determineNextRow() throws AxionException {
        // if _previousRowSet is true,
        // then we've walked back in the delegate list
        // so skip one matching row
        if(isPreviousAvailable()) {
            clearPreviousRow();
            if(!determineNextRow()) {
                return false;
            } else {
                clearNextRow();
            }
        }
        while(getDelegate().hasNext()) {
            Row row = (Row)(getDelegate().next());
            if(acceptable(nextIndex(),row)) {
                setNext(row);
                return true;
            }
        }
        return false;
    }

    protected boolean determinePreviousRow() throws AxionException {
        // if _nextRowSet is true,
        // then we've walked forward in the delegate list
        // so skip one matching row
        if(isNextAvailable()) {
            clearNextRow();
            if(!determinePreviousRow()) {
                return false;
            } else {
                clearPreviousRow();
            }
        }
        while(getDelegate().hasPrevious()) {
            Row row = (Row)(getDelegate().previous());
            if(acceptable(previousIndex(),row)) {
                setPrevious(row);
                return true;
            }
        }
        return false;
    }
}

