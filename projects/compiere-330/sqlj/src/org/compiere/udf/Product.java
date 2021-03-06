/******************************************************************************
 * Product: Compiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 3600 Bridge Parkway #102, Redwood City, CA 94065, USA      *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.udf;

import java.math.*;
import java.sql.*;

import COM.ibm.db2.app.*;


/**
 *	SQLJ Product related Functions
 *	
 *  @author Jorg Janke
 *  @version $Id: Product.java,v 1.3 2006/07/30 00:59:07 jjanke Exp $
 */
public class Product extends UDF
{
	/**
	 * 	Get Product Attribute Instance Name.
	 * 	Previously:  M_Attribute_Name - Now: productAttribute
	 * 	Test:
	 	    SELECT M_Attribute_Name (M_AttributeSetInstance_ID) 
		    FROM M_InOutLine WHERE M_AttributeSetInstance_ID > 0
		    --
		    SELECT p.Name
		    FROM C_InvoiceLine il LEFT OUTER JOIN M_Product p ON (il.M_Product_ID=p.M_Product_ID);
		    SELECT p.Name || M_Attribute_Name (il.M_AttributeSetInstance_ID) 
		    FROM C_InvoiceLine il LEFT OUTER JOIN M_Product p ON (il.M_Product_ID=p.M_Product_ID);
	 *	@param p_M_AttributeSetInstance_ID instance
	 *	@return Name or ""
	 *	@throws SQLException
	 */
	public static String attributeName (int p_M_AttributeSetInstance_ID)
		throws SQLException
	{
		if (p_M_AttributeSetInstance_ID == 0)
			return "";
		//
		StringBuffer sb = new StringBuffer();
		//	Get Base Info
		String sql = "SELECT asi.Lot, asi.SerNo, asi.GuaranteeDate "
			+ "FROM M_AttributeSetInstance asi "
			+ "WHERE asi.M_AttributeSetInstance_ID=?";
		PreparedStatement pstmt = Compiere.getInstance().prepareStatement(sql);
		pstmt.setInt(1, p_M_AttributeSetInstance_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
		{
			String lot = rs.getString(1);
			if (lot != null && lot.length() > 0)
				sb.append(lot).append(" ");
			String serNo = rs.getString(2);
			if (serNo != null && serNo.length() > 0)
				sb.append("#").append(serNo).append(" ");
			Date guarantee = rs.getDate(3);
			if (guarantee != null)
				sb.append(guarantee).append(" ");
		}
		rs.close();
		pstmt.close();

		//	Get Instance Info
		sql = "SELECT ai.Value, a.Name "
			+ "FROM M_AttributeInstance ai"
			+ " INNER JOIN M_Attribute a ON (ai.M_Attribute_ID=a.M_Attribute_ID AND a.IsInstanceAttribute='Y') "
			+ "WHERE ai.M_AttributeSetInstance_ID=?";
		pstmt = Compiere.getInstance().prepareStatement(sql);
		pstmt.setInt(1, p_M_AttributeSetInstance_ID);
		rs = pstmt.executeQuery();
		while (rs.next())
		{
			sb.append(rs.getString(1))					//	value
				.append(":").append(rs.getString(2))	//	name
				.append(" ");
		}
		rs.close();
		pstmt.close();
		
		if (sb.length() == 0)
			return "";
		sb.insert(0, " (");
		sb.append(")");
		return sb.toString();
	}	//	getAttributeName

	
	/**************************************************************************
	 * 	Get BOM Price Limit
	 * 	Previously:  BOM_PriceLimit - Now: bomPriceLimit
	 *	@param p_M_Product_ID
	 *	@param p_M_PriceList_Version_ID
	 *	@return Price Limit
	 *	@throws SQLException
	 */
	public static double  bomPriceLimit (int p_M_Product_ID, int p_M_PriceList_Version_ID) 
		throws SQLException
	{
		return bomPrice(p_M_Product_ID, p_M_PriceList_Version_ID, "PriceLimit");
	}	//	bomPriceLimit
	
	/**
	 * 	Get BOM Price List
	 * 	Previously:  BOM_PriceList - Now: bomPriceList
	 *	@param p_M_Product_ID
	 *	@param p_M_PriceList_Version_ID
	 *	@return Price List
	 *	@throws SQLException
	 */
	public static double  bomPriceList (int p_M_Product_ID, int p_M_PriceList_Version_ID) 
		throws SQLException
	{
		return bomPrice(p_M_Product_ID, p_M_PriceList_Version_ID, "PriceList");
	}	//	bomPriceList
	
	/**
	 * 	Get BOM Price Std
	 * 	Previously:  BOM_PriceStd - Now: bomPriceStd
	 *	@param p_M_Product_ID
	 *	@param p_M_PriceList_Version_ID
	 *	@return Price Std
	 *	@throws SQLException
	 */
	public static double  bomPriceStd (int p_M_Product_ID, int p_M_PriceList_Version_ID) 
		throws SQLException
	{
		return bomPrice(p_M_Product_ID, p_M_PriceList_Version_ID, "PriceStd");
	}	//	bomPriceStd

	/**
	 * 	Get BOM Price
	 *	@param p_M_Product_ID
	 *	@param p_M_PriceList_Version_ID
	 *	@param p_what variable name
	 *	@return Price
	 *	@throws SQLException
	 */
	static double  bomPrice (int p_M_Product_ID, int p_M_PriceList_Version_ID, String p_what) 
		throws SQLException
	{
		BigDecimal price = null;
		//	Try to get price from PriceList directly
		String sql = "SELECT " + p_what
			+ " FROM M_ProductPrice "
			+ "WHERE M_PriceList_Version_ID=? AND M_Product_ID=?";
		PreparedStatement pstmt = Compiere.getInstance().prepareStatement(sql);
		pstmt.setInt(1, p_M_PriceList_Version_ID);
		pstmt.setInt(2, p_M_Product_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
			price = rs.getBigDecimal(1);
		rs.close();
		pstmt.close();
		//	Loop through BOM
		if (price == null || price.signum() == 0)
		{
			price = Compiere.ZERO;
			sql = "SELECT bp.M_ProductBOM_ID, bp.BOMQty "
				+ "FROM M_BOMProduct bp, M_BOM b, M_Product p "
				+ "WHERE p.M_Product_ID=? "
				+ "AND p.IsBOM = 'Y' AND p.IsVerified = 'Y' "
				+ "AND bp.M_BOM_ID = b.M_BOM_ID "
				+ "AND bp.IsActive = 'Y' "
				+ "AND b.M_Product_ID = p.M_Product_ID "
				+ "AND b.BOMType = 'A' AND b.BOMUse = 'A' AND b.IsActive = 'Y' ";
			pstmt = Compiere.getInstance().prepareStatement(sql);
			pstmt.setInt(1, p_M_Product_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				int M_ProductBOM_ID = rs.getInt(1); //not null col
				BigDecimal qty = rs.getBigDecimal(2);
				BigDecimal productPrice = BigDecimal.valueOf(bomPrice(M_ProductBOM_ID, p_M_PriceList_Version_ID, p_what));
				productPrice = productPrice.multiply(qty);
				price = price.add(productPrice);
			}
			rs.close();
			pstmt.close();
		}
		return price.doubleValue();//jz
	}	//	bomPrice

	
	/**************************************************************************
	 * 	Get BOM Quantity Available 
	 * 	Previously:  BOM_Qty_Available - Now: bomQtyAvailable
	 *	@param p_M_Product_ID product
	 *	@param p_M_Warehouse_ID warehouse
	 *	@param p_M_Locator_ID locator
	 *	@return Quantity Available
	 *	@throws SQLException
	 */
	public static double  bomQtyAvailable (int p_M_Product_ID, 
		int p_M_Warehouse_ID, int p_M_Locator_ID) 
		throws SQLException
	{
		return bomQty(p_M_Product_ID, p_M_Warehouse_ID, p_M_Locator_ID, "QtyOnHand", "Y")
			-bomQty(p_M_Product_ID, p_M_Warehouse_ID, p_M_Locator_ID, "QtyReserved", "Y");
	}	//	bomQtyAvailable
	
	/**
	 * 	Get BOM Quantity OnHand 
	 * 	Previously:  BOM_Qty_OnHand - Now: bomQtyOnHand
	 *	@param p_M_Product_ID product
	 *	@param p_M_Warehouse_ID warehouse
	 *	@param p_M_Locator_ID locator
	 *	@return Quantity Available
	 *	@throws SQLException
	 */
	public static double  bomQtyOnHand (int p_M_Product_ID, 
		int p_M_Warehouse_ID, int p_M_Locator_ID) 
		throws SQLException
	{
		return bomQty(p_M_Product_ID, p_M_Warehouse_ID, p_M_Locator_ID, "QtyOnHand", "N");
	}	//	bomQtyOnHand
	
	/**
	 * 	Get BOM Quantity Ordered 
	 * 	Previously:  BOM_Qty_Ordered - Now: bomQtyOrdered
	 *	@param p_M_Product_ID product
	 *	@param p_M_Warehouse_ID warehouse
	 *	@param p_M_Locator_ID locator
	 *	@return Quantity Ordered
	 *	@throws SQLException
	 */
	public static double  bomQtyOrdered (int p_M_Product_ID, 
		int p_M_Warehouse_ID, int p_M_Locator_ID) 
		throws SQLException
	{
		return bomQty(p_M_Product_ID, p_M_Warehouse_ID, p_M_Locator_ID, "QtyOrdered", "N");
	}	//	bomQtyOrdered
	
	/**
	 * 	Get BOM Quantity Reserved 
	 * 	Previously:  BOM_Qty_Reserved - Now: bomQtyReserved
	 *	@param p_M_Product_ID product
	 *	@param p_M_Warehouse_ID warehouse
	 *	@param p_M_Locator_ID locator
	 *	@return Qyantity Reserved
	 *	@throws SQLException
	 */
	public static double  bomQtyReserved (int p_M_Product_ID, 
		int p_M_Warehouse_ID, int p_M_Locator_ID) 
		throws SQLException
	{
		return bomQty(p_M_Product_ID, p_M_Warehouse_ID, p_M_Locator_ID, "QtyReserved", "N");
	}	//	bomQtyReserved
	
	/**
	 * 	Get BOM Quantity
	 *	@param p_M_Product_ID product
	 *	@param p_M_Warehouse_ID warehouse
	 *	@param p_M_Locator_ID locator
	 *	@param p_what variable name
	 *	@return Quantity
	 *	@throws SQLException
	 */
	static double  bomQty (int p_M_Product_ID, 
		int p_M_Warehouse_ID, int p_M_Locator_ID, String p_what, String p_CheckATP) 
		throws SQLException
	{
		//	Check Parameters
		int M_Warehouse_ID = p_M_Warehouse_ID;
		if (M_Warehouse_ID == 0)
		{
			if (p_M_Locator_ID == 0)
				return 0;//Compiere.ZERO;
			else
			{
				String sql = "SELECT M_Warehouse_ID "
					+ "FROM M_Locator "
					+ "WHERE M_Locator_ID=?";
				M_Warehouse_ID = Compiere.getInstance().getSQLValue(sql, p_M_Locator_ID);
			}
		}
		if (M_Warehouse_ID == 0)
			return 0;//Compiere.ZERO;
		
		//	Check, if product exists and if it is stocked
		boolean isBOM = false;
		String ProductType = null;
		boolean isStocked = false;
		boolean isVerified = false;
		String sql = "SELECT IsBOM, ProductType, IsStocked, IsVerified "
			+ "FROM M_Product "
			+ "WHERE M_Product_ID=?";
		PreparedStatement pstmt = Compiere.getInstance().prepareStatement(sql);
		pstmt.setInt(1, p_M_Product_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
		{
			isBOM = "Y".equals(rs.getString(1));
			ProductType = rs.getString(2);
			isStocked = "Y".equals(rs.getString(3));
			isVerified = "Y".equals(rs.getString(4));
		}
		rs.close();
		pstmt.close();
		//	No Product
		if (ProductType == null)
			return 0; //Compiere.ZERO;
		//	Unlimited capacity if no item
		if (!isBOM && (!ProductType.equals("I") || !isStocked))
			return 99999.0;//UNLIMITED;
		//	Get Qty
		if (isStocked)
			return getStorageQty(p_M_Product_ID, M_Warehouse_ID, p_M_Locator_ID, p_what, p_CheckATP);
		if (!isVerified)
			return 0;
		
		//	Go through BOM
		BigDecimal quantity = UNLIMITED;
		BigDecimal productQuantity = null;
		sql = "SELECT bp.M_ProductBOM_ID, bp.BOMQty, p.IsBOM, p.IsStocked, p.ProductType, p.IsVerified "
			+ "FROM M_BOMProduct bp, M_BOM b, M_Product p "
			+ "WHERE bp.M_ProductBOM_ID=p.M_Product_ID "
			+ "AND bp.M_BOM_ID = b.M_BOM_ID "
			+ "AND bp.IsActive = 'Y' "
			+ "AND b.M_Product_ID=? "
			+ "AND b.BOMType = 'A' AND b.BOMUse = 'A' AND b.IsActive = 'Y' ";
		pstmt = Compiere.getInstance().prepareStatement(sql);
		pstmt.setInt(1, p_M_Product_ID);
		rs = pstmt.executeQuery();
		while (rs.next())
		{
			int M_ProductBOM_ID = rs.getInt(1);
			BigDecimal bomQty = rs.getBigDecimal(2);
			isBOM = "Y".equals(rs.getString(3));
			isStocked = "Y".equals(rs.getString(4)); 
			ProductType = rs.getString(5);
			isVerified = "Y".equals(rs.getString(6));
			
			//	Stocked Items "leaf node"
			if (ProductType.equals("I") && isStocked)
			{
				//	Get ProductQty
				productQuantity = BigDecimal.valueOf(getStorageQty(M_ProductBOM_ID, M_Warehouse_ID, p_M_Locator_ID, p_what, p_CheckATP));
				//	Get Rounding Precision
				int uomPrecision = getUOMPrecision(M_ProductBOM_ID);
				//	How much can we make with this product
				productQuantity = productQuantity.setScale(uomPrecision)
					.divide(bomQty, uomPrecision, BigDecimal.ROUND_HALF_UP);
				//	How much can we make overall
				if (productQuantity.compareTo(quantity) < 0)
					quantity = productQuantity;
			}
			else if (isBOM && isVerified)	//	Another BOM
			{
				productQuantity = BigDecimal.valueOf(bomQty (M_ProductBOM_ID, M_Warehouse_ID, p_M_Locator_ID, p_what, p_CheckATP));
				//	How much can we make overall
				if (productQuantity.compareTo(quantity) < 0)
					quantity = productQuantity;
			}
		}
		rs.close();
		pstmt.close();
		
		if (quantity.signum() != 0)
		{
			int uomPrecision = getUOMPrecision(p_M_Product_ID);
			return quantity.setScale(uomPrecision, BigDecimal.ROUND_HALF_UP).doubleValue();//jz
		}
		return 0; //Compiere.ZERO;
	}	//	bomQtyOnHand
	
	/** Unlimited Quantity			*/
	private static final BigDecimal UNLIMITED = new BigDecimal(99999.0);
	
	/**
	 * 	Get Storage Qty
	 *	@param p_M_Product_ID product
	 *	@param M_Warehouse_ID warehouse
	 *	@param p_M_Locator_ID locator
	 *	@param p_what variable name
	 *	@return quantity or zero
	 *	@throws SQLException
	 */
	static double  getStorageQty (int p_M_Product_ID, 
		int M_Warehouse_ID, int p_M_Locator_ID, String p_what, String p_CheckATP)
		throws SQLException
	{
		BigDecimal quantity = null;
		String sql = "SELECT SUM(" + p_what + ") "
			+ "FROM M_Storage s "
			+ "WHERE M_Product_ID=?";
		if (p_M_Locator_ID != 0)
			sql += " AND s.M_Locator_ID=?";
		else
		{
			sql += " AND EXISTS (SELECT * FROM M_Locator l WHERE s.M_Locator_ID=l.M_Locator_ID";
			if ("Y".equals(p_CheckATP))
				sql += " AND l.IsAvailableToPromise='Y'";
			sql += " AND l.M_Warehouse_ID=?)";
		}
		PreparedStatement pstmt = Compiere.getInstance().prepareStatement(sql);
		pstmt.setInt(1, p_M_Product_ID);
		if (p_M_Locator_ID != 0)
			pstmt.setInt(2, p_M_Locator_ID);
		else
			pstmt.setInt(2, M_Warehouse_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
			quantity = rs.getBigDecimal(1);
		rs.close();
		pstmt.close();
		//	Not found
		if (quantity == null)
			return 0;//Compiere.ZERO;
		return quantity.doubleValue();
	}	//	getStorageQty
	
	/**
	 * 	Get UOM Precision for Product
	 *	@param p_M_Product_ID product
	 *	@return precision or 0
	 *	@throws SQLException
	 */
	static int getUOMPrecision (int p_M_Product_ID) throws SQLException
	{
		int precision = 0;
		String sql = "SELECT u.StdPrecision "
			+ "FROM C_UOM u"
			+ " INNER JOIN M_Product p ON (u.C_UOM_ID=p.C_UOM_ID) "
			+ "WHERE p.M_Product_ID=?";
		PreparedStatement pstmt = Compiere.getInstance().prepareStatement(sql);
		pstmt.setInt(1, p_M_Product_ID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next())
			precision = rs.getInt(1);
		rs.close();
		pstmt.close();
		return precision;
	}	//	getStdPrecision

	/**
	 * 	Test
	 *	@param args
	 *
	public static void main (String[] args)
	{
		
		try
		{
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			Compiere.s_type = Compiere.TYPE_ORACLE;
			Compiere.s_url = "jdbc:oracle:thin:@//dev1:1521/dev1.compiere.org";
			Compiere.s_uid = "compiere";
			Compiere.s_pwd = "compiere";
	//		System.out.println(Product.bomQtyOnHand(p_M_Product_ID, 0, p_M_Locator_ID));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}	//	main	/* */

}	//	Product
