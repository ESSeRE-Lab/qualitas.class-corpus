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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.wizard.objects;

import java.util.ArrayList;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.console.wizard.WizardConstants;

/**
 * This class defines a ConnectionTypeInfo, that is all the connection manager
 * information.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ConnectionTypeInfo
{

  String            type     = WizardConstants.CONNECTION_MANAGERS[0];
  final Exception   badValue = new Exception("Bad Parameter");
  private ArrayList values   = new ArrayList();

  /**
   * Get the connection manager attributes.
   * 
   * @return list of attributes
   */
  public String[] getAttributes()
  {
    if (type == WizardConstants.CONNECTION_MANAGERS[0])
      return new String[]{};
    if (type == WizardConstants.CONNECTION_MANAGERS[1])
      return new String[]{WizardTranslate.get("label.poolSize")};
    if (type == WizardConstants.CONNECTION_MANAGERS[2])
      return new String[]{WizardTranslate.get("label.poolSize"),
          WizardTranslate.get("label.timeout")};
    if (type == WizardConstants.CONNECTION_MANAGERS[3])
      return new String[]{WizardTranslate.get("label.initPoolSize"),
          WizardTranslate.get("label.minPoolSize"),
          WizardTranslate.get("label.maxPoolSize"),
          WizardTranslate.get("label.idleTimeout"),
          WizardTranslate.get("label.waitTimeout")};
    else
      return null;
  }

  /**
   * Returns the values value.
   * 
   * @return Returns the values.
   */
  public ArrayList getValues()
  {
    return values;
  }

  /**
   * Get the index'th value.
   * 
   * @param index the value index to look for
   * @return the value
   */
  public int getValue(int index)
  {
    try
    {
      Object value = values.get(index);
      if (value instanceof String)
        return Integer.parseInt((String) value);
      else if (value instanceof Integer)
        return ((Integer) values.get(index)).intValue();
      else
        throw badValue;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * Sets the values value.
   * 
   * @param values The values to set.
   */
  public void setValues(ArrayList values)
  {
    this.values = values;
  }

  /**
   * Returns the type value.
   * 
   * @return Returns the type.
   */
  public String getType()
  {
    return type;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return type;
  }

  /**
   * Sets the type value.
   * 
   * @param type The type to set.
   */
  public void setType(String type)
  {
    this.type = type;
  }
}