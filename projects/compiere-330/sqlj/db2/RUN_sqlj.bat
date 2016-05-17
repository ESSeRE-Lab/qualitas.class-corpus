@if (%COMPIERE_HOME%) == () (CALL ..\myEnvironment.bat Server) else (CALL %COMPIERE_HOME%\utils\myEnvironment.bat Server)
@Title Create DB2 SQLJ - %COMPIERE_HOME% (%COMPIERE_DB_NAME%)
@Rem

call create %COMPIERE_DB_USER%/%COMPIERE_DB_PASSWORD%

@pause
