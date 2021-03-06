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

package org.apache.cayenne.dba.oracle;

import org.apache.cayenne.CayenneException;
import org.apache.cayenne.access.jdbc.BatchAction;
import org.apache.cayenne.access.trans.BatchQueryBuilder;
import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.BatchQuery;

/**
 * @since 1.2
 */
class OracleBatchAction extends BatchAction {

    OracleBatchAction(BatchQuery batchQuery, DbAdapter adapter, EntityResolver entityResolver) {
        super(batchQuery, adapter, entityResolver);
    }

    @Override
    protected BatchQueryBuilder createBuilder() throws CayenneException {
        // intercept super call to configure the builder...
        BatchQueryBuilder builder = super.createBuilder();
        builder.setTrimFunction(OracleAdapter.TRIM_FUNCTION);
        return builder;
    }
}
