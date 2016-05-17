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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.text.commands.dbadmin;

import java.util.ArrayList;

import org.objectweb.cjdbc.common.i18n.ConsoleTranslate;
import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.jmx.mbeans.VirtualDatabaseMBean;
import org.objectweb.cjdbc.common.monitor.backend.BackendStatistics;
import org.objectweb.cjdbc.console.text.ColorPrinter;
import org.objectweb.cjdbc.console.text.formatter.TableFormatter;
import org.objectweb.cjdbc.console.text.module.VirtualDatabaseAdmin;

/**
 * This class defines a ShowBackend
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ShowBackend extends AbstractAdminCommand
{

  /**
   * Creates a new <code>ShowBackend.java</code> object
   * 
   * @param module the commands is attached to
   */
  public ShowBackend(VirtualDatabaseAdmin module)
  {
    super(module);
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#parse(java.lang.String)
   */
  public void parse(String commandText) throws Exception
  {
    if (commandText.trim().length() == 0) 
    {
      console.printError(getUsage());
      return;
    }
    
    VirtualDatabaseMBean db = jmxClient.getVirtualDatabaseProxy(dbName, user,
        password);
    String[] backendNames;
    if (("*").equals(commandText.trim()))
    {
      ArrayList backendNamesList = db.getAllBackendNames();
      backendNames = (String[]) backendNamesList
          .toArray(new String[backendNamesList.size()]);
    }
    else
    {
      String backendName = commandText.trim();
      backendNames = new String[]{backendName};
    }

    ArrayList stats = new ArrayList();
    for (int i = 0; i < backendNames.length; i++)
    {
      String backendName = backendNames[i];
      BackendStatistics stat = db.getBackendStatistics(backendName);
      if (stat == null)
      {
        continue;
      }
      stats.add(stat);
    }
    if (stats.size() == 0) 
    {
      console.println(ConsoleTranslate.get("admin.command.show.backend.no.stats", commandText));
      return;
    }
    String formattedBackends = TableFormatter.format(
        getBackendStatisticsDescriptions(),
        getBackendStatisticsAsStrings(stats), false);
    console.println(formattedBackends, ColorPrinter.STATUS);
  }

  private String[][] getBackendStatisticsAsStrings(ArrayList stats)
  {
    String[][] statsStr = new String[stats.size()][14];
    for (int i = 0; i < statsStr.length; i++)
    {
      BackendStatistics stat = (BackendStatistics)stats.get(i);
      statsStr[i][0] = stat.getBackendName();
      statsStr[i][1] = stat.getDriverClassName();
      statsStr[i][2] = stat.getUrl();
      statsStr[i][3] = Integer.toString(stat.getNumberOfActiveTransactions());
      statsStr[i][4] = Integer.toString(stat.getNumberOfPendingRequests());
      statsStr[i][5] = Boolean.toString(stat.isReadEnabled());
      statsStr[i][6] = Boolean.toString(stat.isWriteEnabled());
      statsStr[i][7] = stat.getInitializationStatus();
      statsStr[i][8] = Boolean.toString(stat.isSchemaStatic());
      statsStr[i][9] = Integer.toString(stat.getNumberOfConnectionManagers());
      statsStr[i][10] = Long.toString(stat.getNumberOfTotalActiveConnections());
      statsStr[i][11] = Integer.toString(stat.getNumberOfTotalRequests());
      statsStr[i][12] = Integer.toString(stat.getNumberOfTotalTransactions());
      statsStr[i][13] = stat.getLastKnownCheckpoint();
    }
    return statsStr;
  }

  private String[] getBackendStatisticsDescriptions()
  {
    String[] descriptions = new String[14];
    descriptions[0] = Translate.get("console.infoviewer.backend.name");
    descriptions[1] = Translate.get("console.infoviewer.backend.driver");
    descriptions[2] = Translate.get("console.infoviewer.backend.url");
    descriptions[3] = Translate
        .get("console.infoviewer.backend.active.transactions");
    descriptions[4] = Translate
        .get("console.infoviewer.backend.pending.requests");
    descriptions[5] = Translate.get("console.infoviewer.backend.read.enabled");
    descriptions[6] = Translate.get("console.infoviewer.backend.write.enabled");
    descriptions[7] = Translate.get("console.infoviewer.backend.init.status");
    descriptions[8] = Translate.get("console.infoviewer.backend.static.schema");
    descriptions[9] = Translate
        .get("console.infoviewer.backend.connection.managers");
    descriptions[10] = Translate
        .get("console.infoviewer.backend.total.active.connections");
    descriptions[11] = Translate
        .get("console.infoviewer.backend.total.requests");
    descriptions[12] = Translate
        .get("console.infoviewer.backend.total.transactions");
    descriptions[13] = Translate
        .get("console.infoviewer.backend.lastknown.checkpoint");
    return descriptions;
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandName()
   */
  public String getCommandName()
  {

    return "show backend";
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandParameters()
   */
  public String getCommandParameters()
  {
    return ConsoleTranslate.get("admin.command.show.backend.params");
  }

  /**
   * @see org.objectweb.cjdbc.console.text.commands.ConsoleCommand#getCommandDescription()
   */
  public String getCommandDescription()
  {
    return ConsoleTranslate.get("admin.command.show.backend.description");
  }

}
