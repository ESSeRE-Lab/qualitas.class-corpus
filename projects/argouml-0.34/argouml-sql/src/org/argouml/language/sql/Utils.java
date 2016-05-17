/* $Id: Utils.java 187 2010-01-13 17:41:03Z linus $
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    drahmann
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.language.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.argouml.model.Facade;
import org.argouml.model.Model;

final class Utils {
    /**
     * Return an association named <code>assocName</code> that is connected to
     * relation.
     * 
     * @param relation
     *            The relation where to search the association.
     * @param assocName
     *            The name of the association to search.
     * @return The association if found, <code>null</code> else.
     */
    public static Object getAssociationForName(Object relation, String assocName) {
        Object association = null;

        Collection assocEnds = Model.getFacade().getAssociationEnds(relation);
        for (Iterator it = assocEnds.iterator(); it.hasNext();) {
            Object assocEnd = it.next();
            Object assoc = Model.getFacade().getAssociation(assocEnd);
            String name = Model.getFacade().getName(assoc);
            if (name.equals(assocName)) {
                association = assoc;
            }
        }

        return association;
    }

    /**
     * Search the attribute named <code>attributeName</code> in the given
     * relation. If there exist more than one attribute the first one is
     * returned.
     * 
     * @param relation
     *            The relation in which to search the attribute.
     * @param attributeName
     *            The name of the attribute to search.
     * @return The attribute if found, <code>null</code> else.
     */
    public static Object getAttributeForName(Object relation,
            String attributeName) {
        Object attribute = null;

        Collection attributes = Model.getFacade().getAttributes(relation);
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            Object attr = it.next();
            if (Model.getFacade().getName(attr).equals(attributeName)) {
                attribute = attr;
                break;
            }
        }

        return attribute;
    }

    /**
     * Build a list of all foreign key attributes that refer a specific
     * association.
     * 
     * @param relation
     *            The relation which contains the fk-attributes.
     * @param association
     *            The association for which to return the fk-attributes.
     * @return A list of all attributes. If there is no attribute, an empty list
     *         is returned.
     */
    public static List getFkAttributes(Object relation, Object association) {
        String assocName = Model.getFacade().getName(association);

        Collection attributes = Model.getFacade().getAttributes(relation);
        Iterator it = attributes.iterator();
        List fkAttributes = new ArrayList();
        while (it.hasNext()) {
            Object attribute = it.next();
            String s = Model.getFacade().getTaggedValueValue(attribute,
                    GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE);

            if (s.equals(assocName)) {
                fkAttributes.add(attribute);
            }
        }

        return fkAttributes;
    }

    /**
     * Build a list of all primary key attributes of entity.
     * 
     * @param relation
     *            The relation for which to return the pk-attributes.
     * @return A list of all primary key attributes. If there is no
     *         pk-attribute, the list is empty.
     */
    public static List getPrimaryKeyAttributes(Object relation) {
        List result = new ArrayList();

        Collection attributes = Model.getFacade().getAttributes(relation);

        for (Iterator it = attributes.iterator(); it.hasNext();) {
            Object attribute = it.next();
            if (isPk(attribute)) {
                result.add(attribute);
            }
        }

        return result;
    }

    /**
     * Returns if an attribute is a foreign key attribute. Effectively checks if
     * the attribute is of stereotype
     * {@link GeneratorSql#FOREIGN_KEY_STEREOTYPE}.
     * 
     * @param attribute
     *            The attribute to check.
     * @return <code>true</code> if it is a fk-attribute, <code>false</code>
     *         else.
     * @see Facade#isStereotype(Object, String)
     */
    public static boolean isFk(Object attribute) {
        return Model.getFacade().isStereotype(attribute,
                GeneratorSql.FOREIGN_KEY_STEREOTYPE);
    }

    /**
     * Returns if an attribute is a primary key attribute. Effectively checks if
     * the attribute is of stereotype
     * {@link GeneratorSql#PRIMARY_KEY_STEREOTYPE}.
     * 
     * @param attribute
     *            The attribute to check.
     * @return <code>true</code> if it is a pk-attribute, <code>false</code>
     *         else.
     * @see Facade#isStereotype(Object, String)
     */
    public static boolean isPk(Object attribute) {
        return Model.getFacade().isStereotype(attribute,
                GeneratorSql.PRIMARY_KEY_STEREOTYPE);
    }

    /**
     * Returns if an attribute is not nullable. Effectively checks if the
     * attribute is of stereotype {@link GeneratorSql#NOT_NULL_STEREOTYPE}.
     * 
     * @param attribute
     *            The attribute to check.
     * @return <code>true</code> if it is not nullable, <code>false</code>
     *         else.
     * @see Facade#isStereotype(Object, String)
     */
    public static boolean isNotNull(Object attribute) {
        return Model.getFacade().isStereotype(attribute,
                GeneratorSql.NOT_NULL_STEREOTYPE);
    }

    /**
     * Returns if an attribute is nullable. Effectively checks if the attribute
     * is of stereotype {@link GeneratorSql#NULL_STEREOTYPE}.
     * 
     * @param attribute
     *            The attribute to check.
     * @return <code>true</code> if it is nullable, <code>false</code> else.
     * @see Facade#isStereotype(Object, String)
     */
    public static boolean isNull(Object attribute) {
        return Model.getFacade().isStereotype(attribute,
                GeneratorSql.NULL_STEREOTYPE);
    }

    /**
     * Get the attribute a foreign key attribute is referencing to. Returns
     * <code>null</code> if the source attribute cannot be determined.
     * 
     * @param fkAttribute
     *            The foreign key attribute.
     * @param srcRelation
     *            The entity the foreign key is referencing to.
     * @return The referenced attribute.
     */
    public static Object getSourceAttribute(Object fkAttribute,
            Object srcRelation) {
        String srcColName = Model.getFacade().getTaggedValueValue(fkAttribute,
                GeneratorSql.SOURCE_COLUMN_TAGGED_VALUE);
        Object srcAttr = null;
        if (srcColName.equals("")) {
            srcColName = Model.getFacade().getName(fkAttribute);
            srcAttr = Utils.getAttributeForName(srcRelation, srcColName);
            if (srcAttr == null) {
                Collection pkAttrs = Utils.getPrimaryKeyAttributes(srcRelation);
                if (pkAttrs.size() == 1) {
                    srcAttr = pkAttrs.iterator().next();
                }
            }
        } else {
            srcAttr = Utils.getAttributeForName(srcRelation, srcColName);
        }
        return srcAttr;
    }

    /**
     * Takes a list of strings and joins them with <code>separators</code>.
     * 
     * @param strings
     *            The list of strings to be joined.
     * @param separators
     *            The string that should be put between the separate strings.
     * @return The joined string.
     */
    public static String stringsToString(List strings, String separators) {
        StringBuffer sb = new StringBuffer();
        Iterator it = strings.iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            if (sb.length() > 0) {
                sb.append(separators);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * Takes a list of strings and joins them with a comma.
     * 
     * @param strings
     *            The list of strings.
     * @return The joined string.
     */
    public static String stringsToCommaString(List strings) {
        return stringsToString(strings, ",");
    }

    /**
     * Private constructor so no instance can be created.
     */
    private Utils() {

    }

    /**
     * Separates a <code>String</code> using the given delimiters and puts the
     * resulting tokens to a <code>List</code>.
     * 
     * @param string
     *            The string to separate.
     * @param delimiters
     *            The delimiters that should used to separate the string.
     * 
     * @return A <code>List</code> containing all string tokens.
     */
    public static List stringToStringList(String string, String delimiters) {
        StringTokenizer st = new StringTokenizer(string, delimiters);
        List result = new ArrayList();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

    /**
     * Separates a <code>String</code> using the standard
     * <code>StringTokenizer</code> delimiters and puts the resulting tokens
     * to a <code>List</code>.
     * 
     * @param string
     *            The string to separate.
     * 
     * @return A <code>List</code> containing all string tokens.
     */
    public static List stringToStringList(String string) {
        StringTokenizer st = new StringTokenizer(string);
        List result = new ArrayList();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

    /**
     * Get a short table name that can be used in triggers etc. Shortens the
     * name by first removing all vowels from the left to the right. If that's
     * not enough the resulting string is just truncated.
     * 
     * @param longName
     * @param maxLength
     * @return
     */
    public static String getShortName(String longName, int maxLength) {
        char[] vowels = new char[] { 'a', 'e', 'i', 'o', 'u' };
        String shortName = longName;
        while (shortName.length() > maxLength) {
            int index = -1;
            for (int i = 0; i < vowels.length; i++) {
                int lastIndex = shortName.lastIndexOf(vowels[i]);
                if (lastIndex > index) {
                    index = lastIndex;
                }
            }
            
            if (index == -1) {
                break;
            }
            
            String firstPart = shortName.substring(0, index);
            String lastPart = shortName.substring(index + 1, shortName.length());
            shortName = firstPart + lastPart;
        }
        if (shortName.length() > maxLength) {
            shortName = shortName.substring(0, maxLength);
        }
        return shortName;    
    }
    
    /**
     * The prefix in URL:s that are files.
     */
    private static final String FILE_PREFIX = "file:";

    /**
     * The prefix in URL:s that are jars.
     */
    private static final String JAR_PREFIX = "jar:";

    /**
     * Class file suffix.
     */
    public static final String CLASS_SUFFIX = ".class";

    public static String getModuleRoot() {
        // Use a little trick to find out where this module is being loaded
        // from. (Code was "stolen" from ModuleLoader2 and modified)
        String resName = Utils.class.getName();
        resName = "/" + resName.replace('.', '/') + CLASS_SUFFIX;
        String extForm = Utils.class.getResource(resName).toExternalForm();

        String moduleRoot = extForm.substring(0, extForm.length()
                - resName.length());

        // If it's a jar, clean it up and make it look like a file url
        if (moduleRoot.startsWith(JAR_PREFIX)) {
            moduleRoot = moduleRoot.substring(JAR_PREFIX.length());
            if (moduleRoot.endsWith("!")) {
                moduleRoot = moduleRoot.substring(0, moduleRoot.length() - 1);
            }
            int p = moduleRoot.lastIndexOf('/');
            moduleRoot = moduleRoot.substring(0, p);
        }

        if (moduleRoot.startsWith(FILE_PREFIX)) {
            moduleRoot = moduleRoot.substring(FILE_PREFIX.length());
        }

        return moduleRoot;
    }
}
