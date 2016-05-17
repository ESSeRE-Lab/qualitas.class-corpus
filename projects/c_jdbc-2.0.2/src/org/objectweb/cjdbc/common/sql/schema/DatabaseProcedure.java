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
 * Contributor(s): 
 */

package org.objectweb.cjdbc.common.sql.schema;

import java.sql.SQLException;
import java.util.ArrayList;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * Represents a procedure
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class DatabaseProcedure
{
  /** May return a result */
  public static final int ProcedureResultUnknown = 0;
  /** Does not return a result */
  public static final int ProcedureNoResult      = 1;
  /** Returns a result */
  public static final int ProcedureReturnsResult = 2;

  ArrayList               parameters;
  private String          name;
  private String          remarks;
  private int             procedureType;

  /**
   * Convert type from string to integer
   * 
   * @param type as a string
   * @return ProcedureNoResult or ProcedureReturnsResult or
   *         ProcedureResultUnknown if not found
   */
  public static int getTypeFromString(String type)
  {
    if (type.equals(DatabasesXmlTags.VAL_noResult))
      return ProcedureNoResult;
    if (type.equals(DatabasesXmlTags.VAL_returnsResult))
      return ProcedureReturnsResult;
    else
      return ProcedureResultUnknown;
  }

  /**
   * Convert type from integer to string
   * 
   * @param type as an int
   * @return string value conforms to xml tags.
   */
  public static String getTypeFromInt(int type)
  {
    switch (type)
    {
      case ProcedureNoResult :
        return DatabasesXmlTags.VAL_noResult;
      case ProcedureReturnsResult :
        return DatabasesXmlTags.VAL_returnsResult;
      default :
        return DatabasesXmlTags.VAL_resultUnknown;
    }
  }

  /**
   * @param name of the procedure
   * @param remarks of the procedure
   * @param procedureType see above types
   */
  public DatabaseProcedure(String name, String remarks, int procedureType)
  {
    this.name = name;
    this.remarks = remarks;
    this.procedureType = procedureType;
    this.parameters = new ArrayList();
  }

  /**
   * Add a parameter to this procedure
   * 
   * @param param to add
   */
  public void addParameter(DatabaseProcedureParameter param)
  {
    parameters.add(param);
  }

  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @return Returns the parameters.
   */
  public ArrayList getParameters()
  {
    return parameters;
  }

  /**
   * @param parameters The parameters to set.
   */
  public void setParameters(ArrayList parameters)
  {
    this.parameters = parameters;
  }

  /**
   * @return Returns the procedureType.
   */
  public int getProcedureType()
  {
    return procedureType;
  }

  /**
   * @param procedureType The procedureType to set.
   */
  public void setProcedureType(int procedureType)
  {
    this.procedureType = procedureType;
  }

  /**
   * @return Returns the remarks.
   */
  public String getRemarks()
  {
    return remarks;
  }

  /**
   * @param remarks The remarks to set.
   */
  public void setRemarks(String remarks)
  {
    this.remarks = remarks;
  }

  /**
   * Merges this procedure parameters with the given procedure's parameters. An
   * exception is thrown if the given procedure parameters conflict with this
   * one. If any parameter is different the exception is thrown.
   * 
   * @param procedure the procedure to merge
   * @throws SQLException if the schemas conflict
   */
  public void mergeParameters(DatabaseProcedure procedure) throws SQLException
  {
    if (procedure == null)
      return;

    ArrayList otherParameters = procedure.getParameters();
    if (otherParameters == null && parameters == null)
      return;

    if (this.equals(procedure))
    {
      // Procedures are the same, no conflict
      return;
    }
    else
    {
      throw new SQLException("Unable to merge procedure " + getName()
          + ": parameters are differents ");
    }
  }

  /**
   * Two <code>DatabaseProcedure</code> are considered equal if they have the
   * same name and the same parameters.
   * 
   * @param other the object to compare with
   * @return <code>true</code> if the DatabaseProcedures are equal
   */
  public boolean equals(Object other)
  {
    if ((other == null) || !(other instanceof DatabaseProcedure))
      return false;

    DatabaseProcedure p = (DatabaseProcedure) other;
    return (p.getName().equals(name)) && (p.getParameters().equals(parameters));
  }

  /**
   * Get xml information about this procedure.
   * 
   * @return xml formatted information on this database procedure.
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<" + DatabasesXmlTags.ELT_DatabaseProcedure + " "
        + DatabasesXmlTags.ATT_name + "=\"" + name + "\" "
        + DatabasesXmlTags.ATT_returnType + "=\""
        + getTypeFromInt(procedureType) + "\">");
    for (int i = 0; i < parameters.size(); i++)
      info.append(((DatabaseProcedureParameter) parameters.get(i)).getXml());
    info.append("</" + DatabasesXmlTags.ELT_DatabaseProcedure + ">");
    return info.toString();
  }

}
