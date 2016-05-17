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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/menu/DrawEditMenu.java,v 1.3 2003/06/08 11:34:11 tom Exp $
 */
package at.bestsolution.drawswf.menu;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.actions.AbstractDrawAction;
import at.bestsolution.drawswf.actions.ClearAction;
import at.bestsolution.drawswf.actions.RedoAction;
import at.bestsolution.drawswf.actions.UndoAction;


/**
 * @author tom
 */
public class DrawEditMenu extends JMenu implements DrawMenuInterface
{
    public DrawEditMenu( String label, char mnemonic )
    {
        super(label);
        setMnemonic(mnemonic);
        initMenu();
    }
    
//  ----------------------------------------------------------------------------
      private void addUndoAction( String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator )
      {
          UndoAction undo_action = new UndoAction( displayedText, description, icon_name, MainWindow.getDrawingPanel(), mnemonicKey, accelerator);
          add(undo_action);
      }


      //----------------------------------------------------------------------------
      private void addRedoAction( String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator )
      {
          RedoAction redo_action = new RedoAction( displayedText, description, icon_name, MainWindow.getDrawingPanel(), mnemonicKey, accelerator);
          add(redo_action);
      }

      //----------------------------------------------------------------------------
      private void addClearAction( String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator )
      {
          ClearAction clear_action = new ClearAction( displayedText, description, icon_name, MainWindow.getDrawingPanel(), mnemonicKey, accelerator);
          add(clear_action);
      }

    //----------------------------------------------------------------------------
    private void initMenu()
    {
        addUndoAction( MainWindow.getI18n().getString( "MainWindowEditItemUndo" ), MainWindow.getI18n().getString( "MainWindowEditItemUndoTooltip" ), "undo.png", MainWindow.getI18n().getString( "MainWindowEditItemUndoMn" ).charAt(0), KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK) );
        addRedoAction( MainWindow.getI18n().getString( "MainWindowEditItemRedo" ), MainWindow.getI18n().getString( "MainWindowEditItemRedoTooltip" ), "redo.png", MainWindow.getI18n().getString( "MainWindowEditItemRedoMn" ).charAt(0), KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
        addClearAction( MainWindow.getI18n().getString( "MainWindowEditItemDeleteAll" ),MainWindow.getI18n().getString( "MainWindowEditItemDeleteAllTooltip" ), "delete.png", MainWindow.getI18n().getString( "MainWindowEditItemDeleteAllMn" ).charAt(0), KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
    }


    /* (non-Javadoc)
     * @see at.bestsolution.drawswf.menu.DrawMenuInterface#addGenericMenuItem(at.bestsolution.drawswf.actions.AbstractDrawAction)
     */
    public void addGenericMenuItem(AbstractDrawAction draw_action, int position )
    {
        insert( draw_action, position );
    }
    
	public void setItemEnabled( String name, boolean enabled )
	{
			System.err.println( "NOT IMPLEMENTED" );
	}

}
