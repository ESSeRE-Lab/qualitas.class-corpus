/*
/* $Id: CoalesceFunction.java,v 1.1 2003/07/07 16:14:28 rwald Exp $ tag
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

/**
 * <code>COALESCE([<i>value</i>]+)</code>:
 * returns the first non-null value in the
 * argument list.
 *
 * @version $Revision: 1.1 $ $Date: 2003/07/07 16:14:28 $
 * @author Amrish Lal
 */
public class CoalesceFunction extends BaseFunction implements ScalarFunction, FunctionFactory {
    public CoalesceFunction() {
        super("COALESCE");
    }

    public ConcreteFunction makeNewInstance() {
        return new CoalesceFunction();
    }
    
    /** {@link StringType} */
    public DataType getDataType() {
        return _returnType;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        Object result = null;
        for (int i = 0; (i < getArgumentCount()) && (result == null) ;i++) {
            Selectable sel = getArgument(i);
            result = sel.evaluate(row);
            _returnType = sel.getDataType();
        }
        return (result);
    }

    public boolean isValid() {
        return (getArgumentCount() > 0);
    }

    private DataType _returnType = null;
}
