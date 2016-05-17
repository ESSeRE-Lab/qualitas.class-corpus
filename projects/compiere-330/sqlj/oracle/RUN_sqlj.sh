# Author + Copyright 1999-2005 Jorg Janke
# $Id: RUN_sqlj.sh,v 1.1 2005/05/31 07:28:21 jjanke Exp $
if [ $COMPIERE_HOME ]; then
  cd $COMPIERE_HOME/utils
fi
. ./myEnvironment.sh Server
echo 	Create Oracle SQLJ - $COMPIERE_HOME \($COMPIERE_DB_NAME\)

sh $COMPIERE_DB_PATH/create.sh $COMPIERE_DB_USER/$COMPIERE_DB_PASSWORD

