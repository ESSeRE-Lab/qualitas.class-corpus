/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): 
 */

package org.objectweb.cjdbc.controller.backend;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * Mapping for dynamic schema gathering and validation
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public abstract class DatabaseBackendSchemaConstants
{
  /** Static level no dynamic schema */
  public static final int DynamicPrecisionStatic     = 0;
  /** Table level for dynamic schema */
  public static final int DynamicPrecisionTable      = 1;
  /** Column level for dynamic schema */
  public static final int DynamicPrecisionColumn     = 2;
  /** procedures names level for dynamic schema */
  public static final int DynamicPrecisionProcedures = 3;
  /** All level for dynamic schema, procedures parameters are retrieved */
  public static final int DynamicPrecisionAll        = 4;

  /**
   * Get the dynamic schema level from string to int
   * 
   * @param stringLevel as a string from <code>DatabaseXmlTags</code>
   * @return an int
   */
  public static int getDynamicSchemaLevel(String stringLevel)
  {
    if (stringLevel.equalsIgnoreCase(DatabasesXmlTags.VAL_static))
      return DynamicPrecisionStatic;
    else if (stringLevel.equalsIgnoreCase(DatabasesXmlTags.VAL_table))
      return DynamicPrecisionTable;
    else if (stringLevel.equalsIgnoreCase(DatabasesXmlTags.VAL_column))
      return DynamicPrecisionColumn;
    else if (stringLevel.equalsIgnoreCase(DatabasesXmlTags.VAL_procedures))
      return DynamicPrecisionProcedures;
    else if (stringLevel.equalsIgnoreCase(DatabasesXmlTags.VAL_all))
      return DynamicPrecisionAll;
    else
      throw new IllegalArgumentException("Invalid dynamic precision "
          + stringLevel);
  }

  /**
   * Get the dynamic schema level from int to string
   * 
   * @param intLevel as an int
   * @return string taken from <code>DatabaseXmlTags</code>
   */
  public static String getDynamicSchemaLevel(int intLevel)
  {
    switch (intLevel)
    {
      case DynamicPrecisionStatic :
        return DatabasesXmlTags.VAL_static;
      case DynamicPrecisionTable :
        return DatabasesXmlTags.VAL_table;
      case DynamicPrecisionColumn :
        return DatabasesXmlTags.VAL_column;
      case DynamicPrecisionProcedures :
        return DatabasesXmlTags.VAL_procedures;
      case DynamicPrecisionAll :
        return DatabasesXmlTags.VAL_all;
      default :
        return DatabasesXmlTags.VAL_all;
    }
  }
}