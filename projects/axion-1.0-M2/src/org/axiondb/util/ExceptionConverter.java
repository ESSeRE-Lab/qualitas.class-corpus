/*
 * $Id: ExceptionConverter.java,v 1.7 2003/05/26 18:11:56 cburdick Exp $
 * =======================================================================
 * Copyright (c) 2002-2003 Axion Development Team.  All rights reserved.
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

package org.axiondb.util;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;

/**
 * Converts Axion-specific {@link Exception}s into 
 * {@link SQLException}s.
 * <p />
 * (This class should eventually handle converting various
 * {@link AxionException}s in to the proper SQLException 
 * with vendor message and code.)
 * 
 * @version $Revision: 1.7 $ $Date: 2003/05/26 18:11:56 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public class ExceptionConverter {
    public static SQLException convert(AxionException e) {
        return convert(null, e);
    }

    public static SQLException convert(String msg, AxionException e) {
        logConversion("AxionException","SQLException",e);
        if(e.getNestedThrowable() instanceof SQLException) {
            return (SQLException)(e.getNestedThrowable());
        } else if (msg != null) {
            return new SQLException(msg + " (" + e.toString() + ")");
        } else {            
            return new SQLException(e.toString());
        }
    }

    public static SQLException convert(RuntimeException e) {
        logConversion("RuntimeException","SQLException",e);
        return new SQLException(e.toString());
    }
    
    public static SQLException convert(String message, RuntimeException e) {
        logConversion("RuntimeException","SQLException",e);
        return new SQLException(message + ": " + e.toString());
    }
    
    public static SQLException convert(IOException e) {
        logConversion("IOException","SQLException",e);
        return new SQLException(e.toString());
    }

    public static IllegalArgumentException convertToIllegalArgumentException(String message, RuntimeException e) {
        if(e instanceof IllegalArgumentException) {
            return (IllegalArgumentException)e;
        } else {
            logConversion("RuntimeException","IllegalArgumentException",e);
            return new IllegalArgumentException(message + ": "+ e.toString());
        }
    }

    public static IOException convertToIOException(Exception e) {
        if(e instanceof IOException) {
            return (IOException)e;
        } else {
            logConversion("Exception","IOException",e);
            return new IOException(e.toString());
        }
    }

    public static RuntimeException convertToRuntimeException(Exception e) {
        if(e instanceof RuntimeException) {
            return (RuntimeException)e;
        } else {
            logConversion("Exception","RuntimeException",e);
            return new RuntimeException(e.toString());
        }
    }

    private static final void logConversion(String from, String to, Throwable t) {
        t.printStackTrace();
        if(_log.isDebugEnabled()) {
            _log.debug("Converting " + from + " to " + to,t);
        }
    }
    
    private static final Log _log = LogFactory.getLog(ExceptionConverter.class);
}
