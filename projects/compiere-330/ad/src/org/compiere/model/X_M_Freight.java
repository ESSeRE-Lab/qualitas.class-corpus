/******************************************************************************
 * Product: Compiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2008 Compiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us at *
 * Compiere, Inc., 3600 Bridge Parkway #102, Redwood City, CA 94065, USA      *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

/** Generated Model - DO NOT CHANGE */
import java.sql.*;
import org.compiere.framework.*;
import org.compiere.util.*;
/** Generated Model for M_Freight
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_Freight extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_Freight_ID id
    @param trx transaction
    */
    public X_M_Freight (Ctx ctx, int M_Freight_ID, Trx trx)
    {
        super (ctx, M_Freight_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_Freight_ID == 0)
        {
            setC_Currency_ID (0);
            setFreightAmt (Env.ZERO);
            setM_FreightCategory_ID (0);
            setM_Freight_ID (0);
            setM_Shipper_ID (0);
            setValidFrom (new Timestamp(System.currentTimeMillis()));
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_Freight (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=596 */
    public static final int Table_ID=596;
    
    /** TableName=M_Freight */
    public static final String Table_Name="M_Freight";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_Freight");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Country.
    @param C_Country_ID Country */
    public void setC_Country_ID (int C_Country_ID)
    {
        if (C_Country_ID <= 0) set_Value ("C_Country_ID", null);
        else
        set_Value ("C_Country_ID", Integer.valueOf(C_Country_ID));
        
    }
    
    /** Get Country.
    @return Country */
    public int getC_Country_ID() 
    {
        return get_ValueAsInt("C_Country_ID");
        
    }
    
    /** Set Currency.
    @param C_Currency_ID The Currency for this record */
    public void setC_Currency_ID (int C_Currency_ID)
    {
        if (C_Currency_ID < 1) throw new IllegalArgumentException ("C_Currency_ID is mandatory.");
        set_Value ("C_Currency_ID", Integer.valueOf(C_Currency_ID));
        
    }
    
    /** Get Currency.
    @return The Currency for this record */
    public int getC_Currency_ID() 
    {
        return get_ValueAsInt("C_Currency_ID");
        
    }
    
    /** Set Region.
    @param C_Region_ID Identifies a geographical Region */
    public void setC_Region_ID (int C_Region_ID)
    {
        if (C_Region_ID <= 0) set_Value ("C_Region_ID", null);
        else
        set_Value ("C_Region_ID", Integer.valueOf(C_Region_ID));
        
    }
    
    /** Get Region.
    @return Identifies a geographical Region */
    public int getC_Region_ID() 
    {
        return get_ValueAsInt("C_Region_ID");
        
    }
    
    /** Set Freight Amount.
    @param FreightAmt Freight Amount */
    public void setFreightAmt (java.math.BigDecimal FreightAmt)
    {
        if (FreightAmt == null) throw new IllegalArgumentException ("FreightAmt is mandatory.");
        set_Value ("FreightAmt", FreightAmt);
        
    }
    
    /** Get Freight Amount.
    @return Freight Amount */
    public java.math.BigDecimal getFreightAmt() 
    {
        return get_ValueAsBigDecimal("FreightAmt");
        
    }
    
    /** Set Freight Category.
    @param M_FreightCategory_ID Category of the Freight */
    public void setM_FreightCategory_ID (int M_FreightCategory_ID)
    {
        if (M_FreightCategory_ID < 1) throw new IllegalArgumentException ("M_FreightCategory_ID is mandatory.");
        set_Value ("M_FreightCategory_ID", Integer.valueOf(M_FreightCategory_ID));
        
    }
    
    /** Get Freight Category.
    @return Category of the Freight */
    public int getM_FreightCategory_ID() 
    {
        return get_ValueAsInt("M_FreightCategory_ID");
        
    }
    
    /** Set Freight.
    @param M_Freight_ID Freight Rate */
    public void setM_Freight_ID (int M_Freight_ID)
    {
        if (M_Freight_ID < 1) throw new IllegalArgumentException ("M_Freight_ID is mandatory.");
        set_ValueNoCheck ("M_Freight_ID", Integer.valueOf(M_Freight_ID));
        
    }
    
    /** Get Freight.
    @return Freight Rate */
    public int getM_Freight_ID() 
    {
        return get_ValueAsInt("M_Freight_ID");
        
    }
    
    /** Set Freight Carrier.
    @param M_Shipper_ID Method or manner of product delivery */
    public void setM_Shipper_ID (int M_Shipper_ID)
    {
        if (M_Shipper_ID < 1) throw new IllegalArgumentException ("M_Shipper_ID is mandatory.");
        set_ValueNoCheck ("M_Shipper_ID", Integer.valueOf(M_Shipper_ID));
        
    }
    
    /** Get Freight Carrier.
    @return Method or manner of product delivery */
    public int getM_Shipper_ID() 
    {
        return get_ValueAsInt("M_Shipper_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getM_Shipper_ID()));
        
    }
    
    
    /** To_Country_ID AD_Reference_ID=156 */
    public static final int TO_COUNTRY_ID_AD_Reference_ID=156;
    /** Set To.
    @param To_Country_ID Receiving Country */
    public void setTo_Country_ID (int To_Country_ID)
    {
        if (To_Country_ID <= 0) set_Value ("To_Country_ID", null);
        else
        set_Value ("To_Country_ID", Integer.valueOf(To_Country_ID));
        
    }
    
    /** Get To.
    @return Receiving Country */
    public int getTo_Country_ID() 
    {
        return get_ValueAsInt("To_Country_ID");
        
    }
    
    
    /** To_Region_ID AD_Reference_ID=157 */
    public static final int TO_REGION_ID_AD_Reference_ID=157;
    /** Set To.
    @param To_Region_ID Receiving Region */
    public void setTo_Region_ID (int To_Region_ID)
    {
        if (To_Region_ID <= 0) set_Value ("To_Region_ID", null);
        else
        set_Value ("To_Region_ID", Integer.valueOf(To_Region_ID));
        
    }
    
    /** Get To.
    @return Receiving Region */
    public int getTo_Region_ID() 
    {
        return get_ValueAsInt("To_Region_ID");
        
    }
    
    /** Set Valid from.
    @param ValidFrom Valid from including this date (first day) */
    public void setValidFrom (Timestamp ValidFrom)
    {
        if (ValidFrom == null) throw new IllegalArgumentException ("ValidFrom is mandatory.");
        set_Value ("ValidFrom", ValidFrom);
        
    }
    
    /** Get Valid from.
    @return Valid from including this date (first day) */
    public Timestamp getValidFrom() 
    {
        return (Timestamp)get_Value("ValidFrom");
        
    }
    
    
}
