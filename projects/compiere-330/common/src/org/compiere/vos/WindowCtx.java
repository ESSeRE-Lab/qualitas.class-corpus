/**
 * 
 */
package org.compiere.vos;

import java.io.*;
import java.util.*;

import org.compiere.util.*;

/**
 * @author gwu
 *
 */
public class WindowCtx implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private HashMap<String,String> m_strMap;
	private HashMap< Integer, ArrayList<String[]>> m_selectedRows;
	private HashMap< Integer, ArrayList<Integer>> m_selectedRowNos;
	private HashMap< String, NamePair > m_selectedOptions;
	public final static String LICENSE_INFO = "#CompiereLicenseInfo"; 
	
	public WindowCtx()
	{
		m_strMap = null;		
		m_selectedRowNos = new HashMap<Integer, ArrayList<Integer>>();
		m_selectedRows = new HashMap<Integer, ArrayList<String[]>>();
		m_selectedOptions = new HashMap<String, NamePair>();
	}
	

	
	public String getStringContext( String name )
	{
		return m_strMap.get( name );
	}
	
	
	
	
	public String get( String name )
	{
		return getStringContext( name );
	}
	
	public int getAsInt( String name )
	{
		int value = 0;
		try
		{
			value = Integer.parseInt( get( name ) );
		}
		catch (Exception e) 
		{
		}
		return value;
	}

	public boolean getAsBoolean( String name )
	{
		return "Y".equals( get( name ) );
	}
	
	public void put( String name, String value )
	{
		m_strMap.put( name, value );
	}
	
	public HashMap<String, String> getStringMap()
	{
		return m_strMap;
	}

	public void setStringMap(HashMap<String, String> strMap) {
		m_strMap = strMap;
	}

	public NamePair getSelectedOption( String name )
	{
		return m_selectedOptions.get(name);
	}
	
	public ArrayList<String[]> getSelectedRows( int tabNO )
	{
		return m_selectedRows.get( tabNO );
	}
	
	public ArrayList<Integer> getSelectedRowNos( int tabNO )
	{
		return m_selectedRowNos.get( tabNO );
	}
	


	public void setSelectedRows(int tabNO, ArrayList<String[]> selectedRows) {
		m_selectedRows.put( tabNO, selectedRows );
	}

	public void setSelectedRowNos(int tabNO, ArrayList<Integer> selectedRowNos) {
		m_selectedRowNos.put( tabNO, selectedRowNos);
	}

	public void setSelectedOption(String columnName, NamePair selectedOption) {
		m_selectedOptions.put(columnName, selectedOption);
	}

	
}
