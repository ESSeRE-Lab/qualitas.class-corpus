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
import java.util.*;
import com.anotherbigidea.flash.interfaces.*;
import com.anotherbigidea.flash.*;
import com.anotherbigidea.io.*;

/**
 * Parse action bytes and drive a SWFActions interface
 */
public class ActionParser implements SWFActionCodes 
{
    protected SWFActions actions;
    protected int blockDepth = 0;
    
    public ActionParser( SWFActions actions )
    {
        this.actions = actions;
    }
    
    public synchronized void parse( byte[] bytes ) throws IOException
    {
        Vector records = createRecords( bytes );
        processRecords( records );
    }
    
    public synchronized void parse( InStream in ) throws IOException
    {
        Vector records = createRecords( in );
        processRecords( records );
    }
        
    protected void processRecords( Vector records ) throws IOException
    {
        //--process action records
        for( Enumeration enum = records.elements(); enum.hasMoreElements(); )
        {
            ActionRecord rec = (ActionRecord)enum.nextElement();
            
            //actions.comment( "depth=" + rec.blockDepth );
            
            //detect end of block
            if( rec.blockDepth < blockDepth )
            {
                blockDepth--;
                actions.endBlock();
            }
            
            if( rec.label != null ) actions.jumpLabel( rec.label );
            
            int code = rec.code;
            byte[] data = rec.data;
            
            InStream in = (data!=null && data.length > 0) ? new InStream(data) : null;

            switch( code )
            {
                case 0: actions.end(); break;
                
                //--Flash 3
                case GOTO_FRAME    : actions.gotoFrame( in.readUI16() ); break;
                case GET_URL       : actions.getURL( in.readString(), in.readString() ); break;
                case NEXT_FRAME    : actions.nextFrame(); break;
                case PREVIOUS_FRAME: actions.prevFrame(); break;
                case PLAY          : actions.play(); break;
                case STOP          : actions.stop(); break;
                case TOGGLE_QUALITY: actions.toggleQuality(); break;
                case STOP_SOUNDS   : actions.stopSounds(); break;
                case WAIT_FOR_FRAME: actions.waitForFrame( in.readUI16(), rec.jumpLabel ); break;
                case SET_TARGET    : actions.setTarget( in.readString() ); break;
                case GOTO_LABEL    : actions.gotoFrame( in.readString() ); break;
                
                //--Flash 4
                case IF              : actions.ifJump( rec.jumpLabel ); break;
                case JUMP            : actions.jump( rec.jumpLabel ); break;
                case WAIT_FOR_FRAME_2: actions.waitForFrame( rec.jumpLabel ); break;
                case POP             : actions.pop(); break;
                case PUSH            : parsePush( data.length, in ); break;
                case ADD             : actions.add(); break;
                case SUBTRACT        : actions.substract(); break;
                case MULTIPLY        : actions.multiply(); break;
                case DIVIDE          : actions.divide(); break;
                    
                case EQUALS          : actions.equals(); break;
                case LESS            : actions.lessThan(); break;
                case AND             : actions.and(); break;
                case OR              : actions.or(); break;
                case NOT             : actions.not(); break;
                                       
                case STRING_EQUALS     : actions.stringEquals(); break;
                case STRING_LENGTH     : actions.stringLength(); break;
                case STRING_ADD        : actions.concat(); break;
                case STRING_EXTRACT    : actions.substring(); break;
                case STRING_LESS       : actions.stringLessThan(); break;                                                        
                case MB_STRING_EXTRACT : actions.substringMB(); break;
                case MB_STRING_LENGTH  : actions.stringLengthMB(); break;

                case TO_INTEGER       : actions.toInteger(); break;
                case CHAR_TO_ASCII    : actions.charToAscii(); break;
                case ASCII_TO_CHAR    : actions.asciiToChar(); break;
                case MB_CHAR_TO_ASCII : actions.charMBToAscii(); break;
                case MB_ASCII_TO_CHAR : actions.asciiToCharMB(); break;
    
                case CALL             : actions.call(); break;
                case GET_VARIABLE     : actions.getVariable(); break;
                case SET_VARIABLE     : actions.setVariable(); break;
                 
                case GET_URL_2        : parseGetURL2( in.readUI8() ); break;
                                        
                case GOTO_FRAME_2  : actions.gotoFrame( in.readUI8() != 0 ); break;
                case SET_TARGET_2  : actions.setTarget(); break;
                case GET_PROPERTY  : actions.getProperty(); break;
                case SET_PROPERTY  : actions.setProperty(); break;
                case CLONE_SPRITE  : actions.cloneSprite(); break;
                case REMOVE_SPRITE : actions.removeSprite(); break;
                case START_DRAG    : actions.startDrag(); break;
                case END_DRAG      : actions.endDrag(); break;
                case TRACE         : actions.trace(); break;
                case GET_TIME      : actions.getTime(); break;
                case RANDOM_NUMBER : actions.randomNumber(); break;

                //--Flash 5                         
                case INIT_ARRAY         : actions.initArray(); break;
                case LOOKUP_TABLE       : parseLookupTable( in ); break;
                case CALL_FUNCTION      : actions.callFunction(); break;
                case CALL_METHOD        : actions.callMethod(); break;
                case DEFINE_FUNCTION    : parseDefineFunction(in); break;
                case DEFINE_LOCAL_VAL   : actions.defineLocalValue(); break;
                case DEFINE_LOCAL       : actions.defineLocal(); break;
                case DEL_VAR            : actions.deleteProperty(); break;
                case DEL_THREAD_VARS    : actions.deleteThreadVars(); break;
                case ENUMERATE          : actions.enumerate(); break;
                case TYPED_EQUALS       : actions.typedEquals(); break;
                case GET_MEMBER         : actions.getMember(); break;
                case INIT_OBJECT        : actions.initObject(); break;
                case CALL_NEW_METHOD    : actions.newMethod(); break;
                case NEW_OBJECT         : actions.newObject(); break;
                case SET_MEMBER         : actions.setMember(); break;
                case GET_TARGET_PATH    : actions.getTargetPath(); break;
                case WITH               : parseWith( in ); break;
                case DUPLICATE          : actions.duplicate(); break;
                case RETURN             : actions.returnValue(); break;
                case SWAP               : actions.swap(); break;
                case REGISTER           : actions.storeInRegister( in.readUI8() ); break;
                case MODULO             : actions.modulo(); break;
                case TYPEOF             : actions.typeOf(); break;
                case TYPED_ADD          : actions.typedAdd(); break;
                case TYPED_LESS_THAN    : actions.typedLessThan(); break;
                case CONVERT_TO_NUMBER  : actions.convertToNumber(); break;
                case CONVERT_TO_STRING  : actions.convertToString(); break;
                case INCREMENT          : actions.increment(); break;
                case DECREMENT          : actions.decrement(); break;
                case BIT_AND            : actions.bitAnd(); break;
                case BIT_OR             : actions.bitOr(); break;
                case BIT_XOR            : actions.bitXor(); break;
                case SHIFT_LEFT         : actions.shiftLeft(); break;
                case SHIFT_RIGHT        : actions.shiftRight(); break;
                case SHIFT_UNSIGNED     : actions.shiftRightUnsigned(); break;

                default: actions.unknown( code, data ); break;
            }            
        }
    }

    protected void parseDefineFunction( InStream in ) throws IOException 
    {
        String name = in.readString();
        int paramCount = in.readUI16();
        
        String[] params = new String[ paramCount ];
        for( int i = 0; i < params.length; i++ )
        {
            params[i] = in.readString();
        }
        
        int codesize = in.readUI16();

        //System.out.println( "codesize=" + codesize ); System.out.flush();

        actions.startFunction( name, params );
        blockDepth++;
    }

    protected void parseWith( InStream in ) throws IOException 
    {
        int codesize = in.readUI16();

        actions.startWith( );
        blockDepth++;
    }    
    
    protected void parseLookupTable( InStream in ) throws IOException 
    {
        String[] strings = new String[ in.readUI16() ];
        
        for( int i = 0; i < strings.length; i++ )
        {
            strings[i] = in.readString();
        }
        
        actions.lookupTable( strings );
    }
    
    protected void parseGetURL2( int flags ) throws IOException
    {
        int sendVars = flags & 0x03;
        int mode = 0;
        
        switch( flags & 0xF0 )
        {
            case 0x40: mode = SWFActions.GET_URL_MODE_LOAD_MOVIE_INTO_SPRITE; break;
            case 0x80: mode = SWFActions.GET_URL_MODE_LOAD_VARS_INTO_LEVEL;   break;
            case 0xC0: mode = SWFActions.GET_URL_MODE_LOAD_VARS_INTO_SPRITE;  break;
            default:   mode = SWFActions.GET_URL_MODE_LOAD_MOVIE_INTO_LEVEL;  break;
        }
        
        actions.getURL( sendVars, mode );
    }
    
    protected void parsePush( int length, InStream in ) throws IOException 
    {
        while( in.getBytesRead() < length )
        {
            int pushType = in.readUI8();
            
            switch( pushType )
            {
                case PUSHTYPE_STRING  : actions.push( in.readString() ); break;
                case PUSHTYPE_FLOAT   : actions.push( in.readFloat() ); break;
                case PUSHTYPE_NULL    : actions.pushNull(); break;
                case PUSHTYPE_03      : break;
                case PUSHTYPE_REGISTER: actions.pushRegister( in.readUI8() ); break;
                case PUSHTYPE_BOOLEAN : actions.push( (in.readUI8() != 0) ? true : false ); break;
                case PUSHTYPE_DOUBLE  : actions.push( in.readDouble() ); break;
                case PUSHTYPE_INTEGER : actions.push( in.readSI32() ); break;
                case PUSHTYPE_LOOKUP  : actions.lookup( in.readUI8() ); break;
                default:
            }
        }
    }
    
    protected static class ActionRecord
    {
        public int offset;   //byte offset from start of the action array
        public int code;
        public String label;
        public String jumpLabel;
        public byte[] data;
        public int blockDepth = 0;
        
        protected ActionRecord( int offset, int code, byte[] data )
        {
            this.offset = offset;
            this.code   = code;
            this.data   = data;
        }
    }

    /**
     * First Pass to determine action offsets and jumps
     */
    protected Vector createRecords( byte[] bytes ) throws IOException 
    {            
        return createRecords( new InStream( bytes ));
    }
        
    /**
     * First Pass to determine action offsets and jumps
     */
    protected Vector createRecords( InStream in ) throws IOException 
    {        
        Vector records  = new Vector();
        Vector jumpers  = new Vector();
        Vector skippers = new Vector();
        Hashtable offsetTable = new Hashtable();
   
        Stack blockSizes = new Stack();
        
        int labelIndex = 0;
        
        while( true )
        {   
            int offset = (int)in.getBytesRead();
            
            //System.out.println( "read=" + offset ); System.out.flush();
            
            int code = in.readUI8();                
            int dataLength = (code >= 0x80) ? in.readUI16() : 0;   
            byte[] data = ( dataLength > 0 ) ? in.read( dataLength ) : null;

            //System.out.println( "size=" + dataLength ); System.out.flush();          
            
            ActionRecord rec = new ActionRecord( offset, code, data );
            records.addElement( rec );   
            offsetTable.put( new Integer(offset), rec );

            if( ! blockSizes.isEmpty() )
            {      
                int depth = blockSizes.size();
                rec.blockDepth = depth;
                int blockDecrement = ( dataLength > 0 ) ? ( dataLength + 3 ) : 1;

                //--subtract the size of this action from all the block sizes
                //  in the block stack
                for( int i = depth-1; i >= 0; i-- )
                {                
                    int[] blockSize = (int[])blockSizes.elementAt(i);
                    int size = blockSize[0];
                
                    size -= blockDecrement;
                    
                    //--reached end of block ?
                    if( size <= 0 ) blockSizes.pop();
                    else blockSize[0] = size;
                }
            }            
            
            if( code == 0 ) break; //end of actions
            
            else if( code == DEFINE_FUNCTION )
            {
                InStream in2 = new InStream( rec.data );
                in2.readString();
                int params = in2.readUI16();
                for( int i = 0; i < params; i++ ) in2.readString();        
                int blockSize = in2.readUI16();
                blockSizes.push( new int[]{ blockSize } );
            }
            else if( code == WITH )
            {
                InStream in2 = new InStream( rec.data );
                int blockSize = in2.readUI16();
                blockSizes.push( new int[]{ blockSize } );                
            }            
            else if( code == WAIT_FOR_FRAME || code == WAIT_FOR_FRAME_2 ) 
            {
                skippers.addElement( new Integer(records.size()-1));
            }
            else if( code == IF || code == JUMP ) jumpers.addElement( rec );
        }        
        
        //--Tie up the jumpers with the offsets
        for( Enumeration enum = jumpers.elements(); enum.hasMoreElements(); )
        {
            ActionRecord rec = (ActionRecord)enum.nextElement();
            
            InStream in2 = new InStream( rec.data );
            int jumpOffset = in2.readSI16();
            int offset = rec.offset + 5;
            int absoluteOffset = offset + jumpOffset;
            
            ActionRecord target = 
                (ActionRecord)offsetTable.get( new Integer(absoluteOffset) );
            
            if( target != null )
            {
                if( target.label == null ) target.label = rec.jumpLabel = "label" + (labelIndex++);
                else rec.jumpLabel = target.label;
            }
        }
        
        //--Tie up the skippers with labels
        for( Enumeration enum = skippers.elements(); enum.hasMoreElements(); )
        {
            int idx = ((Integer)enum.nextElement()).intValue();
            
            ActionRecord rec = (ActionRecord)records.elementAt(idx);
            
            InStream in2 = new InStream( rec.data );
            
            if( rec.code == WAIT_FOR_FRAME ) in2.readUI16();  //skip frame number
            int skip = in2.readUI8();
            int skipIndex = idx + skip + 1;
            
            if( skipIndex < records.size() )
            {
                ActionRecord target = (ActionRecord)records.elementAt(skipIndex);
                
                if( target.label == null ) target.label = rec.jumpLabel = "label" + (labelIndex++);
                else rec.jumpLabel = target.label;                
            }
        }
        
        return records;
    }
    

}
