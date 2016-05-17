#!/bin/bash
#
# $Id: CLUpdateLicense.sh 7040 2009-01-15 22:41:34Z cjandhyala $

# pre requisites:
#	1) This script needs to be placed in  $COMPIERE_HOME/utils/CommandLine directory of a successful Compiere Installation
#	2) Database connection information is obtained from Compiere.properties in  $COMPIERE_HOME directory.
#	Hence needs a valid Compiere.properties in $COMPIERE_HOME directory
#	3) Database needs to be up and running


if [ $# -lt 1 ]; then
	echo Configuration file missing
	echo Please edit the configuration file appropriately and pass the file name to this script
	echo Usage: $0 CLConfiguration.sh
      exit 1
fi


echo calling configuration script
. $1


echo ==========================================
echo Web store credentials for license checking
echo ==========================================
echo CL_SYSTEM_NAME=$CL_SYSTEM_NAME
echo CL_WEBSTORE_EMAIL=$CL_WEBSTORE_EMAIL
echo CL_WEBSTORE_PASS=$CL_WEBSTORE_PASS


echo Setting up environment for Updating License Information
cd $COMPIERE_HOME/utils
. ./RUN_UnixEnv.sh
. ./myEnvironment.sh Server
cd CommandLine


#echo ==================================================================================
#echo Please edit the command below to supply the System Name, web store email and password
#echo $COMPIERE_JAVA $COMPIERE_JAVA_OPTIONS -cp $COMPIERE_HOME/utils/UpdateLicense.jar:$CLASSPATH com.compiere.client.UpdateLicense --sysname xxx --email xxx --password xxx
#echo Without any arguments the program uses the credentials stored earlier
#echo ==================================================================================

echo Updating Licenses
$COMPIERE_JAVA $COMPIERE_JAVA_OPTIONS -cp $CLASSPATH com.compiere.client.UpdateLicense --sysname $CL_SYSTEM_NAME --email $CL_WEBSTORE_EMAIL --password $CL_WEBSTORE_PASS

if [ $? -gt 0 ]; then
	echo ===========================================
	echo An error occurred while running the Update License program
	echo Please check log file in $COMPIERE_HOME/log directory for details ...
	echo ===========================================
	exit
fi
echo .
echo License updated successfully.

