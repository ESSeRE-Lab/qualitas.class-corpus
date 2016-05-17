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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): Marc Wick, Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.common.sql.schema;

import java.sql.Types;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * Represents a parameter of procedure
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class DatabaseProcedureParameter
{
  /** kind of column/parameter */

  /** nobody knows */
  public static final int ProcedureColumnUnknown   = 0;
  /** IN parameter */
  public static final int ProcedureColumnIn        = 1;
  /** INOUT parameter */
  public static final int ProcedureColumnInOut     = 2;
  /** OUT parameter */
  public static final int ProcedureColumnOut       = 3;
  /** procedure return value */
  public static final int ProcedureColumnReturn    = 4;
  /** procedure return value */
  public static final int ProcedureColumnResult    = 5;

  /** Can it contain NULL ? */
  /** does not allow NULL values */
  public static final int ProcedureNoNulls         = 0;
  /** allows NULL values */
  public static final int ProcedureNullable        = 1;
  /** nullability unknown */
  public static final int ProcedureNullableUnknown = 2;

  private String          name;
  private int             columnType;
  private int             dataType;
  private String          typeName;
  private float           precision;
  private int             length;
  private int             scale;
  private int             radix;
  private int             nullable;
  private String          remarks;

  /**
   * get null ability given a string
   * 
   * @param nullable recognized are "nonulls" and "nullable"
   * @return value given the java specification
   */
  public static int getNullFromString(String nullable)
  {
    if (nullable.equalsIgnoreCase(DatabasesXmlTags.VAL_noNulls))
      return ProcedureNoNulls;
    if (nullable.equalsIgnoreCase(DatabasesXmlTags.VAL_nullable))
      return ProcedureNullable;
    else
      return ProcedureNullableUnknown;
  }

  /**
   * get null ability given an int
   * 
   * @param nullable as an integer
   * @return a string conformed to dtd
   */
  public static String getNullFromInt(int nullable)
  {
    switch (nullable)
    {
      case ProcedureNoNulls :
        return DatabasesXmlTags.VAL_noNulls;
      case ProcedureNullable :
        return DatabasesXmlTags.VAL_nullable;
      case ProcedureNullableUnknown :
      default :
        return DatabasesXmlTags.VAL_nullableUnknown;
    }
  }

  /**
   * get column type given an int
   * 
   * @param type as an int from the java specification
   * @return a description as a string
   */
  public static String getColumnTypeFromInt(int type)
  {
    switch (type)
    {
      case ProcedureColumnIn :
        return DatabasesXmlTags.VAL_in;
      case ProcedureColumnOut :
        return DatabasesXmlTags.VAL_out;
      case ProcedureColumnInOut :
        return DatabasesXmlTags.VAL_inout;
      case ProcedureColumnReturn :
        return DatabasesXmlTags.VAL_return;
      case ProcedureColumnResult :
        return DatabasesXmlTags.VAL_result;
      case ProcedureColumnUnknown :
      default :
        return DatabasesXmlTags.VAL_unknown;
    }
  }

  /**
   * get type from string
   * 
   * @param type of the parameter
   * @return value given the java specification
   */
  public static int getColumnTypeFromString(String type)
  {
    if (type.equalsIgnoreCase(DatabasesXmlTags.VAL_in))
      return ProcedureColumnIn;
    if (type.equalsIgnoreCase(DatabasesXmlTags.VAL_out))
      return ProcedureColumnOut;
    if (type.equalsIgnoreCase(DatabasesXmlTags.VAL_inout))
      return ProcedureColumnInOut;
    if (type.equalsIgnoreCase(DatabasesXmlTags.VAL_return))
      return ProcedureColumnReturn;
    if (type.equalsIgnoreCase(DatabasesXmlTags.VAL_result))
      return ProcedureColumnResult;
    else
      return ProcedureColumnUnknown;
  }

  /**
   * Reduced version of constructor for static schemas
   * 
   * @param name column/parameter name
   * @param columnType kind of column/parameter
   * @param nullable can it contain NULL?
   */
  public DatabaseProcedureParameter(String name, int columnType, int nullable)
  {
    this(name, columnType, Types.VARCHAR, "VARCHAR", 0, 0, 0, 0, nullable, "");
  }

  /**
   * @param name column/parameter name
   * @param columnType kind of column/parameter
   * @param dataType SQL type from java.sql.Types
   * @param typeName SQL type name, for a UDT type the type name is fully
   *          qualified
   * @param precision precision
   * @param length length in bytes of data
   * @param scale scale
   * @param radix radix
   * @param nullable can it contain NULL?
   * @param remarks comment describing parameter/column
   */
  public DatabaseProcedureParameter(String name, int columnType, int dataType,
      String typeName, float precision, int length, int scale, int radix,
      int nullable, String remarks)
  {
    this.name = name;
    this.columnType = columnType;
    this.dataType = dataType;
    this.typeName = typeName;
    this.precision = precision;
    this.length = length;
    this.scale = scale;
    this.radix = radix;
    this.nullable = nullable;
    this.remarks = remarks;
  }

  /**
   * @return Returns the columnType.
   */
  public final int getColumnType()
  {
    return columnType;
  }

  /**
   * @param columnType The columnType to set.
   */
  public final void setColumnType(int columnType)
  {
    this.columnType = columnType;
  }

  /**
   * @return Returns the dataType.
   */
  public final int getDataType()
  {
    return dataType;
  }

  /**
   * @param dataType The dataType to set.
   */
  public final void setDataType(int dataType)
  {
    this.dataType = dataType;
  }

  /**
   * @return Returns the length.
   */
  public final int getLength()
  {
    return length;
  }

  /**
   * @param length The length to set.
   */
  public final void setLength(int length)
  {
    this.length = length;
  }

  /**
   * @return Returns the name.
   */
  public final String getName()
  {
    return name;
  }

  /**
   * @param name The name to set.
   */
  public final void setName(String name)
  {
    this.name = name;
  }

  /**
   * @return Returns the nullable.
   */
  public final int getNullable()
  {
    return nullable;
  }

  /**
   * @param nullable The nullable to set.
   */
  public final void setNullable(int nullable)
  {
    this.nullable = nullable;
  }

  /**
   * @return Returns the precision.
   */
  public final float getPrecision()
  {
    return precision;
  }

  /**
   * @param precision The precision to set.
   */
  public final void setPrecision(int precision)
  {
    this.precision = precision;
  }

  /**
   * @return Returns the radix.
   */
  public final int getRadix()
  {
    return radix;
  }

  /**
   * @param radix The radix to set.
   */
  public final void setRadix(int radix)
  {
    this.radix = radix;
  }

  /**
   * @return Returns the remarks.
   */
  public final String getRemarks()
  {
    return remarks;
  }

  /**
   * @param remarks The remarks to set.
   */
  public final void setRemarks(String remarks)
  {
    this.remarks = remarks;
  }

  /**
   * @return Returns the scale.
   */
  public final int getScale()
  {
    return scale;
  }

  /**
   * @param scale The scale to set.
   */
  public final void setScale(int scale)
  {
    this.scale = scale;
  }

  /**
   * @return Returns the typeName.
   */
  public final String getTypeName()
  {
    return typeName;
  }

  /**
   * @param typeName The typeName to set.
   */
  public final void setTypeName(String typeName)
  {
    this.typeName = typeName;
  }

  /**
   * Two <code>DatabaseProcedureParameter</code> are considered equal if they
   * have the same name and the same descriptive attributes.
   * 
   * @param other the object to compare with
   * @return <code>true</code> if the DatabaseProcedureParameter are equal
   */
  public boolean equals(Object other)
  {
    if ((other == null) || !(other instanceof DatabaseProcedureParameter))
      return false;

    DatabaseProcedureParameter p = (DatabaseProcedureParameter) other;

    // first we check simple types
    if (!(p.columnType == columnType && p.dataType == dataType
        && p.precision == precision && p.length == length && p.scale == scale
        && p.radix == radix && p.nullable == nullable))
    {
      return false;
    }

    // now we compare object types
    if (!(name == null ? p.name == null : name.equals(p.name)))
    {
      return false;
    }

    if (!(typeName == null ? p.typeName == null : typeName.equals(p.typeName)))
    {
      return false;
    }

    return remarks == null ? p.remarks == null : remarks.equals(p.remarks);
  }

  /**
   * Get xml information about this procedure.
   * 
   * @return xml formatted information on this database procedure.
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_DatabaseProcedureColumn + " "
        + DatabasesXmlTags.ATT_name + "=\"" + name + "\"" + " "
        + DatabasesXmlTags.ATT_paramType + "=\""
        + getColumnTypeFromInt(columnType) + "\"" + " "
        + DatabasesXmlTags.ATT_nullable + "=\"" + getNullFromInt(nullable)
        + "\"/>");
    return info.toString();
  }

}