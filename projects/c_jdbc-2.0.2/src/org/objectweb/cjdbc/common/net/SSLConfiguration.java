/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Initial developer(s): Marc Wick.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.net;

import java.io.File;
import java.io.Serializable;

/**
 * This class defines a SSLConfiguration
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class SSLConfiguration implements Serializable
{
  private static final long serialVersionUID               = -7030030045041996566L;

  /** kestore file */
  private File              keyStore;
  /** keystore password */
  private String            keyStorePassword;
  /** key password */
  private String            keyStoreKeyPassword;

  // TODO : provide support for naming aliases

  /** need client authentication */
  private boolean           isClientAuthenticationRequired = false;

  /** truststore file */
  private File              trustStore;
  /** truststore password */
  private String            trustStorePassword;

  /**
   * Returns the isClientAuthenticationRequired value.
   * 
   * @return Returns the isClientAuthenticationRequired.
   */
  public boolean isClientAuthenticationRequired()
  {
    return isClientAuthenticationRequired;
  }

  /**
   * Sets the isClientAuthenticationRequired value.
   * 
   * @param isClientAuthenticationRequired The isClientAuthenticationRequired to
   *          set.
   */
  public void setClientAuthenticationRequired(
      boolean isClientAuthenticationRequired)
  {
    this.isClientAuthenticationRequired = isClientAuthenticationRequired;
  }

  /**
   * Returns the keyStore value.
   * 
   * @return Returns the keyStore.
   */
  public File getKeyStore()
  {
    return keyStore;
  }

  /**
   * Sets the keyStore value.
   * 
   * @param keyStore The keyStore to set.
   */
  public void setKeyStore(File keyStore)
  {
    this.keyStore = keyStore;
  }

  /**
   * Returns the keyStoreKeyPassword value.
   * 
   * @return Returns the keyStoreKeyPassword.
   */
  public String getKeyStoreKeyPassword()
  {
    if (keyStoreKeyPassword != null)
      return keyStoreKeyPassword;
    return getKeyStorePassword();
  }

  /**
   * Sets the keyStoreKeyPassword value.
   * 
   * @param keyStoreKeyPassword The keyStoreKeyPassword to set.
   */
  public void setKeyStoreKeyPassword(String keyStoreKeyPassword)
  {
    this.keyStoreKeyPassword = keyStoreKeyPassword;
  }

  /**
   * Returns the keyStorePassword value.
   * 
   * @return Returns the keyStorePassword.
   */
  public String getKeyStorePassword()
  {
    return keyStorePassword;
  }

  /**
   * Sets the keyStorePassword value.
   * 
   * @param keyStorePassword The keyStorePassword to set.
   */
  public void setKeyStorePassword(String keyStorePassword)
  {
    this.keyStorePassword = keyStorePassword;
  }

  /**
   * Returns the trustStore value.
   * 
   * @return Returns the trustStore.
   */
  public File getTrustStore()
  {
    if (trustStore != null)
      return trustStore;

    return getKeyStore();
  }

  /**
   * Sets the trustStore value.
   * 
   * @param trustStore The trustStore to set.
   */
  public void setTrustStore(File trustStore)
  {
    this.trustStore = trustStore;
  }

  /**
   * Returns the trustStorePassword value.
   * 
   * @return Returns the trustStorePassword.
   */
  public String getTrustStorePassword()
  {
    if (trustStorePassword != null)
      return trustStorePassword;

    return getKeyStorePassword();
  }

  /**
   * Sets the trustStorePassword value.
   * 
   * @param trustStorePassword The trustStorePassword to set.
   */
  public void setTrustStorePassword(String trustStorePassword)
  {
    this.trustStorePassword = trustStorePassword;
  }

  /**
   * create a SSLConfiguration with the java default behaviour (using System
   * properties)
   * 
   * @return config
   */
  public static SSLConfiguration getDefaultConfig()
  {
    SSLConfiguration config = new SSLConfiguration();
    config.keyStore = new File(System.getProperty("javax.net.ssl.keyStore"));
    config.keyStorePassword = System
        .getProperty("javax.net.ssl.keyStorePassword");
    config.keyStoreKeyPassword = System
        .getProperty("javax.net.ssl.keyStorePassword");
    config.trustStore = new File(System.getProperty("javax.net.ssl.trustStore"));
    config.trustStorePassword = System
        .getProperty("javax.net.ssl.trustStorePassword");
    return config;
  }

}
