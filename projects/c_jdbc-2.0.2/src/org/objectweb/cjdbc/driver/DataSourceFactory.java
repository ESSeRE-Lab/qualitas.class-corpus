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
 * Initial developer(s): Marek Prochazka.
 * Contributor(s): 
 */
package org.objectweb.cjdbc.driver;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * <code>DataSource</code> factory for to implement <code>Referenceable</code>.
 * The factory serves for the {@link DataSource},<code>PooledDataSource</code>,
 * and <code>XADataSource</code> classes.
 * 
 * @author <a href="mailto:Marek.Prochazka@inrialpes.fr">Marek Prochazka</a>
 * @version 1.0
 */
public class DataSourceFactory implements ObjectFactory
{
  /** DataSource classnames. */
  protected final String dataSourceClassName = "org.objectweb.cjdbc.driver.DataSource";
  protected final String poolDataSourceName  = "org.objectweb.cjdbc.driver.PoolDataSource";
  protected final String xaDataSourceName    = "org.objectweb.cjdbc.driver.XADataSource";

  /**
   * Gets an instance of the requested <code>DataSource</code> object.
   * 
   * @param objRef object containing location or reference information that is
   *          used to create an object instance (could be <code>null</code>).
   * @param name name of this object relative to specified <code>nameCtx</code>
   *          (could be <code>null</code>).
   * @param nameCtx name context (could ne null if the default initial context
   *          is used).
   * @param env environment to use (could be null if default is used)
   * @return a newly created instance of C-JDBC DataSource, <code>null</code>
   *         if an error occurred.
   * @throws Exception if an error occurs when creating the object instance.
   */
  public Object getObjectInstance(Object objRef, Name name, Context nameCtx,
      Hashtable env) throws Exception
  {
    // Check the requested object class
    Reference ref = (Reference) objRef;
    String className = ref.getClassName();
    if ((className == null)
        || !(className.equals(dataSourceClassName)
            | className.equals(poolDataSourceName) | className
            .equals(xaDataSourceName)))
    {
      // Wrong class
      return null;
    }
    DataSource ds = null;
    try
    {
      ds = (DataSource) Class.forName(className).newInstance();
    }
    catch (Exception e)
    {
      throw new RuntimeException("Error when creating C-JDBC" + className
          + " instance: " + e);
    }

    ds.setUrl((String) ref.get(DataSource.URL_PROPERTY).getContent());
    ds.setUser((String) ref.get(DataSource.USER_PROPERTY).getContent());
    ds.setPassword((String) ref.get(DataSource.PASSWORD_PROPERTY).getContent());
    return ds;
  }
}
