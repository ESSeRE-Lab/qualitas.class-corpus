package org.compiere.vos;

import java.util.ArrayList;

import org.compiere.vos.ResponseVO;

public class PrintFormatVO extends ResponseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name, description, isActive, isDefault;
	
	private int AD_PrintFormatID = 0,AD_Client_ID=-1;
	
	private ArrayList<PrintFormatItemVO> pf_items = new ArrayList<PrintFormatItemVO>();

	public PrintFormatVO() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<PrintFormatItemVO> getPf_items() {
		return pf_items;
	}

	public void setPf_items(ArrayList<PrintFormatItemVO> pf_items) {
		this.pf_items = pf_items;
	}

	public int getAD_PrintFormatID() {
		return AD_PrintFormatID;
	}

	public void setAD_PrintFormatID(int printFormatID) {
		AD_PrintFormatID = printFormatID;
	}

	public int getAD_Client_ID() {
		return AD_Client_ID;
	}

	public void setAD_Client_ID(int client_ID) {
		AD_Client_ID = client_ID;
	}

}
