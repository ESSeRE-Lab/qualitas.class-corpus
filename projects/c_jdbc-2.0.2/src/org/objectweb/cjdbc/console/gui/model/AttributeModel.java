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

package org.objectweb.cjdbc.console.gui.model;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.swing.table.AbstractTableModel;

import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.jmx.RmiJmxClient;

/**
 * This class defines a AttributeModel
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class AttributeModel extends AbstractTableModel
{
  private MBeanAttributeInfo[] info;
  private RmiJmxClient         client;
  private ObjectName           mbean;

  /**
   * 
   * Creates a new <code>AttributeModel</code> object
   * 
   * @param info MBean attribute info
   * @param client JMX client
   * @param mbean MBean object
   */
  public AttributeModel(MBeanAttributeInfo[] info, RmiJmxClient client,
      ObjectName mbean)
  {
    this.info = info;
    this.client = client;
    this.mbean = mbean;
  }


  /**
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return 3;
  }

  /**
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount()
  {
    return info.length;
  }

  /**
   * Returns the info value.
   * 
   * @return Returns the info.
   */
  public MBeanAttributeInfo[] getInfo()
  {
    return info;
  }

  /**
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    if ((rowIndex >= 0 && rowIndex < info.length) == false)
      return null;
    switch (columnIndex)
    {
      case 0 :
        return info[rowIndex].getName();
      case 1 :
        return GuiConstants.getParameterType(info[rowIndex].getType());
      case 2 :
        try
        {
          return client.getAttributeValue(mbean, info[rowIndex].getName());
        }
        catch (Exception e)
        {
          return "-----";
        }
      default :
        return null;
    }
  }

  /**
   * @see javax.swing.table.TableModel#isCellEditable(int, int)
   */
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    if ((rowIndex >= 0 && rowIndex < info.length) == false)
      return false;
    if (rowIndex != 2)
      return false;
    else
      return info[rowIndex].isWritable();
  }

  /**
   * @see javax.swing.table.TableModel#getColumnName(int)
   */
  public String getColumnName(int column)
  {
    switch (column)
    {
      case 0 :
        return "Name";
      case 1 :
        return "Type";
      case 2 :
        return "Value";
      default :
        return null;
    }
  }
}