/****************************************************************
 * Copyright (c) 2001, Joachim Sauer, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. The name of the author may not be used to endorse or
 * promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ****************************************************************/

package com.anotherbigidea.flash.movie;

import com.anotherbigidea.flash.SWFConstants;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * A Font Definition that can referenced by Font symbols. It reads the
 * required font- and glyph-information from the provided java.awt.Font
 * object.
 *
 * This class can't currently handle complex Layout, but I don't know
 * if SWF can handle it at all. It is not possible to use multiple Glyphs
 * to create a Character and it is not possible to use one Glyph for multiple
 * Characters.
 *
 * @author Joachim Sauer <saua@gmx.net>
 */
public class SystemFontDefinition extends FontDefinition
{
    private static FontRenderContext frc;
    private static Graphics2D g2d;
    
    private static final boolean DEBUG_GETSHAPE = false;
    private static final boolean DEBUG_KERNING = true;
    
    private static final float HUGE_FONT_SIZE = 512f;
    
    static
    {
        BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        g2d = (Graphics2D) dummyImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        frc = g2d.getFontRenderContext();
        
        if (DEBUG_GETSHAPE) System.out.println("DEUB_GETSHAPE activated");
        if (DEBUG_KERNING) System.out.println("DEBUG_KERNING activated");
    }
    
    private FontMetrics metrics;
    private java.awt.Font font;
    private AffineTransform trans;
    private double scale;
    
    public SystemFontDefinition(java.awt.Font sfont)
    {
        super();
        //create a huge Version of this Font, to get accurate Measurement even
        //in ints
        font = sfont.deriveFont(HUGE_FONT_SIZE);
        metrics = g2d.getFontMetrics(font);
        
        double maxHeight = metrics.getAscent();
        
        /* the SWF-EM-Square is always 1024x1024 and
         * 1.0f Units is 1 Pixel which in turn is 20 TWIPS
         * therefore we scale to 1024 / SWFConstants.TWIPS
         */
        //TODO: correct scaling
        scale = (1024.0 / SWFConstants.TWIPS) / maxHeight;
        
        trans = AffineTransform.getScaleInstance(scale, scale);
        
        setName(font.getName());
        setFontFlags(true, false, false, font.isItalic(), font.isBold(), true);
        setAscent(metrics.getAscent() * scale);
        setDescent(metrics.getDescent() * scale);
        setLeading(metrics.getLeading() * scale);
    }
    
    /**
     * Creates a glyphs by code.
     * @param code the Unicode index of the requested Glyph.
     * @return null if the code has no glyph
     */
    private Glyph createGlyph(int code)
    {
        char[] chars = { (char) code };
        
        if (!font.canDisplay((char) code))
            return null;
        
        GlyphVector gv = font.createGlyphVector(frc, chars);
        Shape glyph_shape = new Shape();
        glyph_shape.drawAWTPathIterator(gv.getGlyphOutline(0).getPathIterator(trans));
        GlyphMetrics gm = gv.getGlyphMetrics(0);
        
        //		FontDefinition.Glyph g = new FontDefinition.Glyph(s, metrics.charWidth(chars[0]) * scale, code);
        FontDefinition.Glyph g = new FontDefinition.Glyph(glyph_shape, gm.getAdvanceX() * scale, code);
        
        return g;
    }
    
    /**
     * Look up a glyph by code.
     * @param code the Unicode index of the requested Glyph.
     * @return null if the code has no glyph
     */
    public Glyph getGlyph(int code)
    {
        Glyph ret;
        
        ret = super.getGlyph(code);
        
        if (ret != null)
            return ret;
        
        ret = createGlyph(code);
        
        if (ret == null)
        {
            glyphs.add(ret);
            glyphLookup.put(new Integer(code), ret);
        }
        
        return ret;
    }
    
    /**
     * Get the kerning adjustment required between the two given codes
     */
    public double getKerningOffset(int code1, int code2)
    {
        double ret;
        
        Integer i1 = new Integer(code1);
        Integer i2 = new Integer(code2);
        
        if (kernLookup == null)
        {
            kernLookup = new HashMap();
            
            if (kerning.size() != 0)
                throw new IllegalStateException("Internal Error in SystemFontDefinition: no kernLookup, but kerning contains entries!");
        }
        else
        {
            HashMap kerns = (HashMap) kernLookup.get(i1);
            if (kerns != null)
            {
                KerningPair pair = (KerningPair) kerns.get(i2);
                if (pair != null)
                {
                    return pair.getAdjustment();
                }
            }
        }
        
        ret = calculateKerningOffset(code1, code2);
        
        HashMap kerns = (HashMap) kernLookup.get(i1);
        if (kerns == null)
        {
            kerns = new HashMap();
            kernLookup.put(i1, kerns);
        }
        
        kerns.put(i2, new KerningPair(code1, code2, ret));
        
        return ret;
    }
    
    /**
     * Calculate the Kerning Offset of the specified characters. This is a hack,
     * because the Java Font API doesn't expose Kerning information directly.
     * @return adjustment needed between this two codes.
     */
    private double calculateKerningOffset(int code1, int code2)
    {
        char[] chars =
        { (char) code1, (char) code2 };
        GlyphVector gv = font.createGlyphVector(frc, chars);
        
        //        double adj = (gv.getGlyphPosition(1).getX() - metrics.charWidth(chars[0])) * scale;
        double adj = (gv.getGlyphPosition(1).getX() - gv.getGlyphMetrics(0).getAdvanceX()) * scale;
        
        if (DEBUG_KERNING) System.out.println("Kerning for '" + (char) code1 + (char) code2 + "': " + adj);
        
        return adj;
    }
}