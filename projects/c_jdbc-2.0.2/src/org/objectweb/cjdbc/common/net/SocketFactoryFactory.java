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
 * Initial developer(s): Marc Wick.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.KeyManagerFactory;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.TrustManager;
import com.sun.net.ssl.TrustManagerFactory;

/**
 * This class defines a SocketFactory
 * 
 * @author <a href="mailto:marc.wick@monte-bre.ch">Marc Wick </a>
 * @version 1.0
 */
public class SocketFactoryFactory
{

  /**
   * create a server socket factory with the specified configuration
   * 
   * @param config - the ssl configuration
   * @return - the socket factory
   * @throws SSLException - could not create factory
   */
  public static ServerSocketFactory createServerFactory(SSLConfiguration config)
      throws SSLException
  {
    try
    {

      if (config == null)
        // nothing todo return default SocketFactory
        return ServerSocketFactory.getDefault();

      SSLContext context = createSSLContext(config);
      // Finally, we get a SocketFactory
      SSLServerSocketFactory ssf = context.getServerSocketFactory();

      if (!config.isClientAuthenticationRequired())
        return ssf;

      return new AuthenticatedServerSocketFactory(ssf);
    }
    catch (Exception e)
    {
      throw new SSLException(e);
    }
  }

  /**
   * create a socket factory with the specified configuration
   * 
   * @param config - the ssl configuration
   * @return - the socket factory
   * @throws Exception - could not create factory
   */
  public static SocketFactory createFactory(SSLConfiguration config)
      throws Exception
  {
    if (config == null)
      // nothing todo return default SocketFactory
      return SocketFactory.getDefault();

    SSLContext context = createSSLContext(config);

    // Finally, we get a SocketFactory
    SSLSocketFactory ssf = context.getSocketFactory();

    if (!config.isClientAuthenticationRequired())
      return ssf;

    return new AuthenticatedSocketFactory(ssf);
  }

  /**
   * create a ssl context
   * 
   * @param config - ssl config
   * @return - the ssl context
   * @throws Exception - problems initializing the content
   */
  public static SSLContext createSSLContext(SSLConfiguration config)
      throws Exception
  {

    KeyManager[] kms = getKeyManagers(config.getKeyStore(), config
        .getKeyStorePassword(), config.getKeyStoreKeyPassword());

    TrustManager[] tms = getTrustManagers(config.getTrustStore(), config
        .getTrustStorePassword());

    // Now construct a SSLContext using these KeyManagers. We
    // specify a null SecureRandom, indicating that the
    // defaults should be used.
    SSLContext context = SSLContext.getInstance("SSL");
    context.init(kms, tms, null);
    return context;
  }

  protected static KeyManager[] getKeyManagers(File keyStore,
      String keyStorePassword, String keyPassword) throws IOException,
      GeneralSecurityException
  {
    // First, get the default KeyManagerFactory.
    String alg = KeyManagerFactory.getDefaultAlgorithm();
    KeyManagerFactory kmFact = KeyManagerFactory.getInstance(alg);

    // Next, set up the KeyStore to use. We need to load the file into
    // a KeyStore instance.
    FileInputStream fis = new FileInputStream(keyStore);
    KeyStore ks = KeyStore.getInstance("jks");

    char[] passwd = null;
    if (keyStorePassword != null)
    {
      passwd = keyStorePassword.toCharArray();
    }
    ks.load(fis, passwd);
    fis.close();

    // Now we initialize the TrustManagerFactory with this KeyStore
    kmFact.init(ks, keyPassword.toCharArray());

    // And now get the TrustManagers
    KeyManager[] kms = kmFact.getKeyManagers();
    return kms;
  }

  protected static TrustManager[] getTrustManagers(File trustStore,
      String trustStorePassword) throws IOException, GeneralSecurityException
  {
    // First, get the default TrustManagerFactory.
    String alg = TrustManagerFactory.getDefaultAlgorithm();
    TrustManagerFactory tmFact = TrustManagerFactory.getInstance(alg);

    // Next, set up the TrustStore to use. We need to load the file into
    // a KeyStore instance.
    FileInputStream fis = new FileInputStream(trustStore);
    KeyStore ks = KeyStore.getInstance("jks");
    ks.load(fis, trustStorePassword.toCharArray());
    fis.close();

    // Now we initialize the TrustManagerFactory with this KeyStore
    tmFact.init(ks);

    // And now get the TrustManagers
    TrustManager[] tms = tmFact.getTrustManagers();
    return tms;
  }
}
