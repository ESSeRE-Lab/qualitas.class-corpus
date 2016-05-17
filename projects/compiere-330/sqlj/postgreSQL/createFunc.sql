--------------------------------------------------------------------------------
-- FILE                : create procedures and functions, from DatabaseBuild.sql
-- CREATED BY/DATE     : Rapid SQL on 12/23/2006 15:51:01.514
-- COMMENTS            : Built from project Database, change:
--                     : 1. conver Number for integer to integer
--                     : 2. add getDaysBetween, getChars
--                     : 3. convert return from getDaysBetween 
--                     : 4. add  SET CLIENT_ENCODING='LATIN1'; for unicode literal input problem
--                     : 5. change into FOR UPDATE OF tableName
--					   : 6. comment out 	NoRate							EXCEPTION;
--					   : 7. comment out 	<<FINISH_PROCESS>>
--					   : 8. comment out 	commit
--					   : 9. change update correlation id
--					   :10. change delete correlation id
--					   :11. change delete to delete from
--					   :12. change TO_NUMBER(DECODE(..))
--					   :13. change for TO_CHAR(date,'D') returning 01,..07, not 1,...7
--					   :14. Host variable Not the same as column name
--					   :15. Variable in SPL could not be used as source variable if not initialized



--<<FINISH_PROCESS>>
SET CLIENT_ENCODING='LATIN1';

--------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION bomPriceLimit
( 
	Product_ID 				IN INTEGER,
	PriceList_Version_ID	IN INTEGER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2002 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: BOM_PriceLimit.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Return Limit Price of Product/BOM
 * Description:
 *			if not found: 0
 ************************************************************************/
AS
	v_Price			NUMBER;
	v_ProductPrice	NUMBER;
	--	Get BOM Product info
	CURSOR CUR_BOM IS
		SELECT b.M_ProductBOM_ID, b.BOMQty, p.IsBOM
		FROM M_Product_BOM b, M_Product p
		WHERE b.M_ProductBOM_ID=p.M_Product_ID
		  AND b.M_Product_ID=Product_ID;
	--
BEGIN
	--	Try to get price from PriceList directly
	SELECT	COALESCE (SUM(PriceLimit), 0)
      INTO	v_Price
   	FROM	M_ProductPrice
	WHERE M_PriceList_Version_ID=PriceList_Version_ID AND M_Product_ID=Product_ID;
--	DBMS_OUTPUT.PUT_LINE('Price=' || v_Price);

	--	No Price - Check if BOM
       IF (v_Price = 0) THEN
		FOR bom IN CUR_BOM LOOP
                    v_ProductPrice := bomPriceLimit (bom.M_ProductBOM_ID, PriceList_Version_ID);
                    v_Price := v_Price + (bom.BOMQty * v_ProductPrice);
		END LOOP;	
	END IF;
	--
	RETURN v_Price;
END bomPriceLimit;
--end--


CREATE OR REPLACE FUNCTION bomPriceList
( 
	Product_ID 				IN INTEGER,
	PriceList_Version_ID	IN INTEGER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2002 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: BOM_PriceList.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Return List Price of Product/BOM
 * Description:
 *			if not found: 0
 ************************************************************************/
AS
	v_Price			NUMBER;
	v_ProductPrice	NUMBER;
	--	Get BOM Product info
	CURSOR CUR_BOM IS
		SELECT b.M_ProductBOM_ID, b.BOMQty, p.IsBOM
		FROM M_Product_BOM b, M_Product p
		WHERE b.M_ProductBOM_ID=p.M_Product_ID
		  AND b.M_Product_ID=Product_ID;
	--
BEGIN
	--	Try to get price from pricelist directly
	SELECT	COALESCE (SUM(PriceList), 0)
      INTO	v_Price
   	FROM	M_ProductPrice
	WHERE M_PriceList_Version_ID=PriceList_Version_ID AND M_Product_ID=Product_ID;
--	DBMS_OUTPUT.PUT_LINE('Price=' || Price);

	--	No Price - Check if BOM
	IF (v_Price = 0) THEN
		FOR bom IN CUR_BOM LOOP
			v_ProductPrice := bomPriceList (bom.M_ProductBOM_ID, PriceList_Version_ID);
			v_Price := v_Price + (bom.BOMQty * v_ProductPrice);
		--	DBMS_OUTPUT.PUT_LINE('Qry=' || bom.BOMQty || ' @ ' || v_ProductPrice || ', Price=' || v_Price);
		END LOOP;	--	BOM
	END IF;
	--
	RETURN v_Price;
END bomPriceList;
--end--

CREATE OR REPLACE FUNCTION bomPriceStd
( 
	Product_ID 				IN INTEGER,
	PriceList_Version_ID	IN INTEGER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2002 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: BOM_PriceStd.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Return Standard Price of Product/BOM
 * Description:
 *			if not found: 0
 ************************************************************************/
AS
	v_Price			NUMBER;
	v_ProductPrice	NUMBER;
	--	Get BOM Product info
	CURSOR CUR_BOM IS
		SELECT b.M_ProductBOM_ID, b.BOMQty, p.IsBOM
		FROM M_Product_BOM b, M_Product p
		WHERE b.M_ProductBOM_ID=p.M_Product_ID
		  AND b.M_Product_ID=Product_ID;
	--
BEGIN
	--	Try to get price from pricelist directly
	SELECT	COALESCE(SUM(PriceStd), 0)
      INTO	v_Price
   	FROM	M_ProductPrice
	WHERE M_PriceList_Version_ID=PriceList_Version_ID AND M_Product_ID=Product_ID;
--	DBMS_OUTPUT.PUT_LINE('Price=' || v_Price);

	--	No Price - Check if BOM
	IF (v_Price = 0) THEN
		FOR bom IN CUR_BOM LOOP
			v_ProductPrice := bomPriceStd (bom.M_ProductBOM_ID, PriceList_Version_ID);
			v_Price := v_Price + (bom.BOMQty * v_ProductPrice);
		--	DBMS_OUTPUT.PUT_LINE('Price=' || v_Price);
		END LOOP;	--	BOM
	END IF;
	--
	RETURN v_Price;
END bomPriceStd;
--end--

CREATE OR REPLACE FUNCTION bomQtyAvailable
( 
	Product_ID 		IN INTEGER,
    Warehouse_ID	IN INTEGER,
	Locator_ID		IN INTEGER	--	Only used, if warehouse is null
)
RETURN NUMBER
/******************************************************************************
 * ** Compiere Product **             Copyright (c) 1999-2001 Accorto, Inc. USA
 * Open  Source  Software        Provided "AS IS" without warranty or liability
 * When you use any parts (changed or unchanged), add  "Powered by Compiere" to
 * your product name;  See license details http://www.compiere.org/license.html
 ******************************************************************************
 *	Return quantity available for BOM
 */
AS
BEGIN
	RETURN bomQtyOnHand(Product_ID, Warehouse_ID, Locator_ID)
		- bomQtyReserved(Product_ID, Warehouse_ID, Locator_ID);
END bomQtyAvailable;
--end--

CREATE OR REPLACE FUNCTION bomQtyOnHand
( 
	Product_ID 		IN INTEGER,
    Warehouse_ID	IN INTEGER,
	Locator_ID		IN INTEGER	--	Only used, if warehouse is null
)
RETURN NUMBER
/******************************************************************************
 * ** Compiere Product **             Copyright (c) 1999-2001 Accorto, Inc. USA
 * Open  Source  Software        Provided "AS IS" without warranty or liability
 * When you use any parts (changed or unchanged), add  "Powered by Compiere" to
 * your product name;  See license details http://www.compiere.org/license.html
 ******************************************************************************
 *	Return quantity on hand for BOM
 */
AS
	myWarehouse_ID	INTEGER;
 	vQuantity		INTEGER := 99999;	--	unlimited
	vIsBOM			CHAR(1);
	vIsStocked		CHAR(1);
	vProductType		CHAR(1);
 	vProductQty		NUMBER;
	vStdPrecision	INTEGER;
	--	Get BOM Product info
	CURSOR CUR_BOM IS
		SELECT b.M_ProductBOM_ID, b.BOMQty, p.IsBOM, p.IsStocked, p.ProductType
		FROM M_Product_BOM b, M_Product p
		WHERE b.M_ProductBOM_ID=p.M_Product_ID
		  AND b.M_Product_ID=Product_ID;
	--
BEGIN
	--	Check Parameters
	myWarehouse_ID := Warehouse_ID;
	IF (myWarehouse_ID IS NULL) THEN
		IF (Locator_ID IS NULL) THEN
			RETURN 0;
		ELSE
			SELECT 	SUM(M_Warehouse_ID) INTO myWarehouse_ID
			FROM	M_Locator
			WHERE	M_Locator_ID=Locator_ID;
		END IF;
	END IF;
	IF (myWarehouse_ID IS NULL) THEN
		RETURN 0;
	END IF;
--	DBMS_OUTPUT.PUT_LINE('Warehouse=' || myWarehouse_ID);

	--	Check, if product exists and if it is stocked
	BEGIN
		SELECT	IsBOM, ProductType, IsStocked
	 	  INTO	vIsBOM, vProductType, vIsStocked
		FROM M_Product
		WHERE M_Product_ID=Product_ID;
		--
	EXCEPTION	--	not found
		WHEN OTHERS THEN
			RETURN 0;
	END;
	--	Unimited capacity if no item
	IF (vIsBOM='N' AND (vProductType<>'I' OR vIsStocked='N')) THEN
		RETURN vQuantity;
	--	Stocked item
	ELSIF (vIsStocked='Y') THEN
		--	Get ProductQty
		SELECT 	NVL(SUM(QtyOnHand), 0)
		  INTO	vProductQty
		FROM 	M_Storage s
		WHERE M_Product_ID=Product_ID
		  AND EXISTS (SELECT * FROM M_Locator l WHERE s.M_Locator_ID=l.M_Locator_ID
		  	AND l.M_Warehouse_ID=myWarehouse_ID);
		--
	--	DBMS_OUTPUT.PUT_LINE('Qty=' || vProductQty);
		RETURN vProductQty;
	END IF;

	--	Go though BOM
--	DBMS_OUTPUT.PUT_LINE('BOM');
	FOR bom IN CUR_BOM LOOP
		--	Stocked Items "leaf node"
		IF (bom.ProductType = 'I' AND bom.IsStocked = 'Y') THEN
			--	Get vProductQty
			SELECT 	NVL(SUM(QtyOnHand), 0)
			  INTO	vProductQty
			FROM 	M_Storage s
			WHERE M_Product_ID=bom.M_ProductBOM_ID
			  AND EXISTS (SELECT * FROM M_Locator l WHERE s.M_Locator_ID=l.M_Locator_ID
			  	AND l.M_Warehouse_ID=myWarehouse_ID);
			--	Get Rounding Precision
			SELECT 	NVL(MAX(u.StdPrecision), 0)
			  INTO	vStdPrecision
			FROM 	C_UOM u, M_Product p 
			WHERE u.C_UOM_ID=p.C_UOM_ID AND p.M_Product_ID=bom.M_ProductBOM_ID;
			--	How much can we make with this product
			vProductQty := ROUND (vProductQty/bom.BOMQty, vStdPrecision);
			--vProductQty := trunc(vProductQty/bom.BOMQty);
			--	How much can we make overall
			IF (vProductQty < vQuantity) THEN
				vQuantity := vProductQty;
			END IF;
		--	Another BOM
		ELSIF (bom.IsBOM = 'Y') THEN
			vProductQty := bomQtyOnHand (bom.M_ProductBOM_ID, myWarehouse_ID, Locator_ID);
			--	How much can we make overall
			IF (vProductQty < vQuantity) THEN
				vQuantity := vProductQty;
			END IF;
		END IF;
	END LOOP;	--	BOM

	IF (vQuantity > 0) THEN
		--	Get Rounding Precision for Product
		SELECT 	NVL(MAX(u.StdPrecision), 0)
		  INTO	vStdPrecision
		FROM 	C_UOM u, M_Product p 
		WHERE u.C_UOM_ID=p.C_UOM_ID AND p.M_Product_ID=Product_ID;
		--
		RETURN ROUND (vQuantity, vStdPrecision);
		--jz RETURN trunc(vQuantity);
	END IF;
	RETURN 0;
END bomQtyOnHand;
--end--

CREATE OR REPLACE FUNCTION bomQtyOrdered
(
	p_Product_ID 		IN INTEGER,
    p_Warehouse_ID		IN INTEGER,
	p_Locator_ID		IN INTEGER	--	Only used, if warehouse is null
)
RETURN NUMBER
/******************************************************************************
 * ** Compiere Product **             Copyright (c) 1999-2001 Accorto, Inc. USA
 * Open  Source  Software        Provided "AS IS" without warranty or liability
 * When you use any parts (changed or unchanged), add  "Powered by Compiere" to
 * your product name;  See license details http://www.compiere.org/license.html
 ******************************************************************************
 *	Return vQuantity ordered for BOM
 */
AS
	v_Warehouse_ID		INTEGER;
 	v_Quantity			INTEGER := 99999;	--	unlimited
	v_IsBOM				CHAR(1);
	v_IsStocked			CHAR(1);
	v_ProductType		CHAR(1);
 	v_ProductQty		INTEGER;
	v_StdPrecision		INTEGER;
	--	Get BOM Product info
	CURSOR CUR_BOM IS
		SELECT b.M_ProductBOM_ID, b.BOMQty, p.IsBOM, p.IsStocked, p.ProductType
		FROM M_Product_BOM b, M_Product p
		WHERE b.M_ProductBOM_ID=p.M_Product_ID
		  AND b.M_Product_ID=p_Product_ID;
	--
BEGIN
	--	Check Parameters
	v_Warehouse_ID := p_Warehouse_ID;
	IF (v_Warehouse_ID IS NULL) THEN
		IF (p_Locator_ID IS NULL) THEN
			RETURN 0;
		ELSE
			SELECT 	MAX(M_Warehouse_ID) INTO v_Warehouse_ID
			FROM	M_Locator
			WHERE	M_Locator_ID=p_Locator_ID;
		END IF;
	END IF;
	IF (v_Warehouse_ID IS NULL) THEN
		RETURN 0;
	END IF;
--	DBMS_OUTPUT.PUT_LINE('Warehouse=' || v_Warehouse_ID);

	--	Check, if product exists and if it is stocked
	BEGIN
		SELECT	IsBOM, ProductType, IsStocked
		  INTO	v_IsBOM, v_ProductType, v_IsStocked
		FROM 	M_Product
		WHERE 	M_Product_ID=p_Product_ID;
		--
	EXCEPTION	--	not found
		WHEN OTHERS THEN
			RETURN 0;
	END;

	--	No reservation for non-stocked
	IF (v_IsBOM='N' AND (v_ProductType<>'I' OR v_IsStocked='N')) THEN
		RETURN 0;
	--	Stocked item
	ELSIF (v_IsStocked='Y') THEN
		--	Get ProductQty
		SELECT 	NVL(SUM(QtyOrdered), 0)
		  INTO	v_ProductQty
		FROM 	M_Storage s
		WHERE 	M_Product_ID=p_Product_ID
		  AND EXISTS (SELECT * FROM M_Locator l WHERE s.M_Locator_ID=l.M_Locator_ID
		  	AND l.M_Warehouse_ID=v_Warehouse_ID);
		--
		RETURN v_ProductQty;
	END IF;

	--	Go though BOM
--	DBMS_OUTPUT.PUT_LINE('BOM');
	FOR bom IN CUR_BOM LOOP
		--	Stocked Items "leaf node"
		IF (bom.ProductType = 'I' AND bom.IsStocked = 'Y') THEN
			--	Get ProductQty
			SELECT 	NVL(SUM(QtyOrdered), 0)
			  INTO	v_ProductQty
			FROM 	M_Storage s
			WHERE 	M_Product_ID=bom.M_ProductBOM_ID
			  AND EXISTS (SELECT * FROM M_Locator l WHERE s.M_Locator_ID=l.M_Locator_ID
			  	AND l.M_Warehouse_ID=v_Warehouse_ID);
			--	Get Rounding Precision
			SELECT 	NVL(MAX(u.StdPrecision), 0)
			  INTO	v_StdPrecision
			FROM 	C_UOM u, M_Product p 
			WHERE 	u.C_UOM_ID=p.C_UOM_ID AND p.M_Product_ID=bom.M_ProductBOM_ID;
			--	How much can we make with this product
			v_ProductQty := ROUND (v_ProductQty/bom.BOMQty, v_StdPrecision);
			--	How much can we make overall
			IF (v_ProductQty < v_Quantity) THEN
				v_Quantity := v_ProductQty;
			END IF;
		--	Another BOM
		ELSIF (bom.IsBOM = 'Y') THEN
			v_ProductQty := bomQtyOrdered (bom.M_ProductBOM_ID, v_Warehouse_ID, p_Locator_ID);
			--	How much can we make overall
			IF (v_ProductQty < v_Quantity) THEN
				v_Quantity := v_ProductQty;
			END IF;
		END IF;
	END LOOP;	--	BOM

	--	Unlimited (e.g. only services)
	IF (v_Quantity = 99999) THEN
		RETURN 0;
	END IF;

	IF (v_Quantity > 0) THEN
		--	Get Rounding Precision for Product
		SELECT 	NVL(MAX(u.StdPrecision), 0)
		  INTO	v_StdPrecision
		FROM 	C_UOM u, M_Product p 
		WHERE 	u.C_UOM_ID=p.C_UOM_ID AND p.M_Product_ID=p_Product_ID;
		--
		RETURN ROUND (v_Quantity, v_StdPrecision);
	END IF;
	--
	RETURN 0;
END bomQtyOrdered;
--end--

CREATE OR REPLACE FUNCTION bomQtyReserved
(
	p_Product_ID 		IN INTEGER,
    p_Warehouse_ID		IN INTEGER,
	p_Locator_ID		IN INTEGER	--	Only used, if warehouse is null
)
RETURN NUMBER
/******************************************************************************
 * ** Compiere Product **             Copyright (c) 1999-2001 Accorto, Inc. USA
 * Open  Source  Software        Provided "AS IS" without warranty or liability
 * When you use any parts (changed or unchanged), add  "Powered by Compiere" to
 * your product name;  See license details http://www.compiere.org/license.html
 ******************************************************************************
 *	Return quantity reserved for BOM
 */
AS
	v_Warehouse_ID		INTEGER;
 	v_Quantity			INTEGER := 99999;	--	unlimited
	v_IsBOM				CHAR(1);
	v_IsStocked			CHAR(1);
	v_ProductType		CHAR(1);
 	v_ProductQty		INTEGER;
	v_StdPrecision		INTEGER;
	--	Get BOM Product info
	CURSOR CUR_BOM IS
		SELECT b.M_ProductBOM_ID, b.BOMQty, p.IsBOM, p.IsStocked, p.ProductType
		FROM M_Product_BOM b, M_Product p
		WHERE b.M_ProductBOM_ID=p.M_Product_ID
		  AND b.M_Product_ID=p_Product_ID;
	--
BEGIN
	--	Check Parameters
	v_Warehouse_ID := p_Warehouse_ID;
	IF (v_Warehouse_ID IS NULL) THEN
		IF (p_Locator_ID IS NULL) THEN
			RETURN 0;
		ELSE
			SELECT 	MAX(M_Warehouse_ID) INTO v_Warehouse_ID
			FROM	M_Locator
			WHERE	M_Locator_ID=p_Locator_ID;
		END IF;
	END IF;
	IF (v_Warehouse_ID IS NULL) THEN
		RETURN 0;
	END IF;
--	DBMS_OUTPUT.PUT_LINE('Warehouse=' || v_Warehouse_ID);

	--	Check, if product exists and if it is stocked
	BEGIN
		SELECT	IsBOM, ProductType, IsStocked
		  INTO	v_IsBOM, v_ProductType, v_IsStocked
		FROM M_Product
		WHERE M_Product_ID=p_Product_ID;
		--
	EXCEPTION	--	not found
		WHEN OTHERS THEN
			RETURN 0;
	END;

	--	No reservation for non-stocked
	IF (v_IsBOM='N' AND (v_ProductType<>'I' OR v_IsStocked='N')) THEN
		RETURN 0;
	--	Stocked item
	ELSIF (v_IsStocked='Y') THEN
		--	Get ProductQty
		SELECT 	NVL(SUM(QtyReserved), 0)
		  INTO	v_ProductQty
		FROM 	M_Storage s
		WHERE M_Product_ID=p_Product_ID
		  AND EXISTS (SELECT * FROM M_Locator l WHERE s.M_Locator_ID=l.M_Locator_ID
		  	AND l.M_Warehouse_ID=v_Warehouse_ID);
		--
		RETURN v_ProductQty;
	END IF;

	--	Go though BOM
--	DBMS_OUTPUT.PUT_LINE('BOM');
	FOR bom IN CUR_BOM LOOP
		--	Stocked Items "leaf node"
		IF (bom.ProductType = 'I' AND bom.IsStocked = 'Y') THEN
			--	Get ProductQty
			SELECT 	NVL(SUM(QtyReserved), 0)
			  INTO	v_ProductQty
			FROM 	M_Storage s
			WHERE 	M_Product_ID=bom.M_ProductBOM_ID
			  AND EXISTS (SELECT * FROM M_Locator l WHERE s.M_Locator_ID=l.M_Locator_ID
			  	AND l.M_Warehouse_ID=v_Warehouse_ID);
			--	Get Rounding Precision
			SELECT 	NVL(MAX(u.StdPrecision), 0)
			  INTO	v_StdPrecision
			FROM 	C_UOM u, M_Product p 
			WHERE 	u.C_UOM_ID=p.C_UOM_ID AND p.M_Product_ID=bom.M_ProductBOM_ID;
			--	How much can we make with this product
			v_ProductQty := ROUND (v_ProductQty/bom.BOMQty, v_StdPrecision);
			--	How much can we make overall
			IF (v_ProductQty < v_Quantity) THEN
				v_Quantity := v_ProductQty;
			END IF;
		--	Another BOM
		ELSIF (bom.IsBOM = 'Y') THEN
			v_ProductQty := bomQtyReserved (bom.M_ProductBOM_ID, v_Warehouse_ID, p_Locator_ID);
			--	How much can we make overall
			IF (v_ProductQty < v_Quantity) THEN
				v_Quantity := v_ProductQty;
			END IF;
		END IF;
	END LOOP;	--	BOM

	--	Unlimited (e.g. only services)
	IF (v_Quantity = 99999) THEN
		RETURN 0;
	END IF;

	IF (v_Quantity > 0) THEN
		--	Get Rounding Precision for Product
		SELECT 	NVL(MAX(u.StdPrecision), 0)
		  INTO	v_StdPrecision
		FROM 	C_UOM u, M_Product p 
		WHERE 	u.C_UOM_ID=p.C_UOM_ID AND p.M_Product_ID=p_Product_ID;
		--
		RETURN ROUND (v_Quantity, v_StdPrecision);
	END IF;
	RETURN 0;
END bomQtyReserved;
--end--

CREATE OR REPLACE FUNCTION currencyBase
(
	p_Amount			IN	NUMBER,
	p_CurFrom_ID		IN	INTEGER,
	p_ConvDate			IN	DATE,
	p_Client_ID			IN	INTEGER,
	p_Org_ID			IN INTEGER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_Base_Convert.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Convert Amount to Base Currency of Client
 * Description:
 *		Get CurrencyTo from Client
 *		Returns NULL, if conversion not found
 *		Standard Rounding
 * Test:
 *		SELECT C_Base_Convert(100,116,11,null) FROM DUAL => 64.72
 ************************************************************************/
AS
	v_CurTo_ID			INTEGER;
BEGIN
	--	Get Currency
	SELECT	MAX(ac.C_Currency_ID)
	  INTO	v_CurTo_ID
	FROM	AD_ClientInfo ci, C_AcctSchema ac
	WHERE	ci.C_AcctSchema1_ID=ac.C_AcctSchema_ID
	  AND	ci.AD_Client_ID=p_Client_ID;
	--	Same as Currency_Conversion - if currency/rate not found - return 0
	IF (v_CurTo_ID IS NULL) THEN
		RETURN NULL;
	END IF;
	--	Same currency
	IF (p_CurFrom_ID = v_CurTo_ID) THEN
		RETURN p_Amount;
	END IF;

	RETURN currencyConvert (p_Amount, p_CurFrom_ID, v_CurTo_ID, p_ConvDate, null, p_Client_ID, p_Org_ID);
END currencyBase;
--end--

CREATE OR REPLACE FUNCTION invoiceOpen
(
	p_C_Invoice_ID	            IN	INTEGER,
    p_C_InvoicePaySchedule_ID   IN  INTEGER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_Invoice_Open.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Calculate Open Item Amount in Invoice Currency 
 * Description:
 *	Add up total amount open for C_Invoice_ID if no split payment.
 *  Grand Total minus Sum of Allocations in Invoice Currency
 *
 *  For Split Payments:
 *  Allocate Payments starting from first schedule.

SELECT C_Invoice_Open (109) FROM DUAL;
SELECT C_Invoice_Open (109, null) FROM DUAL;
SELECT C_Invoice_Open (109, 11) FROM DUAL;
SELECT C_Invoice_Open (109, 102) FROM DUAL;
SELECT C_Invoice_Open (109, 103) FROM DUAL;
SELECT * FROM RV_OpenItem WHERE C_Invoice_ID=109;
SELECT C_InvoicePaySchedule_ID, DueAmt FROM C_InvoicePaySchedule WHERE C_Invoice_ID=109 ORDER BY DueDate;

 *  Cannot be used for IsPaid as mutating
 ************************************************************************/
AS
	v_Currency_ID		INTEGER;
	v_TotalOpenAmt  	NUMBER := 0;
	v_PaidAmt  	        NUMBER := 0;
	v_Remaining	        NUMBER := 0;
    v_MultiplierAP      NUMBER := 0;
    v_MultiplierCM      NUMBER := 0;
    v_Temp              NUMBER := 0;
    --
	CURSOR	Cur_Alloc	IS
		SELECT	a.AD_Client_ID, a.AD_Org_ID, 
            al.Amount, al.DiscountAmt, al.WriteOffAmt, 
            a.C_Currency_ID, a.DateTrx
		FROM	C_AllocationLine al
          INNER JOIN C_AllocationHdr a ON (al.C_AllocationHdr_ID=a.C_AllocationHdr_ID)
		WHERE	al.C_Invoice_ID = p_C_Invoice_ID
          AND   a.IsActive='Y';
    --
	CURSOR	Cur_PaySchedule	IS
        SELECT  C_InvoicePaySchedule_ID, DueAmt 
        FROM    C_InvoicePaySchedule 
		WHERE	C_Invoice_ID = p_C_Invoice_ID
          AND   IsValid='Y'
        ORDER BY DueDate;

BEGIN
	--	Get Currency
	BEGIN
		SELECT	MAX(C_Currency_ID), SUM(GrandTotal), MAX(MultiplierAP), MAX(Multiplier)
		  INTO	v_Currency_ID, v_TotalOpenAmt, v_MultiplierAP, v_MultiplierCM
		FROM	C_Invoice_v		--	corrected for CM / Split Payment
		WHERE	C_Invoice_ID = p_C_Invoice_ID;
	EXCEPTION	--	Invoice in draft form
		WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('InvoiceOpen - ' || SQLERRM);
			RETURN NULL;
	END;
--  DBMS_OUTPUT.PUT_LINE('== C_Invoice_ID=' || p_C_Invoice_ID || ', Total=' || v_TotalOpenAmt || ', AP=' || v_MultiplierAP || ', CM=' || v_MultiplierCM);

	--	Calculate Allocated Amount
	FOR a IN Cur_Alloc LOOP
        v_Temp := a.Amount + a.DisCountAmt + a.WriteOffAmt;
		v_PaidAmt := v_PaidAmt
        -- Allocation
			+ currencyConvert(v_Temp * v_MultiplierAP,
				a.C_Currency_ID, v_Currency_ID, a.DateTrx, null, a.AD_Client_ID, a.AD_Org_ID);
      DBMS_OUTPUT.PUT_LINE('   PaidAmt=' || v_PaidAmt || ', Allocation=' || v_Temp || ' * ' || v_MultiplierAP);
	END LOOP;
    
    --  Do we have a Payment Schedule ?
    IF (p_C_InvoicePaySchedule_ID > 0) THEN --   if not valid = lists invoice amount
        v_Remaining := v_PaidAmt;
        FOR s IN Cur_PaySchedule LOOP
            IF (s.C_InvoicePaySchedule_ID = p_C_InvoicePaySchedule_ID) THEN
                v_TotalOpenAmt := (s.DueAmt*v_MultiplierCM) + v_Remaining;
                IF (s.DueAmt - v_Remaining < 0) THEN
                    v_TotalOpenAmt := 0;
                END IF;
            --  DBMS_OUTPUT.PUT_LINE('Sched Total=' || v_TotalOpenAmt || ', Due=' || s.DueAmt || ',Remaining=' || v_Remaining || ',CM=' || v_MultiplierCM);
            ELSE -- calculate amount, which can be allocated to next schedule
                v_Remaining := v_Remaining - s.DueAmt;
                IF (v_Remaining < 0) THEN
                    v_Remaining := 0;
                END IF;
            --  DBMS_OUTPUT.PUT_LINE('Remaining=' || v_Remaining);
            END IF;
        END LOOP;
    ELSE
        v_TotalOpenAmt := v_TotalOpenAmt - v_PaidAmt;
    END IF;
--  DBMS_OUTPUT.PUT_LINE('== Total=' || v_TotalOpenAmt);

	--	Ignore Rounding
	IF (v_TotalOpenAmt BETWEEN -0.00999 AND 0.00999) THEN
		v_TotalOpenAmt := 0;
	END IF;
    
	--	Round to penny
	v_TotalOpenAmt := ROUND(COALESCE(v_TotalOpenAmt,0), 2);
	RETURN	v_TotalOpenAmt;
END invoiceOpen;
--end--

CREATE OR REPLACE FUNCTION invoicePaid
(
	p_C_Invoice_ID		IN	INTEGER,
	p_C_Currency_ID	    IN	INTEGER,
	p_MultiplierAP		IN	NUMBER	-- DEFAULT 1
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_Invoice_Paid.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Calculate Paid/Allocated amount in Currency
 * Description:
 *	Add up total amount paid for for C_Invoice_ID.
 *  Split Payments are ignored.
 *  all allocation amounts  converted to invoice C_Currency_ID
 *	round it to the nearest cent
 *	and adjust for CreditMemos by using C_Invoice_v
 *  and for Payments with the multiplierAP (-1, 1)
 *
    SELECT C_Invoice_ID, IsPaid, IsSOTrx, GrandTotal, 
    C_Invoice_Paid (C_Invoice_ID, C_Currency_ID, MultiplierAP)
    FROM C_Invoice_v;
    --
    UPDATE C_Invoice_v1	
 	SET IsPaid = CASE WHEN C_Invoice_Paid(C_Invoice_ID,C_Currency_ID,MultiplierAP)=GrandTotal THEN 'Y' ELSE 'N' END 
    WHERE C_Invoice_ID>1000000
 *	
 ************************************************************************/
AS
	v_MultiplierAP		NUMBER := 1;
	v_PaymentAmt		NUMBER := 0;
	CURSOR	Cur_Alloc	IS
		SELECT	a.AD_Client_ID, a.AD_Org_ID, 
            al.Amount, al.DiscountAmt, al.WriteOffAmt, 
            a.C_Currency_ID, a.DateTrx
		FROM	C_AllocationLine al
          INNER JOIN C_AllocationHdr a ON (al.C_AllocationHdr_ID=a.C_AllocationHdr_ID)
		WHERE	al.C_Invoice_ID = p_C_Invoice_ID
          AND   a.IsActive='Y';
BEGIN
	--	Default
	IF (p_MultiplierAP IS NOT NULL) THEN
		v_MultiplierAP := p_MultiplierAP;
	END IF;
	--	Calculate Allocated Amount
	FOR a IN Cur_Alloc LOOP
		v_PaymentAmt := v_PaymentAmt
			+ currencyConvert(a.Amount + a.DisCountAmt + a.WriteOffAmt,
				a.C_Currency_ID, p_C_Currency_ID, a.DateTrx, null, a.AD_Client_ID, a.AD_Org_ID);
	END LOOP;
	--
	RETURN	ROUND(NVL(v_PaymentAmt,0), 2) * v_MultiplierAP;
END invoicePaid;
--end--

CREATE OR REPLACE FUNCTION paymentAllocated
(
	p_C_Payment_ID	IN	INTEGER,
	p_C_Currency_ID	IN	INTEGER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_Payment_Allocated.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Calculate Allocated Payment Amount in Payment Currency
 * Description:
    --
    SELECT C_Payment_Allocated(C_Payment_ID,C_Currency_ID), PayAmt, IsAllocated
    FROM C_Payment_v 
    WHERE C_Payment_ID>=1000000;
    --
    UPDATE C_Payment_v 
    SET IsAllocated=CASE WHEN C_Payment_Allocated(C_Payment_ID, C_Currency_ID)=PayAmt THEN 'Y' ELSE 'N' END
    WHERE C_Payment_ID>=1000000;
 
 ************************************************************************/
AS
	v_AllocatedAmt		NUMBER := 0;
    v_PayAmt            NUMBER;
	CURSOR	Cur_Alloc	IS
		SELECT	a.AD_Client_ID, a.AD_Org_ID, al.Amount, a.C_Currency_ID, a.DateTrx
		FROM	C_AllocationLine al
          INNER JOIN C_AllocationHdr a ON (al.C_AllocationHdr_ID=a.C_AllocationHdr_ID)
		WHERE	al.C_Payment_ID = p_C_Payment_ID
          AND   a.IsActive='Y';
		--  AND	al.C_Invoice_ID IS NOT NULL;
BEGIN
    --  Charge - nothing available
    SELECT MAX(PayAmt) 
      INTO v_PayAmt
    FROM C_Payment 
    WHERE C_Payment_ID=p_C_Payment_ID AND C_Charge_ID > 0;
    IF (v_PayAmt IS NOT NULL) THEN
        RETURN 0;
    END IF;
    
	--	Calculate Allocated Amount
	FOR a IN Cur_Alloc LOOP
		v_AllocatedAmt := v_AllocatedAmt
			+ currencyConvert(a.Amount, a.C_Currency_ID, p_C_Currency_ID, a.DateTrx, null, a.AD_Client_ID, a.AD_Org_ID);
	END LOOP;
	--	Round to penny
	v_AllocatedAmt := ROUND(NVL(v_AllocatedAmt,0), 2);
	RETURN	v_AllocatedAmt;
END paymentAllocated;
--end--

CREATE OR REPLACE FUNCTION paymentAvailable
(
	p_C_Payment_ID	IN	INTEGER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_Payment_Available.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Calculate Available Payment Amount in Payment Currency
 * Description:
 *		similar to C_Invoice_Open
 ************************************************************************/
AS
	v_Currency_ID		INTEGER;
	v_AvailableAmt		NUMBER := 0;
    v_IsReceipt         C_Payment.IsReceipt%TYPE;
    v_Amt               NUMBER := 0;
	CURSOR	Cur_Alloc	IS
		SELECT	a.AD_Client_ID, a.AD_Org_ID, al.Amount, a.C_Currency_ID, a.DateTrx
		FROM	C_AllocationLine al
          INNER JOIN C_AllocationHdr a ON (al.C_AllocationHdr_ID=a.C_AllocationHdr_ID)
		WHERE	al.C_Payment_ID = p_C_Payment_ID
          AND   a.IsActive='Y';
		--  AND	al.C_Invoice_ID IS NOT NULL;
BEGIN
    --  Charge - fully allocated
    SELECT MAX(PayAmt) 
      INTO v_Amt
    FROM C_Payment 
    WHERE C_Payment_ID=p_C_Payment_ID AND C_Charge_ID > 0;
    IF (v_Amt IS NOT NULL) THEN
        RETURN v_Amt;
    END IF;

	--	Get Currency
	SELECT	C_Currency_ID, PayAmt, IsReceipt
	  INTO	v_Currency_ID, v_AvailableAmt, v_IsReceipt
	FROM	C_Payment_v     -- corrected for AP/AR
	WHERE	C_Payment_ID = p_C_Payment_ID;
--  DBMS_OUTPUT.PUT_LINE('== C_Payment_ID=' || p_C_Payment_ID || ', PayAmt=' || v_AvailableAmt || ', Receipt=' || v_IsReceipt);

	--	Calculate Allocated Amount
	FOR a IN Cur_Alloc LOOP
        v_Amt := currencyConvert(a.Amount, a.C_Currency_ID, v_Currency_ID, a.DateTrx, null, a.AD_Client_ID, a.AD_Org_ID);
	    v_AvailableAmt := v_AvailableAmt - v_Amt;
--      DBMS_OUTPUT.PUT_LINE('  Allocation=' || a.Amount || ' - Available=' || v_AvailableAmt);
	END LOOP;
	--	Ignore Rounding
	IF (v_AvailableAmt BETWEEN -0.00999 AND 0.00999) THEN
		v_AvailableAmt := 0;
	END IF;
	--	Round to penny
	v_AvailableAmt := ROUND(NVL(v_AvailableAmt,0), 2);
	RETURN	v_AvailableAmt;
END paymentAvailable;
--end--

CREATE OR REPLACE FUNCTION paymentTermDiscount
(
	Amount			IN	NUMBER,
    Currency_ID     IN  INTEGER,
	PaymentTerm_ID	IN	INTEGER,
	DocDate			IN	DATE,
	PayDate			IN	DATE
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_PaymentTerm_Discount.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Calculate Discount
 * Description:
 *	Calculate the allowable Discount Amount of the Payment Term
 *
 *	Test:	SELECT C_PaymentTerm_Discount(17777, 103, '10-DEC-1999') FROM DUAL
 ************************************************************************/

AS
	Discount			NUMBER := 0;
	CURSOR Cur_PT	IS
		SELECT	*
		FROM	C_PaymentTerm
		WHERE	C_PaymentTerm_ID = PaymentTerm_ID;
	Discount1Date		DATE;
	Discount2Date		DATE;
	Add1Date			NUMBER := 0;
	Add2Date			NUMBER := 0;
BEGIN
	--	No Data - No Discount
	IF (Amount IS NULL OR PaymentTerm_ID IS NULL OR DocDate IS NULL) THEN
		RETURN 0;
	END IF;

	FOR p IN Cur_PT LOOP	--	for convineance only
--		DBMS_OUTPUT.PUT_LINE(p.Name || ' - Doc = ' || TO_CHAR(DocDate));
		Discount1Date := TRUNC(DocDate + p.DiscountDays + p.GraceDays);
		Discount2Date := TRUNC(DocDate + p.DiscountDays2 + p.GraceDays);

		--	Next Business Day
		IF (p.IsNextBusinessDay='Y') THEN
			--	Not fully correct - only does weekends (7=Saturday, 1=Sunday)
			SELECT 	TO_NUMBER(DECODE(TO_CHAR(Discount1Date,'D'), '07',2, '01',1, 0)),
					TO_NUMBER(DECODE(TO_CHAR(Discount2Date,'D'), '07',2, '01',1, 0))
			  INTO	Add1Date, Add2Date
			FROM 	DUAL;
			Discount1Date := Discount1Date+Add1Date;
			Discount2Date := Discount2Date+Add2Date;
		END IF;

		--	Discount 1
		IF (Discount1Date >= TRUNC(PayDate)) THEN
--			DBMS_OUTPUT.PUT_LINE('Discount 1 ' || TO_CHAR(Discount1Date) || ' ' || p.Discount);
			Discount := Amount * p.Discount / 100;
		--	Discount 2
		ELSIF (Discount2Date >= TRUNC(PayDate)) THEN
--			DBMS_OUTPUT.PUT_LINE('Discount 2 ' || TO_CHAR(Discount2Date) || ' ' || p.Discount2);
			Discount := Amount * p.Discount2 / 100;
		END IF;	
	END LOOP;
	--
    RETURN ROUND(NVL(Discount,0), 2);	--	fixed rounding
END paymentTermDiscount;
--end--

CREATE OR REPLACE FUNCTION paymentTermDueDays
(
	PaymentTerm_ID	IN	INTEGER,
	DocDate			IN	DATE,
	PayDate			IN	DATE
)
RETURN INTEGER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_PaymentTerm_DueDays.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Get Due Days
 * Description:
 *	Returns the days due (positive) or the days till due (negative)
 *	Grace days are not considered!
 *	If record is not found it assumes due immediately
 *
 *	Test:	SELECT C_PaymentTerm_DueDays(103, '01-DEC-2000', '15-DEC-2000') FROM DUAL
 ************************************************************************/
AS
 	Days				INTEGER := 0;
	DueDate				DATE := TRUNC(DocDate);
	--
	CURSOR Cur_PT	IS
		SELECT	*
		FROM	C_PaymentTerm
		WHERE	C_PaymentTerm_ID = PaymentTerm_ID;
	FirstDay			DATE;
	NoDays				INTEGER;
BEGIN
	FOR p IN Cur_PT LOOP	--	for convineance only
	--	DBMS_OUTPUT.PUT_LINE(p.Name || ' - Doc = ' || TO_CHAR(DocDate));
		--	Due 15th of following month
		IF (p.IsDueFixed = 'Y') THEN		
		--	DBMS_OUTPUT.PUT_LINE(p.Name || ' - Day = ' || p.FixMonthDay);
			FirstDay := TRUNC(DocDate, 'MM');
			NoDays := TRUNC(DocDate) - FirstDay;
			DueDate := FirstDay + (p.FixMonthDay-1);	--	starting on 1st
			DueDate := ADD_MONTHS(DueDate, p.FixMonthOffset);
			IF (NoDays > p.FixMonthCutoff) THEN
				DueDate := ADD_MONTHS(DueDate, 1);
			END IF;
		ELSE
		--	DBMS_OUTPUT.PUT_LINE('Net = ' || p.NetDays);
			DueDate := TRUNC(DocDate) + p.NetDays;
		END IF;
	END LOOP;
--	DBMS_OUTPUT.PUT_LINE('Due = ' || TO_CHAR(DueDate) || ', Pay = ' || TO_CHAR(PayDate));

--	Days := TRUNC(PayDate) - DueDate;
	Days := getDaysBetween(TRUNC(PayDate),DueDate);
	RETURN Days;
END paymentTermDueDays;
--end--

CREATE OR REPLACE PROCEDURE AD_Sequence_Doc 
(
	p_SequenceName	IN	VARCHAR2,
	p_AD_Client_ID	IN	INTEGER,
	o_DocumentNo	OUT VARCHAR2
 )
AS
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: AD_Sequence_Doc.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Get the next DocumentNo of TableName
 * Description:
 *		store in parameter o_DocumentNo
 *		if ID < 1000000, use System Doc Sequence
 ************************************************************************/
	v_NextNo			INTEGER;
	v_NextNoSys			INTEGER;
	v_Prefix			VARCHAR2(30);
	v_Suffix			VARCHAR2(30);
BEGIN
	SELECT	CurrentNext, CurrentNextSys, Prefix, Suffix
	  INTO	v_NextNo, v_NextNoSys, v_Prefix, v_Suffix
	FROM	AD_Sequence
	WHERE	Name = p_SequenceName
	  AND	IsActive = 'Y'
	  AND	IsTableID = 'N'
	  AND	IsAutoSequence = 'Y'
	  AND	AD_Client_ID = p_AD_Client_ID
	FOR UPDATE OF AD_Sequence;
--	FOR UPDATE OF CurrentNext, CurrentNextSys;

	IF (v_NextNoSys <> -1 AND p_AD_Client_ID < 1000000) THEN	--	System No
		UPDATE	AD_Sequence
		  SET	CurrentNextSys = CurrentNextSys + IncrementNo,
				Updated = SysDate
		WHERE	Name = p_SequenceName;
		o_DocumentNo := NVL(v_Prefix, '') || v_NextNoSys || NVL(v_Suffix, '');
	ELSE								--	Standard No
		UPDATE	AD_Sequence
		  SET	CurrentNext = CurrentNext + IncrementNo,
				Updated = SysDate
		WHERE	Name = p_SequenceName;
		o_DocumentNo := NVL(v_Prefix, '') || v_NextNo || NVL(v_Suffix, '');
	END IF;

EXCEPTION
	WHEN NO_DATA_FOUND THEN
		RAISE_APPLICATION_ERROR (-20100, 'Document Sequence not found - ' || p_SequenceName);

END AD_Sequence_Doc;
--end--

CREATE OR REPLACE PROCEDURE AD_Sequence_DocType
(
	p_DocType_ID		IN	INTEGER,
	p_ID				IN	INTEGER,
	p_DocumentNo		OUT	VARCHAR2
 )
AS
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: AD_Sequence_DocType.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Get the next DocumentNo of Document Type
 * Description:
 *		store in parameter p_DocumentNo
 *		If ID < 1000000, use System Doc Sequence
 *		If no Document Sequence is defined, return null !
 *			Use AD_Sequence_Doc('DocumentNo_myTable',.. to get it directly
 ************************************************************************/

	v_NextNo			INTEGER;
	v_NextNoSys			INTEGER;
	v_Sequence_ID		INTEGER	:= NULL;
	v_Prefix			VARCHAR2(30);
	v_Suffix			VARCHAR2(30);
BEGIN
	--	Is a document Sequence defined and valid?
	BEGIN
		SELECT	DocNoSequence_ID
		  INTO	v_Sequence_ID
		FROM	C_DocType
		WHERE	C_DocType_ID=p_DocType_ID	--	parameter
		  AND	IsDocNoControlled='Y'
		  AND	IsActive='Y';
	EXCEPTION
		WHEN OTHERS THEN
			NULL;
	END;
    
	IF (v_Sequence_ID IS NULL) THEN		--	No Sequence Number
		p_DocumentNo := '';				--	Return NULL
		DBMS_OUTPUT.PUT_LINE('[AD_Sequence_DocType: not found - C_DocType_ID=' || p_DocType_ID || ']');
		RETURN;
	END IF;

	--	Get the numbers
	SELECT	s.AD_Sequence_ID, s.CurrentNext, s.CurrentNextSys, s.Prefix, s.Suffix
	  INTO	v_Sequence_ID, v_NextNo, v_NextNoSys, v_Prefix, v_Suffix
	FROM	C_DocType d, AD_Sequence s
	WHERE	d.C_DocType_ID=p_DocType_ID	--	parameter
	  AND	d.DocNoSequence_ID=s.AD_Sequence_ID
	  AND	s.IsActive = 'Y'
	  AND	s.IsTableID = 'N'
	  AND	s.IsAutoSequence = 'Y'
	FOR UPDATE OF CurrentNext, CurrentNextSys;
--	FOR UPDATE OF AD_Sequence;

	IF (v_NextNoSys <> -1 AND p_ID < 1000000) THEN	--	System No
		UPDATE	AD_Sequence
		  SET	CurrentNextSys = CurrentNextSys + IncrementNo
		WHERE	AD_Sequence_ID = v_Sequence_ID;
		p_DocumentNo := NVL(v_Prefix, '') || v_NextNoSys || NVL(v_Suffix, '');
	ELSE						--	Standard No
		UPDATE AD_Sequence
		  SET CurrentNext = CurrentNext + IncrementNo
		WHERE AD_Sequence_ID = v_Sequence_ID;
		p_DocumentNo := NVL(v_Prefix, '') || v_NextNo || NVL(v_Suffix, '');
	END IF;
--	DBMS_OUTPUT.PUT_LINE(p_DocumentNo);

EXCEPTION
	WHEN NO_DATA_FOUND THEN
		RAISE_APPLICATION_ERROR (-20100, 'AD_Sequence_DocType: not found - DocType_ID='
			|| p_DocType_ID || ', Sequence_ID=' || v_Sequence_ID);

END AD_Sequence_DocType;
--end--

CREATE OR REPLACE PROCEDURE AD_Sequence_Next 
(
	p_TableName		IN	VARCHAR2,
	p_ID			IN	INTEGER,
	p_NextNo		OUT	INTEGER
 )
AS
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: AD_Sequence_Next.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Get the next sequence number of TableName
 * Description:
 *		store in parameter p_NextNo
 *		if ID < 1000000, use System Doc Sequence
 ************************************************************************/

	v_NextNoSys		INTEGER;
	v_ResultStr		VARCHAR(255);
BEGIN
	v_ResultStr := 'Read';
	SELECT CurrentNext, CurrentNextSys
	  INTO p_NextNo, v_NextNoSys
	FROM AD_Sequence
	WHERE Name = p_TableName
	  AND IsActive = 'Y'
	  AND IsTableID = 'Y'
	  AND IsAutoSequence = 'Y'
	FOR UPDATE OF CurrentNext, CurrentNextSys;
--	FOR UPDATE OF AD_Sequence;

	v_ResultStr := 'Write';
	IF (v_NextNoSys <> -1 AND p_ID < 1000000) THEN	--	System No
		UPDATE 	AD_Sequence
		  SET 	CurrentNextSys = CurrentNextSys + IncrementNo,
				Updated = SysDate
		WHERE 	Name = p_TableName;
		p_NextNo := v_NextNoSys;
	ELSE						--	Standard No
		UPDATE 	AD_Sequence
		  SET	CurrentNext = CurrentNext + IncrementNo,
				Updated = SysDate
		WHERE Name = p_TableName;
	END IF;

EXCEPTION
  WHEN NO_DATA_FOUND THEN
	RAISE_APPLICATION_ERROR (-20100, 'Table Sequence not found ');
-- 		|| v_ResultStr || ': ' || p_TableName);

END AD_Sequence_Next;
--end--

CREATE OR REPLACE FUNCTION currencyRound
(
	p_Amount		IN	NUMBER,
	p_CurTo_ID	IN	INTEGER,
	p_Costing		IN	VARCHAR2		--	Default 'N'
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_Currency_Round.SQL,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Round amount for Traget Currency
 * Description:
 *		Round Amount using Costing or Standard Precision
 *		Returns unmodified amount if currency not found
 * Test:
 *		SELECT C_Currency_Round(C_Currency_Convert(100,116,100,null,null),100,null) FROM DUAL => 64.72 
 ************************************************************************/
AS
	v_StdPrecision		INTEGER;
	v_CostPrecision		INTEGER;
BEGIN
	--	Nothing to convert
	IF (p_Amount IS NULL OR p_CurTo_ID IS NULL) THEN
		RETURN p_Amount;
	END IF;

	--	Ger Precision
	SELECT	MAX(StdPrecision), MAX(CostingPrecision)
	  INTO	v_StdPrecision, v_CostPrecision
	FROM	C_Currency
	  WHERE	C_Currency_ID = p_CurTo_ID;
	--	Currency Not Found
	IF (v_StdPrecision IS NULL) THEN
		RETURN p_Amount;
	END IF;

	IF (p_Costing = 'Y') THEN
		RETURN ROUND (p_Amount, v_CostPrecision);
	END IF;

	RETURN ROUND (p_Amount, v_StdPrecision);
END currencyRound;
--end--

CREATE OR REPLACE PROCEDURE C_Order_DrillDown
(
	PInstance_ID		IN INTEGER
)
/******************************************************************************
 * ** Compiere Product **             Copyright (c) 1999-2001 Accorto, Inc. USA
 * Open  Source  Software        Provided "AS IS" without warranty or liability
 * When you use any parts (changed or unchanged), add  "Powered by Compiere" to
 * your product name;  See license details http://www.compiere.org/license.html
 ******************************************************************************
 *	List Orders with their Shipments and Invoices
 *	Spool to T_Spool
 */
AS
	ResultStr						VARCHAR2(2000);
	Message							VARCHAR2(2000);
	Record_ID						INTEGER;
	CURSOR Cur_Parameter (PInstance INTEGER) IS
		SELECT i.Record_ID, p.ParameterName, p.P_String, p.P_Number, p.P_Date
		FROM AD_PInstance i, AD_PInstance_Para p
		WHERE i.AD_PInstance_ID=PInstance
		AND i.AD_PInstance_ID=p.AD_PInstance_ID(+)
		ORDER BY p.SeqNo;
	--	Parameter
	C_Order_ID						INTEGER;
	--
	CURSOR Cur_Order IS
		SELECT	o.C_Order_ID, d.Name, o.DocumentNo, o.DocStatus, o.DocAction, o.Processed
		FROM	C_Order o, C_DocType d
		WHERE	o.C_Order_ID=C_Order_ID
		  AND	o.C_DocType_ID=d.C_DocType_ID
		ORDER BY o.DocumentNo DESC;

BEGIN
	--	No locking or Updating

	--	Get Parameters
	ResultStr := 'ReadingParameters';
	FOR p IN Cur_Parameter (PInstance_ID) LOOP
		Record_ID := p.Record_ID;
		IF (p.ParameterName = 'C_Order_ID') THEN
 			C_Order_ID := p.P_Number;
			DBMS_OUTPUT.PUT_LINE('  C_Order_ID=' || C_Order_ID);
		ELSE
			DBMS_OUTPUT.PUT_LINE('*** Unknown Parameter=' || p.ParameterName);
	 	END IF;
	END LOOP;	--	Get Parameter
	DBMS_OUTPUT.PUT_LINE('  Record_ID=' || Record_ID);

	IF (C_Order_ID IS NULL) THEN
		C_Order_ID := Record_ID;
	END IF;

	--	Should be nothing there
	DELETE FROM 	T_Spool
	WHERE	AD_PInstance_ID=PInstance_ID;

	--	Order Info
	FOR o IN Cur_Order LOOP

		INSERT INTO T_Spool (AD_PInstance_ID, SeqNo, MsgText) VALUES (PInstance_ID, T_Spool_Seq.NextVal, 
			o.Name || ' ' || o.DocumentNo || ':  @DocStatus@=' || o.DocStatus
			|| ', @DocAction@=' || o.DocAction || ', @Processed@=' || o.Processed);

		--	Order Lines
		DECLARE
			CURSOR	Cur_OrderLine	IS
				SELECT	*
				FROM	C_OrderLine
				WHERE	C_Order_ID=o.C_Order_ID
				ORDER BY Line;
		BEGIN
			FOR ol IN Cur_OrderLine LOOP
				INSERT INTO T_Spool (AD_PInstance_ID, SeqNo, MsgText) VALUES (PInstance_ID, T_Spool_Seq.NextVal, 
					'   @QtyOrdered@=' || ol.QtyOrdered || ', @QtyReserved@=' || ol.QtyReserved
					|| ', @QtyDelivered@=' || ol.QtyDelivered || ', @QtyInvoiced@=' || ol.QtyInvoiced
					|| ' - Wh=' || ol.M_Warehouse_ID
					|| ', Prd=' || ol.M_Product_ID);
			END LOOP;
		END;
	
		-- Shipment
		DECLARE
			CURSOR 	Cur_InOut		IS
				SELECT	s.M_InOut_ID, d.Name, s.DocumentNo, s.DocStatus, s.Processed, s.M_Warehouse_ID
				FROM	M_InOut s, C_DocType d
				WHERE	s.C_Order_ID = o.C_Order_ID
				  AND	s.C_DocType_ID=d.C_DocType_ID;
		BEGIN
			FOR s IN Cur_InOut LOOP
				INSERT INTO T_Spool (AD_PInstance_ID, SeqNo, MsgText) VALUES (PInstance_ID, T_Spool_Seq.NextVal, 
					'> ' || s.Name || ' ' || s.DocumentNo || ':  @DocStatus@=' || s.DocStatus 
					|| ', @Processed@=' || s.Processed || ', Wh=' || s.M_Warehouse_ID);

				--	Shipment Lines
				DECLARE
					CURSOR	Cur_InOutLine	IS
						SELECT	*
						FROM	M_InOutLine
						WHERE	M_InOut_ID=s.M_InOut_ID
						ORDER BY Line;
				BEGIN
					FOR sl IN Cur_InOutLine LOOP
						INSERT INTO T_Spool (AD_PInstance_ID, SeqNo, MsgText) VALUES (PInstance_ID, T_Spool_Seq.NextVal, 
							'   @QtyDelivered@=' || sl.MovementQty || ', Prd=' || sl.M_Product_ID);
					END LOOP;
				END;	-- 	Shipment Lines
 			END LOOP;	--	Shipments
		END; --	Shipment

		-- Invoice
		DECLARE
			CURSOR 	Cur_Invoice		IS
				SELECT	i.C_Invoice_ID, d.Name, i.DocumentNo, i.DocStatus, i.Processed
				FROM	C_Invoice i, C_DocType d
				WHERE	i.C_DocType_ID=d.C_DocType_ID
				  AND EXISTS (SELECT * FROM C_InvoiceLine l, C_OrderLine ol
 				  	WHERE 	i.C_Invoice_ID = l.C_Invoice_ID
					  AND	l.C_OrderLine_ID = ol.C_OrderLine_ID
					  AND	ol.C_Order_ID=o.C_Order_ID);
		BEGIN
			FOR i IN Cur_Invoice LOOP

				INSERT INTO T_Spool (AD_PInstance_ID, SeqNo, MsgText) VALUES (PInstance_ID, T_Spool_Seq.NextVal, 
					'> ' || i.Name || ' ' || i.DocumentNo || ':  @DocStatus@=' || i.DocStatus
					|| ', @Processed@=' || i.Processed);

				--	Invoice Lines
				DECLARE
					CURSOR	Cur_InvoiceLine	IS
						SELECT	*
						FROM	C_InvoiceLine
						WHERE	C_Invoice_ID=i.C_Invoice_ID
						ORDER BY Line;
				BEGIN
					FOR il IN Cur_InvoiceLine LOOP
						INSERT INTO T_Spool (AD_PInstance_ID, SeqNo, MsgText) VALUES (PInstance_ID, T_Spool_Seq.NextVal, 
							'   @QtyInvoiced@=' || il.QtyInvoiced || ', Prd=' || il.M_Product_ID);
					END LOOP;
				END;	-- 	Invoice Lines
 			END LOOP;	--	Invoices
		END; --	Invoice
	
	END LOOP;	-- Order


<<FINISH_PROCESS>>
	--  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Finished ' || Message);
    UPDATE  AD_PInstance
    SET Updated = SysDate,
        IsProcessing = 'N',
        Result = 1,                 -- success
        ErrorMsg = Message
    WHERE   AD_PInstance_ID=PInstance_ID;
    -- COMMIT;
    RETURN;

EXCEPTION
    WHEN  OTHERS THEN
		ResultStr := ResultStr || ': ' || SQLERRM || ' - ' || Message;
		DBMS_OUTPUT.PUT_LINE(ResultStr);
        UPDATE  AD_PInstance
        SET Updated = SysDate,
            IsProcessing = 'N',
            Result = 0,             -- failure
            ErrorMsg = ResultStr
        WHERE   AD_PInstance_ID=PInstance_ID;
        --COMMIT;
        RETURN;

END C_Order_DrillDown;
--end--

CREATE OR REPLACE FUNCTION DBA_ConstraintCmd
(
	p_ConstraintName	IN	VARCHAR2
)
RETURN VARCHAR2
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: DBA_ConstraintCmd.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 * $Source: /cvs/compiere/db/database/Functions/DBA_ConstraintCmd.sql,v $
 ***
 * Title:	 Create DML command for given constraint
 * Description:
 *		SELECT DBA_ConstraintCmd(Constraint_Name) FROM User_Constraints WHERE CONSTRAINT_TYPE='R'
 ************************************************************************/
AS
	v_Result			VARCHAR2(2000);
	v_TableName			VARCHAR2(256);
	v_ColumnName		VARCHAR2(256);
	v_ConstraintName	VARCHAR2(256);
	v_DeleteRule		VARCHAR2(256);
BEGIN
	--	Get First Part
	SELECT c.Table_Name, cc.Column_name, c.R_Constraint_Name, c.Delete_Rule
	  INTO	v_TableName, v_ColumnName, v_ConstraintName, v_DeleteRule
	FROM USER_Constraints c, USER_Cons_Columns cc
	WHERE c.Constraint_Name=cc.Constraint_Name
	  AND cc.Constraint_Name=p_ConstraintName;
	--	Create First Part
	v_Result := 'ALTER TABLE ' || v_TableName || ' ADD CONSTRAINT ' || p_ConstraintName
		|| ' FOREIGN KEY (' || v_ColumnName || ') ';

	--	Not a valid FK Reference
	IF (v_ConstraintName IS NULL) THEN
		RETURN NULL;
   	END IF;

	--	Get Second Part
	SELECT c.Table_Name, cc.Column_name
	  INTO	v_TableName, v_ColumnName
	FROM USER_Constraints c, USER_Cons_Columns cc
	WHERE c.Constraint_Name=cc.Constraint_Name
	  AND cc.Constraint_Name=v_ConstraintName;
	--	Create Second Part
	v_Result := v_Result || 'REFERENCES ' || v_TableName || '(' || v_ColumnName || ')';

	IF (v_DeleteRule = 'CASCADE') THEN
		v_Result := v_Result || ' ON DELETE CASCADE';
   	END IF;
--	DBMS_OUTPUT.PUT_LINE(v_Result);
	RETURN v_Result;

END DBA_ConstraintCmd;
--end--


CREATE OR REPLACE PROCEDURE M_PriceList_Create
( 
	PInstance_ID			IN INTEGER
)
AS
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2003 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: M_PriceList_Create.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Create Pricelist
 * Description:
 *		Create PriceList by copying purchase prices (M_Product_PO) 
 *		and applying product category discounts (M_CategoryDiscount)
 ************************************************************************/
	--	Logistice
	ResultStr						VARCHAR2(2000);
	Message							VARCHAR2(2000) := '';
	-- NoRate							EXCEPTION;
	--	Parameter
	CURSOR Cur_Parameter (PInstance INTEGER) IS
		SELECT i.Record_ID, p.ParameterName, p.P_String, p.P_Number, p.P_Date
		FROM AD_PInstance i, AD_PInstance_Para p
		WHERE i.AD_PInstance_ID=PInstance
		AND i.AD_PInstance_ID=p.AD_PInstance_ID(+)
		ORDER BY p.SeqNo;
	--	Parameter Variables
	p_PriceList_Version_ID			INTEGER;
	p_DeleteOld						CHAR(1) := 'N';
	--
	v_Currency_ID					INTEGER;
	v_Client_ID						INTEGER;
	v_Org_ID						INTEGER;
	v_UpdatedBy						INTEGER;
	v_StdPrecision					INTEGER;
	v_DiscountSchema_ID				INTEGER;
	v_PriceList_Version_Base_ID		INTEGER;
	--
	v_NextNo						INTEGER := 0;

	--	Get PL Parameter
	CURSOR Cur_DiscountLine (DiscountSchema_ID INTEGER) IS
		SELECT	* 
		FROM	M_DiscountSchemaLine
		WHERE	M_DiscountSchema_ID=DiscountSchema_ID
		  AND	IsActive='Y'
		ORDER BY SeqNo;

BEGIN
	--  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Processing');
	ResultStr := 'PInstanceNotFound';
	UPDATE AD_PInstance
	SET Created = SysDate,
		IsProcessing = 'Y'
	WHERE AD_PInstance_ID=PInstance_ID;
	--COMMIT;

	--	Get Parameters
	ResultStr := 'ReadingParameters';
	FOR p IN Cur_Parameter (PInstance_ID) LOOP
		p_PriceList_Version_ID := p.Record_ID;
		IF (p.ParameterName = 'DeleteOld') THEN
			p_DeleteOld := p.P_String;
			DBMS_OUTPUT.PUT_LINE('  DeleteOld=' || p_DeleteOld);
		ELSE
			DBMS_OUTPUT.PUT_LINE('*** Unknown Parameter=' || p.ParameterName);
		END IF;
	END LOOP;	--	Get Parameter
	DBMS_OUTPUT.PUT_LINE('  PriceList_Version_ID=' || p_PriceList_Version_ID);

	--	Checking Prerequisites
	--	--	PO Prices must exists
	ResultStr := 'CorrectingProductPO';
	DBMS_OUTPUT.PUT_LINE(ResultStr);
	UPDATE	M_Product_PO
	  SET	PriceList = 0
	WHERE	PriceList IS NULL;
	UPDATE	M_Product_PO	
	  SET	PriceLastPO = 0
	WHERE	PriceLastPO IS NULL;
	UPDATE	M_Product_PO
	  SET	PricePO = PriceLastPO
	WHERE	(PricePO IS NULL OR PricePO = 0) AND PriceLastPO <> 0;
	UPDATE	M_Product_PO
	  SET	PricePO = 0
	WHERE	PricePO IS NULL;
	-- Set default current vendor
	--jz changed correlation ID 'p'
	--UPDATE	M_Product_PO p
	UPDATE	M_Product_PO 
	  SET	IsCurrentVendor = 'Y'
	WHERE	IsCurrentVendor = 'N' 
	  AND NOT EXISTS 
		(SELECT pp.M_Product_ID FROM M_Product_PO pp
		WHERE pp.M_Product_ID=M_Product_PO.M_Product_ID
		GROUP BY pp.M_Product_ID HAVING COUNT(*) > 1);
	--COMMIT;

	/**
	 *	Make sure that we have only one active product
	 */
	ResultStr := 'CorrectingDuplicates';
	DBMS_OUTPUT.PUT_LINE(ResultStr);
	DECLARE
		--	All duplicate products
		CURSOR	Cur_Duplicates	IS
			SELECT	DISTINCT M_Product_ID
			FROM	M_Product_PO po
			WHERE	IsCurrentVendor='Y' AND IsActive='Y'
			  AND EXISTS (	SELECT M_Product_ID FROM M_Product_PO x 
							WHERE x.M_Product_ID=po.M_Product_ID 
							GROUP BY M_Product_ID HAVING COUNT(*) > 1 )
			ORDER BY 1;
		--	All vendors of Product - expensive first
		CURSOR	Cur_Vendors	(Product_ID INTEGER) IS
			SELECT	M_Product_ID, C_BPartner_ID
			FROM	M_Product_PO
			WHERE	IsCurrentVendor='Y' AND IsActive='Y'
			  AND	M_Product_ID=Product_ID
			ORDER BY PriceList DESC;
		--
		Product_ID				INTEGER;
		BPartner_ID				INTEGER;
	BEGIN
		FOR dupl IN Cur_Duplicates LOOP
			OPEN Cur_Vendors (dupl.M_Product_ID);
			FETCH Cur_Vendors INTO Product_ID, BPartner_ID;		--	Leave First
			LOOP
				FETCH Cur_Vendors INTO Product_ID, BPartner_ID;	--	Get Record ID
				EXIT WHEN Cur_Vendors%NOTFOUND;
				--
				DBMS_OUTPUT.PUT_LINE('  Record: ' || Product_ID || ' / ' || BPartner_ID);
				UPDATE	M_Product_PO
				  SET	IsCurrentVendor='N'
				WHERE	M_Product_ID=Product_ID AND C_BPartner_ID=BPartner_ID;
			END LOOP;
			CLOSE Cur_Vendors;
		END LOOP;
		--COMMIT;
	END;
	
	/**	Delete Old Data	*/
	ResultStr := 'DeletingOld';
	IF (p_DeleteOld = 'Y') THEN
		DELETE	M_ProductPrice
		WHERE	M_PriceList_Version_ID = p_PriceList_Version_ID;
		Message := '@Deleted@=' || SQL%ROWCOUNT || ' - ';
		DBMS_OUTPUT.PUT_LINE(Message);
	END IF;

	--	Get PriceList Info
	ResultStr := 'GetPLInfo';
	DBMS_OUTPUT.PUT_LINE(ResultStr);
	SELECT	p.C_Currency_ID, c.StdPrecision,
		v.AD_Client_ID, v.AD_Org_ID, v.UpdatedBy, 
		v.M_DiscountSchema_ID, M_PriceList_Version_Base_ID
	  INTO	v_Currency_ID, v_StdPrecision,
		v_Client_ID, v_Org_ID, v_UpdatedBy, 
		v_DiscountSchema_ID, v_PriceList_Version_Base_ID
	FROM	M_PriceList p, M_PriceList_Version v, C_Currency c
	WHERE	p.M_PriceList_ID=v.M_PriceList_ID 
	  AND	p.C_Currency_ID=c.C_Currency_ID
	  AND	v.M_PriceList_Version_ID=p_PriceList_Version_ID;

	/**
	 *	For All Discount Lines in Sequence
	 */
	FOR dl IN Cur_DiscountLine (v_DiscountSchema_ID) LOOP
		ResultStr := 'Parameter Seq=' || dl.SeqNo;
	--	DBMS_OUTPUT.PUT_LINE(ResultStr);

		--	Clear Temporary Table
		DELETE FROM T_Selection;

		--	-----------------------------------
		--	Create Selection in temporary table
		--	-----------------------------------
		IF (v_PriceList_Version_Base_ID IS NULL) THEN
		--	Create Selection from M_Product_PO
			INSERT INTO T_Selection (T_Selection_ID)
			SELECT	DISTINCT po.M_Product_ID 
			FROM	M_Product p, M_Product_PO po
			WHERE	p.M_Product_ID=po.M_Product_ID
			  AND	(p.AD_Client_ID=v_Client_ID OR p.AD_Client_ID=0)
			  AND	p.IsActive='Y' AND po.IsActive='Y' AND po.IsCurrentVendor='Y'
			--	Optional Restrictions
			  AND (dl.M_Product_Category_ID IS NULL OR p.M_Product_Category_ID=dl.M_Product_Category_ID)
			  AND (dl.C_BPartner_ID IS NULL OR po.C_BPartner_ID=dl.C_BPartner_ID)
			  AND (dl.M_Product_ID IS NULL OR p.M_Product_ID=dl.M_Product_ID);
		ELSE
		--	Create Selection from existing PriceList
			INSERT INTO T_Selection (T_Selection_ID)
			SELECT	DISTINCT p.M_Product_ID 
			FROM	M_Product p, M_ProductPrice pp
			WHERE	p.M_Product_ID=pp.M_Product_ID
			  AND	pp.M_PriceList_Version_ID=v_PriceList_Version_Base_ID
			  AND	p.IsActive='Y' AND pp.IsActive='Y'
			--	Optional Restrictions
			  AND	(dl.M_Product_Category_ID IS NULL OR p.M_Product_Category_ID=dl.M_Product_Category_ID)
			  AND	(dl.C_BPartner_ID IS NULL OR EXISTS 
					(SELECT * FROM M_Product_PO po WHERE po.M_Product_ID=p.M_Product_ID AND po.C_BPartner_ID=dl.C_BPartner_ID))
			  AND	(dl.M_Product_ID IS NULL OR p.M_Product_ID=dl.M_Product_ID);
		END IF;
		Message := Message || '@Selected@=' || SQL%ROWCOUNT;
	--	DBMS_OUTPUT.PUT_LINE(Message);

		--	Delete Prices in Selection, so that we can insert
		IF (v_PriceList_Version_Base_ID IS NULL
				OR v_PriceList_Version_Base_ID <> p_PriceList_Version_ID) THEN
			ResultStr := ResultStr || ', Delete';
			DELETE	M_ProductPrice pp
			WHERE	pp.M_PriceList_Version_ID = p_PriceList_Version_ID
			  AND EXISTS (SELECT * FROM T_Selection s WHERE pp.M_Product_ID=s.T_Selection_ID);
			Message := ', @Deleted@=' || SQL%ROWCOUNT;
		END IF;

		--	--------------------
		--	Copy (Insert) Prices
		--	--------------------
		IF (v_PriceList_Version_Base_ID = p_PriceList_Version_ID) THEN
		--	We have Prices already
			NULL;
		ELSIF (v_PriceList_Version_Base_ID IS NULL) THEN
		--	Copy and Convert from Product_PO
			ResultStr := ResultStr || ',Copy_PO';
			INSERT INTO M_ProductPrice
				(M_PriceList_Version_ID, M_Product_ID,
				AD_Client_ID, AD_Org_ID, IsActive, Created, CreatedBy, Updated, UpdatedBy,
				PriceList, PriceStd, PriceLimit)
			SELECT 
				p_PriceList_Version_ID, po.M_Product_ID, 
				v_Client_ID, v_Org_ID, 'Y', SysDate, v_UpdatedBy, SysDate, v_UpdatedBy,
				--	Price List
				COALESCE(currencyConvert(po.PriceList, 
					po.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0), 
				--	Price Std
				COALESCE(currencyConvert(po.PriceList, 
					po.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0), 
				--	Price Limit
				COALESCE(currencyConvert(po.PricePO,
						po.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0)
			FROM	M_Product_PO po
			WHERE EXISTS (SELECT * FROM T_Selection s WHERE po.M_Product_ID=s.T_Selection_ID)
			  AND	po.IsCurrentVendor='Y' AND po.IsActive='Y';
		ELSE
		--	Copy and Convert from other PriceList_Version
			ResultStr := ResultStr || ',Copy_PL';
			INSERT INTO M_ProductPrice
				(M_PriceList_Version_ID, M_Product_ID,
				AD_Client_ID, AD_Org_ID, IsActive, Created, CreatedBy, Updated, UpdatedBy,
				PriceList, PriceStd, PriceLimit)
			SELECT 
				p_PriceList_Version_ID, pp.M_Product_ID, 
				v_Client_ID, v_Org_ID, 'Y', SysDate, v_UpdatedBy, SysDate, v_UpdatedBy,
				--	Price List
				COALESCE(currencyConvert(pp.PriceList, 
					pl.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0), 
				--	Price Std
				COALESCE(currencyConvert(pp.PriceStd, 
					pl.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0), 
				--	Price Limit
				COALESCE(currencyConvert(pp.PriceLimit,
					pl.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0)
			FROM M_ProductPrice pp
                INNER JOIN M_PriceList_Version plv ON (pp.M_PriceList_Version_ID=plv.M_PriceList_Version_ID)
                INNER JOIN M_PriceList pl ON (plv.M_PriceList_ID=pl.M_PriceList_ID)
			WHERE	pp.M_PriceList_Version_ID=v_PriceList_Version_Base_ID
			  AND EXISTS (SELECT * FROM T_Selection s WHERE pp.M_Product_ID=s.T_Selection_ID)
			  AND	pp.IsActive='Y';
		END IF;
		Message := Message || ', @Inserted@=' || SQL%ROWCOUNT;

		--	-----------
		--	Calculation
		--	-----------
		ResultStr := ResultStr || ',Calc';
		UPDATE	M_ProductPrice p
		  SET	PriceList = (TO_NUMBER(DECODE(dl.List_Base, 'S', PriceStd, 'X', PriceLimit, PriceList)) 
					+ dl.List_AddAmt) * (1 - dl.List_Discount/100),
				PriceStd = (TO_NUMBER(DECODE(dl.Std_Base, 'L', PriceList, 'X', PriceLimit, PriceStd)) 
					+ dl.Std_AddAmt) * (1 - dl.Std_Discount/100),
				PriceLimit = (TO_NUMBER(DECODE(dl.Limit_Base, 'L', PriceList, 'S', PriceStd, PriceLimit)) 
					+ dl.Limit_AddAmt) * (1 - dl.Limit_Discount/100)
		WHERE	M_PriceList_Version_ID=p_PriceList_Version_ID
		  AND EXISTS	(SELECT * FROM T_Selection s
						WHERE s.T_Selection_ID=p.M_Product_ID);

		--	--------
		-- 	Rounding	(AD_Reference_ID=155)
		--	--------
		ResultStr := ResultStr || ',Round';
		UPDATE	M_ProductPrice p
		  SET	PriceList = TO_NUMBER(DECODE(dl.List_Rounding, 
					'N', PriceList,
					'0', ROUND(PriceList, 0),	--	Even .00					
					'D', ROUND(PriceList, 1),	--	Dime .10
					'T', ROUND(PriceList, -1),	--	Ten 10.00
					'5', ROUND(PriceList*20,0)/20,	--	Nickle .05
					'Q', ROUND(PriceList*4,0)/4,	--	Quarter .25	
					ROUND(PriceList, v_StdPrecision))),--	Currency
				PriceStd = TO_NUMBER(DECODE(dl.Std_Rounding, 
					'N', PriceStd, 
					'0', ROUND(PriceStd, 0),	--	Even .00					
					'D', ROUND(PriceStd, 1),	--	Dime .10
					'T', ROUND(PriceStd, -1),	--	Ten 10.00
					'5', ROUND(PriceStd*20,0)/20,	--	Nickle .05
					'Q', ROUND(PriceStd*4,0)/4,		--	Quarter .25	
					ROUND(PriceStd, v_StdPrecision))),	--	Currency
				PriceLimit = TO_NUMBER(DECODE(dl.Limit_Rounding, 
					'N', PriceLimit,
					'0', ROUND(PriceLimit, 0),	--	Even .00					
					'D', ROUND(PriceLimit, 1),	--	Dime .10
					'T', ROUND(PriceLimit, -1),	--	Ten 10.00
					'5', ROUND(PriceLimit*20,0)/20,	--	Nickle .05
					'Q', ROUND(PriceLimit*4,0)/4,	--	Quarter .25	
					ROUND(PriceLimit, v_StdPrecision)))--	Currency
		WHERE	M_PriceList_Version_ID=p_PriceList_Version_ID
		  AND EXISTS	(SELECT * FROM T_Selection s
						WHERE s.T_Selection_ID=p.M_Product_ID);
		Message := Message || ', @Updated@=' || SQL%ROWCOUNT;

		--	Fixed Price overwrite
		ResultStr := ResultStr || ',Fix';
		UPDATE	M_ProductPrice p
		  SET	PriceList = TO_NUMBER(DECODE(dl.List_Base, 'F', dl.List_Fixed, PriceList)), 
				PriceStd = TO_NUMBER(DECODE(dl.Std_Base, 'F', dl.Std_Fixed, PriceStd)),
				PriceLimit = TO_NUMBER(DECODE(dl.Limit_Base, 'F', dl.Limit_Fixed, PriceLimit)) 
		WHERE	M_PriceList_Version_ID=p_PriceList_Version_ID
		  AND EXISTS	(SELECT * FROM T_Selection s
						WHERE s.T_Selection_ID=p.M_Product_ID);

		--	Log Info
		INSERT INTO AD_PInstance_Log	(AD_PInstance_ID, Log_ID, P_ID, P_NUMBER, P_MSG)
		VALUES							(PInstance_ID, v_NextNo, null, dl.SeqNo, Message);
		--
		v_NextNo := v_NextNo + 1;
		Message := '';
	END LOOP;	--	For all DiscountLines

	--	Delete Temporary Selection
	DELETE FROM T_Selection;


<<FINISH_PROCESS>>
	--  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE(Message);
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Finished');
	UPDATE	AD_PInstance
	SET Updated = SysDate,
		IsProcessing = 'N',
		Result = 1,					-- success
		ErrorMsg = Message
	WHERE	AD_PInstance_ID=PInstance_ID;
	--COMMIT;
	RETURN;

EXCEPTION
	WHEN OTHERS THEN
		ResultStr := ResultStr || ':' || SQLERRM || ' ' || Message;
		DBMS_OUTPUT.PUT_LINE(ResultStr);
		UPDATE	AD_PInstance
		SET Updated = SysDate,
			IsProcessing = 'N',
			Result = 0,				-- failure
			ErrorMsg = ResultStr
		WHERE	AD_PInstance_ID=PInstance_ID;
		--COMMIT;
		RETURN;

END M_PriceList_Create;
--end--

CREATE OR REPLACE PROCEDURE M_Product_BOM_Check
(
	PInstance_ID    		IN INTEGER
)
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: M_Product_BOM_Check.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Check BOM Structure (free of cycles)
 * Description:
 *		Tree cannot contain BOMs which are already referenced
 ************************************************************************/
AS
	--	Logistice
	ResultStr						VARCHAR2(2000);
	Message							VARCHAR2(2000);
	Record_ID						INTEGER;
	--	Parameter
	CURSOR Cur_Parameter (PInstance INTEGER) IS
		SELECT i.Record_ID, p.ParameterName, p.P_String, p.P_Number, p.P_Date
		FROM AD_PInstance i, AD_PInstance_Para p
		WHERE i.AD_PInstance_ID=PInstance
		AND i.AD_PInstance_ID=p.AD_PInstance_ID(+)
		ORDER BY p.SeqNo;
	--	Variables
	Verified				CHAR(1) := 'Y';
	IsBOM					CHAR(1);
	CountNo					INTEGER;

BEGIN
    --  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Processing ' || PInstance_ID);
    ResultStr := 'PInstanceNotFound';
    UPDATE AD_PInstance
    SET Created = SysDate,
        IsProcessing = 'Y'
    WHERE AD_PInstance_ID=PInstance_ID;
    --COMMIT;

	--	Get Parameters
	ResultStr := 'ReadingParameters';
	FOR p IN Cur_Parameter (PInstance_ID) LOOP
		Record_ID := p.Record_ID;
	END LOOP;	--	Get Parameter
	DBMS_OUTPUT.PUT_LINE('  Record_ID=' || Record_ID);

	--	Record ID is M_Product_ID of product to be tested
	SELECT 	IsBOM
	  INTO	IsBOM
	FROM	M_Product
	WHERE	M_Product_ID=Record_ID;

	--	No BOM - should not happen, but no problem
	IF (IsBOM = 'N') THEN
		GOTO FINISH_PROCESS;
	--	Did not find product
	ELSIF (IsBOM <> 'Y') THEN
		RETURN;
	END IF;

	--	Checking BOM Structure
	ResultStr := 'InsertingRoot';
	--	Table to put all BOMs - duplicate will cause exception
	DELETE FROM T_Selection2 WHERE Query_ID = 0;
	INSERT INTO T_Selection2 (Query_ID, T_Selection_ID) VALUES (0, Record_ID);
	--	Table of root modes
	DELETE FROM T_Selection;
	INSERT INTO T_Selection (T_Selection_ID) VALUES (Record_ID);

	LOOP
		--	How many do we have?
		SELECT 	COUNT(*) 
		  INTO	CountNo
		FROM	T_Selection;
		--	Nothing to do
		EXIT WHEN (CountNo = 0);

		--	Insert BOM Nodes into "All" table
		INSERT INTO T_Selection2 (Query_ID, T_Selection_ID)
		SELECT 0, p.M_Product_ID
		FROM M_Product p
		WHERE IsBOM='Y' 
		  AND EXISTS (SELECT * FROM M_Product_BOM b WHERE p.M_Product_ID=b.M_ProductBOM_ID
		  	AND b.M_Product_ID IN (SELECT T_Selection_ID FROM T_Selection));

		--	Insert BOM Nodes into temporary table
		DELETE FROM T_Selection2 WHERE Query_ID = 1;
		INSERT INTO T_Selection2 (Query_ID, T_Selection_ID)
		SELECT 1, p.M_Product_ID
		FROM M_Product p
		WHERE IsBOM='Y' 
		  AND EXISTS (SELECT * FROM M_Product_BOM b WHERE p.M_Product_ID=b.M_ProductBOM_ID
		  	AND b.M_Product_ID IN (SELECT T_Selection_ID FROM T_Selection));

		--	Copy into root table
		DELETE FROM T_Selection;
		INSERT INTO T_Selection (T_Selection_ID) 
		SELECT 	T_Selection_ID
		FROM	T_Selection2
		WHERE Query_ID = 1;

	END LOOP;

<<FINISH_PROCESS>>
	--	OK
	Message := 'OK';
	UPDATE M_Product
      SET IsVerified = 'Y'
	WHERE	M_Product_ID=Record_ID;

	--  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Finished ' || Message);
    UPDATE  AD_PInstance
    SET Updated = SysDate,
        IsProcessing = 'N',
        Result = 1,                 -- success
        ErrorMsg = Message
    WHERE   AD_PInstance_ID=PInstance_ID;
    --COMMIT;
    RETURN;

EXCEPTION
    WHEN  OTHERS THEN
		ResultStr := ResultStr || ': ' || SQLERRM || ' - ' || Message;
		DBMS_OUTPUT.PUT_LINE(ResultStr);
        UPDATE  AD_PInstance
        SET Updated = SysDate,
            IsProcessing = 'N',
            Result = 0,             -- failure
            ErrorMsg = ResultStr
        WHERE   AD_PInstance_ID=PInstance_ID;
        --COMMIT;
		--
		UPDATE M_Product
    	  SET IsVerified = 'N'
		WHERE	M_Product_ID=Record_ID;
		--COMMIT;
		--
        RETURN;

END M_Product_BOM_Check;
--end--

CREATE OR REPLACE PROCEDURE M_Product_Delete
(
	whereClause	IN VARCHAR2	DEFAULT NULL 
)
AS
/******************************************************************************
 * ** Compiere Product **             Copyright (c) 1999-2001 Accorto, Inc. USA
 * Open  Source  Software        Provided "AS IS" without warranty or liability
 * When you use any parts (changed or unchanged), add  "Powered by Compiere" to
 * your product name;  See license details http://www.compiere.org/license.html
 ******************************************************************************
 *	Delete Products
 */
	CURSOR CUR_DEL IS
		SELECT 	M_Product_ID, Value, Name
		FROM 	M_Product 
		WHERE	IsActive='N';
	--
	SQL_Base		VARCHAR2(255) := 'SELECT M_Product_ID FROM M_Product WHERE ';
--	SQL_Where		VARCHAR2(255) := 'ValueX IN (SELECT ValueX FROM M_Product GROUP BY ValueX HAVING Count(*) <> 1) AND INSTR(Value,''@'') <> 0';
	SQL_Statement	VARCHAR2(255);
BEGIN
	--	Delete inactive
	IF (whereClause IS NULL OR LENGTH(whereClause) = 0) THEN
		For d IN CUR_DEL LOOP
			BEGIN
				DBMS_OUTPUT.PUT('Deleting ' || d.Name || ' - ');
				DELETE FROM M_Product
				WHERE M_Product_ID=d.M_Product_ID;
				DBMS_OUTPUT.PUT_LINE('OK');
			EXCEPTION WHEN OTHERS THEN
				DBMS_OUTPUT.PUT_LINE('Error ' || SQLERRM);
			END;
		END LOOP;
	END IF;
END M_Product_Delete;
--end--

CREATE OR REPLACE PROCEDURE M_Production_Run
(
	PInstance_ID    		IN INTEGER
)
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: M_Production_Run.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Production of BOMs
 * Description:
 *		1) Creating ProductionLines when IsCreated = 'N'
 *		2) Posting the Lines (optionally only when fully stocked)
 ************************************************************************/
AS
	--	Logistice
	ResultStr						VARCHAR2(2000);
	Message							VARCHAR2(2000);
	Record_ID						INTEGER;
	--	Parameter
	CURSOR Cur_Parameter (PInstance INTEGER) IS
		SELECT i.Record_ID, p.ParameterName, p.P_String, p.P_Number, p.P_Date
		FROM AD_PInstance i, AD_PInstance_Para p
		WHERE i.AD_PInstance_ID=PInstance
		AND i.AD_PInstance_ID=p.AD_PInstance_ID(+)
		ORDER BY p.SeqNo;
	--	Parameter Variables
	MustBeStocked					CHAR(1);
	vIsCreated						CHAR(1);
	vProcessed						CHAR(1);
	Client_ID						INTEGER;
	Org_ID							INTEGER;
	--
	Line							INTEGER;
	NextNo							INTEGER;
	CountNo							INTEGER;
	--	ProductionPlan
	CURSOR CUR_PP	IS
		SELECT 	*
		FROM	M_ProductionPlan
		WHERE	M_Production_ID=Record_ID
		ORDER BY Line, M_Product_ID;
	--	BOM Lines
	CURSOR CUR_BOM (Product_ID INTEGER)	IS
		SELECT 	*
		FROM	M_Product_BOM
		WHERE	M_Product_ID=Product_ID
		ORDER BY Line;
	--	ProductionLines which are non-stocked BOMs (need to be resolved)
	CURSOR CUR_PLineBOM (ProductionPlan_ID INTEGER) 	IS
		SELECT pl.M_ProductionLine_ID, pl.Line, pl.M_Product_ID, pl.MovementQty
		FROM M_ProductionLine pl, M_Product p
		WHERE pl.M_ProductionPlan_ID=ProductionPlan_ID
		  AND pl.M_Product_ID=p.M_Product_ID
		  AND pl.Line<>100	--	Origin Line
		  AND p.IsBOM='Y' AND p.IsStocked='N';

	--	Posting
	CURSOR CUR_PL_Post	IS
		SELECT pl.M_ProductionLine_ID, pl.AD_Client_ID, pl.AD_Org_ID, p.MovementDate,
			pl.M_Product_ID, pl.M_AttributeSetInstance_ID, pl.MovementQty, pl.M_Locator_ID
		FROM M_Production p, M_ProductionLine pl, M_ProductionPlan pp
		WHERE p.M_Production_ID=pp.M_Production_ID
		  AND pp.M_ProductionPlan_ID=pl.M_ProductionPlan_ID
		  AND pp.M_Production_ID=Record_ID
		ORDER BY pp.Line, pl.Line;



BEGIN
    --  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Processing ' || PInstance_ID);
    ResultStr := 'PInstanceNotFound';
    UPDATE AD_PInstance
    SET Created = SysDate,
        IsProcessing = 'Y'
    WHERE AD_PInstance_ID=PInstance_ID;
    --COMMIT;

	--	Get Parameters
	ResultStr := 'ReadingParameters';
	FOR p IN Cur_Parameter (PInstance_ID) LOOP
		Record_ID := p.Record_ID;
		IF (p.ParameterName = 'MustBeStocked') THEN
 			MustBeStocked := p.P_String;
			DBMS_OUTPUT.PUT_LINE('  MustBeStocked=' || MustBeStocked);
		ELSE
			DBMS_OUTPUT.PUT_LINE('*** Unknown Parameter=' || p.ParameterName);
	 	END IF;
	END LOOP;	--	Get Parameter
	DBMS_OUTPUT.PUT_LINE('  Record_ID=' || Record_ID);

	--	Processing??? Lock ????
	--	TODO

	/**
	 *	Get Info + Lock
	 */
	ResultStr := 'ReadingRecord';
	SELECT 	IsCreated, Processed, AD_Client_ID, AD_Org_ID
	  INTO 	vIsCreated, vProcessed, Client_ID, Org_ID
	FROM	M_Production
	WHERE	M_Production_ID=Record_ID
	FOR UPDATE;

	/**
	 *	No Action
	 */
	IF (vProcessed <> 'N') THEN
		Message := '@AlreadyPosted@';
		GOTO FINISH_PROCESS;
	END IF;
	
	/**************************************************************************
	 *	Create Lines
	 */
	IF (vIsCreated <> 'Y') THEN
		-- For every Production Plan
		FOR pp IN CUR_PP LOOP
			--	Delete prior lines
			DELETE 	FROM M_ProductionLine
			WHERE 	M_ProductionPlan_ID=pp.M_ProductionPlan_ID;
		--	DBMS_OUTPUT.PUT_LINE('ProductionPlan=' || pp.M_ProductionPlan_ID);
			--	Create BOM Line
			ResultStr := 'CreatingLine BOM';
			Line := 100;	--	OriginLine
			AD_Sequence_Next('M_ProductionLine', pp.AD_Client_ID, NextNo);
			INSERT INTO M_ProductionLine
				(M_ProductionLine_ID, M_ProductionPlan_ID, Line,
				AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,
				M_Product_ID, MovementQty, M_Locator_ID, Description)
		   	VALUES
				(NextNo, pp.M_ProductionPlan_ID, Line,
				pp.AD_Client_ID,pp.AD_Org_ID,'Y',SysDate,0,SysDate,0,
				pp.M_Product_ID, pp.ProductionQty, pp.M_Locator_ID, pp.Description);

			--	Create First Level
			FOR bom IN CUR_BOM (pp.M_Product_ID) LOOP
				ResultStr := 'CreatingLine Products';
				Line := Line + 100;
				AD_Sequence_Next('M_ProductionLine', pp.AD_Client_ID, NextNo);
				INSERT INTO M_ProductionLine
					(M_ProductionLine_ID, M_ProductionPlan_ID, Line,
					AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,
					M_Product_ID, MovementQty, M_Locator_ID)
			   	VALUES
					(NextNo, pp.M_ProductionPlan_ID, Line,
					pp.AD_Client_ID,pp.AD_Org_ID,'Y',SysDate,0,SysDate,0,
					bom.M_ProductBOM_ID, -pp.ProductionQty*bom.BOMQty, pp.M_Locator_ID);
			END LOOP;

			--	While we have BOMs
			LOOP
				--	Are there non-stored BOMs to list details?
				ResultStr := 'CreatingLine CheckBOM';
				SELECT COUNT(*) INTO CountNo
				FROM M_ProductionLine pl, M_Product p
				WHERE pl.M_Product_ID=p.M_Product_ID
				  AND pl.M_ProductionPlan_ID=pp.M_ProductionPlan_ID
				  AND pl.Line<>100	--	Origin Line
				  AND p.IsBOM='Y' AND p.IsStocked='N';
				--	Nothing to do	
				EXIT WHEN (CountNo = 0);
				--

				--	Resolve BOMs in ProductLine which are not stocked
				FOR pl IN CUR_PLineBOM (pp.M_ProductionPlan_ID) LOOP
					ResultStr := 'CreatingLineBOM Resolution';
					Line := pl.Line;
					--	Resolve BOM Line in product line
					FOR bom IN CUR_BOM (pl.M_Product_ID) LOOP
						ResultStr := 'CreatingLine Products2';
						Line := Line + 10;
						AD_Sequence_Next('M_ProductionLine', pp.AD_Client_ID, NextNo);
						INSERT INTO M_ProductionLine
							(M_ProductionLine_ID, M_ProductionPlan_ID, Line,
							AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,
							M_Product_ID, MovementQty, M_Locator_ID)
			   			VALUES
							(NextNo, pp.M_ProductionPlan_ID, Line,
							pp.AD_Client_ID,pp.AD_Org_ID,'Y',SysDate,0,SysDate,0,
							bom.M_ProductBOM_ID, pl.MovementQty*bom.BOMQty, pp.M_Locator_ID);
					END LOOP;
					--	Delete BOM line
					DELETE  FROM M_ProductionLine
					WHERE 	M_ProductionLine_ID=pl.M_ProductionLine_ID;
				END LOOP;
			END LOOP;	--	While we have BOMs

		END LOOP;	--	For every Production Plan

		--	Modifying locator to have sufficient stock


		--	Indicate that it is Created
		UPDATE	M_Production
		  SET	IsCreated='Y'
		WHERE	M_Production_ID=Record_ID;

	/**************************************************************************
	 *	Post Lines
	 */
	ELSE
		--	All Production Lines
		FOR pl IN CUR_PL_Post LOOP
		--	M_ProductionLine_ID, AD_Client_ID, AD_Org_ID, MovementDate, M_Product_ID, MovementQty, M_Locator_ID
		--	DBMS_OUTPUT.PUT_LINE('ProductionLine=' || pl.M_ProductionLine_ID);
		--	DBMS_OUTPUT.PUT_LINE('  Qty=' || pl.MovementQty || ', OnHand=' || BOM_Qty_OnHand(pl.M_Product_ID, NULL, pl.M_Locator_ID));
			--	Check Stock levels for reductions
			IF (pl.MovementQty < 0 AND MustBeStocked <> 'N'
					AND bomQtyOnHand(pl.M_Product_ID, NULL, pl.M_Locator_ID)+pl.MovementQty < 0) THEN
				--jz ROLLBACK;
				SELECT '@NotEnoughStocked@: ' || Name	INTO Message
				FROM M_Product WHERE M_Product_ID=pl.M_Product_ID;
				GOTO FINISH_PROCESS;
			END IF;

			--	Adjust Quantity at Location
			UPDATE	M_Storage
			  SET	QtyOnHand = QtyOnHand + pl.MovementQty,
					Updated = SysDate
		 	WHERE	M_Locator_ID = pl.M_Locator_ID
              AND   M_AttributeSetInstance_ID = COALESCE(pl.M_AttributeSetInstance_ID,0)
			  AND	M_Product_ID = pl.M_Product_ID;
			--	Product not on Stock yet
			IF (SQL%ROWCOUNT = 0) THEN
				INSERT INTO M_Storage
					(M_Product_ID, M_Locator_ID, M_AttributeSetInstance_ID,
					 AD_Client_ID, AD_Org_ID, IsActive, Created, CreatedBy, Updated, UpdatedBy,
					 QtyOnHand, QtyReserved, QtyOrdered)
			 	VALUES
					(pl.M_Product_ID, pl.M_Locator_ID, COALESCE(pl.M_AttributeSetInstance_ID,0),
					 pl.AD_Client_ID, pl.AD_Org_ID, 'Y', SysDate, 0, SysDate, 0,
					 pl.MovementQty, 0, 0);
			END IF;
				
			--	Create Transaction Entry
			ResultStr := 'CreateTransaction';
			AD_Sequence_Next('M_Transaction', pl.AD_Org_ID, NextNo);
			INSERT INTO M_Transaction
				(M_Transaction_ID, M_ProductionLine_ID,
				AD_Client_ID, AD_Org_ID, IsActive, Created, CreatedBy, Updated, UpdatedBy,
				MovementType, M_Locator_ID, M_Product_ID, M_AttributeSetInstance_ID,
				MovementDate, MovementQty)
			VALUES
				(NextNo, pl.M_ProductionLine_ID,
				pl.AD_Client_ID, pl.AD_Org_ID, 'Y', SysDate, 0, SysDate, 0,
				'P+', pl.M_Locator_ID, pl.M_Product_ID,	COALESCE(pl.M_AttributeSetInstance_ID,0),	-- not distinguishing between assemby/disassembly
				pl.MovementDate, pl.MovementQty);
            --
    		UPDATE	M_ProductionLine
	    	  SET	Processed='Y'
		    WHERE	M_ProductionLine_ID=pl.M_ProductionLine_ID;
		END LOOP;

		--	Indicate that we are done
		UPDATE	M_Production
		  SET	Processed='Y'
		WHERE	M_Production_ID=Record_ID;
		UPDATE	M_ProductionPlan
		  SET	Processed='Y'
		WHERE	M_Production_ID=Record_ID;

	END IF;
	--	Only commit when entire job successful
	--COMMIT;

<<FINISH_PROCESS>>
	--  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Finished ' || Message);
    UPDATE  AD_PInstance
    SET Updated = SysDate,
        IsProcessing = 'N',
        Result = 1,                 -- success
        ErrorMsg = Message
    WHERE   AD_PInstance_ID=PInstance_ID;
    --COMMIT;
    RETURN;

EXCEPTION
    WHEN  OTHERS THEN
		ResultStr := ResultStr || ': ' || SQLERRM || ' - ' || Message;
		DBMS_OUTPUT.PUT_LINE(ResultStr);
        UPDATE  AD_PInstance
        SET Updated = SysDate,
            IsProcessing = 'N',
            Result = 0,             -- failure
            ErrorMsg = ResultStr
        WHERE   AD_PInstance_ID=PInstance_ID;
        --COMMIT;
        RETURN;

END M_Production_Run;
--end--



CREATE OR REPLACE PROCEDURE T_InventoryValue_Create
(
	p_PInstance_ID			IN INTEGER
)
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: T_InventoryValue_Create.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Inventory Valuation Temporary Table
 * Description:
 ************************************************************************/
AS
	--	Logistice
	v_ResultStr					VARCHAR2(2000);
	v_Message						VARCHAR2(2000);
	v_Result						INTEGER := 1;	-- 0=failure
	v_Record_ID					INTEGER;
	v_AD_User_ID					INTEGER;
	--	Parameter
	CURSOR Cur_Parameter (pp_PInstance INTEGER) IS
		SELECT i.Record_ID, i.AD_User_ID,
			p.ParameterName, p.P_String, p.P_Number, p.P_Date
		FROM AD_PInstance i, AD_PInstance_Para p
		WHERE i.AD_PInstance_ID=pp_PInstance
		AND i.AD_PInstance_ID=p.AD_PInstance_ID(+)
		ORDER BY p.SeqNo;
	--	Parameter Variables
	p_M_PriceList_Version_ID			INTEGER;
	p_DateValue					DATE;
	p_M_Warehouse_ID				INTEGER;
	p_C_Currency_ID				INTEGER;

BEGIN
	--  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Processing ' || p_PInstance_ID);
	v_ResultStr := 'PInstanceNotFound';
	UPDATE AD_PInstance
	SET Created = SysDate,
		IsProcessing = 'Y'
	WHERE AD_PInstance_ID=p_PInstance_ID;
	--COMMIT;

	--	Get Parameters
	v_ResultStr := 'ReadingParameters';
	FOR p IN Cur_Parameter (p_PInstance_ID) LOOP
		v_Record_ID := p.Record_ID;
		v_AD_User_ID := p.AD_User_ID;
		IF (p.ParameterName = 'M_PriceList_Version_ID') THEN
			p_M_PriceList_Version_ID := p.P_Number;
			DBMS_OUTPUT.PUT_LINE('  M_PriceList_Version_ID=' || p_M_PriceList_Version_ID);
		ELSIF (p.ParameterName = 'DateValue') THEN
			p_DateValue := p.P_Date;
			DBMS_OUTPUT.PUT_LINE('  DateValue=' || p_DateValue);
		ELSIF (p.ParameterName = 'M_Warehouse_ID') THEN
			p_M_Warehouse_ID := p.P_Number;
			DBMS_OUTPUT.PUT_LINE('  M_Warehouse_ID=' || p_M_Warehouse_ID);
		ELSIF (p.ParameterName = 'C_Currency_ID') THEN
			p_C_Currency_ID := p.P_Number;
			DBMS_OUTPUT.PUT_LINE('  C_Currency_ID=' || p_C_Currency_ID);
		ELSE
			DBMS_OUTPUT.PUT_LINE('*** Unknown Parameter=' || p.ParameterName);
		END IF;
	END LOOP;	--	Get Parameter
	DBMS_OUTPUT.PUT_LINE('  Record_ID=' || v_Record_ID);

	-- Clear
--	v_ResultStr := 'ClearTable';
--	DELETE FROM T_InventoryValue WHERE M_Warehouse_ID=p_M_Warehouse_ID;
--	COMMIT;

	--	Insert Products
	v_ResultStr := 'InsertStockedProducts';
	INSERT INTO T_InventoryValue 
		(AD_Client_ID,AD_Org_ID, AD_PInstance_ID, M_Warehouse_ID,M_Product_ID)
	SELECT AD_Client_ID,AD_Org_ID, p_PInstance_ID, p_M_Warehouse_ID,M_Product_ID
	FROM M_Product 
	WHERE IsStocked='Y';
	--
	IF (SQL%ROWCOUNT = 0) THEN
		v_Message := '@Created@ = 0';
		GOTO FINISH_PROCESS;
	END IF;

	-- Update Constants
	v_ResultStr := 'UpdateConstants';
	UPDATE T_InventoryValue 
	  SET	DateValue = TRUNC(p_DateValue) + 0.9993,
			M_PriceList_Version_ID = p_M_PriceList_Version_ID,
			C_Currency_ID = p_C_Currency_ID
	WHERE	M_Warehouse_ID = p_M_Warehouse_ID;

	--	Get current QtyOnHand
	v_ResultStr := 'GetQtyOnHand';
	--jz UPDATE T_InventoryValue iv
	UPDATE T_InventoryValue
	  SET	QtyOnHand = (SELECT SUM(QtyOnHand) FROM M_Storage s, M_Locator l
				WHERE T_InventoryValue.M_Product_ID=s.M_Product_ID
				 AND l.M_Locator_ID=s.M_Locator_ID
				 AND l.M_Warehouse_ID=T_InventoryValue.M_Warehouse_ID)
	WHERE	T_InventoryValue.M_Warehouse_ID = p_M_Warehouse_ID;

	-- Adjust for Valuation Date
	v_ResultStr := 'AdjustQtyOnHand';
	--jz UPDATE T_InventoryValue iv 
	UPDATE T_InventoryValue 
	  SET	QtyOnHand = 
				(SELECT T_InventoryValue.QtyOnHand - NVL(SUM(t.MovementQty), 0) 
				FROM M_Transaction t, M_Locator l
				WHERE t.M_Product_ID=T_InventoryValue.M_Product_ID 
    --            AND t.M_AttributeSetInstance_ID=T_InventoryValue.M_AttributeSetInstance_ID
				  AND t.MovementDate > T_InventoryValue.DateValue
				  AND t.M_Locator_ID=l.M_Locator_ID 
				  AND l.M_Warehouse_ID=T_InventoryValue.M_Warehouse_ID)
	WHERE	T_InventoryValue.M_Warehouse_ID = p_M_Warehouse_ID;

	--	Delete Records w/o OnHand Qty
	v_ResultStr := 'DeleteZeroQtyOnHand';
	DELETE FROM T_InventoryValue 
	WHERE	QtyOnHand=0 
	  OR	QtyOnHand IS NULL;

	-- Update Prices
	v_ResultStr := 'GetPrices';
	--jz UPDATE T_InventoryValue iv
	UPDATE T_InventoryValue 
	  SET	PricePO = 
				(SELECT currencyConvert (po.PriceList,po.C_Currency_ID,T_InventoryValue.C_Currency_ID,T_InventoryValue.DateValue, null, T_InventoryValue.AD_Client_ID, T_InventoryValue.AD_Org_ID)
				FROM M_Product_PO po WHERE po.M_Product_ID=T_InventoryValue.M_Product_ID
				AND po.IsCurrentVendor='Y' AND RowNum=1),
			PriceList = 
				(SELECT currencyConvert(pp.PriceList,pl.C_Currency_ID,T_InventoryValue.C_Currency_ID,T_InventoryValue.DateValue, null, T_InventoryValue.AD_Client_ID, T_InventoryValue.AD_Org_ID)
				FROM M_PriceList pl, M_PriceList_Version plv, M_ProductPrice pp
				WHERE pp.M_Product_ID=T_InventoryValue.M_Product_ID AND pp.M_PriceList_Version_ID=T_InventoryValue.M_PriceList_Version_ID
				 AND pp.M_PriceList_Version_ID=plv.M_PriceList_Version_ID
				 AND plv.M_PriceList_ID=pl.M_PriceList_ID),
			PriceStd = 
				(SELECT currencyConvert(pp.PriceStd,pl.C_Currency_ID,T_InventoryValue.C_Currency_ID,T_InventoryValue.DateValue, null, T_InventoryValue.AD_Client_ID, T_InventoryValue.AD_Org_ID)
				FROM M_PriceList pl, M_PriceList_Version plv, M_ProductPrice pp
				WHERE pp.M_Product_ID=T_InventoryValue.M_Product_ID AND pp.M_PriceList_Version_ID=T_InventoryValue.M_PriceList_Version_ID
				 AND pp.M_PriceList_Version_ID=plv.M_PriceList_Version_ID
				 AND plv.M_PriceList_ID=pl.M_PriceList_ID), 
			PriceLimit = 
				(SELECT currencyConvert(pp.PriceLimit,pl.C_Currency_ID,T_InventoryValue.C_Currency_ID,T_InventoryValue.DateValue, null, T_InventoryValue.AD_Client_ID, T_InventoryValue.AD_Org_ID)
				FROM M_PriceList pl, M_PriceList_Version plv, M_ProductPrice pp
				WHERE pp.M_Product_ID=T_InventoryValue.M_Product_ID AND pp.M_PriceList_Version_ID=T_InventoryValue.M_PriceList_Version_ID
				 AND pp.M_PriceList_Version_ID=plv.M_PriceList_Version_ID
				 AND plv.M_PriceList_ID=pl.M_PriceList_ID),
			CostStandard = 
				(SELECT currencyConvert(pc.CurrentCostPrice,acs.C_Currency_ID,T_InventoryValue.C_Currency_ID,T_InventoryValue.DateValue, null, T_InventoryValue.AD_Client_ID, T_InventoryValue.AD_Org_ID)
				FROM AD_ClientInfo ci, C_AcctSchema acs, M_Product_Costing pc
				WHERE T_InventoryValue.AD_Client_ID=ci.AD_Client_ID AND ci.C_AcctSchema1_ID=acs.C_AcctSchema_ID
				 AND acs.C_AcctSchema_ID=pc.C_AcctSchema_ID
				 AND T_InventoryValue.M_Product_ID=pc.M_Product_ID)
	WHERE	T_InventoryValue.M_Warehouse_ID = p_M_Warehouse_ID;

	--	Update Values
	v_ResultStr := 'UpdateValue';
	UPDATE T_InventoryValue 
	  SET	PricePOAmt = QtyOnHand * PricePO, 
			PriceListAmt = QtyOnHand * PriceList, 
			PriceStdAmt = QtyOnHand * PriceStd, 
			PriceLimitAmt = QtyOnHand * PriceLimit, 
			CostStandardAmt = QtyOnHand * CostStandard
	WHERE	M_Warehouse_ID = p_M_Warehouse_ID;

	v_Message := '@Created@ = ' || SQL%ROWCOUNT;


<<FINISH_PROCESS>>
	--  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Finished ' || v_Message);
	UPDATE	AD_PInstance
	SET Updated = SysDate,
		IsProcessing = 'N',
		Result = v_Result,			-- 1=success
		ErrorMsg = v_Message
	WHERE	AD_PInstance_ID=p_PInstance_ID;
	--COMMIT;
	RETURN;

EXCEPTION
	WHEN  OTHERS THEN
		v_ResultStr := v_ResultStr || ': ' || SQLERRM || ' - ' || v_Message;
		DBMS_OUTPUT.PUT_LINE(v_ResultStr);
		--jz ROLLBACK;
		UPDATE	AD_PInstance
		SET Updated = SysDate,
			IsProcessing = 'N',
			Result = 0,				-- failure
			ErrorMsg = v_ResultStr
		WHERE	AD_PInstance_ID=p_PInstance_ID;
		--COMMIT;
		RETURN;

END T_InventoryValue_Create;
--end--



CREATE OR REPLACE FUNCTION invoiceDiscount
(
	p_C_Invoice_ID		        IN INTEGER,
	p_PayDate			        IN	DATE,
	p_C_InvoicePaySchedule_ID	IN	INTEGER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_Invoice_Discount.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Calculate Payment Discount Amount
 * Description:
 *			- Calculate discountable amount (i.e. with or without tax)
 *			- Calculate and return payment discount
 ************************************************************************/
AS
	v_Amount			NUMBER;
	v_IsDiscountLineAmt	CHAR(1);
	v_GrandTotal		NUMBER;
	v_TotalLines		NUMBER;
	v_C_PaymentTerm_ID	INTEGER;
	v_DocDate			DATE;
	v_PayDate			DATE := SysDate;
    v_IsPayScheduleValid    CHAR(1);

BEGIN
	SELECT 	ci.IsDiscountLineAmt, i.GrandTotal, i.TotalLines,
		i.C_PaymentTerm_ID, i.DateInvoiced, i.IsPayScheduleValid
	  INTO 	v_IsDiscountLineAmt, v_GrandTotal, v_TotalLines,
		v_C_PaymentTerm_ID, v_DocDate, v_IsPayScheduleValid
	FROM 	AD_ClientInfo ci, C_Invoice i
	WHERE 	ci.AD_Client_ID=i.AD_Client_ID
	  AND 	i.C_Invoice_ID=p_C_Invoice_ID;
	--	What Amount is the Discount Base?
 	IF (v_IsDiscountLineAmt = 'Y') THEN
		v_Amount := v_TotalLines;
	ELSE
		v_Amount := v_GrandTotal;
	END IF;

	--	Anything to discount?
	IF (v_Amount = 0) THEN
		RETURN 0;
   	END IF;
	IF (p_PayDate IS NOT NULL) THEN
		v_PayDate := p_PayDate;
  	END IF;

    --  Valid Payment Schedule
    IF (v_IsPayScheduleValid='Y' AND p_C_InvoicePaySchedule_ID > 0) THEN
        SELECT COALESCE(MAX(DiscountAmt),0)
          INTO v_Amount
        FROM C_InvoicePaySchedule
        WHERE C_InvoicePaySchedule_ID=p_C_InvoicePaySchedule_ID
          AND DiscountDate <= v_PayDate;
        --
        RETURN v_Amount;
    END IF;

	--	return discount amount	
	RETURN paymentTermDiscount (v_Amount, 0, v_C_PaymentTerm_ID, v_DocDate, p_PayDate);

--	Most likely if invoice not found
EXCEPTION
	WHEN OTHERS THEN
		RETURN NULL;
END invoiceDiscount;
--end--

CREATE OR REPLACE FUNCTION currencyRate
(
	p_CurFrom_ID		IN	INTEGER,
	p_CurTo_ID		    IN	INTEGER,
	p_ConvDate		    IN	DATE,
	p_ConversionType_ID	IN	INTEGER,
	p_Client_ID		    IN	INTEGER,
	p_Org_ID			IN	INTEGER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_Currency_Rate.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Return Conversion Rate
 * Description:
 *		from CurrencyFrom_ID to CurrencyTo_ID
 *		Returns NULL, if rate not found
 * Test
 *		SELECT C_Currency_Rate(116, 100, null, null) FROM DUAL;     => .647169
 *		SELECT C_Currency_Rate(116, 100) FROM DUAL;                 => .647169
 ************************************************************************/
AS
	--	Currency From variables
	cf_IsEuro			CHAR(1);
	cf_IsEMUMember		CHAR(1);
	cf_EMUEntryDate	DATE;
	cf_EMURate		NUMBER;
	--	Currency To variables
	ct_IsEuro			CHAR(1);
	ct_IsEMUMember		CHAR(1);
	ct_EMUEntryDate	DATE;
	ct_EMURate		NUMBER;
	--	Triangle
	v_CurrencyFrom		NUMBER;
	v_CurrencyTo		NUMBER;
	v_CurrencyEuro		NUMBER;
	--
	v_ConvDate		    DATE := SysDate;
	v_ConversionType_ID	INTEGER := 0;
	v_Rate			    NUMBER;
BEGIN
	--	No Conversion
	IF (p_CurFrom_ID = p_CurTo_ID) THEN
		RETURN 1;
	END IF;
	--	Default Date Parameter
	IF (p_ConvDate IS NOT NULL) THEN
		v_ConvDate := p_ConvDate;   --  SysDate
	END IF;
    --  Default Conversion Type
	IF (p_ConversionType_ID IS NULL OR p_ConversionType_ID = 0) THEN
        BEGIN
            SELECT C_ConversionType_ID 
              INTO v_ConversionType_ID
            FROM C_ConversionType 
            WHERE IsDefault='Y'
              AND AD_Client_ID IN (0,p_Client_ID)
              AND ROWNUM=1
            ORDER BY AD_Client_ID DESC;
        EXCEPTION WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('Conversion Type Not Found');
        END;
    ELSE
        v_ConversionType_ID := p_ConversionType_ID;
	END IF;

	--	Get Currency Info
	SELECT	MAX(IsEuro), MAX(IsEMUMember), MAX(EMUEntryDate), MAX(EMURate)
	  INTO	cf_IsEuro, cf_IsEMUMember, cf_EMUEntryDate, cf_EMURate
	FROM		C_Currency
	  WHERE	C_Currency_ID = p_CurFrom_ID;
	-- Not Found
	IF (cf_IsEuro IS NULL) THEN
		DBMS_OUTPUT.PUT_LINE('From Currency Not Found');
		RETURN NULL;
	END IF;
	SELECT	MAX(IsEuro), MAX(IsEMUMember), MAX(EMUEntryDate), MAX(EMURate)
	  INTO	ct_IsEuro, ct_IsEMUMember, ct_EMUEntryDate, ct_EMURate
	FROM		C_Currency
	  WHERE	C_Currency_ID = p_CurTo_ID;
	-- Not Found
	IF (ct_IsEuro IS NULL) THEN
		DBMS_OUTPUT.PUT_LINE('To Currency Not Found');
		RETURN NULL;
	END IF;

	--	Fixed - From Euro to EMU
	IF (cf_IsEuro = 'Y' AND ct_IsEMUMember ='Y' AND v_ConvDate >= ct_EMUEntryDate) THEN
		RETURN ct_EMURate;
	END IF;

	--	Fixed - From EMU to Euro
	IF (ct_IsEuro = 'Y' AND cf_IsEMUMember ='Y' AND v_ConvDate >= cf_EMUEntryDate) THEN
		RETURN 1 / cf_EMURate;
	END IF;

	--	Fixed - From EMU to EMU
	IF (cf_IsEMUMember = 'Y' AND cf_IsEMUMember ='Y'
			AND v_ConvDate >= cf_EMUEntryDate AND v_ConvDate >= ct_EMUEntryDate) THEN
		RETURN ct_EMURate / cf_EMURate;
	END IF;

	--	Flexible Rates
	v_CurrencyFrom := p_CurFrom_ID;
	v_CurrencyTo := p_CurTo_ID;

	-- if EMU Member involved, replace From/To Currency
	IF ((cf_isEMUMember = 'Y' AND v_ConvDate >= cf_EMUEntryDate)
	  OR (ct_isEMUMember = 'Y' AND v_ConvDate >= ct_EMUEntryDate)) THEN
		SELECT	MAX(C_Currency_ID)
		  INTO	v_CurrencyEuro
		FROM		C_Currency
		WHERE	IsEuro = 'Y';
		-- Conversion Rate not Found
		IF (v_CurrencyEuro IS NULL) THEN
			DBMS_OUTPUT.PUT_LINE('Euro Not Found');
			RETURN NULL;
		END IF;
		IF (cf_isEMUMember = 'Y' AND v_ConvDate >= cf_EMUEntryDate) THEN
			v_CurrencyFrom := v_CurrencyEuro;
		ELSE
			v_CurrencyTo := v_CurrencyEuro;
		END IF;
	END IF;

	--	Get Rate
	DECLARE
		CURSOR	CUR_Rate	IS
			SELECT	MultiplyRate
			FROM	C_Conversion_Rate
			WHERE	C_Currency_ID=v_CurrencyFrom AND C_Currency_ID_To=v_CurrencyTo
			  AND	C_ConversionType_ID=v_ConversionType_ID
			  AND	v_ConvDate BETWEEN ValidFrom AND ValidTo
			  AND	AD_Client_ID IN (0,p_Client_ID) AND AD_Org_ID IN (0,p_Org_ID)
			ORDER BY AD_Client_ID DESC, AD_Org_ID DESC, ValidFrom DESC;
	BEGIN
		FOR c IN CUR_Rate LOOP
			v_Rate := c.MultiplyRate;
			EXIT;	--	only first
		END LOOP;
	END;
	--	Not found
	IF (v_Rate IS NULL) THEN
		DBMS_OUTPUT.PUT_LINE('Conversion Rate Not Found');
		RETURN NULL;
	END IF;

	--	Currency From was EMU
	IF (cf_isEMUMember = 'Y' AND v_ConvDate >= cf_EMUEntryDate) THEN
		RETURN v_Rate / cf_EMURate;
	END IF;

	--	Currency To was EMU
	IF (ct_isEMUMember = 'Y' AND v_ConvDate >= ct_EMUEntryDate) THEN
		RETURN v_Rate * ct_EMURate;
	END IF;

	RETURN v_Rate;

EXCEPTION WHEN OTHERS THEN
	DBMS_OUTPUT.PUT_LINE(SQLERRM);
	RETURN NULL;

END currencyRate;
--end--

CREATE OR REPLACE FUNCTION currencyConvert
(
	p_Amount			IN	NUMBER,
	p_CurFrom_ID		IN	INTEGER,
	p_CurTo_ID		    IN	INTEGER,
	p_ConvDate		    IN	DATE,
	p_ConversionType_ID IN	INTEGER,
	p_Client_ID		    IN	INTEGER,
	p_Org_ID			IN	INTEGER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_Currency_Convert.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Convert Amount (using IDs)
 * Description:
 *		from CurrencyFrom_ID to CurrencyTo_ID
 *		Returns NULL, if conversion not found
 *		Standard Rounding
 * Test:
 *		SELECT C_Currency_Convert(100,116,100,null,null) FROM DUAL  => 64.72
 *		SELECT C_Currency_Convert(100,116,100) FROM DUAL            => 64.72
 ************************************************************************/
AS
	v_Rate				NUMBER;
BEGIN
	--	Return Amount
	IF (p_Amount = 0 OR p_CurFrom_ID = p_CurTo_ID) THEN
		RETURN p_Amount;
	END IF;
	--	Return NULL
	IF (p_Amount IS NULL OR p_CurFrom_ID IS NULL OR p_CurTo_ID IS NULL) THEN
		RETURN NULL;
	END IF;

	--	Get Rate
	v_Rate := currencyRate (p_CurFrom_ID, p_CurTo_ID, p_ConvDate, p_ConversionType_ID, p_Client_ID, p_Org_ID);
	IF (v_Rate IS NULL) THEN
		RETURN NULL;
	END IF;

	--	Standard Precision
	RETURN currencyRound(p_Amount * v_Rate, p_CurTo_ID, null);	
END currencyConvert;
--end--

CREATE OR REPLACE FUNCTION bpartnerRemitLocation
(
	p_C_BPartner_ID	  C_BPartner.C_BPartner_ID%TYPE
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2002 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_BPartner_RemitLocation.SQL,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:   Return the first RemitTo C_Location_ID of a Business Partner
 * Description:
 *      
 ************************************************************************/
AS
	v_C_Location_ID			INTEGER := NULL;
	CURSOR	CUR_BPLoc	IS
		SELECT	IsRemitTo, C_Location_ID
		FROM	C_BPartner_Location
		WHERE	C_BPartner_ID=p_C_BPartner_ID
		ORDER BY IsRemitTo DESC;
BEGIN
	FOR l IN CUR_BPLoc LOOP
		IF (v_C_Location_ID IS NULL) THEN
			v_C_Location_ID := l.C_Location_ID;
		END IF;
	END LOOP;
	RETURN v_C_Location_ID;
END bpartnerRemitLocation;
--end--

CREATE OR REPLACE PROCEDURE AD_PrintPaper_Default
(
	p_AD_PInstance_ID    		IN INTEGER
)
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: AD_PrintPaper_Default.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Set Current Format as Default
 * Description:
 ************************************************************************/
AS
	--	Logistice
	v_ResultStr						VARCHAR2(2000);
	v_Message						VARCHAR2(2000);
	p_Record_ID						INTEGER;
	--	Parameter
	CURSOR Cur_Parameter (PInstance INTEGER) IS
		SELECT i.Record_ID, p.ParameterName, p.P_String, p.P_Number, p.P_Date
		FROM AD_PInstance i, AD_PInstance_Para p
		WHERE i.AD_PInstance_ID=PInstance
		AND i.AD_PInstance_ID=p.AD_PInstance_ID(+)
		ORDER BY p.SeqNo;
	--	Parameter Variables
	p_AD_Client_ID					INTEGER := NULL;

BEGIN
    --  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Processing ' || p_AD_PInstance_ID);
    v_ResultStr := 'PInstanceNotFound';
    UPDATE AD_PInstance
    SET Created = SysDate,
        IsProcessing = 'Y'
    WHERE AD_PInstance_ID=p_AD_PInstance_ID;
    --COMMIT;

	--	Get Parameters
	v_ResultStr := 'ReadingParameters';
	FOR p IN Cur_Parameter (p_AD_PInstance_ID) LOOP
		p_Record_ID := p.Record_ID;
		IF (p.ParameterName = 'AD_Client_ID') THEN
 			p_AD_Client_ID := p.P_Number;
			DBMS_OUTPUT.PUT_LINE('  AD_Client_ID=' || p_AD_Client_ID);
		ELSE
			DBMS_OUTPUT.PUT_LINE('*** Unknown Parameter=' || p.ParameterName);
	 	END IF;
	END LOOP;	--	Get Parameter
	DBMS_OUTPUT.PUT_LINE('  Record_ID=' || p_Record_ID);


	v_ResultStr := 'Updating';
	UPDATE	AD_PrintFormat pf
	  SET	AD_PrintPaper_ID = p_Record_ID
	WHERE	(AD_Client_ID = p_AD_Client_ID OR p_AD_Client_ID IS NULL)
	  AND EXISTS (SELECT * FROM AD_PrintPaper pp
 	  	WHERE 	pf.AD_PrintPaper_ID=pp.AD_PrintPaper_ID
		  AND	IsLandscape = (SELECT IsLandscape FROM AD_PrintPaper 
		  		WHERE AD_PrintPaper_ID=p_Record_ID));
	v_Message := '@Copied@=' || SQL%ROWCOUNT;

<<FINISH_PROCESS>>
	--  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Finished ' || v_Message);
    UPDATE  AD_PInstance
    SET Updated = SysDate,
        IsProcessing = 'N',
        Result = 1,                 -- success
        ErrorMsg = v_Message
    WHERE   AD_PInstance_ID=p_AD_PInstance_ID;
    --COMMIT;
    RETURN;

EXCEPTION
    WHEN  OTHERS THEN
		v_ResultStr := v_ResultStr || ': ' || SQLERRM || ' - ' || v_Message;
		DBMS_OUTPUT.PUT_LINE(v_ResultStr);
        UPDATE  AD_PInstance
        SET Updated = SysDate,
            IsProcessing = 'N',
            Result = 0,             -- failure
            ErrorMsg = v_ResultStr
        WHERE   AD_PInstance_ID=p_AD_PInstance_ID;
        --COMMIT;
        RETURN;

END AD_PrintPaper_Default;
--end--

CREATE OR REPLACE PROCEDURE Fact_Acct_Balance_Update
(
	p_DeleteFirst		IN	VARCHAR2	DEFAULT 'N'
)
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2003 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: Fact_Acct_Balance_Update.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:		Update ALL Balances	
 * Description:
 *	- Recreates all Balances
 ************************************************************************/
AS
BEGIN

	IF (p_DeleteFirst = 'Y') THEN
		DELETE FROM Fact_Acct_Balance;
		DBMS_OUTPUT.PUT_LINE('  Deletes=' || SQL%ROWCOUNT);
	ELSE
		/** Update		**/
		--jz UPDATE Fact_Acct_Balance ab
		UPDATE Fact_Acct_Balance
		  SET (AmtAcctDr, AmtAcctCr, Qty) =
			(SELECT COALESCE(SUM(AmtAcctDr),0), COALESCE(SUM(AmtAcctCr),0), COALESCE(SUM(Qty),0)
			FROM Fact_Acct a
			WHERE a.AD_Client_ID=Fact_Acct_Balance.AD_Client_ID AND a.AD_Org_ID=Fact_Acct_Balance.AD_Org_ID
				AND a.C_AcctSchema_ID=Fact_Acct_Balance.C_AcctSchema_ID AND TRUNC(a.DateAcct)=TRUNC(Fact_Acct_Balance.DateAcct)
				AND a.Account_ID=Fact_Acct_Balance.Account_ID AND a.PostingType=Fact_Acct_Balance.PostingType
				AND COALESCE(a.M_Product_ID,0)=COALESCE(Fact_Acct_Balance.M_Product_ID,0) AND COALESCE(a.C_BPartner_ID,0)=COALESCE(Fact_Acct_Balance.C_BPartner_ID,0)
				AND COALESCE(a.C_Project_ID,0)=COALESCE(Fact_Acct_Balance.C_Project_ID,0) AND COALESCE(a.AD_OrgTrx_ID,0)=COALESCE(Fact_Acct_Balance.AD_OrgTrx_ID,0)
				AND COALESCE(a.C_SalesRegion_ID,0)=COALESCE(Fact_Acct_Balance.C_SalesRegion_ID,0) AND COALESCE(a.C_Activity_ID,0)=COALESCE(Fact_Acct_Balance.C_Activity_ID,0)
				AND COALESCE(a.C_Campaign_ID,0)=COALESCE(Fact_Acct_Balance.C_Campaign_ID,0) AND COALESCE(a.C_LocTo_ID,0)=COALESCE(Fact_Acct_Balance.C_LocTo_ID,0) AND COALESCE(a.C_LocFrom_ID,0)=COALESCE(Fact_Acct_Balance.C_LocFrom_ID,0)
				AND COALESCE(a.User1_ID,0)=COALESCE(Fact_Acct_Balance.User1_ID,0) AND COALESCE(a.User2_ID,0)=COALESCE(Fact_Acct_Balance.User2_ID,0) AND COALESCE(a.GL_Budget_ID,0)=COALESCE(Fact_Acct_Balance.GL_Budget_ID,0) 
			GROUP BY AD_Client_ID,AD_Org_ID, 
				C_AcctSchema_ID, TRUNC(DateAcct),
				Account_ID, PostingType,
				M_Product_ID, C_BPartner_ID,
				C_Project_ID, AD_OrgTrx_ID,
				C_SalesRegion_ID, C_Activity_ID,
				C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID,
				User1_ID, User2_ID, GL_Budget_ID)
		WHERE EXISTS 
			(SELECT *
			FROM Fact_Acct a
			WHERE a.AD_Client_ID=Fact_Acct_Balance.AD_Client_ID AND a.AD_Org_ID=Fact_Acct_Balance.AD_Org_ID
				AND a.C_AcctSchema_ID=Fact_Acct_Balance.C_AcctSchema_ID AND TRUNC(a.DateAcct)=TRUNC(Fact_Acct_Balance.DateAcct)
				AND a.Account_ID=Fact_Acct_Balance.Account_ID AND a.PostingType=Fact_Acct_Balance.PostingType
				AND COALESCE(a.M_Product_ID,0)=COALESCE(Fact_Acct_Balance.M_Product_ID,0) AND COALESCE(a.C_BPartner_ID,0)=COALESCE(Fact_Acct_Balance.C_BPartner_ID,0)
				AND COALESCE(a.C_Project_ID,0)=COALESCE(Fact_Acct_Balance.C_Project_ID,0) AND COALESCE(a.AD_OrgTrx_ID,0)=COALESCE(Fact_Acct_Balance.AD_OrgTrx_ID,0)
				AND COALESCE(a.C_SalesRegion_ID,0)=COALESCE(Fact_Acct_Balance.C_SalesRegion_ID,0) AND COALESCE(a.C_Activity_ID,0)=COALESCE(Fact_Acct_Balance.C_Activity_ID,0)
				AND COALESCE(a.C_Campaign_ID,0)=COALESCE(Fact_Acct_Balance.C_Campaign_ID,0) AND COALESCE(a.C_LocTo_ID,0)=COALESCE(Fact_Acct_Balance.C_LocTo_ID,0) AND COALESCE(a.C_LocFrom_ID,0)=COALESCE(Fact_Acct_Balance.C_LocFrom_ID,0)
				AND COALESCE(a.User1_ID,0)=COALESCE(Fact_Acct_Balance.User1_ID,0) AND COALESCE(a.User2_ID,0)=COALESCE(Fact_Acct_Balance.User2_ID,0) AND COALESCE(a.GL_Budget_ID,0)=COALESCE(Fact_Acct_Balance.GL_Budget_ID,0) 
			GROUP BY AD_Client_ID,AD_Org_ID, 
				C_AcctSchema_ID, TRUNC(DateAcct),
				Account_ID, PostingType,
				M_Product_ID, C_BPartner_ID,
				C_Project_ID, AD_OrgTrx_ID,
				C_SalesRegion_ID, C_Activity_ID,
				C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID,
				User1_ID, User2_ID, GL_Budget_ID);
		DBMS_OUTPUT.PUT_LINE('  Updates=' || SQL%ROWCOUNT);
	END IF;


	/** Insert		**/
	INSERT INTO Fact_Acct_Balance ab
		(AD_Client_ID, AD_Org_ID, 
		C_AcctSchema_ID, DateAcct,
		Account_ID, PostingType,
		M_Product_ID, C_BPartner_ID,
		C_Project_ID, AD_OrgTrx_ID,
		C_SalesRegion_ID,C_Activity_ID,
		C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID,
		User1_ID, User2_ID, GL_Budget_ID, 
		AmtAcctDr, AmtAcctCr, Qty)
	--
	SELECT AD_Client_ID, AD_Org_ID, 
		C_AcctSchema_ID, TRUNC(DateAcct),
		Account_ID, PostingType,
		M_Product_ID, C_BPartner_ID,
		C_Project_ID, AD_OrgTrx_ID,
		C_SalesRegion_ID,C_Activity_ID,
		C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID,
		User1_ID, User2_ID, GL_Budget_ID, 
		COALESCE(SUM(AmtAcctDr),0), COALESCE(SUM(AmtAcctCr),0), COALESCE(SUM(Qty),0)
	FROM Fact_Acct a
	WHERE NOT EXISTS 
		(SELECT * 
		FROM Fact_Acct_Balance x
		WHERE a.AD_Client_ID=x.AD_Client_ID AND a.AD_Org_ID=x.AD_Org_ID
			AND a.C_AcctSchema_ID=x.C_AcctSchema_ID AND TRUNC(a.DateAcct)=TRUNC(x.DateAcct)
			AND a.Account_ID=x.Account_ID AND a.PostingType=x.PostingType
			AND COALESCE(a.M_Product_ID,0)=COALESCE(x.M_Product_ID,0) AND COALESCE(a.C_BPartner_ID,0)=COALESCE(x.C_BPartner_ID,0)
			AND COALESCE(a.C_Project_ID,0)=COALESCE(x.C_Project_ID,0) AND COALESCE(a.AD_OrgTrx_ID,0)=COALESCE(x.AD_OrgTrx_ID,0)
			AND COALESCE(a.C_SalesRegion_ID,0)=COALESCE(x.C_SalesRegion_ID,0) AND COALESCE(a.C_Activity_ID,0)=COALESCE(x.C_Activity_ID,0)
			AND COALESCE(a.C_Campaign_ID,0)=COALESCE(x.C_Campaign_ID,0) AND COALESCE(a.C_LocTo_ID,0)=COALESCE(x.C_LocTo_ID,0) AND COALESCE(a.C_LocFrom_ID,0)=COALESCE(x.C_LocFrom_ID,0)
			AND COALESCE(a.User1_ID,0)=COALESCE(x.User1_ID,0) AND COALESCE(a.User2_ID,0)=COALESCE(x.User2_ID,0) AND COALESCE(a.GL_Budget_ID,0)=COALESCE(x.GL_Budget_ID,0) )
	GROUP BY AD_Client_ID,AD_Org_ID, 
		C_AcctSchema_ID, TRUNC(DateAcct),
		Account_ID, PostingType,
		M_Product_ID, C_BPartner_ID,
		C_Project_ID, AD_OrgTrx_ID,
		C_SalesRegion_ID, C_Activity_ID,
		C_Campaign_ID, C_LocTo_ID, C_LocFrom_ID,
		User1_ID, User2_ID, GL_Budget_ID;
	DBMS_OUTPUT.PUT_LINE('  Inserts=' || SQL%ROWCOUNT);

	-----------------------
	--COMMIT;

END Fact_Acct_Balance_Update;
--end--

CREATE OR REPLACE FUNCTION productAttribute
(
    p_M_AttributeSetInstance_ID     IN INTEGER
)
RETURN VARCHAR2
--jz RETURN NVARCHAR2
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: M_Attribute_Name.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Return Instance Attribute Info
 * Description:
 *		
 * Test:
    SELECT M_Attribute_Name (M_AttributeSetInstance_ID) 
    FROM M_InOutLine WHERE M_AttributeSetInstance_ID > 0
    --
    SELECT p.Name
    FROM C_InvoiceLine il LEFT OUTER JOIN M_Product p ON (il.M_Product_ID=p.M_Product_ID);
    SELECT p.Name || M_Attribute_Name (il.M_AttributeSetInstance_ID) 
    FROM C_InvoiceLine il LEFT OUTER JOIN M_Product p ON (il.M_Product_ID=p.M_Product_ID);
    
 ************************************************************************/
AS
--jz    v_Name          NVARCHAR2(2000) := NULL;
--jz    v_NameAdd       NVARCHAR2(2000) := '';
   v_Name          VARCHAR2(2000) := NULL;
   v_NameAdd       VARCHAR2(2000) := '';
   --
   v_Lot           M_AttributeSetInstance.Lot%TYPE;
   v_LotStart      M_AttributeSet.LotCharSOverwrite%TYPE;
   v_LotEnd        M_AttributeSet.LotCharEOverwrite%TYPE;
   v_SerNo         M_AttributeSetInstance.SerNo%TYPE;
   v_SerNoStart    M_AttributeSet.SerNoCharSOverwrite%TYPE;
   v_SerNoEnd      M_AttributeSet.SerNoCharEOverwrite%TYPE;
   v_GuaranteeDate M_AttributeSetInstance.GuaranteeDate%TYPE;
   --
    CURSOR CUR_Attributes IS
        SELECT ai.Value, a.Name
        FROM M_AttributeInstance ai
          INNER JOIN M_Attribute a ON (ai.M_Attribute_ID=a.M_Attribute_ID AND a.IsInstanceAttribute='Y')
        WHERE ai.M_AttributeSetInstance_ID=p_M_AttributeSetInstance_ID;

BEGIN
/*    --  Get Product Name
    SELECT Name 
      INTO v_Name
    FROM M_Product WHERE M_Product_ID=p_M_Product_ID;
*/
   --  Get Product Attribute Set Instance
    IF (p_M_AttributeSetInstance_ID > 0) THEN
        SELECT asi.Lot, asi.SerNo, asi.GuaranteeDate,
          	COALESCE(a.SerNoCharSOverwrite, N'#'), COALESCE(a.SerNoCharEOverwrite, N''),
          	COALESCE(a.LotCharSOverwrite, N'«'), COALESCE(a.LotCharEOverwrite, N'»')
         INTO v_Lot, v_SerNo, v_GuaranteeDate,
           v_SerNoStart, v_SerNoEnd, v_LotStart, v_LotEnd
        FROM M_AttributeSetInstance asi
          INNER JOIN M_AttributeSet a ON (asi.M_AttributeSet_ID=a.M_AttributeSet_ID)
        WHERE asi.M_AttributeSetInstance_ID=p_M_AttributeSetInstance_ID;
       --
        IF (v_SerNo IS NOT NULL) THEN
           v_NameAdd := v_NameAdd || v_SerNoStart || v_SerNo || v_SerNoEnd || ' ';
        END IF;
        IF (v_Lot IS NOT NULL) THEN
           v_NameAdd := v_NameAdd || v_LotStart || v_Lot || v_LotEnd || ' ';
        END IF;
        IF (v_GuaranteeDate IS NOT NULL) THEN
            v_NameAdd := v_NameAdd || v_GuaranteeDate || ' ';
        END IF;
       --
        FOR a IN CUR_Attributes LOOP
           v_NameAdd := v_NameAdd || a.Name || ':' || a.Value || ' ';
        END LOOP;
       --
        IF (LENGTH(v_NameAdd) > 0) THEN
           v_Name := v_Name || ' (' || TRIM(v_NameAdd) || ')';
        END IF;
    END IF;

    RETURN v_Name;
END productAttribute;
--end--

CREATE OR REPLACE FUNCTION paymentTermDueDate
(
	PaymentTerm_ID	IN	INTEGER,
	DocDate			IN	DATE
)
RETURN DATE
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: C_PaymentTerm_DueDate.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Get Due Date
 * Description:
 *	Returns the due date
 ************************************************************************/
AS
 	Days				INTEGER := 0;
	DueDate				DATE := TRUNC(DocDate);
	--
	CURSOR Cur_PT	IS
		SELECT	*
		FROM	C_PaymentTerm
		WHERE	C_PaymentTerm_ID = PaymentTerm_ID;
	FirstDay			DATE;
	NoDays				INTEGER;
BEGIN
	FOR p IN Cur_PT LOOP	--	for convineance only
	--	DBMS_OUTPUT.PUT_LINE(p.Name || ' - Doc = ' || TO_CHAR(DocDate));
		--	Due 15th of following month
		IF (p.IsDueFixed = 'Y') THEN		
		--	DBMS_OUTPUT.PUT_LINE(p.Name || ' - Day = ' || p.FixMonthDay);
			FirstDay := TRUNC(DocDate, 'MM');
			NoDays := TRUNC(DocDate) - FirstDay;
			DueDate := FirstDay + (p.FixMonthDay-1);	--	starting on 1st
			DueDate := ADD_MONTHS(DueDate, p.FixMonthOffset);
			IF (NoDays > p.FixMonthCutoff) THEN
				DueDate := ADD_MONTHS(DueDate, 1);
			END IF;
		ELSE
		--	DBMS_OUTPUT.PUT_LINE('Net = ' || p.NetDays);
			DueDate := TRUNC(DocDate) + p.NetDays;
		END IF;
	END LOOP;
--	DBMS_OUTPUT.PUT_LINE('Due = ' || TO_CHAR(DueDate) || ', Pay = ' || TO_CHAR(PayDate));

	RETURN DueDate;
END paymentTermDueDate;
--end--

CREATE OR REPLACE FUNCTION acctBalance
(
    p_Account_ID    IN INTEGER,
    p_AmtDr         IN NUMBER,
    p_AmtCr         IN NUMBER
)
RETURN NUMBER
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2004 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: Acct_Balance.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Aclculate Balance based on Account Sign + Type
 * Description:
 *  If an account is specified and found
 *  - If the account sign is Natural it sets it based on Account Type
 *  Returns Credit or Debit Balance
 * Test:
    SELECT Acct_Balance (0,11,22) FROM DUAL
    SELECT AccountType, AccountSign, 
        Acct_Balance(C_ElementValue_ID, 20, 10) "DR Balance",
        Acct_Balance(C_ElementValue_ID, 10, 20) "CR Balance"
    FROM C_ElementValue
    WHERE AccountSign<>'N'
    ORDER BY AccountSign
 ************************************************************************/
AS
    v_balance           NUMBER;
    v_AccountType       C_ElementValue.AccountType%TYPE;
    v_AccountSign       C_ElementValue.AccountSign%TYPE;
BEGIN
    v_balance := p_AmtDr - p_AmtCr;
    --  
    IF (p_Account_ID > 0) THEN
        SELECT AccountType, AccountSign
          INTO v_AccountType, v_AccountSign
        FROM C_ElementValue
        WHERE C_ElementValue_ID=p_Account_ID;
   --   DBMS_OUTPUT.PUT_LINE('Type=' || v_AccountType || ' - Sign=' || v_AccountSign);
        --  Natural Account Sign
        IF (v_AccountSign='N') THEN
            IF (v_AccountType IN ('A','E')) THEN
                v_AccountSign := 'D';
            ELSE
                v_AccountSign := 'C';
            END IF;
        --  DBMS_OUTPUT.PUT_LINE('Type=' || v_AccountType || ' - Sign=' || v_AccountSign);
        END IF;
        --  Debit Balance
        IF (v_AccountSign = 'C') THEN
            v_balance := p_AmtCr - p_AmtDr;
        END IF;
    END IF;
    --
    RETURN v_balance;
EXCEPTION WHEN OTHERS THEN
    -- In case Acct not found
    RETURN  p_AmtDr - p_AmtCr;
END acctBalance;
--end--

CREATE OR REPLACE PROCEDURE nextID
(
	p_AD_Sequence_ID    		IN  INTEGER,
    p_System                    IN  CHAR,
    o_NextID                    OUT INTEGER
)
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2005 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: nextID.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Get Next ID - no Commit
 * Description:
 *          Test via
DECLARE
    v_NextID       INTEGER;
BEGIN
    nextID(2, 'Y', v_NextID);
	DBMS_OUTPUT.PUT_LINE(v_NextID);
END;
 * 
 ************************************************************************/
AS
BEGIN
    IF (p_System = 'Y') THEN
        SELECT CurrentNextSys
            INTO o_NextID
        FROM AD_Sequence
        WHERE AD_Sequence_ID=p_AD_Sequence_ID
        FOR UPDATE OF AD_Sequence;
--       FOR UPDATE OF CurrentNextSys;
        --
        UPDATE AD_Sequence
          SET CurrentNextSys = CurrentNextSys + IncrementNo
        WHERE AD_Sequence_ID=p_AD_Sequence_ID;
    ELSE
        SELECT CurrentNext
            INTO o_NextID
        FROM AD_Sequence
        WHERE AD_Sequence_ID=p_AD_Sequence_ID
        FOR UPDATE OF AD_Sequence;
--        FOR UPDATE OF CurrentNext;
        --
        UPDATE AD_Sequence
          SET CurrentNext = CurrentNext + IncrementNo
        WHERE AD_Sequence_ID=p_AD_Sequence_ID;
    END IF;
    --
EXCEPTION
    WHEN  OTHERS THEN
    	DBMS_OUTPUT.PUT_LINE(SQLERRM);
END nextID;
--end--

/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: Temporary.sql,v 1.1 2006/04/21 17:51:59 jjanke Exp $
 ***
 * Title:	Temporary Tables
 * Description:	
 ************************************************************************/

--jz DROP TABLE T_Selection CASCADE CONSTRAINTS
--;
--	Truely temporary table
CREATE GLOBAL TEMPORARY TABLE T_Selection	
(
	T_Selection_ID	INTEGER NOT NULL	
		CONSTRAINT T_Selection_Key PRIMARY KEY
)
ON COMMIT DELETE ROWS
;
--end--


--DROP TABLE T_Selection2
--;
--	Temporary table over commit
CREATE GLOBAL TEMPORARY TABLE T_Selection2 
(
	Query_ID	   INTEGER	  NOT NULL,
	T_Selection_ID INTEGER NOT NULL,
	CONSTRAINT T_Selection2_Key PRIMARY KEY (Query_ID,T_Selection_ID)
)
ON COMMIT PRESERVE ROWS 
;


/**
 *	Spool Table
 */
DROP SEQUENCE T_Spool_Seq
;
CREATE SEQUENCE T_Spool_Seq
	INCREMENT BY 1
	START WITH	 1
;
-- INSERT INTO T_Spool (AD_PInstance_ID, SeqNo, Msg) VALUES (123, T_Spool_Seq.NextVal, 'ggg');



/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2001 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: Sequences.sql,v 1.1 2006/04/21 17:51:59 jjanke Exp $
 ***
 * Title:	Sequences
 * Description:	
 *			(Re)Create  Sequences
 ************************************************************************/



/**
 *	Error Messages (deleteable) Primary Key
 */
TRUNCATE TABLE AD_Error
;
DROP SEQUENCE AD_Error_Seq
;
CREATE SEQUENCE AD_Error_Seq 
	START WITH 1
	INCREMENT BY 1
;


/**
 * Process Log
 */
DELETE FROM T_Report
;
DELETE FROM AD_PInstance
;
DROP SEQUENCE AD_PInstance_Seq
;
CREATE SEQUENCE AD_PInstance_Seq 
	START WITH 1
	INCREMENT BY 1
;

/**
 *	T_Spool (Global Temporary Table) Primary Key
 */
DROP SEQUENCE T_Spool_Seq
;
CREATE SEQUENCE T_Spool_Seq 
	START WITH 1
	INCREMENT BY 1
;

/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html 
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either 
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2002 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: Views.sql,v 1.4 2006/10/09 00:25:53 jjanke Exp $
 ***
 * Title:	Views
 * Description:	
 *		These views should be generated out of ER/Studio, but due to bugs
 *		this needs to be done manually at this time.
 *      Afterwards run: PrintFormatUtil
 *
 *		Dictionary
 *			AD_Field_v / _vt
 *			AD_Tab_v / _vt
 *			AD_Window_vt
 *			AD_User_Roles_v
 *          AD_Org_v
 *
 *		Report
 *			C_Invoice_Header_v / _vt
 *			C_Invoice_LineTax_v / _vt
 *			C_Order_Header_v / _vt
 *			C_Order_LineTax_v / _vt
 *			C_PaySelection_Check_v / _vt
 *			C_PaySelection_Remittance_v / _vt
 *			M_InOut_Header_v / _vt
 *			M_InOut_Line_v / _vt
 *			C_Project_Header_v / _vt
 *			C_Project_Details_v / _vt
 *          C_RfQ_v, C_RfQLine_v, C_RfQLineQty_v /_vt
 *
 *		Other
 *			R_Request_v
 *
 *		Report Views
 *			RV_C_Invoice, RV_C_InvoiceLine, RV_C_InvoiceTax
 *			RV_C_Invoice_Day, RV_C_Invoice_Week, RV_C_Invoice_Month 
 *			RV_C_Invoice_CustomerProdQtr, RV_C_Invoice_CustomerVendQtr 
 *			RV_C_Invoice_ProdWeek, RV_C_Invoice_ProdMonth 
 *			RV_C_Invoice_VendorMonth
 *			RV_M_Transaction, RV_M_Transaction_Sum
 *			RV_OpenItem
 *			RV_Order_Open
 *			RV_Cash_Detail
 *			RV_BPartner
 *			RV_Product_Costing
 *          RV_ProjectCycle
 *          RV_Asset_Customer, RV_Asset_Delivery, RV_Asset_SumMonth
 *          RV_Storage, RV_Transaction
 *          RV_Click_Month, RV_Click_Umprocessed
 *          RV_UnPosted
 *          RV_WarehousePrice
 *          RV_Fact_Acct*
 *          RV_C_RfQ_UnAnswered, RV_C_RfQResponse
 *          RV_M_Requisition
 *          RV_InOutConfirm, RV_InOutLineConfirm
 *          RV_Allocation
 *          RV_InOutDetails
 *
 *		Utility Views
 *			C_Invoice_v, C_InvoiceLine_v
 *			C_Invoice_Candidate_v 
 *			M_InOut_Candidate_v 
 *			GL_JournalLine_Acct_v
 *			C_Payment_v
 *
 ************************************************************************/


-----   Temp Conveniance - original in createSQLJ.sql
 

/** Get Character at Position   */
CREATE OR REPLACE FUNCTION charAt
(
    p_string    VARCHAR2,
    p_pos       INTEGER
)
 	RETURN VARCHAR2
AS
BEGIN
    RETURN SUBSTR(p_string, p_pos, 1);
END;    
--end--
/** GetDate                     */
CREATE OR REPLACE FUNCTION getdate
 	RETURN DATE
AS
BEGIN
    RETURN SysDate;
END;    
--end--
/** First Of DD/DY/MM/Q         */
CREATE OR REPLACE FUNCTION firstOf
(
    p_date      DATE,
    p_datePart  VARCHAR2
)
 	RETURN DATE
AS
BEGIN
    RETURN TRUNC(p_date, p_datePart);
END;    
--end--
/** Add Number of Days      */
CREATE OR REPLACE FUNCTION addDays
(
    p_date      DATE,
    p_days      INTEGER
)
 	RETURN DATE
AS
BEGIN
    RETURN TRUNC(p_date) + p_days;
END;    
--end--
/** Difference in Days      */
CREATE OR REPLACE FUNCTION daysBetween
(
    p_date1     DATE,
    p_date2     DATE
)
 	RETURN INTEGER
AS
BEGIN
    RETURN TO_NUMBER(SUBSTR(TRUNC(p_date1) - TRUNC(p_date2), 3, CHAR_LENGTH(TRUNC(p_date1) - TRUNC(p_date2))-7));
END;    

/** Difference in Days      */
CREATE OR REPLACE FUNCTION getDaysBetween
(
    p_date1     DATE,
    p_date2     DATE
)
 	RETURN INTEGER
AS
BEGIN
    --RETURN (TRUNC(p_date1) - TRUNC(p_date2));
    RETURN TO_NUMBER(SUBSTR(TRUNC(p_date1) - TRUNC(p_date2), 3, CHAR_LENGTH(TRUNC(p_date1) - TRUNC(p_date2))-7));
END;    

/** getChars      */
CREATE OR REPLACE FUNCTION getChars
(
    p_num     NUMBER
)
 	RETURN VARCHAR2
AS
BEGIN
    RETURN TO_CHAR(p_num);
END; 
--end--

CREATE OR REPLACE PROCEDURE M_PriceList_Create
( 
	PInstance_ID			IN INTEGER
)
AS
/*************************************************************************
 * The contents of this file are subject to the Compiere License.  You may
 * obtain a copy of the License at    http://www.compiere.org/license.html
 * Software is on an  "AS IS" basis,  WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the License for details. Code: Compiere ERP+CRM
 * Copyright (C) 1999-2003 Jorg Janke, ComPiere, Inc. All Rights Reserved.
 *************************************************************************
 * $Id: M_PriceList_Create.sql,v 1.1 2006/04/21 17:51:58 jjanke Exp $
 ***
 * Title:	Create Pricelist
 * Description:
 *		Create PriceList by copying purchase prices (M_Product_PO) 
 *		and applying product category discounts (M_CategoryDiscount)
 ************************************************************************/
	--	Logistice
	ResultStr						VARCHAR2(2000);
	Message							VARCHAR2(2000) := '';
	-- NoRate							EXCEPTION;
	--	Parameter
	CURSOR Cur_Parameter (PInstance INTEGER) IS
		SELECT i.Record_ID, p.ParameterName, p.P_String, p.P_Number, p.P_Date
		FROM AD_PInstance i, AD_PInstance_Para p
		WHERE i.AD_PInstance_ID=PInstance
		AND i.AD_PInstance_ID=p.AD_PInstance_ID(+)
		ORDER BY p.SeqNo;
	--	Parameter Variables
	p_PriceList_Version_ID			INTEGER;
	p_DeleteOld						CHAR(1) := 'N';
	--
	v_Currency_ID					INTEGER;
	v_Client_ID						INTEGER;
	v_Org_ID						INTEGER;
	v_UpdatedBy						INTEGER;
	v_StdPrecision					INTEGER;
	v_DiscountSchema_ID				INTEGER;
	v_PriceList_Version_Base_ID		INTEGER;
	--
	v_NextNo						INTEGER := 0;

	--	Get PL Parameter
	CURSOR Cur_DiscountLine (DiscountSchema_ID INTEGER) IS
		SELECT	* 
		FROM	M_DiscountSchemaLine
		WHERE	M_DiscountSchema_ID=DiscountSchema_ID
		  AND	IsActive='Y'
		ORDER BY SeqNo;

BEGIN
	--  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Processing');
	ResultStr := 'PInstanceNotFound';
	UPDATE AD_PInstance
	SET Created = SysDate,
		IsProcessing = 'Y'
	WHERE AD_PInstance_ID=PInstance_ID;
	--COMMIT;

	--	Get Parameters
	ResultStr := 'ReadingParameters';
	FOR p IN Cur_Parameter (PInstance_ID) LOOP
		p_PriceList_Version_ID := p.Record_ID;
		IF (p.ParameterName = 'DeleteOld') THEN
			p_DeleteOld := p.P_String;
			DBMS_OUTPUT.PUT_LINE('  DeleteOld=' || p_DeleteOld);
		ELSE
			DBMS_OUTPUT.PUT_LINE('*** Unknown Parameter=' || p.ParameterName);
		END IF;
	END LOOP;	--	Get Parameter
	DBMS_OUTPUT.PUT_LINE('  PriceList_Version_ID=' || p_PriceList_Version_ID);

	--	Checking Prerequisites
	--	--	PO Prices must exists
	ResultStr := 'CorrectingProductPO';
	DBMS_OUTPUT.PUT_LINE(ResultStr);
	UPDATE	M_Product_PO
	  SET	PriceList = 0
	WHERE	PriceList IS NULL;
	UPDATE	M_Product_PO	
	  SET	PriceLastPO = 0
	WHERE	PriceLastPO IS NULL;
	UPDATE	M_Product_PO
	  SET	PricePO = PriceLastPO
	WHERE	(PricePO IS NULL OR PricePO = 0) AND PriceLastPO <> 0;
	UPDATE	M_Product_PO
	  SET	PricePO = 0
	WHERE	PricePO IS NULL;
	-- Set default current vendor
	UPDATE	M_Product_PO 
	  SET	IsCurrentVendor = 'Y'
	WHERE	IsCurrentVendor = 'N' 
	  AND NOT EXISTS 
		(SELECT pp.M_Product_ID FROM M_Product_PO pp
		WHERE pp.M_Product_ID=M_Product_PO.M_Product_ID
		GROUP BY pp.M_Product_ID HAVING COUNT(*) > 1);
	--COMMIT;

	/**
	 *	Make sure that we have only one active product
	 */
	ResultStr := 'CorrectingDuplicates';
	DBMS_OUTPUT.PUT_LINE(ResultStr);
	DECLARE
		--	All duplicate products
		CURSOR	Cur_Duplicates	IS
			SELECT	DISTINCT M_Product_ID
			FROM	M_Product_PO po
			WHERE	IsCurrentVendor='Y' AND IsActive='Y'
			  AND EXISTS (	SELECT M_Product_ID FROM M_Product_PO x 
							WHERE x.M_Product_ID=po.M_Product_ID 
							GROUP BY M_Product_ID HAVING COUNT(*) > 1 )
			ORDER BY 1;
		--	All vendors of Product - expensive first
		CURSOR	Cur_Vendors	(Product_ID INTEGER) IS
			SELECT	M_Product_ID, C_BPartner_ID
			FROM	M_Product_PO
			WHERE	IsCurrentVendor='Y' AND IsActive='Y'
			  AND	M_Product_ID=Product_ID
			ORDER BY PriceList DESC;
		--
		Product_ID				INTEGER;
		BPartner_ID				INTEGER;
	BEGIN
		FOR dupl IN Cur_Duplicates LOOP
			OPEN Cur_Vendors (dupl.M_Product_ID);
			FETCH Cur_Vendors INTO Product_ID, BPartner_ID;		--	Leave First
			LOOP
				FETCH Cur_Vendors INTO Product_ID, BPartner_ID;	--	Get Record ID
				EXIT WHEN Cur_Vendors%NOTFOUND;
				--
				DBMS_OUTPUT.PUT_LINE('  Record: ' || Product_ID || ' / ' || BPartner_ID);
				UPDATE	M_Product_PO
				  SET	IsCurrentVendor='N'
				WHERE	M_Product_ID=Product_ID AND C_BPartner_ID=BPartner_ID;
			END LOOP;
			CLOSE Cur_Vendors;
		END LOOP;
		--COMMIT;
	END;
	
	/**	Delete Old Data	*/
	ResultStr := 'DeletingOld';
	IF (p_DeleteOld = 'Y') THEN
		DELETE FROM	M_ProductPrice
		WHERE	M_PriceList_Version_ID = p_PriceList_Version_ID;
		Message := '@Deleted@=' || SQL%ROWCOUNT || ' - ';
		DBMS_OUTPUT.PUT_LINE(Message);
	END IF;

	--	Get PriceList Info
	ResultStr := 'GetPLInfo';
	DBMS_OUTPUT.PUT_LINE(ResultStr);
	SELECT	p.C_Currency_ID, c.StdPrecision,
		v.AD_Client_ID, v.AD_Org_ID, v.UpdatedBy, 
		v.M_DiscountSchema_ID, M_PriceList_Version_Base_ID
	  INTO	v_Currency_ID, v_StdPrecision,
		v_Client_ID, v_Org_ID, v_UpdatedBy, 
		v_DiscountSchema_ID, v_PriceList_Version_Base_ID
	FROM	M_PriceList p, M_PriceList_Version v, C_Currency c
	WHERE	p.M_PriceList_ID=v.M_PriceList_ID 
	  AND	p.C_Currency_ID=c.C_Currency_ID
	  AND	v.M_PriceList_Version_ID=p_PriceList_Version_ID;

	/**
	 *	For All Discount Lines in Sequence
	 */
	FOR dl IN Cur_DiscountLine (v_DiscountSchema_ID) LOOP
		ResultStr := 'Parameter Seq=' || dl.SeqNo;
	--	DBMS_OUTPUT.PUT_LINE(ResultStr);

		--	Clear Temporary Table
		DELETE FROM T_Selection;

		--	-----------------------------------
		--	Create Selection in temporary table
		--	-----------------------------------
		IF (v_PriceList_Version_Base_ID IS NULL) THEN
		--	Create Selection from M_Product_PO
			INSERT INTO T_Selection (T_Selection_ID)
			SELECT	DISTINCT po.M_Product_ID 
			FROM	M_Product p, M_Product_PO po
			WHERE	p.M_Product_ID=po.M_Product_ID
			  AND	(p.AD_Client_ID=v_Client_ID OR p.AD_Client_ID=0)
			  AND	p.IsActive='Y' AND po.IsActive='Y' AND po.IsCurrentVendor='Y'
			--	Optional Restrictions
			  AND (dl.M_Product_Category_ID IS NULL OR p.M_Product_Category_ID=dl.M_Product_Category_ID)
			  AND (dl.C_BPartner_ID IS NULL OR po.C_BPartner_ID=dl.C_BPartner_ID)
			  AND (dl.M_Product_ID IS NULL OR p.M_Product_ID=dl.M_Product_ID);
		ELSE
		--	Create Selection from existing PriceList
			INSERT INTO T_Selection (T_Selection_ID)
			SELECT	DISTINCT p.M_Product_ID 
			FROM	M_Product p, M_ProductPrice pp
			WHERE	p.M_Product_ID=pp.M_Product_ID
			  AND	pp.M_PriceList_Version_ID=v_PriceList_Version_Base_ID
			  AND	p.IsActive='Y' AND pp.IsActive='Y'
			--	Optional Restrictions
			  AND	(dl.M_Product_Category_ID IS NULL OR p.M_Product_Category_ID=dl.M_Product_Category_ID)
			  AND	(dl.C_BPartner_ID IS NULL OR EXISTS 
					(SELECT * FROM M_Product_PO po WHERE po.M_Product_ID=p.M_Product_ID AND po.C_BPartner_ID=dl.C_BPartner_ID))
			  AND	(dl.M_Product_ID IS NULL OR p.M_Product_ID=dl.M_Product_ID);
		END IF;
		Message := Message || '@Selected@=' || SQL%ROWCOUNT;
	--	DBMS_OUTPUT.PUT_LINE(Message);

		--	Delete Prices in Selection, so that we can insert
		IF (v_PriceList_Version_Base_ID IS NULL
				OR v_PriceList_Version_Base_ID <> p_PriceList_Version_ID) THEN
			ResultStr := ResultStr || ', Delete';
			--jz DELETE	M_ProductPrice pp
			DELETE FROM M_ProductPrice 
			WHERE	M_PriceList_Version_ID = p_PriceList_Version_ID
			  AND EXISTS (SELECT * FROM T_Selection s WHERE M_ProductPrice.M_Product_ID=s.T_Selection_ID);
			Message := ', @Deleted@=' || SQL%ROWCOUNT;
		END IF;

		--	--------------------
		--	Copy (Insert) Prices
		--	--------------------
		IF (v_PriceList_Version_Base_ID = p_PriceList_Version_ID) THEN
		--	We have Prices already
			NULL;
		ELSIF (v_PriceList_Version_Base_ID IS NULL) THEN
		--	Copy and Convert from Product_PO
			ResultStr := ResultStr || ',Copy_PO';
			INSERT INTO M_ProductPrice
				(M_PriceList_Version_ID, M_Product_ID,
				AD_Client_ID, AD_Org_ID, IsActive, Created, CreatedBy, Updated, UpdatedBy,
				PriceList, PriceStd, PriceLimit)
			SELECT 
				p_PriceList_Version_ID, po.M_Product_ID, 
				v_Client_ID, v_Org_ID, 'Y', SysDate, v_UpdatedBy, SysDate, v_UpdatedBy,
				--	Price List
				COALESCE(currencyConvert(po.PriceList, 
					po.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0), 
				--	Price Std
				COALESCE(currencyConvert(po.PriceList, 
					po.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0), 
				--	Price Limit
				COALESCE(currencyConvert(po.PricePO,
						po.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0)
			FROM	M_Product_PO po
			WHERE EXISTS (SELECT * FROM T_Selection s WHERE po.M_Product_ID=s.T_Selection_ID)
			  AND	po.IsCurrentVendor='Y' AND po.IsActive='Y';
		ELSE
		--	Copy and Convert from other PriceList_Version
			ResultStr := ResultStr || ',Copy_PL';
			INSERT INTO M_ProductPrice
				(M_PriceList_Version_ID, M_Product_ID,
				AD_Client_ID, AD_Org_ID, IsActive, Created, CreatedBy, Updated, UpdatedBy,
				PriceList, PriceStd, PriceLimit)
			SELECT 
				p_PriceList_Version_ID, pp.M_Product_ID, 
				v_Client_ID, v_Org_ID, 'Y', SysDate, v_UpdatedBy, SysDate, v_UpdatedBy,
				--	Price List
				COALESCE(currencyConvert(pp.PriceList, 
					pl.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0), 
				--	Price Std
				COALESCE(currencyConvert(pp.PriceStd, 
					pl.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0), 
				--	Price Limit
				COALESCE(currencyConvert(pp.PriceLimit,
					pl.C_Currency_ID, v_Currency_ID, dl.ConversionDate, dl.C_ConversionType_ID, v_Client_ID, v_Org_ID),0)
			FROM M_ProductPrice pp
                INNER JOIN M_PriceList_Version plv ON (pp.M_PriceList_Version_ID=plv.M_PriceList_Version_ID)
                INNER JOIN M_PriceList pl ON (plv.M_PriceList_ID=pl.M_PriceList_ID)
			WHERE	pp.M_PriceList_Version_ID=v_PriceList_Version_Base_ID
			  AND EXISTS (SELECT * FROM T_Selection s WHERE pp.M_Product_ID=s.T_Selection_ID)
			  AND	pp.IsActive='Y';
		END IF;
		Message := Message || ', @Inserted@=' || SQL%ROWCOUNT;

		--	-----------
		--	Calculation
		--	-----------
		ResultStr := ResultStr || ',Calc';
		--jz UPDATE	M_ProductPrice p
		UPDATE	M_ProductPrice 
		  SET	PriceList = (TO_NUMBER(DECODE(dl.List_Base, 'S', PriceStd, 'X', PriceLimit, PriceList)) 
					+ dl.List_AddAmt) * (1 - dl.List_Discount/100),
				PriceStd = (TO_NUMBER(DECODE(dl.Std_Base, 'L', PriceList, 'X', PriceLimit, PriceStd)) 
					+ dl.Std_AddAmt) * (1 - dl.Std_Discount/100),
				PriceLimit = (TO_NUMBER(DECODE(dl.Limit_Base, 'L', PriceList, 'S', PriceStd, PriceLimit)) 
					+ dl.Limit_AddAmt) * (1 - dl.Limit_Discount/100)
		WHERE	M_PriceList_Version_ID=p_PriceList_Version_ID
		  AND EXISTS	(SELECT * FROM T_Selection s
						WHERE s.T_Selection_ID=M_ProductPrice.M_Product_ID);

		--	--------
		-- 	Rounding	(AD_Reference_ID=155)
		--	--------
		ResultStr := ResultStr || ',Round';
		--jz UPDATE	M_ProductPrice p
		UPDATE	M_ProductPrice 
		  SET	PriceList = TO_NUMBER(DECODE(dl.List_Rounding, 
					'N', PriceList,
					'0', ROUND(PriceList, 0),	--	Even .00					
					'D', ROUND(PriceList, 1),	--	Dime .10
					'T', ROUND(PriceList, -1),	--	Ten 10.00
					'5', ROUND(PriceList*20,0)/20,	--	Nickle .05
					'Q', ROUND(PriceList*4,0)/4,	--	Quarter .25	
					ROUND(PriceList, v_StdPrecision))),--	Currency
				PriceStd = TO_NUMBER(DECODE(dl.Std_Rounding, 
					'N', PriceStd, 
					'0', ROUND(PriceStd, 0),	--	Even .00					
					'D', ROUND(PriceStd, 1),	--	Dime .10
					'T', ROUND(PriceStd, -1),	--	Ten 10.00
					'5', ROUND(PriceStd*20,0)/20,	--	Nickle .05
					'Q', ROUND(PriceStd*4,0)/4,		--	Quarter .25	
					ROUND(PriceStd, v_StdPrecision))),	--	Currency
				PriceLimit = TO_NUMBER(DECODE(dl.Limit_Rounding, 
					'N', PriceLimit,
					'0', ROUND(PriceLimit, 0),	--	Even .00					
					'D', ROUND(PriceLimit, 1),	--	Dime .10
					'T', ROUND(PriceLimit, -1),	--	Ten 10.00
					'5', ROUND(PriceLimit*20,0)/20,	--	Nickle .05
					'Q', ROUND(PriceLimit*4,0)/4,	--	Quarter .25	
					ROUND(PriceLimit, v_StdPrecision)))--	Currency
		WHERE	M_PriceList_Version_ID=p_PriceList_Version_ID
		  AND EXISTS	(SELECT * FROM T_Selection s
						WHERE s.T_Selection_ID=M_ProductPrice.M_Product_ID);
		Message := Message || ', @Updated@=' || SQL%ROWCOUNT;

		--	Fixed Price overwrite
		ResultStr := ResultStr || ',Fix';
		UPDATE	M_ProductPrice 
		  SET	PriceList = TO_NUMBER(DECODE(dl.List_Base, 'F', dl.List_Fixed, PriceList)), 
				PriceStd = TO_NUMBER(DECODE(dl.Std_Base, 'F', dl.Std_Fixed, PriceStd)),
				PriceLimit = TO_NUMBER(DECODE(dl.Limit_Base, 'F', dl.Limit_Fixed, PriceLimit)) 
		WHERE	M_PriceList_Version_ID=p_PriceList_Version_ID
		  AND EXISTS	(SELECT * FROM T_Selection s
						WHERE s.T_Selection_ID=M_ProductPrice.M_Product_ID);

		--	Log Info
		INSERT INTO AD_PInstance_Log	(AD_PInstance_ID, Log_ID, P_ID, P_NUMBER, P_MSG)
		VALUES							(PInstance_ID, v_NextNo, null, dl.SeqNo, Message);
		--
		v_NextNo := v_NextNo + 1;
		Message := '';
	END LOOP;	--	For all DiscountLines

	--	Delete Temporary Selection
	DELETE FROM T_Selection;

--<<FINISH_PROCESS>>
	--  Update AD_PInstance
	DBMS_OUTPUT.PUT_LINE(Message);
	DBMS_OUTPUT.PUT_LINE('Updating PInstance - Finished');
	UPDATE	AD_PInstance
	SET Updated = SysDate,
		IsProcessing = 'N',
		Result = 1,					-- success
		ErrorMsg = Message
	WHERE	AD_PInstance_ID=PInstance_ID;
	--COMMIT;
	RETURN;

EXCEPTION
	WHEN OTHERS THEN
		ResultStr := ResultStr || ':' || SQLERRM || ' ' || Message;
		DBMS_OUTPUT.PUT_LINE(ResultStr);
		UPDATE	AD_PInstance
		SET Updated = SysDate,
			IsProcessing = 'N',
			Result = 0,				-- failure
			ErrorMsg = ResultStr
		WHERE	AD_PInstance_ID=PInstance_ID;
		--COMMIT;
		RETURN;

END M_PriceList_Create;
--end--



