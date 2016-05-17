if [ $COMPIERE_HOME ]; then
  cd $COMPIERE_HOME/utils
fi
. ./myEnvironment.sh Server
echo 	Create DB2 SQLJ - $COMPIERE_HOME \($COMPIERE_DB_NAME\)

sh $COMPIERE_DB_PATH/create.sh $COMPIERE_DB_USER/$COMPIERE_DB_PASSWORD

