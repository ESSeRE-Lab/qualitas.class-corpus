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
/** Generated Model for CM_ChatEntry
 *  @author Jorg Janke (generated) 
 *  @version Release 3.2.2_Dev - $Id$ */
public class X_CM_ChatEntry extends PO
{
    /** Standard Constructor
    @param ctx context
    @param CM_ChatEntry_ID id
    @param trx transaction
    */
    public X_CM_ChatEntry (Ctx ctx, int CM_ChatEntry_ID, Trx trx)
    {
        super (ctx, CM_ChatEntry_ID, trx);
        
        /* The following are the mandatory fields for this object.
        
        if (CM_ChatEntry_ID == 0)
        {
            setCM_ChatEntry_ID (0);
            setCM_Chat_ID (0);
            setChatEntryType (null);	// N
            setConfidentialType (null);
            
        }
        */
        
    }
    /** Load Constructor 
    @param ctx context
    @param rs result set 
    @param trx transaction
    */
    public X_CM_ChatEntry (Ctx ctx, ResultSet rs, Trx trx)
    {
        super (ctx, rs, trx);
        
    }
    /** Serial Version No */
    private static final long serialVersionUID = 27509345926789L;
    /** Last Updated Timestamp 2008-11-20 14:36:50.0 */
    public static final long updatedMS = 1227220610000L;
    /** AD_Table_ID=877 */
    public static final int Table_ID=877;
    
    /** TableName=CM_ChatEntry */
    public static final String Table_Name="CM_ChatEntry";
    
    protected static KeyNamePair Model = new KeyNamePair(Table_ID,"CM_ChatEntry");
    
    /**
     *  Get AD Table ID.
     *  @return AD_Table_ID
     */
    @Override public int get_Table_ID()
    {
        return Table_ID;
        
    }
    /** Set User/Contact.
    @param AD_User_ID User within the system - Internal or Business Partner Contact */
    public void setAD_User_ID (int AD_User_ID)
    {
        if (AD_User_ID <= 0) set_Value ("AD_User_ID", null);
        else
        set_Value ("AD_User_ID", Integer.valueOf(AD_User_ID));
        
    }
    
    /** Get User/Contact.
    @return User within the system - Internal or Business Partner Contact */
    public int getAD_User_ID() 
    {
        return get_ValueAsInt("AD_User_ID");
        
    }
    
    
    /** CM_ChatEntryGrandParent_ID AD_Reference_ID=399 */
    public static final int CM_CHATENTRYGRANDPARENT_ID_AD_Reference_ID=399;
    /** Set Chat Entry Grandparent.
    @param CM_ChatEntryGrandParent_ID Link to Grand Parent (root level) */
    public void setCM_ChatEntryGrandParent_ID (int CM_ChatEntryGrandParent_ID)
    {
        if (CM_ChatEntryGrandParent_ID <= 0) set_Value ("CM_ChatEntryGrandParent_ID", null);
        else
        set_Value ("CM_ChatEntryGrandParent_ID", Integer.valueOf(CM_ChatEntryGrandParent_ID));
        
    }
    
    /** Get Chat Entry Grandparent.
    @return Link to Grand Parent (root level) */
    public int getCM_ChatEntryGrandParent_ID() 
    {
        return get_ValueAsInt("CM_ChatEntryGrandParent_ID");
        
    }
    
    
    /** CM_ChatEntryParent_ID AD_Reference_ID=399 */
    public static final int CM_CHATENTRYPARENT_ID_AD_Reference_ID=399;
    /** Set Chat Entry Parent.
    @param CM_ChatEntryParent_ID Link to direct Parent */
    public void setCM_ChatEntryParent_ID (int CM_ChatEntryParent_ID)
    {
        if (CM_ChatEntryParent_ID <= 0) set_Value ("CM_ChatEntryParent_ID", null);
        else
        set_Value ("CM_ChatEntryParent_ID", Integer.valueOf(CM_ChatEntryParent_ID));
        
    }
    
    /** Get Chat Entry Parent.
    @return Link to direct Parent */
    public int getCM_ChatEntryParent_ID() 
    {
        return get_ValueAsInt("CM_ChatEntryParent_ID");
        
    }
    
    /** Set Chat Entry.
    @param CM_ChatEntry_ID Individual Chat / Discussion Entry */
    public void setCM_ChatEntry_ID (int CM_ChatEntry_ID)
    {
        if (CM_ChatEntry_ID < 1) throw new IllegalArgumentException ("CM_ChatEntry_ID is mandatory.");
        set_ValueNoCheck ("CM_ChatEntry_ID", Integer.valueOf(CM_ChatEntry_ID));
        
    }
    
    /** Get Chat Entry.
    @return Individual Chat / Discussion Entry */
    public int getCM_ChatEntry_ID() 
    {
        return get_ValueAsInt("CM_ChatEntry_ID");
        
    }
    
    /** Get Record ID/ColumnName
    @return ID/ColumnName pair */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getCM_ChatEntry_ID()));
        
    }
    
    /** Set Chat.
    @param CM_Chat_ID Chat or discussion thread */
    public void setCM_Chat_ID (int CM_Chat_ID)
    {
        if (CM_Chat_ID < 1) throw new IllegalArgumentException ("CM_Chat_ID is mandatory.");
        set_ValueNoCheck ("CM_Chat_ID", Integer.valueOf(CM_Chat_ID));
        
    }
    
    /** Get Chat.
    @return Chat or discussion thread */
    public int getCM_Chat_ID() 
    {
        return get_ValueAsInt("CM_Chat_ID");
        
    }
    
    /** Set Character Data.
    @param CharacterData Long Character Field */
    public void setCharacterData (String CharacterData)
    {
        set_ValueNoCheck ("CharacterData", CharacterData);
        
    }
    
    /** Get Character Data.
    @return Long Character Field */
    public String getCharacterData() 
    {
        return (String)get_Value("CharacterData");
        
    }
    
    
    /** ChatEntryType AD_Reference_ID=398 */
    public static final int CHATENTRYTYPE_AD_Reference_ID=398;
    /** Forum (threaded) = F */
    public static final String CHATENTRYTYPE_ForumThreaded = X_Ref_CM_Chat_EntryType.FORUM_THREADED.getValue();
    /** Note (flat) = N */
    public static final String CHATENTRYTYPE_NoteFlat = X_Ref_CM_Chat_EntryType.NOTE_FLAT.getValue();
    /** Wiki = W */
    public static final String CHATENTRYTYPE_Wiki = X_Ref_CM_Chat_EntryType.WIKI.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isChatEntryTypeValid(String test)
    {
         return X_Ref_CM_Chat_EntryType.isValid(test);
         
    }
    /** Set Chat Entry Type.
    @param ChatEntryType Type of Chat/Forum Entry */
    public void setChatEntryType (String ChatEntryType)
    {
        if (ChatEntryType == null) throw new IllegalArgumentException ("ChatEntryType is mandatory");
        if (!isChatEntryTypeValid(ChatEntryType))
        throw new IllegalArgumentException ("ChatEntryType Invalid value - " + ChatEntryType + " - Reference_ID=398 - F - N - W");
        set_Value ("ChatEntryType", ChatEntryType);
        
    }
    
    /** Get Chat Entry Type.
    @return Type of Chat/Forum Entry */
    public String getChatEntryType() 
    {
        return (String)get_Value("ChatEntryType");
        
    }
    
    
    /** ConfidentialType AD_Reference_ID=340 */
    public static final int CONFIDENTIALTYPE_AD_Reference_ID=340;
    /** Public Information = A */
    public static final String CONFIDENTIALTYPE_PublicInformation = X_Ref_R_Request_Confidential.PUBLIC_INFORMATION.getValue();
    /** Partner Confidential = C */
    public static final String CONFIDENTIALTYPE_PartnerConfidential = X_Ref_R_Request_Confidential.PARTNER_CONFIDENTIAL.getValue();
    /** Internal = I */
    public static final String CONFIDENTIALTYPE_Internal = X_Ref_R_Request_Confidential.INTERNAL.getValue();
    /** Private Information = P */
    public static final String CONFIDENTIALTYPE_PrivateInformation = X_Ref_R_Request_Confidential.PRIVATE_INFORMATION.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isConfidentialTypeValid(String test)
    {
         return X_Ref_R_Request_Confidential.isValid(test);
         
    }
    /** Set Confidentiality.
    @param ConfidentialType Type of Confidentiality */
    public void setConfidentialType (String ConfidentialType)
    {
        if (ConfidentialType == null) throw new IllegalArgumentException ("ConfidentialType is mandatory");
        if (!isConfidentialTypeValid(ConfidentialType))
        throw new IllegalArgumentException ("ConfidentialType Invalid value - " + ConfidentialType + " - Reference_ID=340 - A - C - I - P");
        set_Value ("ConfidentialType", ConfidentialType);
        
    }
    
    /** Get Confidentiality.
    @return Type of Confidentiality */
    public String getConfidentialType() 
    {
        return (String)get_Value("ConfidentialType");
        
    }
    
    
    /** ModeratorStatus AD_Reference_ID=396 */
    public static final int MODERATORSTATUS_AD_Reference_ID=396;
    /** Not Displayed = N */
    public static final String MODERATORSTATUS_NotDisplayed = X_Ref_CM_ChatEntry_ModeratorStatus.NOT_DISPLAYED.getValue();
    /** Published = P */
    public static final String MODERATORSTATUS_Published = X_Ref_CM_ChatEntry_ModeratorStatus.PUBLISHED.getValue();
    /** To be reviewed = R */
    public static final String MODERATORSTATUS_ToBeReviewed = X_Ref_CM_ChatEntry_ModeratorStatus.TO_BE_REVIEWED.getValue();
    /** Suspicious = S */
    public static final String MODERATORSTATUS_Suspicious = X_Ref_CM_ChatEntry_ModeratorStatus.SUSPICIOUS.getValue();
    /** Is test a valid value.
    @param test testvalue
    @return true if valid **/
    public static boolean isModeratorStatusValid(String test)
    {
         return X_Ref_CM_ChatEntry_ModeratorStatus.isValid(test);
         
    }
    /** Set Moderation Status.
    @param ModeratorStatus Status of Moderation */
    public void setModeratorStatus (String ModeratorStatus)
    {
        if (!isModeratorStatusValid(ModeratorStatus))
        throw new IllegalArgumentException ("ModeratorStatus Invalid value - " + ModeratorStatus + " - Reference_ID=396 - N - P - R - S");
        set_Value ("ModeratorStatus", ModeratorStatus);
        
    }
    
    /** Get Moderation Status.
    @return Status of Moderation */
    public String getModeratorStatus() 
    {
        return (String)get_Value("ModeratorStatus");
        
    }
    
    /** Set Subject.
    @param Subject Email Message Subject */
    public void setSubject (String Subject)
    {
        set_Value ("Subject", Subject);
        
    }
    
    /** Get Subject.
    @return Email Message Subject */
    public String getSubject() 
    {
        return (String)get_Value("Subject");
        
    }
    
    
}
