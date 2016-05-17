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
 * Created on 21.02.2003
 *
 */
package at.bestsolution.drawswf.menu;

import javax.swing.JMenu;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.actions.AbstractDrawAction;
import at.bestsolution.drawswf.actions.PlayAction;

/**
 * @author tom
 */
public class DrawMovieMenu extends JMenu implements DrawMenuInterface
{
    public DrawMovieMenu(String label, char mnemonic)
    {
        super(label);
        setMnemonic(mnemonic);
        initMenu();
    }
    
    //----------------------------------------------------------------------------
    private void addPlayAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator, boolean start )
    {
        PlayAction play_action = new PlayAction( displayedText, description, icon_name, MainWindow.getDrawingPanel(), mnemonicKey, accelerator, start);
        add(play_action);
    }

    
    //----------------------------------------------------------------------------
    private void initMenu()
    {
        addPlayAction(MainWindow.getI18n().getString( "MainWindowMovieItemPlay"),MainWindow.getI18n().getString( "MainWindowMovieItemPlayTooltip"), "play.png", MainWindow.getI18n().getString( "MainWindowMovieItemPlayMn").charAt(0), null, true);
        addPlayAction(MainWindow.getI18n().getString( "MainWindowMovieItemStop"),MainWindow.getI18n().getString( "MainWindowMovieItemStopTooltip"), "stop.png", MainWindow.getI18n().getString( "MainWindowMovieItemStopMn").charAt(0), null, false);
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
