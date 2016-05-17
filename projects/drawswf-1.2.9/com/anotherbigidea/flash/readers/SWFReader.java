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
package com.anotherbigidea.flash.readers;

import java.io.*;
import com.anotherbigidea.io.*;
import com.anotherbigidea.flash.structs.Rect;
import com.anotherbigidea.flash.*;
import com.anotherbigidea.flash.interfaces.*;
import com.anotherbigidea.flash.writers.SWFWriter;

/**
 * Reads a SWF input stream and drives the SWFConsumer interface.
 */
public class SWFReader
{
    protected SWFTags     consumer;
    protected InStream    in;
    protected InputStream inputstream;
    
    public SWFReader( SWFTags consumer, InputStream inputstream )
    {
        this.consumer    = consumer;
        this.inputstream = inputstream;
        this.in          = new InStream( inputstream );
    }

    public SWFReader( SWFTags consumer, InStream instream )
    {
        this.consumer = consumer;
        this.in       = instream;
    }    
    
    /**
     * Drive the consumer by reading a SWF File - including the header and all tags
     */
    public void readFile() throws IOException
    {
        readHeader();
        readTags();
    }
    
    /**
     * Drive the consumer by reading SWF tags only
     */
    public void readTags() throws IOException 
    {
        while( readOneTag() != SWFConstants.TAG_END );
    }
    
    /**
     * Drive the consumer by reading one tag
     * @return the tag type
     */
    public int readOneTag() throws IOException 
    {
        int header = in.readUI16();
        
        int  type   = header >> 6;    //only want the top 10 bits
        int  length = header & 0x3F;  //only want the bottom 6 bits
        boolean longTag = (length == 0x3F);
        
        if( longTag )
        {
            length = (int)in.readUI32();
        }
        
        byte[] contents = in.read( length );
        
        consumer.tag( type, longTag, contents );
        
        return type;
    }
    
    /** 
     * Only read the SWF file header
     */
    public void readHeader() throws IOException
    {
        //--Verify File Signature
        if( ( in.readUI8() != 0x46 ) ||  // "F"
            ( in.readUI8() != 0x57 ) ||  // "W"
            ( in.readUI8() != 0x53 ) )   // "S"
        {
            throw new IOException( "Invalid SWF File Signature" );
        }

        int  version   = in.readUI8();
        long length    = in.readUI32();
        Rect frameSize = new Rect( in );
        int frameRate  = in.readUI16() >> 8;
        int frameCount = in.readUI16();                
        
        consumer.header( version, length, 
                         frameSize.getMaxX(), frameSize.getMaxY(), 
                         frameRate, frameCount );                         
    }
    
    public static void main( String[] args ) throws IOException
    {
        SWFWriter writer = new SWFWriter( System.out );
        SWFReader reader = new SWFReader( writer, System.in );
        reader.readFile();
        System.out.flush();
    }
}
