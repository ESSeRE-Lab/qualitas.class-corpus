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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/drawobjects/StraightLine.java,v 1.26 2003/06/07 20:39:47 tom Exp $
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
import java.awt.geom.Line2D;
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
public class StraightLine extends DrawObject
{
    private Line2D line_;
    private static int instance_counter = 1;
    private TableModel model_;
    private JTable options_table_;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of Line */
    public StraightLine()
    {
        super( "StraightLine" + instance_counter++ );
        line_ = null;
    }
    
    //----------------------------------------------------------------------------
    public void drawObject(Movie movie, int layer, int speed)
    {
        Frame frame = movie.appendFrame();
        
        AlphaColor line_color = new AlphaColor( pen_color_.getRed(), pen_color_.getGreen(), pen_color_.getBlue(), pen_color_.getAlpha() );
        
        Shape shape = new Shape();
        shape.defineLineStyle((stroke_.getLineWidth()), line_color);
        shape.setLineStyle(1);
        shape.move( line_.getX1(), line_.getY1());
        shape.line( line_.getX2(), line_.getY2());
        
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
        g.setColor(pen_color_);
        g.draw( line_ );
        if (panel.isReplay() == true)
        {
            pause(100*ANIMATION_DELAY);
        }
    }
    
    //----------------------------------------------------------------------------
    public void mouseDragged(int x, int y, Graphics2D g)
    {
        g.setXORMode(Color.white);
        
        g.setColor(pen_color_);
        g.setStroke(stroke_);
        g.draw( line_ );
        line_ = new Line2D.Double(line_.getX1(), line_.getY1(), x, y);
        g.draw( line_ );
    }
    
    //----------------------------------------------------------------------------
    public void mousePressed(int x, int y, Graphics2D g)
    {
        g.setColor(pen_color_);
        g.setStroke(stroke_);
        line_ = new Line2D.Double(x, y, x, y);
        g.draw( line_ );
    }
    
    //----------------------------------------------------------------------------
    public boolean mouseReleased(int x, int y, Graphics2D g)
    {
        g.setXORMode(Color.white);
        
        g.setColor(pen_color_);
        g.setStroke(stroke_);
        g.draw( line_ );
        line_ = new Line2D.Double(line_.getX1(), line_.getY1(), x, y);
        g.setPaintMode();
        g.setColor(fill_color_);
        g.fill( line_ );
        g.setColor(pen_color_);
        g.draw( line_ );
        
        return ( line_.getP1().distance(line_.getP2()) > 0.0 );
    }
    
    //----------------------------------------------------------------------------
    protected String toSVG(long time)
    {
        return "<" + DrawObjectFactory.SVG_STRAIGHT_LINE
        + " x1=\"" + line_.getX1()
        + "\" y1=\"" + line_.getY1()
        + "\" x2=\"" + line_.getX2()
        + "\" y2=\"" + line_.getY2() + "\" "
        + toSVGStroke() + "/>";
    }
    
    //----------------------------------------------------------------------------
    protected void createAWTObject(Element xml_node, double scale)
    {
        double x1 = Double.parseDouble( xml_node.getAttribute("x1") );
        double y1 = Double.parseDouble( xml_node.getAttribute("y1") );
        double x2 = Double.parseDouble( xml_node.getAttribute("x2") );
        double y2 = Double.parseDouble( xml_node.getAttribute("y2") );
        
        setAWTStroke(xml_node, scale);
        
        line_ = new Line2D.Double( x1 * scale, y1 * scale, x2 * scale, y2 * scale );
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyX1( String x )
    {
        line_.setLine( Double.parseDouble( x ), line_.getY1(), line_.getX2(), line_.getY2() );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyX1()
    {
        return line_.getX1();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyY1( String y )
    {
        line_.setLine( line_.getX1(), Double.parseDouble( y ), line_.getX2(), line_.getY2() );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyY1()
    {
        return line_.getY1();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyX2( String x )
    {
        line_.setLine( line_.getX1(), line_.getY1(), Double.parseDouble( x ), line_.getY2() );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyX2()
    {
        return line_.getX2();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyY2( String y )
    {
        line_.setLine( line_.getX1(), line_.getY2(), line_.getX2(), Double.parseDouble( y ) );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyY2()
    {
        return line_.getY2();
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
        return line_.intersects( p.getX() - 1.0, p.getY() - 1.0, 3.0, 3.0);
    }
    
    //----------------------------------------------------------------------------
    public void move(double x, double y)
    {
        line_.setLine( line_.getX1() + x, line_.getY1() + y, line_.getX2() + x, line_.getY2() + y );
    }
    
	public String getGradientAsSVG()
	{
		return null;
	}

}
