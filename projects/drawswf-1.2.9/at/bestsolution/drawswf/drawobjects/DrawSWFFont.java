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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/drawobjects/DrawSWFFont.java,v 1.6 2003/04/11 12:23:13 tom Exp $
 */

package at.bestsolution.drawswf.drawobjects;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;

/**
 *
 * @author  tom
 */
public class DrawSWFFont
{
    private  Font font_;
    private  int effect_;
    private String text_;
    private Color color_;
    public static final FontRenderContext CONTEXT = new FontRenderContext(null, true, true);
    
    public static final int NO_EFFECT      = 0;
    public static final int EFFECT_TYPE    = 1;
    public static final int EFFECT_FADE_IN = 2;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of DrawSWFFont */
    public DrawSWFFont( Font font, int effect, String text, Color color )
    {
        effect_ = effect;
        font_   = font;
        text_   = text;
        color_ = color;
    }
    
    public Color getColor()
    {
        return color_;
    }
    
    public void setColor( Color color )
    {
        color_ = color;
    }
    
    //----------------------------------------------------------------------------
    public Font getAWTFont()
    {
        return font_;
    }
    
    //----------------------------------------------------------------------------
    public int getEffect()
    {
        return effect_;
    }
    
    //----------------------------------------------------------------------------
    public String getText()
    {
        return text_;
    }
    
    public void setText( String text )
    {
        text_ = text;
    }
    
    //----------------------------------------------------------------------------
    public String toString()
    {
        return font_.getFamily();
    }
}
