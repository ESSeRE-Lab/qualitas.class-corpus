/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.ext.awt.image.rendered;


import org.apache.batik.ext.awt.image.GraphicsUtil;

import java.awt.color.ColorSpace;

import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

/**
 * This function will tranform an image from any colorspace into a
 * luminance image.  The alpha channel if any will be copied to the
 * new image.
 *
 * @author <a href="mailto:Thomas.DeWeeese@Kodak.com">Thomas DeWeese</a>
 * @version $Id: Any2sRGBRed.java,v 1.1 2003/04/11 07:56:53 tom Exp $ */
public class Any2sRGBRed extends AbstractRed {

    boolean srcIsLsRGB = false;

    /**
     * Construct a luminace image from src.
     *
     * @param src The image to convert to a luminance image
     */
    public Any2sRGBRed(CachableRed src) {
        super(src,src.getBounds(), 
              fixColorModel(src),
              fixSampleModel(src),
              src.getTileGridXOffset(),
              src.getTileGridYOffset(),
              null);

        ColorModel srcCM = src.getColorModel();
        if (srcCM == null) return;
        ColorSpace srcCS = srcCM.getColorSpace();
        if (srcCS == ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB))
            srcIsLsRGB = true;
    }

    public static boolean is_INT_PACK_COMP(SampleModel sm) {
        if(!(sm instanceof SinglePixelPackedSampleModel)) return false;

        // Check transfer types
        if(sm.getDataType() != DataBuffer.TYPE_INT)       return false;

        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel)sm;

        int [] masks = sppsm.getBitMasks();
        if ((masks.length != 3) && (masks.length != 4)) return false;
        if(masks[0] != 0x00ff0000) return false;
        if(masks[1] != 0x0000ff00) return false;
        if(masks[2] != 0x000000ff) return false;
        if ((masks.length == 4) &&
            (masks[3] != 0xff000000)) return false;
 
        return true;
   }

    /**
     * Exponent for linear to sRGB convertion
     */
    private static final double GAMMA = 2.4;

    /**
     * Lookup tables for RGB lookups. The linearToSRGBLut is used
     * when noise values are considered to be on a linearScale. The
     * linearToLinear table is used when the values are considered to
     * be on the sRGB scale to begin with.
     */
    private static final int linearToSRGBLut[] = new int[256];
    static {
        final double scale = 1.0/255;
        final double exp   = 1.0/GAMMA;
        // System.out.print("L2S: ");
        for(int i=0; i<256; i++){
            double value = i*scale;
            if(value <= 0.0031308)
                value *= 12.92;
            else
                value = 1.055 * Math.pow(value, exp) - 0.055;
            
            linearToSRGBLut[i] = (int)Math.round(value*255.);
            // System.out.print(linearToSRGBLut[i] + ",");
        }
        // System.out.println("");
    }
     
    public static WritableRaster applyLut_INT(WritableRaster wr,
                                              final int []lut) {
        SinglePixelPackedSampleModel sm =
            (SinglePixelPackedSampleModel)wr.getSampleModel();
        DataBufferInt db = (DataBufferInt)wr.getDataBuffer();

        final int     srcBase 
            = (db.getOffset() + 
               sm.getOffset(wr.getMinX()-wr.getSampleModelTranslateX(), 
                            wr.getMinY()-wr.getSampleModelTranslateY()));
        // Access the pixel data array
        final int pixels[]   = db.getBankData()[0];
        final int width      = wr.getWidth();
        final int height     = wr.getHeight();
        final int scanStride = sm.getScanlineStride();

        int end, pix;

        // For alpha premult we need to multiply all comps.
        for (int y=0; y<height; y++) {
            int sp  = srcBase + y*scanStride;
            end = sp + width;

            while (sp<end) {
                pix = pixels[sp];
                pixels[sp] = 
                    ((     pix      &0xFF000000)|
                     (lut[(pix>>>16)&0xFF]<<16) |
                     (lut[(pix>>> 8)&0xFF]<< 8) |
                     (lut[(pix     )&0xFF]    ));
                sp++;
            }
        }

        return wr;
    }

    public WritableRaster copyData(WritableRaster wr) {

        // Get my source.
        CachableRed src   = (CachableRed)getSources().get(0);
        ColorModel  srcCM = src.getColorModel();
        SampleModel srcSM = src.getSampleModel();


        // Fast case, Linear SRGB source, INT Pack writable raster...
        if (srcIsLsRGB && 
            is_INT_PACK_COMP(wr.getSampleModel())) {
            src.copyData(wr);
            if (srcCM.hasAlpha())
                GraphicsUtil.coerceData(wr, srcCM, false);
            applyLut_INT(wr, linearToSRGBLut);
            return wr;
        }

        if (srcCM == null) {
            // We don't really know much about this source, let's
            // guess based on the number of bands...

            float [][] matrix = null;
            switch (srcSM.getNumBands()) {
            case 1:
                matrix = new float[1][3];
                matrix[0][0] = 1; // Red
                matrix[0][1] = 1; // Grn
                matrix[0][2] = 1; // Blu
                break;
            case 2:
                matrix = new float[2][4];
                matrix[0][0] = 1; // Red
                matrix[0][1] = 1; // Grn
                matrix[0][2] = 1; // Blu
                matrix[1][3] = 1; // Alpha
                break;
            case 3:
                matrix = new float[3][3];
                matrix[0][0] = 1; // Red
                matrix[1][1] = 1; // Grn
                matrix[2][2] = 1; // Blu
                break;
            default:
                matrix = new float[srcSM.getNumBands()][4];
                matrix[0][0] = 1; // Red
                matrix[1][1] = 1; // Grn
                matrix[2][2] = 1; // Blu
                matrix[3][3] = 1; // Alpha
                break;
            }
            Raster srcRas = src.getData(wr.getBounds());
            BandCombineOp op = new BandCombineOp(matrix, null);
            op.filter(srcRas, wr);
        } else {
            ColorModel dstCM = getColorModel();
            if (srcCM.getColorSpace() == dstCM.getColorSpace()) {
                // No transform needed, just reformat data...
                // System.out.println("Bypassing");

                if (is_INT_PACK_COMP(srcSM))
                    src.copyData(wr);
                else
                    GraphicsUtil.copyData(src.getData(wr.getBounds()), wr);

                return wr;
            }

            Raster srcRas = src.getData(wr.getBounds());
            WritableRaster srcWr  = (WritableRaster)srcRas;

            // Divide out alpha if we have it.  We need to do this since
            // the color convert may not be a linear operation which may 
            // lead to out of range values.
            ColorModel srcBICM = srcCM;
            if (srcCM.hasAlpha())
                srcBICM = GraphicsUtil.coerceData(srcWr, srcCM, false);

            BufferedImage srcBI, dstBI;
            srcBI = new BufferedImage(srcBICM, 
                                      srcWr.createWritableTranslatedChild(0,0),
                                      false, 
                                      null);

            // System.out.println("src: " + srcBI.getWidth() + "x" + 
            //                    srcBI.getHeight());

            ColorConvertOp op = new ColorConvertOp(dstCM.getColorSpace(), 
                                                   null);
            dstBI = op.filter(srcBI, null);

            // System.out.println("After filter:");
            
            WritableRaster wr00 = wr.createWritableTranslatedChild(0,0);
            for (int i=0; i<dstCM.getColorSpace().getNumComponents(); i++)
                copyBand(dstBI.getRaster(), i, wr00,    i);

            if (dstCM.hasAlpha())
                copyBand(srcWr, srcSM.getNumBands()-1,
                         wr,    getSampleModel().getNumBands()-1);
        }
        return wr;
    }

        /**
         * This function 'fixes' the source's color model.  Right now
         * it just selects if it should have one or two bands based on
         * if the source had an alpha channel.
         */
    protected static ColorModel fixColorModel(CachableRed src) {
        ColorModel  cm = src.getColorModel();
        if (cm != null) {
            if (cm.hasAlpha())
                return GraphicsUtil.sRGB_Unpre;

            return GraphicsUtil.sRGB;
        }
        else {
            // No ColorModel so try to make some intelligent
            // decisions based just on the number of bands...
            // 1 bands -> replicated into RGB
            // 2 bands -> Band 0 replicated into RGB & Band 1 -> alpha premult
            // 3 bands -> sRGB (not-linear?)
            // 4 bands -> sRGB premult (not-linear?)
            SampleModel sm = src.getSampleModel();

            switch (sm.getNumBands()) {
            case 1:
                return GraphicsUtil.sRGB;
            case 2:
                return GraphicsUtil.sRGB_Unpre;
            case 3:
                return GraphicsUtil.sRGB;
            }
            return GraphicsUtil.sRGB_Unpre;
        }
    }

    /**
     * This function 'fixes' the source's sample model.
     * Right now it just selects if it should have 3 or 4 bands
     * based on if the source had an alpha channel.
     */
    protected static SampleModel fixSampleModel(CachableRed src) {
        SampleModel sm = src.getSampleModel();
        ColorModel  cm = src.getColorModel();

        int width  = sm.getWidth();
        int height = sm.getHeight();

        boolean alpha = false;

        if (cm != null)
            alpha = cm.hasAlpha();
        else {
            switch (sm.getNumBands()) {
            case 1: case 3:
                alpha = false;
                break;
            default:
                alpha = true;
                break;
            }
        }
        if (alpha)
            return new SinglePixelPackedSampleModel
                (DataBuffer.TYPE_INT,
                 sm.getWidth(),
                 sm.getHeight(),
                 new int [] {0xFF0000, 0xFF00, 0xFF, 0xFF000000});
        else
            return new SinglePixelPackedSampleModel
                (DataBuffer.TYPE_INT,
                 sm.getWidth(),
                 sm.getHeight(),
                 new int [] {0xFF0000, 0xFF00, 0xFF});
    }
}
