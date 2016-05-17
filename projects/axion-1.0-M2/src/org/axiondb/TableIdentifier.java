/*
 * $Id: TableIdentifier.java,v 1.2 2002/07/02 00:51:22 cburdick Exp $
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

/**
 * An identifier for a table.
 * <p>
 * Table names and aliases always stored (and returned) in upper case.
 *
 * @version $Revision: 1.2 $ $Date: 2002/07/02 00:51:22 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public class TableIdentifier extends NamedIdentifier {
    public TableIdentifier() {
    }

    public TableIdentifier(String tablename) {
        setTableName(tablename);
    }

    public TableIdentifier(String tablename, String tablealias) {
        setTableName(tablename);
        setTableAlias(tablealias);
    }

    public void setTableName(String table) {
        _table = toUpperOrNull(table);
    }

    public String getTableName() {
        return _table;
    }

    public void setTableAlias(String table) {
        _tableAlias = toUpperOrNull(table);
    }

    public String getTableAlias() {
        return _tableAlias;
    }

    public boolean equals(Object otherobject) {
        if(otherobject instanceof TableIdentifier) {
            TableIdentifier that = (TableIdentifier)otherobject;
            return (
                (null == getTableName() ? null == that.getTableName() : getTableName().equals(that.getTableName())) &&
                (null == getTableAlias() ? null == that.getTableAlias() : getTableAlias().equals(that.getTableAlias()))
            );
        } else {
            return false;
        }
    }

    public int hashCode() {
        int hashCode = 0;
        if(null != _table) {
            hashCode ^= _table.hashCode(); 
        }
        if(null != _tableAlias) {
            hashCode ^= _tableAlias.hashCode() << 4; 
        }
        return hashCode;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(getTableName());
        if(getTableAlias() != null) {
            result.append(" as ");
            result.append(getTableAlias());
        }
        return result.toString();
    }

    private String _table = null;
    private String _tableAlias = null;
}
