/*
 * $Id: Base64DecodeFunction.java,v 1.1 2003/05/14 22:28:40 rwald Exp $
 * =======================================================================
 * Copyright (c) 2003 Axion Development Team.  All rights reserved.
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

import org.apache.commons.codec.base64.Base64;
import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.FunctionFactory;
import org.axiondb.RowDecorator;
import org.axiondb.Selectable;
import org.axiondb.types.AnyType;
import org.axiondb.types.StringType;

/**
 * <tt>BASE64DECODE(string)</tt>: returns a byte array  
 * representing the Base64 decoded value of the given <i>string</i>.
 *
 * @version $Revision: 1.1 $ $Date: 2003/05/14 22:28:40 $
 * @author Rodney Waldhoff
 */
public class Base64DecodeFunction extends BaseFunction implements ScalarFunction, FunctionFactory {
    public Base64DecodeFunction() {
        super("BASE64DECODE");
    }

    public ConcreteFunction makeNewInstance() {
        return new Base64DecodeFunction();
    }

    public DataType getDataType() {
        return RETURN_TYPE;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        Selectable sel = getArgument(0);
        Object val = sel.evaluate(row);
        if(STRING_TYPE.accepts(val)) {
            String strval = (String)(STRING_TYPE.convert(val));
            if(null == strval) {
                return null;
            } else {
                return Base64.decode(strval.getBytes());
            }
        } else {
            throw new AxionException("Value " + val + " cannot be converted to a StringType.");
        }
    }

    public boolean isValid() {
        return getArgumentCount() == 1;
    }

    private static final DataType RETURN_TYPE = new AnyType(); // ???
    private static final DataType STRING_TYPE = new StringType();
}
