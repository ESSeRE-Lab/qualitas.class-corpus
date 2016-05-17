/*
 * Created on 08.06.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package at.bestsolution.drawswf.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import at.bestsolution.drawswf.DrawingPanel;

/**
 * @author tom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SaveAction extends AbstractDrawAction
{

	//----------------------------------------------------------------------------
	public SaveAction(String description, String icon_name, DrawingPanel drawing_panel)
	{
		super(description, tool_bar_icon_path + icon_name, drawing_panel);
	}
    
	//----------------------------------------------------------------------------
	public SaveAction(String displayedText, String description, String icon_name, DrawingPanel drawing_panel, int mnemonicKey, KeyStroke accelerator)
	{
		super(displayedText, description, menu_bar_icon_path + icon_name, drawing_panel, mnemonicKey, accelerator);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent actionEvent)
	{
		drawing_panel_.saveAnimation();
	}

}
