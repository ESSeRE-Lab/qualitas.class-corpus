@Title Install Compiere Server
@Rem  $Header: /cvsroot/compiere/install/Compiere2/RUN_setup.bat,v 1.19 2005/09/08 21:54:12 jjanke Exp $
@Echo off


@if not "%JAVA_HOME%" == "" goto JAVA_HOME_OK
@Set JAVA=java
@Echo JAVA_HOME is not set.  
@Echo You may not be able to start the required Setup window !!
@Echo Set JAVA_HOME to the directory of your local 1.5 JDK.
@Echo If you experience problems, run utils/WinEnv.js
@Echo Example: cscript utils\WinEnv.js C:\Compiere2 "C:\Program Files\Java\jdk1.5.0_04"
goto START

:JAVA_HOME_OK
@Set JAVA=%JAVA_HOME%\bin\java


:START
@Echo =======================================
@Echo Starting Setup Dialog ...
@Echo =======================================
@SET CP=lib\CInstall.jar;lib\CompiereInstall.jar;lib\jPDF.jar;lib\CCTools.jar;lib\oracle.jar;lib\jboss.jar;lib\db2.jar;lib\postgreSQL.jar;lib\sqlServer.jar

@Rem Trace Level Parameter, e.g. SET ARGS=ALL
@SET ARGS=CONFIG

@Rem To test the OCI driver, add -DTestOCI=Y to the command - example:
@Rem %JAVA% -classpath %CP% -DCOMPIERE_HOME=%COMPIERE_HOME% -DTestOCI=Y org.compiere.install.Setup %ARGS%

@"%JAVA%" -Xmx258m -classpath %CP% -DCOMPIERE_HOME=%COMPIERE_HOME% org.compiere.install.Setup %ARGS%
@Echo ErrorLevel = %ERRORLEVEL%
@IF NOT ERRORLEVEL = 1 GOTO NEXT
@Echo ***************************************
@Echo Check the error message above.
@Echo ***************************************
@Echo Make sure that the environment is set correctly!
@Echo Set environment variable JAVA_HOME manually
@Echo or use WinEnv.js in the util directory
@Echo ***************************************
@Pause
@Exit


:NEXT
@Rem ===================================
@Rem Setup Compiere Environment
@Rem ===================================
@Call utils\RUN_WinEnv.bat

@Rem ===================================
@Rem Run Ant directly
@Rem ===================================
@Rem %JAVA% -classpath lib\CInstall.jar; -DCOMPIERE_HOME=%COMPIERE_HOME% -Dant.home="." org.apache.tools.ant.launch.Launcher setup


@Rem ================================
@Rem Test local Connection
@Rem ================================
@Rem %JAVA% -classpath lib\Compiere.jar;lib\CompiereCLib.jar org.compiere.install.ConnectTest localhost


@Echo .
@Echo For problems, check log file in base directory
@pause
