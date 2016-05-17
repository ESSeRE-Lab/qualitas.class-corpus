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

import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.actions.AbstractDrawAction;
import at.bestsolution.drawswf.actions.ClearAction;
import at.bestsolution.drawswf.actions.RedoAction;
import at.bestsolution.drawswf.actions.UndoAction;

/**
 * @author tom
 */
public class DrawEditToolbar extends JToolBar implements DrawToolbarInterface
{
    public DrawEditToolbar()
    {
        super();
        // setFloatable(false);
        initToolbar();
    }

    //----------------------------------------------------------------------------
    private void addUndoAction( String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator )
    {
        UndoAction undo_action = new UndoAction(description, icon_name, MainWindow.getDrawingPanel());
        add(undo_action);
    }

    //----------------------------------------------------------------------------
    private void addRedoAction( String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator )
    {
        RedoAction redo_action = new RedoAction(description, icon_name, MainWindow.getDrawingPanel());
        add(redo_action);
    }

    //----------------------------------------------------------------------------
    private void addClearAction( String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator )
    {
        ClearAction clear_action = new ClearAction(description, icon_name, MainWindow.getDrawingPanel());
        add(clear_action);
    }

    //----------------------------------------------------------------------------
    private void initToolbar()
    {
        ResourceBundle international = MainWindow.getI18n();
        
        addUndoAction( international.getString( "MainWindowEditItemUndo" ), international.getString( "MainWindowEditItemUndoTooltip" ), "undo.png", international.getString( "MainWindowEditItemUndoMn" ).charAt(0), KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK) );
        addRedoAction( international.getString( "MainWindowEditItemRedo" ), international.getString( "MainWindowEditItemRedoTooltip" ), "redo.png", international.getString( "MainWindowEditItemRedoMn" ).charAt(0), KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
        addClearAction( international.getString( "MainWindowEditItemDeleteAll" ),international.getString( "MainWindowEditItemDeleteAllTooltip" ), "delete.png", international.getString( "MainWindowEditItemDeleteAllMn" ).charAt(0), KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));

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
		
	}

}
