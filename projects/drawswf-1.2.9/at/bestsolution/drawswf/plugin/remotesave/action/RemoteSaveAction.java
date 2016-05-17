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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/plugin/remotesave/action/RemoteSaveAction.java,v 1.1 2003/02/27 14:20:29 tom Exp $
 */
package at.bestsolution.drawswf.plugin.remotesave.action;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.drawswf.AboutWindow;
import at.bestsolution.drawswf.plugin.remotesave.dialog.RemoteSaveDialog;
import at.bestsolution.drawswf.actions.AbstractDrawAction;

/**
 *
 * @author  tom
 */
public class RemoteSaveAction extends AbstractDrawAction
{
    private AboutWindow about_window_;
    private static final String plugin_tool_bar_image_path_ = "at/bestsolution/drawswf/plugin/remotesave/images/24x24/";
    private static final String plugin_menu_bar_image_path_ = "at/bestsolution/drawswf/plugin/remotesave/images/16x16/";

    //----------------------------------------------------------------------------
    public RemoteSaveAction(String description, String icon_name, DrawingPanel drawing_panel)
    {
        super(description, plugin_tool_bar_image_path_ + icon_name, drawing_panel);
        about_window_ = null;
    }
    
    //----------------------------------------------------------------------------
    public RemoteSaveAction(String displayedText, String description, String icon_name, DrawingPanel drawing_panel, int mnemonicKey, KeyStroke accelerator)
    {
        super( displayedText, description, plugin_menu_bar_image_path_+ icon_name, drawing_panel, mnemonicKey, accelerator );
    }
    
    //----------------------------------------------------------------------------
    public void actionPerformed(ActionEvent action_event)
    {
        RemoteSaveDialog.getInstance().show();
    }
}
