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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/drawobjects/Ellipse.java,v 1.35 2004/05/05 08:04:58 tom Exp $
 */

/*
 * Line.java
 *
 * Created on 21. Mai 2002, 22:57
 */

package at.bestsolution.drawswf.drawobjects;

import at.bestsolution.drawswf.DrawingPanel;

import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Instance;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Shape;
import com.anotherbigidea.flash.structs.AlphaColor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.w3c.dom.Element;

/**
 *
 * @author  tom
 */
public class Ellipse extends DrawObject
{
    private Point2D start_point_;
    private Ellipse2D ellipse_;
    private static int instance_counter = 1;
    private TableModel model_;
    private JTable options_table_;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of Ellispe */
    public Ellipse()
    {
        super( "Ellipse" + instance_counter++ );
        ellipse_ = null;
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
        
        shape.drawAWTPathIterator(ellipse_.getPathIterator(null));

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
        g.setStroke(stroke_);
        
		if( gradient_ == null )
		{
			g.setColor( fill_color_ );
		}
		else
		{
			g.setPaint(gradient_);
		}

        g.fill( ellipse_ );
        g.setColor(pen_color_);
        g.draw( ellipse_ );
        if (panel.isReplay() == true)
        {
            pause(100*ANIMATION_DELAY);
        }
    }
    
    //----------------------------------------------------------------------------
    private java.awt.geom.Ellipse2D.Double newEllipse2D(double end_x, double end_y)
    {
        double ex;
        double ey;
        double width;
        double height;
        double x = start_point_.getX();
        double y = start_point_.getY();
        
        if (x < end_x)
        {
            ex = x;
            width = end_x - x;
        }
        else
        {
            ex = end_x;
            width = x - end_x;
        }
        
        if (y < end_y)
        {
            ey = y;
            height = end_y - y;
        }
        else
        {
            ey = end_y;
            height = y - end_y;
        }
        
        return new java.awt.geom.Ellipse2D.Double(ex, ey, width, height);
    }
    
    //----------------------------------------------------------------------------
    public void mouseDragged(int x, int y, Graphics2D g)
    {
        g.setXORMode(Color.white);
        
        g.setColor(pen_color_);
        g.setStroke(stroke_);
        g.draw( ellipse_ );
        ellipse_ = newEllipse2D(x, y);
        g.draw( ellipse_ );
    }
    
    //----------------------------------------------------------------------------
    public void mousePressed(int x, int y, Graphics2D g)
    {
        g.setColor(pen_color_);
        g.setStroke(stroke_);
        start_point_ = new Point2D.Double(x, y);
        ellipse_ = newEllipse2D(x, y);
        g.draw( ellipse_ );
    }
    
    //----------------------------------------------------------------------------
    public boolean mouseReleased(int x, int y, Graphics2D g)
    {
        g.setXORMode(Color.white);
        
        g.setColor(pen_color_);
        g.setStroke(stroke_);
        g.draw( ellipse_ );
        ellipse_ = newEllipse2D(x, y);
        g.setPaintMode();
        g.setColor(fill_color_);
        g.fill( ellipse_ );
        g.setColor(pen_color_);
        g.draw( ellipse_ );
        return ( (ellipse_.getWidth() > 0.0) || (ellipse_.getHeight() > 0.0) );
    }
    
    //----------------------------------------------------------------------------
    protected String toSVG(long time)
    {
        double radius_x = ellipse_.getWidth()  / 2.0;
        double radius_y = ellipse_.getHeight() / 2.0;
        
        return "<" + DrawObjectFactory.SVG_ELLIPSE
                + " cx=\""   + ( ellipse_.getX() + radius_x )
                + "\" cy=\"" + ( ellipse_.getY() + radius_y )
                + "\" rx=\"" + radius_x
                + "\" ry=\"" + radius_y + "\" "
                + toSVGFillColor()
                + toSVGStroke() + "/>";
    }
    
    //----------------------------------------------------------------------------
    protected void createAWTObject(Element xml_node, double scale)
    {
        double width  = 2.0 * Double.parseDouble( xml_node.getAttribute("rx") );
        double height = 2.0 * Double.parseDouble( xml_node.getAttribute("ry") );
        double x      = Double.parseDouble( xml_node.getAttribute("cx") ) - width/2.0;
        double y      = Double.parseDouble( xml_node.getAttribute("cy") ) - height/2.0;

        setAWTFillColor(xml_node);
        setAWTStroke(xml_node, scale);

        ellipse_ = new java.awt.geom.Ellipse2D.Double( x * scale, y * scale, width * scale, height * scale );
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyX( String x )
    {
        // ellipse_ = new java.awt.geom.Ellipse2D.Double( Double.parseDouble(x), ellipse_.getY(), ellipse_.getWidth(), ellipse_.getHeight() );
		move(  Double.parseDouble(x)-ellipse_.getX(),0);
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyX()
    {
        return ellipse_.getX();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyY( String y )
    {
        // ellipse_ = new java.awt.geom.Ellipse2D.Double( ellipse_.getX(), Double.parseDouble(y), ellipse_.getWidth(), ellipse_.getHeight() );
		move(  0, Double.parseDouble(y)-ellipse_.getY());
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyY()
    {
        return ellipse_.getY();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyWidth( String width )
    {
        ellipse_ = new java.awt.geom.Ellipse2D.Double( ellipse_.getX(), ellipse_.getY(), Double.parseDouble(width), ellipse_.getHeight() );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyWidth()
    {
        return ellipse_.getWidth();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyHeight( String height )
    {
        ellipse_ = new java.awt.geom.Ellipse2D.Double( ellipse_.getX(), ellipse_.getY(), ellipse_.getWidth(), Double.parseDouble( height ) );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyHeight()
    {
        return ellipse_.getHeight();
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
        return ellipse_.intersects( p.getX() - 1.0, p.getY() - 1.0, 3.0, 3.0);
    }
    
    //----------------------------------------------------------------------------
    public void move(double x, double y)
    {
        ellipse_ = new Ellipse2D.Double( ellipse_.getX() + x, ellipse_.getY() + y, ellipse_.getWidth(), ellipse_.getHeight() );
        moveGradient(x,y);
    }
}
