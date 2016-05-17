/*
 
   Derby - Class org.apache.derbyTesting.functionTests.tests.jdbc4.Wrapper41Driver
 
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
 */

package org.apache.derbyTesting.functionTests.tests.jdbc4;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.apache.derby.jdbc.AutoloadedDriver40;
import org.apache.derby.jdbc.ClientDriver40;
import org.apache.derby.jdbc.Driver40;

/**
 * A wrapper around the methods added by JDBC 4.1.
 * We can eliminate this class after Java 7 goes GA and we are allowed
 * to use the Java 7 compiler to build our released versions of derbyTesting.jar.
 */
public  class   Wrapper41Driver
{
    ///////////////////////////////////////////////////////////////////////
    //
    // STATE
    //
    ///////////////////////////////////////////////////////////////////////

    private AutoloadedDriver40    _embedded;
    private Driver40            _driver40;
    private ClientDriver40      _netclient;
    
    ///////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTORS
    //
    ///////////////////////////////////////////////////////////////////////

    public Wrapper41Driver( Object wrapped ) throws Exception
    {
        if ( wrapped instanceof AutoloadedDriver40 ) { _embedded = (AutoloadedDriver40) wrapped; }
        else if ( wrapped instanceof Driver40 ) { _driver40 = (Driver40) wrapped; }
        else if ( wrapped instanceof ClientDriver40 ) { _netclient = (ClientDriver40) wrapped; }
        else { throw nothingWrapped( wrapped ); }
    }
    
    ///////////////////////////////////////////////////////////////////////
    //
    // JDBC 4.1 BEHAVIOR
    //
    ///////////////////////////////////////////////////////////////////////

    public  Logger    getParentLogger() throws SQLException
    {
        if ( _embedded != null ) { return _embedded.getParentLogger(); }
        else if ( _driver40 != null ) { return _driver40.getParentLogger(); }
        else if ( _netclient != null ) { return _netclient.getParentLogger(); }
        else { throw nothingWrapped( null ); }
    }

    ///////////////////////////////////////////////////////////////////////
    //
    // OTHER PUBLIC BEHAVIOR
    //
    ///////////////////////////////////////////////////////////////////////

    public Driver   getWrappedObject() throws SQLException
    {
        if ( _embedded != null ) { return _embedded; }
        else if ( _driver40 != null ) { return _driver40; }
        else if ( _netclient != null ) { return _netclient; }
        else { throw nothingWrapped( null ); }
    }

    ///////////////////////////////////////////////////////////////////////
    //
    // MINIONS
    //
    ///////////////////////////////////////////////////////////////////////

    private SQLException nothingWrapped( Object wrapped )
    {
        String  wrappedString = (wrapped == null ? "NULL" : wrapped.getClass().getName() );
        return new SQLException( "Nothing wrapped: " + wrappedString );
    }

}

