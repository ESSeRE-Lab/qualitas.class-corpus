/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: c-jdbc@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Julie Marguerite.
 */

package org.objectweb.cjdbc.common.sql.schema;

import java.io.Serializable;
import java.sql.Types;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * A <code>DatabaseColumn</code> represents a column of a database table. It
 * is composed of a name, type (not used yet) and a boolean indicated whether or
 * not rows are unique or not (like primary keys or columns created explicitely
 * with the <code>UNIQUE</code> keyword).
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @author <a href="mailto:Julie.Marguerite@inria.fr">Julie Marguerite</a>
 * @version 1.0
 */
public class DatabaseColumn implements Serializable
{
  private static final long serialVersionUID = 1118853825798791836L;

  /** Column name. */
  private String            name;

  /**
   * <code>true</code> if this column has a <code>UNIQUE</code> constraint
   * (like primary keys for example).
   */
  private boolean           isUnique;

  /** Type of the column (<code>VARCHAR</code>,<code>TEXT</code>, ...). */
  private int               type;

  /**
   * Creates a new <code>DatabaseColumn</code> instance.
   * 
   * @param name name of the column
   * @param isUnique <code>true</code> if this column has a
   *          <code>UNIQUE</code> constraint
   */
  public DatabaseColumn(String name, boolean isUnique)
  {
    this(name, isUnique, Types.NULL);
  }

  /**
   * Creates a new <code>DatabaseColumn</code> instance.
   * 
   * @param name name of the column
   * @param isUnique <code>true</code> if this column has a
   *          <code>UNIQUE</code> constraint
   * @param type type of the column (<code>VARCHAR</code>,<code>TEXT</code>,
   *          ...)
   */
  public DatabaseColumn(String name, boolean isUnique, int type)
  {
    if (name == null)
      throw new IllegalArgumentException(
          "Illegal null column name in DatabaseColumn constructor");

    this.name = name;
    this.isUnique = isUnique;
    this.type = type;
  }

  /**
   * Gets the column name.
   * 
   * @return a <code>String</code> value.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Tests if the column has a <code>UNIQUE</code> constraint (like primary
   * keys for example).
   * 
   * @return <code>true</code> if the column has a <code>UNIQUE</code>
   *         constraint
   */
  public boolean isUnique()
  {
    return isUnique;
  }

  /**
   * Sets the value of {@link #isUnique}.
   * 
   * @param bool <code>true</code> if the column has a <code>UNIQUE</code>
   *          constraint (like primary keys for example).
   */
  public void setIsUnique(boolean bool)
  {
    isUnique = bool;
  }

  /**
   * Returns the column type according to <code>java.sql.Types</code>.
   * 
   * @return the column type. Returns <code>Types.NULL</code> if the type is
   *         not set.
   * @see java.sql.Types
   */
  public int getType()
  {
    return type;
  }

  /**
   * Two <code>DatabaseColumn</code> are considered equal if they have the
   * same name and type and if they are both unique or both non unique.
   * 
   * @param other the object to compare with
   * @return <code>true</code> if the columns are equal
   */
  public boolean equals(Object other)
  {
    if ((other == null) || !(other instanceof DatabaseColumn))
      return false;

    DatabaseColumn c = (DatabaseColumn) other;
    return (isUnique == c.isUnique()) && name.equals(c.getName())
        && (type == c.getType());
  }

  /**
   * This function is the same as equal but ignores the column type.
   * 
   * @param other the object to compare with
   * @return true if the columns are equal ignoring their type.
   * @see #equals(Object)
   */
  public boolean equalsIgnoreType(Object other)
  {
    if ((other == null) || !(other instanceof DatabaseColumn))
      return false;

    DatabaseColumn c = (DatabaseColumn) other;
    return (isUnique == c.isUnique()) && name.equals(c.getName());
  }

  /**
   * Get xml information about this column.
   * 
   * @return xml formatted information on this database column.
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_DatabaseColumn + " "
        + DatabasesXmlTags.ATT_columnName + "=\"" + name + "\" "
        + DatabasesXmlTags.ATT_isUnique + "=\"" + isUnique + "\">");
    info.append("</" + DatabasesXmlTags.ELT_DatabaseColumn + ">");
    return info.toString();
  }
}
