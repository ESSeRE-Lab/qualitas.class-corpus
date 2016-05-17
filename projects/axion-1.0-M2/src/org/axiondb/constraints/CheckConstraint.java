/*
 * $Id: CheckConstraint.java,v 1.8 2003/05/13 19:33:47 rwald Exp $
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
import org.axiondb.Database;
import org.axiondb.Row;
import org.axiondb.RowDecorator;
import org.axiondb.TableIdentifier;
import org.axiondb.WhereNode;
import org.axiondb.event.RowEvent;

/**
 * A CHECK constraint, which is violated whenever 
 * the given {@link #setCondition condition} is
 * violated.
 * 
 * @version $Revision: 1.8 $ $Date: 2003/05/13 19:33:47 $
 * @author Rodney Waldhoff
 * @author Amrish Lal
 */
public class CheckConstraint extends BaseConstraint {
    public CheckConstraint(String name) {
        super(name,"CHECK");
    }
    
    public void setCondition(WhereNode where) {
        _condition = where;
    }
    
    public WhereNode getCondition() {
        return _condition;
    }

    public void resolve(Database db, TableIdentifier table) throws AxionException {
        TableIdentifier[] tables = null;
        if(null != table) {
            tables = new TableIdentifier[1];
            tables[0] = table;
        } else {
            tables = new TableIdentifier[0];
        }
        db.resolveWhereNode(_condition,tables);
    }
    
    public boolean evaluate(RowEvent event) throws AxionException {
        Row row = event.getNewRow();
        if(null == row) {
            return true;
        } else {
            RowDecorator dec = event.getTable().makeRowDecorator();
            dec.setRow(row);
            return ((Boolean)_condition.evaluate(dec)).booleanValue();
        }
    }

    public String toSqlString() {
        StringBuffer buf = new StringBuffer();
        buf.append("CHECK(");
        buf.append(_condition.toString());
        buf.append(")");
        return (buf.toString());
    }
    
    private WhereNode _condition = null;
}

