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
package at.bestsolution.ext.swing.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;

import at.bestsolution.ext.swing.*;

/**
 * @author tom
 */
public class PreviewPanel extends JPanel implements ChangeListener, ItemListener
{
	private AlphaColorJButton button1_;
	private AlphaColorJButton button2_;
	private JComboBox type_;
	private JComboBox repeat_;
	private ButtonGroup group_;
	private String selected_ = "";
	private MultipleGradientPaint gradient_;
	private ChangeListener change_listener_ = null;

	public PreviewPanel(AlphaColorJButton b1, AlphaColorJButton b2, JComboBox type, JComboBox repeat)
	{
		super(null);
		button1_ = b1;
		button2_ = b2;
		type_ = type;
		repeat_ = repeat;

		setBorder(BorderFactory.createLoweredBevelBorder());
		setMinimumSize(new Dimension(200, 200));
		setPreferredSize(new Dimension(200, 200));
		setMaximumSize(new Dimension(200, 200));
		setBackground(Color.white);

		button1_.addChangeListener(this);
		button2_.addChangeListener(this);
		type_.addItemListener(this);
		repeat_.addItemListener(this);
	}

	public void paintComponent(Graphics g_real)
	{
		super.paintComponent(g_real);

		Rectangle rect = new Rectangle(20, 20, 160, 160);

		Graphics2D g = ((Graphics2D) g_real);
		createGradient();
		g.setPaint(gradient_);
		g.fill(rect);
		g.draw(rect);
	}

	private void createGradient()
	{
		Paint gradient = null;

		Color[] colors = { button1_.getColor(), button2_.getColor()};

		if (type_.getSelectedIndex() == 0)
		{
			if (repeat_.getSelectedIndex() == 0)
			{
				gradient_ =
					new LinearGradientPaint(
						JGradientChooser.START_,
						JGradientChooser.CENTER_,
						JGradientChooser.fractions_,
						colors,
						MultipleGradientPaint.NO_CYCLE,
						MultipleGradientPaint.SRGB);
			}
			else
				if (repeat_.getSelectedIndex() == 1)
				{
					gradient_ =
						new LinearGradientPaint(
							JGradientChooser.START_,
							JGradientChooser.CENTER_,
							JGradientChooser.fractions_,
							colors,
							MultipleGradientPaint.REFLECT,
							MultipleGradientPaint.SRGB);
				}
				else
				{
					gradient_ =
						new LinearGradientPaint(
							JGradientChooser.START_,
							JGradientChooser.CENTER_,
							JGradientChooser.fractions_,
							colors,
							MultipleGradientPaint.REPEAT,
							MultipleGradientPaint.SRGB);
				}
		}
		else
		{
			if (repeat_.getSelectedIndex() == 0)
			{
				gradient_ =
					new RadialGradientPaint(
						JGradientChooser.CENTER_,
						(float) JGradientChooser.CENTER_.distance(JGradientChooser.END_) / 2,
						JGradientChooser.CENTER_,
						JGradientChooser.fractions_,
						colors,
						MultipleGradientPaint.NO_CYCLE,
						MultipleGradientPaint.SRGB);
			}
			else
				if (repeat_.getSelectedIndex() == 1)
				{

					gradient_ =
						new RadialGradientPaint(
							JGradientChooser.CENTER_,
							(float) JGradientChooser.CENTER_.distance(JGradientChooser.END_) / 2,
							JGradientChooser.CENTER_,
							JGradientChooser.fractions_,
							colors,
							MultipleGradientPaint.REFLECT,
							MultipleGradientPaint.SRGB);
				}
				else
				{
					gradient_ =
						new RadialGradientPaint(
							JGradientChooser.CENTER_,
							(float) JGradientChooser.CENTER_.distance(JGradientChooser.END_) / 2,
							JGradientChooser.CENTER_,
							JGradientChooser.fractions_,
							colors,
							MultipleGradientPaint.REPEAT,
							MultipleGradientPaint.SRGB);
				}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		createGradient();
		repaint();

		if (change_listener_ != null)
		{
			change_listener_.stateChanged(new ChangeEvent(this));
		}
	}


	public MultipleGradientPaint getGradient()
	{
		return gradient_;
	}

	public void setChangeListener(ChangeListener change_listener)
	{
		change_listener_ = change_listener;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e)
	{
		if( e.getStateChange() == ItemEvent.SELECTED)
		{
			createGradient();
			repaint();
			
			if (change_listener_ != null)
			{
				change_listener_.stateChanged(new ChangeEvent(this));
			}
		}
	}

}
