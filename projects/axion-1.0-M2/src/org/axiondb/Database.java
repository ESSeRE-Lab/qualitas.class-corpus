/*
 * $Id: Database.java,v 1.22 2003/03/27 18:35:26 rwald Exp $
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
import java.util.List;

import org.axiondb.event.DatabaseModificationListener;

/**
 * An Axion database.
 * 
 * @version $Revision: 1.22 $ $Date: 2003/03/27 18:35:26 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 * @author Amrish Lal
 */
public interface Database {
    /** 
     * Returns the name of this <code>Database</code>.
     */
    String getName();

    /** 
     * Get the specified {@link Table}, or <tt>null</tt>
     * if no such table can be found.
     * <p>
     * Table name matching is case-insensitive.
     */
    Table getTable(String name) throws AxionException;

    /** 
     * Get the specified {@link Table}, or <tt>null</tt>
     * if no such table can be found.
     * <p>
     * Table name matching is case-insensitive.
     */
    Table getTable(TableIdentifier table) throws AxionException;

    /** 
     * Drop the specified {@link Table} from this database.
     * <p>
     * Table name matching is case-insensitive.
     */
    void dropTable(String name) throws AxionException;

    /** 
     * Add the given {@link Table} to this database.
     */
    void addTable(Table table) throws AxionException;

    /** 
     * Get the {@link DataType} currently registered for the 
     * given name, or <tt>null</tt>.
     */
    DataType getDataType(String name);

    /** 
     * Get the {@link IndexFactory} currently registered for the 
     * given name, or <tt>null</tt>.
     */
    IndexFactory getIndexFactory(String name);

    /** 
     * Get the {@link TableFactory} currently registered for the 
     * given name, or <tt>null</tt>.
     */
    TableFactory getTableFactory(String name);

    /** 
     * Get the directory into which table information is stored, 
     * or <tt>null</tt>.
     */
    File getTableDirectory();

    /** 
     * "Resolve" the given {@link Selectable} relative 
     * to the given list of {@link TableIdentifier tables}, 
     * converting aliased or relative references into 
     * absolute ones. 
     */
    Selectable resolveSelectable(Selectable selectable, TableIdentifier[] tables) throws AxionException;

    void resolveFromNode(FromNode from, TableIdentifier[] tables) throws AxionException;
    /** 
     * "Resolve" the {@link Selectable}s within the
     * given {@link WhereNode} tree, relative 
     * to the given list of {@link TableIdentifier tables}, 
     * converting aliased or relative references into 
     * absolute ones. 
     */
    void resolveWhereNode(WhereNode where, TableIdentifier[] tables) throws AxionException;

    /** 
     * Make sure any modified state or data has been written to disk.
     */
    void checkpoint() throws AxionException;

    /** 
     * Close this database and free any resources associated with it.
     */
    void shutdown() throws AxionException;

    /** 
     * Notify this database that its root directory has been moved to
     * the given location. (E.g., the CD containing the data for a 
     * CD-resident database has changed drives.)  
     */    
     void remount(File newdir) throws AxionException;

    /** 
     * Is this database read-only?  
     */    
     boolean isReadOnly();

    /**
     * Create a numeric sequence
     */
    void createSequence(Sequence seq) throws AxionException;

    /** 
     * Get the specified {@link Sequence}, or <tt>null</tt>
     * if no such sequence can be found.
     * <p>
     * Sequence name matching is case-insensitive.
     */
    Sequence getSequence(String name);

    /** 
     * Drop the specified {@link Sequence} from this database.
     * <p>
     * Sequence name matching is case-insensitive.
     */
    void dropSequence(String name) throws AxionException;
    
    /** Get the {@link TransactionManager} for this database. */
    TransactionManager getTransactionManager();
    
    /** Update metadata tables since this table has changed. */
    void tableAltered(Table t) throws AxionException;

    /** Adds a listener to receive events on this database */
    void addDatabaseModificationListener(DatabaseModificationListener l);

    /** Returns all listeners set to receive events on this database */
    List getDatabaseModificationListeners();
    
}
