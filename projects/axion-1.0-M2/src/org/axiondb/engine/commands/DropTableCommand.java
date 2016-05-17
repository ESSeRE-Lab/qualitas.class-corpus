/*
 * $Id: DropTableCommand.java,v 1.10 2003/07/07 16:14:28 rwald Exp $
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

package org.axiondb.engine.commands;

import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.jdbc.AxionResultSet;

/**
 * A <code>DROP TABLE</code> command.
 * @version $Revision: 1.10 $ $Date: 2003/07/07 16:14:28 $
 * @author Rodney Waldhoff 
 */
public class DropTableCommand extends BaseAxionCommand {
    String _tableName = null;
    boolean _exists = false;

    public DropTableCommand(String tableName, boolean exists) {
        _tableName = tableName;
        _exists = exists;
    }

    

    public boolean execute(Database db) throws AxionException {
        if (_exists && db.getTable(_tableName) == null) {
            return false;
        }
        assertNotReadOnly(db);
        db.dropTable(_tableName);
        return false;
    }

    /** Unsupported */
    public AxionResultSet executeQuery(Database database) throws AxionException {
        throw new UnsupportedOperationException("Use execute.");
    }

    public int executeUpdate(Database database) throws AxionException {
        execute(database);
        return 0;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("DROP TABLE ");
        buf.append(_tableName);
        return buf.toString();
    }
}
