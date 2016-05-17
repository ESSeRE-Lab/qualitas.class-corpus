/*
 * $Id: BindVariable.java,v 1.5 2002/11/30 15:56:31 rwald Exp $
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

import org.axiondb.types.AnyType;

/**
 * A {@link Literal} that can be bound via 
 * {@link java.sql.PreparedStatement} methods.
 * 
 * @version $Revision: 1.5 $ $Date: 2002/11/30 15:56:31 $
 * @author Rodney Waldhoff 
 */
public class BindVariable extends Literal {
    public BindVariable() {
        super(AnyType.INSTANCE);
    }

    public boolean isBound() {
        return _bound;
    }

    public void setValue(Object obj) {
        super.setValue(obj);
        _bound = true;
    }

    protected Object getValue() throws AxionException {
        if(!_bound) {
            throw new AxionException("Not bound");
        } else {
            return super.getValue();
        }
    }
    
    public void clearBoundValue() {
        _bound = false;
        super.setValue(null);
    }
    
    public String toString() {
        if(!_bound) {
            return "unbound";
        } else {
            return super.toString();
        }
    }

    private boolean _bound = false;
}
