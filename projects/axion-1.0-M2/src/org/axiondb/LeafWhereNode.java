/*
 * $Id: LeafWhereNode.java,v 1.7 2003/07/09 21:48:48 cburdick Exp $
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

import java.util.Comparator;

import org.axiondb.DataType;
import org.axiondb.types.BooleanType;
/**
 * A {@link WhereNode} representing a comparison between two columns and/or literal
 * values via some {@link ComparisonOperator}.
 *
 * @version $Revision: 1.7 $ $Date: 2003/07/09 21:48:48 $
 * @author Rodney Waldhoff
 * @author Amrish Lal
 * @author Rahul Dwivedi
 * @author Chuck Burdick
 */
public class LeafWhereNode implements WhereNode {

    public LeafWhereNode(Selectable left, ComparisonOperator op, Selectable right) {
        setLeft(left);
        setOperator(op);
        setRight(right);
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        Object left = null == _left ? null : _left.evaluate(row);
        Object right = null == _right ? null : _right.evaluate(row);

        // convert literal to appropriate type for comparision
        Comparator cmp = null;
        if(_left instanceof Literal && (!(_right instanceof Literal))) {
            left = _right.getDataType().convert(left);
            cmp = _right.getDataType().getComparator();
        } else if((!(_left instanceof Literal)) && _right instanceof Literal)  {
            right = _left.getDataType().convert(right);
            cmp = _left.getDataType().getComparator();
        } else {
            cmp = _left.getDataType().getComparator();
        }
        return (_operator.compare(left,right,cmp) ? Boolean.TRUE : Boolean.FALSE);
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

    public Selectable getLeft() {
        return _left;
    }

    public void setLeft(Selectable left) {
        _left = left;
    }

    public Selectable getRight() {
        return _right;
    }

    public void setRight(Selectable right) {
        _right = right;
    }

    public ComparisonOperator getOperator() {
        return _operator;
    }

    public void setOperator(ComparisonOperator op) {
        _operator = op;
    }

    public boolean isColumnColumn() {
        return (_left instanceof ColumnIdentifier && _right instanceof ColumnIdentifier);
    }

    public boolean isColumnLiteral() {
        return (_left instanceof ColumnIdentifier && _right instanceof Literal) ||
               (_left instanceof Literal && _right instanceof ColumnIdentifier);
    }

    public boolean isLiteralLiteral() {
        return (_left instanceof Literal && _right instanceof Literal);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(_left);
        buf.append(_operator);
        buf.append(_right);
        return buf.toString();
    }

    private Selectable _left = null;
    private ComparisonOperator _operator = null;
    private Selectable _right = null;
    private static final DataType RETURN_TYPE = new BooleanType();

}

