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

/**
 *	Import BPartners from I_BPartner
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ImportBPartner.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class ImportBPartner extends SvrProcess
{
	/**	Client to be imported to		*/
	private int				p_AD_Client_ID = 0;
	/**	Delete old Imported				*/
	private boolean			p_deleteOldImported = false;

	/** Effective						*/
	private Timestamp		p_DateValue = null;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	@Override
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (ProcessInfoParameter element : para) 
		{
			String name = element.getParameterName();
			if (element.getParameter() == null)
				;
			else if (name.equals("AD_Client_ID"))
				p_AD_Client_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("DeleteOldImported"))
				p_deleteOldImported = "Y".equals(element.getParameter());
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		if (p_DateValue == null)
			p_DateValue = new Timestamp (System.currentTimeMillis());
	}	//	prepare


	/**
	 *  Perrform process.
	 *  @return Message
	 *  @throws Exception
	 */
	@Override
	protected String doIt() throws java.lang.Exception
	{
		StringBuffer sql = null;
		int no = 0;
		String clientCheck = " AND AD_Client_ID=" + p_AD_Client_ID;

		//	****	Prepare	****

		//	Delete Old Imported
		if (p_deleteOldImported)
		{
			sql = new StringBuffer ("DELETE FROM I_BPartner "
				+ "WHERE I_IsImported='Y'").append(clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			log.fine("Delete Old Impored =" + no);
		}

		//	Set Client, Org, IsActive, Created/Updated
		sql = new StringBuffer ("UPDATE I_BPartner "
			+ "SET AD_Client_ID = COALESCE (AD_Client_ID, ").append(p_AD_Client_ID).append("),"
			+ " AD_Org_ID = COALESCE (AD_Org_ID, 0),"
			+ " IsActive = COALESCE (IsActive, 'Y'),"
			+ " Created = COALESCE (Created, SysDate),"
			+ " CreatedBy = COALESCE (CreatedBy, 0),"
			+ " Updated = COALESCE (Updated, SysDate),"
			+ " UpdatedBy = COALESCE (UpdatedBy, 0),"
			+ " I_ErrorMsg = NULL,"
			+ " I_IsImported = 'N' "
			+ "WHERE I_IsImported<>'Y' OR I_IsImported IS NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Reset=" + no);

		//	Set BP_Group
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET GroupValue=(SELECT MAX(Value) FROM C_BP_Group g WHERE g.IsDefault='Y'"
			+ " AND g.AD_Client_ID=i.AD_Client_ID) ");
		sql.append("WHERE GroupValue IS NULL AND C_BP_Group_ID IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Group Default=" + no);
		//
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET C_BP_Group_ID=(SELECT C_BP_Group_ID FROM C_BP_Group g"
			+ " WHERE i.GroupValue=g.Value AND g.AD_Client_ID=i.AD_Client_ID) "
			+ "WHERE C_BP_Group_ID IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Group=" + no);
		//
		String ts = DB.isPostgreSQL()?"COALESCE(I_ErrorMsg,'')":"I_ErrorMsg";  //java bug, it could not be used directly
		sql = new StringBuffer ("UPDATE I_BPartner "
			+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Group, ' "
			+ "WHERE C_BP_Group_ID IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Invalid Group=" + no);

		//	Set Country
		/**
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET CountryCode=(SELECT CountryCode FROM C_Country c WHERE c.IsDefault='Y'"
			+ " AND c.AD_Client_ID IN (0, i.AD_Client_ID) AND ROWNUM=1) "
			+ "WHERE CountryCode IS NULL AND C_Country_ID IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Country Default=" + no);
		**/
		//
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET C_Country_ID=(SELECT C_Country_ID FROM C_Country c"
			+ " WHERE i.CountryCode=c.CountryCode AND c.IsSummary='N' AND c.AD_Client_ID IN (0, i.AD_Client_ID)) "
			+ "WHERE C_Country_ID IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Country=" + no);
		//
		sql = new StringBuffer ("UPDATE I_BPartner "
			+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Country, ' "
			+ "WHERE C_Country_ID IS NULL AND (City IS NOT NULL OR Address1 IS NOT NULL)"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Invalid Country=" + no);

		//	Set Region
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "Set RegionName=(SELECT Name FROM C_Region r"
			+ " WHERE r.IsDefault='Y' AND r.C_Country_ID=i.C_Country_ID"
			+ " AND r.AD_Client_ID IN (0, i.AD_Client_ID)) " );
		/*
		if (DB.isOracle()) //jz
		{
			sql.append(" AND ROWNUM=1) ");
		}
		else 
			sql.append(" AND r.UPDATED IN (SELECT MAX(UPDATED) FROM C_Region r1"
			+ " WHERE r1.IsDefault='Y' AND r1.C_Country_ID=i.C_Country_ID"
			+ " AND r1.AD_Client_ID IN (0, i.AD_Client_ID) ");
			*/
		sql.append("WHERE RegionName IS NULL AND C_Region_ID IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Region Default=" + no);
		//
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "Set C_Region_ID=(SELECT C_Region_ID FROM C_Region r"
			+ " WHERE r.Name=i.RegionName AND r.C_Country_ID=i.C_Country_ID"
			+ " AND r.AD_Client_ID IN (0, i.AD_Client_ID)) "
			+ "WHERE C_Region_ID IS NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Region=" + no);
		//
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Region, ' "
			+ "WHERE C_Region_ID IS NULL "
			+ " AND EXISTS (SELECT * FROM C_Country c"
			+ " WHERE c.C_Country_ID=i.C_Country_ID AND c.HasRegion='Y')"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Invalid Region=" + no);

		//	Set Greeting
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET C_Greeting_ID=(SELECT C_Greeting_ID FROM C_Greeting g"
			+ " WHERE i.BPContactGreeting=g.Name AND g.AD_Client_ID IN (0, i.AD_Client_ID)) "
			+ "WHERE C_Greeting_ID IS NULL AND BPContactGreeting IS NOT NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Greeting=" + no);
		//
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Greeting, ' "
			+ "WHERE C_Greeting_ID IS NULL AND BPContactGreeting IS NOT NULL"
			+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.config("Invalid Greeting=" + no);

		//	Existing User ?
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET (C_BPartner_ID,AD_User_ID)="
				+ "(SELECT C_BPartner_ID,AD_User_ID FROM AD_User u "
				+ "WHERE i.EMail=u.EMail AND u.AD_Client_ID=i.AD_Client_ID) "
			+ "WHERE i.EMail IS NOT NULL AND I_IsImported='N'").append(clientCheck);

		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Found EMail User=" + no);

		//	Existing BPartner ? Match Value
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET C_BPartner_ID=(SELECT C_BPartner_ID FROM C_BPartner p"
			+ " WHERE i.Value=p.Value AND p.AD_Client_ID=i.AD_Client_ID) "
			+ "WHERE C_BPartner_ID IS NULL AND Value IS NOT NULL"
			+ " AND I_IsImported='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Found BPartner=" + no);

		//	Existing Contact ? Match Name
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET AD_User_ID=(SELECT AD_User_ID FROM AD_User c"
			+ " WHERE i.ContactName=c.Name AND i.C_BPartner_ID=c.C_BPartner_ID AND c.AD_Client_ID=i.AD_Client_ID) "
			+ "WHERE C_BPartner_ID IS NOT NULL AND AD_User_ID IS NULL AND ContactName IS NOT NULL"
			+ " AND I_IsImported='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Found Contact=" + no);

		//	Existing Location ? Exact Match
		sql = new StringBuffer ("UPDATE I_BPartner i "
			+ "SET C_BPartner_Location_ID=(SELECT C_BPartner_Location_ID"
			+ " FROM C_BPartner_Location bpl INNER JOIN C_Location l ON (bpl.C_Location_ID=l.C_Location_ID)"
			+ " WHERE i.C_BPartner_ID=bpl.C_BPartner_ID AND bpl.AD_Client_ID=i.AD_Client_ID"
			+ " AND COALESCE(i.Address1,N' ')=COALESCE(l.Address1,N' ') "
			+ " AND COALESCE(i.Address2,N' ')=COALESCE(l.Address2,N' ')"
			+ " AND COALESCE(i.Address3,N' ')=COALESCE(l.Address3,N' ') "
			+ " AND COALESCE(i.Address4,N' ')=COALESCE(l.Address4,N' ')"
			+ " AND COALESCE(i.City,N' ')=COALESCE(l.City,N' ') "
			+ " AND COALESCE(i.Postal,N' ')=COALESCE(l.Postal,N' ') "
			+ " AND COALESCE(i.Postal_Add,N' ')=COALESCE(l.Postal_Add,N' ')"
			+ " AND COALESCE(i.C_Region_ID,0)=COALESCE(l.C_Region_ID,0)"
			+ " AND COALESCE(i.C_Country_ID,0)=COALESCE(l.C_Country_ID,0)) "
			+ "WHERE C_BPartner_ID IS NOT NULL AND C_BPartner_Location_ID IS NULL"
			+ " AND I_IsImported='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Found Location=" + no);

		//	Interest Area
		sql = new StringBuffer ("UPDATE I_BPartner i " 
			+ "SET R_InterestArea_ID=(SELECT R_InterestArea_ID FROM R_InterestArea ia "
				+ "WHERE i.InterestAreaName=ia.Name AND ia.AD_Client_ID=i.AD_Client_ID) "
			+ "WHERE R_InterestArea_ID IS NULL AND InterestAreaName IS NOT NULL"
			+ " AND I_IsImported='N'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Interest Area=" + no);

		
		commit();
		//	-------------------------------------------------------------------
		int noInsert = 0;
		int noUpdate = 0;

		//	Go through Records
		sql = new StringBuffer ("SELECT * FROM I_BPartner "
			+ "WHERE I_IsImported='N'").append(clientCheck);
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				X_I_BPartner impBP = new X_I_BPartner (getCtx(), rs, get_TrxName());
				log.fine("I_BPartner_ID=" + impBP.getI_BPartner_ID()
					+ ", C_BPartner_ID=" + impBP.getC_BPartner_ID()
					+ ", C_BPartner_Location_ID=" + impBP.getC_BPartner_Location_ID()
					+ ", AD_User_ID=" + impBP.getAD_User_ID());


				//	****	Create/Update BPartner	****
				MBPartner bp = null;
				if (impBP.getC_BPartner_ID() == 0)	//	Insert new BPartner
				{
					bp = new MBPartner(impBP);
					if (bp.save())
					{
						impBP.setC_BPartner_ID(bp.getC_BPartner_ID());
						log.finest("Insert BPartner - " + bp.getC_BPartner_ID());
						noInsert++;
					}
					else
					{
						sql = new StringBuffer ("UPDATE I_BPartner i "
							+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||")
							.append("Cannot Insert BPartner")
							.append("WHERE I_BPartner_ID=").append(impBP.getI_BPartner_ID());
						DB.executeUpdate(sql.toString(), get_TrxName());
						continue;
					}
				}
				else				//	Update existing BPartner
				{
					bp = new MBPartner(getCtx(), impBP.getC_BPartner_ID(), get_TrxName());
				//	if (impBP.getValue() != null)			//	not to overwite
				//		bp.setValue(impBP.getValue());
					if (impBP.getName() != null)
					{
						bp.setName(impBP.getName());
						bp.setName2(impBP.getName2());
					}
					if (impBP.getDUNS() != null)
						bp.setDUNS(impBP.getDUNS());
					if (impBP.getTaxID() != null)
						bp.setTaxID(impBP.getTaxID());
					if (impBP.getNAICS() != null)
						bp.setNAICS(impBP.getNAICS());
					if (impBP.getC_BP_Group_ID() != 0)
						bp.setC_BP_Group_ID(impBP.getC_BP_Group_ID());
					if (impBP.getDescription() != null)
						bp.setDescription(impBP.getDescription());
					//
					if (bp.save())
					{
						log.finest("Update BPartner - " + bp.getC_BPartner_ID());
						noUpdate++;
					}
					else
					{
						sql = new StringBuffer ("UPDATE I_BPartner i "
							+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||")
							.append("' Cannot Update BPartner' ") //jz
							.append("WHERE I_BPartner_ID=").append(impBP.getI_BPartner_ID());
						DB.executeUpdate(sql.toString(), get_TrxName());
						continue;
					}
				}

				//	****	Create/Update BPartner Location	****
				MBPartnerLocation bpl = null;
				if (impBP.getC_BPartner_Location_ID() != 0)		//	Update Location
				{
					bpl = new MBPartnerLocation(getCtx(), impBP.getC_BPartner_Location_ID(), get_TrxName());
					MLocation location = new MLocation(getCtx(), bpl.getC_Location_ID(), get_TrxName());
					location.setC_Country_ID(impBP.getC_Country_ID());
					location.setC_Region_ID(impBP.getC_Region_ID());
					location.setCity(impBP.getCity());
					location.setAddress1(impBP.getAddress1());
					location.setAddress2(impBP.getAddress2());
					location.setAddress3(impBP.getAddress3());
					location.setAddress4(impBP.getAddress4());
					location.setPostal(impBP.getPostal());
					location.setPostal_Add(impBP.getPostal_Add());
					location.setRegionName(impBP.getRegionName());
					if (!location.save())
						log.warning("Location not updated");
					else
						bpl.setC_Location_ID(location.getC_Location_ID());
					if (impBP.getPhone() != null)
						bpl.setPhone(impBP.getPhone());
					if (impBP.getPhone2() != null)
						bpl.setPhone2(impBP.getPhone2());
					if (impBP.getFax() != null)
						bpl.setFax(impBP.getFax());
					bpl.save();
				}
				else 	//	New Location
					if (impBP.getC_Country_ID() != 0 &&
						impBP.getC_Region_ID() != 0 &&
						( impBP.getAddress1() != null
						|| impBP.getAddress2() != null
						|| impBP.getAddress3() != null						
						|| impBP.getAddress4() != null
						|| impBP.getPostal() != null 
						|| impBP.getCity() != null))
				{
					MLocation location = new MLocation(getCtx(), impBP.getC_Country_ID(), 
						impBP.getC_Region_ID(), impBP.getCity(), get_TrxName());
					location.setAddress1(impBP.getAddress1());
					location.setAddress2(impBP.getAddress2());
					location.setAddress3(impBP.getAddress3());
					location.setAddress4(impBP.getAddress4());
					location.setPostal(impBP.getPostal());
					location.setPostal_Add(impBP.getPostal_Add());
					location.setRegionName(impBP.getRegionName());
					if (location.save())
						log.finest("Insert Location - " + location.getC_Location_ID());
					else
					{
						rollback();
						noInsert--;
						sql = new StringBuffer ("UPDATE I_BPartner i "
							+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||")
							.append("Cannot Insert Location")
							.append("WHERE I_BPartner_ID=").append(impBP.getI_BPartner_ID());
						DB.executeUpdate(sql.toString(), get_TrxName());
						continue;
					}
					//
					bpl = new MBPartnerLocation (bp);
					bpl.setC_Location_ID(location.getC_Location_ID());
					bpl.setPhone(impBP.getPhone());
					bpl.setPhone2(impBP.getPhone2());
					bpl.setFax(impBP.getFax());
					if (bpl.save())
					{
						log.finest("Insert BP Location - " + bpl.getC_BPartner_Location_ID());
						impBP.setC_BPartner_Location_ID(bpl.getC_BPartner_Location_ID());
					}
					else
					{
						rollback();
						noInsert--;
						sql = new StringBuffer ("UPDATE I_BPartner i "
							+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||")
							.append("Cannot Insert BPLocation")
							.append("WHERE I_BPartner_ID=").append(impBP.getI_BPartner_ID());
						DB.executeUpdate(sql.toString(), get_TrxName());
						continue;
					}
				}

				//	****	Create/Update Contact	****
				MUser user = null;
				if (impBP.getAD_User_ID() != 0)
				{
					user = new MUser (getCtx(), impBP.getAD_User_ID(), get_TrxName());
					if (user.getC_BPartner_ID() == 0)
						user.setC_BPartner_ID(bp.getC_BPartner_ID());
					else if (user.getC_BPartner_ID() != bp.getC_BPartner_ID())
					{
						rollback();
						noInsert--;
						sql = new StringBuffer ("UPDATE I_BPartner i "
							+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||")
							.append("BP of User <> BP")
							.append("WHERE I_BPartner_ID=").append(impBP.getI_BPartner_ID());
						DB.executeUpdate(sql.toString(), get_TrxName());
						continue;
					}
					if (impBP.getC_Greeting_ID() != 0)
						user.setC_Greeting_ID(impBP.getC_Greeting_ID());
					String name = impBP.getContactName();
					if (name == null || name.length() == 0)
						name = impBP.getEMail();
					user.setName(name);
					if (impBP.getTitle() != null)
						user.setTitle(impBP.getTitle());
					if (impBP.getContactDescription() != null)
						user.setDescription(impBP.getContactDescription());
					if (impBP.getComments() != null)
						user.setComments(impBP.getComments());
					if (impBP.getPhone() != null)
						user.setPhone(impBP.getPhone());
					if (impBP.getPhone2() != null)
						user.setPhone2(impBP.getPhone2());
					if (impBP.getFax() != null)
						user.setFax(impBP.getFax());
					if (impBP.getEMail() != null)
						user.setEMail(impBP.getEMail());
					if (impBP.getBirthday() != null)
						user.setBirthday(impBP.getBirthday());
					if (bpl != null)
						user.setC_BPartner_Location_ID(bpl.getC_BPartner_Location_ID());
					if (user.save())
					{
						log.finest("Update BP Contact - " + user.getAD_User_ID());
					}
					else
					{
						rollback();
						noInsert--;
						sql = new StringBuffer ("UPDATE I_BPartner i "
							+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||")
							.append("Cannot Update BP Contact")
							.append("WHERE I_BPartner_ID=").append(impBP.getI_BPartner_ID());
						DB.executeUpdate(sql.toString(), get_TrxName());
						continue;
					}
				}
				else 	//	New Contact
					if (impBP.getContactName() != null || impBP.getEMail() != null)
				{
					user = new MUser (bp);
					if (impBP.getC_Greeting_ID() != 0)
						user.setC_Greeting_ID(impBP.getC_Greeting_ID());
					String name = impBP.getContactName();
					if (name == null || name.length() == 0)
						name = impBP.getEMail();
					user.setName(name);
					user.setTitle(impBP.getTitle());
					user.setDescription(impBP.getContactDescription());
					user.setComments(impBP.getComments());
					user.setPhone(impBP.getPhone());
					user.setPhone2(impBP.getPhone2());
					user.setFax(impBP.getFax());
					user.setEMail(impBP.getEMail());
					user.setBirthday(impBP.getBirthday());
					if (bpl != null)
						user.setC_BPartner_Location_ID(bpl.getC_BPartner_Location_ID());
					if (user.save())
					{
						log.finest("Insert BP Contact - " + user.getAD_User_ID());
						impBP.setAD_User_ID(user.getAD_User_ID());
					}
					else
					{
						rollback();
						noInsert--;
						sql = new StringBuffer ("UPDATE I_BPartner i "
							+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||")
							.append("Cannot Insert BPContact")
							.append("WHERE I_BPartner_ID=").append(impBP.getI_BPartner_ID());
						DB.executeUpdate(sql.toString(), get_TrxName());
						continue;
					}
				}

				//	Interest Area
				if (impBP.getR_InterestArea_ID() != 0 && user != null)
				{
					MContactInterest ci = MContactInterest.get(getCtx(), 
						impBP.getR_InterestArea_ID(), user.getAD_User_ID(), 
						true, get_TrxName());
					ci.save();		//	don't subscribe or re-activate
				}
				//
				impBP.setI_IsImported(X_I_BPartner.I_ISIMPORTED_Yes);
				impBP.setProcessed(true);
				impBP.setProcessing(false);
				impBP.save();
				commit();
			}	//	for all I_Product
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, "", e);
			rollback();
		}

		//	Set Error to indicator to not imported
		sql = new StringBuffer ("UPDATE I_BPartner "
			+ "SET I_IsImported='N', Updated=SysDate "
			+ "WHERE I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		addLog (0, null, new BigDecimal (no), "@Errors@");
		addLog (0, null, new BigDecimal (noInsert), "@C_BPartner_ID@: @Inserted@");
		addLog (0, null, new BigDecimal (noUpdate), "@C_BPartner_ID@: @Updated@");
		return "";
	}	//	doIt

}	//	ImportBPartner
