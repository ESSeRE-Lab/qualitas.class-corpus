/*
 * $Id: AxionTypesMetaTableUpdater.java,v 1.2 2003/03/27 19:14:03 rwald Exp $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.Database;
import org.axiondb.Row;
import org.axiondb.event.BaseDatabaseModificationListener;
import org.axiondb.event.DatabaseModificationListener;
import org.axiondb.event.DatabaseTypeEvent;

/**
 * Updates the <code>AXION_TYPES</code> meta table
 * 
 * @version $Revision: 1.2 $ $Date: 2003/03/27 19:14:03 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class AxionTypesMetaTableUpdater extends BaseDatabaseModificationListener implements DatabaseModificationListener {
    private static Log _log = LogFactory.getLog(AxionTypesMetaTableUpdater.class);
    private Database _db = null;

    public AxionTypesMetaTableUpdater(Database db) {
        _db = db;
    }

    public void typeAdded(DatabaseTypeEvent e) {
        Row row = createRowForAddedType(e.getName(), e.getDataType());
        try {
            _db.getTable("AXION_TYPES").addRow(row);
        } catch (AxionException ex) {
            _log.error("Unable to mention type in system tables", ex);
        }
    }

    private Row createRowForAddedType(String name, DataType type) {
        SimpleRow row = new SimpleRow(18);
        row.set(0,name);                                  // TYPE_NAME
        row.set(1,new Short((short)type.getJdbcType()));  // DATA_TYPE
        row.set(2,null);                                  // PRECISION
        row.set(3,type.getLiteralPrefix());               // LITERAL_PREFIX
        row.set(4,type.getLiteralSuffix());               // LITERAL_SUFFIX
        row.set(5,null);                                  // CREATE_PARAMS
        row.set(6,new Integer(type.getNullableCode()));   // NULLABLE
        row.set(7,new Boolean(type.isCaseSensitive()));   // CASE_SENSITIVE
        row.set(8,new Integer(type.getSearchableCode())); // SEARCHABLE
        row.set(9,new Boolean(type.isUnsigned()));        // UNSIGNED_ATTRIBUTE
        row.set(10,Boolean.FALSE);                        // FIXED_PREC_SCALE
        row.set(11,Boolean.FALSE);                        // AUTO_INCREMENT
        row.set(12,null);                                 // LOCAL_TYPE_NAME
        row.set(13,null);                                 // MINIMUM_SCALE
        row.set(14,null);                                 // MAXIMUM_SCALE
        row.set(15,null);                                 // SQL_DATA_TYPE
        row.set(16,null);                                 // SQL_DATETIME_SUB
        row.set(17,new Integer(10));                      // NUM_PREC_RADIX
        return row;
    }
}
