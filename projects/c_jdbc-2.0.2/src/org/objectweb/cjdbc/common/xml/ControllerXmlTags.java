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
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.common.xml;

/**
 * List of the xml tags recognized to read and write the controller
 * configuration with.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */

public final class ControllerXmlTags
{
  /**
   * XML Tag and attributes to work with on the DTD and the xml file
   */

  /** Controller tag */
  public static final String ELT_CONTROLLER                   = "Controller";
  /** Controller name */
  public static final String ATT_CONTROLLER_NAME              = "name";
  /** Controller rmi port */
  public static final String ATT_CONTROLLER_PORT              = "port";
  /** <code>backlogSize</code> attribute in <code>Controller</code>. */
  public static final String ATT_backlogSize                  = "backlogSize";
  /** Controller IP address */
  public static final String ATT_CONTROLLER_IP                = "ipAddress";

  /** Internationalization */
  public static final String ELT_INTERNATIONALIZATION         = "Internationalization";
  /** Language */
  public static final String ATT_LANGUAGE                     = "language";

  /** Report Tag */
  public static final String ELT_REPORT                       = "Report";
  /** Enabled */
  public static final String ATT_REPORT_ENABLED               = "enabled";
  /** Hide data */
  public static final String ATT_REPORT_HIDE_SENSITIVE_DATA   = "hideSensitiveData";
  /** Generate on shutdown */
  public static final String ATT_REPORT_GENERATE_ON_SHUTDOWN  = "generateOnShutdown";
  /** Generate on fatal */
  public static final String ATT_REPORT_GENERATE_ON_FATAL     = "generateOnFatal";
  /** Enable file loggin */
  public static final String ATT_REPORT_ENABLE_FILE_LOGGING   = "enableFileLogging";
  /** Report Location */
  public static final String ATT_REPORT_REPORT_LOCATION       = "reportLocation";
  /** Delete on shutdown */
  public static final String ATT_REPORT_DELETE_ON_SHUTDOWN    = "deleteOnShutdown";

  /** Virtual Database tag */
  public static final String ELT_VIRTUAL_DATABASE             = "VirtualDatabase";
  /** Virtual Database name */
  public static final String ATT_VIRTUAL_DATABASE_NAME        = "virtualDatabaseName";
  /** Config file attribute */
  public static final String ATT_VIRTUAL_DATABASE_FILE        = "configFile";
  /** auto-enable backend attribute */
  public static final String ATT_VIRTUAL_DATABASE_AUTO_ENABLE = "autoEnableBackends";
  /** checkpoint when autoEnable is set to force */
  public static final String ATT_VIRTUAL_DATABASE_CHECKPOINT  = "checkpointName";
  /** True value for restoring backend */
  public static final String VAL_true                         = "true";
  /** False value for restoring backend */
  public static final String VAL_false                        = "false";
  /** Force value for restoring backend */
  public static final String VAL_force                        = "force";

  /** Jmx Settings tag */
  public static final String ELT_JMX                          = "JmxSettings";
  /** Jmx enable attribute */
  public static final String ATT_JMX_ENABLE                   = "enabled";
  /** http Jmx adaptor */
  public static final String ELT_HTTP_JMX_ADAPTOR             = "HttpJmxAdaptor";
  /** Rmi Jmx adaptor */
  public static final String ELT_RMI_JMX_ADAPTOR              = "RmiJmxAdaptor";
  /** Port of the adaptor */
  public static final String ATT_JMX_ADAPTOR_PORT             = "port";
  /** username of the adaptor */
  public static final String ATT_JMX_CONNECTOR_USERNAME       = "username";
  /** password of the adaptor */
  public static final String ATT_JMX_CONNECTOR_PASSWORD       = "password";

  /** ssl configuration */
  public static final String ELT_SSL                          = "SSL";
  /** kestore file */
  public static final String ATT_SSL_KEYSTORE                 = "keyStore";
  /** keystore password */
  public static final String ATT_SSL_KEYSTORE_PASSWORD        = "keyStorePassword";
  /** key password */
  public static final String ATT_SSL_KEYSTORE_KEYPASSWORD     = "keyStoreKeyPassword";
  /** need client authentication */
  public static final String ATT_SSL_NEED_CLIENT_AUTH         = "isClientAuthNeeded";
  /** truststore file */
  public static final String ATT_SSL_TRUSTSTORE               = "trustStore";
  /** truststore password */
  public static final String ATT_SSL_TRUSTSTORE_PASSWORD      = "trustStorePassword";

  /** Security tag */
  public static final String ELT_SECURITY                     = "SecuritySettings";
  /** Default Accept Connect */
  public static final String ATT_DEFAULT_CONNECT              = "defaultConnect";

  /** jar tag */
  public static final String ELT_JAR                          = "jar";
  /** allow driver attribute */
  public static final String ATT_JAR_ALLOW_DRIVER             = "allowAdditionalDriver";

  /** Shutdown configuration */
  public static final String ELT_SHUTDOWN                     = "Shutdown";
  /** should we backup */
  public static final String ATT_BACKUP_ON_SHUDOWN            = "backupOnShutdown";
  /** Configuration for the client shutdown */
  public static final String ELT_CLIENT                       = "Client";
  /** Allow attribute */
  public static final String ATT_ALLOW                        = "allow";
  /** limit to localhost */
  public static final String ATT_ONLY_LOCALHOST               = "onlyLocalhost";
  /** Configuration for the console shutdown */
  public static final String ELT_CONSOLE                      = "Console";
  /** accept attribute */
  public static final String ELT_ACCEPT                       = "Accept";
  /** block attribute */
  public static final String ELT_BLOCK                        = "Block";
  /** ipaddress attribute */
  public static final String ELT_IPADDRESS                    = "IpAddress";
  /** iprange attribute */
  public static final String ELT_IPRANGE                      = "IpRange";
  /** Hostname */
  public static final String ELT_HOSTNAME                     = "Hostname";
  /** Value */
  public static final String ATT_VALUE                        = "value";
}
