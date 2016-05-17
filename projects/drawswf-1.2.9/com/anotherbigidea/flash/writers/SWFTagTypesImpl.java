/****************************************************************
 * Copyright (c) 2001, David N. Main, All rights reserved.
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
package com.anotherbigidea.flash.writers;

import java.io.*;
import java.util.*;
import com.anotherbigidea.flash.structs.*;
import com.anotherbigidea.flash.interfaces.*;

/**
 * A pass-through implementation of the SWFTagTypes interface - useful as a base class
 */
public class SWFTagTypesImpl implements SWFTagTypes 
{
    protected SWFTagTypes tags;
    
    /**
     * @param tags may be null
     */
    public SWFTagTypesImpl( SWFTagTypes tags )
    {
        this.tags = tags;
    }
    
    /**
     * SWFTags interface
     */    
    public void tag( int tagType, boolean longTag, byte[] contents ) 
        throws IOException
    {
        if( tags != null ) tags.tag( tagType, longTag, contents );
    }

    /**
     * SWFHeader interface.
     * Sets movie length to -1 to force a recalculation since the length
     * cannot be guaranteed to be the same as the original.
     */
    public void header( int version, long length,
                        int twipsWidth, int twipsHeight,
                        int frameRate, int frameCount ) throws IOException
    {
        if( tags != null ) tags.header( version, length, twipsWidth, twipsHeight,
                                        frameRate, frameCount );
    }
    
    /**
     * SWFTagTypes interface
     */
    public void tagEnd() throws IOException
    {
        if( tags != null ) tags.tagEnd();
    }

    /**
     * SWFTagTypes interface
     */
    public void tagDefineSound( int id, int format, int frequency,
                                boolean bits16, boolean stereo,
                                int sampleCount, byte[] soundData ) 
        throws IOException    {
        if( tags != null ) tags.tagDefineSound( id, format, frequency,
                                bits16, stereo, sampleCount, soundData );    }        /**
     * SWFTagTypes interface
     */
    public void tagDefineButtonSound( int buttonId,                    int rollOverSoundId, SoundInfo rollOverSoundInfo,                    int rollOutSoundId,  SoundInfo rollOutSoundInfo,                    int pressSoundId,    SoundInfo pressSoundInfo,                    int releaseSoundId,  SoundInfo releaseSoundInfo )        throws IOException    {
        if( tags != null ) tags.tagDefineButtonSound( buttonId,                    rollOverSoundId, rollOverSoundInfo,                    rollOutSoundId,  rollOutSoundInfo,                    pressSoundId,    pressSoundInfo,                    releaseSoundId,  releaseSoundInfo );    }    
    
    /**
     * SWFTagTypes interface
     */
    public void tagStartSound( int soundId, SoundInfo info ) throws IOException    {
        if( tags != null ) tags.tagStartSound( soundId, info );    }
    
    /**
     * SWFTagTypes interface
     */
    public void tagSoundStreamHead(         int playbackFrequency, boolean playback16bit, boolean playbackStereo,        int streamFormat, int streamFrequency, boolean stream16bit, boolean streamStereo,        int averageSampleCount ) throws IOException    {
        if( tags != null ) tags.tagSoundStreamHead(             playbackFrequency, playback16bit, playbackStereo,            streamFormat, streamFrequency, stream16bit, streamStereo,            averageSampleCount );    }
    
    /**
     * SWFTagTypes interface
     */
    public void tagSoundStreamHead2(         int playbackFrequency, boolean playback16bit, boolean playbackStereo,        int streamFormat, int streamFrequency, boolean stream16bit, boolean streamStereo,        int averageSampleCount ) throws IOException    {
        if( tags != null ) tags.tagSoundStreamHead2(             playbackFrequency, playback16bit, playbackStereo,            streamFormat, streamFrequency, stream16bit, streamStereo,            averageSampleCount );    }
        /**
     * SWFTagTypes interface
     */
    public void tagSoundStreamBlock( byte[] soundData ) throws IOException    {
        if( tags != null ) tags.tagSoundStreamBlock( soundData );    }        /**
     * SWFTagTypes interface
     */
    public void tagSerialNumber( String serialNumber ) throws IOException    {
        if( tags != null ) tags.tagSerialNumber( serialNumber );    }        /**
     * SWFTagTypes interface
     */
    public void tagGenerator( byte[] data ) throws IOException    {
        if( tags != null ) tags.tagGenerator( data );    }        /**
     * SWFTagTypes interface
     */
    public void tagGeneratorText( byte[] data ) throws IOException    {
        if( tags != null ) tags.tagGeneratorText( data );    }

    /**
     * SWFTagTypes interface
     */    public void tagGeneratorFont( byte[] data ) throws IOException    {
        if( tags != null ) tags.tagGeneratorFont( data );    }         
    /**
     * SWFTagTypes interface
     */
    public void tagGeneratorCommand( byte[] data ) throws IOException    {
        if( tags != null ) tags.tagGeneratorCommand( data );    }    

    /**
     * SWFTagTypes interface
     */
    public void tagNameCharacter( byte[] data ) throws IOException    {
        if( tags != null ) tags.tagNameCharacter( data );    }                /**
     * SWFTagTypes interface
     */
    public void tagDefineBits( int id, byte[] imageData ) throws IOException    {
        if( tags != null ) tags.tagDefineBits( id, imageData );    }                /**
     * SWFTagTypes interface
     */
    public void tagJPEGTables( byte[] jpegEncodingData ) throws IOException    {
        if( tags != null ) tags.tagJPEGTables( jpegEncodingData );    }        
    /**
     * SWFTagTypes interface
     */
    public void tagDefineBitsJPEG3( int id, byte[] imageData, byte[] alphaData )         throws IOException    {
        if( tags != null ) tags.tagDefineBitsJPEG3( id, imageData, alphaData );    }        
        
    /**
     * SWFTagTypes interface
     */
    public void tagShowFrame() throws IOException
    {
        if( tags != null ) tags.tagShowFrame();    }    
    /**
     * SWFTagTypes interface
     */
    public SWFActions tagDoAction() throws IOException    {        if( tags != null ) return tags.tagDoAction();
        return null;    }
        /**
     * SWFTagTypes interface
     */
    public SWFShape tagDefineShape( int id, Rect outline ) throws IOException    {        if( tags != null ) return tags.tagDefineShape( id, outline );
        return null;    }
        /**
     * SWFTagTypes interface
     */
    public SWFShape tagDefineShape2( int id, Rect outline ) throws IOException    {
        if( tags != null ) return tags.tagDefineShape2( id, outline );        return null;    }
        /**
     * SWFTagTypes interface
     */
    public SWFShape tagDefineShape3( int id, Rect outline ) throws IOException    {        if( tags != null ) return tags.tagDefineShape3( id, outline );
        return null;    }    
    
    /**
     * SWFTagTypes interface
     */    
    public void tagFreeCharacter( int charId ) throws IOException 
    {
        if( tags != null ) tags.tagFreeCharacter( charId );     
    }
    
    /**
     * SWFTagTypes interface
     */    
    public void tagPlaceObject( int charId, int depth,                                 Matrix matrix, AlphaTransform cxform )         throws IOException
    {        if( tags != null ) tags.tagPlaceObject( charId, depth, matrix, cxform );
    }    
    /**
     * SWFTagTypes interface
     */    
    public SWFActions tagPlaceObject2( boolean isMove,
                                       int clipDepth,
                                       int depth,
                                       int charId,
                                       Matrix matrix,
                                       AlphaTransform cxform,
                                       int ratio,
                                       String name,
                                       int clipActionFlags )  
        throws IOException    
    {
        if( tags != null ) return tags.tagPlaceObject2( isMove, clipDepth, depth,
                                                        charId, matrix, cxform, ratio,
                                                        name, clipActionFlags );
        return null;
    }
        
    /**
     * SWFTagTypes interface
     */     public void tagRemoveObject( int charId, int depth ) throws IOException
    {
        if( tags != null ) tags.tagRemoveObject( charId, depth );    }
        
    /**
     * SWFTagTypes interface
     */     public void tagRemoveObject2(int depth ) throws IOException
    {
        if( tags != null ) tags.tagRemoveObject2( depth );    }

    /**
     * SWFTagTypes interface
     */     public void tagSetBackgroundColor( Color color ) throws IOException
    {
        if( tags != null ) tags.tagSetBackgroundColor( color );
    }    

    /**
     * SWFTagTypes interface
     */     public void tagFrameLabel( String label ) throws IOException    {
        if( tags != null ) tags.tagFrameLabel( label );    }
    
    /**
     * SWFTagTypes interface
     */     public SWFTagTypes tagDefineSprite( int id ) throws IOException
    {
        if( tags != null ) return tags.tagDefineSprite( id );
        return null;    }
    
    /**
     * SWFTagTypes interface
     */     public void tagProtect( byte[] password ) throws IOException
    {
        if( tags != null ) tags.tagProtect( password );    }    
    /**
     * SWFTagTypes interface
     */     public void tagEnableDebug( byte[] password ) throws IOException
    {
        if( tags != null ) tags.tagEnableDebug( password );    }        
    /**
     * SWFTagTypes interface
     */     public SWFVectors tagDefineFont( int id, int numGlyphs ) throws IOException
    {
        if( tags != null ) return tags.tagDefineFont( id, numGlyphs );        return null;    }
    /**
     * SWFTagTypes interface
     */     public void tagDefineFontInfo( int fontId, String fontName, int flags, int[] codes )
        throws IOException
    {
        if( tags != null ) tags.tagDefineFontInfo( fontId, fontName, flags, codes );    }    
    /**
     * SWFTagTypes interface
     */     public SWFVectors tagDefineFont2( int id, int flags, String name, int numGlyphs,
                                      int ascent, int descent, int leading,
                                      int[] codes, int[] advances, Rect[] bounds,
                                      int[] kernCodes1, int[] kernCodes2,
                                      int[] kernAdjustments ) throws IOException
    {
        if( tags != null ) return tags.tagDefineFont2( id, flags, name, numGlyphs,
                                      ascent, descent, leading, codes, advances,
                                      bounds, kernCodes1, kernCodes2, kernAdjustments );
        return null;    }
    
    /**
     * SWFTagTypes interface
     */     public void tagDefineTextField( int fieldId, String fieldName,
                    String initialText, Rect boundary, int flags,
                    AlphaColor textColor, int alignment, int fontId, int fontSize, 
                    int charLimit, int leftMargin, int rightMargin, int indentation,
                    int lineSpacing ) 
        throws IOException
    {
        if( tags != null ) tags.tagDefineTextField( fieldId, fieldName, initialText,
                               boundary, flags, textColor, alignment, fontId,
                               fontSize, charLimit, leftMargin, rightMargin,
                               indentation, lineSpacing );
    }

    /**
     * SWFTagTypes interface
     */     public SWFText tagDefineText( int id, Rect bounds, Matrix matrix )
        throws IOException
    {         if( tags != null ) return tags.tagDefineText( id, bounds, matrix );        
        return null;    }    /**
     * SWFTagTypes interface
     */     public SWFText tagDefineText2( int id, Rect bounds, Matrix matrix ) throws IOException
    {         if( tags != null ) return tags.tagDefineText2( id, bounds, matrix );
        return null;    }    
    /**
     * SWFTagTypes interface
     */     public SWFActions tagDefineButton( int id, Vector buttonRecords )
        throws IOException
    {
        if( tags != null ) return tags.tagDefineButton( id, buttonRecords );
        return null;    }
    
    /**
     * SWFTagTypes interface
     */     public void tagButtonCXForm( int buttonId, ColorTransform transform ) 
        throws IOException
    {
        if( tags != null ) tags.tagButtonCXForm( buttonId, transform );    }        
    /**
     * SWFTagTypes interface
     */     public SWFActions tagDefineButton2( int id,                                         boolean trackAsMenu,                                         Vector buttonRecord2s )
        throws IOException
    {
        if( tags != null ) return tags.tagDefineButton2( id, trackAsMenu, 
                                                         buttonRecord2s );        return null;    }
    /**
     * SWFTagTypes interface
     */     public void tagExport( String[] names, int[] ids ) throws IOException
    {
        if( tags != null ) tags.tagExport( names, ids );
    }
    
    /**
     * SWFTagTypes interface
     */     public void tagImport( String movieName, String[] names, int[] ids ) 
        throws IOException
    {
        if( tags != null ) tags.tagImport( movieName, names, ids );
    }
    
    /**
     * SWFTagTypes interface
     */     public void tagDefineQuickTimeMovie( int id, String filename ) throws IOException
    {        if( tags != null ) tags.tagDefineQuickTimeMovie( id, filename );
    }    
    /**
     * SWFTagTypes interface
     */     public void tagDefineBitsJPEG2( int id, byte[] data ) throws IOException
    {        if( tags != null ) tags.tagDefineBitsJPEG2( id, data );
    }        /**
     * SWFTagTypes interface
     */     public void tagDefineBitsJPEG2( int id, InputStream jpegImage ) throws IOException
    {
        if( tags != null ) tags.tagDefineBitsJPEG2( id, jpegImage );    }
        /**
     * SWFTagTypes interface
     */     public SWFShape tagDefineMorphShape( int id, Rect startBounds, Rect endBounds )         throws IOException
    {
        if( tags != null ) return tags.tagDefineMorphShape( id, startBounds, endBounds );
        return null;    }    
    
    /**
     * SWFTagTypes interface
     */     public void tagDefineBitsLossless( int id, int format, int width, int height,                                       Color[] colors, byte[] imageData )        throws IOException
    {        if( tags != null ) tags.tagDefineBitsLossless( id , format, width, height,
                                                       colors, imageData );
    }
        /**
     * SWFTagTypes interface
     */ 
    public void tagDefineBitsLossless2( int id, int format, int width, int height,                                        Color[] colors, byte[] imageData )        throws IOException
    {
        if( tags != null ) tags.tagDefineBitsLossless2( id , format, width, height,
                                                        colors, imageData );
    }
}