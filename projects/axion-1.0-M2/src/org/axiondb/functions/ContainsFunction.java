/*
 * $Id: ContainsFunction.java,v 1.4 2003/07/09 21:50:51 cburdick Exp $
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

import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.FunctionFactory;
import org.axiondb.RowDecorator;
import org.axiondb.types.BooleanType;
import org.axiondb.types.StringType;

/**
 * <tt>CONTAINS(string, string)</tt>: returns a {@link BooleanType boolean} 
 * that indicates whether the second string is a substring of the first.
 *
 * @version $Revision: 1.4 $ $Date: 2003/07/09 21:50:51 $
 * @author Chuck Burdick
 */
public class ContainsFunction
    extends BaseFunction
    implements ScalarFunction, FunctionFactory {
    public ContainsFunction() {
        super("CONTAINS");
    }

    public ConcreteFunction makeNewInstance() {
        return new ContainsFunction();
    }

    public DataType getDataType() {
        return RETURN_TYPE;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        String[] args = new String[2];

        for (int i = 0; i < 2; i++) {
            Object arg = getArgument(i).evaluate(row);
            if (ARG_TYPE.accepts(arg)) {
                args[i] = (String) ARG_TYPE.convert(arg);
            } else {
                throw new AxionException(
                    "Value " + arg + " cannot be converted to a StringType.");
            }
        }

        return (
            ((args[0] != null) && (args[1] != null) && (args[0].indexOf(args[1]) != -1))
                ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean isValid() {
        return (getArgumentCount() == 2);
    }

    private static final DataType ARG_TYPE = new StringType();
    private static final DataType RETURN_TYPE = new BooleanType();
}
