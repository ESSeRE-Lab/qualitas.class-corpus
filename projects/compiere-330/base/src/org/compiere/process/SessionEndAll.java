/**
 *
 */
package org.compiere.process;

import java.util.logging.*;

import org.compiere.*;
import org.compiere.util.*;


/**
 * @author jjanke
 *
 */
public class SessionEndAll extends SvrProcess
{
	/** Session Type			*/
	private String	p_SessionType	= null;

	/**	Logger	*/
    private static CLogger s_log = CLogger.getCLogger(SessionEndAll.class);

	/**
	 * 	Get Parameters
	 *	@see org.compiere.process.SvrProcess#prepare()
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
			else if (name.equals("SessionType"))
				p_SessionType = (String)element.getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare


	/**
	 * 	Kill Sessions
	 *	@see org.compiere.process.SvrProcess#doIt()
	 */
	@Override
	protected String doIt() throws Exception
	{
		log.info("SessionType=" + p_SessionType);
		return endAll(getCtx(), p_SessionType);
	}	//	doIt

	/**
	 * 	End all Sessions of Session type or all
	 *	@param ctx context for AD_Tenant and own session
	 *	@param SessionType optional type
	 *	@return info
	 */
	public static String endAll (Ctx ctx, String SessionType)
	{
		int AD_Session_ID = ctx.getContextAsInt("#AD_Session_ID");
		int AD_Client_ID = ctx.getAD_Client_ID();
		s_log.info("SessionType=" + SessionType
			+ ",AD_Client_ID" + AD_Client_ID
			+ " (own AD_Session_ID=" + AD_Session_ID + ")");

		String sql = "UPDATE AD_Session SET Processed='Y' WHERE Processed='N'";
		if (AD_Client_ID != 0)
			sql += " AND AD_Client_ID=" + AD_Client_ID;
		if (!Util.isEmpty(SessionType))
			sql += " AND SessionType='" + SessionType + "'";
		if (AD_Session_ID != 0)
			sql += " AND AD_Session_ID<>" + AD_Session_ID;

		int counter = DB.executeUpdate(sql, (Trx) null);
		s_log.info("#" + counter);
		return "#" + counter;
	}	//	endAll

	/**
	 * 	End All Sessions for all tenants
	 *	@param args session type
	 */
	public static void main(String[] args)
    {
	    Compiere.startup(true);
	    String st = null;
	    if (args.length > 0)
	    	st = args[0];
	    endAll(Env.getCtx(), st);
    }	//	main

}	//	SessionEndAll
