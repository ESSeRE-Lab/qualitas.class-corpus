/*
 * $Id: BaseFunction.java,v 1.3 2003/05/13 19:33:47 rwald Exp $
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

package org.axiondb.functions;

import java.util.ArrayList;
import java.util.List;

import org.axiondb.DataType;
import org.axiondb.Selectable;

/**
 * An abstract base {@link ConcreteFunction} implementation.
 * 
 * @version $Revision: 1.3 $ $Date: 2003/05/13 19:33:47 $
 * @author Rodney Waldhoff
 * @author Chuck Burdick
 */
public abstract class BaseFunction implements ConcreteFunction {
    public abstract DataType getDataType();
    public abstract boolean isValid();

    public BaseFunction(String name) {
        setName(name);
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public String getLabel() {
        return getName();
    }

    public void addArgument(Selectable arg) {
        _args.add(arg);
    }

    public int getArgumentCount() {
        return _args.size();
    }

    public Selectable getArgument(int i) {
        return (Selectable)(_args.get(i));
    }

    public String toString() {
        return _name + "(" + _args + ")";
    }

    private List _args = new ArrayList();
    private String _name = null;
}
