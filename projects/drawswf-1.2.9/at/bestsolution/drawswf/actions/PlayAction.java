/*
 *  Copyright (c) 2002
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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/actions/PlayAction.java,v 1.6 2003/02/27 14:20:29 tom Exp $
 */
package at.bestsolution.drawswf.actions;

import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.DrawingPanel;

/**
 *
 * @author  heli
 */
public class PlayAction extends AbstractDrawAction
{
    private boolean start_;

    //----------------------------------------------------------------------------
    public PlayAction(String description, String icon_name, DrawingPanel drawing_panel, boolean start)
    {
        super(description, tool_bar_icon_path + icon_name, drawing_panel);
        start_ = start;
    }
    
    //----------------------------------------------------------------------------
    public PlayAction(String displayedText, String description, String icon_name, DrawingPanel drawing_panel, int mnemonicKey, KeyStroke accelerator, boolean start)
    {
        super(displayedText, description, menu_bar_icon_path + icon_name, drawing_panel, mnemonicKey, accelerator);
        start_ = start;
    }
    
    //----------------------------------------------------------------------------
    public void actionPerformed(ActionEvent action_event)
    {
        drawing_panel_.replay(start_);
    }
}
