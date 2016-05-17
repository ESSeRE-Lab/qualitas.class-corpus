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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/FlashGenerator.java,v 1.7 2003/01/24 01:35:53 tom Exp $
 */

package at.bestsolution.drawswf;

import java.io.IOException;
import java.util.LinkedList;
import java.awt.Dimension;

import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Frame;

import at.bestsolution.drawswf.drawobjects.DrawObject;

/**
 *
 * @author  heli
 */
public class FlashGenerator
{
    Dimension size_;
    int speed_;
    
    //----------------------------------------------------------------------------
    /**
     * Creates a new instance of FlashGenerator
     */
    public FlashGenerator(Dimension size, int speed)
    {
        size_  = size;
        speed_ = speed;
    }
    
    //----------------------------------------------------------------------------
    public void generateFile( String filename, LinkedList draw_list )
    {
        DrawObject draw_object;
        Frame frame;
        Movie movie = new Movie();
        
        // movie.setBackcolor(new Color(255,255,255)); // is default
        movie.setWidth( size_.width );
        movie.setHeight( size_.height );
        
        for( int count = 0; count < draw_list.size(); count++ )
        {
            draw_object = (DrawObject) draw_list.get(count);
            if ( draw_object != null )
            {
                draw_object.drawObject(movie, count + 1, speed_);
            }
        }

        frame = movie.appendFrame();
        frame.stop();  
        
        try
        {
            movie.write(filename);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
