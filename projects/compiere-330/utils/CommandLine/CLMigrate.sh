#!/bin/bash
#
echo Install Compiere Server
# $Id: CLMigrate.sh 7087 2009-01-23 00:31:17Z freyes $

if [ $# -lt 1 ]; then
	echo Configuration file missing
	echo Please edit the configuration file appropriately and pass the file name to this script
	echo Usage: $0 CLConfiguration.sh
      exit 1
fi

./CLInstall.sh $1 '--migrate y'
if [ $? -gt 0 ]; then
	echo ===========================================
	echo An error occurred while running the program
	echo Please check log files for details
	echo ===========================================
	exit 1
fi

echo Compiere Migration completed. Check log files for details.
