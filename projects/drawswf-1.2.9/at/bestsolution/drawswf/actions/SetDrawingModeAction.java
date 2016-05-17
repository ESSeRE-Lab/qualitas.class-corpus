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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/actions/SetDrawingModeAction.java,v 1.5 2003/01/24 01:35:53 tom Exp $
 */
package at.bestsolution.drawswf.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;


import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.drawswf.MainWindow;

/**
 *
 * @author  heli
 */
public class SetDrawingModeAction extends AbstractAction
{
    private int drawing_mode_;
    private DrawingPanel drawing_panel_;
    private MainWindow frame_;
    private String buttonType_;
    private int buttonIndex_;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of LineAction */
    public SetDrawingModeAction(String description, DrawingPanel drawing_panel, int drawing_mode, MainWindow frame, int buttonIndex)
    {
        drawing_panel_ = drawing_panel;
        drawing_mode_  = drawing_mode;
        frame_         = frame;
        buttonType_    = "ToolBarButton";
        buttonIndex_   = buttonIndex;
        putValue(Action.SHORT_DESCRIPTION, description);
    }
    
    //----------------------------------------------------------------------------
    public SetDrawingModeAction(String displayedText, String description, DrawingPanel drawing_panel, int drawing_mode, int mnemonicKey, KeyStroke accelerator, MainWindow frame, int buttonIndex)
    {
        drawing_panel_ = drawing_panel;
        drawing_mode_  = drawing_mode;
        frame_         = frame;
        buttonType_    = "MenuBarButton";
        buttonIndex_   = buttonIndex;
        putValue(Action.SHORT_DESCRIPTION, description);
        putValue(Action.NAME, displayedText);
        putValue(Action.MNEMONIC_KEY, new Integer(mnemonicKey));
    }
    
    //----------------------------------------------------------------------------
    public void actionPerformed(ActionEvent action_event)
    {
        frame_.swapRadioButtons( buttonType_, buttonIndex_);
        drawing_panel_.setDrawingMode(drawing_mode_);
    }
}
