package org.apache.cayenne.itest.cpa.defaults.client.auto;

import org.apache.cayenne.PersistentObject;

/**
 * A generated persistent class mapped as "DefaultsTable1" Cayenne entity. It is a good idea to
 * avoid changing this class manually, since it will be overwritten next time code is
 * regenerated. If you need to make any customizations, put them in a subclass.
 */
public class _DefaultsTable1 extends PersistentObject {

    public static final String NAME_PROPERTY = "name";

    protected String name;

    public String getName() {
        if(objectContext != null) {
            objectContext.prepareForAccess(this, "name", false);
        }
        
        return name;
    }
    public void setName(String name) {
        if(objectContext != null) {
            objectContext.prepareForAccess(this, "name", false);
        }
        
        Object oldValue = this.name;
        this.name = name;
        
        // notify objectContext about simple property change
        if(objectContext != null) {
            objectContext.propertyChanged(this, "name", oldValue, name);
        }
    }
    
    
}
