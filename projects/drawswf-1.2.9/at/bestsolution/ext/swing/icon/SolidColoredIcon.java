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
 * Created on 27.02.2003
 *
 */
package at.bestsolution.ext.swing.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import at.bestsolution.ext.swing.dialog.AlphaColorChooser;

/**
 * @author tom
 */
public class SolidColoredIcon implements Icon, ChangeListener
{
    public static final int BORDER_SIZE = 2;
    public static final int DEFAULT_SIZE = 20;
    protected int width_;
    protected int height_;
    protected Color fill_color_; 
    protected Color fill_color_no_alpha_;
    protected int fill_width_;
    protected int fill_height_;
    protected int border_size_h_;
    protected int border_size_v_;
    protected EventListenerList listenerList = new EventListenerList();

    public SolidColoredIcon(Color fill_color, int width, int height, int border_size_h, int border_size_v)
    {
        setFillColor(fill_color);
        width_ = width;
        height_ = height;
        border_size_h_ = border_size_h;
        border_size_v_ = border_size_v;
        fill_width_ = (width - 2 * border_size_h) / 2;
        fill_height_ = height - 2 * border_size_v;
    }

    public SolidColoredIcon(Color fill_color, int width, int height)
    {
        this(fill_color, width, height, BORDER_SIZE, BORDER_SIZE);
    }

    public SolidColoredIcon(Color fill_color)
    {
        this(fill_color, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    public void paintIcon(Component comp, Graphics g, int x, int y)
    {
        Color c = g.getColor();
        g.setColor(Color.WHITE);
        g.fillRect(x + border_size_h_, y + border_size_v_, fill_width_, fill_height_);
        g.setColor(fill_color_);
        g.fillRect(x + border_size_h_, y + border_size_v_, fill_width_, fill_height_);
        g.setColor(fill_color_no_alpha_);
        g.fillRect(x + border_size_h_ + fill_width_, y + border_size_v_, fill_width_, fill_height_);

        g.setColor(c);
    }

    public void setFillColor(Color color)
    {
        fill_color_ = color;
        fill_color_no_alpha_ = new Color(color.getRed(), color.getGreen(), color.getBlue());
        fireStateChanged();
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth()
    {
        return width_;
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight()
    {
        return height_;
    }

    public Color getColor()
    {
        return fill_color_;
    }

    public void addChangeListener(ChangeListener l)
    {
        listenerList.add(ChangeListener.class, l);
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created.
     * @see EventListenerList
     */
    protected void fireStateChanged()
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == ChangeListener.class)
            {
                ((ChangeListener) listeners[i + 1]).stateChanged(new ChangeEvent(this));
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        if( e.getSource() instanceof AlphaColorChooser )
        {
            setFillColor( ((AlphaColorChooser)e.getSource()).getColor() );
        }
    }

}
