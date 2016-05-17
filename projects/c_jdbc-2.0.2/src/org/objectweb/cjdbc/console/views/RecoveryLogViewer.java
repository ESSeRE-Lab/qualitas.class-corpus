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

package org.objectweb.cjdbc.console.views;

import org.objectweb.cjdbc.common.i18n.Translate;

/**
 * This class defines a RecoveryLogViewer
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class RecoveryLogViewer extends InfoViewer
{

  /**
   * Creates a new <code>RecoveryLogViewer</code> object
   * 
   * @param data the content of the recovery log
   */
  public RecoveryLogViewer(Object[][] data)
  {
    super(data);
  }

  /**
   * @see org.objectweb.cjdbc.console.views.InfoViewer#getDataTypes(java.lang.Object[][])
   */
  protected Object[][] getDataTypes(Object[][] stats)
  {
    int iSize = stats.length;
    Object[][] ret = new Object[iSize][];
    for (int i = 0; i < iSize; i++)
    {
      String[] aStat = (String[]) stats[i];
      int jSize = aStat.length;
      ret[i] = new Object[jSize];
      ret[i][0] = aStat[0];
      for (int j = 0; j < jSize; j++)
      {
        if (j == 2 || j == 3)
          ret[i][j] = new Long(aStat[j]);
        else
          ret[i][j] = new String(aStat[j]);
      }
    }
    return ret;
  }

  /**
   * @see org.objectweb.cjdbc.console.views.InfoViewer#getColumnNames()
   */
  public String[] getColumnNames()
  {
    String[] columnNames = new String[4];
    columnNames[0] = Translate.get("console.infoviewer.recoverylog.column.0");
    columnNames[1] = Translate.get("console.infoviewer.recoverylog.column.1");
    columnNames[2] = Translate.get("console.infoviewer.recoverylog.column.2");
    columnNames[3] = Translate.get("console.infoviewer.recoverylog.column.3");
    return columnNames;
  }

  /**
   * @see org.objectweb.cjdbc.console.views.InfoViewer#setLabels()
   */
  public void setLabels()
  {
    frameTitle = Translate.get("console.infoviewer.recoverylog.frame.title");
    infoViewerMenuBarString = Translate
        .get("console.infoviewer.recoverylog.menubar");
    actionToolTipText = Translate
        .get("console.infoviewer.recoverylog.action.tooltiptext");
    actionErrorMessage = Translate
        .get("console.infoviewer.recoverylog.action.error.message");
    actionSuccessMessage = Translate
        .get("console.infoviewer.recoverylog.action.success.message");
    tableHeaderToolTipText = Translate
        .get("console.infoviewer.table.tooltip.text");
  }

}
