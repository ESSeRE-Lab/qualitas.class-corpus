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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/actions/SetDrawing2GradientAction.java,v 1.1 2003/05/11 08:55:12 tom Exp $
 */
package at.bestsolution.drawswf.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.drawswf.MainWindow;

/**
 *
 * @author  heli
 */
public class SetDrawing2GradientAction extends AbstractAction
{
    private int drawing_mode_;
    private DrawingPanel drawing_panel_;
    private MainWindow frame_;
    private String buttonType_;
    private int buttonIndex_;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of LineAction */
    public SetDrawing2GradientAction(DrawingPanel drawing_panel, int drawing_mode, MainWindow frame, int buttonIndex, boolean isToolbar)
    {
        drawing_panel_ = drawing_panel;
        drawing_mode_  = drawing_mode;
        frame_         = frame;
        
        if( isToolbar )
        {
			buttonType_    = "ToolBarButton";
        }
        else
        {
			buttonType_    = "MenuBarButton";
        }
        
        buttonIndex_   = buttonIndex;
    }
        
    //----------------------------------------------------------------------------
    public void actionPerformed(ActionEvent action_event)
    {
        frame_.swapRadioButtons( buttonType_, buttonIndex_);
        drawing_panel_.setDrawingMode(drawing_mode_);
    }
}
