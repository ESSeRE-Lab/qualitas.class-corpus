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

import java.util.ResourceBundle;

import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.actions.AbstractDrawAction;
import at.bestsolution.drawswf.actions.PlayAction;

/**
 * @author tom
 */
public class DrawMovieToolbar extends JToolBar implements DrawToolbarInterface
{
    public DrawMovieToolbar()
    {
        super();
        //setFloatable(false);
        initToolbar();
    }
    
    //----------------------------------------------------------------------------
    private void addPlayAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator, boolean start )
    {
        PlayAction play_action = new PlayAction(description, icon_name, MainWindow.getDrawingPanel(), start);
		add(play_action);
    }
    
    //----------------------------------------------------------------------------
    private void initToolbar()
    {
        ResourceBundle international = MainWindow.getI18n();
        
        addPlayAction(international.getString( "MainWindowMovieItemPlay"),international.getString( "MainWindowMovieItemPlayTooltip"), "play.png", international.getString( "MainWindowMovieItemPlayMn").charAt(0), null, true);
		addPlayAction(international.getString( "MainWindowMovieItemStop"),international.getString( "MainWindowMovieItemStopTooltip"), "stop.png", international.getString( "MainWindowMovieItemStopMn").charAt(0), null, false);
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
