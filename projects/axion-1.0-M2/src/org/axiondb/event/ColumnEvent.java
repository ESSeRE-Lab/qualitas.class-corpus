/*
 * $Id: ColumnEvent.java,v 1.1 2003/02/12 16:22:56 cburdick Exp $
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

package org.axiondb.event;

import org.axiondb.AxionException;
import org.axiondb.Column;
import org.axiondb.Table;

/**
 * An event signifying that a {@link Column} in a {@link Table} has changed,
 * for example, due to a DDL command.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/02/12 16:22:56 $
 * @author Chuck Burdick
 */
public class ColumnEvent extends TableModifiedEvent {
    public ColumnEvent(Table table, Column col) {
        setTable(table);
        setColumn(col);
    }

    public Column getColumn() {
        return _col;
    }

    public void setColumn(Column col) {
        _col = col;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(getTable());
        buf.append(",");
        buf.append(getColumn());
        buf.append("}");
        return buf.toString();
    }

    public void visit(TableModificationListener listener) throws AxionException {
        listener.columnAdded(this);
    }

    private Column _col;
}
