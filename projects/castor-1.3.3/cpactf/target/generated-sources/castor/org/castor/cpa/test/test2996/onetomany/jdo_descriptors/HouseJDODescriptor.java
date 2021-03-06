/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.castor.cpa.test.test2996.onetomany.jdo_descriptors;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.lang.reflect.Method;
import org.castor.core.exception.IllegalClassDescriptorInitialization;
import org.castor.cpa.test.test2996.onetomany.House;
import org.castor.jdo.engine.SQLTypeInfos;
import org.exolab.castor.jdo.engine.nature.ClassDescriptorJDONature;
import org.exolab.castor.jdo.engine.nature.FieldDescriptorJDONature;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.mapping.FieldDescriptor;
import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.mapping.loader.FieldDescriptorImpl;
import org.exolab.castor.mapping.loader.FieldHandlerImpl;
import org.exolab.castor.mapping.loader.TypeInfo;
import org.exolab.castor.mapping.xml.ClassChoice;
import org.exolab.castor.mapping.xml.ClassMapping;
import org.exolab.castor.mapping.xml.FieldMapping;
import org.exolab.castor.mapping.xml.MapTo;
import org.exolab.castor.mapping.xml.Sql;
import org.exolab.castor.mapping.xml.types.ClassMappingAccessType;
import org.exolab.castor.mapping.xml.types.FieldMappingCollectionType;

/**
 * Class HouseJDODescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class HouseJDODescriptor extends org.exolab.castor.mapping.loader.ClassDescriptorImpl {


      //----------------/
     //- Constructors -/
    //----------------/

    public HouseJDODescriptor() {
        super();
        ClassMapping mapping = new ClassMapping();
        ClassChoice choice = new ClassChoice();
        MapTo mapTo = new MapTo();

        addNature(ClassDescriptorJDONature.class.getName());
        ClassDescriptorJDONature jdoNature = new ClassDescriptorJDONature(this);
        jdoNature.setTableName("test2996_onetomany_house");
        setJavaClass(House.class);
        jdoNature.setAccessMode(AccessMode.valueOf("shared"));
        jdoNature.addCacheParam("name", "org.castor.cpa.test.test2996.onetomany.House");

        mapping.setAccess(ClassMappingAccessType.fromValue("shared"));
        mapping.setAutoComplete(true);
        mapping.setName("org.castor.cpa.test.test2996.onetomany.House");
        mapping.setClassChoice(choice);
        mapTo.setTable("test2996_onetomany_house");
        mapping.setMapTo(mapTo);
        setMapping(mapping);

        //id field
        String idFieldName = "id";
        FieldDescriptorImpl idFieldDescr;
        FieldMapping idFM = new FieldMapping();
        TypeInfo idType = new TypeInfo(java.lang.Long.class);
        // Set columns required (= not null)
        idType.setRequired(true);

        FieldHandler idHandler;
        try {
            Method idGetMethod = House.class.getMethod("getId", null);
            Method idSetMethod = House.class.getMethod("setId", new Class[]{
                long.class});

            idHandler = new FieldHandlerImpl(idFieldName, null, null,
                idGetMethod, idSetMethod, idType);

        } catch (SecurityException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (MappingException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (NoSuchMethodException e1) {
            throw new RuntimeException(e1.getMessage());
        }
        // Instantiate id field descriptor
        idFieldDescr = new FieldDescriptorImpl(idFieldName, idType,idHandler, false);
        idFieldDescr.addNature(FieldDescriptorJDONature.class.getName());
        FieldDescriptorJDONature idFieldJdoNature = new FieldDescriptorJDONature(idFieldDescr);
        idFieldJdoNature.setSQLName(new String[] { "id" });
        idFieldJdoNature.setSQLType(new int[] {SQLTypeInfos.javaType2sqlTypeNum(java.lang.Long.class) });
        idFieldJdoNature.setManyTable(null);
        idFieldJdoNature.setManyKey(new String[] {});
        idFieldJdoNature.setDirtyCheck(false);
        idFieldJdoNature.setReadOnly(false);

        idFieldDescr.setContainingClassDescriptor(this);
        idFieldDescr.setIdentity(true);
        idFM.setIdentity(true);
        idFM.setDirect(false);
        idFM.setName("id");
        idFM.setRequired(true);
        idFM.setSetMethod("setId");
        idFM.setGetMethod("getId");
        Sql idSql = new Sql();
        idSql.addName("id");
        idSql.setType("integer");
        idFM.setSql(idSql);
        idFM.setType("long");
        choice.addFieldMapping(idFM);

        //flats field
        String flatsFieldName = "flats";
        String flatsSqlName = "house_id";
        FieldDescriptorImpl flatsFieldDescr;
        FieldMapping flatsFM = new FieldMapping();
        TypeInfo flatsType = new TypeInfo(org.castor.cpa.test.test2996.onetomany.Flat.class);
        // Set columns required (= not null)
        flatsType.setRequired(true);

        FieldHandler flatsHandler;
        try {
            Method flatsGetMethod = House.class.getMethod("getFlats", null);
            Method flatsSetMethod = House.class.getMethod("setFlats", new Class[]{
                org.castor.cpa.test.test2996.onetomany.Flat[].class});

            flatsHandler = new FieldHandlerImpl(flatsFieldName, null, null,
                flatsGetMethod, flatsSetMethod, flatsType);

        } catch (SecurityException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (MappingException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (NoSuchMethodException e1) {
            throw new RuntimeException(e1.getMessage());
        }
        // Instantiate flats field descriptor
        flatsFieldDescr = new FieldDescriptorImpl(flatsFieldName, flatsType,flatsHandler, false);
        flatsFieldDescr.addNature(FieldDescriptorJDONature.class.getName());
        FieldDescriptorJDONature flatsFieldJdoNature = new FieldDescriptorJDONature(flatsFieldDescr);
        flatsFieldJdoNature.setSQLName(null);
        flatsFieldJdoNature.setSQLType(new int[] {SQLTypeInfos.javaType2sqlTypeNum(org.castor.cpa.test.test2996.onetomany.Flat.class) });
        flatsFieldJdoNature.setManyKey(new String[] { flatsSqlName });
        flatsFieldJdoNature.setDirtyCheck(false);
        flatsFieldJdoNature.setReadOnly(false);

        flatsFieldDescr.setContainingClassDescriptor(this);
        flatsFieldDescr.setClassDescriptor(new FlatJDODescriptor());
        flatsFieldDescr.setMultivalued(true);
        flatsFM.setIdentity(false);
        flatsFM.setDirect(false);
        flatsFM.setName("flats");
        flatsFM.setRequired(true);
        flatsFM.setCollection(FieldMappingCollectionType.ARRAY);
        Sql flatsSql = new Sql();
        flatsSql.addName("house_id");
        flatsSql.setManyKey(new String[] {"house_id"});
        flatsFM.setSql(flatsSql);
        flatsFM.setType("org.castor.cpa.test.test2996.onetomany.Flat");
        choice.addFieldMapping(flatsFM);

        setFields(new FieldDescriptor[] {flatsFieldDescr});
        setIdentities(new FieldDescriptor[] {idFieldDescr});
    }

}
