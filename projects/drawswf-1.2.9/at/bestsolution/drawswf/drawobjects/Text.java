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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/drawobjects/Text.java,v 1.27 2003/06/08 09:34:32 tom Exp $
 */

/*
 * Line.java
 *
 * Created on 21. Mai 2002, 22:57
 */

package at.bestsolution.drawswf.drawobjects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.font.GlyphVector;
import java.awt.FontMetrics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Shape;
import com.anotherbigidea.flash.movie.Instance;
import com.anotherbigidea.flash.movie.Transform;
import com.anotherbigidea.flash.structs.AlphaTransform;
import com.anotherbigidea.flash.structs.AlphaColor;

import org.w3c.dom.Element;

import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.dialog.FontDialog;
import at.bestsolution.ext.awt.FontLoader;

/**
 *
 * @author  tom
 */
public class Text extends DrawObject implements ChangeListener
{
	private Point2D position_;
	private Rectangle2D bounds_;
	private Point2D dummy_;
	//    private String text_;
	private DrawSWFFont font_;
	private static FontDialog font_dialog_ = null;
	private TableModel model_;
	private JTable options_table_;

	public static final int NO_EFFECT = 0;
	public static final int EFFECT_TYPE = 1;
	public static final int EFFECT_FADE_IN = 2;
	private static int instance_counter = 1;

	//----------------------------------------------------------------------------
	/** Creates a new instance of Line */
	public Text()
	{
		super("Text" + instance_counter++);
		position_ = null;
		font_ = null;
		bounds_ = null;
	}

	//----------------------------------------------------------------------------
	protected void noEffect(Movie movie, int speed)
	{
		Frame frame = movie.appendFrame();
		AlphaColor fill_color = new AlphaColor(font_.getColor().getRed(), font_.getColor().getGreen(), font_.getColor().getBlue(), font_.getColor().getAlpha());
		GlyphVector glyphs = font_.getAWTFont().layoutGlyphVector(DrawSWFFont.CONTEXT, font_.getText().toCharArray(), 0, font_.getText().length(), Font.LAYOUT_LEFT_TO_RIGHT);

		Shape shape = new Shape();
		shape.defineFillStyle(fill_color);
		shape.setRightFillStyle(1);
		shape.drawAWTPathIterator(glyphs.getOutline().getPathIterator(null));

		Instance instance = frame.placeSymbol(shape, (int) position_.getX(), (int) position_.getY());

		waitMovie(movie, speed);
	}

	//----------------------------------------------------------------------------
	private void alterFrame(Frame frame, Instance instance, double x, double y, double sx, double sy, double alpha)
	{
		Transform transform;
		AlphaTransform alpha_transform;

		transform = new Transform();
		transform.setTranslateX(x);
		transform.setTranslateY(y);
		transform.setScaleX(sx);
		transform.setScaleY(sy);
		alpha_transform = new AlphaTransform();
		alpha_transform.setMultAlpha(alpha);
		frame.alter(instance, transform, alpha_transform);
	}

	//----------------------------------------------------------------------------
	protected void fadeIn(Movie movie, int speed)
	{
		Frame frame;
		Instance instance;
		AlphaColor fill_color = new AlphaColor(font_.getColor().getRed(), font_.getColor().getGreen(), font_.getColor().getBlue(), font_.getColor().getAlpha());
		GlyphVector glyphs = font_.getAWTFont().layoutGlyphVector(DrawSWFFont.CONTEXT, font_.getText().toCharArray(), 0, font_.getText().length(), Font.LAYOUT_LEFT_TO_RIGHT);
		Shape shape = new Shape();
		shape.defineFillStyle(fill_color);
		shape.setRightFillStyle(1);
		shape.drawAWTPathIterator(glyphs.getOutline().getPathIterator(null));

		double width = glyphs.getLogicalBounds().getWidth() / 2.0;
		double center = position_.getX() + width;
		double scale = 2.0;

		frame = movie.appendFrame();
		instance = frame.placeSymbol(shape, new Transform(), new AlphaTransform());
		alterFrame(frame, instance, center - width * scale, position_.getY(), scale, 0.5, 0.025);

		for (int i = speed; i <= 40; i += speed)
		{
			frame = movie.appendFrame();
			scale = 1.0 + (40 - i) / 40.0;
			alterFrame(frame, instance, center - width * scale, position_.getY(), scale, 0.5 + i / 80.0, i / 40.0);
		}

		frame = movie.appendFrame();
		scale = 1.0;
		alterFrame(frame, instance, center - width * scale, position_.getY(), scale, 1.0, 1.0);
	}

	//----------------------------------------------------------------------------
	protected void typeLetters(Movie movie, int speed)
	{
		Frame frame;
		Instance instance;
		AlphaColor fill_color = new AlphaColor(font_.getColor().getRed(), font_.getColor().getGreen(), font_.getColor().getBlue(), font_.getColor().getAlpha());
		GlyphVector glyphs = font_.getAWTFont().layoutGlyphVector(DrawSWFFont.CONTEXT, font_.getText().toCharArray(), 0, font_.getText().length(), Font.LAYOUT_LEFT_TO_RIGHT);
		Shape shape;

		for (int char_index = 0; char_index < glyphs.getNumGlyphs(); char_index++)
		{
			if (font_.getText().charAt(char_index) != ' ')
			{
				shape = new Shape();
				shape.defineFillStyle(fill_color);
				shape.setRightFillStyle(1);
				shape.drawAWTPathIterator(glyphs.getGlyphOutline(char_index).getPathIterator(null));

				double width = glyphs.getGlyphOutline(char_index).getBounds2D().getX() + glyphs.getGlyphOutline(char_index).getBounds2D().getWidth() / 2.0;
				double height = glyphs.getLogicalBounds().getHeight() / 2.0;
				double center_x = position_.getX() + width;
				double center_y = position_.getY() - height;
				double scale = 2.0;

				frame = movie.appendFrame();
				instance = frame.placeSymbol(shape, new Transform(), new AlphaTransform());
				alterFrame(frame, instance, center_x - width * scale, center_y + height * scale, scale, scale, 0.05);

				for (int i = speed; i <= 10; i += speed)
				{
					frame = movie.appendFrame();
					scale = 0.8 + (10 - i) / 10.0;
					alterFrame(frame, instance, center_x - width * scale, center_y + height * scale, scale, scale, i / 10.0);
				}

				frame = movie.appendFrame();
				scale = 1.0;
				alterFrame(frame, instance, center_x - width * scale, center_y + height * scale, scale, scale, 1.0);
			}
		}
	}

	//----------------------------------------------------------------------------
	public void drawObject(Movie movie, int layer, int speed)
	{
		switch (font_.getEffect())
		{
			case DrawSWFFont.NO_EFFECT :
				noEffect(movie, speed);
				break;
			case DrawSWFFont.EFFECT_FADE_IN :
				fadeIn(movie, speed);
				break;
			case DrawSWFFont.EFFECT_TYPE :
				typeLetters(movie, speed);
				break;
		}
	}

	//----------------------------------------------------------------------------
	public void drawObject(Graphics2D g, DrawingPanel panel)
	{
		g.setFont(font_.getAWTFont());

		if (gradient_ == null)
		{
			g.setColor(font_.getColor());
		}
		else
		{
			g.setPaint(gradient_);
		}

		bounds_ = font_.getAWTFont().getStringBounds(font_.getText(), DrawSWFFont.CONTEXT);

		g.drawString(font_.getText(), (int) position_.getX(), (int) position_.getY());

		if (panel.isReplay() == true)
		{
			pause(100 * ANIMATION_DELAY);
		}
	}

	//----------------------------------------------------------------------------
	public void mouseDragged(int x, int y, Graphics2D g)
	{
		g.setXORMode(Color.white);

		int xx = (int) dummy_.getX();
		int yy = (int) dummy_.getY();

		g.drawLine(xx, yy, xx + 50, yy);
		g.drawLine(xx, yy, xx, yy + 50);

		dummy_ = new Point2D.Double(x, y);
		g.setColor(Color.BLACK);

		g.drawLine(x, y, x + 50, y);
		g.drawLine(x, y, x, y + 50);
	}

	//----------------------------------------------------------------------------
	private void showFontDialog()
	{
		if (font_dialog_ == null)
		{
			font_dialog_ = new FontDialog(MainWindow.MAIN_WINDOW);
		}

		font_dialog_.removeAllChangeListeners();
		font_dialog_.addChangeListener(this);

		font_dialog_.show();
	}

	//----------------------------------------------------------------------------
	public void mousePressed(int x, int y, Graphics2D g)
	{
		dummy_ = new Point2D.Double(x, y);

		g.setColor(Color.BLACK);
		g.drawLine(x, y, x + 50, y);
		g.drawLine(x, y, x, y + 50);
	}

	//----------------------------------------------------------------------------
	public boolean mouseReleased(int x, int y, Graphics2D g)
	{
		boolean result = false;

		showFontDialog();

		font_ = font_dialog_.getDrawSWFFont();

		g.setXORMode(Color.white);

		int xx = (int) dummy_.getX();
		int yy = (int) dummy_.getY();

		g.drawLine(xx, yy, xx + 50, yy);
		g.drawLine(xx, yy, xx, yy + 50);

		g.setPaintMode();

		if (fill_color_ != null && font_ != null && font_.getText() != null && !font_.getText().equals(""))
		{
			g.setColor(font_.getColor());
			g.setFont(font_.getAWTFont());

			FontMetrics fm = g.getFontMetrics();
			position_ = new Point2D.Double(x, y + fm.getHeight());

			g.drawString(font_.getText(), (int) position_.getX(), (int) position_.getY());
			result = true;
		}

		return result;
	}

	//----------------------------------------------------------------------------
	protected String toSVG(long time)
	{
		return "<"
			+ DrawObjectFactory.SVG_TEXT
			+ " x=\""
			+ position_.getX()
			+ "\" y=\""
			+ position_.getY()
			+ "\" font-size=\""
			+ font_.getAWTFont().getSize()
			+ "\" font-family=\""
			+ font_.getAWTFont().getFamily()
			+ "\" "
			+ toSVGColor("fill", font_.getColor())
			+ " "
			+ "drawswf:effect=\""
			+ font_.getEffect()
			+ "\">"
			+ font_.getText()
			+ "</"
			+ DrawObjectFactory.SVG_TEXT
			+ ">";
	}

	//----------------------------------------------------------------------------
	protected void createAWTObject(Element xml_node, double scale)
	{
		double x = Double.parseDouble(xml_node.getAttribute("x"));
		double y = Double.parseDouble(xml_node.getAttribute("y"));
		String font_name = xml_node.getAttribute("font-family");
		int font_size;
		int effect = 0;

		try
		{
			font_size = Integer.parseInt(xml_node.getAttribute("font-size"));
		}
		catch (NumberFormatException e)
		{
			font_size = 12;
		}

		System.out.println("Get the effect...");

		String tmp = xml_node.getAttributeNS("http://drawswf.sf.net/2002/drawswf", "effect");
		// System.out.println("Found effect = " + tmp);

		if (!tmp.equals(""))
		{
			effect = Integer.parseInt(tmp);
		}

		setAWTFillColor(xml_node);
		font_ = new DrawSWFFont(FontLoader.getInstance().getFont(font_name, Font.PLAIN, font_size), effect, xml_node.getFirstChild().getNodeValue(), fill_color_);
		// font_ = new DrawSWFFont( new Font( font_name, Font.PLAIN, font_size ), effect, xml_node.getFirstChild().getNodeValue() );

		position_ = new Point2D.Double(x * scale, y * scale);
	}

	//----------------------------------------------------------------------------
	public void setPropertyX(String x)
	{
		// position_ = new Point((int) Double.parseDouble(x), (int) position_.getY());
		move(  Double.parseDouble(x)-position_.getX(),0);
		repaint();
	}

	//----------------------------------------------------------------------------
	public double getPropertyX()
	{
		return position_.getX();
	}

	//----------------------------------------------------------------------------
	public void setPropertyY(String y)
	{
		// position_ = new Point((int) position_.getX(), (int) Double.parseDouble(y));
		move(  0, Double.parseDouble(y)-position_.getY());
		repaint();
	}

	//----------------------------------------------------------------------------
	public double getPropertyY()
	{
		return position_.getY();
	}

	//----------------------------------------------------------------------------
	public void setPropertyFillColor(Color fill_color)
	{
		font_.setColor(fill_color);
		repaint();
	}

	//----------------------------------------------------------------------------
	public Color getPropertyFillColor()
	{
		return font_.getColor();
	}

	//----------------------------------------------------------------------------
	public void setPropertyFont(DrawSWFFont font)
	{
		font_ = font;
		repaint();
	}

	//----------------------------------------------------------------------------
	public DrawSWFFont getPropertyFont()
	{
		return font_;
	}

	//----------------------------------------------------------------------------
	public void setPropertyText(String text)
	{
		font_.setText(text);
		repaint();
	}

	//----------------------------------------------------------------------------
	public String getPropertyText()
	{
		return font_.getText();
	}

	//    public void setPropertyEffect( int effect )
	//    {
	//        
	//    }
	//    
	//    public void getPropertyText()
	//    {
	//        
	//    }

	//----------------------------------------------------------------------------
	public boolean contains(Point2D p)
	{
		boolean result;
		Point2D corrected = new Point2D.Double(p.getX() - position_.getX(), p.getY() - position_.getY());

		if (bounds_ == null)
		{
			result = position_.distance(corrected) < 10.0;
		}
		else
		{
			result = bounds_.intersects(corrected.getX() - 1.0, corrected.getY() - 1.0, 3.0, 3.0);
		}

		return result;
	}

	//----------------------------------------------------------------------------
	public void move(double x, double y)
	{
		position_.setLocation(position_.getX() + x, position_.getY() + y);
		moveGradient(x,y);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() instanceof FontDialog)
		{
			FontDialog dialog = (FontDialog) e.getSource();

			font_ = dialog.getDrawSWFFont();
			repaint();
		}
	}
}
