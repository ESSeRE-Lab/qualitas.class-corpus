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

package org.objectweb.cjdbc.scenario.templates;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.controller.core.Controller;
import org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabase;
import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;
import org.objectweb.cjdbc.scenario.tools.components.backend.DatabaseManager;
import org.objectweb.cjdbc.scenario.tools.components.controller.ControllerManager;

/**
 * This class defines a SQLInjectionTemplate
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class SQLInjectionTemplate extends Template
{
  protected ControllerManager    cm             = new ControllerManager();
  protected DatabaseManager      hm             = new DatabaseManager();
  protected ComponentInterface   hm1            = null, hm2 = null, hm3 = null;
  protected Controller           controller     = null;
  protected VirtualDatabase      mainVdb;
  static final String            standAlone     = System
                                                    .getProperty("standalone");
  protected static final boolean standaloneTest = !(Boolean.valueOf(standAlone)
                                                    .booleanValue());

  static String[] tokenize(String param)
  {
    if (standaloneTest)
      return null;

    int counter = 0;
    StringTokenizer tokenizer = new StringTokenizer(param, ",");
    String[] params = new String[tokenizer.countTokens()];
    while (tokenizer.hasMoreTokens())
    {
      String pa = tokenizer.nextToken();
      params[counter] = (pa.equals("NULL")) ? "" : pa;
      counter++;
    }
    return params;
  }

  // Get Bundle
  protected static final ResourceBundle bundle                   = (standaloneTest)
                                                                     ? null
                                                                     : ResourceBundle
                                                                         .getBundle("sqlinjection");

  // jdbc urls
  protected static final String         jdbcurl                  = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("jdbc.url");
  protected static final String[]       urls                     = tokenize(jdbcurl);
  // jdbc users
  protected static final String         jdbcuser                 = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("jdbc.user");
  protected static final String[]       users                    = tokenize(jdbcuser);
  // jdbc passwords
  protected static final String         jdbcpassword             = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("jdbc.password");
  protected static final String[]       passwords                = tokenize((jdbcpassword));
  // jdbc classes
  protected static final String         jdbcdriverclass          = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("jdbc.driver.class");
  protected static final String[]       classes                  = tokenize(jdbcdriverclass);

  // cjdbc info
  protected static final String         cjdbcurl                 = (standaloneTest)
                                                                     ? "jdbc:cjdbc://localhost:25322/myDB"
                                                                     : bundle
                                                                         .getString("cjdbc.url");
  protected static final String         cjdbcpassword            = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("cjdbc.password");
  protected static final String         cjdbcuser                = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("cjdbc.user");

  // sql injection info
  protected static final String         threadcount              = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("thread.count");
  protected static final String         threadstartwaittimerange = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("thread.start.wait.time.range");
  protected static final String         unitruncount             = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("unit.run.count");
  protected static final String         threadwaittime           = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("thread.wait.time");
  protected static final String         jointhreadtimeout        = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("join.thread.timeout");

  protected static final String         dropTables               = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("drop.tables");

  protected static final String         createTables             = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("create.tables");

  protected static final String         keyIndex                 = (standaloneTest)
                                                                     ? null
                                                                     : bundle
                                                                         .getString("key.index");

  /**
   * @see org.objectweb.cjdbc.scenario.templates.Template#getCJDBCConnection()
   */
  public Connection getConnection() throws Exception
  {
    if (standaloneTest)
      return super.getCJDBCConnection();
    else
      return DriverManager.getConnection(cjdbcurl, cjdbcuser, cjdbcpassword);
  }

  /**
   * @see org.objectweb.cjdbc.scenario.templates.Template#getHypersonicConnection(int)
   */
  public Connection getBackendConnection(int index) throws Exception
  {
    if (standaloneTest)
      return super.getHypersonicConnection(index);
    else
    {
      Class.forName(classes[index]);
      {
        System.out.println("Connecting to:"+urls[index] +";"+users[index]+";"+passwords[index]);
      return DriverManager.getConnection(urls[index], users[index],
          passwords[index]);
      }
    }
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    if (!standaloneTest)
      return;
    try
    {
      hm1 = hm.start("9001");
      hm1.loadDatabase("database-raidb1-user.template");
      hm2 = hm.start("9002");
      hm2.loadDatabase("database-raidb1-user.template");
      hm3 = hm.start("9003");
      hm.loaddatabase("9003");
      controller = (Controller) cm.start("25322").getProcess();
      cm.loaddatabase("" + controller.getPortNumber(),
          "hsqldb-raidb1-variablepool-waitforall.xml");
      mainVdb = controller.getVirtualDatabase("myDB");
      mainVdb.enableAllBackends();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fail("Could not start controller");
      tearDown();
    }
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown()
  {
    if (!standaloneTest)
      return;

    hm.stopAll();
    cm.stopAll();
  }

}