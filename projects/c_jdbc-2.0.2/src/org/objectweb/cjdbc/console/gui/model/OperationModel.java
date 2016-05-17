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

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.swing.table.AbstractTableModel;

import org.objectweb.cjdbc.console.gui.constants.GuiConstants;

/**
 * This class defines a OperationModel
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class OperationModel extends AbstractTableModel
{
  private MBeanOperationInfo[] info;

  /**
   * Creates a new <code>OperationModel</code> object
   * 
   * @param info MBean operation info
   */
  public OperationModel(MBeanOperationInfo[] info)
  {
    this.info = info;
  }

  /**
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return 2;
  }

  /**
   * Returns the info value.
   * 
   * @return Returns the info.
   */
  public MBeanOperationInfo[] getInfo()
  {
    return info;
  }

  /**
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount()
  {
    return info.length;
  }

  /**
   * @see javax.swing.table.TableModel#isCellEditable(int, int)
   */
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return false;
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
        StringBuffer name = new StringBuffer(info[rowIndex].getName());
        MBeanParameterInfo[] param = info[rowIndex].getSignature();
        name.append("(");
        for (int i = 0; i < param.length; i++)
        {
          if (i != 0)
            name.append(",");
          name.append(GuiConstants.getParameterType(param[i].getType()));
        }
        name.append(")");
        return name.toString();
      case 1 :
        return GuiConstants.getParameterType(info[rowIndex].getReturnType());
      default :
        return null;
    }
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
        return "Return";
      default :
        return null;
    }
  }
}