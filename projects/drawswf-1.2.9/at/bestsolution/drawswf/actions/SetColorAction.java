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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/actions/SetColorAction.java,v 1.12 2004/03/24 15:39:10 tom Exp $
 */
package at.bestsolution.drawswf.actions;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.ext.swing.dialog.AlphaColorChooser;
import at.bestsolution.ext.swing.icon.SolidColoredIcon;

/**
 *
 * @author  heli
 */
public class SetColorAction extends AbstractDrawAction implements ChangeListener
{
    private AlphaColorChooser color_chooser_;
    private JMenuItem menu_item_;
    private boolean pen_color_;
    private static AbstractButton[] fill_color_buttons_ = new AbstractButton[2];
    private static AbstractButton[] pen_color_buttons_ = new AbstractButton[2];

    //    //----------------------------------------------------------------------------
    //    public SetColorAction(String description, String icon_name, DrawingPanel drawing_panel, boolean pen_color)
    //    {
    //        super(description, tool_bar_icon_path + icon_name, drawing_panel);
    //        color_chooser_ = null;
    //        pen_color_ = pen_color;
    //    }

    //----------------------------------------------------------------------------
    //    public SetColorAction(String displayedText, String description, String icon_name, DrawingPanel drawing_panel, int mnemonicKey, KeyStroke accelerator, boolean pen_color)
    //    {
    //        super(displayedText, description, menu_bar_icon_path + icon_name, drawing_panel, mnemonicKey, accelerator);
    //        color_chooser_ = null;
    //        pen_color_ = pen_color;
    //    }

    public SetColorAction(DrawingPanel panel, boolean pen_color, AbstractButton button)
    {
        super(panel);
        pen_color_ = pen_color;

        if (pen_color)
        {
            if (pen_color_buttons_[0] == null)
            {
                pen_color_buttons_[0] = button;
            }
            else
            {
                pen_color_buttons_[1] = button;
            }
        }
        else
        {
            if (fill_color_buttons_[0] == null)
            {
                fill_color_buttons_[0] = button;
            }
            else
            {
                fill_color_buttons_[1] = button;
            }
        }
    }

    //----------------------------------------------------------------------------
    public void actionPerformed(ActionEvent action_event)
    {
        Color new_color;

        // TODO: make international
        
        if (color_chooser_ == null)
        {
            if (pen_color_)
            {
//                color_chooser_ = new AlphaColorChooser(MainWindow.getI18n().getString("AlphaColorChooserPenTitle"), drawing_panel_.getPenColor());
            }
            else
            {
//                color_chooser_ = new AlphaColorChooser(MainWindow.getI18n().getString("AlphaColorChooserPenTitle"), drawing_panel_.getFillColor());
            }

            color_chooser_.setChangeListener(this);
        }

        color_chooser_.show();
    }

    //----------------------------------------------------------------------------
    public void setColor(Color new_color)
    {
        if (pen_color_)
        {
            drawing_panel_.setPenColor(new_color);

            for (int i = 0; i < pen_color_buttons_.length; i++)
            {
                if (pen_color_buttons_[i] != null)
                {
                    SolidColoredIcon icon = (SolidColoredIcon) pen_color_buttons_[i].getIcon();
                    icon.setFillColor(new_color);
                    pen_color_buttons_[i].repaint();
                }
            }

        }
        else
        {
            drawing_panel_.setFillColor(new_color);

            for (int i = 0; i < fill_color_buttons_.length; i++)
            {
                if (fill_color_buttons_[i] != null)
                {
                    SolidColoredIcon icon = (SolidColoredIcon) fill_color_buttons_[i].getIcon();
                    icon.setFillColor(new_color);
                    fill_color_buttons_[i].repaint();
                }
            }
        }
    }

    //----------------------------------------------------------------------------
    public void stateChanged(ChangeEvent e)
    {
        setColor(color_chooser_.getColor());
    }
}
