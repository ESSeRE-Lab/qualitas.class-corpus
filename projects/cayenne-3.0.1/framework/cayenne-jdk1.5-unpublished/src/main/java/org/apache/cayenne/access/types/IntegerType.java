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
package org.apache.cayenne.access.types;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.validation.BeanValidationFailure;
import org.apache.cayenne.validation.ValidationResult;

/**
 * @since 3.0
 */
public class IntegerType implements ExtendedType {

    public String getClassName() {
        return Integer.class.getName();
    }

    public Object materializeObject(ResultSet rs, int index, int type) throws Exception {
        int value = rs.getInt(index);
        return (rs.wasNull()) ? null : value;
    }

    public Object materializeObject(CallableStatement rs, int index, int type)
            throws Exception {
        int value = rs.getInt(index);
        return (rs.wasNull()) ? null : value;
    }

    public void setJdbcObject(
            PreparedStatement statement,
            Object value,
            int pos,
            int type,
            int scale) throws Exception {

        if (value == null) {
            statement.setNull(pos, type);
        }
        else {
            statement.setInt(pos, ((Number) value).intValue());
        }
    }

    /**
     * @deprecated since 3.0 as validation should not be done at the DataNode level.
     */
    public boolean validateProperty(
            Object source,
            String property,
            Object value,
            DbAttribute dbAttribute,
            ValidationResult validationResult) {
        if (dbAttribute.isMandatory() && value == null) {
            validationResult.addFailure(new BeanValidationFailure(source, property, "'"
                    + property
                    + "' must be not null"));
            return false;
        }

        return true;
    }

}
