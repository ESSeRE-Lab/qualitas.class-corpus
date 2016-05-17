/*
 * $Id: RowComparator.java,v 1.9 2003/05/13 23:42:59 rwald Exp $
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.util.ExceptionConverter;

/**
 * A {@link Comparator} for {@link Row Rows}, 
 * which will sort a collection according to the
 * value of a single selectable.  By using a ComparatorChain,
 * one can perform in-memory sorts over multiple
 * columns.
 * 
 * @author Morgan Delagrange
 * @author Rodney Waldhoff (the hacks are all mine ;)
 */
public class RowComparator implements Comparator {

    private Selectable _sel = null;
    private RowDecorator _dec = null;
    private Comparator _cmp = null;
    private static Log _log = LogFactory.getLog(RowComparator.class);

    public RowComparator(Selectable sel, RowDecorator dec) {
        _sel = sel;
        _dec = dec;
        _cmp = _sel.getDataType().getComparator();
    }

    public int compare(Object o1, Object o2) {
        Object column1 = evaluate(o1);
        Object column2 = evaluate(o2);

        if (_log.isDebugEnabled()) {
            _log.debug("Selectable: " + _sel);
            _log.debug("COMPARING " + column1 + " TO " + column2);
        }
        int eval = 0;
        if(null == column1) {
            if(null == column2) {
                eval = 0;
            } else {
                eval = 1;
            }
        } else if(null == column2) {
            eval = -1;
        } else {
            eval = _cmp.compare(column1,column2);
        }
        if (_log.isDebugEnabled()) {
            _log.debug("COMPARE TO VALUE: " + eval);
        }
        return eval;
    }
    
    private Object evaluate(Object obj) {
        RowDecorator dec = null;
        if(obj instanceof RowDecorator) {
            dec = (RowDecorator)obj;
        } else if(obj instanceof Row) {
            _dec.setRow(-1,(Row)obj); // XXX FIX ME XXX
            dec = _dec; 
        } else {
            throw new RuntimeException("Expected RowDecorator or Row, found " + obj); // XXX FIX ME XXX
        }
        try {
            return _sel.evaluate(dec);
        } catch(AxionException e) {
            throw ExceptionConverter.convertToRuntimeException(e);
        }
    }
}
