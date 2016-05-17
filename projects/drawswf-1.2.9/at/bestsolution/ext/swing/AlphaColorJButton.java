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
 * Created on 05.04.2003
 *
 */
package at.bestsolution.ext.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.bestsolution.ext.swing.dialog.*;
import at.bestsolution.ext.swing.icon.IIconFactory;
import at.bestsolution.ext.swing.icon.SolidColoredIcon;

/**
 * @author tom
 */
public class AlphaColorJButton extends JButton implements ChangeListener, ActionListener, AlphaColorButtonI
{
    private String title_ = "Choose Color";
    private SolidColoredIcon fill_icon_ = null;

    public AlphaColorJButton(Color color)
    {
        this(color, 20, 20, 0, 0);
    }

    public AlphaColorJButton(Color color, int width, int height, int border_x, int border_y)
    {
        super();

        fill_icon_ = new SolidColoredIcon(color, width, height, border_x, border_y);
        fill_icon_.addChangeListener(this);
        setIcon(fill_icon_);
        addActionListener(this);
    }

    public AlphaColorJButton( IIconFactory icon_provider, String name, Color color )
    {
        this(icon_provider,name);
        fill_icon_.setFillColor(color);
    }

    public AlphaColorJButton( IIconFactory icon_provider, String name )
    {
        super();
        fill_icon_ = icon_provider.getSolidColorIcon( name );
        fill_icon_.addChangeListener(this);
        setIcon(fill_icon_);
        addActionListener( this );
    }


    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        
        // fill_icon_.setFillColor( ((AlphaColorChooser)e.getSource()).getColor() );
        fireStateChanged();
        repaint();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        AlphaColorChooser color_chooser = AlphaColorChooser.getInstance();
        
        color_chooser.setTitle( title_ );
        color_chooser.setChangeListener( fill_icon_ );
        color_chooser.setColor( ((SolidColoredIcon) getIcon()).getColor() );
        color_chooser.show();
    }

    public Color getColor()
    {
        return ((SolidColoredIcon) getIcon()).getColor();
    }
    
    public void setColor( Color color )
    {
        ((SolidColoredIcon) getIcon()).setFillColor(color);
    }
}