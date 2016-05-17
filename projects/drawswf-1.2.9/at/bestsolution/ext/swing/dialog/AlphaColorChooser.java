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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/ext/swing/dialog/AlphaColorChooser.java,v 1.3 2003/05/08 10:47:11 tom Exp $
 */

package at.bestsolution.ext.swing.dialog;

import java.awt.Color;
import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JSlider;
import javax.swing.JColorChooser;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.BorderFactory;

/**
 *
 * @author  heli
 */
public class AlphaColorChooser extends JDialog implements ChangeListener
{
    private JSlider slider_;
    private JColorChooser color_chooser_;
    private ChangeListener change_listener_;
    public static AlphaColorChooser instance_ = null;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of AlphaColorChooser */
    private AlphaColorChooser(String title, Color color)
    {
        super();
        setTitle(title);
        setModal(true);
        change_listener_ = null;
        color_chooser_ = new JColorChooser(color);
        color_chooser_.getSelectionModel().addChangeListener(this);
        getContentPane().add(color_chooser_, BorderLayout.CENTER);
        slider_ = new JSlider(JSlider.HORIZONTAL, 0, 255, color.getAlpha());
        slider_.addChangeListener(this);
        slider_.setMajorTickSpacing(85);
        slider_.setMinorTickSpacing(17);
        slider_.setPaintTicks(true);
        slider_.setPaintLabels(true);
        slider_.setBorder(BorderFactory.createTitledBorder("Alpha"));
        getContentPane().add(slider_, BorderLayout.SOUTH);
        pack();
    }
    
    public static AlphaColorChooser getInstance()
    {
        if( instance_ == null )
        {
            instance_ = new AlphaColorChooser("Choose Color",Color.RED);
        }
        
        return instance_; 
    }
    
    //----------------------------------------------------------------------------
    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e  a ChangeEvent object
     */
    public void stateChanged(ChangeEvent e)
    {
        if (change_listener_ != null)
        {
			color_chooser_.setColor( getColor() );
            change_listener_.stateChanged( new ChangeEvent(this) );
        }
    }
    
    //----------------------------------------------------------------------------
    public void setChangeListener(ChangeListener change_listener)
    {
        change_listener_ = change_listener;
    }
    
    //----------------------------------------------------------------------------
    public void setColor(Color new_color)
    {
        color_chooser_.setColor(new_color);
        slider_.setValue( new_color.getAlpha() );
    }
    
    //----------------------------------------------------------------------------
    public Color getColor()
    {
        Color chooser_color = color_chooser_.getColor();
        return new Color(chooser_color.getRed(), chooser_color.getGreen(), chooser_color.getBlue(), slider_.getValue());
    }
}
