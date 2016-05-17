/*
 * $Id: Sequence.java,v 1.8 2003/05/13 19:33:46 rwald Exp $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.axiondb.event.DatabaseSequenceEvent;
import org.axiondb.event.SequenceModificationListener;
import org.axiondb.types.IntegerType;

/**
 * A database sequence.
 * A sequence provides a mechanism for obtaining unique integer values 
 * from the database.
 *
 * @version $Revision: 1.8 $ $Date: 2003/05/13 19:33:46 $
 * @author Chuck Burdick
 */
public class Sequence extends Literal implements Selectable {
    
    /**
     * Create a equence starting whose
     * initial value is 0.
     */
    public Sequence(String name) {
        this(name, 0);
    }

    /**
     * Create a equence starting whose
     * initial value is <i>startVal</i>.
     */
    public Sequence(String name, int startVal) {
        super(new IntegerType());
        _name = name.toUpperCase();
        _val = new Integer(startVal);
        _listeners = new ArrayList();
    }

    /**
     * Get the name of this sequence.
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the name of this sequence.
     */
    public String getLabel() {
        return getName();
    }

    /**
     * Get the current value of this sequence.
     */
    public Object getValue() throws AxionException {
        return _val;
    }

    /** 
     * Increment and return the next value in this sequence.
     */
    public Object evaluate() throws AxionException {
        _val = new Integer(_val.intValue() + 1);
        Iterator it = _listeners.iterator();
        while(it.hasNext()) {
            SequenceModificationListener cur = (SequenceModificationListener)it.next();
            cur.sequenceIncremented(new DatabaseSequenceEvent(this));
        }
        return _val;
    }

    public void addSequenceModificationListener(SequenceModificationListener listener) {
        _listeners.add(listener);
    }

    private String _name = null;
    private Integer _val = null;
    private List _listeners = null;
}
