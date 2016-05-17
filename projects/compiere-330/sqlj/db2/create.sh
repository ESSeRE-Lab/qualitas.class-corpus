# Create DB2 SQLJ
#
# Parameter: <compiereDBuser>/<compiereDBpassword>

echo .
echo Load DB2 SQLJ ...
db2 "CALL sqlj.install_jar('$COMPIERE_HOME/lib/sqlj.jar', 'compiere_sqlj')"

echo .
echo Create DB2 Functions ...
db2 -f$COMPIERE_HOME/utils/db2/createSQLJ.sql
