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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/actions/SetCanvasSizeAction.java,v 1.8 2004/03/24 15:39:10 tom Exp $
 */
package at.bestsolution.drawswf.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.DrawingPanel;

/**
 *
 * @author  heli
 */
public class SetCanvasSizeAction extends AbstractDrawAction
{
    private Dimension size_;
    private JFrame frame_;
    
    //----------------------------------------------------------------------------
    public SetCanvasSizeAction(String description, String icon_name, DrawingPanel drawing_panel, JFrame frame)
    {
        super(description, tool_bar_icon_path + icon_name, drawing_panel);
        frame_ = frame;
    }
    
    //----------------------------------------------------------------------------
    public SetCanvasSizeAction(String displayedText, String description, String icon_name, DrawingPanel drawing_panel, JFrame frame, int mnemonicKey, KeyStroke accelerator)
    {
        super(displayedText, description, menu_bar_icon_path + icon_name, drawing_panel, mnemonicKey, accelerator);
        frame_ = frame;
    }
    
    //----------------------------------------------------------------------------
    public void actionPerformed(ActionEvent action_event)
    {
        Object[] possible_values =
        { "4:3","4:1","1:1","1:4","3:4" };
        String selected_value = (String) JOptionPane.showInputDialog(null, MainWindow.getI18n().getString("SetCanvasSizeAction"), MainWindow.getI18n().getString("SetCanvasSizeAction"), JOptionPane.INFORMATION_MESSAGE, null, possible_values, possible_values[0]);
        if (selected_value != null)
        {
            if (selected_value.equals("4:3"))
            {
                size_ = new Dimension(800,600);
                drawing_panel_.setCanvasSize(4,3);
            }
            else if(selected_value.equals("4:1"))
            {
                size_ = new Dimension(800,200);
                drawing_panel_.setCanvasSize(4,1);
            }
            else if(selected_value.equals("1:1"))
            {
                size_ = new Dimension(800,800);
                drawing_panel_.setCanvasSize(1,1);
            }
            else if(selected_value.equals("1:4"))
            {
                size_ = new Dimension(200,800);
                drawing_panel_.setCanvasSize(1,4);
            }
            else if(selected_value.equals("3:4"))
            {
                size_ = new Dimension(600,800);
                drawing_panel_.setCanvasSize(3,4);
            }
        }
    }
}
