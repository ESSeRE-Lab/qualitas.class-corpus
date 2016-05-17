/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.unit;

import java.sql.Connection;
import java.util.Collection;

import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.map.DataMap;

/**
 * @since 1.1
 */
public class SQLServerStackAdapter extends SybaseStackAdapter {

    public SQLServerStackAdapter(DbAdapter adapter) {
        super(adapter);
    }

    @Override
    public boolean handlesNullVsEmptyLOBs() {
        return true;
    }

    @Override
    public void willCreateTables(Connection con, DataMap map) throws Exception {
    }

    @Override
    public void willDropTables(Connection conn, DataMap map, Collection tablesToDrop)
            throws Exception {
        dropConstraints(conn, map, tablesToDrop);
        dropProcedures(conn, map);
    }
    
    @Override
    public boolean supportsNullBoolean() {
        return true;
    }

    // The code below was used with SQLServer <= 2005 to turn of autogenerated keys.
    // Modern SQLServer driver supports autogen keys just fine.

    // public void unchecked(CayenneTestResources resources) {
    // // see if MSSQL driver is used and turn off identity columns in this case...
    //
    // String driver = resources.getConnectionInfo().getJdbcDriver();
    // if (driver != null && driver.startsWith("com.microsoft.") ) {
    // ((JdbcAdapter) getAdapter()).setSupportsGeneratedKeys(false);
    // }
    // }
}
