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

package org.objectweb.cjdbc.scenario.tools.testlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;

/**
 * This class defines a MacroStatementTestLet
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class MacroStatementTestLet extends AbstractVdbTestLet
{
  static final String         UPDATE     = "update ADDRESS set FIRSTNAME=";

  /**
   * Creates a new <code>MacroStatementTestLet</code> object
   * 
   * @param vdb virtual database
   */
  public MacroStatementTestLet(VirtualDatabase vdb)
  {
    super(vdb);
    set(MACRO_NAME, "now()");
    set(USE_OPTIMIZED_STATEMENT, "true");
    set(USE_PREPARED_STATEMENT, "true");
    set(USE_UPDATE_STATEMENT, "true");
  }

  /**
   * @see org.objectweb.cjdbc.scenario.tools.testlet.AbstractTestLet#execute()
   */
  public void execute() throws Exception
  {
    boolean useOptimized = getConfigBoolean(USE_OPTIMIZED_STATEMENT);
    boolean usePrepared = getConfigBoolean(USE_PREPARED_STATEMENT);
    boolean useUpdate = getConfigBoolean(USE_UPDATE_STATEMENT);
    String statement = UPDATE + ((String) config.get(MACRO_NAME));

    Properties props = new Properties();
    if (useOptimized)
      props.put("driverProcessed", "false");
    Connection con = getCJDBCConnection(props);
    if (usePrepared)
    {
      PreparedStatement st = con.prepareStatement(statement);
      if (useUpdate)
        st.executeUpdate();
      else
        st.executeQuery();
    }
    else
    {
      Statement st = con.createStatement();
      if (useUpdate)
        st.executeUpdate(statement);
      else
        st.executeQuery(statement);
    }
  }

}