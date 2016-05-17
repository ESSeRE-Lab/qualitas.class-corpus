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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/drawobjects/Rectangle.java,v 1.43 2004/05/05 08:04:58 tom Exp $
 */

/*
 * Line.java
 *
 * Created on 21. Mai 2002, 22:57
 */

package at.bestsolution.drawswf.drawobjects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.BasicStroke;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Shape;
import com.anotherbigidea.flash.movie.Instance;
import com.anotherbigidea.flash.structs.AlphaColor;

import org.w3c.dom.Element;

import at.bestsolution.drawswf.DrawingPanel;

/**
 *
 * @author  tom
 */
public class Rectangle extends DrawObject
{
    private Point2D start_point_;
    private Rectangle2D rect_;
    private static int instance_counter = 1;
    private TableModel model_;
    private JTable options_table_;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of Line */
    public Rectangle()
    {
        super( "Rectangle" + instance_counter++ );
        rect_        = null;
        start_point_ = null;
    }
    
    //----------------------------------------------------------------------------
    public void drawObject(Movie movie, int layer, int speed)
    {
        Frame frame = movie.appendFrame();
        Shape shape = new Shape();
        AlphaColor line_color = new AlphaColor( pen_color_.getRed(), pen_color_.getGreen(), pen_color_.getBlue(), pen_color_.getAlpha() );
        
        if( gradient_ == null )
        {
        	AlphaColor fill_color = new AlphaColor( fill_color_.getRed(), fill_color_.getGreen(), fill_color_.getBlue(), fill_color_.getAlpha() );
        	shape.defineFillStyle(fill_color);
        }
        else
        {
        	shape.defineFillStyle( getGradientColors4Flash(gradient_), getRatios4Flash(gradient_), getTransform4Flash(gradient_), isRadial(gradient_) );
        }

        shape.defineLineStyle((stroke_.getLineWidth()), line_color);
        shape.setLineStyle(1);
        shape.setRightFillStyle(1);
        
        shape.drawAWTPathIterator(rect_.getPathIterator(null));
        
        Instance instance = frame.placeSymbol(shape, 0, 0);
        
        // sleep in flash...
        for (int count = 0; count < 5; count++ )
        {
            movie.appendFrame();
        }
    }
    
    //----------------------------------------------------------------------------
    public void drawObject(Graphics2D g, DrawingPanel panel)
    {
        g.setStroke( stroke_ );
        
        if( gradient_ == null )
        {
			g.setColor( fill_color_ );
        }
        else
        {
        	g.setPaint(gradient_);
        }
        
		g.fill( rect_ );
        g.setColor(pen_color_);
        g.draw( rect_ );
        
        if (panel.isReplay() == true)
        {
            pause(100*ANIMATION_DELAY);
        }
    }
    
    //----------------------------------------------------------------------------
    private Rectangle2D.Double newRectangle2D(double end_x, double end_y)
    {
        double rx;
        double ry;
        double width;
        double height;
        double x = start_point_.getX();
        double y = start_point_.getY();
        
        if (x < end_x)
        {
            rx = x;
            width = end_x - x;
        }
        else
        {
            rx = end_x;
            width = x - end_x;
        }
        
        if (y < end_y)
        {
            ry = y;
            height = end_y - y;
        }
        else
        {
            ry = end_y;
            height = y - end_y;
        }
        
        return new Rectangle2D.Double(rx, ry, width, height);
    }
    
    //----------------------------------------------------------------------------
    public void mouseDragged(int x, int y, Graphics2D g)
    {
        g.setXORMode(Color.white);
        
        g.setColor(pen_color_);
        g.setStroke(stroke_);
        g.draw( rect_ );
        rect_ = newRectangle2D(x, y);
        g.draw( rect_ );
    }
    
    //----------------------------------------------------------------------------
    public void mousePressed(int x, int y, Graphics2D g)
    {
        g.setColor(pen_color_);
        g.setStroke(stroke_);
        start_point_ = new Point2D.Double(x, y);
        rect_ = newRectangle2D(x, y);
        g.draw( rect_ );
    }
    
    //----------------------------------------------------------------------------
    public boolean mouseReleased(int x, int y, Graphics2D g)
    {
        g.setXORMode(Color.white);
        
        g.setColor( pen_color_ );
        g.setStroke( stroke_ );
        g.draw( rect_ );
        rect_ = newRectangle2D( x, y );
        g.setPaintMode();
        
		if( gradient_ == null )
		{
			g.setColor( fill_color_ );
		}
		else
		{
			g.setPaint(gradient_);
		}
        
        g.fill( rect_ );
        g.setColor(pen_color_);
        g.draw( rect_ );

        return ( (rect_.getWidth() > 0.0) || (rect_.getHeight() > 0.0) );
    }
    
    //----------------------------------------------------------------------------
    protected String toSVG(long time)
    {
    	String rv = "<" + DrawObjectFactory.SVG_RECTANGLE
		+ " x=\"" + rect_.getX()
		+ "\" y=\"" + rect_.getY()
		+ "\" width=\"" + rect_.getWidth()
		+ "\" height=\"" + rect_.getHeight() + "\" ";
        
		if( gradient_ != null)
		{
			rv += "fill=\"url(#" + name_ + ")\"";
		}
		else
		{
			rv += toSVGFillColor();
		}
        
		rv += toSVGStroke() + "/>";
		
        return rv; 
    }
    
    //----------------------------------------------------------------------------
    protected void createAWTObject(Element xml_node, double scale)
    {
        double width  = Double.parseDouble( xml_node.getAttribute("width") );
        double height = Double.parseDouble( xml_node.getAttribute("height") );
        double x      = Double.parseDouble( xml_node.getAttribute("x") );
        double y      = Double.parseDouble( xml_node.getAttribute("y") );
        
        setAWTFill(xml_node);
        setAWTStroke(xml_node, scale);
        
        rect_ = new Rectangle2D.Double( x * scale, y * scale, width * scale, height * scale );
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyX( String x )
    {
        // rect_ = new java.awt.geom.Rectangle2D.Double( Double.parseDouble(x), rect_.getY(), rect_.getWidth(), rect_.getHeight() );
        move(  Double.parseDouble(x)-rect_.getX(),0);
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyX()
    {
        return rect_.getX();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyY( String y )
    {
        // rect_ = new java.awt.geom.Rectangle2D.Double( rect_.getX(), Double.parseDouble(y), rect_.getWidth(), rect_.getHeight() );
		move(  0, Double.parseDouble(y)-rect_.getY());
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyY()
    {
        return rect_.getY();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyWidth( String width )
    {
        rect_ = new java.awt.geom.Rectangle2D.Double( rect_.getX(), rect_.getY(), Double.parseDouble(width), rect_.getHeight() );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyWidth()
    {
        return rect_.getWidth();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyHeight( String height )
    {
        rect_ = new java.awt.geom.Rectangle2D.Double( rect_.getX(), rect_.getY(), rect_.getWidth(), Double.parseDouble( height ) );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyHeight()
    {
        return rect_.getHeight();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyFillColor( Color fill_color )
    {
        fill_color_ = fill_color;
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public Color getPropertyFillColor()
    {
        return fill_color_;
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyPenColor( Color pen_color )
    {
        pen_color_ = pen_color;
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public Color getPropertyPenColor()
    {
        return pen_color_;
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyStrokeWidth( String width )
    {
        stroke_ = new BasicStroke( Float.parseFloat( width ) );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public float getPropertyStrokeWidth()
    {
        return stroke_.getLineWidth();
    }
    
    //----------------------------------------------------------------------------
    public boolean contains(Point2D p)
    {
        return rect_.intersects( p.getX() - 1.0, p.getY() - 1.0, 3.0, 3.0);
    }
    
    //----------------------------------------------------------------------------
    public void move(double x, double y)
    {
        rect_ = new Rectangle2D.Double( rect_.getX() + x, rect_.getY() + y, rect_.getWidth(), rect_.getHeight() );
		moveGradient(x,y);        
    }
}
