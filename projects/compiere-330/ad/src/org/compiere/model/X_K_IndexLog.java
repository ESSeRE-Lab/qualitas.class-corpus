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
/** Generated Model for K_IndexLog
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_K_IndexLog extends PO
{
    /** Standard Constructor
    @param ctx context
    @param K_IndexLog_ID id
    @param trx transaction
    */
    public X_K_IndexLog (Ctx ctx, int K_IndexLog_ID, Trx trx)
    {
        super (ctx, K_IndexLog_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (K_IndexLog_ID == 0)
        {
            setIndexQuery (null);
            setIndexQueryResult (0);
            setK_IndexLog_ID (0);
            setQuerySource (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_K_IndexLog (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27495261242789L;
    /** Last Updated Timestamp 2008-06-10 15:12:06.0 */
    public static final long updatedMS = 1213135926000L;
    /** AD_Table_ID=899 */
    public static final int Table_ID=899;
    
    /** TableName=K_IndexLog */
    public static final String Table_Name="K_IndexLog";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"K_IndexLog");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set Index Query.
    @param IndexQuery Text Search Query */
    public void setIndexQuery (String IndexQuery)
    {
        if (IndexQuery == null) throw new IllegalArgumentException ("IndexQuery is mandatory.");
        set_ValueNoCheck ("IndexQuery", IndexQuery);
        
    }
    
    /** Get Index Query.
    @return Text Search Query */
    public String getIndexQuery() 
    {
        return (String)get_Value("IndexQuery");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getIndexQuery());
        
    }
    
    /** Set Query Result.
    @param IndexQueryResult Result of the text query */
    public void setIndexQueryResult (int IndexQueryResult)
    {
        set_ValueNoCheck ("IndexQueryResult", Integer.valueOf(IndexQueryResult));
        
    }
    
    /** Get Query Result.
    @return Result of the text query */
    public int getIndexQueryResult() 
    {
        return get_ValueAsInt("IndexQueryResult");
        
    }
    
    /** Set Index Log.
    @param K_IndexLog_ID Text search log */
    public void setK_IndexLog_ID (int K_IndexLog_ID)
    {
        if (K_IndexLog_ID < 1) throw new IllegalArgumentException ("K_IndexLog_ID is mandatory.");
        set_ValueNoCheck ("K_IndexLog_ID", Integer.valueOf(K_IndexLog_ID));
        
    }
    
    /** Get Index Log.
    @return Text search log */
    public int getK_IndexLog_ID() 
    {
        return get_ValueAsInt("K_IndexLog_ID");
        
    }
    
    
    /** QuerySource AD_Reference_ID=391 */
    public static final int QUERYSOURCE_AD_Reference_ID=391;
    /** Collaboration Management = C */
    public static final String QUERYSOURCE_CollaborationManagement = X_Ref_K_IndexLog_QuerySource.COLLABORATION_MANAGEMENT.getValue();
    /** HTML Client = H */
    public static final String QUERYSOURCE_HTMLClient = X_Ref_K_IndexLog_QuerySource.HTML_CLIENT.getValue();
    /** Java Client = J */
    public static final String QUERYSOURCE_JavaClient = X_Ref_K_IndexLog_QuerySource.JAVA_CLIENT.getValue();
    /** Self Service = W */
    public static final String QUERYSOURCE_SelfService = X_Ref_K_IndexLog_QuerySource.SELF_SERVICE.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isQuerySourceValid(String test)
    {
         return X_Ref_K_IndexLog_QuerySource.isValid(test);
         
    }
    /** Set Query Source.
    @param QuerySource Source of the Query */
    public void setQuerySource (String QuerySource)
    {
        if (QuerySource == null) throw new IllegalArgumentException ("QuerySource is mandatory");
        if (!isQuerySourceValid(QuerySource))
        throw new IllegalArgumentException ("QuerySource Invalid value - " + QuerySource + " - Reference_ID=391 - C - H - J - W");
        set_Value ("QuerySource", QuerySource);
        
    }
    
    /** Get Query Source.
    @return Source of the Query */
    public String getQuerySource() 
    {
        return (String)get_Value("QuerySource");
        
    }
    
    
}
