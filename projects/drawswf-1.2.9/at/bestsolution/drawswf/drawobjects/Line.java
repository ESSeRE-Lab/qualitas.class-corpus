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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/drawobjects/Line.java,v 1.30 2003/06/07 20:39:47 tom Exp $
 */

/*
 * Line.java
 *
 * Created on 21. Mai 2002, 22:57
 */

package at.bestsolution.drawswf.drawobjects;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.Color;
import java.awt.BasicStroke;

import java.util.ArrayList;

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
public class Line extends DrawObject
{
    private int last_x_;
    private int last_y_;
    private ArrayList points_;
    private static int instance_counter = 1;
    private TableModel model_;
    private JTable options_table_;
    
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of Line */
    public Line()
    {
        super( "Line" + instance_counter++ );
        points_ = new ArrayList();
    }
    
    //----------------------------------------------------------------------------
    public void drawObject(Movie movie, int layer, int speed)
    {
        Frame frame;
        Shape line_shape;
        AlphaColor alpha_color;
        int draw_points = speed + 1;
        Point[] point = new Point[draw_points];
        Instance instance;
        
        if ( points_.size() > 0 )
        {
            alpha_color = new AlphaColor( pen_color_.getRed(), pen_color_.getGreen(), pen_color_.getBlue(), pen_color_.getAlpha() );
            point[0] = (Point) points_.get(0);
            
            // draw all points with stepsize speed
            for (int count = 1; count < points_.size(); count += speed)
            {
                line_shape = new Shape();
                line_shape.defineLineStyle(stroke_.getLineWidth(), alpha_color);
                line_shape.setLineStyle(1);
                line_shape.move(point[0].x, point[0].y);
                
                // draw points for this step ( amount of points = speed + 1 = draw_points )
                for (int point_count = 1; point_count < draw_points; point_count++ )
                {
                    if (count + point_count < points_.size())
                    {
                        point[point_count] = (Point) points_.get(count + point_count);
                        line_shape.line(point[point_count].x, point[point_count].y);
                    }
                }
                
                // draw it into a new frame of SWF
                frame = movie.appendFrame();
                frame.placeSymbol(line_shape, 0, 0);
                
                // the startpoint of next step is our last point.
                point[0] = point[draw_points - 1];
            }
        }
    }
    
    //----------------------------------------------------------------------------
    public void drawObject(Graphics2D g, DrawingPanel panel)
    {
        Point p1;
        Point p2;
        
        if ( points_.size() > 0 )
        {
            p1 = (Point)points_.get(0);
            
            g.setColor(pen_color_);
            g.setStroke(stroke_);
            
            
            g.draw( new Line2D.Float(p1.x, p1.y, p1.x, p1.y) );
            
            for (int count = 1; count < points_.size(); count ++)
            {
                p2 = p1;
                p1 = (Point) points_.get(count);
                g.draw( new Line2D.Float(p1.x, p1.y, p2.x, p2.y) );
                
                if (panel.isReplay() == true)
                {
                    pause(ANIMATION_DELAY);
                }
            }
        }
    }
    
    //----------------------------------------------------------------------------
    public void mouseDragged(int x, int y, Graphics2D g)
    {
        if (new Point(x, y).distanceSq(last_x_, last_y_) >= 9.0)
        {
            g.setColor(pen_color_);
            g.setStroke(stroke_);
            g.draw( new Line2D.Float(last_x_, last_y_, x, y) );
            
            last_x_ = x;
            last_y_ = y;
            
            points_.add(new Point(x, y));
        }
    }
    
    //----------------------------------------------------------------------------
    public void mousePressed(int x, int y, Graphics2D g)
    {
        last_x_ = x;
        last_y_ = y;
        
        g.setColor(pen_color_);
        g.setStroke(stroke_);
        g.draw( new Line2D.Float(last_x_, last_y_, last_x_, last_y_) );
        
        points_.add(new Point(x, y));
    }
    
    //----------------------------------------------------------------------------
    public boolean mouseReleased(int x, int y, Graphics2D g)
    {
        g.setColor(pen_color_);
        g.setStroke(stroke_);
        g.draw( new Line2D.Float(last_x_, last_y_, last_x_, last_y_) );
        
        points_.add(new Point(x, y));
        return ( points_.size() > 1 );
    }
    
    //----------------------------------------------------------------------------
    protected String toSVG(long time)
    {
        Point single_point;
        StringBuffer pointsstring = new StringBuffer();
        
        for( int i = 0; i < points_.size(); i++ )
        {
            single_point = (Point) points_.get(i);
            pointsstring.append( (int) single_point.getX() + "," + (int) single_point.getY() + " " );
        }
        
        return "<" + DrawObjectFactory.SVG_LINE
        + " points=\"" + pointsstring.toString() + "\" "
        + toSVGStroke() + " fill=\"none\" />";
    }
    
    //----------------------------------------------------------------------------
    protected void createAWTObject(Element xml_node, double scale)
    {
        String value = xml_node.getAttribute("points");
        //        System.out.println("Create line...");
        
        if (value != null)
        {
            int counter;
            String[] positions;
            Point single_point;
            String[] points = value.split(" "); // split something like "10.0,20.0 14.0,21.0 32.0,1.0"
            
            //            System.out.println("#Points: " + points.length);
            
            points_.clear();
            
            for (counter = 0; counter < points.length; counter++)
            {
                positions = points[counter].split(","); // split something like "10.0,20.0"
                if (positions.length == 2)
                {
                    try
                    {
                        single_point = new Point((int) (Integer.parseInt(positions[0]) * scale), (int) (Integer.parseInt(positions[1]) * scale) );
                        points_.add(single_point);
                    }
                    catch (NumberFormatException e)
                    {
                        System.err.println("CREATE LINE - Wrong Number Format: " + e.getMessage());
                    }
                }
            }
        }
        
        setAWTStroke(xml_node, scale);
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyX( String x )
    {
        double diff = Double.parseDouble( x ) - ((Point)points_.get(0)).getX();
        Point point;
        
        for( int i = 0; i < points_.size(); i++ )
        {
            point = (Point)points_.get( i );
            points_.set(i, new Point( (int)(point.getX() + diff), (int)point.getY() ) );
        }
        
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyX()
    {
        return ((Point)points_.get( 0 )).getX();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyY( String y )
    {
        double diff = Double.parseDouble( y ) - ((Point)points_.get(0)).getY();
        Point point;
        
        for( int i = 0; i < points_.size(); i++ )
        {
            point = (Point)points_.get( i );
            points_.set(i, new Point( (int)point.getX(), (int)(point.getY() + diff) ) );
        }
        
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyY()
    {
        return ((Point)points_.get( 0 )).getY();
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
        Point point;

        for( int i = 0; i < points_.size(); i++ )
        {
            point = (Point)points_.get( i );
            if (point.distance(p) < 10.0)
            {
                return true;
            }
        }

        return false;
    }
    
    //----------------------------------------------------------------------------
    public void move(double x, double y)
    {
        Point point;
        for( int i = 0; i < points_.size(); i++ )
        {
            point = (Point)points_.get( i );
            points_.set(i, new Point( (int) (point.getX() + x), (int) (point.getY() + y) ) );
        }
    }
    
	public String getGradientAsSVG()
	{
		return null;
	}

}
