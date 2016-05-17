/*
 * $Id: PrimaryKeyConstraint.java,v 1.9 2003/05/13 19:33:47 rwald Exp $
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

package org.axiondb.constraints;

import org.axiondb.AxionException;
import org.axiondb.Row;
import org.axiondb.RowDecorator;
import org.axiondb.Selectable;
import org.axiondb.event.RowEvent;

/**
 * A PRIMARY KEY constraint, which is violated
 * whenever any of my {@link Selectable}s are <code>null</code>
 * or my collection of {@link Selectable}s is not
 * {@link UniqueConstraint#evaluate unique}.
 * 
 * @version $Revision: 1.9 $ $Date: 2003/05/13 19:33:47 $
 * @author James Strachan
 * @author Rodney Waldhoff
 */
public class PrimaryKeyConstraint extends UniqueConstraint {
    public PrimaryKeyConstraint(String name) {
        super(name,"PRIMARY KEY");
    }

    public boolean evaluate(RowEvent event) throws AxionException {
        Row row = event.getNewRow();
        if(null == row) {
            return true;
        } else {
            RowDecorator dec = event.getTable().makeRowDecorator();
            dec.setRow(row);
            for(int i=0;i<getSelectableCount();i++) {
                Selectable sel = getSelectable(i);
                Object value = sel.evaluate(dec);
                if(null == value) {
                    return false;
                }
            }
            return super.evaluate(event);
        }
        
    }

}
