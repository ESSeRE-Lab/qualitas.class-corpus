/**
 *
 */
package org.compiere.framework;

import org.compiere.common.*;
import org.compiere.common.constants.*;
import org.compiere.model.*;
import org.compiere.util.*;


/**
 * 	Compiere Unique ID
 *	@author Jorg Janke
 */
public class UniqueID
{
	/**
	 * 	Return Array: Identifier, Unique ID
	 *	@param columnName column name
	 *	@param AD_Reference_ID reference
	 *	@param AD_Reference_Value_ID reference value
	 *	@param localID id
	 *	@return null or info for ID
	 */
	static String[] getInfo (String columnName, int AD_Reference_ID, int AD_Reference_Value_ID, Object localID)
	{
		if (localID == null)
			return null;
		String identifier = getIdentifier(columnName, AD_Reference_ID, AD_Reference_Value_ID, localID);
		if (identifier == null)
			return null;
		String uniqueID = getUniqueID(columnName, AD_Reference_ID, AD_Reference_Value_ID, localID);
		return new String[]{identifier, uniqueID};
	}	//	getInfo


	/**
	 * 	Return Identifier
	 *	@param columnName column name
	 *	@param AD_Reference_ID reference
	 *	@param AD_Reference_Value_ID reference value
	 *	@param localID id
	 *	@return null or identifier for ID
	 */
	static String getIdentifier (String columnName, int AD_Reference_ID, int AD_Reference_Value_ID, Object localID)
	{
		if (localID == null)
			return null;
		//
		Ctx ctx = Env.getCtx();
		if (AD_Reference_ID == DisplayTypeConstants.List)
		{
			MRefList list = MRefList.get(ctx, AD_Reference_Value_ID, localID.toString(), null);
			if (list != null)
				return list.getName();
			else
			{
				log.warning("Not found (List) " + columnName + "=" + localID);
				return null;
			}
		}
		else
		{
			if (!(localID instanceof Integer))
			{
				log.warning("ID not Integer - " + localID.getClass().getName()
					+ " - " + columnName + "=" + localID);
				return null;
			}
			int id = ((Integer)localID).intValue();

			if (AD_Reference_ID == DisplayTypeConstants.Table)
			{
				MRefTable refTable = MRefTable.get(ctx, AD_Reference_Value_ID);
				MTable table = MTable.get(ctx, refTable.getAD_Table_ID());
				return getIdentifier(table, id);
			}
			else if ((AD_Reference_ID == DisplayTypeConstants.Search) || (AD_Reference_ID == DisplayTypeConstants.TableDir))
			{
				if (!columnName.endsWith("_ID"))
				{
					log.warning("ColumnName does not end with _ID - " + columnName);
					return null;
				}
				String tableName = columnName.substring(0, columnName.length()-3);
				MTable table = MTable.get(ctx, tableName);
				return getIdentifier(table, id);
			}
		}
		log.warning("Not supported: " + columnName + "=" + localID);
		return null;
	}	//	getIdentifier

	/**
	 * 	Return Identifier
	 *	@param table table
	 *	@param Record_ID id
	 *	@return null or identifier for ID
	 */
	static String getIdentifier (MTable table, int Record_ID)
	{
		Ctx ctx = Env.getCtx();
		PO po = table.getPO(ctx, Record_ID, null);
		if (po == null)
		{
			log.warning("Not found - Table=" + table.get_TableName() + ", ID=" + Record_ID);
			return null;
		}
		//
		String[] identifierColumns = table.getIdentifierColumns();
		StringBuffer sb = new StringBuffer();
		for (String columnName : identifierColumns)
		{
        	if (sb.length() > 0)
        		sb.append("_");
	        MColumn column = table.getColumn(columnName);
	        int displayType = column.getAD_Reference_ID();
	        //	Recursive
	        if ((displayType != DisplayTypeConstants.ID) && FieldType.isID(displayType))
	        {
	        	Object value = po.get_Value(columnName);
	        	String id = getIdentifier(columnName, column.getAD_Reference_ID(), column.getAD_Reference_Value_ID(), value);
	        	sb.append(id);
	        }
	        else
	        {
	        	Object value = po.get_Value(columnName);
	        	sb.append(value);
	        }
        }
		return sb.toString();
	}	//	getIdentifier

	/**
	 * 	Return Unique ID
	 *	@param columnName column name
	 *	@param AD_Reference_ID reference
	 *	@param AD_Reference_Value_ID reference value
	 *	@param localID id
	 *	@return null or unique id
	 */
	static String getUniqueID (String columnName, int AD_Reference_ID, int AD_Reference_Value_ID, Object localID)
	{
		if (localID == null)
			return null;
		//
		Ctx ctx = Env.getCtx();
		if (AD_Reference_ID == DisplayTypeConstants.List)
		{
			MRefList list = MRefList.get(ctx, AD_Reference_Value_ID, localID.toString(), null);
			if (list != null)
			{
				MReference ref = MReference.get(ctx, AD_Reference_Value_ID);
				return ref.getName() + "." + list.getValue();
			}
			else
			{
				log.warning("Not found (List) " + columnName + "=" + localID);
				return null;
			}
		}
		else
		{
			if (!(localID instanceof Integer))
			{
				if ((localID instanceof String) && columnName.equals("AD_Language"))
					return (String)localID;
				log.warning("ID not Integer - " + localID.getClass().getName()
					+ " - " + columnName + "=" + localID);
				return null;
			}
			int id = ((Integer)localID).intValue();

			if (AD_Reference_ID == DisplayTypeConstants.Table)
			{
				MRefTable refTable = MRefTable.get(ctx, AD_Reference_Value_ID);
				MTable table = MTable.get(ctx, refTable.getAD_Table_ID());
				return getUniqueID(table, id);
			}
			else if ((AD_Reference_ID == DisplayTypeConstants.Search) || (AD_Reference_ID == DisplayTypeConstants.TableDir))
			{
				if (!columnName.endsWith("_ID"))
				{
					log.warning("ColumnName does not end with _ID - " + columnName);
					return null;
				}
				String tableName = columnName.substring(0, columnName.length()-3);
				MTable table = MTable.get(ctx, tableName);
				return getUniqueID(table, id);
			}
		}
		log.warning("Not supported: " + columnName + "=" + localID);
		return null;
	}	//	getUniqueID

	/**
	 * 	Return Unique ID for record
	 *	@param table table
	 *	@param Record_ID id
	 *	@return null or unique ID
	 */
	static String getUniqueID (MTable table, int Record_ID)
	{
		Ctx ctx = Env.getCtx();
		PO po = table.getPO(ctx, Record_ID, null);
		if (po == null)
		{
			log.warning("Not found - Table=" + table.get_TableName() + ", ID=" + Record_ID);
			return null;
		}
		return getUniqueID(table, po);
	}	//	getUniqueID

	/**
	 * 	Return Unique ID for PO
	 *	@param table table
	 *	@param po po
	 *	@return null or unique ID
	 */
	public static String getUniqueID (MTable table, PO po)
	{
		String[] columns = table.getUniqueIDColumns();
		StringBuffer sb = new StringBuffer();
		for (String columnName : columns)
		{
        	if (sb.length() > 0)
        		sb.append(".");
	        MColumn column = table.getColumn(columnName);
	        int displayType = column.getAD_Reference_ID();
	        //	Recursive
	        if ((displayType != DisplayTypeConstants.ID) && FieldType.isID(displayType))
	        {
	        	Object value = po.get_Value(columnName);
	        	String id = getUniqueID(columnName, column.getAD_Reference_ID(), column.getAD_Reference_Value_ID(), value);
	        	sb.append(id);
	        }
	        else
	        {
	        	Object value = po.get_Value(columnName);
	        	sb.append(value);
	        }
        }
		return sb.toString();
	}	//	getUniqueID


	/**	Logger	*/
    private static CLogger log = CLogger.getCLogger(UniqueID.class);

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
	}	//	main

}	//	UniqueID
