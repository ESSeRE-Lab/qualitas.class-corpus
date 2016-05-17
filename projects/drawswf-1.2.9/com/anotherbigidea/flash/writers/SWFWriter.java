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
import com.anotherbigidea.io.*;
import com.anotherbigidea.flash.*;
import com.anotherbigidea.flash.structs.Rect;
import com.anotherbigidea.flash.interfaces.*;

/**
 * Implements the SWFTags interface and writes a SWF file to the output stream
 */
public class SWFWriter implements SWFTags
{
    protected OutStream out;
    protected OutputStream outputstream;
    protected ByteArrayOutputStream byteout;
    protected String filename;    
    
    //--deferred header values
    protected int  frameCount;
    protected int  version;
    protected Rect frameSize;
    protected int  height;
    protected int  rate;
    
    public SWFWriter( String filename ) throws IOException 
    {
        this( new FileOutputStream( filename ) );
        this.filename = filename;
    }
    
    public SWFWriter( OutputStream outputstream )
    {
        this.outputstream = outputstream;
        out = new OutStream( outputstream );
    } 
    
    public SWFWriter( OutStream outstream )
    {
        out = outstream;
    }
    
    /**
     * Interface SWFTags
     */
    public void header( int version, long length,
                        int twipsWidth, int twipsHeight,
                        int frameRate, int frameCount ) throws IOException
    {
        frameSize = new Rect( 0, 0, twipsWidth, twipsHeight );        

        //--Unknown values
        if( length < 0 || frameCount < 0 )
        {
            //--defer the header
            this.version    = version;
            this.rate       = frameRate;
            this.frameCount = 0;
                
            if( filename != null ) //write the header later
            {
                length     = 0;
                frameCount = 0;
            }
            else //write to a byte array first
            {
                //--set up a byte array for the output
                if( byteout == null )
                {
                    byteout = new ByteArrayOutputStream( 20000 );
                    out = new OutStream( byteout );
                }
            
                return;
            }            
        }            

        writeHeader( version, length, frameRate, frameCount );        
    }
    
    /**
     * Interface SWFTags
     */
    public void tag( int tagType, boolean longTag, 
                     byte[] contents ) throws IOException
    {
        //System.out.println( "OUT Tag " + tagType + " " + longTag + " " + ( (contents==null) ? 0 : contents.length) );
        //System.out.println();
        
        int length = (contents != null ) ? contents.length : 0;
        longTag = ( length > 62 ) || longTag;
        
        int hdr = ( tagType << 6 ) + ( longTag ? 0x3f : length );

        out.writeUI16( hdr );
        
        if( longTag ) out.writeUI32( length );        
        
        if( contents != null ) out.write( contents );
        
        if( tagType == SWFConstants.TAG_SHOWFRAME ) frameCount++;        
        if( tagType == SWFConstants.TAG_END       ) finish();
    }
    
    protected void writeHeader( int version, long length,
                                int frameRate, int frameCount ) throws IOException 
    {        
        //--Write File Signature
        out.write( new byte[] { 0x46, 0x57, 0x53 } ); 
        
        out.writeUI8( version );
        out.writeUI32( length );        
        frameSize.write( out );
        out.writeUI16( frameRate << 8 );
        out.writeUI16( frameCount );    
    }
    
    /**
     * Finish writing
     */
    protected void finish() throws IOException
    {
        out.flush();

        //--Close the output file, calculate length and framecount and then
        // rewrite the header.
        if( filename != null )
        {
            outputstream.close();
            
            RandomAccessFile raf = new RandomAccessFile( filename, "rw" );
            int length = (int)raf.length();

            byteout = new ByteArrayOutputStream();
            out = new OutStream( byteout );
            
            writeHeader( version, length, rate, frameCount );
            out.flush();
            
            raf.write( byteout.toByteArray() );
            raf.close();
            
            return;
        }
        
        //--Writing to a byte array - need to recalculate lengths
        if( byteout != null )
        {
            byte[] bytes = byteout.toByteArray();

            long length = 12L + frameSize.getLength() + bytes.length;

            out = new OutStream( outputstream );
            
            writeHeader( version, length, rate, frameCount );
            
            out.write( bytes );
            out.flush();
        }
    }
}
