/*
 *  Copyright (c) 2003
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
 */

/*
 * Created on 23.03.2003
 *
 */
package at.bestsolution.ext.awt;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TreeMap;

/**
 * @author tom
 */
public class FontLoader
{
    private static FontLoader instance_ = null;
    private TreeMap fonts_;

    private FontLoader()
    {
        fonts_ = new TreeMap();
        loadStandardFonts();
    }

    private void loadAdditionalFonts(String path_to_additional_fonts)
    {
        if (path_to_additional_fonts != null && !path_to_additional_fonts.equals(""))
        {
            String[] paths = path_to_additional_fonts.split(File.pathSeparator);

            FileFilter filter = new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return (pathname.getName().endsWith(".ttf")) ? true : false;
                }
            };

            File font_dir;
            File[] ttf_font_files;
            FileInputStream file_stream;
            Font font;

            for (int j = 0; j < paths.length; j++)
            {
                font_dir = new File(paths[j]);
                ttf_font_files = font_dir.listFiles(filter);

                try
                {

                    for (int i = 0; i < ttf_font_files.length; i++)
                    {
                        file_stream = new FileInputStream(ttf_font_files[i]);
                        font = Font.createFont(Font.TRUETYPE_FONT, file_stream);
                        fonts_.put(font.getFontName(),font);
                        file_stream.close();
                    }

                }
                catch (FontFormatException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }

    private void loadStandardFonts()
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = ge.getAllFonts();

        for (int i = 0; i < fonts.length; i++)
        {
            fonts_.put(fonts[i].getFontName(), fonts[i]);
        }
    }

    public void addAdditionalPath( String path_to_additional_fonts )
    {
        loadAdditionalFonts(path_to_additional_fonts);
    }

    public String[] getFontNames()
    {
        String[] a = new String[0];
        
        return (String[])fonts_.keySet().toArray(a);
    }

    public Font getFont( String name, int style, int size )
    {
        Font font = null;
        
        if( fonts_.containsKey(name) )
        {
            font = ((Font)fonts_.get(name)).deriveFont(style,size); 
        }
        else
        {
            font = new Font( GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()[0],style,size );
        }
        
        return font;
    }

    public static FontLoader getInstance()
    {
        if (instance_ == null)
        {
            instance_ = new FontLoader();
        }

        return instance_;
    }
}
