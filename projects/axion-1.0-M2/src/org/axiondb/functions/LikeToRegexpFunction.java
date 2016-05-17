/*
 * $Id: LikeToRegexpFunction.java,v 1.3 2003/07/09 17:09:55 cburdick Exp $
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

package org.axiondb.functions;

import java.util.HashMap;
import java.util.Map;

import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.FunctionFactory;
import org.axiondb.RowDecorator;
import org.axiondb.types.StringType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 1.3 $ $Date: 2003/07/09 17:09:55 $
 * @author Chuck Burdick
 */
public class LikeToRegexpFunction extends BaseFunction implements ScalarFunction, FunctionFactory {
    private static final Log _log = LogFactory.getLog(LikeToRegexpFunction.class);
    private static final DataType ARG_TYPE = new StringType();
    private StringBuffer _buf = null;
    private Map _convertCache = null;

    public LikeToRegexpFunction() {
        super("LIKE_TO_REGEXP");
        _buf = new StringBuffer();
        _convertCache = new HashMap();
    }

    public ConcreteFunction makeNewInstance() {
        return new LikeToRegexpFunction();
    }

    public DataType getDataType() {
        return ARG_TYPE;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        Object arg = getArgument(0).evaluate(row);
        String likePattern = null;
        String result = null;

        if (ARG_TYPE.accepts(arg)) {
            likePattern = (String)ARG_TYPE.convert(arg);
        } else {
            throw new AxionException(
                "Value " + arg + " cannot be converted to a StringType.");
        }
        
        result = (String)_convertCache.get(likePattern);
        if (result == null) {
            result = convertLike(likePattern);
            _convertCache.put(likePattern, result);
            if (_log.isDebugEnabled()) {
                _log.debug("Converted " + likePattern + " to " + result);
            }
        }
        return result;
    }

    protected String convertLike(String orig) {
        _buf.setLength(0);
        int cap = 2 * orig.length();
        if (_buf.capacity() < cap) {
            _buf.ensureCapacity(cap);
        }

        if (orig.charAt(0) != '%') {
            _buf.append("^");
        }
        boolean escaped = false;
        String repl = null;
        for (int i = 0; i < orig.length(); i++) {
            char next = orig.charAt(i);
            switch (next) {
            case '%':
                repl = ".*";
                break;
            case '_':
                repl = ".";
                break;
            case '?':
                repl = "\\?";
                break;
            case '*':
                repl = "\\*";
                break;
            case '.':
                repl = "\\.";
                break;
            case '\\':
                if (i == orig.length() -1) {
                    break;
                } else {
                    escaped = true;
                    continue;
                }
            default:
                if (escaped) {
                    _buf.append('\\');
                }
            }
            if (repl == null || escaped) {
                _buf.append(next);
            } else {
                _buf.append(repl);
            }
            repl = null;
            escaped = false;
        }
        if (orig.length() < 2 || (orig.charAt(orig.length()-1) != '%' &&
                                  orig.charAt(orig.length()-2) != '\\')) {
            _buf.append("$");
        }
        return _buf.toString();
    }

    public boolean isValid() {
        return (getArgumentCount() == 1);
    }
}
