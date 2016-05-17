/* $Id: GeneratorSql.java 263 2011-03-30 15:15:58Z thn $
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

// Copyright (c) 2006-2008 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.argouml.application.api.Argo;
import org.argouml.configuration.Configuration;
import org.argouml.model.Model;
import org.argouml.ui.ExceptionDialog;
import org.argouml.ui.ProjectBrowser;
import org.argouml.ui.SelectCodeCreatorDialog;
import org.argouml.uml.generator.CodeGenerator;
import org.argouml.uml.generator.SourceUnit;
import org.argouml.uml.generator.TempFileUtils;

/**
 * SQL generator.
 */
public final class GeneratorSql implements CodeGenerator {
    /**
     * Constant representing the system dependent line separator.
     */
    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Constant representing the attribute stereotype (as <code>String</code>)
     * that will be used to recognize a primary key attribute.
     */
    static final String PRIMARY_KEY_STEREOTYPE = "PK";

    /**
     * Constant representing the attribute stereotype (as <code>String</code>)
     * that will be used to recognize a foreign key attribute.
     */
    static final String FOREIGN_KEY_STEREOTYPE = "FK";

    /**
     * Constant representing the attribute stereotype (as <code>String</code>)
     * that will be used to recognize an attribute to be not nullable.
     */
    static final String NOT_NULL_STEREOTYPE = "NOT_NULL";

    /**
     * Constant representing the attribute stereotype (as <code>String</code>)
     * that will be used to recognize an attribute to be nullable.
     */
    static final String NULL_STEREOTYPE = "NULL";

    /**
     * Constant representing the attribute tagged value (as <code>String</code>)
     * that will be used to recognize what column a foreign key attribute is
     * referencing to.
     */
    static final String SOURCE_COLUMN_TAGGED_VALUE = "source_column";

    /**
     * Constant representing the attribute tagged value (as <code>String</code>)
     * that will be used to recognize what association a foreign key attribute
     * is referencing to.
     */
    static final String ASSOCIATION_NAME_TAGGED_VALUE = "association_name";

    private static final Logger LOG = Logger.getLogger(GeneratorSql.class);

    /**
     * The instances.
     */
    private static final GeneratorSql INSTANCE = new GeneratorSql();

    private DomainMapper domainMapper;

    private SqlCodeCreator sqlCodeCreator;

    private List<SqlCodeCreator> sqlCodeCreators;

    /**
     * Constructor.
     */
    private GeneratorSql() {
        domainMapper = new DomainMapper();
    }

	private List<SqlCodeCreator> loadSqlCodeCreators() {
		List<SqlCodeCreator> result = new ArrayList<SqlCodeCreator>();

        // URL url = getClass().getResource("GeneratorSql.class");
        // String extForm = url.toExternalForm();
        //
        // if (extForm.startsWith("file:")) {
        // String className = getClass().getName();
        // ... minus 7 because of length of ".class" and the trailing "/"
        // extForm = extForm.substring(0, extForm.length()
        // - className.length() - 7);
        // }

        SqlCreatorLoader el = new SqlCreatorLoader();

		// URI uri = new URI(extForm);
		// Collection classes = el.getLoadableClassesFromUri(uri,
		// SqlCodeCreator.class);
		Collection<Class<SqlCodeCreator>> classes = el.getCodeCreators();
		for (Class<SqlCodeCreator> c : classes) {
			try {
				SqlCodeCreator scc = c.newInstance();
				result.add(scc);
				// } catch (URISyntaxException e) {
				// LOG.error("Exception", e);
			} catch (InstantiationException e) {
				LOG.error("Exception while instantiating a SqlCodeCreator "
						+ c.getName(), e);
			} catch (IllegalAccessException e) {
				LOG.error("Exception while accessing the constructor of a "
						+ "SqlCodeCreator " + c.getName(), e);
			}
		}
        return result;
	}

    /**
     * @return the singleton instance.
     */
    public static GeneratorSql getInstance() {
        return INSTANCE;
    }

    /**
     * Generate code for the specified classifiers. If generation of
     * dependencies is requested, then every file the specified elements depends
     * on is generated too (e.g. if the class MyClass has an attribute of type
     * OtherClass, then files for OtherClass are generated too).
     * 
     * @param elements
     *            the UML model elements to generate code for.
     * @param deps
     *            Recursively generate dependency files too.
     * @return A collection of {@link org.argouml.uml.generator.SourceUnit}
     *         objects. The collection may be empty if no file is generated.
     * @see org.argouml.uml.generator.CodeGenerator#generate( Collection,
     *      boolean)
     */
    public Collection<SourceUnit> generate(Collection elements, boolean deps) {
        LOG.debug("generate() called");
        File tmpdir = null;
        try {
            tmpdir = TempFileUtils.createTempDir();
            if (tmpdir != null) {
                generateFiles(elements, tmpdir.getPath(), deps);
                return TempFileUtils.readAllFiles(tmpdir);
            }
            return Collections.emptyList();
        } finally {
            if (tmpdir != null) {
                TempFileUtils.deleteDir(tmpdir);
            }
            LOG.debug("generate() terminated");
        }
    }

    private TableDefinition getTableDefinition(Object element) {
        TableDefinition tableDefinition = new TableDefinition();
        tableDefinition.setName(Model.getFacade().getName(element));

        for (Object attribute : Model.getFacade().getAttributes(element)) {
            String name = Model.getFacade().getName(attribute);

            ColumnDefinition cd = new ColumnDefinition();
            cd.setName(name);

            Object domain = Model.getFacade().getType(attribute);
            String domainName = Model.getFacade().getName(domain);
            String datatype = domainMapper.getDatatype(sqlCodeCreator
                    .getClass(), domainName);
            cd.setDatatype(datatype);

            if (Utils.isNull(attribute)) {
                cd.setNullable(Boolean.TRUE);
            } else if (Utils.isNotNull(attribute)) {
                cd.setNullable(Boolean.FALSE);
            } else {
                cd.setNullable(null);
            }

            tableDefinition.addColumnDefinition(cd);

            if (Utils.isPk(attribute)) {
                cd.setNullable(Boolean.FALSE);
                tableDefinition.addPrimaryKeyField(name);
            }
        }

        return tableDefinition;
    }

    private void setNullable(TableDefinition tableDef, List<String> columnNames,
            boolean nullable) {
        for (String name : columnNames) {
            tableDef.getColumnDefinition(name).setNullable(
                    Boolean.valueOf(nullable));
        }
    }

    private String generateCode(Collection elements) {
        tableDefinitions = new HashMap<Object, TableDefinition>();
        foreignKeyDefinitions = new ArrayList<ForeignKeyDefinition>();

        for (Object element : elements) {
            if (Model.getFacade().isAClass(element)) {
                TableDefinition tableDef = getTableDefinition(element);
                tableDefinitions.put(element, tableDef);
            }
        }

        if (elements.size() > 1) {
            for (Object element : elements) {
                Collection<ForeignKeyDefinition> fkDefs = 
                	getForeignKeyDefinitions(element);
                TableDefinition tableDef = tableDefinitions.get(element);
                for (ForeignKeyDefinition fkDef : fkDefs) {
                    if (fkDef.getReferencesLower() == 0) {
                        setNullable(tableDef, fkDef.getColumnNames(), true);
                    } else {
                        setNullable(tableDef, fkDef.getColumnNames(), false);
                    }
                }
                foreignKeyDefinitions.addAll(fkDefs);
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append("-- Table definitions").append(LINE_SEPARATOR);
        for (TableDefinition tableDef : tableDefinitions.values()) {
            sb.append(sqlCodeCreator.createTable(tableDef));
        }

        if (elements.size() > 1) {
            sb.append("-- Foreign key definitions").append(LINE_SEPARATOR);
            for (ForeignKeyDefinition fkDef : foreignKeyDefinitions) {
                sb.append(sqlCodeCreator.createForeignKey(fkDef));
            }
        }

        return sb.toString();
    }

    private static final String SCRIPT_FILENAME = "script.sql";

    private Map<Object, TableDefinition> tableDefinitions;

    private List<ForeignKeyDefinition> foreignKeyDefinitions;

    /**
     * Generate files for the specified classifiers.
     * 
     * @see #generate(Collection, boolean)
     * @param elements
     *            the UML model elements to generate code for.
     * @param path
     *            The source base path.
     * @param deps
     *            Recursively generate dependency files too.
     * @return The filenames (with relative path) as a collection of Strings.
     *         The collection may be empty if no file will be generated.
     * @see org.argouml.uml.generator.CodeGenerator#generateFiles( Collection,
     *      String, boolean)
     */
    public Collection<String> generateFiles(Collection elements, String path,
            boolean deps) {
        String filename = SCRIPT_FILENAME;
        if (!path.endsWith(FILE_SEPARATOR)) {
            path += FILE_SEPARATOR;
        }

        Collection<String> result = new ArrayList<String>();
        String fullFilename = path + filename;

        if (!elements.isEmpty()) {
            LOG.debug("validating model");
            ModelValidator validator = new ModelValidator();
            List<String> problems = validator.validate(elements);
            if (problems.size() > 0) {
                LOG.debug("model not valid, exiting code generation");
                String error = Utils.stringsToString(problems, LINE_SEPARATOR);

                ExceptionDialog ed = new ExceptionDialog(ProjectBrowser
                        .getInstance(), "Error in model", "Model not valid", error);
                ed.setModal(true);
                ed.setVisible(true);
            } else if (SelectCodeCreatorDialog.execute()) {
                String code = generateCode(elements);
                writeFile(fullFilename, code);
                result.add(fullFilename);
            }
        }
        return result;
    }

    private void writeFile(String filename, String content) {
        BufferedWriter fos = null;
        try {
            String inputSrcEnc = Configuration
                    .getString(Argo.KEY_INPUT_SOURCE_ENCODING);
            if (inputSrcEnc == null || inputSrcEnc.trim().equals("")) {
                inputSrcEnc = System.getProperty("file.encoding");
            }
            fos = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename), inputSrcEnc));
            fos.write(content);
        } catch (IOException e) {
            LOG.error("IO Exception: " + e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LOG.error("FAILED: " + filename);
            }
        }
    }

    private Collection<ForeignKeyDefinition> getForeignKeyDefinitions(Object relation) {
        Collection<ForeignKeyDefinition> fkDefs = 
        	new HashSet<ForeignKeyDefinition>();

        for (Object assocEnd : Model.getFacade().getAssociationEnds(relation)) {

            Collection otherAssocEnds = Model.getFacade()
                    .getOtherAssociationEnds(assocEnd);
            Object otherAssocEnd = otherAssocEnds.iterator().next();

            ForeignKeyDefinition fkDef = getFkDef(relation, assocEnd,
                    otherAssocEnd);
            if (fkDef != null) {
                fkDefs.add(fkDef);
            }
        }

        return fkDefs;
    }

    private ForeignKeyDefinition getFkDef(Object relation, Object assocEnd,
            Object otherAssocEnd) {
        Object assoc = Model.getFacade().getAssociation(assocEnd);
        List fkAttributes = Utils.getFkAttributes(relation, assoc);
        int otherUpper = Model.getFacade().getUpper(otherAssocEnd);
        if (otherUpper != 1 || fkAttributes.size() == 0) {
            return null;
        }
        ForeignKeyDefinition fkDef = new ForeignKeyDefinition();

        List<Object> srcAttributes = new ArrayList<Object>();

        Object srcRelation = Model.getFacade().getClassifier(otherAssocEnd);
        for (Object fkAttr : fkAttributes) {
            Object srcAttr = Utils.getSourceAttribute(fkAttr, srcRelation);
            srcAttributes.add(srcAttr);
        }

        // List colNames = new ArrayList();
        TableDefinition tableDef = tableDefinitions
                .get(relation);
        fkDef.setTable(tableDef);
        for (Object fkAttr : fkAttributes) {
            // colNames.add(Model.getFacade().getName(fkAttr));

            ColumnDefinition colDef = tableDef.getColumnDefinition(Model
                    .getFacade().getName(fkAttr));
            fkDef.addColumnDefinition(colDef);
        }

        // List refColNames = new ArrayList();
        tableDef = tableDefinitions.get(srcRelation);
        fkDef.setReferencesTable(tableDef);
        for (Object srcAttr : srcAttributes) {
            // refColNames.add(Model.getFacade().getName(srcAttr));

            ColumnDefinition colDef = tableDef.getColumnDefinition(Model
                    .getFacade().getName(srcAttr));
            fkDef.addReferencesColumn(colDef);
        }

        int lower = Model.getFacade().getLower(assocEnd);
        int upper = Model.getFacade().getUpper(assocEnd);
        int refLower = Model.getFacade().getLower(otherAssocEnd);
        int refUpper = Model.getFacade().getUpper(otherAssocEnd);

        fkDef.setForeignKeyName(Model.getFacade().getName(assoc));

        // fkDef.setTableName(Model.getFacade().getName(relation));
        // fkDef.setColumnNames(colNames);

        // fkDef.setReferencesTableName(Model.getFacade().getName(srcRelation));
        // fkDef.setReferencesColumnNames(refColNames);

        fkDef.setLower(lower);
        fkDef.setUpper(upper);
        fkDef.setReferencesLower(refLower);
        fkDef.setReferencesUpper(refUpper);

        return fkDef;
    }

    /**
     * Returns a list of files that will be generated from the specified
     * modelelements.
     * 
     * @see #generate(Collection, boolean)
     * @param elements
     *            the UML model elements to generate code for.
     * @param deps
     *            Recursively generate dependency files too.
     * @return The filenames (with relative path) as a collection of Strings.
     *         The collection may be empty if no file will be generated.
     * @see org.argouml.uml.generator.CodeGenerator#generateFileList(
     *      Collection, boolean)
     */
    public Collection<String> generateFileList(Collection elements, 
    		boolean deps) {
		Collection<String> c = new HashSet<String>();
		c.add(SCRIPT_FILENAME);
		return c;
	}

    /**
     * @return A <code>List</code> of all code creators known to this class.
     */
    public synchronized List<SqlCodeCreator> getSqlCodeCreators() {
    	if (sqlCodeCreators == null) {
    		sqlCodeCreators = loadSqlCodeCreators();
    	}
        return sqlCodeCreators;
    }

    /**
     * @return The {@link DomainMapper} class responsible for mappings of
     *         domains to datatypes.
     */
    public DomainMapper getDomainMapper() {
        return domainMapper;
    }

    /**
     * Set a {@link SqlCodeCreator} to be the one to generate code.
     * 
     * @param sqlCodeCreator
     *            The {@link SqlCodeCreator} that should be used to generate DDL
     *            statements.
     */
    public void setSqlCodeCreator(SqlCodeCreator sqlCodeCreator) {
        this.sqlCodeCreator = sqlCodeCreator;
    }
} /* end class GeneratorSql */
