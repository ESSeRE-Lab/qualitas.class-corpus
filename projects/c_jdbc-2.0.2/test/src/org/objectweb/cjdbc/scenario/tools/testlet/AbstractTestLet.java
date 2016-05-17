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

package org.objectweb.cjdbc.scenario.tools.testlet;

import java.util.Hashtable;

import junit.framework.TestCase;

/**
 * This class defines a AbstractTestLet. A testlet is meant to be a portion of
 * testing. This should be included in a scenario based on a template.
 * <ul>
 * <li><tt>Template</tt>: Starts the test configuration, including
 * controller, virtual database and backends.</li>
 * <li><tt>TestLet</tt>: the stand alone part of the test. It needs a
 * connection and test parameters</li>
 * <li><tt>Scenario</tt>: it coordinates the template with the testlet.
 * </ul>
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class AbstractTestLet extends TestCase
{
  /** <tt>TABLE_NAME</tt> configuration parameter */
  public static final String TABLE_NAME              = "TABLE_NAME";
  /** <tt>COLUMN_NAME</tt> configuration parameter */
  public static final String COLUMN_NAME             = "COLUMN_NAME";
  /** <tt>UPDATED_COLUMN_VALUE</tt> configuration parameter */
  public static final String UPDATED_COLUMN_VALUE    = "UPDATED_COLUMN_VALUE";
  /** <tt>SELECTED_COLUMNS</tt> configuration parameter */
  public static final String SELECTED_COLUMNS        = "SELECTED_COLUMNS";
  /** <tt>USE_PREPARED_STATEMENT</tt> configuration parameter */
  public static final String USE_PREPARED_STATEMENT  = "USE_PREPARED_STATEMENT";
  /** <tt>VIRTUAL_DATABASE</tt> configuration parameter */
  public static final String VIRTUAL_DATABASE        = "VIRTUAL_DATABASE";
  /** <tt>IGNORE_CASE</tt> configuration parameter */
  public static final String IGNORE_CASE             = "IGNORE_CASE";
  /** <tt>TABLE_METADATA_COLUMNS</tt> configuration parameter */
  public static final String TABLE_METADATA_COLUMNS  = "TABLE_METADATA_COLUMNS";
  /** <tt>USE_TRANSACTIONS</tt> configuration parameter */
  public static final String USE_TRANSACTIONS        = "USE_TRANSACTIONS";
  /** <tt>USE_CJDBC_CLASS</tt> configuration parameter */
  public static final String USE_CJDBC_CLASS         = "USE_CJDBC_CLASS";
  /** <tt>FILE_NAME</tt> configuration parameter */
  public static final String FILE_NAME               = "FILE_NAME";
  /** <tt>LIST_FAILOVER_BACKENDS</tt> configuration parameter */
  public static final String LIST_FAILOVER_BACKENDS  = "LIST_FAILOVER_BACKENDS";
  /** <tt>ITERATION</tt> configuration parameter */
  public static final String ITERATION               = "ITERATION";
  /** <tt>PROCEDURE_NAME</tt> configuration parameter */
  public static final String PROCEDURE_NAME          = "PROCEDURE";
  /** <tt>USE_UPDATE_STATEMENT</tt> configuration parameter */
  public static final String USE_UPDATE_STATEMENT    = "USE_UPDATE_STATEMENT";
  /** <tt>NUMBER_OF_UPDATES</tt> configuration parameter */
  public static final String NUMBER_OF_UPDATES       = "NUMBER_OF_UPDATES";
  /** <tt>USE_OPTIMIZED_STATEMENT</tt> configuration parameter */
  public static final String USE_OPTIMIZED_STATEMENT = "USE_OPTIMIZED_STATEMENT";
  /** <tt>MACRO_NAME</tt> configuration parameter */
  public static final String MACRO_NAME              = "MACRO_NAME";

  protected Hashtable        config;
  private long               initialMemoryUsage;
  private long               initialTime;

  /**
   * Creates a new <code>AbstractTestLet</code> object
   */
  public AbstractTestLet()
  {
    config = new Hashtable();
    System.gc();
    initialMemoryUsage = checkMemoryUsage();
    initialTime = System.currentTimeMillis();
  }

  /**
   * Execute the content of the test. This method can call JUnit's assert
   * methods and can therefore validate or invalidate the whole test.
   * 
   * @throws Exception if fails
   */
  public abstract void execute() throws Exception;

  /**
   * Collect the current memory usage
   * 
   * @return a value in Kilobytes
   */
  public long checkMemoryUsage()
  {
    long total = Runtime.getRuntime().totalMemory();
    long before = Runtime.getRuntime().freeMemory();
    return (total - before) / 1024;
  }

  /**
   * This calls the <code>execute</code> method multiple times. The batch
   * method is limited to change values on a single entry of the configuration
   * of this testlet.
   * 
   * @param batchCategory the configuration value description to change
   * @param batchValues the different value the parameter will take
   * @throws Exception if any fail
   */
  public void executeBatch(String batchCategory, Object[] batchValues)
      throws Exception
  {
    for (int i = 0; i < batchValues.length; i++)
    {
      set(batchCategory, batchValues[i]);
      execute();
    }
  }

  /**
   * Execute the same let with the same parameters a couple of times
   * 
   * @param numberOfTimes the number of times to repeat the test
   */
  public void executeBatch(int numberOfTimes) throws Exception
  {
    for (int i = 0; i < numberOfTimes; i++)
      execute();
  }

  /**
   * Configure the test with new values. The values are specific to each test
   * let. If this method is not called before <code>execute</code>, the test
   * will use predefined test properties
   * 
   * @param properties set of properties to use for this test
   * @throws Exception if fails
   */
  public void configure(Hashtable properties) throws Exception
  {
    properties.putAll(properties);
  }

  /**
   * get the boolean value of a configuration value
   * 
   * @param key key name of the value
   * @return <code>boolean</code> primitive type
   */
  public boolean getConfigBoolean(String key)
  {
    return Boolean.valueOf((String) config.get(key)).booleanValue();
  }

  /**
   * Get the current test configuration
   * 
   * @return <code>Hashtable</code> with all the defined properties
   */
  public Hashtable getConfig()
  {
    return config;
  }

  /**
   * Sets the hashtable of properties for this test
   * 
   * @param config <code>Hashtable</code> with all the defined properties
   */
  public void setConfig(Hashtable config)
  {
    this.config = config;
  }

  /**
   * Sets a single test parameter
   * 
   * @param key the key to define
   * @param value the value of the key
   */
  public void set(String key, Object value)
  {
    config.put(key, value);
  }

  /**
   * Shortcut to know if we should use prepare statement in this let
   * 
   * @return true or false
   */
  public boolean usePreparedStatement()
  {
    return Boolean.valueOf((String) config.get(USE_PREPARED_STATEMENT))
        .booleanValue();
  }

  /**
   * Should we ignore case related problems in this let
   * 
   * @return true or false
   */
  public boolean ignoreCase()
  {
    return Boolean.valueOf((String) config.get(IGNORE_CASE)).booleanValue();
  }

  /**
   * Should we use transactions in this let
   * 
   * @return true or false
   */
  public boolean useTransaction()
  {
    return Boolean.valueOf((String) config.get(USE_TRANSACTIONS))
        .booleanValue();
  }

  /**
   * Should we use CJDBC class while we can use generic ones in this let This is
   * useful for blobs and clobs
   * 
   * @return true or false
   */
  public boolean useCJDBCClass()
  {
    return Boolean.valueOf((String) config.get(USE_CJDBC_CLASS)).booleanValue();
  }

  /**
   * Returns the initialMemoryUsage value.
   * 
   * @return Returns the initialMemoryUsage.
   */
  public long getInitialMemoryUsage()
  {
    return initialMemoryUsage;
  }

  /**
   * Gather the total memory usage since the let was instanciated
   * 
   * @return a long value of kilobytes of memory used
   */
  public long getTotalMemoryUsage()
  {
    System.gc();
    return checkMemoryUsage() - initialMemoryUsage;
  }

  /**
   * Get the total time usage in seconds
   * 
   * @return a long value of seconds
   */
  public long getTotalTimeUsage()
  {
    return (System.currentTimeMillis() - initialTime) / 1000;
  }
}