/*
 * $Id: ConcatFunction.java,v 1.2 2002/12/16 23:34:54 rwald Exp $
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
import org.axiondb.Selectable;
import org.axiondb.types.StringType;

/**
 * <code>CONCAT(<i>string</i> [, <i>string</i>]*)</code>: 
 * concatenates one or more {@link StringType strings}.
 * <p />
 * Evaluates to <code>NULL</code> if any of the arguments
 * are <code>null</code>.
 * 
 *
 * @version $Revision: 1.2 $ $Date: 2002/12/16 23:34:54 $
 * @author Rodney Waldhoff
 */
public class ConcatFunction extends BaseFunction implements ScalarFunction, FunctionFactory {
    public ConcatFunction() {
        super("CONCAT");
    }

    public ConcreteFunction makeNewInstance() {
        return new ConcatFunction();
    }
    
    /** {@link StringType} */
    public DataType getDataType() {
        return RETURN_TYPE;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        StringBuffer buf = new StringBuffer();
        for(int i=0;i<getArgumentCount();i++) {
            Object val = null;
            Selectable sel = getArgument(i);
            val = sel.evaluate(row);
            if(RETURN_TYPE.accepts(val)) {
                String strval = (String)(RETURN_TYPE.convert(val));
                if(null != strval) {
                    buf.append(strval);
                } else {
                    return null;
                }
            } else {
                throw new AxionException("Value " + val + " cannot be converted to a StringType.");
            }
        }
        return buf.toString();
    }

    public boolean isValid() {
        if(getArgumentCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private static final DataType RETURN_TYPE = new StringType();
}
