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

import javax.swing.BorderFactory;
import javax.swing.JToolBar;

import at.bestsolution.drawswf.toolbar.DrawEditToolbar;
import at.bestsolution.drawswf.toolbar.DrawFileToolbar;
import at.bestsolution.drawswf.toolbar.DrawMovieToolbar;
import at.bestsolution.drawswf.toolbar.DrawToolbarInterface;
import at.bestsolution.drawswf.toolbar.DrawToolsToolbar;

/**
 * @author tom
 */
public class DrawToolBar extends JToolBar
{
    private DrawFileToolbar file_tool_;
    private DrawEditToolbar edit_tool_;
    private DrawMovieToolbar movie_tool_;
    private DrawToolsToolbar tools_tool_;

    public DrawToolBar(DrawObjectList draw_object_list)
    {
        super();
        setBorder(BorderFactory.createRaisedBevelBorder());
        file_tool_ = new DrawFileToolbar();
        add(file_tool_);
        edit_tool_ = new DrawEditToolbar();
        add(edit_tool_);
        tools_tool_ = new DrawToolsToolbar();
        add(tools_tool_);
        movie_tool_ = new DrawMovieToolbar();
        add(movie_tool_);
    }

    public void changeDrawingType(int index)
    {
        tools_tool_.changeDrawingType(index);
    }

    public void addGenericMenuItem()
    {
        // TODO for plugins implement a generic add method like we did for menu
    }
    
	public void setEnabled( String menu_name, String item_name, boolean enabled )
	{
		getToolbar(menu_name).setItemEnabled( item_name, enabled );
	}
	
	public DrawToolbarInterface getToolbar(String toolbar_name)
	{
		DrawToolbarInterface rv = null;
		
		if( toolbar_name.equals("file") )
		{
			rv = file_tool_;
		}
		else if( toolbar_name.equals("edit") )
		{
			rv = edit_tool_;
		}
		else if( toolbar_name.equals("tools") )
		{
			rv = tools_tool_;
		}
		else if( toolbar_name.equals("movie") )
		{
			rv = movie_tool_;
		}
		
		return rv;
	}
}
