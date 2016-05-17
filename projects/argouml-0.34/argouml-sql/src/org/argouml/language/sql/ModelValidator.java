/* $Id: ModelValidator.java 187 2010-01-13 17:41:03Z linus $
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tfmorris
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.argouml.model.Model;

// TODO Use Translator for error messages
/**
 * Class that validates an UML model to be a valid relational model.
 */
class ModelValidator {
    private Map<String, Object> associationForName = 
    	new HashMap<String, Object>();

    private Map<Object, Object> fkAttrForAssoc = new HashMap<Object, Object>();

    private List<String> problems;

    /**
     * Default constructor.
     */
    public ModelValidator() {
    }

    /**
     * Validate the specified elements.
     * 
     * @param elements
     *            The elements to validate.
     * @return A list of problems found by validation. If there are no problems
     *         the returned list is empty.
     */
    public List<String> validate(Collection elements) {
        problems = new ArrayList<String>();

        for (Iterator it = elements.iterator(); it.hasNext();) {
            Object relation = it.next();
            if (Model.getFacade().isAClass(relation)
                    && !Model.getFacade().isAAssociationClass(relation)) {
                validateRelation(relation);
            }
        }

        Set<Entry<String, Object>> entries = associationForName.entrySet();
        for (Entry<String, Object> entry : entries) {
            String assocName = (String) entry.getKey();
            Object association = entry.getValue();
            Object fkAttribute = fkAttrForAssoc.get(association);
            if (fkAttribute == null) {
                problems.add("Foreign key attribute missing for association "
                        + assocName);
            }
        }

        return problems;
    }

    private void validateRelation(Object relation) {
        validatePrimaryKey(relation);
        validateFkAttributes(relation);
        validateAssociations(relation);
    }

    private void validateFkAttributes(Object relation) {
        Collection attributes = Model.getFacade().getAttributes(relation);
        for (Iterator it = attributes.iterator(); it.hasNext();) {
            Object attribute = it.next();
            if (Utils.isFk(attribute)) {
                validateFkAttribute(relation, attribute);
            }
        }
    }

    /**
     * Checks if every relation has a primary key. (rule 1)
     * 
     * @param relation
     *            The relation to validate.
     */
    private void validatePrimaryKey(Object relation) {
        List attributes = Model.getFacade().getAttributes(relation);
        Iterator it = attributes.iterator();
        boolean valid = false;
        while (it.hasNext()) {
            Object attribute = it.next();
            if (Utils.isPk(attribute)) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            problems.add("Primary key missing for "
                    + Model.getFacade().getName(relation));
        }
    }

    /**
     * Checks if a foreign key attribute is referencing an association. Further
     * checks if this foreign key attribute is referencing an attribute in
     * another relation. Checks rules 2 to 6.
     * 
     * @param relation
     * @param attribute
     */
    private void validateFkAttribute(Object relation, Object attribute) {
        String relName = Model.getFacade().getName(relation);
        String attrName = Model.getFacade().getName(attribute);
        String assocName = Model.getFacade().getTaggedValueValue(attribute,
                GeneratorSql.ASSOCIATION_NAME_TAGGED_VALUE);

        Object association = Utils.getAssociationForName(relation, assocName);
        if (association == null) {
            problems.add("association named '" + assocName + "' for relation "
                    + Model.getFacade().getName(relation) + " not found");
        } else {
            fkAttrForAssoc.put(association, attribute);

            Object relationAssocEnd = Model.getFacade().getAssociationEnd(
                    relation, association);
            Collection otherAssocEnds = Model.getFacade()
                    .getOtherAssociationEnds(relationAssocEnd);

            if (otherAssocEnds.size() == 1) {
                Object otherAssocEnd = otherAssocEnds.iterator().next();
                Object otherRelation = Model.getFacade().getClassifier(
                        otherAssocEnd);

                Object srcAttr = Utils.getSourceAttribute(attribute,
                        otherRelation);
                if (srcAttr == null) {
                    problems.add("fk attribute " + relName + "." + attrName
                            + " does not reference " + " an attribute in "
                            + Model.getFacade().getName(otherRelation));
                }

                int otherUpper = Model.getFacade().getUpper(otherAssocEnd);
                if (otherUpper != 1) {
                    problems.add("foreign key attribute " + relName + "."
                            + attrName
                            + " cannot be used to reference multiple "
                            + Model.getFacade().getName(otherRelation));
                }

                int otherLower = Model.getFacade().getLower(otherAssocEnd);
                validateFkConsistence(relation, attribute, otherLower);
            }
        }
    }

    /**
     * <p>
     * Checks if the <code>foreignKey</code> is of a stereotype NULL/NOT NULL
     * and if it conflicts with the multiplicity of the association end. A
     * conflict results from one of these constellations:
     * <ol>
     * <li>attribute is of stereotype NOT NULL, the corresponding association
     * end multiplicity is 0..1
     * <li>attribute is of stereotype NULL, the corresponding association end
     * multiplicity is 1
     * </ol>
     * <p>
     * If attribute is none of these two stereotypes there is no conflict.
     * <p>
     * Checks rules 5 and 6.
     * 
     * @param fkAttribute
     *            The foreign key attribute to check
     * @param relation
     *            The relatoin the foreign key should refer to
     * @param lowerBound
     *            The lower multiplicity of the corresponding association end
     */
    private void validateFkConsistence(Object relation, Object fkAttribute,
            int lowerBound) {
        String entName = Model.getFacade().getName(relation);
        String attrName = Model.getFacade().getName(fkAttribute);

        if (Utils.isNull(fkAttribute) && lowerBound == 1) {
            problems.add("conflict in " + entName + "." + attrName + ": "
                    + "attribute is nullable and association lower bound "
                    + "is one");
        } else if (Utils.isNotNull(fkAttribute) && lowerBound == 0) {
            problems.add("conflict in " + entName + "." + attrName + ": "
                    + "attribute is not nullable and association lower "
                    + "bound is zero");
        }
    }

    /**
     * Validate every association for the given relation.
     * 
     * @param relation
     */
    private void validateAssociations(Object relation) {
        Collection associationEnds = Model.getFacade().getAssociationEnds(
                relation);
        Iterator it = associationEnds.iterator();
        while (it.hasNext()) {
            Object relationAssocEnd = it.next();
            Object association = Model.getFacade().getAssociation(
                    relationAssocEnd);
            validateAssociation(association);
        }
    }

    private Set validatedAssociations = new HashSet();

    /**
     * Validate the specified association. The association needs to have a
     * unique name, must be binary and at most 1:n. And there must exist a
     * foreign key attribute for an association.
     * 
     * @param association
     */
    private void validateAssociation(Object association) {
        if (validatedAssociations.contains(association)) {
            return;
        }

        validatedAssociations.add(association);

        String assocName = Model.getFacade().getName(association);
        if (associationForName.containsKey(assocName)) {
            problems.add("Association name " + assocName
                    + " found more than once");
        } else {
            associationForName.put(assocName, association);

            Collection assocEnds = Model.getFacade()
                    .getConnections(association);
            if (assocEnds.size() != 2) {
                problems.add("Association " + assocName + " is not binary");
            } else {
                Iterator it = assocEnds.iterator();

                Object assocEnd1 = it.next();
                Object assocEnd2 = it.next();

                int end1Upper = Model.getFacade().getUpper(assocEnd1);
                int end2Upper = Model.getFacade().getUpper(assocEnd2);

                if (end1Upper != 1 && end2Upper != 1) {
                    problems.add("Association " + assocName + " is n:m (not "
                            + "allowed in a relational data model)");
                }
            }
        }
    }
}
