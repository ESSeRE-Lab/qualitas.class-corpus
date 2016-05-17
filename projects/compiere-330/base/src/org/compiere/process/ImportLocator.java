package org.compiere.process;

import java.math.*;
import java.sql.*;
import java.util.logging.*;

import org.compiere.model.*;
import org.compiere.util.*;

public class ImportLocator extends SvrProcess {

	/**	Client to be imported to		*/
	private int				p_AD_Client_ID = 0;
	/**	Organization to be imported to		*/
	private int				p_AD_Org_ID = 0;
	/**	Delete old Imported				*/
	private boolean			p_deleteOldImported = false;

	@Override
	protected String doIt() throws Exception {
		StringBuffer sql = null;
		int no = 0;
		String clientCheck = " AND AD_Client_ID=" + p_AD_Client_ID;

		//	****	Prepare	****

		//	Delete Old Imported
		if (p_deleteOldImported)
		{
			sql = new StringBuffer ("DELETE FROM I_Locator "
				  + "WHERE I_IsImported='Y'").append (clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			log.fine("Delete Old Impored =" + no);
		}

		//Set Client from Key
		sql = new StringBuffer ("UPDATE I_Locator l"
				  + " SET AD_Client_ID = (SELECT AD_Client_ID FROM AD_Client c WHERE c.Value = l.ClientValue), " 
				  +	" ClientName = (SELECT Name FROM AD_Client c WHERE c.Value = l.ClientValue), "
				  + " Updated = COALESCE (Updated, SysDate),"
				  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND AD_Client_ID is NULL"
				  + " AND ClientValue is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set client from key =" + no);

		//	Set Client from Name
		sql = new StringBuffer ("UPDATE I_Locator l"
				  + " SET AD_Client_ID = (SELECT AD_Client_ID FROM AD_Client c WHERE c.Name = l.ClientName), "
				  + " ClientValue = (SELECT Value FROM AD_Client c WHERE c.Name = l.ClientName),"
				  + " Updated = COALESCE (Updated, SysDate),"
				  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND AD_Client_ID is NULL"
				  + " AND ClientName is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set client from name =" + no);

		//Set Org from Key
		sql = new StringBuffer ("UPDATE I_Locator l"
					  + " SET AD_Org_ID = (SELECT AD_Org_ID FROM AD_Org o WHERE o.Value = l.OrgValue), "
					  + " OrgName = (SELECT Name FROM AD_Org o WHERE o.Value = l.OrgValue), "
					  + " Updated = COALESCE (Updated, SysDate),"
					  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
					  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
					  + " AND (AD_Org_ID is NULL OR AD_Org_ID =0)"
					  + " AND OrgValue is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set org from key =" + no);

		//	Set Org from Name
		sql = new StringBuffer ("UPDATE I_Locator l"
					  + " SET AD_Org_ID = (SELECT AD_Org_ID FROM AD_Org o WHERE o.Name = l.OrgName), "
					  + " OrgValue = (SELECT Value FROM AD_Org o WHERE o.Name = l.OrgName), "
					  + " Updated = COALESCE (Updated, SysDate),"
					  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
					  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
					  + " AND (AD_Org_ID is NULL OR AD_Org_ID =0)"
					  + " AND OrgName is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set Org from name =" + no);


		sql = new StringBuffer ("UPDATE I_Locator l"
			  + " SET AD_Client_ID = COALESCE (AD_Client_ID,").append (p_AD_Client_ID).append ("),"
			  + " AD_Org_ID = COALESCE (AD_Org_ID,").append (p_AD_Org_ID).append ("),"
			  + " IsActive = COALESCE (IsActive, 'Y'),"
			  + " IsDefault = COALESCE (IsDefault, 'N'),"
			  + " IsAvailableToPromise = COALESCE (IsAvailableToPromise, 'Y'),"
			  + " IsAvailableForAllocation = COALESCE (IsAvailableForAllocation, 'Y'),"
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
		sql = new StringBuffer ("UPDATE I_Locator l "
				+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Org, '"
				+ "WHERE (AD_Org_ID IS NULL "
				+ " OR NOT EXISTS (SELECT * FROM AD_Org oo WHERE l.AD_Org_ID=oo.AD_Org_ID AND oo.IsSummary='N' AND oo.IsActive='Y'))"
				+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid Org=" + no);

		//Set Warehouse from Key
		sql = new StringBuffer ("UPDATE I_Locator l"
					  + " SET M_Warehouse_ID = (SELECT M_Warehouse_ID FROM M_Warehouse w WHERE w.Value = l.WarehouseValue), "
					  + " WarehouseName = (SELECT Name FROM M_Warehouse w WHERE w.Value = l.WarehouseValue), "
					  + " Updated = COALESCE (Updated, SysDate),"
					  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
					  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
					  + " AND (M_Warehouse_ID is NULL OR M_Warehouse_ID =0)"
					  + " AND WarehouseValue is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set Warehouse from key =" + no);

		//	Set Warehouse from Name
		sql = new StringBuffer ("UPDATE I_Locator l"
					  + " SET M_Warehouse_ID = (SELECT M_Warehouse_ID FROM M_Warehouse w WHERE w.Name = l.WarehouseName), "
					  + " WarehouseValue = (SELECT Value FROM M_Warehouse w WHERE w.Name = l.WarehouseName), "
					  + " Updated = COALESCE (Updated, SysDate),"
					  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
					  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
					  + " AND (M_Warehouse_ID is NULL OR M_Warehouse_ID =0)"
					  + " AND WarehouseName is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set Warehouse from name =" + no);

		sql = new StringBuffer ("UPDATE I_Locator l "
				+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Warehouse, '"
				+ "WHERE M_Warehouse_ID IS NULL "
				+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid Warehouse=" + no);

		sql = new StringBuffer ("UPDATE I_Locator l "
				+ "SET M_Locator_ID=(SELECT M_Locator_ID FROM M_Locator loc"
				+ " WHERE l.Value=loc.Value " 
				+ " AND l.AD_Client_ID=loc.AD_Client_ID "
				+ " AND l.M_Warehouse_ID=loc.M_Warehouse_ID) "
				+ " WHERE M_Locator_ID IS NULL"
				+ " AND l.Value IS NOT NULL"
				+ " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info("Locator Existing Value=" + no);

		sql = new StringBuffer ("UPDATE I_Locator i "
					+ "SET M_Locator_ID=(SELECT MAX(M_Locator_ID) FROM M_Locator l"
					+ " WHERE i.X=l.X AND i.Y=l.Y AND i.Z=l.Z "
					+ " AND (i.Bin IS NULL OR i.Bin = l.Bin) "
					+ " AND (i.Position IS NULL OR i.Position =l.Position) "
					+ " AND i.M_Warehouse_ID=l.M_Warehouse_ID "
					+ " AND i.AD_Client_ID=l.AD_Client_ID) "
					+ "WHERE M_Locator_ID IS NULL AND X IS NOT NULL AND Y IS NOT NULL AND Z IS NOT NULL"
					+ " AND I_IsImported<>'Y'").append (clientCheck);
				no = DB.executeUpdate (sql.toString (), get_TrxName());
				log.fine("Set Locator from X,Y,Z =" + no);

		sql = new StringBuffer ("UPDATE I_Locator l "
				+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Aisle, Bay and Row segments are mandatory' "
				+ "WHERE I_IsImported<>'Y'"
				+ " AND (M_Locator_ID IS NULL OR M_Locator_ID=0)"
				+ " AND (X IS NULL OR Y IS NULL OR Z IS NULL)")
				.append(clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			if (no != 0)
				log.warning("Missing Aisle, Bay or Row=" + no);

		//Set Picking UOM from Name
		sql = new StringBuffer ("UPDATE I_Locator l"
					  + " SET Picking_UOM_ID = (SELECT C_UOM_ID FROM C_UOM u WHERE u.Name = l.PickingUOMName), "
					  + " PickingUOMSymbol = (SELECT UOMSymbol FROM C_UOM u WHERE u.Name = l.PickingUOMName), "
					  + " Updated = COALESCE (Updated, SysDate),"
					  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
					  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
					  + " AND (Picking_UOM_ID is NULL OR Picking_UOM_ID =0)"
					  + " AND PickingUOMName is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set Picking UOM from name =" + no);

		//	Set Picking UOM from Symbol
		sql = new StringBuffer ("UPDATE I_Locator l"
					  + " SET Picking_UOM_ID = (SELECT C_UOM_ID FROM C_UOM u WHERE u.UOMSymbol = l.PickingUOMSymbol), "
					  + " PickingUOMName = (SELECT Name FROM C_UOM u WHERE u.UOMSymbol = l.PickingUOMSymbol), "
					  + " Updated = COALESCE (Updated, SysDate),"
					  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
					  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
					  + " AND (Picking_UOM_ID is NULL OR Picking_UOM_ID =0)"
					  + " AND PickingUOMSymbol is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set Picking UOM from symbol=" + no);

		sql = new StringBuffer ("UPDATE I_Locator l "
				+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Picking UOM, '"
				+ "WHERE Picking_UOM_ID IS NULL "
				+ " AND (l.PickingUOMName IS NOT NULL OR l.PickingUOMSymbol IS NOT NULL)"
				+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid Picking UOM=" + no);

		//Set Stocking UOM from Name
		sql = new StringBuffer ("UPDATE I_Locator l"
					  + " SET Stocking_UOM_ID = (SELECT C_UOM_ID FROM C_UOM u WHERE u.Name = l.StockingUOMName), "
					  + " StockingUOMSymbol = (SELECT UOMSymbol FROM C_UOM u WHERE u.Name = l.StockingUOMName), "
					  + " Updated = COALESCE (Updated, SysDate),"
					  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
					  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
					  + " AND (Stocking_UOM_ID is NULL OR Stocking_UOM_ID =0)"
					  + " AND StockingUOMName is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set Stocking UOM from name =" + no);

		//	Set Stocking UOM from Symbol
		sql = new StringBuffer ("UPDATE I_Locator l"
					  + " SET Stocking_UOM_ID = (SELECT C_UOM_ID FROM C_UOM u WHERE u.UOMSymbol = l.StockingUOMSymbol), "
					  + " StockingUOMName = (SELECT Name FROM C_UOM u WHERE u.UOMSymbol = l.StockingUOMSymbol), "
					  + " Updated = COALESCE (Updated, SysDate),"
					  + " UpdatedBy = COALESCE (UpdatedBy, 0)"
					  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
					  + " AND (Stocking_UOM_ID is NULL OR Stocking_UOM_ID =0)"
					  + " AND StockingUOMSymbol is NOT NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Set Stocking UOM from symbol=" + no);

		sql = new StringBuffer ("UPDATE I_Locator l "
				+ "SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Stocking UOM, '"
				+ "WHERE Stocking_UOM_ID IS NULL "
				+ " AND (l.StockingUOMName IS NOT NULL OR l.StockingUOMSymbol IS NOT NULL)"
				+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid Stocking UOM=" + no);
		
		//	Set Product from Product Key
		sql = new StringBuffer ("UPDATE I_Locator l"
				  + " SET M_Product_ID=(SELECT M_Product_ID FROM M_Product m"
				  + " WHERE l.ProductValue=m.Value AND l.AD_Client_ID=m.AD_Client_ID ), "
				  + " ProductName =(SELECT Name FROM M_Product m"
				  + " WHERE l.ProductValue=m.Value AND l.AD_Client_ID=m.AD_Client_ID ) "
				  + " WHERE M_Product_ID IS NULL AND ProductValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Product from ProductValue =" + no);

			//	Set Product from Product Name
		sql = new StringBuffer ("UPDATE I_Locator l"
				  + " SET M_Product_ID = (SELECT M_Product_ID FROM M_Product m"
				  + " WHERE l.ProductName=m.Name AND l.AD_Client_ID=m.AD_Client_ID ), "
				  + " ProductValue =(SELECT Value FROM M_Product m"
				  + " WHERE l.ProductName=m.Name AND l.AD_Client_ID=m.AD_Client_ID )"
				  + " WHERE M_Product_ID IS NULL AND ProductName IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.fine("Set Product from ProductValue =" + no);


		// Error - Invalid Product
		sql = new StringBuffer ("UPDATE I_Locator l"
				  + " SET I_IsImported='E', I_ErrorMsg="+ts +"||'ERR=Invalid Product, ' "
				  + " WHERE (I_IsImported<>'Y' OR I_IsImported IS NULL)"
				  + " AND l.M_Product_ID IS NULL"
				  + " AND (l.productName IS NOT NULL OR l.ProductValue is NOT NULL)").append (clientCheck);				  
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning("Invalid Product=" + no);


		commit();

		//	-- New Locators -----------------------------------------------------

		int noInserted = 0;
		int noUpdated = 0;

		//	Go through Locator Records w/o
		sql = new StringBuffer ("SELECT * FROM I_Locator "
				  + "WHERE I_IsImported='N'").append (clientCheck)
					.append(" ORDER BY I_Locator_ID");
		try
		{
			PreparedStatement pstmt = DB.prepareStatement (sql.toString(), get_TrxName());
			ResultSet rs = pstmt.executeQuery ();

			MLocator locator = null;

			while (rs.next ())
			{
				X_I_Locator loc = new X_I_Locator (getCtx(), rs, get_TrxName());
				int M_Locator_ID = loc.getM_Locator_ID();
				
				boolean update = M_Locator_ID==0?false:true;
				
				locator = new MLocator (M_Locator_ID, loc);
				
				// Save Locator
				if (!locator.save())
				{
					String msg = "Could not save Locator";
					ValueNamePair pp = CLogger.retrieveError();
					if (pp != null)
						msg += " - " + pp.toStringX();
					loc.setI_ErrorMsg(msg);
					loc.save();
					continue;
				}
				if(update)
					noUpdated++;
				else
					noInserted++;
				
				loc.setM_Locator_ID(locator.getM_Locator_ID());
				
				if(loc.getM_Product_ID()!=0)
				{
					MProductLocator pl=MProductLocator.getOfProductLocator(getCtx(), loc.getM_Product_ID(), loc.getM_Locator_ID());
					if(pl == null)
						pl= new MProductLocator(getCtx(), loc.getM_Product_ID(), loc.getM_Locator_ID(), get_TrxName());
					pl.setMaxQuantity(loc.getProductMaxQuantity());
					pl.setMinQuantity(loc.getProductMinQuantity());
					pl.save();
				}
				loc.setI_IsImported(X_I_Locator.I_ISIMPORTED_Yes);
				loc.setProcessed(true);
				loc.save();
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Locator - " + sql.toString(), e);
		}

		//Set Error to indicator to not imported
		sql = new StringBuffer ("UPDATE I_Locator "
			+ "SET I_IsImported='N', Updated=SysDate "
			+ "WHERE I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		addLog (0, null, new BigDecimal (no), "@Errors@");
		addLog (0, null, new BigDecimal (noUpdated), " @M_Locator_ID@: @Updated@");
		addLog (0, null, new BigDecimal (noInserted), " @M_Locator_ID@: @Inserted@");
		return "#" + noInserted + "/" + noUpdated;

	}

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
			else if (name.equals("AD_Org_ID"))
				p_AD_Org_ID = ((BigDecimal)element.getParameter()).intValue();
			else if (name.equals("DeleteOldImported"))
				p_deleteOldImported = "Y".equals(element.getParameter());
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

}
