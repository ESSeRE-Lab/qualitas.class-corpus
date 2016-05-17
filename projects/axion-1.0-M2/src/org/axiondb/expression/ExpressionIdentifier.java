/*
 * $Id: ExpressionIdentifier.java,v 1.4 2003/07/07 21:50:15 rwald Exp $
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

package org.axiondb.expression;

import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.RowDecorator;
import org.axiondb.Selectable;

/**
 * A database expression identifier.
 *
 * @version $Revision: 1.4 $ $Date: 2003/07/07 21:50:15 $
 * @author Rahul Dwivedi 
 */
public class ExpressionIdentifier implements Selectable{
    
    public ExpressionIdentifier(String name) {
         _name = name;
    }
    
    public Object evaluate(RowDecorator row) throws AxionException {
        throw new AxionException("Unsupported.");
    }
    
    public DataType getDataType() {
        return null;
    }
    
    public String getLabel() {
        return getName();
    }
    
    public String getName() {
        return _name;
    }

    public void setLeftSelectable(Selectable sel){
        _leftSelectable = sel;
    }
    
    public void setRightSelectable(Selectable sel){
        _rightSelectable = sel;
    }
    
    public void setOperationType(short opType){
        _operationType = opType;
    }
    
    public Selectable getLeftSelectable(){
        return _leftSelectable;
    }
    
    public short getOperationType(){
        return _operationType;
    }
        
    public Selectable getRightSelectable(){
        return _rightSelectable;
    }
    
    private String _name;
    private Selectable _leftSelectable=null;
    private Selectable _rightSelectable =null;
    
    private short _operationType =-1 ;
    public static short MINUS_OPERATION = 0;
    public static short PLUS_OPERATION = 1;
    public static short CONCAT_OPERATION = 2;
    public static short MULT_OPERATION = 3;
    public static short DIV_OPERATION = 4;
    
}
