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

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.actions.AboutAction;
import at.bestsolution.drawswf.actions.AbstractDrawAction;

/**
 * @author tom
 */
public class DrawHelpMenu extends JMenu implements DrawMenuInterface
{
    public DrawHelpMenu(String label, char mnemonic)
    {
        super(label);
        setMnemonic(mnemonic);
        initMenu();
    }
    
    //----------------------------------------------------------------------------
    private void initMenu()
    {
        setMnemonic( MainWindow.getI18n().getString("MainWindowHelpMn").charAt(0) );
        AboutAction about_action = new AboutAction(MainWindow.getI18n().getString( "MainWindowHelpItemAbout"),MainWindow.getI18n().getString( "MainWindowHelpItemAboutTooltip"), "about.png", MainWindow.getDrawingPanel(), MainWindow.getI18n().getString( "MainWindowHelpItemAboutMn").charAt(0), null);
        add(about_action);
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
