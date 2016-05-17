/*
 * Copyright 2008 Werner Guttmann, Peter Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.castor.cpa.jpa.info.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.junit.Ignore;

/**
 * Domain class annotated with JPA annotation used for unit testing.
 * @author <a href="mailto:peter-list AT stayduebeauty DOT com">Peter Schmidt</a>
 * @author <a href=" mailto:wguttmn AT codehaus DOT org">Werner Guttmann</a>
 * @version $Revision: 8994 $ $Date: 2011-08-02 01:40:59 +0200 (Di, 02 Aug 2011) $
 */
@Ignore

@Entity
public class TestI2DCJPATest {
    //-----------------------------------------------------------------------------------
    
    /** 
     * A primary key. 
     */
    private String _primaryKey;

    /** 
     * A string property. 
     */
    private String _bla;

    /** 
     * A string property. 
     */
    private String _blob;
    
    private int _default;
    
    //-----------------------------------------------------------------------------------

    public final int getDefault() {
        return _default;
    }

    public final void setDefault(final int default1) {
        _default = default1;
    }

    @Id
    @Column(name = "primary_key", 
            unique = true, 
            nullable = false, 
            insertable = true, 
            updatable = true, 
            columnDefinition = "TESTDefinitionPrimaryKey", 
            table = "JPAtableTEST", 
            length = 10, 
            precision = 100, 
            scale = 1000)
    public String getPrimaryKey() {
        return _primaryKey;
    }

    public void setPrimaryKey(final String primaryKey) {
        _primaryKey = primaryKey;
    }

    @Column(name = "JPAcolumnTESTbla", 
            unique = true, 
            nullable = false, 
            insertable = true, 
            updatable = true, 
            columnDefinition = "TESTDefinitionBla", 
            table = "JPAtableTEST", 
            length = 10, 
            precision = 100, 
            scale = 1000)
    public final String getBla() {
        return _bla;
    }

    public final void setBla(final String bla) {
        _bla = bla;
    }

    @Column(name = "JPAcolumnTESTblob", 
            unique = false, 
            nullable = true, 
            insertable = false, 
            updatable = false, 
            columnDefinition = "TESTDefinitionBlob", 
            table = "JPAtableTEST", 
            length = 2000, 
            precision = 200, 
            scale = 20)
    public final String getBlob() {
        return _blob;
    }

    public final void setBlob(final String blob) {
        _blob = blob;
    }
    
    //-----------------------------------------------------------------------------------
}
