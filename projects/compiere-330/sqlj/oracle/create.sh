# Create Oracle SQLJ
# Author + Copyright 1999-2005 Jorg Janke
# $Id: create.sh,v 1.3 2005/06/28 18:55:38 jjanke Exp $
#
# Parameter: <compiereDBuser>/<compiereDBpassword>

# unset CLASSPATH=

echo .
echo Load Oracle SQLJ ...
loadjava -user $1@$COMPIERE_DB_NAME -verbose -force -resolve $COMPIERE_HOME/lib/sqlj.jar

echo .
echo Create Oracle Functions ...
sqlplus $1@$COMPIERE_DB_NAME @$COMPIERE_HOME/utils/oracle/createSQLJ.sql $COMPIERE_DB_USER
