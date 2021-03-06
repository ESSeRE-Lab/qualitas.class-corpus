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
package org.compiere.model;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.util.*;


/**
 *	Accounting Processor Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MAcctProcessor.java,v 1.3 2006/07/30 00:51:02 jjanke Exp $
 */
public class MAcctProcessor extends X_C_AcctProcessor
	implements CompiereProcessor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 	Get Active
	 *	@param ctx context
	 *	@return active processors
	 */
	public static MAcctProcessor[] getActive (Ctx ctx)
	{
		ArrayList<MAcctProcessor> list = new ArrayList<MAcctProcessor>();
		String sql = "SELECT * FROM C_AcctProcessor WHERE IsActive='Y'";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, (Trx) null);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MAcctProcessor (ctx, rs, null));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, "getActive", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		MAcctProcessor[] retValue = new MAcctProcessor[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getActive

	/**	Static Logger	*/
	private static CLogger	s_log	= CLogger.getCLogger (MAcctProcessor.class);

	
	/**
	 * 	Standard Construvtor
	 *	@param ctx context
	 *	@param C_AcctProcessor_ID id
	 *	@param trx transaction
	 */
	public MAcctProcessor (Ctx ctx, int C_AcctProcessor_ID, Trx trx)
	{
		super (ctx, C_AcctProcessor_ID, trx);
		if (C_AcctProcessor_ID == 0)
		{
		//	setName (null);
		//	setSupervisor_ID (0);
			setFrequencyType (FREQUENCYTYPE_Hour);
			setFrequency (1);
			setKeepLogDays (7);	// 7
		}	
	}	//	MAcctProcessor

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MAcctProcessor (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MAcctProcessor

	/**
	 * 	Parent Constructor
	 *	@param client parent
	 *	@param Supervisor_ID admin
	 */
	public MAcctProcessor (MClient client, int Supervisor_ID)
	{
		this (client.getCtx(), 0, client.get_Trx());
		setClientOrg(client);
		setName (client.getName() + " - " 
			+ Msg.translate(getCtx(), "C_AcctProcessor_ID"));
		setSupervisor_ID (Supervisor_ID);
	}	//	MAcctProcessor
	
	
	
	/**
	 * 	Get Server ID
	 *	@return id
	 */
	public String getServerID ()
	{
		return "AcctProcessor" + get_ID();
	}	//	getServerID

	/**
	 * 	Get Date Next Run
	 *	@param requery requery
	 *	@return date next run
	 */
	public Timestamp getDateNextRun (boolean requery)
	{
		if (requery)
			load(get_Trx());
		return getDateNextRun();
	}	//	getDateNextRun

	/**
	 * 	Get Logs
	 *	@return logs
	 */
	public CompiereProcessorLog[] getLogs ()
	{
		ArrayList<MAcctProcessorLog> list = new ArrayList<MAcctProcessorLog>();
		String sql = "SELECT * "
			+ "FROM C_AcctProcessorLog "
			+ "WHERE C_AcctProcessor_ID=? " 
			+ "ORDER BY Created DESC";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_Trx());
			pstmt.setInt (1, getC_AcctProcessor_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MAcctProcessorLog (getCtx(), rs, get_Trx()));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		MAcctProcessorLog[] retValue = new MAcctProcessorLog[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getLogs

	/**
	 * 	Delete old Request Log
	 *	@return number of records
	 */
	public int deleteLog()
	{
		if (getKeepLogDays() < 1)
			return 0;
		String sql = "DELETE FROM C_AcctProcessorLog "
			+ "WHERE C_AcctProcessor_ID=" + getC_AcctProcessor_ID() 
			//jz + " AND (Created+" + getKeepLogDays() + ") < SysDate";
			+ " AND addDays(Created," + getKeepLogDays() + ") < SysDate";
		int no = DB.executeUpdate(sql, get_Trx());
		return no;
	}	//	deleteLog

}	//	MAcctProcessor
