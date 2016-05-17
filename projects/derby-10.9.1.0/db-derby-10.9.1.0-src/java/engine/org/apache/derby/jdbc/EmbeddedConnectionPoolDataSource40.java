/*
 
   Derby - Class org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40
 
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
 */
package org.apache.derby.jdbc;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import org.apache.derby.impl.jdbc.Util;
import org.apache.derby.iapi.reference.SQLState;

/** 
	EmbeddedConnectionPoolDataSource40 is Derby's ConnectionPoolDataSource
	implementation for the JDBC4.0 environment.
	

	<P>A ConnectionPoolDataSource is a factory for PooledConnection
	objects. An object that implements this interface will typically be
	registered with a JNDI service.
	<P>
	EmbeddedConnectionPoolDataSource40 supports the JDBC 4.0 specification
	for the J2SE 6.0 Java Virtual Machine environment. Use
	EmbeddedConnectionPoolDataSource if your application runs in the
	following environments:
	<UL>
	<LI> JDBC 3.0 - Java 2 - JDK 1.4, J2SE 5.0
	</UL>	

	<P>EmbeddedConnectionPoolDataSource40 is serializable and referenceable.

	<P>See EmbeddedDataSource40 for DataSource properties.

 */
public class EmbeddedConnectionPoolDataSource40 
                                extends EmbeddedConnectionPoolDataSource 
                                implements ConnectionPoolDataSource {    
    
    /**
     * Returns false unless <code>interfaces</code> is implemented 
     * 
     * @param  interfaces             a Class defining an interface.
     * @return true                   if this implements the interface or 
     *                                directly or indirectly wraps an object 
     *                                that does.
     * @throws java.sql.SQLException  if an error occurs while determining 
     *                                whether this is a wrapper for an object 
     *                                with the given interface.
     */
    public boolean isWrapperFor(Class<?> interfaces) throws SQLException {
        return interfaces.isInstance(this);
    }
    
    /**
     * Returns <code>this</code> if this class implements the interface
     *
     * @param  interfaces a Class defining an interface
     * @return an object that implements the interface
     * @throws java.sql.SQLExption if no object if found that implements the 
     * interface
     */
    public <T> T unwrap(java.lang.Class<T> interfaces) 
                            throws SQLException{
        //Derby does not implement non-standard methods on 
        //JDBC objects
        //hence return this if this class implements the interface 
        //or throw an SQLException
        try {
            return interfaces.cast(this);
        } catch (ClassCastException cce) {
            throw Util.generateCsSQLException(SQLState.UNABLE_TO_UNWRAP,
                    interfaces);
        }
    }
   
    ////////////////////////////////////////////////////////////////////
    //
    // INTRODUCED BY JDBC 4.1 IN JAVA 7
    //
    ////////////////////////////////////////////////////////////////////

    public  Logger getParentLogger()
        throws SQLFeatureNotSupportedException
    {
        throw (SQLFeatureNotSupportedException) Util.notImplemented( "getParentLogger()" );
    }

}
