/*
 * $Id: DiskSystemTable.java,v 1.6 2003/03/27 19:14:03 rwald Exp $
 * =======================================================================
 * Copyright (c) 2003 Axion Development Team.  All rights reserved.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.Row;
import org.axiondb.RowIterator;
import org.axiondb.Table;
import org.axiondb.event.ColumnEvent;
import org.axiondb.event.ConstraintEvent;
import org.axiondb.event.RowEvent;
import org.axiondb.event.TableModificationListener;

/**
 * A disk-resident {@link org.axiondb.Table} in plain-text format.
 * 
 * @version $Revision: 1.6 $ $Date: 2003/03/27 19:14:03 $
 * @author Chuck Burdick
 */
public class DiskSystemTable extends MemoryTable implements Table, TableModificationListener {
    private static final Log _log = LogFactory.getLog(DiskSystemTable.class);
    private File _dir = null;
    private boolean _dirty = false;

    public DiskSystemTable(String name, File dir) {
        super(name);
        if (_log.isDebugEnabled()) {
            _log.debug("Creating system table in " + dir.getAbsolutePath());
        }
        _dir = dir;
        setType(SYSTEM_TABLE_TYPE);
        addTableModificationListener(this);
    }

    public File getDirectory() {
        return _dir;
    }

    public void checkpoint() throws AxionException {
        super.checkpoint();
        if (isDirty()) {
            FileWriter fWriter = null;
            BufferedWriter bWriter = null;
            PrintWriter writer = null;
            try {
                File file = new File(_dir, getName() + ".sys");
                if (_log.isDebugEnabled()) {
                    _log.debug("writing " + file.getAbsolutePath());
                }
                fWriter = new FileWriter(file);
                bWriter = new BufferedWriter(fWriter);
                writer = new PrintWriter(bWriter);
                for (int i = 0; i < getColumnCount(); i++) {
                    if (i > 0) {
                        writer.print(",");
                    }
                    writer.print(getColumn(i).getName());
                }
                writer.println();
                RowIterator it = getRowIterator();
                while (it.hasNext()) {
                    Row cur = it.next();
                    for (int i = 0; i < getColumnCount(); i++) {
                        if (i > 0) {
                            writer.print(",");
                        }
                        DataType type = getColumn(i).getDataType();
                        writer.print(type.toString(cur.get(i)));
                    }
                    writer.println();
                }
                writer.flush();
                _dirty = false;
            } catch (IOException e) {
                throw new AxionException("Unable to create file for table", e);
            } catch (SQLException ex) {
                throw new AxionException("Unable to create file for table", ex);
            } finally {
                if (writer != null) try { writer.close(); } catch (Exception e) {}
                if (bWriter != null) try { bWriter.close(); } catch (Exception e) {}
                if (fWriter != null) try { fWriter.close(); } catch (Exception e) {}
            }
        }
    }

    public boolean isDirty() {
        return _dirty;
    }

    // -------------------------------------------------------------- INTERFACE

    public void columnAdded(ColumnEvent event) throws AxionException {
        _dirty = true;
    }

    public void rowInserted(RowEvent event) throws AxionException {
        _dirty = true;
    }

    public void rowDeleted(RowEvent event) throws AxionException {
        _dirty = true;
    }

    public void rowUpdated(RowEvent event) throws AxionException {
        _dirty = true;
    }

    public void constraintAdded(ConstraintEvent event) throws AxionException {
    }

    public void constraintRemoved(ConstraintEvent event) throws AxionException {
    }
    
}

