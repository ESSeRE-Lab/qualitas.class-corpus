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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/drawobjects/DrawObjectFactory.java,v 1.20 2003/06/09 11:24:56 tom Exp $
 */

package at.bestsolution.drawswf.drawobjects;

import java.awt.Color;
import java.awt.BasicStroke;
import java.util.LinkedList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import org.apache.xpath.XPathAPI;
import org.xml.sax.SAXException;

import at.bestsolution.drawswf.DrawingPanel;

/**
 *
 * @author  tom
 */
public class DrawObjectFactory
{
    public static final int LINE          = 0;
    public static final int STRAIGHT_LINE = 1;
    public static final int RECTANGLE     = 2;
    public static final int ELLIPSE       = 3;
    public static final int TEXT          = 4;
    public static final int PICTURE       = 5;
    public static final int MAX_OBJECTS   = 6;
    
    
    protected static final String SVG_LINE          = "polyline";
    protected static final String SVG_STRAIGHT_LINE = "line";
    protected static final String SVG_RECTANGLE     = "rect";
    protected static final String SVG_ELLIPSE       = "ellipse";
    protected static final String SVG_TEXT          = "text";
    protected static final String SVG_PICTURE       = "image";

    private   static final String FORMAT_DEFINITION = "Made with DrawSWF 1.2 (http://drawswf.sf.net)";
    
    private static boolean scale_;
    
    //----------------------------------------------------------------------------
    public static DrawObject createObject( int drawing_mode, Color pen_color, Color fill_color, BasicStroke stroke )
    {
        DrawObject result = null;
        
        switch( drawing_mode )
        {
            case LINE:
                result = new Line();
                break;
            case RECTANGLE:
                result = new Rectangle();
                break;
            case ELLIPSE:
                result = new Ellipse();
                break;
            case STRAIGHT_LINE:
                result = new StraightLine();
                break;
            case TEXT:
                result = new Text();
                break;
            case PICTURE:
                result = new Picture();
                break;
            default:
                result = null;
                break;
        }
        
        result.setColor( pen_color );
        result.setFillColor( fill_color );
        result.setStroke( stroke );
        
        return result;
    }
    
    //----------------------------------------------------------------------------
    public static DrawObject createObject( Element xml_node )
    {
        double scale = 1.0;
        DrawObject result = null;
        String tag_name = xml_node.getTagName();
        
        if( tag_name.equals(SVG_LINE) )
        {
            result = new Line();
        }
        else if( tag_name.equals(SVG_RECTANGLE) )
        {
            result = new Rectangle();
        }
        else if( tag_name.equals(SVG_ELLIPSE) )
        {
            result = new Ellipse();
        }
        else if( tag_name.equals(SVG_STRAIGHT_LINE) )
        {
            result = new StraightLine();
        }
        else if( tag_name.equals(SVG_TEXT) )
        {
            result = new Text();
        }
        else if( tag_name.equals(SVG_PICTURE) )
        {
            result = new Picture();
        }
        
        if( result != null )
        {
            if (scale_ == true)
            {
                scale = 0.05;
            }
            result.createAWTObject( xml_node, scale );
        }

        return result;
    }

    //----------------------------------------------------------------------------
    public static LinkedList loadFile( File file, DrawingPanel panel )
    {
        LinkedList list     = null;
        NodeList svg_shapes = null;
        Element root;
        Element g_node;
        Element title;
        Document document;
        DrawObject draw_object;
        
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(file.toString());
            root = document.getDocumentElement();

            System.out.println("Document loaded... ");
            
            title = (Element) document.getElementsByTagName( "title" ).item(0);
            //System.out.println("title  = " + title);

            if (document.getElementsByTagName( "g" ).getLength() == 0)
            {
                svg_shapes = XPathAPI.selectNodeList( root, "*" );
                scale_ = false;
            }
            else
            {
                svg_shapes = XPathAPI.selectNodeList( document.getElementsByTagName( "g" ).item(0), "*" );
                scale_ = false;
            }

            System.out.println("shapes = " + svg_shapes);

            if ( (title == null) || ! FORMAT_DEFINITION.equals(title.getFirstChild().getNodeValue()) )
            {
                JOptionPane.showMessageDialog( panel, "Wrong or old file format", "Error", JOptionPane.ERROR_MESSAGE );
            }
            else if ( (svg_shapes != null) && (title.getFirstChild() != null) )
            {
                System.out.println("Found " + svg_shapes.getLength() + " shapes");
                list = new LinkedList();
                
                list.add( root.getAttributeNS(null, "width") );
                list.add( root.getAttributeNS(null, "height") );
                
                for (int i = 0; i < svg_shapes.getLength(); i++)
                {
                    draw_object = DrawObjectFactory.createObject( (Element) svg_shapes.item(i) );
                    
                    if (draw_object != null)
                    {
                        list.add( draw_object );
                    }
                }
            }
        }
        catch (FactoryConfigurationError e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            JOptionPane.showMessageDialog( panel, "Wrong or old file format", "Error", JOptionPane.ERROR_MESSAGE );
            e.printStackTrace();
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog( panel, "Could not open file", "Error", JOptionPane.ERROR_MESSAGE );
            e.printStackTrace();
        }
        catch( TransformerException e )
        {
            JOptionPane.showMessageDialog( panel, "Wrong or old file format", "Error", JOptionPane.ERROR_MESSAGE );
            e.printStackTrace();
        }
        
        return list;
    }

    //----------------------------------------------------------------------------
    public static boolean saveFile( LinkedList draw_list, File target_file, DrawingPanel drawing_panel )
    {
        boolean result = false;
        DrawObject draw_object;
        
        try
        {
        	StringBuffer svg_elements = new StringBuffer();
            StringBuffer svg_defs = new StringBuffer();
            // PrintWriter out2 = new PrintWriter( new BufferedOutputStream( new FileOutputStream( target_file ) ) );
            
			FileOutputStream out = new FileOutputStream( target_file );
            
            out.write( makeBytesOutOfString( "<?xml version=\"1.0\" encoding= \"UTF-8\"?>", true ) );
            out.write( makeBytesOutOfString("<svg width=\"" + drawing_panel.getCanvasWidth() + "\" height=\"" + drawing_panel.getCanvasHeight(), false ) );
            out.write( makeBytesOutOfString("\" xmlns:drawswf=\"http://drawswf.sf.net/2002/drawswf\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">", true) );
            out.write( makeBytesOutOfString("  <title>" + FORMAT_DEFINITION + "</title>", true ) );
			out.write( makeBytesOutOfString("<g>", true) );

            for( long i = 0; i < draw_list.size(); i++ )
            {
                draw_object = (DrawObject) draw_list.get((int) i);
                
                if( draw_object.hasGradient() )
                {
					svg_defs.append( draw_object.getGradientAsSVG() + System.getProperty("line.separator") );
                }
                
				svg_elements.append( "    " + draw_object.toSVG( i*250 ) + System.getProperty("line.separator") );
            }
			
			out.write( makeBytesOutOfString("<defs>", true ) );
			out.write( makeBytesOutOfString( svg_defs.toString(), false ) );
			out.write( makeBytesOutOfString("</defs>",true) );
			
			try
			{
				out.write( svg_elements.toString().getBytes("UTF8") );
			}
			catch( UnsupportedEncodingException e )
			{
				System.err.println( e.getStackTrace() );
			}
			
            out.write( makeBytesOutOfString("</g>", true) );
			out.write( makeBytesOutOfString("</svg>", true) );
            out.close();
            
            result = true;
        }
		catch ( IOException e )
		{
			System.err.println( e.getMessage() );
		}
        
        return result;
    }
    
    public static byte[] makeBytesOutOfString( String line, boolean end_line )
    {
    	byte[] rv = new byte[0];
    	String line_end = "";
    	
    	if( end_line )
    	{
			line_end = System.getProperty("line.separator");
    	}
    	
    	try
    	{
			rv = (line + line_end ).getBytes("UTF8");
    	}
		catch( UnsupportedEncodingException e )
		{
			System.err.println( e.getStackTrace() );
		}
		
		return rv;
    }
}
