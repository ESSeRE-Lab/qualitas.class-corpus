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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/GenericFileFilter.java,v 1.4 2002/10/02 13:12:24 heli Exp $
 */

package at.bestsolution.drawswf;

import java.io.File;
//import javax.swing.*;
import javax.swing.filechooser.FileFilter;

/**
 * @author  heli
 */
public class GenericFileFilter extends FileFilter
{
    private String[] extensions_;
    private String description_;
    
    //----------------------------------------------------------------------------
    /**
     * Create a generic filefilter.
     *
     * @param description the discription for this filefilter.
     * @param extension to filter (has to be lower case).
     */
    public GenericFileFilter(String description, String extension)
    {
        description_ = description;
        extensions_  = new String[1];
        // we compare extensions case insensitive
        extensions_[0] = extension.toLowerCase();
    }
    
    //----------------------------------------------------------------------------
    /**
     * Create a generic filefilter.
     *
     * @param description the discription for this filefilter.
     * @param extensions to filter (has to be lower case).
     */
    public GenericFileFilter(String description, String[] extensions)
    {
        description_ = description;
        extensions_  = extensions;
        for (int i=0; i < extensions_.length; i++)
        {
            // we compare extensions case insensitive
            extensions_[i] = extensions_[i].toLowerCase();
        }
    }
    
    //----------------------------------------------------------------------------
    /**
     * Get the extension of a file. Looks for the last point in the filename and
     * returns everything after that transformed to lowercase.
     *
     * @param f file where we get the name from.
     * @return the extension of the file.
     */
    private String getExtension(File f)
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if (i > 0 &&  i < s.length() - 1)
        {
            ext = s.substring(i+1).toLowerCase();
        }
        
        return ext;
    }
    
    //----------------------------------------------------------------------------
    /**
     * Check if this filefilter accepts a given file.
     *
     * @param f the file to check.
     * @return true if file is accepted.
     */
    public boolean accept(File f)
    {
        if (f.isDirectory())
        {
            return true;
        }
        
        String extension = getExtension(f);
        if (extension != null)
        {
            for (int i = 0; i < extensions_.length; i++)
            {
                if (extension.equals(extensions_[i]))
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    //----------------------------------------------------------------------------
    /**
     * Get the description for this filefilter.
     *
     * @return the description.
     */
    public String getDescription()
    {
        return description_;
    }
}
