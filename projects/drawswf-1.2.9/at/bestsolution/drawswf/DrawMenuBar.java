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
package at.bestsolution.drawswf;

import javax.swing.JMenuBar;

import at.bestsolution.drawswf.actions.AbstractDrawAction;
import at.bestsolution.drawswf.menu.DrawEditMenu;
import at.bestsolution.drawswf.menu.DrawFileMenu;
import at.bestsolution.drawswf.menu.DrawHelpMenu;
import at.bestsolution.drawswf.menu.DrawMenuInterface;
import at.bestsolution.drawswf.menu.DrawMovieMenu;
import at.bestsolution.drawswf.menu.DrawToolsMenu;

/**
 * @author tom
 */
public class DrawMenuBar extends JMenuBar
{
    private DrawFileMenu file_menu_;
    private DrawEditMenu edit_menu_;
    private DrawToolsMenu tools_menu_;
    private DrawMovieMenu movie_menu_;
    private DrawHelpMenu help_menu_;
    
    public DrawMenuBar(DrawObjectList draw_object_list)
    {
        super();
    
        file_menu_ = new DrawFileMenu(MainWindow.getI18n().getString("MainWindowFile"), MainWindow.getI18n().getString("MainWindowFileMn").charAt(0));
        add(file_menu_);
        edit_menu_ = new DrawEditMenu(MainWindow.getI18n().getString("MainWindowEdit"), MainWindow.getI18n().getString("MainWindowEditMn").charAt(0));
        add(edit_menu_);
        tools_menu_ = new DrawToolsMenu(MainWindow.getI18n().getString("MainWindowTools"), MainWindow.getI18n().getString("MainWindowToolsMn").charAt(0), draw_object_list);
        add(tools_menu_);
        movie_menu_ = new DrawMovieMenu(MainWindow.getI18n().getString("MainWindowMovie"), MainWindow.getI18n().getString("MainWindowMovieMn").charAt(0));
        add(movie_menu_);
        help_menu_ = new DrawHelpMenu(MainWindow.getI18n().getString("MainWindowHelp"), MainWindow.getI18n().getString("MainWindowHelpMn").charAt(0));
        add(help_menu_);
    }

    public void changeDrawingType(int index)
    {
        tools_menu_.changeDrawingType(index);
    }

    public void addGenericMenuItem(String menu_name, AbstractDrawAction abstract_action, int position)
    {
		getMenu(menu_name).addGenericMenuItem(abstract_action, position);
    }
    
    public void setEnabled( String menu_name, String item_name, boolean enabled )
    {
		getMenu(menu_name).setItemEnabled( item_name, enabled );
    }
    
    private DrawMenuInterface getMenu( String menu_name )
    {
		DrawMenuInterface rv = null;
		
		if( menu_name.equals("file") )
		{
			rv = file_menu_;
		}
		else if( menu_name.equals("edit") )
		{
			rv = edit_menu_;
		}
		else if( menu_name.equals("tools") )
		{
			rv = tools_menu_;
		}
		else if( menu_name.equals("movie") )
		{
			rv = movie_menu_;
		}
		else if( menu_name.equals("help") )
		{
			rv = help_menu_;
		}
		
		return rv;
    }
}
