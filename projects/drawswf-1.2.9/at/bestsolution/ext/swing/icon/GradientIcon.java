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
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;

import at.bestsolution.ext.swing.dialog.JGradientChooser;

/**
 * @author tom
 */
public class GradientIcon implements Icon, ChangeListener
{
	public static final int BORDER_SIZE = 2;
	public static final int DEFAULT_SIZE = 20;
	protected int width_;
	protected int height_;
	protected MultipleGradientPaint gradient_;
	protected MultipleGradientPaint transformed_gradient_;
	protected int fill_width_;
	protected int fill_height_;
	protected int border_size_h_;
	protected int border_size_v_;
	protected EventListenerList listenerList = new EventListenerList();

	private Point2D.Float start_;
	private Point2D.Float end_;
	private Point2D.Float center_;

	public GradientIcon(MultipleGradientPaint gradient, int width, int height, int border_size_h, int border_size_v)
	{
		width_ = width;
		height_ = height;
		border_size_h_ = border_size_h;
		border_size_v_ = border_size_v;
		fill_width_ = width - 2 * border_size_h;
		fill_height_ = height - 2 * border_size_v;
		start_ = new Point2D.Float(0,0);
		end_ = new Point2D.Float(width_, height_);
		center_ = new Point2D.Float(width_ / 2, height_ / 2);
		setGradient(gradient);
	}

	public GradientIcon(MultipleGradientPaint gradient, int width, int height)
	{
		this(gradient, width, height, BORDER_SIZE, BORDER_SIZE);
	}

	public GradientIcon(MultipleGradientPaint gradient)
	{
		this(gradient, DEFAULT_SIZE, DEFAULT_SIZE);
	}

	/* (non-Javadoc)
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	public void paintIcon(Component comp, Graphics g, int x, int y)
	{
		if (gradient_ instanceof LinearGradientPaint)
		{
			LinearGradientPaint tmp_gradient = (LinearGradientPaint) gradient_;

			Point2D.Float start =  new Point2D.Float(start_.x+x,start_.y+y);
			Point2D.Float end =  new Point2D.Float(center_.x+x,center_.y+y);

			transformed_gradient_ =
				new LinearGradientPaint(start, end, tmp_gradient.getFractions(), tmp_gradient.getColors(), tmp_gradient.getCycleMethod(), MultipleGradientPaint.SRGB);
		}
		else
		{
			RadialGradientPaint tmp_gradient = (RadialGradientPaint) gradient_;

			Point2D.Float center =  new Point2D.Float(center_.x+x,center_.y+y);
		    Point2D.Float end =  new Point2D.Float(end_.x+x,end_.y+y);

			transformed_gradient_ =
				new RadialGradientPaint(
					center,
					(float) center.distance(end) / 2,
					center,
					tmp_gradient.getFractions(),
					tmp_gradient.getColors(),
					tmp_gradient.getCycleMethod(),
					MultipleGradientPaint.SRGB);
		}

		Graphics2D g2 = (Graphics2D) g;
		Color c = g.getColor();
		g2.setColor(Color.WHITE);
		g2.fillRect(x + border_size_h_, y + border_size_v_, fill_width_, fill_height_);

		g2.setPaint(transformed_gradient_);
		g2.fillRect(x + border_size_h_, y + border_size_v_, fill_width_, fill_height_);

		g2.setColor(c);
	}

	public void setGradient(MultipleGradientPaint gradient)
	{
		gradient_ = gradient;

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

	public MultipleGradientPaint getGradient()
	{
		return gradient_;
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
		if (e.getSource() instanceof JGradientChooser)
		{
			setGradient(((JGradientChooser) e.getSource()).getGradient());
		}
	}
}
