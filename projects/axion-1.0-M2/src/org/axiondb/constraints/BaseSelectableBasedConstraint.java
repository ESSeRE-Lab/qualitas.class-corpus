/*
 * $Id: BaseSelectableBasedConstraint.java,v 1.4 2003/02/12 16:19:28 cburdick Exp $
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

package org.axiondb.constraints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.axiondb.AxionException;
import org.axiondb.Database;
import org.axiondb.Selectable;
import org.axiondb.SelectableBasedConstraint;
import org.axiondb.TableIdentifier;
import org.axiondb.event.RowEvent;

/**
 * Abstract base {@link SelectableBasedConstraint} implementation.
 * 
 * @version $Revision: 1.4 $ $Date: 2003/02/12 16:19:28 $
 * @author Rodney Waldhoff
 * @author James Strachan
 */
public abstract class BaseSelectableBasedConstraint extends BaseConstraint implements SelectableBasedConstraint {
    /**
     * Creates a {@link org.axiondb.Constraint} with the 
     * given <i>name</i> and <i>type</i>.
     * @param name the name of this constraint (see {@link #setName})
     *             which may be <code>null</code>
     * @param type the type of this constraint (see {@link #getType}), 
     *             which should not be <code>null</code>
     */
    public BaseSelectableBasedConstraint(String name, String type) {
        super(name,type);
        _selectables = new ArrayList();
    }
    
    public abstract boolean evaluate(RowEvent event) throws AxionException;
    
    public void addSelectable(Selectable sel) {
        _selectables.add(sel);
    }
    
    public int getSelectableCount() {
        return _selectables.size();
    }
    
    public Selectable getSelectable(int i) {
        return (Selectable)(_selectables.get(i));
    }
    
    public List getSelectableList() {
        // XXX FIX ME XXX would an iterator suffice?
        return Collections.unmodifiableList(_selectables);
    }

    /**
     * This base implementation 
     * {@link Database#resolveSelectable resolves} 
     * all of the
     * {@link Selectable}s in my list.
     */
    public void resolve(Database db, TableIdentifier table) throws AxionException {
        TableIdentifier[] tables = null;
        if(null != table) {
            tables = new TableIdentifier[1];
            tables[0] = table;
        } else {
            tables = new TableIdentifier[0];
        }
        for(int i=0;i<_selectables.size();i++) {
            _selectables.set(i,db.resolveSelectable((Selectable)_selectables.get(i),tables));
        }
    }

    private List _selectables = null;
}

