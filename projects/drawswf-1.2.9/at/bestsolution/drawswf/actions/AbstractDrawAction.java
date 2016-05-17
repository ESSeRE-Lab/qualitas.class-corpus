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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/actions/AbstractDrawAction.java,v 1.7 2003/03/07 10:52:37 tom Exp $
 */
package at.bestsolution.drawswf.actions;

import java.net.URL;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.DrawingPanel;

/**
 *
 * @author  heli
 */
public abstract class AbstractDrawAction extends AbstractAction
{
    protected DrawingPanel drawing_panel_;
    protected static final String tool_bar_icon_path = "at/bestsolution/drawswf/images/24x24/";
    protected static final String menu_bar_icon_path = "at/bestsolution/drawswf/images/16x16/";

    //----------------------------------------------------------------------------
    public AbstractDrawAction(String description, String icon_name, DrawingPanel drawing_panel)
    {
       ImageIcon icon; 
        URL icon_url;
 
        icon_url = getClass().getClassLoader().getResource(icon_name);
        icon = new ImageIcon(icon_url);

        putValue(Action.SMALL_ICON, icon);
        putValue(Action.SHORT_DESCRIPTION, description);

        drawing_panel_ = drawing_panel;
    }

    //----------------------------------------------------------------------------
    public AbstractDrawAction(String displayedText, String description, String icon_name, DrawingPanel drawing_panel, int mnemonicKey, KeyStroke accelerator)
    {
        ImageIcon icon;
        URL icon_url;

        icon_url = getClass().getClassLoader().getResource(icon_name);
        icon = new ImageIcon(icon_url);
        putValue(Action.NAME, displayedText);
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.SHORT_DESCRIPTION, description);
        putValue(Action.MNEMONIC_KEY, new Integer(mnemonicKey));
        putValue(Action.ACCELERATOR_KEY, accelerator);

        drawing_panel_ = drawing_panel;
    }

    public AbstractDrawAction( DrawingPanel drawing_panel )
    {
        drawing_panel_ = drawing_panel;  
    }

    //----------------------------------------------------------------------------
    public abstract void actionPerformed(ActionEvent actionEvent);
}
