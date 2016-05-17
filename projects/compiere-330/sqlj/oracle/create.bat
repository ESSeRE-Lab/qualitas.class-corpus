@Rem Create Oracle SQLJ
@Rem Author + Copyright 1999-2005 Jorg Janke
@Rem $Id: create.bat,v 1.8 2005/05/31 07:28:22 jjanke Exp $
@Rem
@Rem Parameter: <compiereDBuser>/<compiereDBpassword>
@Rem

@Echo .
@Echo Load Oracle SQLJ ...
@SET CLASSPATH=
@call loadjava -user %1@%COMPIERE_DB_NAME% -verbose -force -resolve %COMPIERE_HOME%\lib\sqlj.jar

@Echo .

@Echo Create Oracle Functions ...
@sqlplus %1@%COMPIERE_DB_NAME% @%COMPIERE_HOME%\utils\oracle\createSQLJ.sql %COMPIERE_DB_USER%
