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
 * Created on 11.04.2003
 *
 */
package at.bestsolution.drawswf;

import java.awt.Color;
import java.util.HashMap;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;

import at.bestsolution.ext.swing.dialog.JGradientChooser;
import at.bestsolution.ext.swing.icon.GradientIcon;
import at.bestsolution.ext.swing.icon.IIconFactory;
import at.bestsolution.ext.swing.icon.SolidColoredIcon;

/**
 * @author tom
 */
public class IconProvider implements IIconFactory
{
	private static HashMap solid_icons_map_ = new HashMap();
	private static HashMap gradient_icons_map_ = new HashMap();
	private static IconProvider instance_ = null;

	private IconProvider()
	{
		solid_icons_map_.put("pen_color", new SolidColoredIcon(Color.BLACK, 20, 20, 2, 7));
		solid_icons_map_.put("fill_color", new SolidColoredIcon(Color.BLACK, 20, 20, 2, 2));
		solid_icons_map_.put("text_color", new SolidColoredIcon(Color.BLACK, 20, 20, 2, 2));

		gradient_icons_map_.put(
			"fill_gradient",
			new GradientIcon(new LinearGradientPaint(
		JGradientChooser.START_,
		JGradientChooser.CENTER_,
		JGradientChooser.fractions_,
		JGradientChooser.colors,
		MultipleGradientPaint.NO_CYCLE,
		MultipleGradientPaint.SRGB),20,20,2,2)
			);
	}

	public static IconProvider getInstance()
	{
		if (instance_ == null)
		{
			instance_ = new IconProvider();
		}

		return instance_;
	}

	/* (non-Javadoc)
	 * @see at.bestsolution.ext.swing.icon.IIconFactory#getSolidColorIcon(java.lang.String)
	 */
	public SolidColoredIcon getSolidColorIcon(String name)
	{
		return (SolidColoredIcon) solid_icons_map_.get(name);
	}

	/* (non-Javadoc)
	 * @see at.bestsolution.ext.swing.icon.IIconFactory#getGradientIcon(java.lang.String)
	 */
	public GradientIcon getGradientIcon(String name)
	{
		return (GradientIcon) gradient_icons_map_.get(name);
	}

}
