/*
 * $Id: MinFunction.java,v 1.6 2003/03/27 19:14:06 rwald Exp $
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
import org.axiondb.ColumnIdentifier;
import org.axiondb.DataType;
import org.axiondb.FunctionFactory;
import org.axiondb.RowDecorator;
import org.axiondb.RowDecoratorIterator;
import org.axiondb.Selectable;
import org.axiondb.types.IntegerType;

/**
 * <code>MIN(<i>number</i>)</code>: 
 * an aggregate function returning the least of the 
 * {@link IntegerType integer}-valued inputs.
 *
 * @version $Revision: 1.6 $ $Date: 2003/03/27 19:14:06 $
 * @author Rodney Waldhoff
 */
public class MinFunction extends BaseFunction implements AggregateFunction, FunctionFactory {
    public MinFunction() {
        super("MIN");
    }

    public ConcreteFunction makeNewInstance() {
        return new MinFunction();
    }

    public DataType getDataType() {
        return RETURN_TYPE;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        return row.get(new ColumnIdentifier(getName()));
    }

    public Object evaluate(RowDecoratorIterator rows) throws AxionException {
        if(!rows.hasNext()) {
            return null;
        } else {
            int min = Integer.MAX_VALUE;
            Selectable sel = getArgument(0);
            while(rows.hasNext()) {
                Object val = sel.evaluate(rows.next());
                if(RETURN_TYPE.accepts(val)) {
                    Integer intval = (Integer)(RETURN_TYPE.convert(val));
                    if(min > intval.intValue()) {
                        min = intval.intValue();
                    }
                } else {
                    throw new AxionException("Value " + val + " cannot be converted to an Integer.");
                }
            }
            return new Integer(min);
        }
    }

    public boolean isValid() {
        if(getArgumentCount() != 1) {
            return false;
        } else {
            return true;
        }
    }

    private static final DataType RETURN_TYPE = new IntegerType();
}
