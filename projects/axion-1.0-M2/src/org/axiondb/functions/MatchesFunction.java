/*
 * $Id: MatchesFunction.java,v 1.4 2003/07/09 21:50:51 cburdick Exp $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.FunctionFactory;
import org.axiondb.RowDecorator;
import org.axiondb.Selectable;
import org.axiondb.types.BooleanType;
import org.axiondb.types.StringType;

/**
 * <tt>MATCHES(string, string)</tt>: returns a {@link BooleanType
 * boolean} that indicates whether first string matches the {@link RE regular
 * expression} represented by the second string
 *
 * @version $Revision: 1.4 $ $Date: 2003/07/09 21:50:51 $
 * @author Chuck Burdick
 */
public class MatchesFunction extends BaseFunction implements ScalarFunction, FunctionFactory {
    public MatchesFunction() {
        super("MATCHES");
        _reCache = new HashMap();
    }

    public ConcreteFunction makeNewInstance() {
        return new MatchesFunction();
    }

    public DataType getDataType() {
        return RETURN_TYPE;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        String compare = getStringFromArg(getArgument(0), row);
        String pattern = getStringFromArg(getArgument(1), row);        

        if (pattern == null) {
            throw new AxionException(
                "Expected a regular expression string as second argument");
        }

        RE regex = (RE)_reCache.get(pattern);
        if (regex == null) {
            try {
                regex = new RE(pattern);
                _reCache.put(pattern, regex);
                if (_log.isDebugEnabled()) {
                    _log.debug("Compiled regular expression " + pattern);
                }
            } catch (RESyntaxException e) {
                throw new AxionException(e);
            }
        }
        return ((compare != null) && regex.match(compare) ? Boolean.TRUE : Boolean.FALSE);
    }

    private String getStringFromArg(Selectable sel, RowDecorator row) throws AxionException {
        String result = null;
        Object arg = sel.evaluate(row);
        if (ARG_TYPE.accepts(arg)) {
            result = (String)ARG_TYPE.convert(arg);
        } else {
            throw new AxionException(
                "Value " + arg + " cannot be converted to a StringType.");
        }
        return result;
    }

    public boolean isValid() {
        return (getArgumentCount() == 2);
    }

    private Map _reCache = null;
    protected static final DataType ARG_TYPE = new StringType();
    protected static final DataType RETURN_TYPE = new BooleanType();
    private static final Log _log = LogFactory.getLog(MatchesFunction.class);
}
