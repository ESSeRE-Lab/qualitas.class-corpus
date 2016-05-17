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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.console.views;

import org.objectweb.cjdbc.common.i18n.Translate;

/**
 * Graphical statistics viewer. Quick and dirty implementation.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier</a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class SchedulerViewer extends InfoViewer
{

  /**
   * Create a CacheStatsViewer
   * 
   * @param data Stats to display in the table
   */
  public SchedulerViewer(Object[][] data)
  {
    super(data);
  }

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
      for (int j = 1; j < jSize; j++)
      {
          ret[i][j] = new Integer(aStat[j]);
      }
    }
    return ret;
  }

  /**
   * @see InfoViewer#getColumnNames()
   */
  public String[] getColumnNames()
  {
    String[] columnNames = new String[7];
    columnNames[0] = Translate.get("console.infoviewer.scheduler.column.0");
    columnNames[1] = Translate.get("console.infoviewer.scheduler.column.1");
    columnNames[2] = Translate.get("console.infoviewer.scheduler.column.2");
    columnNames[3] = Translate.get("console.infoviewer.scheduler.column.3");
    columnNames[4] = Translate.get("console.infoviewer.scheduler.column.4");
    columnNames[5] = Translate.get("console.infoviewer.scheduler.column.5");
    columnNames[6] = Translate.get("console.infoviewer.scheduler.column.6");
    return columnNames;
  }

  /**
   * @see InfoViewer#setLabels()
   */
  public void setLabels()
  {
    frameTitle = Translate.get("console.infoviewer.scheduler.frame.title");
    infoViewerMenuBarString = Translate
        .get("console.infoviewer.scheduler.menubar");
    actionToolTipText = Translate
        .get("console.infoviewer.scheduler.action.tooltiptext");
    actionErrorMessage = Translate
        .get("console.infoviewer.scheduler.action.error.message");
    actionSuccessMessage = Translate
        .get("console.infoviewer.scheduler.action.success.message");
    tableHeaderToolTipText = Translate
        .get("console.infoviewer.table.tooltip.text");
  }

  /**
   * @see org.objectweb.cjdbc.console.views.InfoViewer#getTraceableColumns()
   */
  public int[] getTraceableColumns()
  {
    return new int[]{2,3,4};
  }

}
