/*
 * $Id: MemoryDatabase.java,v 1.6 2003/05/01 16:39:00 rwald Exp $
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

package org.axiondb.engine;

import java.io.File;
import java.util.Properties;

import org.axiondb.AxionException;
import org.axiondb.Table;
import org.axiondb.TableFactory;

/**
 * A memory-resident {@link org.axiondb.Database}.
 * 
 * @version $Revision: 1.6 $ $Date: 2003/05/01 16:39:00 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 * @author Morgan Delagrange
 */
public class MemoryDatabase extends BaseDatabase {

    //------------------------------------------------------------- Constructors

    public MemoryDatabase() throws AxionException {
        this(null,null);
    }

    public MemoryDatabase(String name) throws AxionException {
        this(name,null);
    }

    public MemoryDatabase(String name, Properties props) throws AxionException {
        super(name);
        createMetaDataTables();
        if(null != props) {
            loadProperties(props);
        } else {
            loadProperties(getBaseProperties());
        }
    }

    public TableFactory getTableFactory(String name) {
        if(null == name || "default".equals(name)) {
            return DEFAULT_TABLE_FACTORY;
        } else {
            return super.getTableFactory(name);
        }
    }

    public File getTableDirectory() {
        return null;
    }

    protected Table createSystemTable(String name) {
        MemoryTable t = new MemoryTable(name);
        t.setType(Table.SYSTEM_TABLE_TYPE);
        return t;
    }

    //-------------------------------------------------------------- Attributes

    private static final TableFactory DEFAULT_TABLE_FACTORY = new MemoryTableFactory();
}
