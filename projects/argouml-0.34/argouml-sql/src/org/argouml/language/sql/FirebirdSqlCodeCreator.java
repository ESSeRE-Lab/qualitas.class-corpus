package org.argouml.language.sql;

import java.util.Iterator;
import java.util.List;

/**
 * Class for creating DDL statements for Firebird.
 * 
 * @author Kai
 */
public class FirebirdSqlCodeCreator implements SqlCodeCreator {
    private static final String LINE_SEPARATOR = System
            .getProperty("line.separator");

    private int primaryKeyCounter;

    private int exceptionCounter;

    /**
     * Construct a new code creator.
     * 
     */
    public FirebirdSqlCodeCreator() {
        resetCounters();
    }

    /**
     * Resets counters which are used for auto-generated names (e.g. for primary
     * keys or for exceptions).
     */
    public void resetCounters() {
        primaryKeyCounter = 1;
        exceptionCounter = 1;
    }

    /**
     * Generate DDL statements for the specified foreign key definition.
     * 
     * @param foreignKeyDefinition
     *            The foreign key definition
     * @return The generated DDL statement
     * 
     * @see org.argouml.language.sql.SqlCodeCreator#createForeignKey(org.argouml.language.sql.ForeignKeyDefinition)
     */
    public String createForeignKey(ForeignKeyDefinition foreignKeyDefinition) {
        String tableName = foreignKeyDefinition.getTableName();
        List columnNames = foreignKeyDefinition.getColumnNames();
        String referencesTableName = foreignKeyDefinition
                .getReferencesTableName();
        List referencesColumnNames = foreignKeyDefinition
                .getReferencesColumnNames();
        String foreignKeyName = foreignKeyDefinition.getForeignKeyName();

        StringBuffer sb = new StringBuffer();
        sb.append(LINE_SEPARATOR);
        sb.append("ALTER TABLE ").append(tableName);
        sb.append(" ADD CONSTRAINT ").append(foreignKeyName).append(
                LINE_SEPARATOR);
        sb.append("FOREIGN KEY (");
        sb.append(Utils.stringsToCommaString(columnNames));
        sb.append(") REFERENCES ");
        sb.append(referencesTableName).append(" (");
        sb.append(Utils.stringsToCommaString(referencesColumnNames));
        sb.append(")");

        int refLower = foreignKeyDefinition.getReferencesLower();

        if (refLower == 0) {
            sb.append(" ON DELETE SET NULL");
        } else {
            sb.append(" ON DELETE CASCADE");
        }

        sb.append(";").append(LINE_SEPARATOR);
        sb.append(LINE_SEPARATOR);

        int upper = foreignKeyDefinition.getUpper();
        if (upper == 1) {
            sb.append(getOneToOneTrigger(foreignKeyDefinition));
        }

        return sb.toString();
    }

    private String getOneToOneTriggerBody(ForeignKeyDefinition fkDef,
            String exceptionName) {
        String tableName = fkDef.getTableName();

        StringBuffer sb = new StringBuffer();
        sb.append("    DECLARE VARIABLE x INTEGER;").append(LINE_SEPARATOR);

        sb.append("BEGIN").append(LINE_SEPARATOR);

        sb.append("    ");
        sb.append("SELECT COUNT(*) FROM ").append(tableName);
        sb.append(" WHERE ");

        StringBuffer sbWhere = new StringBuffer();
        List columnNames = fkDef.getColumnNames();
        for (int i = 0; i < columnNames.size(); i++) {
            if (sbWhere.length() > 0) {
                sbWhere.append(" AND ");
            }

            String colName = (String) columnNames.get(i);

            sbWhere.append(colName);
            sbWhere.append(" = NEW.").append(colName);
        }

        List pkFields = fkDef.getTable().getPrimaryKeyFields();
        for (int i = 0; i < pkFields.size(); i++) {
            String pkFieldName = (String) pkFields.get(i);
            sbWhere.append(" AND ").append(pkFieldName);
            sbWhere.append(" <> NEW.").append(pkFieldName);
            sbWhere.append(" ");
        }

        sb.append(sbWhere).append(" INTO :x;").append(LINE_SEPARATOR);

        sb.append("    IF (:x = 1) THEN").append(LINE_SEPARATOR);

        sb.append("        ");
        sb.append("EXCEPTION ").append(exceptionName);
        sb.append(";").append(LINE_SEPARATOR);

        sb.append("END !!").append(LINE_SEPARATOR);

        return sb.toString();
    }

    private String getOneToOneTrigger(ForeignKeyDefinition fkDef) {
        String excName1to1violated = "EXC_ONE_TO_ONE_VIOLATED"
                + exceptionCounter;
        exceptionCounter++;

        String tableName = fkDef.getTableName();
        String referencesTableName = fkDef.getReferencesTableName();

        StringBuffer sb = new StringBuffer();
        sb.append("CREATE EXCEPTION ").append(excName1to1violated);
        sb.append(" 'One record in ").append(referencesTableName);
        sb.append(" references more than one record in ").append(tableName);
        sb.append("';").append(LINE_SEPARATOR);

        sb.append("SET TERM !! ;").append(LINE_SEPARATOR);
        sb.append(LINE_SEPARATOR);

        // Names in Firebird need to be shorter than 30 characters
        String shortTableName = Utils.getShortName(tableName, 22);
        sb.append("CREATE TRIGGER trig_bef_ins_").append(shortTableName);
        sb.append(" FOR ").append(tableName);
        sb.append(LINE_SEPARATOR);

        sb.append("BEFORE INSERT AS").append(LINE_SEPARATOR);

        sb.append(getOneToOneTriggerBody(fkDef, excName1to1violated));
        sb.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        sb.append("CREATE TRIGGER trig_bef_upd_").append(shortTableName);
        sb.append(" FOR ").append(tableName);
        sb.append(LINE_SEPARATOR);

        sb.append("BEFORE UPDATE AS").append(LINE_SEPARATOR);

        sb.append(getOneToOneTriggerBody(fkDef, excName1to1violated));
        sb.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        sb.append("SET TERM ; !!").append(LINE_SEPARATOR);
        sb.append(LINE_SEPARATOR);

        return sb.toString();
    }

    /**
     * Generates DDL statements for creating a table according to the parameter
     * <code>tableDefinition</code>.
     * 
     * @param tableDefinition
     *            A <code>TableDefinition</code> object that holds alls
     *            necessary data for generating code.
     * @return The generated code.
     * @see org.argouml.language.sql.SqlCodeCreator#createTable(org.argouml.language.sql.TableDefinition)
     */
    public String createTable(TableDefinition tableDefinition) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE ");
        sb.append(tableDefinition.getName()).append(" (");
        sb.append(LINE_SEPARATOR);

        Iterator it = tableDefinition.getColumnDefinitions().iterator();
        while (it.hasNext()) {
            ColumnDefinition colDef = (ColumnDefinition) it.next();
            sb.append("    ");
            sb.append(colDef.getName()).append(" ");
            sb.append(colDef.getDatatype());
            Boolean nullable = colDef.getNullable();
            if (nullable != null) {
                if (nullable.equals(Boolean.FALSE)) {
                    sb.append(" ").append("NOT NULL");
                }
            }
            sb.append(",").append(LINE_SEPARATOR);
        }

        StringBuffer sbPk = new StringBuffer();
        it = tableDefinition.getPrimaryKeyFields().iterator();
        while (it.hasNext()) {
            String primaryKeyField = (String) it.next();
            if (sbPk.length() > 0) {
                sbPk.append(", ");
            }
            sbPk.append(primaryKeyField);
        }

        sb.append("CONSTRAINT PK").append(primaryKeyCounter);
        sb.append(" PRIMARY KEY (").append(sbPk).append(")");
        sb.append(LINE_SEPARATOR);

        sb.append(");").append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        primaryKeyCounter++;

        return sb.toString();
    }

    /**
     * @return The name of this code creator.
     * @see org.argouml.language.sql.SqlCodeCreator#getName()
     */
    public String getName() {
        return "Firebird";
    }
}
