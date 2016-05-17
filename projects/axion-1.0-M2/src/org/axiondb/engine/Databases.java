/*
 * $Id: Databases.java,v 1.5 2003/05/20 20:21:28 rwald Exp $
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.jdbc.AxionConnection;
import org.axiondb.tools.BatchSqlCommandRunner;

/**
 * A static {@link Map} of {@link Database}s by name.
 * 
 * @version $Revision: 1.5 $ $Date: 2003/05/20 20:21:28 $
 * @author Rodney Waldhoff
 */
public class Databases {
    private Databases() { }

    public static synchronized Database getOrCreateDatabase(String name, File dir) throws AxionException {
        name = name.toUpperCase();
        Database db = (Database)(_map.get(name));
        if(null == db) {
            if(null == dir) {
                db = new MemoryDatabase(name);
            } else {
                db = new DiskDatabase(name,dir);
            }
            try {
                runOnStartup(db);
            } catch(SQLException e) {
                _log.warn("SQLException while executing startup script for \"" + name + "\".",e);
                throw new AxionException(e);                
            } catch(IOException e) {
                _log.warn("IOException while executing startup script for \"" + name + "\".",e);
                throw new AxionException(e);                
            }
            _map.put(name,db);
        }
        return db;
    }

    public static synchronized void forgetDatabase(String name) {
        name = name.toUpperCase();
        _map.remove(name);
    }

    private static void runOnStartup(Database db) throws SQLException, IOException  {
        String startupScriptName = System.getProperty(RUN_ON_STARTUP_PREFIX + db.getName() + RUN_ON_STARTUP_SUFFIX);
        if(null != startupScriptName) {
            InputStream scriptStream = INSTANCE.getClass().getClassLoader().getResourceAsStream(startupScriptName);
            if(null != scriptStream) {
                Connection conn = null;
                BatchSqlCommandRunner runner = null;
                
                try {
                    conn = new AxionConnection(db);
                    conn.setAutoCommit(false);
                    runner = new BatchSqlCommandRunner(conn);
                    runner.runCommands(scriptStream);
                    conn.commit();
                } finally {
                    try { runner.close(); } catch(Exception e) { }
                    try { conn.close(); } catch(Exception e) { }
                }                    
            }
            
        }
        
    }

    public static final Databases INSTANCE = new Databases();
    
    private static HashMap _map = new HashMap();
    private static final String RUN_ON_STARTUP_PREFIX = "axiondb.database.";
    private static final String RUN_ON_STARTUP_SUFFIX = ".runonstartup";
    private static Log _log = LogFactory.getLog(Databases.class);

}
