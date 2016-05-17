/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.jpa.map;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.UniqueConstraint;

import org.apache.cayenne.util.XMLEncoder;
import org.apache.cayenne.util.XMLSerializable;

public class JpaUniqueConstraint implements XMLSerializable {

    protected Collection<String> columnNames;

    public JpaUniqueConstraint() {

    }

    public JpaUniqueConstraint(UniqueConstraint annotation) {
        // resolve collection
        getColumnNames();

        for (int i = 0; i < annotation.columnNames().length; i++) {
            columnNames.add(annotation.columnNames()[i]);
        }
    }

    public Collection<String> getColumnNames() {
        if (columnNames == null) {
            columnNames = new ArrayList<String>(2);
        }
        return columnNames;
    }

    public void encodeAsXML(XMLEncoder encoder) {
    }
}
