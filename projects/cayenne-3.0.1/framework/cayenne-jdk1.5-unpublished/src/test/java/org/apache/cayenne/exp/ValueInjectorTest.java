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
package org.apache.cayenne.exp;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.remote.RemoteCayenneCase;
import org.apache.cayenne.testdo.mt.ClientMtTable1Subclass;
import org.apache.cayenne.testdo.mt.MtTable1Subclass;

public class ValueInjectorTest extends RemoteCayenneCase {
    public void test() {
        ObjectContext context = createDataContext();
        ObjEntity entity = context.getEntityResolver().lookupObjEntity(MtTable1Subclass.class);
        Expression qualifier = entity.getDeclaredQualifier();
        
        try {
            MtTable1Subclass ee = context.newObject(MtTable1Subclass.class);
            assertEquals(ee.getGlobalAttribute1(), "sub1");
            
            //check AND
            entity.setDeclaredQualifier(qualifier.andExp(Expression.fromString("serverAttribute1 = 'sa'")));
            ee = context.newObject(MtTable1Subclass.class);
            assertEquals(ee.getGlobalAttribute1(), "sub1");
            assertEquals(ee.getServerAttribute1(), "sa");
        }
        finally {
            entity.setDeclaredQualifier(qualifier);
        }
    }
    
    public void testRemote() {
        ObjectContext context = createROPContext();
        ObjEntity entity = context.getEntityResolver().lookupObjEntity(ClientMtTable1Subclass.class);
        Expression qualifier = entity.getDeclaredQualifier();
        
        try {
            ClientMtTable1Subclass ee = context.newObject(ClientMtTable1Subclass.class);
            assertEquals(ee.getGlobalAttribute1(), "sub1");
            
            //check AND
            entity.setDeclaredQualifier(qualifier.andExp(Expression.fromString("serverAttribute1 = 'sa'")));
            ee = context.newObject(ClientMtTable1Subclass.class);
            assertEquals(ee.getGlobalAttribute1(), "sub1");
            assertEquals(ee.getServerAttribute1(), "sa");
        }
        finally {
            entity.setDeclaredQualifier(qualifier);
        }
    }
}
