package org.compiere.vos;

import org.compiere.util.*;

public class ProductInfo extends NamePair
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ProductInfo(){}
	/**
	 * 	ProductInfo
	 *	
	 */
	public ProductInfo (int newM_Product_ID, 
		String newName,String newTitle,String newGroupName,String newBomType,String newQuantity,
		String newSupplyType, int newLocatorID, String newFieldIdentifier)
	{
		super(newName);
		M_Product_ID = newM_Product_ID;		
		Name = newName;
		title = newTitle;
		groupName = newGroupName;
		bomType = newBomType;
		quantity = newQuantity;
		supplyType = newSupplyType;
		locatorID = newLocatorID;
		fieldIdentifier = newFieldIdentifier;
	}
	public int M_Product_ID;
	public String Name;
	public String title; 
	public String groupName;
	public String bomType;
	public String quantity;
	public String supplyType;
	public int locatorID;
	public String fieldIdentifier;  // identifer composed of bom component ID + bom level

	/**
	 * 	to String
	 *	@return infoint
	 */
	@Override
	public String toString()
	{
		return Name;
	}

	
	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return Integer.toString(M_Product_ID);
	}
	
	public int getM_Product_ID() {
		return M_Product_ID;
	}
	
	public String getGroupName(){
		return groupName;
	}
	
	public String getTitle(){
		return title;
	}
	
	/*public String getName(){
		return Name;
	}*/
	
	public String getBomType(){
		return bomType;
	}
	
	public String getQuantity(){
		return quantity;
	}
	
	public String getSupplyType(){
		return supplyType;
	}
	
	public int getLocatorID(){
		return locatorID;
	}
	
	public String getFieldIdentifier(){
		return fieldIdentifier;
	}
}   //  ProductInfo


