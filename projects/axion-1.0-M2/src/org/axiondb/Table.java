/*
 * $Id: Table.java,v 1.38 2003/07/08 06:55:39 rwald Exp $
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

package org.axiondb;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.primitives.IntIterator;
import org.axiondb.event.TableModificationListener;

/**
 * A database table.
 *
 * @version $Revision: 1.38 $ $Date: 2003/07/08 06:55:39 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public interface Table extends RowSource {
    public static final String REGULAR_TABLE_TYPE = "TABLE";
    public static final String SYSTEM_TABLE_TYPE = "SYSTEM TABLE";

    /** Get the name of this table. */
    public String getName();

    /** Get the type of this table. */
    public String getType();

    /**
     * Adds a listener to receive events on this table
     */
    public void addTableModificationListener(TableModificationListener listener);

    /**
     * Removes a listener so that it stops receiving events on this
     * table
     */
    public void removeTableModificationListener(TableModificationListener listener);

    public void addConstraint(Constraint constraint) throws AxionException;
    public void removeConstraint(String name);
    public Iterator getConstraints();

    /**
     * Add an index, associating it with a {@link Column}, and adding
     * it as a {@link org.axiondb.TableModificationListener} to the table.
     *
     * @see #addIndex
     * @see #addTableModificationListener
     * @see #getIndexForColumn
     * @see #isColumnIndexed
     * @see #populateIndex
     * @param index
     * @exception AxionException
     */
    public void addIndex(Index index) throws AxionException;

   /**
    * Remove an index, both from the indices and as a TableModificationListener
    *
    * @param index
    * @exception AxionException
    */
    public void removeIndex(Index index) throws AxionException;


    /**
     * Populate an {@link Index}, adding my current rows to it. 
     * Does not {@link #addIndex add} the index.
     *
     * @see #addIndex 
     * @param index
     * @exception AxionException
     */
    public void populateIndex(Index index) throws AxionException;

    /**
     * Return the first {@link Index} that pertains to the given
     * {@link Column}, or <code>null</code> if no such
     * {@link Index} exists.
     * 
     * @return the pertinent {@link Column}, or <code>null</code> 
     *         if no such {@link Index} exists
     */
    public Index getIndexForColumn(Column column);

    /**
     * Check to see if an {@link Index} exists for the given
     * {@link Column}
     * 
     * @param column {@link Column} to check
     * @return true iff there is an existing {@link Index} for 
     *         the given {@link Column}
     */
    public boolean isColumnIndexed(Column column);

    /** 
     * Add the given {@link Column} to this table.
     */
    public void addColumn(Column col) throws AxionException;

    /** 
     * Return the {@link Column} corresponding to the 
     * given zero-based <i>index</i>.
     */
    public Column getColumn(int index);

    /** 
     * Return the {@link Column} for the given <i>name</i>. 
     */
    public Column getColumn(String name);

    /** 
     * Indicate whether the {@link ColumnIdentifier} references a
     * column in this table
     */
    public boolean hasColumn(ColumnIdentifier id);

    /** 
     * Return the zero-based index of the {@link Column} with 
     * the given <i>name</i>. 
     */
    public int getColumnIndex(String name) throws AxionException;

    /** 
     * Return an {@link Iterator} over the 
     * {@link ColumnIdentifier ColumnIdentifiers} for my
     * {@link Column}s.
     */
    public Iterator getColumnIdentifiers();

    /** 
     * Return the number of {@link Column}s I contain. 
     */
    public int getColumnCount();

    /** 
     * Insert the given {@link Row}. 
     */
    public void addRow(Row row) throws AxionException;

    /** 
     * Obtain an {@link RowIterator iterator} over my {@link Row}s. 
     * @param readOnly when <code>true</code>, the caller does
     *                 not expect to be able to modify (i.e., call
     *                 {@link RowIterator#set} or {@link RowIterator#remove} on)
     *                 the returned {@link RowIterator}, the returned iterator
     *                 <i>may</i> be unmodifiable. 
     */
    public RowIterator getRowIterator(boolean readOnly) throws AxionException;

    /** 
     * Obtain an {@link RowIterator iterator} over my {@link Row}s where
     * each {@link Selectable Selectable} in the <i>selectable</i> 
     * {@link List list} {@link Selectable#evaluate evaluates} to 
     * the corresponding value in the <i>value</i> {@link List list}.
     * This is functionally similiar to executing a SELECT over
     * this table where <i>selectable[i]</i> = <i>value[i]</i> for
     * each value of <i>i</i>.
     * 
     * The return RowIterator is not modifiable.
     */
    public RowIterator getMatchingRows(List selectables, List values) throws AxionException;

    /** 
     * @param readOnly when <code>true</code>, the caller does
     *                 not expect to be able to modify (i.e., call
     *                 {@link RowIterator#set} or {@link RowIterator#remove} on)
     *                 the returned {@link RowIterator}, the returned iterator
     *                 <i>may</i> be unmodifiable. 
     */
    public RowIterator getIndexedRows(WhereNode where, boolean readOnly) throws AxionException;

    /** 
     * Return the number of {@link Row}s I contain. 
     */
    public int getRowCount();

    /** Reserve a row id. */
    public int getNextRowId();
    
    /** Un-reserve a row id. */
    public void freeRowId(int id); 

    /**
     * Insert the given {@link Row Rows}, but don't invoke any
     * {@link RowEvent}s, triggers, etc.
     * 
     * @param rows an {@link Iterator} over {@link Row Rows} (but not a RowIterator)
     * @throws AxionException
     */
    public void applyInserts(Iterator rows) throws AxionException;

    /**
     * Delete the specified rows (by identifiers), but don't 
     * invoke any {@link RowEvent}s, triggers, etc.
     */
    public void applyDeletes(IntIterator rowids) throws AxionException;

    /**
     * Update the given {@link Row Rows}, but don't invoke any
     * {@link RowEvent}s, triggers, etc.
     */
    public void applyUpdates(Iterator rows) throws AxionException;

    /** Drop this table from the database. */
    public void drop() throws AxionException;
    
    /** Execute a CHECKPOINT, persisting any un-persisted data. */
    public void checkpoint() throws AxionException;

    /** The database is shutting down, shutdown this table also. */
    public void shutdown() throws AxionException;
    
    /** Notify this table that its disk-location has moved. */
    public void remount(File dir, boolean dataOnly) throws AxionException;

    /** Create a {@link TransactableTable} for this table. */
    public TransactableTable makeTransactableTable();

    /** Obtain an {@link Iterator} over my indices. */
    public Iterator getIndices();
}
