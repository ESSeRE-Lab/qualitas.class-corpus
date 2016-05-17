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
package org.apache.cayenne.jpa.entity;

import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class MockTypesEntity {

    protected Calendar defaultCalendar;

    @Temporal(value = TemporalType.TIME)
    protected Calendar timeCalendar;

    @Temporal(value = TemporalType.DATE)
    protected Calendar dateCalendar;

    @Temporal(value = TemporalType.TIMESTAMP)
    protected Calendar timestampCalendar;

    protected MockEnum defaultEnum;

    @Enumerated(value = EnumType.ORDINAL)
    protected MockEnum ordinalEnum;

    @Enumerated(value = EnumType.STRING)
    protected MockEnum stringEnum;
    
    protected byte[] byteArray;
}
