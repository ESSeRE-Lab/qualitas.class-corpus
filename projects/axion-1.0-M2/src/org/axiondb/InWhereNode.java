/*
 * $Id: InWhereNode.java,v 1.6 2003/07/09 21:43:17 cburdick Exp $
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

package org.axiondb;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.types.BooleanType;
/**
 * A {@link WhereNode} representing an membership test on a list of
 * arguments.
 *
 * @version $Revision: 1.6 $ $Date: 2003/07/09 21:43:17 $
 * @author Chuck Burdick
 * @author Amrish Lal
 */
public class InWhereNode implements WhereNode {
    private static Log _log = LogFactory.getLog(InWhereNode.class);
    private Selectable _left = null;
    private List _els = null;
    private static final DataType RETURN_TYPE = new BooleanType();

    /**
     * Used to {@link #evaluate} whether the value of the {@link
     * Selectable} is in the list of {@link Literal Literals}.
     */
    public InWhereNode(Selectable left, List literals) {
        setLeft(left);
        _els = literals;
    }

    public DataType getDataType() {
        return RETURN_TYPE;
    }

    public String getName() {
        return ("CONDITION");
    }

    public String getLabel() {
        return (getName());
    }

    public void setLeft(Selectable left) {
        _left = left;
    }

    public Selectable getLeft() {
        return _left;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        boolean found = false;
        Object eval = _left.evaluate(row);
        if (eval != null) {
            Iterator i = _els.iterator();
            while (!found && i.hasNext()) {
                Object elt = ((Literal)i.next()).evaluate(row);
                found = eval.equals(_left.getDataType().convert(elt));
            }
            if (_log.isDebugEnabled()) {
                _log.debug("Evaluating InWhere on " + eval.getClass().getName() + ":" + eval + " returning " + found);
            }
        }
        return (found ? Boolean.TRUE : Boolean.FALSE );
    }
}
