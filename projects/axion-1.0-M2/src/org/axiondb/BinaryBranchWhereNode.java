/*
 * $Id: BinaryBranchWhereNode.java,v 1.4 2003/07/09 21:48:48 cburdick Exp $
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
import org.axiondb.DataType;
import org.axiondb.types.BooleanType;

/**
 * A {@link WhereNode} representing a Boolean combination of  
 * its children.
 * 
 * @version $Revision: 1.4 $ $Date: 2003/07/09 21:48:48 $
 * @author Rodney Waldhoff
 * @author Amrish Lal
 * @author Chuck Burdick
 */
public class BinaryBranchWhereNode extends BranchWhereNode {
    public BinaryBranchWhereNode() {
    }

    public DataType getDataType() {
        return RETURN_TYPE;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        boolean result = false;
        if(isAnd()) {
            result = ((Boolean)getLeft().evaluate(row)).booleanValue() && ((Boolean)getRight().evaluate(row)).booleanValue();
        } else {
            result = ((Boolean)getLeft().evaluate(row)).booleanValue() || ((Boolean)getRight().evaluate(row)).booleanValue();
        }
        return (result ? Boolean.TRUE : Boolean.FALSE);
    }

    public String getName() {
        return ("CONDITION");
    }

    public String getLabel() {
        return (getName());
    }

    public WhereNode getLeft() {
        return _left;
    }
    
    public void setLeft(WhereNode left) {
        _left = left;
    }
    
    public WhereNode getRight() {
        return _right;
    }

    public void setRight(WhereNode right) {
        _right = right;
    }
    
    public boolean isAnd() {
        return _isAnd;
    }

    public void setIsAnd(boolean isAnd) {
        _isAnd = isAnd;
    }

    public boolean isOr() {
        return !_isAnd;
    }

    public void setIsOr(boolean isOr) {
        _isAnd = !isOr;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("( ");
        buf.append(_left);
        if(_isAnd) {
            buf.append(" AND ");
        } else {
            buf.append(" OR ");
        }
        buf.append(_right);
        buf.append(" )");
        return buf.toString();
    }

    private WhereNode _left = null;
    private WhereNode _right = null;
    private boolean _isAnd = true; 
    private static final DataType RETURN_TYPE = new BooleanType();
}

