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
/** Generated Model for M_DemandDetail
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_M_DemandDetail extends PO
{
    /** Standard Constructor
    @param ctx context
    @param M_DemandDetail_ID id
    @param trx transaction
    */
    public X_M_DemandDetail (Ctx ctx, int M_DemandDetail_ID, Trx trx)
    {
        super (ctx, M_DemandDetail_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (M_DemandDetail_ID == 0)
        {
            setM_DemandDetail_ID (0);
            setM_DemandLine_ID (0);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_M_DemandDetail (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=721 */
    public static final int Table_ID=721;
    
    /** TableName=M_DemandDetail */
    public static final String Table_Name="M_DemandDetail";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"M_DemandDetail");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Order Line.
    @param C_OrderLine_ID Order Line */
    public void setC_OrderLine_ID (int C_OrderLine_ID)
    {
        if (C_OrderLine_ID <= 0) set_Value ("C_OrderLine_ID", null);
        else
        set_Value ("C_OrderLine_ID", Integer.valueOf(C_OrderLine_ID));
        
    }
    
    /** Get Order Line.
    @return Order Line */
    public int getC_OrderLine_ID() 
    {
        return get_ValueAsInt("C_OrderLine_ID");
        
    }
    
    /** Set Demand Detail.
    @param M_DemandDetail_ID Material Demand Line Source Detail */
    public void setM_DemandDetail_ID (int M_DemandDetail_ID)
    {
        if (M_DemandDetail_ID < 1) throw new IllegalArgumentException ("M_DemandDetail_ID is mandatory.");
        set_ValueNoCheck ("M_DemandDetail_ID", Integer.valueOf(M_DemandDetail_ID));
        
    }
    
    /** Get Demand Detail.
    @return Material Demand Line Source Detail */
    public int getM_DemandDetail_ID() 
    {
        return get_ValueAsInt("M_DemandDetail_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getM_DemandDetail_ID()));
        
    }
    
    /** Set Demand Line.
    @param M_DemandLine_ID Material Demand Line */
    public void setM_DemandLine_ID (int M_DemandLine_ID)
    {
        if (M_DemandLine_ID < 1) throw new IllegalArgumentException ("M_DemandLine_ID is mandatory.");
        set_ValueNoCheck ("M_DemandLine_ID", Integer.valueOf(M_DemandLine_ID));
        
    }
    
    /** Get Demand Line.
    @return Material Demand Line */
    public int getM_DemandLine_ID() 
    {
        return get_ValueAsInt("M_DemandLine_ID");
        
    }
    
    /** Set Forecast Line.
    @param M_ForecastLine_ID Forecast Line */
    public void setM_ForecastLine_ID (int M_ForecastLine_ID)
    {
        if (M_ForecastLine_ID <= 0) set_Value ("M_ForecastLine_ID", null);
        else
        set_Value ("M_ForecastLine_ID", Integer.valueOf(M_ForecastLine_ID));
        
    }
    
    /** Get Forecast Line.
    @return Forecast Line */
    public int getM_ForecastLine_ID() 
    {
        return get_ValueAsInt("M_ForecastLine_ID");
        
    }
    
    /** Set Requisition Line.
    @param M_RequisitionLine_ID Material Requisition Line */
    public void setM_RequisitionLine_ID (int M_RequisitionLine_ID)
    {
        if (M_RequisitionLine_ID <= 0) set_Value ("M_RequisitionLine_ID", null);
        else
        set_Value ("M_RequisitionLine_ID", Integer.valueOf(M_RequisitionLine_ID));
        
    }
    
    /** Get Requisition Line.
    @return Material Requisition Line */
    public int getM_RequisitionLine_ID() 
    {
        return get_ValueAsInt("M_RequisitionLine_ID");
        
    }
    
    
}
