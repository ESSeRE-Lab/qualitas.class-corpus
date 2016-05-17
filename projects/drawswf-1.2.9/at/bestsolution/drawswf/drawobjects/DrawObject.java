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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/drawobjects/DrawObject.java,v 1.35 2004/05/05 08:04:58 tom Exp $
 */

/*
 * DrawObject.java
 *
 * Created on 21. Mai 2002, 22:41
 */

package at.bestsolution.drawswf.drawobjects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.xml.transform.TransformerException;

import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Transform;
import com.anotherbigidea.flash.structs.AlphaColor;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.ext.swing.dialog.JGradientChooser;

/**
 * An abstract class which represents the parent
 * of all draw objects.
 * @author tom
 */
public abstract class DrawObject
{
	/** the stroke used to draw */
	protected BasicStroke stroke_;
	/** the color of lines */
	protected Color pen_color_;
	/** the fill color */
	protected Color fill_color_;
	/** this is the delay in milliseconds for animation */
	protected static final long ANIMATION_DELAY = 5;
	/** name of the draw object */
	protected String name_;
	/** gradient */
	protected MultipleGradientPaint gradient_ = null;

	//----------------------------------------------------------------------------
	/**
	 * Constructor of DrawObject
	 *
	 * @param name the name of the drawobject
	 */
	public DrawObject(String name)
	{
		name_ = name;
	}

	//----------------------------------------------------------------------------
	/**
	 * This method is called to draw the object onto the
	 * drawing panel.
	 *
	 * @param g the graphics context.
	 * @param panel to test isReplay.
	 */
	public abstract void drawObject(Graphics2D g, DrawingPanel panel);

	//----------------------------------------------------------------------------
	/**
	 * This method is called to draw the object into the
	 * flash movie.
	 *
	 * @param movie the movie to add the drawing.
	 * @param layer of the actual draw object.
	 * @param the speed to draw the animation.
	 */
	public abstract void drawObject(Movie movie, int layer, int speed);

	//----------------------------------------------------------------------------
	/**
	 * This is called by the DrawingPanel when the mouse button is pressed.
	 *
	 * @param x the x position of the mouse pointer
	 * @param y the y position of the mouse pointer
	 * @param g the graphics context to draw to.
	 * @see at.bestsolution.drawswf.DrawingPanel
	 */
	public abstract void mousePressed(int x, int y, Graphics2D g);

	//----------------------------------------------------------------------------
	/**
	 * This is called by the DrawingPanel when the mouse is dragged.
	 *
	 * @param x the x position of the mouse pointer
	 * @param y the y position of the mouse pointer
	 * @param g the graphics context to draw to.
	 * @see at.bestsolution.drawswf.DrawingPanel
	 */
	public abstract void mouseDragged(int x, int y, Graphics2D g);

	//----------------------------------------------------------------------------
	/**
	 * This is called by the DrawingPanel when the mouse button is released.
	 *
	 * @param x the x position of the mouse pointer
	 * @param y the y position of the mouse pointer
	 * @param g the graphics context to draw to.
	 * @see at.bestsolution.drawswf.DrawingPanel
	 */
	public abstract boolean mouseReleased(int x, int y, Graphics2D g);

	//----------------------------------------------------------------------------
	protected abstract String toSVG(long time);

	//----------------------------------------------------------------------------
	protected abstract void createAWTObject(Element xml_node, double scale);

	//----------------------------------------------------------------------------
	/**
	 * Set the color of the pen.
	 *
	 * @param color the new color.
	 */
	public void setColor(Color color)
	{
		pen_color_ = color;
	}

	//----------------------------------------------------------------------------
	/**
	 * Set the fill color.
	 *
	 * @param color the new color.
	 */
	public void setFillColor(Color color)
	{
		fill_color_ = color;
	}

	//----------------------------------------------------------------------------
	/**
	 * Set the stroke of lines.
	 *
	 * @param stroke the new stroke.
	 */
	public void setStroke(BasicStroke stroke)
	{
		stroke_ = stroke;
	}

	//----------------------------------------------------------------------------
	/**
	 * Wait a given delay.
	 *
	 * @param milli the delay in milliseconds.
	 */
	protected void pause(long milli)
	{
		try
		{
			Thread.sleep(milli);
			Thread.yield();
		}
		catch (InterruptedException e)
		{}
	}

	//----------------------------------------------------------------------------
	/**
	 * Extract Color with alpha channel from given SVG values
	 * 
	 * @param color the RGB-Color as hash-code
	 * @param alpha_text Alpha-Channel as value ranging from 0 to 1
	 * @return parsed_color RGB-Color with alpha values set 
	 */
	private Color parseSVGColor(String color, String alpha_text)
	{
		int rgb;
		int red = 0;
		int green = 0;
		int blue = 0;
		int alpha = 0;

		try
		{
			rgb = Integer.decode(color).intValue();

			red = (rgb >>> 16) & 0xff;
			green = (rgb >>> 8) & 0xff;
			blue = rgb & 0xff;

			alpha = (int) (Double.parseDouble(alpha_text) * 255.0);
		}
		catch (NumberFormatException e)
		{}

		Color parsed_color = new Color(red, green, blue, alpha);

		return parsed_color;
	}

	//----------------------------------------------------------------------------
	/**
	 * This method transforms AWT-Colors into SVG-Colors (hex-encoded)
	 * 
	 * @param color AWT-Color which has to be transformed into SVG
	 * @return color in hex notation
	 */
	private String encodeColor(Color color)
	{
		String encoded = Integer.toHexString(0xffffff & color.getRGB());

		if (encoded.length() < 6)
		{
			// fill string with zeros so it has always length of 6
			encoded = "000000".substring(encoded.length()) + encoded;
		}

		return encoded;
	}

	//----------------------------------------------------------------------------
	protected String toSVGFillColor()
	{
		return " fill=\"#" + encodeColor(fill_color_) + "\" fill-opacity=\"" + fill_color_.getAlpha() / 255.0 + "\"";
	}

	protected String toSVGColor(String name, Color color)
	{
		return " " + name + "=\"#" + encodeColor(color) + "\" " + name + "-opacity=\"" + color.getAlpha() / 255.0 + "\"";
	}

	protected String toSVGGradientColor(Color color)
	{
		return " stop-color=\"#" + encodeColor(color) + "\" stop-opacity=\"" + color.getAlpha() / 255.0 + "\"";
	}

	protected String toSVGGradient(String name, MultipleGradientPaint gradient)
	{
		String rv = "";

		if (gradient instanceof LinearGradientPaint)
		{
			LinearGradientPaint tmp_gradient = (LinearGradientPaint) gradient;
			rv = "<linearGradient id=\"" + name + "\" gradientUnits = \"userSpaceOnUse\"";
			rv += " x1=\"" + tmp_gradient.getStartPoint().getX() + "\"";
			rv += " y1=\"" + tmp_gradient.getStartPoint().getY() + "\"";
			rv += " x2=\"" + tmp_gradient.getEndPoint().getX() + "\"";
			rv += " y2=\"" + tmp_gradient.getEndPoint().getY() + "\"";
			rv += " spreadMethod=\"" + translateCycleMethodToSVG(tmp_gradient.getCycleMethod()) + "\">";
			rv += "<stop offset=\"0\" " + toSVGGradientColor(tmp_gradient.getColors()[0]) + "/>";
			rv += "<stop offset=\"1\" " + toSVGGradientColor(tmp_gradient.getColors()[1]) + "/>";
			rv += "</linearGradient>";
		}
		else
		{
			RadialGradientPaint tmp_gradient = (RadialGradientPaint) gradient;
			rv = "<radialGradient id=\"" + name + "\" gradientUnits = \"userSpaceOnUse\"";
			rv += " cx=\"" + tmp_gradient.getCenterPoint().getX() + "\"";
			rv += " cy=\"" + tmp_gradient.getCenterPoint().getY() + "\"";
			rv += " r=\"" + tmp_gradient.getRadius() + "\"";
			rv += " fx=\"" + tmp_gradient.getFocusPoint().getX() + "\"";
			rv += " fy=\"" + tmp_gradient.getFocusPoint().getY() + "\"";
			rv += " spreadMethod=\"" + translateCycleMethodToSVG(tmp_gradient.getCycleMethod()) + "\">";
			rv += "<stop offset=\"0\" " + toSVGGradientColor(tmp_gradient.getColors()[0]) + "/>";
			rv += "<stop offset=\"1\" " + toSVGGradientColor(tmp_gradient.getColors()[1]) + "/>";
			rv += "</radialGradient>";
		}

		return rv;
	}

	private String translateCycleMethodToSVG(MultipleGradientPaint.CycleMethodEnum type)
	{
		String rv = "";
		if (MultipleGradientPaint.NO_CYCLE == type)
		{
			rv = "pad";
		}
		else if (MultipleGradientPaint.REPEAT == type)
		{
			rv = "repeat";
		}
		else
		{
			rv = "reflect";
		}

		return rv;
	}

	//----------------------------------------------------------------------------
	protected String toSVGStroke()
	{
		return " stroke=\"#"
			+ encodeColor(pen_color_)
			+ "\" stroke-width=\""
			+ stroke_.getLineWidth()
			+ "\" stroke-opacity=\""
			+ pen_color_.getAlpha() / 255.0
			+ "\" stroke-linecap=\"round\"";
	}

	//----------------------------------------------------------------------------
	protected void setAWTFillColor(Element svg_element)
	{
		fill_color_ = parseSVGColor(svg_element.getAttribute("fill"), svg_element.getAttribute("fill-opacity"));
	}

	//----------------------------------------------------------------------------
	protected void setAWTStroke(Element svg_element, double scale)
	{
		pen_color_ = parseSVGColor(svg_element.getAttribute("stroke"), svg_element.getAttribute("stroke-opacity"));
		stroke_ = new BasicStroke((float) scale * Float.parseFloat(svg_element.getAttribute("stroke-width")), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}

	//----------------------------------------------------------------------------
	protected void waitMovie(Movie movie, int speed)
	{
		// sleep in flash...
		for (int count = 0; count < 12 / speed; count++)
		{
			movie.appendFrame();
		}
	}

	//----------------------------------------------------------------------------
	public String getName()
	{
		return name_;
	}

	//	----------------------------------------------------------------------------
	public void setGradient(MultipleGradientPaint gradient)
	{
		gradient_ = gradient;
	}

	//	----------------------------------------------------------------------------
	public MultipleGradientPaint getGradient()
	{
		return gradient_;
	}

	//----------------------------------------------------------------------------
	protected void repaint()
	{
		MainWindow.drawing_panel_.repaint();
	}

	//----------------------------------------------------------------------------
	public abstract boolean contains(Point2D p);

	//----------------------------------------------------------------------------
	public abstract void move(double x, double y);

	public boolean hasGradient()
	{
		boolean rv = false;

		if (gradient_ != null)
		{
			rv = true;
		}

		return rv;
	}

	public String getGradientAsSVG()
	{
		return toSVGGradient(name_, gradient_);
	}

	//----------------------------------------------------------------------------
	protected void moveGradient(double x, double y)
	{
		if (gradient_ != null)
		{
			if (gradient_ instanceof LinearGradientPaint)
			{
				LinearGradientPaint tmp_gradient = (LinearGradientPaint) gradient_;
				Point2D.Double start = new Point2D.Double(tmp_gradient.getStartPoint().getX() + x, tmp_gradient.getStartPoint().getY() + y);
				Point2D.Double end = new Point2D.Double(tmp_gradient.getEndPoint().getX() + x, tmp_gradient.getEndPoint().getY() + y);
				gradient_ = new LinearGradientPaint(start, end, tmp_gradient.getFractions(), tmp_gradient.getColors(), tmp_gradient.getCycleMethod(), tmp_gradient.getColorSpace());
			}
			else
			{
				RadialGradientPaint tmp_gradient = (RadialGradientPaint) gradient_;
				Point2D.Double center = new Point2D.Double(tmp_gradient.getCenterPoint().getX() + x, tmp_gradient.getCenterPoint().getY() + y);
				gradient_ =
					new RadialGradientPaint(
						center,
						tmp_gradient.getRadius(),
						center,
						tmp_gradient.getFractions(),
						tmp_gradient.getColors(),
						tmp_gradient.getCycleMethod(),
						tmp_gradient.getColorSpace());
			}
		}
	}

	protected void setAWTFill(Element svg_element)
	{
		String fill = svg_element.getAttribute("fill");
		String rv = "";

		if (fill.indexOf("url(#") != -1)
		{
			setAWTFillGradient(svg_element);
			fill_color_ = Color.RED;
		}
		else
		{
			setAWTFillColor(svg_element);
		}
	}

	public void setAWTFillGradient(Element svg_element)
	{
		String fill = svg_element.getAttribute("fill");

		try
		{
			Node defs_node = ((Element)svg_element.getParentNode()).getElementsByTagName("defs").item(0);
			Element gradient = (Element) XPathAPI.selectSingleNode(defs_node, "*[@id='" + fill.substring(fill.indexOf('#') + 1, fill.indexOf(')')) + "']");
			gradient_ = getGradientFromSVG(gradient);
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
	}

	private MultipleGradientPaint getGradientFromSVG(Element xml_gradient)
	{
		MultipleGradientPaint awt_gradient = null;

		if (xml_gradient.getNodeName().equals("linearGradient"))
		{
			Point2D.Double start = new Point2D.Double(Double.parseDouble(xml_gradient.getAttribute("x1")), Double.parseDouble(xml_gradient.getAttribute("y1")));
			Point2D.Double end = new Point2D.Double(Double.parseDouble(xml_gradient.getAttribute("x2")), Double.parseDouble(xml_gradient.getAttribute("y2")));
			awt_gradient =
				new LinearGradientPaint(
					start,
					end,
					JGradientChooser.fractions_,
					translateSVGGradientColorsToAWT(xml_gradient),
					translateSVGSpreadToCycleMethod(xml_gradient),
					MultipleGradientPaint.SRGB);
		}
		else
		{
			Point2D.Double center = new Point2D.Double(Double.parseDouble(xml_gradient.getAttribute("cx")), Double.parseDouble(xml_gradient.getAttribute("cy")));
			float radius = Float.parseFloat(xml_gradient.getAttribute("r"));

			awt_gradient =
				new RadialGradientPaint(
					center,
					radius,
					center,
					JGradientChooser.fractions_,
					translateSVGGradientColorsToAWT(xml_gradient),
					translateSVGSpreadToCycleMethod(xml_gradient),
					MultipleGradientPaint.SRGB);
		}

		return awt_gradient;
	}

	private MultipleGradientPaint.CycleMethodEnum translateSVGSpreadToCycleMethod(Element xml_gradient)
	{
		MultipleGradientPaint.CycleMethodEnum rv;
		String method = xml_gradient.getAttribute("spreadMethod");

		if (method.equals("pad"))
		{
			rv = MultipleGradientPaint.NO_CYCLE;
		}
		else if (method.equals("repeat"))
		{
			rv = MultipleGradientPaint.REPEAT;
		}
		else
		{
			rv = MultipleGradientPaint.REFLECT;
		}

		return rv;
	}

	private Color[] translateSVGGradientColorsToAWT(Element xml_gradient)
	{
		NodeList stop_nodes = xml_gradient.getElementsByTagName("stop");
		Color[] colors = new Color[stop_nodes.getLength()];
		Node stop_node;
		NamedNodeMap att_nodes;

		for (int i = 0; i < stop_nodes.getLength(); i++)
		{
			stop_node = stop_nodes.item(i);
			att_nodes = stop_node.getAttributes();
			colors[i] = parseSVGColor(att_nodes.getNamedItem("stop-color").getNodeValue(), att_nodes.getNamedItem("stop-opacity").getNodeValue());
		}

		return colors;
	}
	
	protected AlphaColor[] getGradientColors4Flash( MultipleGradientPaint gradient )
	{
		Color[] colors = gradient.getColors();
		AlphaColor[] alpha_colors = new AlphaColor[colors.length];
		
		for( int i = 0; i < colors.length; i++ )
		{
			alpha_colors[i] = new AlphaColor( colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), colors[i].getAlpha() );
		}
		
		System.out.println("Colors: " + colors.length);
		
		return alpha_colors;
	}
	
	protected int[] getRatios4Flash( MultipleGradientPaint gradient )
	{
		int[] ratios = new int[2];
		
		ratios[0] = 0;
		ratios[1] = 255;
		
		return ratios;
	}
	
	protected Transform getTransform4Flash( MultipleGradientPaint gradient )
	{
		Transform transform = new Transform();
		
			AffineTransform g_transform = gradient.getTransform();
			
			transform.setTranslateX(g_transform.getTranslateX());
			transform.setTranslateY(g_transform.getTranslateY());
			transform.setScaleX( g_transform.getScaleX() );
			transform.setScaleY( g_transform.getScaleY() );
			transform.setScaleX( g_transform.getShearX() );
			transform.setScaleY( g_transform.getShearY() );
		
		return transform;
	}
	
	protected boolean isRadial( MultipleGradientPaint gradient )
	{
		return ! (gradient instanceof LinearGradientPaint);
	}
}