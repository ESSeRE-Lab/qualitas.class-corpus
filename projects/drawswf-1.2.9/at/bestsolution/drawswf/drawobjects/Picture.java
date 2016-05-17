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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/drawobjects/Picture.java,v 1.3 2003/06/07 20:39:47 tom Exp $
 */

/*
 * Line.java
 *
 * Created on 21. Mai 2002, 22:57
 */

package at.bestsolution.drawswf.drawobjects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.MediaTracker;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.anotherbigidea.flash.SWFConstants;

import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Shape;
import com.anotherbigidea.flash.movie.Instance;
import com.anotherbigidea.flash.movie.Transform;
import com.anotherbigidea.flash.movie.ImageUtil;


import org.w3c.dom.Element;

import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.drawswf.PictureDialog;
import at.bestsolution.drawswf.MainWindow;

/**
 *
 * @author  tom
 */
public class Picture extends DrawObject
{
    private Point2D position_;
    private Rectangle2D bounds_;
    private Point2D dummy_;
    private BufferedImage picture_;
    private PictureDialog picture_dialog_ = null;
    private TableModel model_;
    private JTable options_table_;
    private double scale_;
    private String pic_path_;
    
    public static final int NO_EFFECT      = 0;
    public static final int EFFECT_TYPE    = 1;
    public static final int EFFECT_FADE_IN = 2;
    private static int instance_counter = 1;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of Line */
    public Picture()
    {
        super( "Picture " + instance_counter++ );
        position_ = null;
        picture_  = null;
        bounds_   = null;
        scale_    = 1.0;
    }
    
    //----------------------------------------------------------------------------
    public void drawObject(Movie movie, int layer, int speed)
    {
        Frame frame = movie.appendFrame();
        
        com.anotherbigidea.flash.movie.Image.Lossless img = ImageUtil.createLosslessImage( picture_, SWFConstants.BITMAP_FORMAT_32_BIT, true );
        Shape shape = ImageUtil.shapeForImage( img, (double)picture_.getWidth(), (double)picture_.getHeight() );
        
        Transform transe = new Transform();
        transe.setScaleX(scale_);
        transe.setScaleY(scale_);
        transe.setTranslateX( position_.getX() );
        transe.setTranslateY( position_.getY() );
        
        Instance instance = frame.placeSymbol( shape, transe, null );
        
        // sleep in flash...
        for (int count = 0; count < 5; count++ )
        {
            movie.appendFrame();
        }
    }
    
    //----------------------------------------------------------------------------
    public void drawObject(Graphics2D g, DrawingPanel panel)
    {
        g.drawImage( picture_, (int) position_.getX(), (int) position_.getY(), (int)(picture_.getWidth() * scale_), (int)(picture_.getHeight() * scale_), panel);
        
        if (panel.isReplay() == true)
        {
            pause(100*ANIMATION_DELAY);
        }
    }
    
    //----------------------------------------------------------------------------
    public void mouseDragged(int x, int y, Graphics2D g)
    {
        g.setXORMode(Color.white);
        
        int xx = (int) dummy_.getX();
        int yy = (int) dummy_.getY();
        
        g.drawLine( xx, yy, xx + 50, yy );
        g.drawLine( xx, yy, xx, yy + 50 );
        
        dummy_ = new Point2D.Double(x, y);
        g.setColor(Color.BLACK);
        
        g.drawLine( x, y, x + 50, y );
        g.drawLine( x, y, x, y + 50 );
    }
    
    //----------------------------------------------------------------------------
    private int showPictureDialog()
    {
        if (picture_dialog_ == null)
        {
            picture_dialog_ = new PictureDialog();
        }
        
        return picture_dialog_.showOpenDialog( MainWindow.MAIN_WINDOW );
    }
    
    //----------------------------------------------------------------------------
    public void mousePressed(int x, int y, Graphics2D g)
    {
        dummy_ = new Point2D.Double(x, y);
        
        g.setColor(Color.BLACK);
        g.drawLine( x, y, x + 50, y );
        g.drawLine( x, y, x, y + 50 );
    }
    
    //----------------------------------------------------------------------------
    public boolean mouseReleased(int x, int y, Graphics2D g)
    {
        boolean result = false;
        
        int action_type = showPictureDialog();
        
        g.setXORMode(Color.white);
        
        int xx = (int) dummy_.getX();
        int yy = (int) dummy_.getY();
        
        g.drawLine( xx, yy, xx + 50, yy );
        g.drawLine( xx, yy, xx, yy + 50 );
        
        g.setPaintMode();
        
        if( action_type == PictureDialog.APPROVE_OPTION )
        {
            pic_path_ = picture_dialog_.getSelectedFile().getPath();
            result = loadImage();
            
            if (result == true)
            {
                position_ = new Point2D.Double(x, y);
                g.drawImage( picture_, (int) position_.getX(), (int) position_.getY(), null );
            }
        }
        
        return result;
    }
    
    //----------------------------------------------------------------------------
    protected String toSVG(long time)
    {
        return "<" + DrawObjectFactory.SVG_PICTURE
        + " x=\"" + position_.getX()
        + "\" y=\"" + position_.getY()
        + "\" width=\"" + picture_.getWidth()*scale_
        + "\" height=\"" + picture_.getHeight()*scale_
        + "\" xlink:href=\"" + pic_path_
        + "\" />";
    }
    
    //----------------------------------------------------------------------------
    protected void createAWTObject(Element xml_node, double scale)
    {
        double x = Double.parseDouble( xml_node.getAttribute("x") );
        double y = Double.parseDouble( xml_node.getAttribute("y") );

        position_ = new Point2D.Double( x * scale, y * scale );

        pic_path_ = xml_node.getAttributeNS("http://www.w3.org/1999/xlink", "href");

        boolean result = loadImage();

        if (result == false)
        {
            picture_ = new BufferedImage( 10, 10, BufferedImage.TYPE_INT_ARGB );
        }
        else
        {
            scale_ = Double.parseDouble( xml_node.getAttribute("width") ) / picture_.getWidth();
        }
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyX( String x )
    {
        position_ = new Point( (int)Double.parseDouble(x), (int)position_.getY() );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyX()
    {
        return position_.getX();
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyY( String y )
    {
        position_ = new Point( (int)position_.getX(), (int)Double.parseDouble(y) );
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyScale()
    {
        return scale_;
    }
    
    //----------------------------------------------------------------------------
    public void setPropertyScale( String scale )
    {
        scale_ = Double.parseDouble( scale );
        
        repaint();
    }
    
    //----------------------------------------------------------------------------
    public double getPropertyY()
    {
        return position_.getY();
    }
    
    //----------------------------------------------------------------------------
    public boolean contains(Point2D p)
    {
        boolean result;
        
        Rectangle2D rect = new Rectangle2D.Double(position_.getX(),position_.getY(),picture_.getWidth()*scale_, picture_.getHeight()*scale_ );
        
        result = rect.intersects( p.getX() - 1.0, p.getY() - 1.0, 3.0, 3.0 );
        
        return result;
    }
    
    //----------------------------------------------------------------------------
    public void move(double x, double y)
    {
        position_.setLocation( position_.getX() + x, position_.getY() + y );
    }
    
    private boolean loadImage()
    {
        boolean result = false;
        
        Image image = Toolkit.getDefaultToolkit().getImage( pic_path_ );
        MediaTracker tracker = new MediaTracker( MainWindow.MAIN_WINDOW );
        tracker.addImage(image, 1);
        
        try
        {
            // wait til image is loaded.
            tracker.waitForID(1);
            
            picture_ = new BufferedImage( image.getWidth(null), image.getWidth(null), BufferedImage.TYPE_INT_ARGB );
            Graphics2D picture_g = picture_.createGraphics();
            
            picture_g.drawImage( image, 0, 0, null );
            
            result = true;
        }
        catch (InterruptedException e)
        {
            System.err.println("File '" + pic_path_ + "' not found!");
            result = false;
        }
        
        return result;
    }
    
	public String getGradientAsSVG()
	{
		return null;
	}

}
