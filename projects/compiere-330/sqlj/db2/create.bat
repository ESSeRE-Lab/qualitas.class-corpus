@Rem Create DB2 SQLJ
@Rem
@Rem Parameter: <compiereDBuser>/<compiereDBpassword>

@Echo .
@Echo Load DB2 SQLJ ...
@db2cmd db2 "CALL sqlj.install_jar('%COMPIERE_HOME%\lib\sqlj.jar', 'compiere_sqlj')"
 
@Echo .
@Echo Create DB2 Functions ...
@db2cmd db2 -f%COMPIERE_HOME%\utils\db2\createSQLJ.sql
