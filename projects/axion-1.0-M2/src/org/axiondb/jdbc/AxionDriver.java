/*
 * $Id: AxionDriver.java,v 1.9 2003/07/09 23:56:17 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002 Axion Development Team.  All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above 
 *    copyright notice, this list of conditions and the following 
 *    disclaimer. 
 *   
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution. 
 *   
 * 3. The names "Tigris", "Axion", nor the names of its contributors may 
 *    not be used to endorse or promote products derived from this 
 *    software without specific prior written permission. 
 *  
 * 4. Products derived from this software may not be called "Axion", nor 
 *    may "Tigris" or "Axion" appear in their names without specific prior
 *    written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =======================================================================
 */

package org.axiondb.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.axiondb.AxionException;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link Driver} implementation.
 *
 * @version $Revision: 1.9 $ $Date: 2003/07/09 23:56:17 $
 * @author Chuck Burdick
 */
public class AxionDriver extends ConnectionFactory implements Driver {
   static {
      try {
         DriverManager.registerDriver(new AxionDriver());
      } catch (SQLException e) {
      }
   }

   public boolean acceptsURL(String url) throws SQLException {
      return isValidConnectString(url);
   }

   public Connection connect(String url, Properties info) throws SQLException {
      if (!acceptsURL(url)) {
         return null; // for some silly reason, jdbc insists we return null here
      }
      try {
          return createConnection(url);
      } catch(AxionException e) {
          throw ExceptionConverter.convert(e);
      }
   }

   public int getMajorVersion() {
      return 0;
   }

   public int getMinorVersion() {
      return 9;
   }

   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
      return new DriverPropertyInfo[0];
   }

   public boolean jdbcCompliant() {
      return false;
   }
}
