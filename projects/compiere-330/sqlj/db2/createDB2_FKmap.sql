-- This CLP file was created using DB2LOOK Version 9.1

-- Timestamp: 10/19/2006 10:11:13 AM

-- Database Name: COMPIERE       

-- Database Manager Version: DB2/NT Version 9.1.0          

-- Database Codepage: 1208

-- Database Collating Sequence is: IDENTITY





CONNECT TO COMPIERE;



-- Mimic tablespace



ALTER TABLESPACE SYSCATSPACE

      PREFETCHSIZE AUTOMATIC

      OVERHEAD 7.500000

      TRANSFERRATE 0.060000;





ALTER TABLESPACE TEMPSPACE1

      PREFETCHSIZE AUTOMATIC

      OVERHEAD 7.500000

      TRANSFERRATE 0.060000;





ALTER TABLESPACE USERSPACE1

      PREFETCHSIZE AUTOMATIC

      OVERHEAD 7.500000

      TRANSFERRATE 0.060000;





------------------------------------------------

-- DDL Statements for table "COMPIERE"."AD_FOREIGNKEYINFO"

------------------------------------------------

 



CREATE TABLE COMPIERE.AD_FOREIGNKEYINFO  (

		  CONSTRAINTNAME VARCHAR(126) NOT NULL , 

		  TABLENAME VARCHAR(62) NOT NULL , 

		  P_TABLENAME VARCHAR(62) , 

		  C_TYPE CHAR(1) NOT NULL WITH DEFAULT 'F' , 

		  C_STATEMENT VARCHAR(2000) , 

		  ORIGINALNAME VARCHAR(128) )   

		 IN USERSPACE1 ; 



COMMENT ON COLUMN COMPIERE.AD_FOREIGNKEYINFO.C_TYPE IS 'F: FK, P:PK, T:Trigger';





-- DDL Statements for primary key on Table COMPIERE.AD_FOREIGNKEYINFO



ALTER TABLE COMPIERE.AD_FOREIGNKEYINFO 

	ADD CONSTRAINT AD_FOREIGNKEYINFO_KEY PRIMARY KEY

		(CONSTRAINTNAME);



















COMMIT WORK;



CONNECT RESET;



TERMINATE;



-- Generate statistics for all creators 

-- The db2look utility will consider only the specified tables 

-- Creating DDL for table(s)

;
