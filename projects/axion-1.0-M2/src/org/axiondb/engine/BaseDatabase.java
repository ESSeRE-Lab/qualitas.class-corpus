/*
 * $Id: BaseDatabase.java,v 1.34 2003/07/07 23:36:12 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002-2003 Axion Development Team.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The names "Tigris", "Axion", nor the names of its contributors may
 *    not be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * 4. Products derived from this software may not be called "Axion", nor
 *    may "Tigris" or "Axion" appear in their names without specific prior
 *    written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =======================================================================
 */

package org.axiondb.engine;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.axiondb.AxionException;
import org.axiondb.BinaryBranchWhereNode;
import org.axiondb.BindVariable;
import org.axiondb.Column;
import org.axiondb.ColumnIdentifier;
import org.axiondb.DataType;
import org.axiondb.DataTypeFactory;
import org.axiondb.Database;
import org.axiondb.FromNode;
import org.axiondb.FunctionFactory;
import org.axiondb.InWhereNode;
import org.axiondb.IndexFactory;
import org.axiondb.LeafWhereNode;
import org.axiondb.Literal;
import org.axiondb.NotWhereNode;
import org.axiondb.Row;
import org.axiondb.Selectable;
import org.axiondb.Sequence;
import org.axiondb.Table;
import org.axiondb.TableFactory;
import org.axiondb.TableIdentifier;
import org.axiondb.TransactionManager;
import org.axiondb.WhereNode;
import org.axiondb.event.DatabaseModificationListener;
import org.axiondb.event.DatabaseModifiedEvent;
import org.axiondb.event.DatabaseSequenceEvent;
import org.axiondb.event.DatabaseTypeEvent;
import org.axiondb.event.SequenceModificationListener;
import org.axiondb.event.TableModificationListener;
import org.axiondb.functions.AggregateFunction;
import org.axiondb.functions.ConcreteFunction;
import org.axiondb.functions.FunctionIdentifier;
import org.axiondb.types.BooleanType;
import org.axiondb.types.IntegerType;
import org.axiondb.types.ShortType;
import org.axiondb.types.StringType;

import org.axiondb.expression.Expression;
import org.axiondb.expression.ExpressionIdentifier;
/**
 * Abstract base {@link Database} implementation.
 * 
 * @version $Revision: 1.34 $ $Date: 2003/07/07 23:36:12 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 * @author Morgan Delagrange
 * @author James Strachan
 * @author Amrish Lal
 * @author Rahul Dwivedi
 */
public abstract class BaseDatabase implements Database {
    
    //------------------------------------------------------------ Constructors
    
    public BaseDatabase(String name) {
        _name = name;
    }

    //------------------------------------------------------------------ Public
    
    public String getName() {
        return _name;
    }

    public boolean isReadOnly() {
        return _readOnly;
    }

    public IndexFactory getIndexFactory(String name) {
        return (IndexFactory)(_indexTypes.get(name.toUpperCase()));
    }

    public TableFactory getTableFactory(String name) {
        return (TableFactory)(_tableTypes.get(name.toUpperCase()));
    }

    public DataType getDataType(String name) {
        return (DataType)(_dataTypes.get(name.toUpperCase()));
    }

    public Table getTable(String name) throws AxionException {
        String upName = name.toUpperCase();
        return (Table)(_tables.get(upName));
    }

    public Table getTable(TableIdentifier table) throws AxionException {
        return (Table)(_tables.get(table.getTableName()));
    }

    public void dropTable(String name) throws AxionException {
        String upName = name.toUpperCase();
        if(_tables.containsKey(upName)) {
            Table table = (Table)(_tables.remove(upName));
            Iterator i = getDatabaseModificationListeners().iterator();
            while (i.hasNext()) {
                DatabaseModificationListener cur = (DatabaseModificationListener)i.next();
                cur.tableDropped(new DatabaseModifiedEvent(table));
            }            
            table.drop();
        } else {
            throw new AxionException("No table " + upName + " found");
        }
    }

    public void tableAltered(Table t) throws AxionException {
        Iterator i = getDatabaseModificationListeners().iterator();
        while (i.hasNext()) {
            DatabaseModificationListener cur = (DatabaseModificationListener)i.next();
            DatabaseModifiedEvent e = new DatabaseModifiedEvent(t);
            cur.tableDropped(e);
            cur.tableAdded(e);
        }            
    }
    
    public void addTable(Table t) throws AxionException {
        if(t != null) {
            String upName = t.getName().toUpperCase();
            if(_tables.containsKey(upName)) {
                throw new AxionException("A table named " + t.getName() + " already exists.");
            } else {
                _tables.put(upName, t);
                t.addTableModificationListener((TableModificationListener)_colUpd);
                Iterator i = getDatabaseModificationListeners().iterator();
                while (i.hasNext()) {
                    DatabaseModificationListener cur = (DatabaseModificationListener)i.next();
                    cur.tableAdded(new DatabaseModifiedEvent(t));
                }
            }
        }
    }

    public void shutdown() throws AxionException {        
        for(Iterator tables = _tables.values().iterator();tables.hasNext();) {
            Table table = (Table)(tables.next());
            table.shutdown();
        }
        Databases.forgetDatabase(getName());
    }

    public void remount(File newdir) throws AxionException {
        for(Iterator tables = _tables.values().iterator();tables.hasNext();) {
            Table table = (Table)(tables.next());
            table.remount(new File(newdir,table.getName()),false);
        }
    }

    public void resolveFromNode(FromNode node, TableIdentifier[] tables) throws AxionException {
        if (node == null) {
            return;
        }
        Object left = node.getLeft();
        Object right = node.getRight();
        
        WhereNode whereNode = node.getCondition();
        resolveWhereNode(whereNode, node.toTableArray());
        if (left instanceof FromNode) {
            FromNode childNode = (FromNode) left;
            resolveFromNode(childNode, childNode.toTableArray());
        }

        if (right instanceof FromNode) {
            FromNode childNode = (FromNode) right;
            resolveFromNode(childNode, childNode.toTableArray());
        }        
    }

    /**
     * Resolves all {@link Selectable}s within the given {@link
     * WhereNode} tree, relative to the given {@link Database} and
     * {@link TableIdentifier tables}.
     */
    public void resolveWhereNode(WhereNode node, TableIdentifier[] tables) throws AxionException {
        if(null == node) {
            return;
        } else if(node instanceof BinaryBranchWhereNode) {
            BinaryBranchWhereNode branch = (BinaryBranchWhereNode)node;
            resolveWhereNode(branch.getLeft(),tables);
            resolveWhereNode(branch.getRight(),tables);
        } else if(node instanceof NotWhereNode) {
            NotWhereNode nnode = (NotWhereNode)node;
            resolveWhereNode(nnode.getChild(),tables);
        } else if(node instanceof LeafWhereNode) {
            LeafWhereNode leaf = (LeafWhereNode)node;
            if(null != leaf.getLeft()) {
                Selectable lsel = resolveSelectable(leaf.getLeft(), tables);
                if (lsel instanceof AggregateFunction) {
                    throw new AxionException("Use of aggregate functions in expression is not allowed.");
                }
                leaf.setLeft(lsel);
            }

            if(null != leaf.getRight()) {
                Selectable rsel = resolveSelectable(leaf.getRight(), tables);
                if (rsel instanceof AggregateFunction) {
                    throw new AxionException("use of aggregate function in expression is not allowed.");
                } 
                leaf.setRight(rsel);
            }
        } else if(node instanceof InWhereNode) {
            InWhereNode in = (InWhereNode)node;
            if(null != in.getLeft()) {
                in.setLeft(resolveSelectable(in.getLeft(),tables));
            }
        } else {
            throw new AxionException("Unrecognized WhereNode " + node);
        }
    }

    public Selectable resolveSelectable(Selectable selectable, TableIdentifier[] tables) throws AxionException {
        if(selectable instanceof ColumnIdentifier) {
            return resolveColumn((ColumnIdentifier)selectable,tables);
        } else if(selectable instanceof FunctionIdentifier) {
            return resolveFunctionIdentifier((FunctionIdentifier)selectable,tables);
        } else if(selectable instanceof ConcreteFunction) {
            return selectable;
        } else if(selectable instanceof BindVariable) {
            BindVariable bvar = (BindVariable)selectable;
            if(!bvar.isBound()) {
                throw new AxionException("Unbound variable found.");
            } else {
                return selectable;
            }
        } else if(selectable instanceof Literal) {
            return selectable;
        } else if(selectable instanceof  ExpressionIdentifier) { // rahul added for evaluating expression 
            return resolveExpression((ExpressionIdentifier)selectable,tables);
        } else if (selectable instanceof WhereNode) {
            resolveWhereNode((WhereNode)selectable, tables);
            return (selectable);
        } else {
            throw new AxionException("Couldn't resolve Selectable " + selectable);
        }
    }

    public void checkpoint() throws AxionException {
        Iterator tables = _tables.values().iterator();
        while(tables.hasNext()) {
            Table table = (Table)(tables.next());
            table.checkpoint();
        }
    }

    public void createSequence(Sequence seq) throws AxionException {
        if (seq != null) {
            DatabaseSequenceEvent e = new DatabaseSequenceEvent(seq);
            Iterator i = getDatabaseModificationListeners().iterator();
            while (i.hasNext()) {
                DatabaseModificationListener cur = (DatabaseModificationListener)i.next();
                cur.sequenceAdded(e);
            }            
            _sequences.put(seq.getName(), seq);
            seq.addSequenceModificationListener((SequenceModificationListener)_seqUpd);
        }
    }

    public void dropSequence(String name) {
        String uName = name.toUpperCase();
        if (getSequence(uName) != null) {
            _sequences.remove(uName);
        }
    }

    public Sequence getSequence(String name) {
        Sequence result = null;
        if (name != null) {
            result = (Sequence)_sequences.get(name.toUpperCase());
        }
        return result;
    }
    
    public TransactionManager getTransactionManager() {
        return _transactionManager;
    }

    public void addDatabaseModificationListener(DatabaseModificationListener l) {
        _listeners.add(l);
    }

    public List getDatabaseModificationListeners() {
        return _listeners;
    }

    //--------------------------------------------------------------- Protected
    
    protected Iterator getSequences() {
        return _sequences.values().iterator();
    }

    protected int getSequenceCount() {
        return _sequences.size();
    }
    
    protected Iterator getTables() {
        return _tables.values().iterator();
    }

    protected void loadProperties(Properties props) throws AxionException {
        Enumeration enum = props.propertyNames();
        while(enum.hasMoreElements()) {
            String key = (String)(enum.nextElement());
            if(key.startsWith("type.")) {
                addDataType(key.substring("type.".length()),props.getProperty(key));
            } else if(key.startsWith("function.")) {
                addFunction(key.substring("function.".length()),props.getProperty(key));
            } else if(key.startsWith("index.")) {
                addIndexType(key.substring("index.".length()),props.getProperty(key));
            } else if(key.equals("readonly")) {
                String val = props.getProperty(key);
                if("yes".equalsIgnoreCase(val) || "true".equalsIgnoreCase(val) || "on".equalsIgnoreCase(val)) {
                    _readOnly = true;
                } else {
                    _readOnly = false;
                }
            } else {
                _log.warn("Unrecognized property \"" + key + "\".");
            }
        }
    }

    /** Callers should treat the returned Properties as immutable. */
    protected static synchronized Properties getBaseProperties() {
        if(null == _props) {
            _props = new Properties();
            // lets try the class loader that loaded me
            ClassLoader classLoader = BaseDatabase.class.getClassLoader();
            InputStream in = classLoader.getResourceAsStream("axiondb.properties");
            
            if(null == in) {
                _log.debug( "Couldn't find axiondb.properties via my system classloader, trying the context class loader" );
                
                // lets try the context class loader
                classLoader = Thread.currentThread().getContextClassLoader();
                in = classLoader.getResourceAsStream("axiondb.properties");
            }
            if(null != in) {
                try {
                    _props.load(in);
                } catch(Exception e) {
                    _log.error("Exception while loading properties from CLASSPATH axiondb.properties",e); // PROPOGATE UP!?!
                }
                try { in.close(); } catch(Exception e) { }
            } else {
                _log.warn("Could not find axiondb.properties on the classpath.");
            }
        }
        return _props;
    }

    protected abstract Table createSystemTable(String name);

    //----------------------------------------------------------------- Private

    /**
     * Should get called by subclasses in constructors
     */
    protected void createMetaDataTables() throws AxionException {
        Table columns = null;
        {
            columns = createSystemTable("AXION_COLUMNS");
            columns.addColumn(new Column("TABLE_CAT",new StringType()));
            columns.addColumn(new Column("TABLE_SCHEM",new StringType()));
            columns.addColumn(new Column("TABLE_NAME",new StringType()));
            columns.addColumn(new Column("COLUMN_NAME",new StringType()));
            columns.addColumn(new Column("DATA_TYPE",new ShortType()));
            columns.addColumn(new Column("TYPE_NAME",new StringType()));
            columns.addColumn(new Column("COLUMN_SIZE",new IntegerType()));
            columns.addColumn(new Column("BUFFER_LENGTH",new IntegerType()));
            columns.addColumn(new Column("DECIMAL_DIGITS",new IntegerType()));
            columns.addColumn(new Column("NUM_PREC_RADIX",new IntegerType()));
            columns.addColumn(new Column("NULLABLE",new IntegerType()));
            columns.addColumn(new Column("REMARKS",new StringType()));
            columns.addColumn(new Column("COLUMN_DEF",new StringType()));
            columns.addColumn(new Column("SQL_DATA_TYPE",new IntegerType()));
            columns.addColumn(new Column("SQL_DATETIME_SUB",new IntegerType()));
            columns.addColumn(new Column("CHAR_OCTET_LENGTH",new IntegerType()));
            columns.addColumn(new Column("ORDINAL_POSITION",new IntegerType()));
            columns.addColumn(new Column("IS_NULLABLE",new StringType())); // yes, StringType "NO", "YES", ""
        }
        addDatabaseModificationListener(_colUpd);
        addTable(columns);

        Table tables  = null;
        AxionTablesMetaTableUpdater updTables = new AxionTablesMetaTableUpdater(this);
        {
            tables = createSystemTable("AXION_TABLES");
            tables.addColumn(new Column("TABLE_CAT", new StringType()));
            tables.addColumn(new Column("TABLE_SCHEM", new StringType()));
            tables.addColumn(new Column("TABLE_NAME", new StringType()));
            tables.addColumn(new Column("TABLE_TYPE", new StringType()));
            tables.addColumn(new Column("REMARKS", new StringType()));
            // bootstrap AXION_COLUMNS into AXION_TABLES
            Row row = updTables.createRowForAddedTable(columns);
            tables.addRow(row);
        }
        addDatabaseModificationListener(updTables);
        addTable(tables);

        {
            Table tableTypes = createSystemTable("AXION_TABLE_TYPES");
            tableTypes.addColumn(new Column("TABLE_TYPE",new StringType()));
            String[] types = new String[] { Table.REGULAR_TABLE_TYPE,
                                            Table.SYSTEM_TABLE_TYPE };
            for (int i = 0; i < types.length; i++) {
                SimpleRow row = new SimpleRow(1);
                row.set(0, types[i]);
                tableTypes.addRow(row);
            }
            addTable(tableTypes);
        }

        {
            Table catalogs = createSystemTable("AXION_CATALOGS");
            catalogs.addColumn(new Column("TABLE_CAT",new StringType()));
            {
                SimpleRow row = new SimpleRow(1);
                row.set(0,"");
                catalogs.addRow(row);
            }
            addTable(catalogs);
        }

        {
            Table schemata = createSystemTable("AXION_SCHEMATA");
            schemata.addColumn(new Column("TABLE_CAT",new StringType()));
            schemata.addColumn(new Column("TABLE_SCHEM",new StringType()));
            {
                SimpleRow row = new SimpleRow(2);
                row.set(0,"");
                row.set(1,"");
                schemata.addRow(row);
            }
            addTable(schemata);
        }

        {
            // XXX FIX ME XXX
            // these are a bit hacked
            Table types = createSystemTable("AXION_TYPES");
            types.addColumn(new Column("TYPE_NAME",new StringType()));
            types.addColumn(new Column("DATA_TYPE",new ShortType()));
            types.addColumn(new Column("PRECISION",new IntegerType()));
            types.addColumn(new Column("LITERAL_PREFIX",new StringType()));
            types.addColumn(new Column("LITERAL_SUFFIX",new StringType()));
            types.addColumn(new Column("CREATE_PARAMS",new StringType()));
            types.addColumn(new Column("NULLABLE",new IntegerType()));
            types.addColumn(new Column("CASE_SENSITIVE",new BooleanType()));
            types.addColumn(new Column("SEARCHABLE",new ShortType()));
            types.addColumn(new Column("UNSIGNED_ATTRIBUTE",new BooleanType()));
            types.addColumn(new Column("FIXED_PREC_SCALE",new BooleanType()));
            types.addColumn(new Column("AUTO_INCREMENT",new BooleanType()));
            types.addColumn(new Column("LOCAL_TYPE_NAME",new StringType()));
            types.addColumn(new Column("MINIMUM_SCALE",new ShortType()));
            types.addColumn(new Column("MAXIMUM_SCALE",new ShortType()));
            types.addColumn(new Column("SQL_DATA_TYPE",new IntegerType()));
            types.addColumn(new Column("SQL_DATETIME_SUB",new IntegerType()));
            types.addColumn(new Column("NUM_PREC_RADIX",new IntegerType()));
            addTable(types);
            addDatabaseModificationListener(new AxionTypesMetaTableUpdater(this));
        }

        {
            Table seqTable = createSystemTable("AXION_SEQUENCES");
            seqTable.addColumn(new Column("SEQUENCE_NAME", new StringType()));
            seqTable.addColumn(new Column("SEQUENCE_VALUE", new IntegerType()));
            addTable(seqTable);
            addDatabaseModificationListener(_seqUpd);
        }
    }

    private void addIndexType(String typename, String factoryclassname) throws AxionException {
        assertNotNull(typename, factoryclassname);
        try {
            IndexFactory factory = (IndexFactory)(getInstanceForClassName(factoryclassname));
            addIndexType(typename,factory);
        } catch(ClassCastException e) {
            throw new AxionException("Expected IndexFactory for \"" + factoryclassname + "\".");
        }
    }

    private void addIndexType(String typename, IndexFactory factory) throws AxionException {
        assertNotNull(typename, factory);
        if(null != _indexTypes.get(typename.toUpperCase())) {
            throw new AxionException("An index type named \"" + typename + "\" already exists (" + _indexTypes.get(typename.toUpperCase()) + ")");
        }
        _log.debug("Adding index type \"" + typename + "\" (" + factory+ ").");
        _indexTypes.put(typename.toUpperCase(),factory);
    }
    
    private void addDataType(String typename, String factoryclassname) throws AxionException {
        assertNotNull(typename, factoryclassname);
        try {
            DataTypeFactory factory = (DataTypeFactory)(getInstanceForClassName(factoryclassname));
            addDataType(typename, factory);
        } catch(ClassCastException e) {
            throw new AxionException("Expected DataType for \"" + factoryclassname + "\".");
        }
    }

    private void addDataType(String typename, DataTypeFactory factory) throws AxionException {
        assertNotNull(typename, factory);
        if(null != _dataTypes.get(typename.toUpperCase())) {
            throw new AxionException("A type named \"" + typename + "\" already exists (" + _dataTypes.get(typename.toUpperCase()) + ")");
        }
        _log.debug("Adding type \"" + typename + "\" (" + factory+ ").");
        DataType type = factory.makeNewInstance();
        typename = typename.toUpperCase();
        _dataTypes.put(typename, type);
        DatabaseTypeEvent e = new DatabaseTypeEvent(typename, type);
        Iterator i = getDatabaseModificationListeners().iterator();
        while (i.hasNext()) {
            DatabaseModificationListener cur = (DatabaseModificationListener)i.next();
            cur.typeAdded(e);
        }
    }

    private void addFunction(String fnname, String factoryclassname) throws AxionException {
        assertNotNull(fnname, factoryclassname);
        try {
            FunctionFactory factory = (FunctionFactory)(getInstanceForClassName(factoryclassname));
            addFunction(fnname,factory);
        } catch(ClassCastException e) {
            throw new AxionException("Expected FunctionFactory for \"" + factoryclassname + "\".");
        }
    }

    private void addFunction(String fnname, FunctionFactory factory ) throws AxionException {
        assertNotNull(fnname, factory);
        if(null != _functions.get(fnname.toUpperCase())) {
            throw new AxionException("A function named \"" + fnname+ "\" already exists (" + _functions.get(fnname.toUpperCase()) + ")");
        }
        _log.debug("Adding function \"" + fnname + "\" (" + factory + ").");
        _functions.put(fnname.toUpperCase(),factory);
    }

    private ConcreteFunction getFunction(String name) {
        FunctionFactory factory = (FunctionFactory)(_functions.get(name.toUpperCase()));
        if(null != factory) {
            return factory.makeNewInstance();
        } else {
            return null;
        }
    }


    private void assertNotNull(Object obj1, Object obj2) throws AxionException {
        if(null == obj1|| null == obj2) {
            throw new AxionException("Neither argument can be null.");
        }
    }

    private Object getInstanceForClassName(String classname) throws AxionException {
        try {
            Class clazz = Class.forName(classname);
            return clazz.newInstance();
        } catch(ClassNotFoundException e) {
            throw new AxionException("Class \"" + classname + "\" not found.");
        } catch(InstantiationException e) {
            throw new AxionException("Unable to instantiate class \"" + classname + "\" via a no-arg constructor.");
        } catch(IllegalAccessException e) {
            throw new AxionException("IllegalAccessException trying to instantiate class \"" + classname + "\" via a no-arg constructor.");
        }
    }

    private DataType getDataType(String tablename, String columnname) throws AxionException {
        Table table = getTable(tablename);
        if(null == table) { 
            throw new AxionException("Table " + tablename + " not found.");
        }
        Column col = table.getColumn(columnname);
        if(null == col) { 
            throw new AxionException("Column " + columnname + " not found in table " + tablename + ".");
        }
        return col.getDataType();
    }

    private Selectable resolveColumn(ColumnIdentifier column, TableIdentifier[] tables) throws AxionException {
        if(null == column) {
            return null;
        }
        if (column.getTableName() != null && getSequence(column.getTableName()) != null) {
            return getSequence(column.getTableName());
        }
        if(column.getTableName() != null && column.getTableAlias() != null && column.getDataType() != null) {
            // if the column already has a table name, table alias and data type, we're done
            return column;
        } else {
            boolean foundit = false;
            for(int i=0;i<tables.length;i++) {
                if(null != tables[i].getTableAlias() && tables[i].getTableAlias().equals(column.getTableName())) {
                    // if the column's table name is the table's alias name,
                    // the column belongs to this table
                    column.setTableIdentifier(tables[i]);
                    foundit = true;
                    break;
                } else if(tables[i].getTableName().equals(column.getTableName())) {
                    // else if the column's table name is the table's name
                    // the column belongs to this table
                    column.setTableIdentifier(tables[i]);
                    foundit = true;
                    break;
                } else if(null == column.getTableName()) {
                    // else if the column has no table name
                    if("*".equals(column.getName())) {
                        // if the column is "*", we're done
                        foundit = true;
                        break;
                    } else {
                        // look for it in this table
                        Table table = getTable(tables[i]);
                        if(null == table) { 
                            throw new AxionException("Table " + tables[i] + " not found.");
                        }
                        if(table.hasColumn(column)) {
                            column.setTableIdentifier(tables[i]);
                            foundit = true;
                            break;
                        }
                    }
                }
            }
            if(foundit) {
                if(column.getTableName() != null) {
                    column.setDataType(getDataType(column.getTableName(),column.getName()));
                }
                return column;
            } else {
                throw new AxionException("Couldn't resolve column " + column);
            }
        }
    }

    private ConcreteFunction resolveFunctionIdentifier(FunctionIdentifier fn, TableIdentifier[] tables) throws AxionException {
        _log.debug("resolveFunction(): " + fn);
        ConcreteFunction cfn = getFunction(fn.getName());
        if(null == cfn) {
            _log.debug("resolveFunction(): no function found");
            throw new AxionException("No function matching " + fn);
        } else {
            _log.debug("resolveFunction(): found " + cfn);
            for(int i=0;i<fn.getArgumentCount();i++) {
                cfn.addArgument(resolveSelectable(fn.getArgument(i),tables));
            }
            if(!cfn.isValid()) {
                _log.info("resolveFunction(): function isn't valid");
                throw new AxionException("Function " + fn + " isn't valid.");
            }
            return cfn;
        }
    }

    private Expression resolveExpression(ExpressionIdentifier expr, TableIdentifier[] tables) throws AxionException {
        _log.debug("resolveExpression(): " + expr);
        Expression expression = new Expression();
        Selectable left = resolveSelectable(expr.getLeftSelectable(), tables);
        if (left instanceof AggregateFunction) {
            throw new AxionException("Use of aggregate functions in expression is not allowed.");
        }
        expression.setLeftSelectable(left);        
        Selectable right = resolveSelectable(expr.getRightSelectable(), tables);
        if (right instanceof AggregateFunction) {
            throw new AxionException("Use of aggregate functions in expression is not allowed.");
        }
        expression.setRightSelectable(right);
        expression.setOperationType(expr.getOperationType());
        return expression;
        
    }
    //-------------------------------------------------------------- Attributes
    
    private String _name = null;
    private boolean _readOnly = false;
    private List _listeners = new ArrayList();
    private Map _tables = new HashMap();
    private Map _dataTypes = new HashMap();
    private Map _functions = new HashMap();
    private Map _indexTypes = new HashMap();
    private Map _tableTypes = new HashMap();
    private Map _sequences = new HashMap();
    private TransactionManager _transactionManager = new TransactionManagerImpl(this);
    private DatabaseModificationListener _colUpd = new AxionColumnsMetaTableUpdater(this);
    private DatabaseModificationListener _seqUpd = new AxionSequencesMetaTableUpdater(this);

    private static Log _log = LogFactory.getLog(BaseDatabase.class);
    private static Properties _props = null;
}
