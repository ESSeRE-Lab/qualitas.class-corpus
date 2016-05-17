/*
 * $Id: Expression.java,v 1.3 2003/06/10 17:48:42 cburdick Exp $
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
package org.axiondb.expression;

import org.axiondb.Selectable;
import org.axiondb.RowDecorator;
import org.axiondb.AxionException;
import org.axiondb.DataType;
import org.axiondb.types.*;

/**
 * A database expression.
 *
 * @version $Revision: 1.3 $ $Date: 2003/06/10 17:48:42 $
 * @author Rahul Dwivedi 
 */

public class Expression implements Selectable {
    
    public Expression() {
        _name = "Expression";
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

    public short getOperationType(){
        return _operationType;
    }

    public Selectable getLeftSelectable(){
        return _leftSelectable;
    }
    
    public Selectable getRightSelectable(){
        return _rightSelectable;
    }
     
    public Object evaluate(RowDecorator row) throws AxionException {
        Object rVal = null;
        Object lVal = null;
        
        Object leftVal = _leftSelectable.evaluate(row);
        Object rightVal = _rightSelectable.evaluate(row);
        
        
        if(getDataType().accepts(rightVal)) {
            rVal =  getDataType().convert(rightVal);
        } else {
            throw new AxionException("Right  Value " + rightVal + " cannot be converted to a required Return Type For Expression.");
        }
        if(getDataType().accepts(leftVal)) {
            lVal = getDataType().convert(leftVal);
        } else {
            throw new AxionException("Left Value " + leftVal + " cannot be converted to a Return Type For Expression.");
        }
        if(rVal == null || lVal == null){
            return null;
        }
        leftVal = doOperation(lVal,rVal,_operationType);

        if(null == leftVal){
            throw new AxionException(" Operation not supported between given Data Types.");
        }
        return leftVal;
    }
    
    private Object doOperation(Object left , Object right, short operationType) throws AxionException{
        if(getDataType() instanceof LongType){
            return doOperationForLong((Long)left, (Long)right , operationType);
        } else if(getDataType() instanceof IntegerType){
            return doOperationForInteger((Integer)left, (Integer)right , operationType);
        }else if(getDataType() instanceof ShortType){
            return doOperationForShort((Short)left, (Short)right , operationType);
        }else if(getDataType() instanceof FloatType){
            return doOperationForFloat((Float)left, (Float)right , operationType);
        }else if(getDataType() instanceof StringType){
            return doOperationForString((String)left, (String)right , operationType);
        }else {
            return null;
        }        
    }

    private String doOperationForString(String left , String right, short operationType) throws AxionException{        
        if(operationType == CONCAT_OPERATION){
            return left + right;
        } else
            return null;
    }

    private Long doOperationForLong(Long left , Long right, short operationType) throws AxionException{
        if(operationType == MINUS_OPERATION){
            return new Long(left.longValue() - right.longValue());
        } else if(operationType==PLUS_OPERATION){
            return new Long(left.longValue() + right.longValue());
        } else if(operationType==MULT_OPERATION){
            return new Long(left.longValue() * right.longValue());
        } else
            return null;
    }

     private Short doOperationForShort(Short left , Short right, short operationType) throws AxionException{        
        if(operationType == MINUS_OPERATION){
            return new Short((short)(left.shortValue() - right.shortValue()));
        } else if(operationType==PLUS_OPERATION){
            return new Short((short)(left.shortValue() + right.shortValue()));
        } else if(operationType==MULT_OPERATION){
            return new Short((short)(left.shortValue() * right.shortValue()));
        } else
            return null;
    }

    private Integer doOperationForInteger(Integer left , Integer right, short operationType) throws AxionException{        
        if(operationType == MINUS_OPERATION){
            return new Integer(left.intValue() - right.intValue());
        } else if(operationType==PLUS_OPERATION){
            return new Integer(left.intValue() + right.intValue());
        } else if(operationType==MULT_OPERATION){
            return new Integer(left.intValue() * right.intValue());
        } else
            return null;
    }

    private Float doOperationForFloat(Float left , Float right, short operationType) throws AxionException{        
        if(operationType == MINUS_OPERATION){
            return new Float(left.floatValue() - right.floatValue());
        } else if(operationType==PLUS_OPERATION){
            return new Float(left.floatValue() + right.floatValue());
        } else if(operationType==MULT_OPERATION){
            return new Float(left.floatValue() * right.floatValue());
        } else if(operationType==DIV_OPERATION){
            return new Float(left.floatValue() / right.floatValue());
        }else {
            return null;
        }
    }
    
    
    public DataType getDataType() {
        if(_returnType==null){
            return resolveReturnType();
        }
        else {
            return _returnType;
        }
    }
    
    public String getLabel() {
        return _name;
    }
    
    public String getName() {
        return _name;
    }

    /*
     * Function identifies the return DataType resolved depending on
     * the Type of Operation Performed.
     */
    private DataType resolveReturnType() {
        DataType lDType = _leftSelectable.getDataType();
        DataType rDType = _rightSelectable.getDataType();
        if(_operationType == CONCAT_OPERATION)
        {
            _returnType = new StringType();
        } else if(_operationType == DIV_OPERATION)
        {
            _returnType = new FloatType();
        } else if(lDType instanceof FloatType || rDType instanceof FloatType){
            _returnType = new FloatType();
        } else if(lDType instanceof LongType || rDType instanceof LongType){
            _returnType = new LongType();
        } else if(lDType instanceof IntegerType || rDType instanceof IntegerType){
            _returnType = new IntegerType();
        } else {
            _returnType = lDType;
        }
        return _returnType;
    }

    private String _name ;
    private Selectable _leftSelectable=null;
    private Selectable _rightSelectable = null;
    
    private short _operationType = -1;
    public static short MINUS_OPERATION = 0;
    public static short PLUS_OPERATION = 1;
    public static short CONCAT_OPERATION = 2;
    public static short MULT_OPERATION = 3;
    public static short DIV_OPERATION = 4;
   
    private   DataType _returnType = null;
}
