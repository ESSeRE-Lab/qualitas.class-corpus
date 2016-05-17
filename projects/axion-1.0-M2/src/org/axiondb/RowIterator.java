/*
 * $Id: RowIterator.java,v 1.9 2002/12/17 17:02:44 rwald Exp $
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

import java.util.NoSuchElementException;

/**
 * A bidirectional iterator over a collection of {@link Row}s.
 *
 * @version $Revision: 1.9 $ $Date: 2002/12/17 17:02:44 $
 * @author Rodney Waldhoff
 */
public interface RowIterator {
    /** 
     * Returns the last {@link Row} returned by 
     * me (by {@link #next}, {@link #previous},
     * {@link #first}, {@link #last}, etc.).
     *
     * @throws NoSuchElementException when no {@link Row} has yet been returned
     */
    Row current() throws NoSuchElementException;

    /**
     * Returns the next {@link Row} in the list,
     * or throws {@link java.util.NoSuchElementException}
     * if no next <code>Row</code> exists.
     *
     * @throws NoSuchElementException when there is no next {@link Row}
     * @throws AxionException when a problem occurs accessing the {@link Row}
     */
    Row next() throws NoSuchElementException, AxionException;
    
    /** 
     * Returns the previous {@link Row} in the list,
     * or throws {@link java.util.NoSuchElementException}
     * if no next <code>Row</code> exists.
     *
     * @throws NoSuchElementException when there is no next {@link Row}
     * @throws AxionException when a problem occurs accessing the {@link Row}
     */
    Row previous() throws NoSuchElementException, AxionException;
        
    /** 
     * Returns <code>true</code> if I have more {@link Row}s when 
     * traversing the list in the forward direction. (In other words, 
     * returns <code>true</code> iff {@link #next} would return 
     * a {@link Row} rather than throwing an exception.)
     */
    boolean hasNext();
        
    /** 
     * Returns <code>true</code> if I have more {@link Row}s when 
     * traversing the list in the reverse direction. (In other words, 
     * returns <code>true</code> iff {@link #previous} would return 
     * a {@link Row} rather than throwing an exception.)
     */
    boolean hasPrevious();
        
    /** 
     * Returns <code>true</code> if I have a current {@link Row}. 
     * (In other words, returns <code>true</code> iff
     * {@link #current} would return a {@link Row} rather than 
     * throwing an exception.)
     */
    boolean hasCurrent();    

    /** 
     * Returns <code>true</code> if there are no rows to report with
     * this iterator.
     */
    boolean isEmpty();

    /** 
     * Returns the first {@link Row} in the list,
     * positioning the cursor to just before the 
     * first {@link Row} in the list.  (In other words, 
     * after <code>first</code> is called both 
     * {@link #next} and {@link #current} 
     * will return the first row in the list.)
     *
     * @throws NoSuchElementException when there is no first {@link Row}
     * @throws AxionException when a problem occurs accessing the {@link Row}
     */
    Row first() throws NoSuchElementException, AxionException;
    
    /** 
     * Returns the last {@link Row} in the list,
     * positioning the cursor to just after the 
     * last {@link Row} in the list.  (In other words, 
     * after <code>last</code> is called both 
     * {@link #previous} and {@link #current} 
     * will return the last row in the list.)
     *
     * @throws NoSuchElementException when there is no last {@link Row}
     * @throws AxionException when a problem occurs accessing the {@link Row}
     */
    Row last() throws NoSuchElementException, AxionException;

    /**
     * Return the value that would be returned by a call to {@link #next},
     * if any, but don't update my position.
     *
     * @throws NoSuchElementException when there is no next {@link Row}
     * @throws AxionException when a problem occurs accessing the {@link Row}
     */
    Row peekNext() throws NoSuchElementException, AxionException;

    /**
     * Return the value that would be returned by a call to {@link #previous},
     * if any, but don't update my position.
     *
     * @throws NoSuchElementException when there is no previous {@link Row}
     * @throws AxionException when a problem occurs accessing the {@link Row}
     */
    Row peekPrevious() throws NoSuchElementException, AxionException;
    
    /**
     * Re-initialize this <code>RowIterator</code> to its initial state
     * (positioned just before the first {@link Row} in the list).
     */
    void reset() throws AxionException;
    
    /**
     * Add a {@link Row} at the current position in
     * my underlying collection, or 
     * throw {@link UnsupportedOperationException}.
     * (Optional operation.)
     */
    void add(Row row) throws UnsupportedOperationException, AxionException;

    /**
     * Set the {@link Row} at the current position in
     * my underlying collection, or 
     * throw {@link UnsupportedOperationException}.
     * (Optional operation.)
     */
    void set(Row row) throws UnsupportedOperationException, AxionException;

    /**
     * Set the {@link Row} at the current position in
     * my underlying collection, or 
     * throw {@link UnsupportedOperationException}.
     * (Optional operation.)
     */
    void remove() throws UnsupportedOperationException, AxionException;

    /** 
     * Returns the index of the {@link #current} row, if any.
     *
     * @throws NoSuchElementException when no {@link Row} has yet been returned
     */
    int currentIndex() throws NoSuchElementException;

    /** 
     * Returns the index of the {@link #next} row, if any, 
     * or the number of elements is the iterator if we've
     * reached the end.
     */
    int nextIndex();

    /** 
     * Returns the index of the {@link #previous} row, if any,
     * or -1 if we're add the beginning of the list.
     */
    int previousIndex();
}

