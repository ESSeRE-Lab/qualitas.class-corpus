/*
 * $Id: AxionResultSetMetaData.java,v 1.4 2003/05/14 19:07:31 rwald Exp $
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

package org.axiondb.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.axiondb.ColumnIdentifier;
import org.axiondb.DataType;
import org.axiondb.Selectable;

/**
 * A {@link ResultSetMetaData} implementation.
 *
 * @version $Revision: 1.4 $ $Date: 2003/05/14 19:07:31 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 */
public class AxionResultSetMetaData implements ResultSetMetaData {

    public AxionResultSetMetaData(Selectable[] selected) {
        _sels = selected;
    }

    public String getCatalogName(int column) throws SQLException {
        checkColumnIndex(column);
        // per JDBC API Tutorial and Reference (pg 668)
        // return "" if not applicable
        return "";
    }

    public int getColumnCount() throws SQLException {
        return _sels.length;
    }

    public String getColumnClassName(int column) throws SQLException {
        return getDataType(column).getPreferredValueClassName();
    }

    public int getColumnDisplaySize(int column) throws SQLException {
        return getDataType(column).getColumnDisplaySize();
    }

    public String getColumnLabel(int column) throws SQLException {
        return getSelectable(column).getLabel();
    }

    public String getColumnName(int column) throws SQLException {
        return getSelectable(column).getName();
    }

    public int getColumnType(int column) throws SQLException {
        return getDataType(column).getJdbcType();
    }

    public String getColumnTypeName(int column) throws SQLException {
        return getDataType(column).getClass().getName();
    }

    public int getPrecision(int column) throws SQLException {
        return getDataType(column).getPrecision();
    }

    public int getScale(int column) throws SQLException {
        return getDataType(column).getScale();
    }

    public String getSchemaName(int column) throws SQLException {
        checkColumnIndex(column);
        // per JDBC API Tutorial and Reference (pg 673)
        // return "" if not applicable
        return "";
    }

    public String getTableName(int column) throws SQLException {
        checkColumnIndex(column);
        String val = null;
        Selectable sel = getSelectable(column);
        if(sel instanceof ColumnIdentifier) {
            val = ((ColumnIdentifier)(sel)).getTableName();
        }
        if(null == val) {
            return ""; // per JDBC API Tutorial and Reference (pg 673)
        } else {
            return val;
        }
    }

    public boolean isAutoIncrement(int column) throws SQLException {
        checkColumnIndex(column);
        return false;
    }

    public boolean isCaseSensitive(int column) throws SQLException {
        return getDataType(column).isCaseSensitive();
    }

    public boolean isCurrency(int column) throws SQLException {
        return getDataType(column).isCurrency();
    }

    public int isNullable(int column) throws SQLException {
        return getDataType(column).getNullableCode();
    }

    public boolean isSearchable(int column) throws SQLException {
        return getDataType(column).getSearchableCode() != DatabaseMetaData.typePredNone;
    }

    public boolean isSigned(int column) throws SQLException {
        return !(getDataType(column).isUnsigned());
    }

    public boolean isReadOnly(int column) throws SQLException {
        return !(isWritable(column)); // ???
    }

    public boolean isWritable(int column) throws SQLException {
        return true; // ???
    }

    public boolean isDefinitelyWritable(int column) throws SQLException {
        checkColumnIndex(column);
        return false; // ???
    }

    private DataType getDataType(int column) throws SQLException {
        DataType type = getSelectable(column).getDataType();
        if(null != type) {
            return type;
        } else {
            throw new SQLException("Unable to determine the type of column " + column);
        }
    }
    
    private Selectable getSelectable(int column) throws SQLException {
        checkColumnIndex(column);
        Selectable sel = _sels[column-1];
        if(null != sel) {
            return sel;
        } else {
            throw new SQLException("Unable to access column " + column);
        }
    }
    
    private void checkColumnIndex(int column) throws SQLException {
        if(column-1 < 0 || column-1 >= _sels.length) {
            throw new SQLException("Index " + column + " is out of bounds.");
        }
    }

    private Selectable[] _sels = null;

	public Object unwrap(Class iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isWrapperFor(Class iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
}
