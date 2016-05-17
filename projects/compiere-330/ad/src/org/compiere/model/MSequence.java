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
import java.util.concurrent.*;
import java.util.logging.*;

import org.compiere.util.*;

/**
 *	Sequence Model.
 *	@see org.compiere.process.SequenceCheck
 *  @author Jorg Janke
 *  @version $Id: MSequence.java,v 1.3 2006/07/30 00:58:04 jjanke Exp $
 */
public class MSequence extends X_AD_Sequence
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/** Test with transaction						*/
	private static final boolean	TEST_TRX = false;
	/** Log Level for Next ID Call					*/
	private static final Level LOGLEVEL = Level.ALL;

	//initialize all to 0 so nextSeq < endSeq won't be satisfied when Sequence is 1st created 
	private static class Sequence {
		int nextSeq = 0;
		int incrementNo = 0;
		int endSeq = 0;
	}
	private static final ConcurrentHashMap<String,Sequence> s_sequences = new ConcurrentHashMap<String, Sequence>();
	public static void onSystemShutdown() {
		for(Map.Entry<String, Sequence> entry:s_sequences.entrySet()) {
		//}
		//for(String key: s_sequences.keySet()) {
			String[] tokens = entry.getKey().split("\\.");
			String TableName = tokens[1];
			int AD_Client_ID = Integer.parseInt(tokens[0]);
			String selectSQL = "SELECT CurrentNext, CurrentNextSys, IncrementNo, AD_Sequence_ID "
				+ "FROM AD_Sequence "
				+ "WHERE Name=?"
				+ " AND IsActive='Y' AND IsTableID='Y' AND IsAutoSequence='Y' "
				+ " FOR UPDATE";
			Sequence seq = entry.getValue();
			//at this point there should not be a need for syncrhonization, just for safety
			synchronized(seq) {
				Trx trx = Trx.get("MSequence.onSystemShutdown()");
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				try {
					//
					pstmt = trx.getConnection().prepareStatement(selectSQL, ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_UPDATABLE);
					pstmt.setString(1, TableName);
					//
					rs = pstmt.executeQuery();
					if (rs.next()) {
						if (isCompiereSys(AD_Client_ID)) {
							int dbNextSeq = rs.getInt(2);
							// only when db nextseq equals to the jvm endseq then i'll write back. this is so if there are multiple
							//jvms running, i know that other jvms already advanced the sequenes so that i don't mess with it
							if(dbNextSeq == seq.endSeq) {
								seq.endSeq = seq.nextSeq;
								rs.updateInt(2, seq.nextSeq);
							}
						} else {
							int dbNextSeq = rs.getInt(1);
							// only when db nextseq equals to the jvm endseq then i'll write back. this is so if there are multiple
							//jvms running, i know that other jvms already advanced the sequenes so that i don't mess with it
							if(dbNextSeq == seq.endSeq) {
								seq.endSeq = seq.nextSeq;
								rs.updateInt(1, seq.nextSeq);
							}
						}
						rs.updateRow();
					}
				}catch (Exception e) {
					s_log.log(Level.SEVERE, TableName + " - " + e.getMessage(), e);
				} finally {
					if( rs != null )
						try {
							rs.close();
						} catch (SQLException e) {
							s_log.log(Level.SEVERE, "Finish", e);
						}

						if (pstmt != null)
							try {
								pstmt.close();
							} catch (SQLException e) {
								s_log.log(Level.SEVERE, "Finish", e);
							}
							pstmt = null;

							if (trx != null) {
								trx.commit();
								trx.close();
							}
				}
			}
		}
	}
	public static boolean isCompiereSys(int AD_Client_ID) {
		boolean compiereSys = Ini.isPropertyBool(Ini.P_COMPIERESYS);
		if (compiereSys && AD_Client_ID > 11)
			compiereSys = false;
		return compiereSys;
	}
	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 * @param TableName table name
	 *  @return next no or (-1=not found, -2=error)
	 */
	public static int getNextID (int AD_Client_ID, String TableName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("TableName missing");
		int retValue = -1;

		//	Check CompiereSys
		boolean compiereSys = isCompiereSys(AD_Client_ID);

		String hashKey = AD_Client_ID + "." + TableName;
		Sequence seq = s_sequences.get(hashKey);
		if (seq == null) {
			// Standard idiom using putIfAbsent(). This will properly handle
			// multiple simultaneous inserts without resorting to manual locking
			Sequence newSeq = new Sequence();
			seq = s_sequences.putIfAbsent(hashKey, newSeq);
			if (seq == null)
				seq = newSeq;
		}


		synchronized(seq) {
			if(seq.nextSeq < seq.endSeq) {
				retValue = seq.nextSeq;
				seq.nextSeq+=seq.incrementNo;
				return retValue;
			}
			else {
				if (CLogMgt.isLevel(LOGLEVEL))
					s_log.log(LOGLEVEL, TableName + " - CompiereSys=" + compiereSys  + " [" + null + "]");
				String selectSQL = "SELECT CurrentNext, CurrentNextSys, IncrementNo, AD_Sequence_ID "
					+ "FROM AD_Sequence "
					+ "WHERE Name=?"
					+ " AND IsActive='Y' AND IsTableID='Y' AND IsAutoSequence='Y' "
					+ " FOR UPDATE";


				Trx trx = Trx.get("MSequence.getNextID()");
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				try {
					//
					pstmt = trx.getConnection().prepareStatement(selectSQL, ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_UPDATABLE);
					pstmt.setString(1, TableName);
					//
					rs = pstmt.executeQuery();
					if (rs.next()) {
						// int AD_Sequence_ID = rs.getInt(4);
						//
						seq.incrementNo = rs.getInt(3);

						if (compiereSys) {
							retValue = rs.getInt(2);
							seq.nextSeq = retValue + seq.incrementNo;
							// if system, then no gap, since system sequence has
							// a smaller ceiling
							seq.endSeq = retValue + 1 * seq.incrementNo;

							rs.updateInt(2, seq.endSeq);
						} else {
							retValue = rs.getInt(1);
							seq.nextSeq = retValue + seq.incrementNo;
							seq.endSeq = retValue + 100 * seq.incrementNo;

							rs.updateInt(1, seq.endSeq);
						}
						rs.updateRow();
					} else
						s_log.severe("No record found - " + TableName);
					//
				} catch (Exception e) {
					s_log.log(Level.SEVERE, TableName + " - " + e.getMessage(), e);
				} finally {
					if( rs != null )
						try {
							rs.close();
						} catch (SQLException e) {
							s_log.log(Level.SEVERE, "Finish", e);
						}

						if (pstmt != null)
							try {
								pstmt.close();
							} catch (SQLException e) {
								s_log.log(Level.SEVERE, "Finish", e);
							}
							pstmt = null;

							if (trx != null) {
								trx.commit();
								trx.close();
							}
				}

				s_log.finest (retValue + " - Table=" + TableName + " [" + null + "]");
				return retValue;
			}
		}
	}	// getNextID

	/***************************************************************************
	 * Get Document No from table
	 * 
	 * @param AD_Client_ID
	 *            client
	 * @param TableName
	 *            table name
	 * @param trx
	 *            optional Transaction Name
	 * @return document no or null
	 */
	public static String   getDocumentNo (int AD_Client_ID, String TableName, final Trx trx)
	{
		if (Util.isEmpty(TableName))
			throw new IllegalArgumentException("TableName missing");

		Trx localTrx = trx;
		if (TableName.equals(X_M_MatchInv.Table_Name)
				|| TableName.equals(X_M_MatchPO.Table_Name))
			localTrx = null;

		//	Check CompiereSys
		boolean compiereSys = Ini.isPropertyBool(Ini.P_COMPIERESYS);
		if (compiereSys && AD_Client_ID > 11)
			compiereSys = false;
		//
		if (CLogMgt.isLevel(LOGLEVEL))
			s_log.log(LOGLEVEL, TableName + " - CompiereSys=" + compiereSys  + " [" + localTrx + "]");
		String selectSQL = "SELECT CurrentNext, CurrentNextSys, IncrementNo, Prefix, Suffix, AD_Sequence_ID "
			+ "FROM AD_Sequence "
			+ "WHERE Name=?"
			//jz fix duplicated nextID  + " AND AD_Client_ID IN (0,?)"
			+ " AND AD_Client_ID = ?"
			+ " AND IsActive='Y' AND IsTableID='N' AND IsAutoSequence='Y' ";
		if (DB.isOracle())
			selectSQL += " ORDER BY AD_Client_ID DESC ";
		selectSQL +=  "FOR UPDATE";

		if(localTrx == null)
			localTrx = Trx.get("MSequence.getDocumentNo()");

		PreparedStatement pstmt = null;
		ResultSet rs = null; 

		int incrementNo = 0;
		int next = -1;
		String prefix = "";
		String suffix = "";
		try
		{
			//	Error
			//
			pstmt = localTrx.getConnection().prepareStatement(selectSQL,
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setString(1, PREFIX_DOCSEQ + TableName);
			pstmt.setInt(2, AD_Client_ID);
			//
			rs = pstmt.executeQuery();
			//	s_log.fine("AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation()
			//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				//	AD_Sequence_ID = rs.getInt(6);
				prefix = rs.getString(4);
				suffix = rs.getString(5);
				incrementNo = rs.getInt(3);

				if (compiereSys)
				{
					next = rs.getInt(2);
					rs.updateInt(2, next + incrementNo);
				}
				else
				{
					next = rs.getInt(1);
					rs.updateInt(1, next + incrementNo);
				}
				rs.updateRow();
			}
			else
			{
				s_log.warning ("(Table) - no record found - " + TableName);
				next = MSequence.getNextID(AD_Client_ID, TableName);
			}
			//	Commit
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, "(Table) [" + localTrx + "]", e);
			next = -2;
		}
		//	Finish
		finally
		{
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					s_log.log(Level.SEVERE, "(Table) - finish", e);
				}
				rs = null;

				if (pstmt != null)
					try {
						pstmt.close();
					} catch (SQLException e) {
						s_log.log(Level.SEVERE, "(Table) - finish", e);
					}
					pstmt = null;

					if( localTrx != null && localTrx != trx )
					{
						localTrx.commit();
						localTrx.close();
					}
		}

		//	Error
		if (next < 0)
			return null;

		//	create DocumentNo
		StringBuffer doc = new StringBuffer();
		if (prefix != null && prefix.length() > 0)
			doc.append(prefix);
		doc.append(next);
		if (suffix != null && suffix.length() > 0)
			doc.append(suffix);
		String documentNo = doc.toString();
		s_log.finer (documentNo + " (" + incrementNo + ")"
				+ " - Table=" + TableName + " [" + localTrx + "]");
		return documentNo;
	}	//	getDocumentNo

	/**
	 * 	Get Document No based on Document Type
	 *	@param C_DocType_ID document type
	 * 	@param trx optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo (int C_DocType_ID, final Trx trx)
	{
		if (C_DocType_ID == 0)
		{
			s_log.severe ("C_DocType_ID=0");
			return null;
		}
		MDocType dt = MDocType.get (Env.getCtx(), C_DocType_ID);	//	wrong for SERVER, but r/o
		if (dt != null && !dt.isDocNoControlled())
		{
			s_log.finer("DocType_ID=" + C_DocType_ID + " Not DocNo controlled");
			return null;
		}
		if (dt == null || dt.getDocNoSequence_ID() == 0)
		{
			s_log.warning ("No Sequence for DocType - " + dt);
			return null;
		}
		Trx localTrx = trx;
		String docBaseType = dt.getDocBaseType();
		if (MDocBaseType.DOCBASETYPE_MatchInvoice.equals(docBaseType)
				|| MDocBaseType.DOCBASETYPE_MatchInvoice.equals(docBaseType))
			localTrx = null;

		//	Check CompiereSys
		boolean compiereSys = Ini.isPropertyBool(Ini.P_COMPIERESYS);
		if (CLogMgt.isLevel(LOGLEVEL))
			s_log.log(LOGLEVEL, "DocType_ID=" + C_DocType_ID + " [" + localTrx + "]");
		String selectSQL = "SELECT CurrentNext, CurrentNextSys, IncrementNo, Prefix, Suffix, AD_Client_ID, AD_Sequence_ID "
			+ "FROM AD_Sequence "
			+ "WHERE AD_Sequence_ID=?"
			+ " AND IsActive='Y' AND IsTableID='N' AND IsAutoSequence='Y' "
			+ " FOR UPDATE";

		if( localTrx == null )
			localTrx = Trx.get("MSequence.getDocumentNo()");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		//
		int incrementNo = 0;
		int next = -1;
		String prefix = "";
		String suffix = "";
		try
		{
			//
			pstmt = localTrx.getConnection().prepareStatement(selectSQL,
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			pstmt.setInt(1, dt.getDocNoSequence_ID());
			//
			rs = pstmt.executeQuery();
			//	s_log.fine("AC=" + conn.getAutoCommit() + " -Iso=" + conn.getTransactionIsolation()
			//		+ " - Type=" + pstmt.getResultSetType() + " - Concur=" + pstmt.getResultSetConcurrency());
			if (rs.next())
			{
				incrementNo = rs.getInt(3);
				prefix = rs.getString(4);
				suffix = rs.getString(5);
				int AD_Client_ID = rs.getInt(6);
				if (compiereSys && AD_Client_ID > 11)
					compiereSys = false;
				//	AD_Sequence_ID = rs.getInt(7);
				if (compiereSys)
				{
					next = rs.getInt(2);
					rs.updateInt(2, next + incrementNo);
				}
				else
				{
					next = rs.getInt(1);
					rs.updateInt(1, next + incrementNo);
				}
				rs.updateRow();
			}
			else
			{
				s_log.warning ("(DocType)- no record found - " + dt);
				next = -2;
			}
			rs.close();
			pstmt.close();
			pstmt = null;
			//	Commit
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, "(DocType) [" + localTrx + "]", e);
			next = -2;
		}
		//	Finish
		finally
		{
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					s_log.log(Level.SEVERE, "(Table) - finish", e);
				}
				rs = null;

				if (pstmt != null)
					try {
						pstmt.close();
					} catch (SQLException e) {
						s_log.log(Level.SEVERE, "(Table) - finish", e);
					}
					pstmt = null;

					if( localTrx != null && localTrx != trx )
					{
						localTrx.commit();
						localTrx.close();
					}
		}
		//	Error
		if (next < 0)
			return null;

		//	create DocumentNo
		StringBuffer doc = new StringBuffer();
		if (prefix != null && prefix.length() > 0)
			doc.append(prefix);
		doc.append(next);
		if (suffix != null && suffix.length() > 0)
			doc.append(suffix);
		String documentNo = doc.toString();
		s_log.finer (documentNo + " (" + incrementNo + ")"
				+ " - C_DocType_ID=" + C_DocType_ID + " [" + localTrx + "]");
		return documentNo;
	}	//	getDocumentNo


	/**************************************************************************
	 *	Check/Initialize Client DocumentNo/Value Sequences
	 *	@param ctx context
	 *	@param AD_Client_ID client
	 *	@param trx transaction
	 *	@return true if no error
	 */
	public static boolean checkClientSequences (Ctx ctx, int AD_Client_ID, Trx trx)
	{
		String sql = "SELECT TableName "
			+ "FROM AD_Table t "
			+ "WHERE IsActive='Y' AND IsView='N'"
			//	Get all Tables with DocumentNo or Value
			+ " AND AD_Table_ID IN "
			+ "(SELECT AD_Table_ID FROM AD_Column "
			+ "WHERE ColumnName = 'DocumentNo' OR ColumnName = 'Value')"
			//	Ability to run multiple times
			+ " AND 'DocumentNo_' || TableName NOT IN "
			+ "(SELECT Name FROM AD_Sequence s "
			+ "WHERE s.AD_Client_ID=?)";
		int counter = 0;
		boolean success = true;
		//
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, trx);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				String tableName = rs.getString(1);
				s_log.fine("Add: " + tableName);
				MSequence seq = new MSequence (ctx, AD_Client_ID, tableName, trx);
				if (seq.save())
					counter++;
				else
				{
					s_log.severe ("Not created - AD_Client_ID=" + AD_Client_ID
							+ " - "  + tableName);
					success = false;
				}
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		s_log.info ("AD_Client_ID=" + AD_Client_ID
				+ " - created #" + counter
				+ " - success=" + success);
		return success;
	}	//	checkClientSequences


	/**
	 * 	Create Table ID Sequence
	 * 	@param ctx context
	 * 	@param TableName table name
	 *	@param trx transaction
	 * 	@return true if created
	 */
	public static boolean createTableSequence (Ctx ctx, String TableName, Trx trx)
	{
		MSequence seq = new MSequence (ctx, 0, trx);
		seq.setClientOrg(0, 0);
		seq.setName(TableName);
		seq.setDescription("Table " + TableName);
		seq.setIsTableID(true);
		return seq.save();
	}	//	createTableSequence

	/**
	 * 	Delete Table ID Sequence
	 * 	@param ctx context
	 * 	@param TableName table name
	 *	@param trx transaction
	 * 	@return true if created
	 */
	public static boolean deleteTableSequence (Ctx ctx, String TableName, Trx trx)
	{
		MSequence seq = get (ctx, TableName, trx);
		return seq.delete(true);
	}	//	deleteTableSequence

	/**
	 * 	Get Table Sequence
	 *	@param ctx context
	 *	@param tableName table name
	 *	@return Sequence
	 */
	public static MSequence get (Ctx ctx, String tableName, Trx trx)
	{
		String sql = "SELECT * FROM AD_Sequence "
			+ "WHERE UPPER(Name)=?"
			+ " AND IsTableID='Y'";
		MSequence retValue = null;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trx);
			pstmt.setString (1, tableName.toUpperCase());
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
				retValue = new MSequence (ctx, rs, trx);
			if (rs.next())
				s_log.log(Level.SEVERE, "More then one sequence for " + tableName);
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e);
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
		return retValue;
	}	//	get


	/**	Sequence for Table Document No's	*/
	private static final String	PREFIX_DOCSEQ = "DocumentNo_";
	/**	Start Number			*/
	public static final int		INIT_NO = 1000000;	//	1 Mio
	/**	Start System Number		*/
	public static final int		INIT_SYS_NO = 100;
	/** Static Logger			*/
	private static CLogger 		s_log = CLogger.getCLogger(MSequence.class);


	/**************************************************************************
	 *	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Sequence_ID id
	 *	@param trx transaction
	 */
	public MSequence (Ctx ctx, int AD_Sequence_ID, Trx trx)
	{
		super(ctx, AD_Sequence_ID, trx);
		if (AD_Sequence_ID == 0)
		{
			//	setName (null);
			//
			setIsTableID(false);
			setStartNo (INIT_NO);
			setCurrentNext (INIT_NO);
			setCurrentNextSys (INIT_SYS_NO);
			setIncrementNo (1);
			setIsAutoSequence (true);
			setIsAudited(false);
			setStartNewYear(false);
		}
	}	//	MSequence

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trx transaction
	 */
	public MSequence (Ctx ctx, ResultSet rs, Trx trx)
	{
		super(ctx, rs, trx);
	}	//	MSequence

	/**
	 * 	New Document Sequence Constructor
	 *	@param ctx context
	 *	@param AD_Client_ID owner
	 *	@param tableName name
	 *	@param trx transaction
	 */
	public MSequence (Ctx ctx, int AD_Client_ID, String tableName, Trx trx)
	{
		this (ctx, 0, trx);
		setClientOrg(AD_Client_ID, 0);			//	Client Ownership
		setName(PREFIX_DOCSEQ + tableName);
		setDescription("DocumentNo/Value for Table " + tableName);
	}	//	MSequence;

	/**
	 * 	New Document Sequence Constructor
	 *	@param ctx context
	 *	@param AD_Client_ID owner
	 *	@param sequenceName name
	 *	@param StartNo start
	 *	@param trx p_trx
	 */
	public MSequence (Ctx ctx, int AD_Client_ID, String sequenceName, int StartNo, Trx trx)
	{
		this (ctx, 0, trx);
		setClientOrg(AD_Client_ID, 0);			//	Client Ownership
		setName(sequenceName);
		setDescription(sequenceName);
		setStartNo(StartNo);
		setCurrentNext(StartNo);
		setCurrentNextSys(StartNo/10);
	}	//	MSequence;



	/**
	 * 	Validate Table Sequence Values
	 *	@return true if updated
	 */
	public boolean validateTableIDValue()
	{
		if (!isTableID())
			return false;
		String tableName = getName();
		int AD_Column_ID = DB.getSQLValue(null, "SELECT MAX(c.AD_Column_ID) "
				+ "FROM AD_Table t"
				+ " INNER JOIN AD_Column c ON (t.AD_Table_ID=c.AD_Table_ID) "
				+ "WHERE t.TableName=?"
				+ " AND c.ColumnName=?", new Object[]{tableName, tableName+"_ID"});
		if (AD_Column_ID <= 0)
			return false;
		//
		MSystem system = MSystem.get(getCtx());
		int IDRangeEnd = 0;
		if (system.getIDRangeEnd() != null)
			IDRangeEnd = system.getIDRangeEnd().intValue();
		boolean change = false;
		String info = null;

		//	Current Next
		String sql = "SELECT MAX(" + tableName + "_ID) FROM " + tableName;
		if (IDRangeEnd > 0)
			sql += " WHERE " + tableName + "_ID < " + IDRangeEnd;
		int maxTableID = DB.getSQLValue(null, sql);
		if (maxTableID < INIT_NO)
			maxTableID = INIT_NO - 1;
		maxTableID++;		//	Next
		if (getCurrentNext() < maxTableID)
		{
			setCurrentNext(maxTableID);
			info = "CurrentNext=" + maxTableID;
			change = true;
		}

		//	Get Max System_ID used in Table
		sql = "SELECT MAX(" + tableName + "_ID) FROM " + tableName
		+ " WHERE " + tableName + "_ID < " + INIT_NO;
		int maxTableSysID = DB.getSQLValue(null, sql);
		if (maxTableSysID <= 0)
			maxTableSysID = INIT_SYS_NO - 1;
		maxTableSysID++;	//	Next
		if (getCurrentNextSys() < maxTableSysID)
		{
			setCurrentNextSys(maxTableSysID);
			if (info == null)
				info = "CurrentNextSys=" + maxTableSysID;
			else
				info += " - CurrentNextSys=" + maxTableSysID;
			change = true;
		}
		if (info != null)
			log.config(getName() + " - " + info);
		return change;
	}	//	validate


	/**************************************************************************
	 *	Test
	 *	@param args ignored
	 */
	static public void main (String[] args)
	{
		org.compiere.Compiere.startup(true);
		CLogMgt.setLevel(Level.SEVERE);
		CLogMgt.setLoggerLevel(Level.SEVERE, null);
		s_list = new Vector<Integer>(1000);

		/**	Lock Test Start **
		Trx trx = "test";
		System.out.println(DB.getDocumentNo(115, trx));
		System.out.println(DB.getDocumentNo(116, trx));
		System.out.println(DB.getDocumentNo(117, trx));
		System.out.println(DB.getDocumentNo(118, trx));
		System.out.println(DB.getDocumentNo(118, trx));
		System.out.println(DB.getDocumentNo(117, trx));

		/**	Lock Test **
		trx = "test1";
		System.out.println(DB.getDocumentNo(115, trx));	//	hangs here as supposed
		System.out.println(DB.getDocumentNo(116, trx));
		System.out.println(DB.getDocumentNo(117, trx));
		System.out.println(DB.getDocumentNo(118, trx));
		/** **/

		/** Time Test	*/
		long time = System.currentTimeMillis();
		Thread[] threads = new Thread[10];
		for (int i = 0; i < 10; i++)
		{
			Runnable r = new GetIDs(i);
			threads[i] = new Thread(r);
			threads[i].start();
		}
		for (int i = 0; i < 10; i++)
		{
			try
			{
				threads[i].join();
			}
			catch (InterruptedException e)
			{
			}
		}
		time = System.currentTimeMillis() - time;

		System.out.println("-------------------------------------------");
		System.out.println("Size=" + s_list.size() + " (should be 1000)");
		Integer[] ia = new Integer[s_list.size()];
		s_list.toArray(ia);
		Arrays.sort(ia);
		Integer last = null;
		int duplicates = 0;
		for (Integer element : ia) {
			if (last != null)
			{
				if (last.compareTo(element) == 0)
				{
					//	System.out.println(i + ": " + ia[i]);
					duplicates++;
				}
			}
			last = element;
		}
		System.out.println("-------------------------------------------");
		System.out.println("Size=" + s_list.size() + " (should be 1000)");
		System.out.println("Duplicates=" + duplicates);
		System.out.println("Time (ms)=" + time + " - " + (float)time/s_list.size() + " each" );
		System.out.println("-------------------------------------------");



		/** **
		try
		{
			int retValue = -1;
			Connection conn = DB.getConnectionRW ();
		//	DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
		//	Connection conn = DriverManager.getConnection ("jdbc:oracle:thin:@//dev2:1521/dev2", "compiere", "compiere");

			conn.setAutoCommit(false);
			String sql = "SELECT CurrentNext, CurrentNextSys, IncrementNo "
				+ "FROM AD_Sequence "
				+ "WHERE Name='AD_Sequence' ";
			sql += "FOR UPDATE";
			//	creates ORA-00907: missing right parenthesis
		//	sql += "FOR UPDATE OF CurrentNext, CurrentNextSys";


			PreparedStatement pstmt = conn.prepareStatement(sql,
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = pstmt.executeQuery();
			System.out.println("AC=" + conn.getAutoCommit() + ", RO=" + conn.isReadOnly()
				+ " - Isolation=" + conn.getTransactionIsolation() + "(" + Connection.TRANSACTION_READ_COMMITTED
				+ ") - RSType=" + pstmt.getResultSetType() + "(" + ResultSet.TYPE_SCROLL_SENSITIVE
				+ "), RSConcur=" + pstmt.getResultSetConcurrency() + "(" + ResultSet.CONCUR_UPDATABLE
				+ ")");

			if (rs.next())
			{
				int IncrementNo = rs.getInt(3);
				retValue = rs.getInt(1);
				rs.updateInt(1, retValue + IncrementNo);
				rs.updateRow();
			}
			else
				s_log.severe ("no record found");
			rs.close();
			pstmt.close();
			conn.commit();
			conn.close();
			//
			System.out.println("Next=" + retValue);

		}
		catch (Exception e)
		{
			e.printStackTrace ();
		}

		System.exit(0);
		 */

		int AD_Client_ID = 0;
		int C_DocType_ID = 115;	//	GL
		String TableName = "C_Invoice";
		Trx trx = Trx.get("x");
		Trx p_trx = trx;

		System.out.println ("none " + getNextID (0, "Test"));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getNextID (0, "Test"));
		System.out.println ("trx2 " + getNextID (0, "Test"));
		//	p_trx.rollback();
		System.out.println ("trx3 " + getNextID (0, "Test"));
		//	p_trx.commit();
		System.out.println ("trx4 " + getNextID (0, "Test"));
		//	p_trx.rollback();
		//	p_trx.close();
		System.out.println ("----------------------------------------------");
		System.out.println ("none " + getNextID (0, "Test"));
		System.out.println ("==============================================");

		p_trx = trx;
		System.out.println ("none " + getDocumentNo(AD_Client_ID, TableName, null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getDocumentNo(AD_Client_ID, TableName, trx));
		System.out.println ("trx2 " + getDocumentNo(AD_Client_ID, TableName, trx));
		p_trx.rollback();
		System.out.println ("trx3 " + getDocumentNo(AD_Client_ID, TableName, trx));
		p_trx.commit();
		System.out.println ("trx4 " + getDocumentNo(AD_Client_ID, TableName, trx));
		p_trx.rollback();
		p_trx.close();
		System.out.println ("----------------------------------------------");
		System.out.println ("none " + getDocumentNo(AD_Client_ID, TableName, null));
		System.out.println ("==============================================");


		p_trx = trx;
		System.out.println ("none " + getDocumentNo(C_DocType_ID, null));
		System.out.println ("----------------------------------------------");
		System.out.println ("trx1 " + getDocumentNo(C_DocType_ID, trx));
		System.out.println ("trx2 " + getDocumentNo(C_DocType_ID, trx));
		p_trx.rollback();
		System.out.println ("trx3 " + getDocumentNo(C_DocType_ID, trx));
		p_trx.commit();
		System.out.println ("trx4 " + getDocumentNo(C_DocType_ID, trx));
		p_trx.rollback();
		p_trx.close();
		System.out.println ("----------------------------------------------");
		System.out.println ("none " + getDocumentNo(C_DocType_ID, null));
		System.out.println ("==============================================");
		/** **/
	}	//	main

	/** Test		*/
	static Vector<Integer> s_list = null;

	/**
	 * 	Test Sequence - Get IDs
	 *
	 *  @author Jorg Janke
	 *  @version $Id: MSequence.java,v 1.3 2006/07/30 00:58:04 jjanke Exp $
	 */
	public static class GetIDs implements Runnable
	{
		/**
		 * 	Get IDs
		 *	@param i
		 */
		public GetIDs (int i)
		{
			m_i = i;
		}
		/** Instance	*/
		private int 	m_i;
		private int		m_errors = 0;
		private int		m_no = 0;

		private	Trx		m_trx = null;

		/**
		 * 	Run
		 */
		public void run()
		{
			System.out.println("Run #" + m_i + " - started");
			if (TEST_TRX)
			{
				m_trx = Trx.get ("Number" + m_i);
				m_trx.getConnection();
			}


			for (int i = 0; i < 100; i++)
			{
				try
				{
					int no = DB.getNextID(0, "Test", m_trx);
					if (m_trx != null)
						m_trx.commit();
					//
					s_list.add(Integer.valueOf(no));
					m_no++;
					//	System.out.println("#" + m_i + ": " + no);
				}
				catch (Exception e)
				{
					m_errors++;
					System.err.println("#" + m_i + "-" + m_errors
							+ ": " + e.toString());
				}
			}
			if (m_trx != null)
				m_trx.close();
			System.out.println("Run #" + m_i
					+ " - complete - Errors=" + m_errors
					+ " - Created=" + m_no);
		}	//	run

		/**
		 * 	Info
		 *	@return info
		 */
		@Override
		public String toString()
		{
			return "GetID #" + m_i + " - Size=" + s_list.size();
		}
	}	//	GetIDs

}	//	MSequence
