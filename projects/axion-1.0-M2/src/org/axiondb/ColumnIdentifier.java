/*
 * $Id: ColumnIdentifier.java,v 1.8 2003/02/07 01:44:13 rwald Exp $
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

/**
 * An identifier for a column.
 * <p>
 * Column names and aliases always stored (and returned) in upper case.
 *
 * @version $Revision: 1.8 $ $Date: 2003/02/07 01:44:13 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public class ColumnIdentifier extends NamedIdentifier implements Selectable {
    /**
     * @param column the local name of my column
     */
    public ColumnIdentifier(String column) {
        this(null,column);
    }

    /**
     * @param table my table, which may be <code>null</code>
     * @param column my column
     */
    public ColumnIdentifier(TableIdentifier table, String columnName) {
        this(table, columnName, null);
    }

    /**
     * @param table my table, which may be <code>null</code>
     * @param column my column
     * @param columnAlias the alias for my column, which may be <code>null</code>
     */
    public ColumnIdentifier(TableIdentifier table, String columnName, String columnAlias) {
        this(table, columnName, columnAlias, null);
    }

    /**
     * @param table my table, which may be <code>null</code>
     * @param column my column
     * @param columnAlias the alias for my column, which may be <code>null</code>
     * @param type the {@link DataType} of my column, which may be <code>null</code>
     */
    public ColumnIdentifier(TableIdentifier table, String columnName, String columnAlias, DataType type) {
        setTableIdentifier(table);
        setName(columnName);
        setAlias(columnAlias);
        setDataType(type);
    }

    /**
     * Returns the value of the column I identify within the
     * given <i>row</i>.
     */
    public Object evaluate(RowDecorator row) throws AxionException {
        if(null == row) { 
            throw new AxionException("Expected non-null RowDecorator here.");
        } else {
            return row.get(this);
        }
    }

    /**
     * Returns my column alias or name.
     */
    public String getLabel() {
        String result = getAlias();
        if (result == null) {
            result = getName();
        }
        return result;
    }

    /**
     * Sets the name of this column, 
     * and the name of my table if 
     * the given name includes "<code>.</code>".
     */
    private void setName(String column) {
        _column = toUpperOrNull(column);
        if(_column != null) {
           int pivot = _column.indexOf(".");
           if (pivot != -1) {
              setTableIdentifier(new TableIdentifier(_column.substring(0, pivot)));
              _column = _column.substring(pivot + 1);
           }
        }
    }

    /**
     * Returns the name of my column, if any.
     */
    public String getName() {
        return _column;
    }

    /**
     * Sets my column alias name, if any.
     */
    private void setAlias(String column) {
        _columnAlias = toUpperOrNull(column);
    }

    /**
     * Returns my column alias name, if any.
     */
    public String getAlias() {
        return _columnAlias;
    }

    /**
     * Sets my table identifier, if any.
     */
    public void setTableIdentifier(TableIdentifier table) {
        _table = table;
    }

    /**
     * Returns my table identifier, if any.
     */
    public TableIdentifier getTableIdentifier() {
        return _table;
    }

    /**
     * Returns the name of my table or null.
     * Unlike 
     * <code>{@link #getTableIdentifier getTableIdentifier()}.{@link TableIdentifier#getTableName getTableName()}</code>
     * this method will return <code>null</code> when I don't have a table identifier.
     */
    public String getTableName() {
        return (null == _table ? null : _table.getTableName());
    }

    /**
     * Returns the alias name of my table or null.
     * Unlike 
     * <code>{@link #getTableIdentifier getTableIdentifier()}.{@link TableIdentifier#getTableAlias getTableAlias()}</code>
     * this method will return <code>null</code> when I don't have a table identifier.
     */
    public String getTableAlias() {
        return (null == _table ? null : _table.getTableAlias());
    }

    /**
     * Returns my {@link DataType}, if any.
     */
    public DataType getDataType() {
        return _type;
    }

    /**
     * Sets my {@link DataType}, if any.
     */
    public void setDataType(DataType type) {
        _type = type;
    }

    /**
     * Returns <code>true</code> iff
     * <i>otherobject</i> is a {@link ColumnIdentifier}
     * whose name, table identifier, and alias are equal
     * to mine.
     */
    public boolean equals(Object otherobject) {
        if(otherobject instanceof ColumnIdentifier) {
            ColumnIdentifier that = (ColumnIdentifier)otherobject;
            return (
                (null == getName() ? null == that.getName() : getName().equals(that.getName())) &&
                (null == getTableIdentifier() ? null == that.getTableIdentifier() : getTableIdentifier().equals(that.getTableIdentifier())) &&
                (null == getAlias() ? null == that.getAlias() : getAlias().equals(that.getAlias()))
            );
        } else {
            return false;
        }
    }

    /**
     * Returns a hash code in keeping with the
     * standard {@link Object#equals equals}/{@link Object#hashCode hashCode}
     * contract.
     */
    public int hashCode() {
        int hashCode = 0;
        if(null != getName()) {
            hashCode ^= getName().hashCode();
        }
        if(null != getTableIdentifier()) {
            hashCode ^= getTableIdentifier().hashCode() << 4;
        }
        return hashCode;
    }

    /**
     * Returns a <code>String</code> representation of me,
     * suitable for debugging output.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        if(getTableIdentifier() != null) {
            result.append("(");
            result.append(getTableIdentifier().toString());
            result.append(").");
        }
        result.append(getName());
        return result.toString();
    }

    /** My {@link TableIdentifier}, if any. */
    private TableIdentifier _table = null;
    /** My column name, if any. */
    private String _column = null;
    /** My column alias, if any. */
    private String _columnAlias = null;
    /** My {@link DataType}, if any. */
    private DataType _type = null;
}
