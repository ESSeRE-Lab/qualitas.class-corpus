@Title Build Compiere Incremental + Install
@Rem $Header: /cvsroot/compiere/utils_dev/RUN_buildIncremental.bat,v 1.14 2005/09/08 21:56:11 jjanke Exp $

@CALL myDevEnv.bat
@IF NOT %COMPIERE_ENV%==Y GOTO NOBUILD

@Echo	Stop Apps Server (waiting)
@START %COMPIERE_HOME%\utils\RUN_Server2Stop.bat
@Sleep 5

@echo Building ...
@"%JAVA_HOME%\bin\java" -Dant.home="." %ANT_PROPERTIES% org.apache.tools.ant.Main complete
@Echo ErrorLevel = %ERRORLEVEL%

@IF NOT ERRORLEVEL 0 GOTO BUILDOK
@Pause
@Exit

:BUILDOK
dir %COMPIERE_INSTALL%

@Echo	Cleaning up ...
@erase /q /s %TMP%

@Echo	Starting Apps Server ...
@Start %COMPIERE_HOME%\utils\RUN_Server2.bat

@Sleep 10
@Exit

:NOBUILD
@Echo Check myDevEnv.bat (copy from myDevEnvTemplate.bat)
@Pause