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
package org.compiere.process;

import java.math.*;
import java.sql.*;
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.util.*;

public class ImportRequest extends SvrProcess {

	/**	Client to be imported to		*/
	private int				m_AD_Client_ID = 0;
	/**	Organization to be imported to		*/
	private int				m_AD_Org_ID = 0;
	/**	Delete old Imported				*/
	private boolean			m_deleteOldImported = false;

	/**
	 *  Perrform process.
	 *  @return Message
	 *  @throws Exception
	 */
	@Override
	protected String doIt() throws Exception {

		StringBuffer sql = null;
		int no = 0;
		String clientCheck = " AND AD_Client_ID=" + m_AD_Client_ID;

		//	****	Prepare	****

		//	Delete Old Imported
		if (m_deleteOldImported)
		{
			sql = new StringBuffer ("DELETE FROM I_Request "
				  + "WHERE I_IsImported='Y'").append (clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			log.fine("Delete Old Impored =" + no);
		}

		//Set Client from Key
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET AD_Client_ID = (SELECT AD_Client_ID FROM AD_Client c WHERE c.Value = r.ClientValue), " 
				  +	" ClientName = (SELECT Name FROM AD_Client c WHERE c.Value = r.ClientValue), "
				  + " Updated = COALESCE (Updated, SysDate),"
				  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND AD_Client_ID is NULL"
				  + " AND ClientValue is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set client from key =" + no);

		//	Set Client from Name
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET AD_Client_ID = (SELECT AD_Client_ID FROM AD_Client c WHERE c.Name = r.ClientName), "
				  + " ClientValue = (SELECT Value FROM AD_Client c WHERE c.Name = r.ClientName),"
				  + " Updated = COALESCE (Updated, SysDate),"
				  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND AD_Client_ID is NULL"
				  + " AND ClientName is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set client from name =" + no);

		//Set Org from Key
		sql = new StringBuffer ("UPDATE I_Request r"
					  + " SET AD_Org_ID = (SELECT AD_Org_ID FROM AD_Org o WHERE o.Value = r.OrgValue), "
					  + " OrgName = (SELECT Name FROM AD_Org o WHERE o.Value = r.OrgValue), "
					  + " Updated = COALESCE (Updated, SysDate),"
					  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
					  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
					  + " AND (AD_Org_ID is NULL OR AD_Org_ID =0)"
					  + " AND OrgValue is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set org from key =" + no);

		//	Set Org from Name
		sql = new StringBuffer ("UPDATE I_Request r"
					  + " SET AD_Org_ID = (SELECT AD_Org_ID FROM AD_Org o WHERE o.Name = r.OrgName), "
					  + " OrgValue = (SELECT Value FROM AD_Org o WHERE o.Name = r.OrgName), "
					  + " Updated = COALESCE (Updated, SysDate),"
					  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
					  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
					  + " AND (AD_Org_ID is NULL OR AD_Org_ID =0)"
					  + " AND OrgName is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set Org from name =" + no);


		sql = new StringBuffer ("UPDATE I_Request r"
			  + " SET AD_Client_ID = COALESCE (AD_Client_ID,").append (m_AD_Client_ID).append ("),"
			  + " AD_Org_ID = COALESCE (AD_Org_ID,").append (m_AD_Org_ID).append ("),"
			  + " IsActive = COALESCE (IsActive, 'Y'),"
			  + " Created = COALESCE (Created, SysDate),"
			  + " CreatedBy = COALESCE (CreatedBy, 0),"
			  + " Updated = COALESCE (Updated, SysDate),"
			  + " UpdatedBy = COALESCE (UpdatedBy, 0),"
			  + " I_ErrorMsg = NULL,"
			  + " I_IsImported = 'N' "
			  + "WHERE I_IsImported<>'Y' OR I_IsImported IS NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Reset=" + no);

		String ts = DB.isPostgreSQL() ? 
				"COALESCE(I_ErrorMsg,'')"
				: "I_ErrorMsg";  //java bug, it could not be used directly
		sql = new StringBuffer ("UPDATE I_Request r "
				+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Org, '"
				+ "WHERE (AD_Org_ID IS NULL "
				+ " OR NOT EXISTS (SELECT * FROM AD_Org oo WHERE r.AD_Org_ID=oo.AD_Org_ID AND oo.IsSummary='N' AND oo.IsActive='Y'))"
				+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid Org=" + no);
		
		// Set Request Type
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET R_RequestType_ID = " 
				  + " (SELECT R_RequestType_ID FROM R_RequestType rt WHERE rt.Name = r.ReqTypeName)"
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_RequestType_ID is NULL"
				  + " AND r.ReqTypeName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set RequestType=" + no);

		// Error - Request Type not specified
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid RequestTypeName, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_RequestType_ID is NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid ReqType=" + no);
			
		// Set Group
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET R_Group_ID = " 
				  + " (SELECT R_Group_ID FROM R_Group g WHERE g.Name = r.GroupName)"
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_Group_ID is NULL"
				  + " AND r.GroupName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Group=" + no);
		
		// Error - Invalid Group
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid GroupName, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_Group_ID is NULL"
				  + " AND r.GroupName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid GroupName=" + no);
		
		// Set Category
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET R_Category_ID = " 
				  + " (SELECT R_Category_ID FROM R_Category c WHERE c.Name = r.CategoryName)"
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_Category_ID is NULL"
				  + " AND r.CategoryName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Category=" + no);

		// Error - Invalid Category
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid CategoryName, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_Category_ID is NULL"
				  + " AND r.CategoryName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid CategoryName=" + no);

		
		// Set Status
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET R_Status_ID = " 
				  + " (SELECT R_Status_ID FROM R_Status s, R_StatusCategory sc, R_RequestType t " 
				  +	" WHERE s.Name = r.StatusName and t.R_RequestType_ID = r.R_RequestType_ID and t.R_StatusCategory_ID = sc.R_StatusCategory_ID" 
				  + " AND s.R_StatusCategory_ID = sc.R_StatusCategory_ID)" 
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_Status_ID is NULL"
				  + " AND r.StatusName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Status=" + no);
		
		// Error - Invalid Status
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid StatusName, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_Status_ID is NULL"
				  + " AND r.StatusName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Status=" + no);

		
		// Set Resolution
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET R_Resolution_ID = " 
				  + " (SELECT R_Resolution_ID FROM R_Resolution rr " 
				  +	" WHERE rr.Name = r.ResolutionName )"  
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_Resolution_ID is NULL"
				  + " AND r.ResolutionName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Resolution=" + no);
		
		// Error - Invalid Resolution
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid ResolutionName, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_Resolution_ID is NULL"
				  + " AND r.ResolutionName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Resolution=" + no);

		// Error - Invalid Summary
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Summary, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.Summary is NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Summary=" + no);

		// Set BP from BPartnerKey
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_BPartner_ID = (SELECT C_BPartner_ID FROM C_BPartner b"
				  + " WHERE b.Value=r.BPartnerValue AND b.AD_Client_ID=r.AD_Client_ID ), " 
				  +	" BPartnerName=(SELECT Name FROM C_BPartner b"
				  + " WHERE b.Value=r.BPartnerValue AND b.AD_Client_ID=r.AD_Client_ID )"
				  + " WHERE C_BPartner_ID IS NULL AND BPartnerValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set BPartner from BPartnerKey=" + no);

		//	Set BP from BPartnerName
		sql = new StringBuffer ("UPDATE I_Request r"
			  + " SET C_BPartner_ID=(SELECT C_BPartner_ID FROM C_BPartner b"
			  + " WHERE b.Name=r.BpartnerName AND b.AD_Client_ID=r.AD_Client_ID ), "
			  + " BPartnerValue=(SELECT Value FROM C_BPartner b"
			  + " WHERE b.Name=r.BpartnerName AND b.AD_Client_ID=r.AD_Client_ID )"
			  + " WHERE C_BPartner_ID IS NULL AND BPartnerName IS NOT NULL"
			  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set BPartner from BPartnerName=" + no);

		// Error - Invalid BPartner
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Bpartner, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.C_BPartner_ID is NULL"
				  + " AND (r.BPartnerName is NOT NULL OR r.BPartnerValue IS NOT NULL)").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid BPartner=" + no);

		//	Set User from UserKey
		sql = new StringBuffer ("UPDATE I_Request r"
			  + " SET AD_User_ID =(SELECT AD_User_ID FROM AD_User u"
			  + " WHERE r.ContactValue=u.Value AND r.AD_Client_ID=u.AD_Client_ID AND u.C_Bpartner_ID = r.C_BPartner_ID), "
			  + " ContactName =(SELECT Name FROM AD_User u"
			  + " WHERE r.ContactValue=u.Value AND r.AD_Client_ID=u.AD_Client_ID AND u.C_Bpartner_ID = r.C_BPartner_ID) "
			  + " WHERE AD_User_ID IS NULL AND ContactValue IS NOT NULL"
			  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set User from UserKey=" + no);

		//	Set User from UserName
		sql = new StringBuffer ("UPDATE I_Request r"
			  + " SET AD_User_ID=(SELECT AD_User_ID FROM AD_User u"
			  + " WHERE r.ContactName=u.Name AND r.AD_Client_ID=u.AD_Client_ID AND u.C_Bpartner_ID = r.C_BPartner_ID ),"
			  + " ContactValue =(SELECT Value FROM AD_User u"
			  + " WHERE r.ContactName=u.Name AND r.AD_Client_ID=u.AD_Client_ID AND u.C_Bpartner_ID = r.C_BPartner_ID )"
			  + " WHERE AD_User_ID IS NULL AND ContactName IS NOT NULL"
			  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set User from UserName=" + no);

		// Error - Invalid User
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid User, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND ("
				  + " (r.AD_User_ID is NULL AND (r.ContactName is NOT NULL OR r.ContactValue IS NOT NULL))"
				  + " OR (r.AD_User_ID is NOT NULL AND NOT EXISTS"
				  + "(SELECT 1 FROM AD_USER u WHERE u.AD_User_ID = r.AD_User_ID "
				  + " AND r.AD_Client_ID=u.AD_Client_ID AND u.C_Bpartner_ID = r.C_BPartner_ID)))").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid User=" + no);

		//	Set SalesRep from SalesRepKey
		sql = new StringBuffer ("UPDATE I_Request r"
			  + " SET SalesRep_ID = (SELECT AD_User_ID FROM AD_User u"
			  + " WHERE r.SalesRepValue=u.Value AND r.AD_Client_ID=u.AD_Client_ID ), " 
			  + " SalesRepName = (SELECT name FROM AD_User u"
			  + " WHERE r.SalesRepValue=u.Value AND r.AD_Client_ID=u.AD_Client_ID ) "
			  + "WHERE SalesRep_ID IS NULL AND SalesRepValue IS NOT NULL"
			  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set SalesRep from SalesRepValue=" + no);

		//	Set Representative from RepresentativeName
		sql = new StringBuffer ("UPDATE I_Request r"
			  + " SET SalesRep_ID = (SELECT AD_User_ID FROM AD_User u"
			  + " WHERE r.SalesRepName=u.Name AND r.AD_Client_ID=u.AD_Client_ID ), "
			  + " SalesRepValue =(SELECT  value FROM AD_User u"
			  + " WHERE r.SalesRepName=u.Name AND r.AD_Client_ID=u.AD_Client_ID )"
			  + " WHERE SalesRep_ID IS NULL AND SalesRepName IS NOT NULL"
			  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set SalesRep from SalesRepName=" + no);


		// Error - Invalid SalesRep
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Representative, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.SalesRep_ID is NULL").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid SalesRep=" + no);

		// Set Table
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET AD_Table_ID = " 
				  + " (SELECT AD_Table_ID FROM AD_Table t " 
				  +	" WHERE t.Name = r.TableName )"  
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.AD_Table_ID is NULL"
				  + " AND r.TableName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Table=" + no);
		
		// Error - Invalid Table
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid TableName, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.AD_Table_ID is NULL"
				  + " AND r.TableName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Table=" + no);
		
		// Set Related Request
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET R_RequestRelated_ID = " 
				  + " (SELECT R_Request_ID FROM R_Request rr " 
				  +	" WHERE rr.DocumentNo = r.RequestRelatedDocNo )"  
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_RequestRelated_ID is NULL"
				  + " AND r.RequestRelatedDocNo is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Resolution=" + no);
		
		// Error - Invalid Related Request
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Rel, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_RequestRelated_ID is NULL"
				  + " AND r.RequestRelatedDocNo is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid RelatedRequest=" + no);

		//	Set Source from SourceKey
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET R_Source_ID = (SELECT R_Source_ID FROM R_Source s"
				  + " WHERE r.SourceValue=s.Value AND r.AD_Client_ID=s.AD_Client_ID ), "
				  + " SourceName = (SELECT name FROM R_Source s"
				  + " WHERE r.SourceValue=s.Value AND r.AD_Client_ID=s.AD_Client_ID )"
				  + " WHERE R_Source_ID IS NULL AND SourceValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Source from SourceValue =" + no);

		//	Set Source from SourceName
		sql = new StringBuffer ("UPDATE I_Request r"
			  + " SET R_Source_ID=(SELECT R_Source_ID FROM R_Source s"
			  + " WHERE r.SourceName=s.Name AND r.AD_Client_ID=s.AD_Client_ID ), "
			  + " SourceValue = (SELECT value FROM R_Source s"
			  + " WHERE r.SourceName=s.Name AND r.AD_Client_ID=s.AD_Client_ID ) "
			  + " WHERE R_Source_ID IS NULL AND SourceName IS NOT NULL"
			  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Source from SourceName=" + no);


		// Error - Invalid Source
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Source, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.R_Source_ID IS NULL"
				  + " AND (r.SourceName IS NOT NULL OR r.SourceValue is NOT NULL)").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Source=" + no);

		// Set Role
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET AD_Role_ID = " 
				  + " (SELECT AD_Role_ID FROM AD_Role ar " 
				  +	" WHERE ar.Name = r.RoleName )"  
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.AD_Role_ID is NULL"
				  + " AND r.RoleName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Role=" + no);
		
		// Error - Invalid Role
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid RoleName, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.AD_Role_ID is NULL"
				  + " AND r.RoleName is NOT NULL").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Role=" + no);

		//	Set ProductSpent from ProductSpentKey
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET M_ProductSpent_ID=(SELECT M_Product_ID FROM M_Product m"
				  + " WHERE r.ProductSpentValue=m.Value AND r.AD_Client_ID=m.AD_Client_ID ), "
				  + " ProductSpentName =(SELECT Name FROM M_Product m"
				  + " WHERE r.ProductSpentValue=m.Value AND r.AD_Client_ID=m.AD_Client_ID ) "
				  + " WHERE M_ProductSpent_ID IS NULL AND ProductSpentValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set ProductSpent from ProductSpentValue =" + no);

			//	Set ProductSpent from ProductSpentName
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET M_ProductSpent_ID = (SELECT M_Product_ID FROM M_Product m"
				  + " WHERE r.ProductSpentName=m.Name AND r.AD_Client_ID=m.AD_Client_ID ), "
				  + " ProductSpentValue =(SELECT Value FROM M_Product m"
				  + " WHERE r.ProductSpentName=m.Name AND r.AD_Client_ID=m.AD_Client_ID )"
				  + " WHERE M_ProductSpent_ID IS NULL AND ProductSpentName IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set ProductSpent from ProductSpentValue =" + no);


		// Error - Invalid ProductSpent
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid ProductSpent, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.M_ProductSpent_ID IS NULL"
				  + " AND (r.productSpentName IS NOT NULL OR r.ProductSpentValue is NOT NULL)").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid ProductSpent=" + no);
		
		//	Set Activity from ActivityKey
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_Activity_ID=(SELECT C_Activity_ID FROM C_Activity a"
				  + " WHERE r.ActivityValue=a.Value AND r.AD_Client_ID=a.AD_Client_ID ), "
				  + " ActivityName=(SELECT Name FROM C_Activity a"
				  + " WHERE r.ActivityValue=a.Value AND r.AD_Client_ID=a.AD_Client_ID )"
				  + " WHERE C_Activity_ID IS NULL AND ActivityValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Activity from ActivityValue =" + no);

		//	Set Activity from ActivityName
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_Activity_ID = (SELECT C_Activity_ID FROM C_Activity a"
				  + " WHERE r.ActivityName=a.Name AND r.AD_Client_ID=a.AD_Client_ID ), "
				  + " ActivityValue =(SELECT Value FROM C_Activity a"
				  + " WHERE r.ActivityName=a.Name AND r.AD_Client_ID=a.AD_Client_ID )"
				  + " WHERE C_Activity_ID IS NULL AND ActivityName IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Activity from ActivityName =" + no);
			
		// Error - Invalid Activity
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Activity, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.C_Activity_ID IS NULL"
				  + " AND (r.ActivityName IS NOT NULL OR r.ActivityValue is NOT NULL)").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Activity=" + no);

		// Set BP from BPartnerKey
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_BPartnerSR_ID=(SELECT C_BPartner_ID FROM C_BPartner b"
				  + " WHERE b.Value=r.BPartnerSRValue AND b.AD_Client_ID=r.AD_Client_ID AND isSalesRep='Y'), "
				  + " BPartnerSRName =(SELECT Name FROM C_BPartner b"
				  + " WHERE b.Value=r.BPartnerSRValue AND b.AD_Client_ID=r.AD_Client_ID AND isSalesRep='Y')"
				  + " WHERE C_BPartnerSR_ID IS NULL AND BPartnerSRValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set BPartnerSR from BPartnerSRKey=" + no);

		//	Set BP (Agent) from BPartnerSRName
		sql = new StringBuffer ("UPDATE I_Request r"
			  + " SET C_BPartnerSR_ID=(SELECT C_BPartner_ID FROM C_BPartner b"
			  + " WHERE b.Name=r.BPartnerSRName AND b.AD_Client_ID=r.AD_Client_ID AND isSalesRep='Y'), "
			  + " BPartnerSRValue =(SELECT value FROM C_BPartner b"
			  + " WHERE b.Name=r.BPartnerSRName AND b.AD_Client_ID=r.AD_Client_ID AND isSalesRep='Y')"
			  + " WHERE C_BPartnerSR_ID IS NULL AND BPartnerSRName IS NOT NULL"
			  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set BPartnerSR from BPartnerSRName=" + no);

		// Error - Invalid BPartnerSR
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Bpartner(Agent), ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND ((r.C_BPartnerSR_ID is NULL AND (r.BPartnerSRName is NOT NULL OR r.BPartnerSRValue IS NOT NULL))"
				  + " OR (r.C_BPartnerSR_ID is NOT NULL AND "
				  + " NOT EXISTS (SELECT 1 from C_BPartner b WHERE b.C_BPartner_ID = r.C_BPartnerSR_ID"
				  + " AND b.AD_Client_ID=r.AD_Client_ID AND isSalesRep='Y')))").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid BPartner (Agent)=" + no);

		// Set Project from ProjectKey
		sql = new StringBuffer ("UPDATE I_Request r "
				  + " SET C_Project_ID=(SELECT C_Project_ID FROM C_Project p"
				  + " WHERE p.Value=r.ProjectValue AND p.AD_Client_ID=r.AD_Client_ID ), "
				  + " ProjectName=(SELECT Name FROM C_Project p"
				  + " WHERE p.Value=r.ProjectValue AND p.AD_Client_ID=r.AD_Client_ID ) "
				  + " WHERE C_Project_ID IS NULL AND ProjectValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Project from ProjectKey=" + no);

		// Set Project from ProjectName
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_Project_ID=(SELECT C_Project_ID FROM C_Project p"
				  + " WHERE p.Name=r.ProjectName AND p.AD_Client_ID=r.AD_Client_ID ), "
				  + " ProjectValue=(SELECT Value FROM C_Project p"
				  + " WHERE p.Name=r.ProjectName AND p.AD_Client_ID=r.AD_Client_ID ) "
				  + " WHERE C_Project_ID IS NULL AND ProjectName IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Project from ProjectName=" + no);

		// Error - Invalid Project
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Project, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.C_Project_ID is NULL"
				  + " AND (r.ProjectName is NOT NULL OR r.ProjectValue IS NOT NULL)").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Project=" + no);
		
		// Set Asset from AssetKey
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET A_Asset_ID=(SELECT A_Asset_ID FROM A_Asset a"
				  + " WHERE a.Value=r.AssetValue AND a.AD_Client_ID=r.AD_Client_ID ), "
				  + " AssetName =(SELECT Name FROM A_Asset a"
				  + " WHERE a.Value=r.AssetValue AND a.AD_Client_ID=r.AD_Client_ID ) "
				  + " WHERE A_Asset_ID IS NULL AND AssetValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Asset from AssetKey=" + no);

		// Set Project from AssetName
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET A_Asset_ID=(SELECT A_Asset_ID FROM A_Asset a"
				  + " WHERE a.Name=r.AssetName AND a.AD_Client_ID=r.AD_Client_ID ), "
				  + " AssetValue=(SELECT Value FROM A_Asset a"
				  + " WHERE a.Name=r.AssetName AND a.AD_Client_ID=r.AD_Client_ID ) "
				  + " WHERE A_Asset_ID IS NULL AND AssetName IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Asset from AssetName=" + no);

		// Error - Invalid Asset
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Asset, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.A_Asset_ID is NULL"
				  + " AND (r.AssetName is NOT NULL OR r.AssetValue IS NOT NULL)").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Asset=" + no);

		//	Set Product from ProductKey
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET M_Product_ID=(SELECT M_Product_ID FROM M_Product m"
				  + " WHERE r.ProductValue=m.Value AND r.AD_Client_ID=m.AD_Client_ID ), "
				  + " ProductName=(SELECT Name FROM M_Product m"
				  + " WHERE r.ProductValue=m.Value AND r.AD_Client_ID=m.AD_Client_ID )"
				  + " WHERE M_Product_ID IS NULL AND ProductValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Product from ProductValue =" + no);

		//	Set Product from ProductName
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET M_Product_ID =(SELECT M_Product_ID FROM M_Product m"
				  + " WHERE r.ProductName=m.Name AND r.AD_Client_ID=m.AD_Client_ID ), "
				  + " ProductValue =(SELECT Value FROM M_Product m"
				  + " WHERE r.ProductName=m.Name AND r.AD_Client_ID=m.AD_Client_ID )"
				  + " WHERE M_Product_ID IS NULL AND ProductName IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Product from ProductName =" + no);


		// Error - Invalid Product
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid ProductSpent, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.M_Product_ID IS NULL"
				  + " AND (r.productName IS NOT NULL OR r.ProductValue is NOT NULL)").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Product=" + no);

		//	Set Campaign from CampaignKey
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_Campaign_ID=(SELECT C_Campaign_ID FROM C_Campaign c"
				  + " WHERE r.CampaignValue=c.Value AND r.AD_Client_ID=c.AD_Client_ID ), "
				  + " CampaignName =(SELECT Name FROM C_Campaign c"
				  + " WHERE r.CampaignValue=c.Value AND r.AD_Client_ID=c.AD_Client_ID )"
				  + " WHERE C_Campaign_ID IS NULL AND CampaignValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Campaign from CampaignValue =" + no);

		//	Set Campaign from CampaignName
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_Campaign_ID =(SELECT C_Campaign_ID FROM C_Campaign c"
				  + " WHERE r.CampaignName=c.Name AND r.AD_Client_ID=c.AD_Client_ID ), "
				  + " CampaignValue =(SELECT Value FROM C_Campaign c"
				  + " WHERE r.CampaignName=c.Name AND r.AD_Client_ID=c.AD_Client_ID )"
				  + " WHERE C_Campaign_ID IS NULL AND CampaignName IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Campaign from CampaignValue =" + no);


		// Error - Invalid Campaign
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Campaign, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.C_Campaign_ID IS NULL"
				  + " AND (r.CampaignName IS NOT NULL OR r.CampaignValue is NOT NULL)").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Campaign=" + no);

		//	Set SalesRegion from SalesRegionKey
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_SalesRegion_ID=(SELECT C_SalesRegion_ID FROM C_SalesRegion s"
				  + " WHERE r.SalesRegionValue=s.Value AND r.AD_Client_ID=s.AD_Client_ID ), "
				  + " SalesRegionName=(SELECT Name FROM C_SalesRegion s"
				  + " WHERE r.SalesRegionValue=s.Value AND r.AD_Client_ID=s.AD_Client_ID )"
				  + " WHERE C_SalesRegion_ID IS NULL AND SalesRegionValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set SalesRegion from SalesRegionValue =" + no);

		//	Set SalesRegion from SalesRegionName
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_SalesRegion_ID=(SELECT C_SalesRegion_ID FROM C_SalesRegion s"
				  + " WHERE r.SalesRegionName=s.Name AND r.AD_Client_ID=s.AD_Client_ID ), "
				  + " SalesRegionValue=(SELECT SalesRegionValue FROM C_SalesRegion s"
				  + " WHERE r.SalesRegionName=s.Name AND r.AD_Client_ID=s.AD_Client_ID )"
				  + " WHERE C_SalesRegion_ID IS NULL AND SalesRegionName IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set SalesRegion from SalesRegionValue =" + no);

		// Error - Invalid SalesRegion
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid SalesRegion, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.C_SalesRegion_ID IS NULL"
				  + " AND (r.SalesRegionName IS NOT NULL OR r.SalesRegionValue is NOT NULL)").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid SalesRegion=" + no);

		//	Set Order from OrderDocumentNo
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_Order_ID =(SELECT C_Order_ID FROM C_Order o"
				  + " WHERE r.OrderDocumentNo = o.DocumentNo AND r.AD_Client_ID=o.AD_Client_ID )"
				  + " WHERE C_Order_ID IS NULL AND OrderDocumentNo IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Order from OrderDocumentNo =" + no);

		// Error - Invalid OrderDocumentNo
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid OrderDocumentNo, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.C_Order_ID IS NULL"
				  + " AND r.OrderDocumentNo IS NOT NULL ").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid OrderDocumentNo=" + no);

		//	Set Invoice from InvoiceDocumentNo
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_Invoice_ID =(SELECT C_Invoice_ID FROM C_Invoice i"
				  + " WHERE r.InvoiceDocumentNo = i.DocumentNo AND r.AD_Client_ID=i.AD_Client_ID )"
				  + " WHERE C_Invoice_ID IS NULL AND InvoiceDocumentNo IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Invoice from InvoiceDocumentNo =" + no);

		// Error - Invalid InvoiceDocumentNo
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid InvoiceDocumentNo, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.C_Invoice_ID IS NULL"
				  + " AND r.InvoiceDocumentNo IS NOT NULL ").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid InvoiceDocumentNo=" + no);

		//	Set Payment from PaymentDocumentNo
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_Payment_ID =(SELECT C_Payment_ID FROM C_Payment p"
				  + " WHERE r.PaymentDocumentNo = p.DocumentNo AND r.AD_Client_ID=p.AD_Client_ID )"
				  + " WHERE C_Payment_ID IS NULL AND PaymentDocumentNo IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Payment from PaymentDocumentNo =" + no);

		// Error - Invalid PaymentDocumentNo
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid PaymentDocumentNo, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.C_Payment_ID IS NULL"
				  + " AND r.PaymentDocumentNo IS NOT NULL ").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid PaymentDocumentNo=" + no);

		//	Set Order from InOutDocumentNo
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET M_InOut_ID =(SELECT M_InOut_ID FROM M_InOut i"
				  + " WHERE r.InOutDocumentNo = i.DocumentNo AND r.AD_Client_ID=i.AD_Client_ID )"
				  + " WHERE M_InOut_ID IS NULL AND InOutDocumentNo IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Shipment/Receipt from InOutDocumentNo =" + no);

		// Error - Invalid InOutDocumentNo
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid InOutDocumentNo, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.M_InOut_ID IS NULL"
				  + " AND r.InOutDocumentNo IS NOT NULL ").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid InOutDocumentNo=" + no);

		//	Set Lead from LeadDocumentNo
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET C_Lead_ID =(SELECT C_Lead_ID FROM C_Lead l"
				  + " WHERE r.LeadDocumentNo = l.DocumentNo AND r.AD_Client_ID=l.AD_Client_ID )"
				  + " WHERE C_Lead_ID IS NULL AND LeadDocumentNo IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Lead from LeadDocumentNo =" + no);

		// Error - Invalid LeadDocumentNo
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid LeadDocumentNo, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.C_Lead_ID IS NULL"
				  + " AND r.LeadDocumentNo IS NOT NULL ").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid LeadDocumentNo=" + no);

		//	Set ChangeRequest from ChangeRequestName
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET M_ChangeRequest_ID =(SELECT M_ChangeRequest_ID FROM M_ChangeRequest c"
				  + " WHERE r.ChangeRequestName = c.Name AND r.AD_Client_ID=c.AD_Client_ID )"
				  + " WHERE M_ChangeRequest_ID IS NULL AND ChangeRequestName IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set ChangeRequest from ChangeRequestName =" + no);

		// Error - Invalid ChangeRequestName
		sql = new StringBuffer ("UPDATE I_Request r"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid ChangeRequestName, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND r.M_ChangeRequest_ID IS NULL"
				  + " AND r.ChangeRequestName IS NOT NULL ").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid ChangeRequestName=" + no);

		commit();
		
		//	-- New Requests -----------------------------------------------------

		int noInsert = 0;
		int noProcessed = 0;

		//	Go through Request Records w/o
		sql = new StringBuffer ("SELECT * FROM I_Request "
				  + "WHERE I_IsImported='N'").append (clientCheck)
					.append(" ORDER BY DocumentNo, I_Request_ID");
		try
		{
			PreparedStatement pstmt = DB.prepareStatement (sql.toString(), get_TrxName());
			ResultSet rs = pstmt.executeQuery ();

			MRequest request = null;

			while (rs.next ())
			{
				X_I_Request imp = new X_I_Request (getCtx(), rs, get_TrxName());
				request = new MRequest (imp);
				
				// Save Request
				if (!request.save())
				{
					String msg = "Could not save Request";
					ValueNamePair pp = CLogger.retrieveError();
					if (pp != null)
						msg += " - " + pp.toStringX();
					imp.setI_ErrorMsg(msg);
					imp.save();
					continue;
				}
					
				noProcessed++;
				imp.setR_Request_ID(request.getR_Request_ID());
				imp.setI_IsImported(X_I_Locator.I_ISIMPORTED_Yes);
				imp.setProcessed(true);
				imp.save();
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Order - " + sql.toString(), e);
		}

		//Set Error to indicator to not imported
		sql = new StringBuffer ("UPDATE I_Request "
			+ "SET I_IsImported='N', Updated=SysDate "
			+ "WHERE I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		addLog (0, null, new BigDecimal (no), "@Errors@");
		//addLog (0, null, new BigDecimal (noInsert), "@R_Request_ID@: @Inserted@");
		addLog (0, null, new BigDecimal (noProcessed), " @Processed@");
		return "#" + noInsert + "/" + noProcessed;
	}

	
	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare() {

		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) 
		{
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("AD_Org_ID"))
				m_AD_Org_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("DeleteOldImported"))
				m_deleteOldImported = "Y".equals(element.getParameter());
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}// prepare

}
