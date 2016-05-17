/*
 * $Id: NotWhereNode.java,v 1.5 2003/07/09 21:48:48 cburdick Exp $
 * =======================================================================
 * Copyright (c) 2002-2003 Axion Development Team.  All rights reserved.
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
 * @version $Revision: 1.5 $ $Date: 2003/07/09 21:48:48 $
 * @author Rodney Waldhoff
 * @author Amrish Lal
 * @author Chuck Burdick
 */
public class NotWhereNode extends BranchWhereNode {
    public NotWhereNode(WhereNode child) {
        setChild(child);
    }

    public DataType getDataType() {
        return RETURN_TYPE;
    }

    public Object evaluate(RowDecorator row) throws AxionException {
        boolean result = ((Boolean) getChild().evaluate(row)).booleanValue();
        return (result ? Boolean.FALSE : Boolean.TRUE);
    }

    public String getName() {
        return ("CONDITION");
    }

    public String getLabel() {
        return (getName());
    }

    public WhereNode getChild() {
        return _child;
    }
    
    public void setChild(WhereNode child) {
        _child = child;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("( NOT ( ");
        buf.append(_child);
        buf.append(" ) )");
        return buf.toString();
    }

    private WhereNode _child = null;
    private static final DataType RETURN_TYPE = new BooleanType();
}

