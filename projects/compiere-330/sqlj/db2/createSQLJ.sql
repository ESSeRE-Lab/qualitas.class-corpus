--
-- 	Create SQL Java Functions (Derby)
-- 
-- 	Author + Copyright 1999-2005 Jorg Janke
-- 	09-28-2006 Jinglun Zhang     modified from sqlj/oracle, ignore drop errors
--								 copy java class files to DB2 sqllib\function directory
-- 
--  NO need to run this script normally. The RUN_importCompiereDB2.bat (.sh) will do have all function definitions
-- 
-- 


-- customize the following 3 statements
connect to compiere user compiere using compiere;
Call SQLJ.REMOVE_JAR ('compiere_sqlj');
Call SQLJ.INSTALL_JAR ('file:C:\compiere\compiere-all2\sqlj\sqlj.jar', 'compiere_sqlj');



-- drop/create functions


-- 	procedure for Time2Chars     
DROP FUNCTION Time2Chars;
CREATE FUNCTION Time2Chars
(
    p_ts     TIMESTAMP
)
 	RETURNS VARCHAR(36)
 	PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA RETURNS NULL ON NULL INPUT
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.Time2Chars';


DROP FUNCTION compiereVersion;
 
CREATE FUNCTION compiereVersion()
 	RETURNS VARCHAR(255)
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.getVersion';

DROP FUNCTION compiereProperties;
 
CREATE FUNCTION compiereProperties()
 	RETURNS VARCHAR(1022)
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.getProperties';

DROP FUNCTION compiereProperty;
 
CREATE FUNCTION compiereProperty(p_key VARCHAR(255))
 	RETURNS VARCHAR(1022)
 	 PARAMETER STYLE JAVA LANGUAGE JAVA CALLED ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.getProperty';


-- Product	-- 
DROP FUNCTION productAttribute;
 
CREATE FUNCTION productAttribute (M_AttributeSetInstance_ID int)
 	RETURNS VARCHAR(255)
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT NO EXTERNAL ACTION 
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Product.attributeName';


DROP FUNCTION bomPriceLimit;
 
CREATE FUNCTION bomPriceLimit (M_Product_ID int, M_PriceList_Version_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT NO EXTERNAL ACTION 
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Product.bomPriceLimit';

DROP FUNCTION bomPriceList;
 
CREATE FUNCTION bomPriceList (M_Product_ID int, M_PriceList_Version_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT NO EXTERNAL ACTION 
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Product.bomPriceList';

DROP FUNCTION bomPriceStd;
 
CREATE FUNCTION bomPriceStd (M_Product_ID int, M_PriceList_Version_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT NO EXTERNAL ACTION  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Product.bomPriceStd';


DROP FUNCTION bomQtyAvailable;
 
CREATE FUNCTION bomQtyAvailable (M_Product_ID int, M_Warehouse_ID int, 
        M_Locator_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT NO EXTERNAL ACTION  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Product.bomQtyAvailable';

DROP FUNCTION bomQtyOnHand;
 
CREATE FUNCTION bomQtyOnHand (M_Product_ID int, M_Warehouse_ID int, 
        M_Locator_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT NO EXTERNAL ACTION 
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Product.bomQtyOnHand';

DROP FUNCTION bomQtyOrdered;
 
CREATE FUNCTION bomQtyOrdered (M_Product_ID int, M_Warehouse_ID int, 
        M_Locator_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT NO EXTERNAL ACTION 
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Product.bomQtyOrdered';

DROP FUNCTION bomQtyReserved;
 
CREATE FUNCTION bomQtyReserved (M_Product_ID int, M_Warehouse_ID int, 
        M_Locator_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT NO EXTERNAL ACTION 
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Product.bomQtyReserved';


-- Currency -- 
DROP FUNCTION currencyBase;
 
CREATE FUNCTION currencyBase (Amount DECIMAL(31,5), C_CurrencyFrom_ID int, 
        ConversionDate TIMESTAMP, AD_Client_ID int, AD_Org_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Currency.base';

-- to work around the data type issue and function signature 	
DROP FUNCTION currencyBaseD;
 
CREATE FUNCTION currencyBaseD (Amount double, C_CurrencyFrom_ID int, 
        ConversionDate TIMESTAMP, AD_Client_ID int, AD_Org_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Currency.baseD';
	

DROP FUNCTION currencyConvert;
 
--CREATE FUNCTION currencyConvert (Amount DOUBLE, C_CurrencyFrom_ID int, 
--        C_CurrencyTo_ID int,
--    ConversionDate TIMESTAMP, C_ConversionType_ID int, AD_Client_ID int, AD_Org_ID int)
-- 	RETURNS DOUBLE
CREATE FUNCTION currencyConvert (Amount DECIMAL(31,5), C_CurrencyFrom_ID int, 
        C_CurrencyTo_ID int,
    ConversionDate TIMESTAMP, C_ConversionType_ID int, AD_Client_ID int, AD_Org_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Currency.convert';
	

DROP FUNCTION currencyConvertD;
 
CREATE FUNCTION currencyConvertD (Amount DOUBLE, C_CurrencyFrom_ID int, 
        C_CurrencyTo_ID int,
    ConversionDate TIMESTAMP, C_ConversionType_ID int, AD_Client_ID int, AD_Org_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Currency.convertD';


DROP FUNCTION currencyRate;
 
CREATE FUNCTION currencyRate (C_CurrencyFrom_ID int, C_CurrencyTo_ID int,
        ConversionDate TIMESTAMP, C_ConversionType_ID int, AD_Client_ID int, AD_Org_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Currency.rate';

DROP FUNCTION currencyRound;
 
CREATE FUNCTION currencyRound (Amt DECIMAL(31,5), C_CurrencyTo_ID int, IsCosting VARCHAR(2))
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Currency.round';


-- BPartner -- 
DROP FUNCTION bpartnerRemitLocation;
 
CREATE FUNCTION bpartnerRemitLocation (p_C_BPartner_ID int)
 	RETURNS int
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.BPartner.remitLocation';


-- Invoice -- 
DROP FUNCTION invoiceOpen;
 
CREATE FUNCTION invoiceOpen (p_C_Invoice_ID int, p_C_InvoicePaySchedule_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Invoice.open';

DROP FUNCTION invoicePaid;
 
CREATE FUNCTION invoicePaid (p_C_Invoice_ID int, p_C_Currency_ID int, 
        p_MultiplierAP int)
 	RETURNS DOUBLE
 	PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Invoice.paid';

DROP FUNCTION invoiceDiscount;
 
CREATE FUNCTION invoiceDiscount (p_C_Invoice_ID int, p_PayDate TIMESTAMP, 
        p_C_InvoicePaySchedule_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Invoice.discount';


-- Payment Term -- 
DROP FUNCTION paymentTermDueDays;
 
CREATE FUNCTION paymentTermDueDays (p_C_PaymentTerm_ID int, p_DocDate TIMESTAMP, 
        p_PayDate TIMESTAMP)
 	RETURNS int
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.PaymentTerm.dueDays';

DROP FUNCTION paymentTermDiscount;
 
CREATE FUNCTION paymentTermDiscount (p_Amount DECIMAL(31,5), p_C_Currency_ID int,
		p_C_PaymentTerm_ID int, p_DocDate TIMESTAMP, p_PayDate TIMESTAMP)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.PaymentTerm.discount';
	
	
DROP FUNCTION paymentTermDiscountD;
 
CREATE FUNCTION paymentTermDiscountD (p_Amount DOUBLE, p_C_Currency_ID int,
		p_C_PaymentTerm_ID int, p_DocDate TIMESTAMP, p_PayDate TIMESTAMP)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.PaymentTerm.discount';

DROP FUNCTION paymentTermDueDate;
 
CREATE FUNCTION paymentTermDueDate (p_C_PaymentTerm_ID int, p_DocDate TIMESTAMP)
 	RETURNS TIMESTAMP
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.PaymentTerm.dueDate';


-- Payment -- 
DROP FUNCTION paymentAllocated;
 
CREATE FUNCTION paymentAllocated (p_C_Payment_ID int, p_C_Currency_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Payment.allocated';

DROP FUNCTION paymentAvailable;
 
CREATE FUNCTION paymentAvailable (p_C_Payment_ID int)
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Payment.available';


-- Account -- 
DROP FUNCTION acctBalance;
 
CREATE FUNCTION acctBalance (p_Account_ID int, p_AmtDr Decimal(31,5), p_AmtCr Decimal(31,5))
 	RETURNS DOUBLE
 	 PARAMETER STYLE JAVA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Account.balance';


-- General	-- 
--BEGIN
--	dbms_java.grant_permission('COMPIERE','SYS:java.util.PropertyPermission', '*', 'read,write');
--END;


-- Get Character at Position   
DROP FUNCTION charAt;
CREATE FUNCTION charAt
(
    p_string    VARCHAR(2046),
    p_pos       int
)
 	RETURNS CHAR
 	 PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA RETURNS NULL ON NULL INPUT  DETERMINISTIC  NO EXTERNAL ACTION  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.charAt';

-- GetDate                     
DROP FUNCTION getdate;
CREATE FUNCTION getdate()
 	RETURNS TIMESTAMP
 	 PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.getDate';

-- First Of DD/DY/MM/Q         
DROP FUNCTION firstOf;
CREATE FUNCTION firstOf
(
    p_date      TIMESTAMP,
    p_datePart  VARCHAR(26)
)
 	RETURNS TIMESTAMP
 	 PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA RETURNS NULL ON NULL INPUT  DETERMINISTIC  NO EXTERNAL ACTION  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.firstOf';

-- Add Number of Days      
DROP FUNCTION addDays;
CREATE FUNCTION addDays
(
    p_date      TIMESTAMP,
    p_days      int
)
 	RETURNS TIMESTAMP
 	 PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA RETURNS NULL ON NULL INPUT  DETERMINISTIC  NO EXTERNAL ACTION  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.addDays';

-- Difference in Days      
DROP FUNCTION getDaysBetween;
CREATE FUNCTION getDaysBetween
(
    p_date1     TIMESTAMP,
    p_date2     TIMESTAMP
)
 	RETURNS int
 	 PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA RETURNS NULL ON NULL INPUT  DETERMINISTIC  NO EXTERNAL ACTION  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.getDaysBetween';


-- 	Truncate Date     
DROP FUNCTION trunc;
CREATE FUNCTION trunc
(
    p_dateTime     TIMESTAMP,
    p_fmt			VARCHAR(10)
)
 	RETURNS TIMESTAMP
 	 PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA RETURNS NULL ON NULL INPUT DETERMINISTIC  NO EXTERNAL ACTION  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.trunc';


-- 	convert number to chars     
DROP FUNCTION getChars;
CREATE FUNCTION getChars
(
    p_number     DECIMAL(31,5)
)
 	RETURNS VARCHAR(38)
 	 PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA RETURNS NULL ON NULL INPUT   DETERMINISTIC  NO EXTERNAL ACTION 
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.getChars';


-- 	function for nextID     
DROP FUNCTION nextID;
CREATE FUNCTION nextID
(
    p_number     INTEGER,
    p_string     VARCHAR(6)
)
 	RETURNS INTEGER
 	 PARAMETER STYLE JAVA READS SQL DATA LANGUAGE JAVA RETURNS NULL ON NULL INPUT  
	EXTERNAL NAME 'COMPIERE_SQLJ:org.compiere.udf.Compiere.nextID';
	
	


SELECT compiereVersion(), compiereProperty('java.vendor'), 
    getdate() FROM sysibm.sysdummy1;