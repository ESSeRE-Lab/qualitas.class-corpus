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
package org.apache.cayenne.itest;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A JDBC template for reading a single row from the database.
 * 
 */
abstract class RowTemplate extends ResultSetTemplate {

    public RowTemplate(ItestDBUtils parent) {
        super(parent);
    }

    abstract void readRow(ResultSet rs, String sql) throws SQLException;

    @Override
    void readResultSet(ResultSet rs, String sql) throws SQLException {
        if (rs.next()) {

            readRow(rs, sql);

            if (rs.next()) {
                throw new SQLException("More than one result for sql: " + sql);
            }
        }
        else {
            throw new SQLException("No results for sql: " + sql);
        }
    }
}
