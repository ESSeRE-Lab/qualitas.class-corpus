/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
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

package org.objectweb.cjdbc.driver;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.objectweb.cjdbc.controller.core.ControllerConstants;
import org.objectweb.cjdbc.driver.connectpolicy.AbstractControllerConnectPolicy;
import org.objectweb.cjdbc.driver.connectpolicy.OrderedConnectPolicy;
import org.objectweb.cjdbc.driver.connectpolicy.PreferredListConnectPolicy;
import org.objectweb.cjdbc.driver.connectpolicy.RandomConnectPolicy;
import org.objectweb.cjdbc.driver.connectpolicy.RoundRobinConnectPolicy;
import org.objectweb.cjdbc.driver.connectpolicy.SingleConnectPolicy;

/**
 * This class defines a C-JDBC url with parsed Metadata and so on. The
 * connection policy is interpreted while reading the URL. We could rename it to
 * ParsedURL.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet
 *         </a>
 * @version 1.0
 */
public class CjdbcUrl
{
  private String                          url;
  private String                          databaseName;
  private ControllerInfo[]                controllerList;
  private HashMap                         parameters;
  private AbstractControllerConnectPolicy controllerConnectPolicy;

  // Debug information
  private int                             debugLevel;
  /** Most verbose level of debug */
  public static final int                 DEBUG_LEVEL_DEBUG = 2;
  /** Informational level of debug */
  public static final int                 DEBUG_LEVEL_INFO  = 1;
  /** No debug messages */
  public static final int                 DEBUG_LEVEL_OFF   = 0;

  /**
   * Creates a new <code>CjdbcUrl</code> object, parse it and instantiate the
   * connection creation policy.
   * 
   * @param url the URL to parse
   * @throws SQLException if an error occurs while parsing the url
   */
  public CjdbcUrl(String url) throws SQLException
  {
    this.url = url;
    parseUrl();
    String debugProperty = (String) parameters.get(Driver.DEBUG_PROPERTY);
    debugLevel = DEBUG_LEVEL_OFF;
    if (debugProperty != null)
    {
      if ("debug".equals(debugProperty))
        debugLevel = DEBUG_LEVEL_DEBUG;
      else if ("info".equals(debugProperty))
        debugLevel = DEBUG_LEVEL_INFO;
    }
    controllerConnectPolicy = createConnectionPolicy();
  }

  /**
   * Returns true if debugging is set to debug level 'debug'
   * 
   * @return true if debugging is enabled
   */
  public boolean isDebugEnabled()
  {
    return debugLevel == DEBUG_LEVEL_DEBUG;
  }

  /**
   * Returns true if debug level is 'info' or greater
   * 
   * @return true if debug level is 'info' or greater
   */
  public boolean isInfoEnabled()
  {
    return debugLevel >= DEBUG_LEVEL_INFO;
  }

  /**
   * Returns the controllerConnectPolicy value.
   * 
   * @return Returns the controllerConnectPolicy.
   */
  public AbstractControllerConnectPolicy getControllerConnectPolicy()
  {
    return controllerConnectPolicy;
  }

  /**
   * Returns the controllerList value.
   * 
   * @return Returns the controllerList.
   */
  public ControllerInfo[] getControllerList()
  {
    return controllerList;
  }

  /**
   * Returns the database name.
   * 
   * @return Returns the database name.
   */
  public String getDatabaseName()
  {
    return databaseName;
  }

  /**
   * Returns the URL parameters/value in a HashMap (warning this is not a
   * clone).
   * <p>
   * The HashMap is 'parameter name'=>'value'
   * 
   * @return Returns the parameters and their value in a Hasmap.
   */
  public HashMap getParameters()
  {
    return parameters;
  }

  /**
   * Returns the url value.
   * 
   * @return Returns the url.
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Sets the url value.
   * 
   * @param url The url to set.
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  //
  // Private methods (mainly parsing)
  //

  /**
   * Create the corresponding controller connect policy according to what is
   * found in the URL. If no policy was specified then a
   * <code>RandomConnectPolicy</code> is returned.
   * 
   * @return an <code>AbstractControllerConnectPolicy</code>
   */
  private AbstractControllerConnectPolicy createConnectionPolicy()
  {
    if (controllerList.length == 1)
      return new SingleConnectPolicy(controllerList, debugLevel);

    String policy = (String) parameters
        .get(Driver.PREFERRED_CONTROLLER_PROPERTY);
    String retryInterval = (String) parameters
        .get(Driver.RETRY_INTERVAL_IN_MS_PROPERTY);
    long retryIntervalInMs;

    // Default is random policy with retry interval of 5 seconds
    if (retryInterval == null)
      retryIntervalInMs = Driver.DEFAULT_RETRY_INTERVAL_IN_MS;
    else
      retryIntervalInMs = Long.parseLong(retryInterval);

    if (policy == null)
      return new RandomConnectPolicy(controllerList, retryIntervalInMs,
          debugLevel);

    // preferredController: defines the strategy to use to choose a preferred
    // controller to connect to
    // - jdbc:cjdbc://node1,node2,node3/myDB?preferredController=roundRobin
    //   round robin starting with first node in URL
    if (policy.equals("roundRobin"))
      return new RoundRobinConnectPolicy(controllerList, retryIntervalInMs,
          debugLevel);

    // - jdbc:cjdbc://node1,node2,node3/myDB?preferredController=ordered
    //   Always connect to node1, and if not available then try to node2 and
    //   finally if none are available try node3.
    if (policy.equals("ordered"))
      return new OrderedConnectPolicy(controllerList, retryIntervalInMs,
          debugLevel);

    // - jdbc:cjdbc://node1,node2,node3/myDB?preferredController=random
    //   default strategy
    if (policy.equals("random"))
      return new RandomConnectPolicy(controllerList, retryIntervalInMs,
          debugLevel);

    // - jdbc:cjdbc://node1,node2,node3/myDB?preferredController=node2,node3
    //   same as above but round-robin (or random?) between 2 and 3
    return new PreferredListConnectPolicy(controllerList, retryIntervalInMs,
        policy, debugLevel);
  }

  /**
   * Checks for URL correctness and extract database name, controller list and
   * parameters.
   * 
   * @exception SQLException if an error occurs.
   */
  private void parseUrl() throws SQLException
  {
    // Find the hostname and check for URL correctness
    if (url == null)
    {
      throw new IllegalArgumentException(
          "Illegal null URL in parseURL(String) method");
    }

    if (!url.toLowerCase().startsWith(Driver.CJDBC_URL_HEADER))
      throw new SQLException("Malformed header from URL '" + url
          + "' (expected '" + Driver.CJDBC_URL_HEADER + "')");
    else
    {
      // Get the controllers list
      int nextSlash = url.indexOf('/', Driver.CJDBC_URL_HEADER_LENGTH);
      if (nextSlash == -1)
        // Missing '/' between hostname and database name.
        throw new SQLException("Malformed URL '" + url + "' (expected '"
            + Driver.CJDBC_URL_HEADER + "<hostname>/<database>')");

      // Found end of database name
      int questionMark = url.indexOf('?', nextSlash);
      questionMark = (questionMark == -1)
          ? url.indexOf(';', nextSlash)
          : questionMark;

      String controllerURLs = url.substring(Driver.CJDBC_URL_HEADER_LENGTH,
          nextSlash);
      // Check the validity of each controller in the list
      // empty tokens (when successive delims) are ignored
      StringTokenizer controllers = new StringTokenizer(controllerURLs, ",",
          false);
      int tokenNumber = controllers.countTokens();
      if (tokenNumber == 0)
      {
        throw new SQLException("Empty controller name in '" + controllerURLs
            + "' in URL '" + url + "'");
      }
      controllerList = new ControllerInfo[tokenNumber];
      int i = 0;
      String token;
      // TODO: the following code does not recognize the following buggy urls:
      // jdbc:cjdbc://,localhost:/tpcw or jdbc:cjdbc://host1,,host2:/tpcw
      while (controllers.hasMoreTokens())
      {
        token = controllers.nextToken().trim();
        if (token.equals("")) // whitespace tokens
        {
          throw new SQLException("Empty controller name in '" + controllerURLs
              + "' in URL '" + url + "'");
        }
        controllerList[i] = parseController(token);
        i++;
      }

      // Check database name validity
      databaseName = (questionMark == -1) ? url.substring(nextSlash + 1, url
          .length()) : url.substring(nextSlash + 1, questionMark);
      Character c = validDatabaseName(databaseName);
      if (c != null)
        throw new SQLException(
            "Unable to validate database name (unacceptable character '" + c
                + "' in database '" + databaseName + "' from URL '" + url
                + "')");

      // Get the parameters from the url
      parameters = parseUrlParams(url);
    }
  }

  /**
   * Parse the given URL and returns the parameters in a HashMap containing
   * ParamaterName=>Value.
   * 
   * @param urlString the URL to parse
   * @return a Hashmap of param name=>value possibly empty
   * @throws SQLException if an error occurs
   */
  private HashMap parseUrlParams(String urlString) throws SQLException
  {
    HashMap props = parseUrlParams(urlString, '?', "&", "=");
    if (props == null)
      props = parseUrlParams(urlString, ';', ";", "=");
    if (props == null)
      props = new HashMap();

    return props;
  }

  /**
   * Parse the given URL looking for parameters starting after the beginMarker,
   * using parameterSeparator as the separator between parameters and equal as
   * the delimiter between a parameter and its value.
   * 
   * @param urlString the URL to parse
   * @param beginMarker delimiter for beginning of parameters
   * @param parameterSeparator delimiter between parameters
   * @param equal delimiter between parameter and its value
   * @return HashMap of ParameterName=>Value
   * @throws SQLException if an error occurs
   */
  private HashMap parseUrlParams(String urlString, char beginMarker,
      String parameterSeparator, String equal) throws SQLException
  {
    int questionMark = urlString.indexOf(beginMarker, urlString
        .lastIndexOf('/'));
    if (questionMark == -1)
      return null;
    else
    {
      HashMap props = new HashMap();
      String params = urlString.substring(questionMark + 1);
      StringTokenizer st1 = new StringTokenizer(params, parameterSeparator);
      while (st1.hasMoreTokens())
      {
        String param = st1.nextToken();
        StringTokenizer st2 = new StringTokenizer(param, equal);
        if (st2.hasMoreTokens())
        {
          try
          {
            String paramName = st2.nextToken();
            String paramValue = (st2.hasMoreTokens()) ? st2.nextToken() : "";
            props.put(paramName, paramValue);
          }
          catch (Exception e) // TODOC: what are we supposed to catch here?
          {
            throw new SQLException("Invalid parameter in URL: " + urlString);
          }
        }
      }
      return props;
    }
  }

  /**
   * Checks the validity of the hostname, port number and controller name given
   * in the URL and build the full URL used to lookup a controller.
   * 
   * @param controller information regarding a controller.
   * @return a <code>ControllerInfo</code> object
   * @exception SQLException if an error occurs.
   */
  public static ControllerInfo parseController(String controller)
      throws SQLException
  {
    ControllerInfo controllerInfo = new ControllerInfo();

    // Check controller syntax
    StringTokenizer controllerURL = new StringTokenizer(controller, ":", true);

    // Get hostname
    controllerInfo.setHostname(controllerURL.nextToken());
    Character c = validHostname(controllerInfo.getHostname());
    if (c != null)
      throw new SQLException(
          "Unable to validate hostname (unacceptable character '" + c
              + "' in hostname '" + controllerInfo.getHostname()
              + "' from the URL part '" + controller + "')");

    if (!controllerURL.hasMoreTokens())
      controllerInfo.setPort(ControllerConstants.DEFAULT_PORT);
    else
    {
      controllerURL.nextToken(); // should be ':'
      if (!controllerURL.hasMoreTokens())
        controllerInfo.setPort(ControllerConstants.DEFAULT_PORT);
      else
      { // Get the port number
        String port = controllerURL.nextToken();
        if (controllerURL.hasMoreTokens())
          throw new SQLException(
              "Invalid controller definition with more than one semicolon in URL part '"
                  + controller + "'");

        // Check the port number validity
        try
        {
          controllerInfo.setPort(Integer.parseInt(port));
        }
        catch (NumberFormatException ne)
        {
          throw new SQLException(
              "Unable to validate port number (unacceptable port number '"
                  + port + "' in this URL part '" + controller + "')");
        }
      }
    }
    return controllerInfo;
  }

  /**
   * Checks that the given name contains acceptable characters for a hostname
   * name ([0-9][A-Z][a-z][["-_."]).
   * 
   * @param hostname name to check (caller must check that it is not
   *          <code>null</code>).
   * @return <code>null</code> if the hostname is acceptable, else the
   *         character that causes the fault.
   */
  private static Character validHostname(String hostname)
  {
    char[] name = hostname.toCharArray();
    int size = hostname.length();
    char c;
    //boolean lastCharWasPoint = false; // used to avoid '..' in hostname
    char lastChar = ' ';

    for (int i = 0; i < size; i++)
    {
      c = name[i];

      if (c == '.' || c == '-')
      {
        if (lastChar == '.' || lastChar == '-' || (i == size - 1) || (i == 0))
        {
          // . or - cannot be the first or the last char of hostname
          // hostname cannot contain '..' or '.-' or '-.' or '--'
          return new Character(c);
        }
      }
      else
      {
        if (((c < '0') || (c > 'z') || ((c > '9') && (c < 'A'))
            || ((c > 'Z') && (c < '_')) || (c == '`')))
        {
          return new Character(c);
        }
      }
      lastChar = c;
    }
    return null;
  }

  /**
   * Checks that the given name contains acceptable characters for a database
   * name ([0-9][A-Z][a-z]["-_"]).
   * 
   * @param databaseName name to check (caller must check that it is not
   *          <code>null</code>).
   * @return <code>null</code> if the name is acceptable, else the character
   *         that causes the fault.
   */
  private static Character validDatabaseName(String databaseName)
  {
    char[] name = databaseName.toCharArray();
    int size = databaseName.length();
    char c;

    for (int i = 0; i < size; i++)
    {
      c = name[i];
      if ((c < '-') || (c > 'z') || (c == '/') || (c == '.') || (c == '`')
          || ((c > '9') && (c < 'A')) || ((c > 'Z') && (c < '_')))
        return new Character(c);
    }
    return null;
  }
}
