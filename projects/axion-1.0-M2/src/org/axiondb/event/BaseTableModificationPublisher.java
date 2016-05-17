/*
 * $Id: BaseTableModificationPublisher.java,v 1.3 2003/03/27 19:14:05 rwald Exp $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.axiondb.AxionException;

/**
 * Provides utilities for publishing {@link RowEvent events}
 * to one or more {@link TableModificationListener listeners}.
 * 
 * @version $Revision: 1.3 $ $Date: 2003/03/27 19:14:05 $
 * @author Rodney Waldhoff 
 * @author Chuck Burdick
 */
public class BaseTableModificationPublisher {
    public void addTableModificationListener(TableModificationListener listener) {
        _tableModificationListeners.add(listener);
    }

    public void removeTableModificationListener(TableModificationListener listener) {
        _tableModificationListeners.remove(listener);
    }

    protected Iterator getTableModificationListeners() {
        return _tableModificationListeners.iterator();
    }

    public void publishEvent(TableModifiedEvent e) throws AxionException {
        Iterator iter = _tableModificationListeners.iterator();
        while(iter.hasNext()) {
            TableModificationListener listener = (TableModificationListener)(iter.next());
            e.visit(listener);
        }
    }

    private List _tableModificationListeners = new ArrayList();
}
