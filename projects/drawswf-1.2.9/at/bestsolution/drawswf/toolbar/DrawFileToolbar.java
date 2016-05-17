/*
 *  Copyright (c) 2003
 *  bestsolution EDV Systemhaus GmbH,
 *  http://www.bestsolution.at
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

/*
 * Created on 22.02.2003
 *
 */
package at.bestsolution.drawswf.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.actions.AbstractDrawAction;
import at.bestsolution.drawswf.actions.FileAction;
import at.bestsolution.drawswf.actions.GraphicTemplateAction;
import at.bestsolution.drawswf.actions.SaveAction;
import at.bestsolution.drawswf.actions.SaveSWFAction;

/**
 * @author tom
 */
public class DrawFileToolbar extends JToolBar implements DrawToolbarInterface
{
	JButton save_action_;
	
    public DrawFileToolbar()
    {
        super();
        // setFloatable(false);
        initToolbar();
    }

    //----------------------------------------------------------------------------
    private void addFileAction(String displayedText, String description, String icon_name, boolean load, int mnemonicKey, KeyStroke accelerator, boolean append)
    {
        FileAction load_action = new FileAction(description, icon_name, MainWindow.getDrawingPanel(), load, append);
        add(load_action);
    }

    //----------------------------------------------------------------------------
    private void addGraphicTemplateAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
    {
        GraphicTemplateAction template_action = new GraphicTemplateAction(description, icon_name, MainWindow.getDrawingPanel());
        add(template_action);
    }

    //----------------------------------------------------------------------------
    private void addSaveSWFAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
    {
        SaveSWFAction save_swf_action = new SaveSWFAction(description, icon_name, MainWindow.getDrawingPanel());
        add(save_swf_action);
    }

	//----------------------------------------------------------------------------
	private void addSaveAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
	{
		SaveAction save_action = new SaveAction(description, icon_name, MainWindow.getDrawingPanel());
		save_action_ = add(save_action);
		save_action_.setEnabled( false );
	}
    
    //----------------------------------------------------------------------------
    private void initToolbar()
    {
        ResourceBundle international = MainWindow.getI18n();
        
        addFileAction( international.getString( "MainWindowFileItemOpen" ), international.getString( "MainWindowFileItemOpenTooltip"), "open.png", true, international.getString( "MainWindowFileItemOpenMn" ).charAt(0), KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), false );
        addFileAction( international.getString( "MainWindowFileItemAppend" ), international.getString( "MainWindowFileItemAppendTooltip" ), "append.png", true, international.getString( "MainWindowFileItemAppendMn" ).charAt(0), null,true );
        addFileAction( international.getString( "MainWindowFileItemSaveAs" ), international.getString( "MainWindowFileItemSaveAsTooltip" ), "saveas.png", false, international.getString( "MainWindowFileItemSaveAsMn" ).charAt(0), null, false );
		addSaveAction( international.getString( "MainWindowFileItemSave" ),international.getString( "MainWindowFileItemSaveTooltip" ), "save.png", international.getString( "MainWindowFileItemSaveMn" ).charAt(0), KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK) );
        addGraphicTemplateAction( international.getString( "MainWindowFileItemLoadTemp" ), international.getString( "MainWindowFileItemLoadTempTooltip" ), "graphic_template.png", international.getString( "MainWindowFileItemLoadTempMn" ).charAt(0), null );
        addSaveSWFAction( international.getString( "MainWindowFileItemExport" ),international.getString( "MainWindowFileItemExportTooltip" ), "export.png", international.getString( "MainWindowFileItemExportMn" ).charAt(0), KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK) );
        
        addSeparator();
    }

    /* (non-Javadoc)
     * @see at.bestsolution.drawswf.toolbar.DrawToolbarInterface#addGenericToolbarItem(at.bestsolution.drawswf.actions.AbstractDrawAction, int)
     */
    public void addGenericToolbarItem(AbstractDrawAction draw_action, int position)
    {
        // TODO Auto-generated method stub

    }
	
	public void setItemEnabled( String name, boolean enabled )
	{
		if( name.equals("save_action") )
		{
			save_action_.setEnabled( enabled );
		}
		else
		{
			System.err.println( "NOT IMPLEMENTED" );
		}
		
	}
}
