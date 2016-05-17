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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.backend;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import org.objectweb.cjdbc.common.i18n.Translate;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.controller.connection.DriverManager;

/**
 * This class checks if a given driver provides the mandatory features necessary
 * for C-JDBC.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class DriverCompliance
{
  private boolean             isCompliant                 = false;
  private boolean             hasBeenTested               = false;
  private boolean             supportSetQueryTimeout      = false;
  private boolean             supportGetGeneratedKeys     = false;
  private boolean             supportGetColumnCount       = false;
  private boolean             supportGetColumnClassName   = false;
  private boolean             supportGetColumnTypeName    = false;
  private boolean             supportGetColumnType        = false;
  private boolean             supportGetColumnDisplaySize = false;
  private boolean             supportGetTableName         = false;
  private boolean             supportSetCursorName        = false;
  private boolean             supportSetFetchSize         = false;
  private boolean             supportSetMaxRows           = false;

  private Trace               logger;

  private static final int    TIMEOUT_VALUE               = 1000;
  private static final String DEFAULT_TEST_STATEMENT      = "select 1";
  private String              databaseProductName         = "C-JDBC";

  /**
   * Builds a new DriverCompliance object.
   * 
   * @param logger the logger to use
   */
  public DriverCompliance(Trace logger)
  {
    this.logger = logger;
  }

  /**
   * Check the driver compliance.
   * 
   * @param backendUrl the JDBC URL to connect to
   * @param login the user login
   * @param password the user password
   * @param driverPath path for driver
   * @param driverClassName class name for driver
   * @param connectionTestStatement SQL statement used to check if a connection
   *          is still valid
   * @return true if the driver is C-JDBC compliant
   * @throws ConnectException if it is not possible to connect to the backend
   */
  public boolean complianceTest(String backendUrl, String login,
      String password, String driverPath, String driverClassName,
      String connectionTestStatement) throws ConnectException
  {
    if (hasBeenTested)
      return isCompliant;

    isCompliant = false;

    Connection c = null;
    try
    {
      c = DriverManager.getConnection(backendUrl, login, password, driverPath,
          driverClassName);
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("backend.driver.test.connection.failed", e),
            e);
      else
        logger.error(Translate.get("backend.driver.test.connection.failed", e));
      throw new ConnectException(e.getMessage());
    }
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.test.connection.ok"));

    DatabaseMetaData connectionMetaData;
    try
    {
      connectionMetaData = c.getMetaData();
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
        logger
            .debug(Translate.get("backend.driver.test.metadata.failed", e), e);
      else
        logger.error(Translate.get("backend.driver.test.metadata.failed", e));
      return isCompliant;
    }
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.test.metadata.ok"));

    try
    {
      this.databaseProductName = connectionMetaData.getDatabaseProductName();
      logger.info(Translate
          .get("backend.detected.as", this.databaseProductName));
    }
    catch (Exception e)
    {
      logger.warn(Translate
          .get("backend.driver.test.database.productname.failed"));
    }

    Statement s;
    try
    {
      s = c.createStatement();
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("backend.driver.test.statement.failed", e),
            e);
      else
        logger.error(Translate.get("backend.driver.test.statement.failed", e));
      return isCompliant;
    }

    try
    {
      if (connectionTestStatement == null)
      {
        if (logger.isDebugEnabled())
          logger.debug(Translate.get("backend.driver.using.default.statement",
              DEFAULT_TEST_STATEMENT));
        connectionTestStatement = DEFAULT_TEST_STATEMENT;
      }
      s.execute(connectionTestStatement);
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("backend.driver.test.statement.invalid",
            new String[]{connectionTestStatement, e.getMessage()}), e);
      else
        logger.error(Translate.get("backend.driver.test.statement.invalid",
            new String[]{connectionTestStatement, e.getMessage()}));
      return isCompliant;
    }
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.test.statement.ok"));

    try
    {
      s.setCursorName("testcursor");
      supportSetCursorName = true;
      if (logger.isDebugEnabled())
        logger
            .debug(Translate.get("backend.driver.statement.setCursorName.ok"));
    }
    catch (Exception e1)
    {
      logger.warn(Translate
          .get("backend.driver.statement.setCursorName.failed"));
      supportSetMaxRows = false;
    }

    try
    {
      s.setFetchSize(25);
      supportSetFetchSize = true;
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("backend.driver.statement.setFetchSize.ok"));
    }
    catch (Exception e1)
    {
      logger
          .warn(Translate.get("backend.driver.statement.setFetchSize.failed"));
      supportSetMaxRows = false;
    }

    try
    {
      s.setMaxRows(5);
      supportSetMaxRows = true;
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("backend.driver.statement.setMaxRows.ok"));
    }
    catch (Exception e1)
    {
      logger.warn(Translate.get("backend.driver.statement.setMaxRows.failed"));
      supportSetMaxRows = false;
    }

    try
    {
      s.getGeneratedKeys();
      supportGetGeneratedKeys = true;
      if (logger.isDebugEnabled())
        logger.debug(Translate
            .get("backend.driver.statement.getGeneratedKeys.ok"));
    }
    catch (Exception e1)
    {
      logger.warn(Translate
          .get("backend.driver.statement.getGeneratedKeys.failed"));
      supportGetGeneratedKeys = false;
    }
    catch (AbstractMethodError e1)
    {
      logger.warn(Translate
          .get("backend.driver.statement.getGeneratedKeys.failed"));
      supportGetGeneratedKeys = false;
    }
    catch (java.lang.NoSuchMethodError e1)
    {
      logger.warn(Translate
          .get("backend.driver.statement.getGeneratedKeys.failed"));
      supportGetGeneratedKeys = false;
    }

    // Commented out:
    // A prepared statement can be sent to the DBMS right away to be compiled
    // Should fine a work around for this test.

    // PreparedStatement ps;
    // try
    // {
    // ps = c.prepareStatement("INSERT INTO versions VALUES (?,?)");
    // ps.setInt(1, 10);
    // ps.setString(2, "just a test");
    // }
    // catch (Exception e)
    // {
    // logger.warn(Translate.get("backend.driver.prepared.statement.failed"),
    // e);
    // }
    // if (logger.isDebugEnabled())
    // logger.debug(Translate.get("backend.driver.prepared.statement.ok"));

    try
    {
      s.setQueryTimeout(TIMEOUT_VALUE);
      supportSetQueryTimeout = true;
    }
    catch (Exception e)
    {
      logger.warn(Translate.get("backend.driver.setQueryTimeout.failed", e));
    }
    if (supportSetQueryTimeout && logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.setQueryTimeout.ok"));

    ResultSet rs;
    try
    {
      String[] types = {"TABLE", "VIEW"};
      rs = connectionMetaData.getTables(null, null, "%", types);
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("backend.driver.metadata.getTables.failed",
            e), e);
      else
        logger.error(Translate.get("backend.driver.metadata.getTables.failed",
            e));
      return isCompliant;
    }
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.metadata.getTables.ok"));

    java.sql.ResultSetMetaData rsMetaData;
    try
    {
      rsMetaData = rs.getMetaData();
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get(
            "backend.driver.resultset.getMetaData.failed", e), e);
      else
        logger.error(Translate.get(
            "backend.driver.resultset.getMetaData.failed", e));
      return isCompliant;
    }
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.resultset.getMetaData.ok"));

    try
    {
      if (rs.next() && (rsMetaData.getColumnCount() > 0))
      {
        rs.getObject(1);
      }
      else
        logger.warn(Translate.get("backend.driver.resultset.getObject.unable"));
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get("backend.driver.resultset.getObject.failed",
            e), e);
      else
        logger.error(Translate.get("backend.driver.resultset.getObject.failed",
            e));
      return isCompliant;
    }
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.resultset.getObject.ok"));

    try
    {
      rsMetaData.getColumnCount();
      supportGetColumnCount = true;
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get(
            "backend.driver.metadata.getColumnCount.failed", e), e);
      else
        logger.error(Translate.get(
            "backend.driver.metadata.getColumnCount.failed", e));
      return isCompliant;
    }
    if (supportGetColumnCount && logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.metadata.getColumnCount.ok"));

    try
    {
      rsMetaData.getColumnName(1);
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
        logger.debug(Translate.get(
            "backend.driver.metadata.getColumnName.failed", e), e);
      else
        logger.error(Translate.get(
            "backend.driver.metadata.getColumnName.failed", e));
      return isCompliant;
    }
    if (logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.metadata.getColumnName.ok"));

    try
    {
      rsMetaData.getTableName(1);
      supportGetTableName = true;
    }
    catch (Exception e)
    {
      logger.warn(Translate.get("backend.driver.metadata.getTableName.failed",
          e));
    }

    if (supportGetTableName && logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.metadata.getTableName.ok"));

    try
    {
      rsMetaData.getColumnDisplaySize(1);
      supportGetColumnDisplaySize = true;
    }
    catch (Exception e)
    {
      logger.warn(Translate.get(
          "backend.driver.metadata.getColumnDisplaySize.failed", e));
    }
    if (supportGetColumnDisplaySize && logger.isDebugEnabled())
      logger.debug(Translate
          .get("backend.driver.metadata.getColumnDisplaySize.ok"));

    try
    {
      rsMetaData.getColumnType(1);
      supportGetColumnType = true;
    }
    catch (Exception e)
    {
      logger.warn(Translate.get("backend.driver.metadata.getColumnType.failed",
          e));
    }
    if (supportGetColumnType && logger.isDebugEnabled())
      logger.debug(Translate.get("backend.driver.metadata.getColumnType.ok"));

    try
    {
      rsMetaData.getColumnTypeName(1);
      supportGetColumnTypeName = true;
    }
    catch (Exception e)
    {
      logger.warn(Translate.get(
          "backend.driver.metadata.getColumnTypeName.failed", e));
    }
    if (supportGetColumnTypeName && logger.isDebugEnabled())
      logger.debug(Translate
          .get("backend.driver.metadata.getColumnTypeName.ok"));

    try
    {
      rsMetaData.getColumnClassName(1);
      supportGetColumnClassName = true;
    }
    catch (Exception e)
    {
      logger.warn(Translate.get(
          "backend.driver.metadata.getColumnClassName.failed", e));
    }
    if (supportGetColumnClassName && logger.isDebugEnabled())
      logger.debug(Translate
          .get("backend.driver.metadata.getColumnClassName.ok"));

    isCompliant = true;
    hasBeenTested = true;
    return isCompliant;
  }

  /**
   * @return true if the driver is compliant to the C-JDBC requirements
   */
  public boolean isCompliant()
  {
    return isCompliant;
  }

  /**
   * @return true if the driver supports getGeneratedKeys
   */
  public boolean supportGetGeneratedKeys()
  {
    return supportGetGeneratedKeys;
  }

  /**
   * @return true if the driver supports getColumnClassName
   */
  public boolean supportGetColumnClassName()
  {
    return supportGetColumnClassName;
  }

  /**
   * @return true if the driver supports getColumnCount
   */
  public boolean supportGetColumnCount()
  {
    return supportGetColumnCount;
  }

  /**
   * @return true if the driver supports getColumnDisplaySize
   */
  public boolean supportGetColumnDisplaySize()
  {
    return supportGetColumnDisplaySize;
  }

  /**
   * @return true if the driver supports getColumnType
   */
  public boolean supportGetColumnType()
  {
    return supportGetColumnType;
  }

  /**
   * @return true if the driver supports getColumnTypeName
   */
  public boolean supportGetColumnTypeName()
  {
    return supportGetColumnTypeName;
  }

  /**
   * @return true if the driver supports getTableName
   */
  public boolean supportGetTableName()
  {
    return supportGetTableName;
  }

  /**
   * @return true if the driver supports setQueryTimeout
   */
  public boolean supportSetQueryTimeout()
  {
    return supportSetQueryTimeout;
  }

  /**
   * @return true if the driver supports Statement.setMaxRows
   */
  public boolean supportSetMaxRows()
  {
    return supportSetMaxRows;
  }

  /**
   * Returns the supportSetCursorName value.
   * 
   * @return Returns the supportSetCursorName.
   */
  public boolean supportSetCursorName()
  {
    return supportSetCursorName;
  }

  /**
   * Returns the supportSetFetchSize value.
   * 
   * @return Returns the supportSetFetchSize.
   */
  public boolean supportSetFetchSize()
  {
    return supportSetFetchSize;
  }

  /**
   * Returns the databaseProductName value.
   * 
   * @return Returns the databaseProductName.
   */
  public String getDatabaseProductName()
  {
    return this.databaseProductName;
  }
}