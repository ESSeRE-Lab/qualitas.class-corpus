/*

   Derby - Class org.apache.derby.iapi.jdbc.EngineStatement

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.derby.iapi.jdbc;

import java.sql.SQLException;
import java.sql.Statement;


/**
 * Additional methods the embedded engine exposes on its Statement object
 * implementations. An internal api only, mainly for the network
 * server. Allows consistent interaction between emebdded statements
 * and brokered statements.
 * 
 */
public interface EngineStatement extends Statement {
    
    /**
     * Identical to the JDBC 3 getMoreResults(int).
     * 
     * @see java.sql.Statement#getMoreResults(int)
     */
    public boolean getMoreResults(int current) throws SQLException;
    
    /**
     * Identical to the JDBC 3 getResultSetHoldability(int).
     * 
     * @see java.sql.Statement#getResultSetHoldability()
     */ 
    public int getResultSetHoldability() throws SQLException;

    /**
     * Identical to the JDBC 4 isClosed() method.
     */
    public boolean isClosed() throws SQLException;
    
    ////////////////////////////////////////////////////////////////////
    //
    // INTRODUCED BY JDBC 4.1 IN JAVA 7
    //
    ////////////////////////////////////////////////////////////////////

    public  void    closeOnCompletion() throws SQLException;
    public  boolean isCloseOnCompletion() throws SQLException;
    
}
