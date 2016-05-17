/*
 * $Id: BaseConstraint.java,v 1.10 2003/02/12 16:19:28 cburdick Exp $
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

import java.util.Random;

import org.axiondb.AxionException;
import org.axiondb.Constraint;
import org.axiondb.Database;
import org.axiondb.TableIdentifier;
import org.axiondb.event.RowEvent;

/**
 * Abstract base {@link Constraint} implementation.
 * 
 * @version $Revision: 1.10 $ $Date: 2003/02/12 16:19:28 $
 * @author Rodney Waldhoff
 * @author James Strachan
 */
public abstract class BaseConstraint implements Constraint {
    /**
     * Creates a {@link Constraint} with the 
     * given <i>name</i> and <i>type</i>.
     * @param name the name of this constraint (see {@link #setName})
     *             which may be <code>null</code>
     * @param type the type of this constraint (see {@link #getType}), 
     *             which should not be <code>null</code>
     */
    public BaseConstraint(String name, String type) {
        setName(name);
        _type = type;
    }
    
    public abstract boolean evaluate(RowEvent event) throws AxionException;
    
    /** This base implementation is a no-op. */
    public void resolve(Database db, TableIdentifier table) throws AxionException {
    }
    
    public String getName() {
        return _name;
    }
    
    /**
     * Sets the name of this constraint.
     * When <i>name</i> is <code>null</code>
     * a unique name is programatically generated.
     */
    public void setName(String name) {
        if(null == name) {
            name = "C_" +
                String.valueOf(System.currentTimeMillis()) +
                "_" +
                _idCounter++;
        } else {
            name = name.toUpperCase();
        }
        _name = name;
    }

    public String getType() {
        return _type;
    }

    public boolean isDeferred() {
        return _deferred;
    }
    
    public void setDeferred(boolean deferred) throws AxionException {
        if(deferred && !_deferrable) {
            throw new AxionException("Not deferrable.");
        } else {
            _deferred = deferred;
        }
    }

    public boolean isDeferrable() {
        return _deferrable;
    }

    public void setDeferrable(boolean deferrable) {
        _deferrable = deferrable;
        if(!_deferrable) {
            _deferred = false;
        }
    }

    private String _name = null;
    private String _type = null;
    private boolean _deferred = false;
    private boolean _deferrable = false;

    private static int _idCounter = 0;
    static {
        Random random = new Random();
        _idCounter = random.nextInt();
    }
}

