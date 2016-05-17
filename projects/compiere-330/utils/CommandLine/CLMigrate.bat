@Title Compiere Migration
@Rem  $Id: CLMigrate.bat 7087 2009-01-23 00:31:17Z freyes $
@Echo off

@IF NOT [%1]==[] goto ARG_PASS
@Echo Configuration file missing
@Echo Please edit the configuration file appropriately and pass the file name to this script
@Echo Usage: CLInstall.bat CLConfiguration.bat
@EXIT /B 1

:ARG_PASS
echo calling clinstall
@CALL CLInstall.bat %1 migrate
@IF ERRORLEVEL = 1 ((echo Install error)&&(EXIT /B 1))

