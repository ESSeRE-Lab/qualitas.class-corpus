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
import com.anotherbigidea.flash.*;
import com.anotherbigidea.flash.interfaces.*;

/**
 * A writer that implements the SWFActions interface and writes
 * actions to a text format
 */
public class ActionTextWriter implements SWFActions, SWFActionCodes 
{
    protected PrintWriter printer;
    protected String indent = "";

    public ActionTextWriter( PrintWriter printer )
    {
        this.printer = printer;
    }

    protected void print( String mnemonic, String[] args )
    {
        printer.print( indent + "    " );
        writePaddedString( mnemonic + " ", 15 );
        
        if( args != null )
        {
            for( int i = 0; i < args.length; i++ )
            {
                if( i > 0 ) printer.print( ", " );
                printer.print( args[i] );
            }
        }
        
        printer.println();
    }
    
    protected void writePaddedString( String s, int length )
    {
        int pad = length - s.length();
        
        printer.print( s );
        while( pad > 0 )
        {
            printer.print( " " );
            pad--;
        }
    }

    public void start( int conditions ) throws IOException
    {
        print( "conditions", new String[] { Integer.toBinaryString( conditions ) } );
        printer.flush();
    }
    
    public void end() throws IOException
    {
        print( "end", null );
        printer.println();
    }
 
    public void done() throws IOException
    {
        printer.flush();
    }
        
    public void blob( byte[] blob ) throws IOException
    {
        print( "(blob)", null );
        printer.println();
    }    
    
    public void unknown( int code, byte[] data ) throws IOException
    {
        print( "unknown code =", new String[] { Integer.toString( code ) } );
    }
    
    public void initArray() throws IOException 
    {
        print( "initArray", null );
    }    
        
    public void jumpLabel( String label ) throws IOException
    {
        printer.println( indent + label + ":" );
    }    
  
    public void gotoFrame( int frameNumber ) throws IOException
    {
        print( "gotoFrame", new String[] { Integer.toString( frameNumber ) } );
    }
    
    public void gotoFrame( String label ) throws IOException
    {
        print( "gotoFrame", new String[] { "\"" + label + "\"" } );
    }
    
    public void getURL( String url, String target ) throws IOException
    {
        print( "getURL", new String[] { "\"" + url + "\"", "\"" + target + "\"" } );
    }
    
    public void nextFrame() throws IOException
    {
        print( "nextFrame", null );
    }
    
    public void prevFrame() throws IOException
    {
        print( "previousFrame", null );
    }
    
    public void play() throws IOException
    {
        print( "play", null );
    }
    
    public void stop() throws IOException
    {
        print( "stop", null );
    }
    
    public void toggleQuality() throws IOException
    {
        print( "toggleQuality", null );
    }
    
    public void stopSounds() throws IOException
    {
        print( "stopSounds", null );
    }
    
    public void setTarget( String target ) throws IOException
    {
        print( "setTarget", new String[] { "\"" + target + "\"" } );
    }
    
    public void jump( String jumpLabel ) throws IOException
    {
        print( "jump", new String[] { "\"" + jumpLabel + "\"" } );
    }
    
    public void ifJump( String jumpLabel ) throws IOException
    {
        print( "ifJump", new String[] { "\"" + jumpLabel + "\"" } );
    }
    
    public void waitForFrame( int frameNumber, String jumpLabel ) throws IOException
    {
        print( "waitForFrame", new String[] { Integer.toString( frameNumber ), 
                                              "\"" + jumpLabel + "\"" } );        
    }
    
    public void waitForFrame( String jumpLabel ) throws IOException
    {
        print( "waitForFrame", new String[] { "\"" + jumpLabel + "\"" } );       
    }
    
    public void pop() throws IOException
    {
        print( "pop", null );
    }
    
    public void push( String value ) throws IOException
    {
        print( "push", new String[] { "\"" + value + "\"" } );
    }
    
    public void push( float  value ) throws IOException
    {
        print( "push", new String[] { "float " + value } );
    }
    
    public void push( double value ) throws IOException
    {
        print( "push", new String[] { "double " + value } );
    }
    
    public void pushNull() throws IOException
    {
        print( "push", new String[] { "null" } );
    }
    
    public void pushRegister( int registerNumber ) throws IOException
    {
        print( "push", new String[] { "register( " + registerNumber + " )" } );
    }
    
    public void push( boolean value ) throws IOException
    {
        print( "push", new String[] { value ? "true" : "false" } );
    }
    
    public void push( int value ) throws IOException
    {
        print( "push", new String[] { "" + value } );
    }
    
    public void lookup( int dictionaryIndex ) throws IOException
    {
        print( "push", new String[] { "lookup( " + dictionaryIndex + " )" } );
    }
    
    public void add() throws IOException
    {
        print( "add", null );
    }
    
    public void substract() throws IOException
    {
        print( "substract", null );
    }
    
    public void multiply() throws IOException
    {
        print( "multiply", null );
    }
    
    public void divide() throws IOException
    {
        print( "divide", null );
    }
    
    public void equals() throws IOException
    {
        print( "equals", null );
    }
    
    public void lessThan() throws IOException
    {
        print( "lessThan", null );
    }
    
    public void and() throws IOException
    {
        print( "and", null );
    }
    
    public void or() throws IOException
    {
        print( "or", null );
    }
    
    public void not() throws IOException
    {
        print( "not", null );
    }
    
    public void stringEquals() throws IOException
    {
        print( "stringEquals", null );
    }
    
    public void stringLength() throws IOException
    {
        print( "stringLength", null );
    }
    
    public void concat() throws IOException
    {
        print( "concat", null );
    }
    
    public void substring() throws IOException
    {
        print( "substring", null );
    }
    
    public void stringLessThan() throws IOException
    {
        print( "stringLessThan", null );
    }
    
    public void stringLengthMB() throws IOException
    {
        print( "stringLengthMB", null );
    }
    
    public void substringMB() throws IOException
    {
        print( "substringMB", null );
    }
        
    public void toInteger() throws IOException
    {
        print( "toInteger", null );
    }
        
    public void charToAscii() throws IOException
    {
        print( "charToAscii", null );
    }
        
    public void asciiToChar() throws IOException
    {
        print( "asciiToChar", null );
    }
        
    public void charMBToAscii() throws IOException
    {
        print( "charMBToAscii", null );
    }
        
    public void asciiToCharMB() throws IOException
    {
        print( "asciiToCharMB", null );
    }
        
    public void call() throws IOException
    {
        print( "call", null );
    }
    
    public void getVariable() throws IOException
    {
        print( "getVariable", null );
    }
    
    public void setVariable() throws IOException
    {
        print( "setVariable", null );
    }
    
    public void getURL( int sendVars, int loadMode ) throws IOException
    {        
        String sendVars_ = null;
        switch( sendVars )
        {
            case GET_URL_SEND_VARS_GET:
                sendVars_ = "send vars via GET";
                break;
            
            case GET_URL_SEND_VARS_POST:
                sendVars_ = "send vars via POST";
                break;
            
            case GET_URL_SEND_VARS_NONE:
            default:
                sendVars_ = "no send";
                break;
        }
        
        String mode = null;
        switch( loadMode )
        {
            case GET_URL_MODE_LOAD_MOVIE_INTO_LEVEL : 
                mode = "load movie into level";
                break;
                
            case GET_URL_MODE_LOAD_MOVIE_INTO_SPRITE: 
                mode = "load movie into sprite";
                break;
  
            case GET_URL_MODE_LOAD_VARS_INTO_LEVEL :
                mode = "load vars into level";
                break;
  
            case GET_URL_MODE_LOAD_VARS_INTO_SPRITE:
                mode = "load vars into sprite";
                break;
  
            default:
                mode = "???";
                break;
        }
        
        print( "getURL",  new String[] { sendVars_, mode } );
    }
    
    public void gotoFrame( boolean play ) throws IOException
    {
        print( "gotoFrame", new String[] { play ? "and play" : "and stop" } );
    }
    
    public void setTarget() throws IOException
    {
        print( "setTarget", null );
    }
    
    public void getProperty() throws IOException
    {
        print( "getProperty", null );
    }
    
    public void setProperty() throws IOException
    {
        print( "setProperty", null );
    }
    
    public void cloneSprite() throws IOException
    {
        print( "cloneSprite", null );
    }
    
    public void removeSprite() throws IOException
    {
        print( "removeSprite", null );
    }
    
    public void startDrag() throws IOException
    {
        print( "startDrag", null );
    }
    
    public void endDrag() throws IOException
    {
        print( "endDrag", null );
    }
    
    public void trace() throws IOException
    {
        print( "trace", null );
    }
    
    public void getTime() throws IOException
    {
        print( "getTime", null );
    }
    
    public void randomNumber() throws IOException
    {
        print( "randomNumber", null );
    }    
    
    public void lookupTable( String[] values ) throws IOException
    {
        print( "lookupTable", null );
        
        for( int i = 0; i < values.length; i++ )
        {
            printer.print( indent + "        " );
            writePaddedString( Integer.toString( i ) + ":", 5  );
            printer.println( "\"" + values[i] + "\"" );
        }
    }
    
    public void callFunction() throws IOException
    {
        print( "callFunction", null );
    }    
    
    public void callMethod() throws IOException
    {
        print( "callMethod", null );
    }        
    
    public void startFunction( String name, String[] paramNames ) throws IOException
    {
        String args = name + "(";
        
        if( paramNames != null)
        {
            for( int i = 0; i < paramNames.length; i++ )
            {
                if( i > 0 ) args += ",";
                args += " " + paramNames[i];
            }

            if( paramNames.length > 0 ) args += " ";
        }
        
        args += ")";
        
        printer.println();
        print( "defineFunction", new String[] { args } );
        print( "{", null );

        indent += "    ";
    }    
    
    public void endBlock() throws IOException
    {
        if( indent.length() <= 4 ) indent = "";
        else if( indent.length() >= 4 ) indent = indent.substring( 4 );
        
        print( "}", null );
        printer.println();
    }
 
    public void comment( String comment ) throws IOException
    {
        printer.println( indent + "    // " + comment );
    }
     
    public void defineLocalValue() throws IOException
    {
        print( "defineLocalValue", null );
    }    
    
    public void defineLocal() throws IOException
    {
        print( "defineLocal", null );
    }      
    
    public void deleteProperty() throws IOException
    {
        print( "deleteProperty", null );
    }    
    
    public void deleteThreadVars() throws IOException
    {
        print( "deleteThreadVars", null );
    }    
    
    public void enumerate() throws IOException
    {
        print( "enumerate", null );
    }    
    
    public void typedEquals() throws IOException
    {
        print( "typedEquals", null );
    }    
    
    public void getMember() throws IOException
    {
        print( "getMember", null );
    }    
    
    public void initObject() throws IOException
    {
        print( "initObject", null );
    }    
    
    public void newMethod() throws IOException
    {
        print( "newMethod", null );
    }    
    
    public void newObject() throws IOException
    {
        print( "newObject", null );
    }    
    
    public void setMember() throws IOException
    {
        print( "setMember", null );
    }    
    
    public void getTargetPath() throws IOException
    {
        print( "getTargetPath", null );
    }   
    
    public void startWith() throws IOException
    {       
        printer.println();
        print( "with", null );
        print( "{", null );

        indent += "    ";
    }   
         
    public void duplicate() throws IOException
    {
        print( "duplicate", null );
    }   
    
    public void returnValue() throws IOException
    {
        print( "return", null );
    }   
    
    public void swap() throws IOException
    {
        print( "swap", null );
    }   
    
    public void storeInRegister( int registerNumber ) throws IOException
    {
        print( "register", new String[] { Integer.toString( registerNumber ) } );
    }   
    
    public void convertToNumber() throws IOException
    {
        print( "convertToNumber", null );
    }   
    
    public void convertToString() throws IOException
    {
        print( "convertToString", null );
    }   
    
    public void typeOf() throws IOException
    {
        print( "typeOf", null );
    }   
    
    public void typedAdd() throws IOException
    {
        print( "typedAdd", null );
    }   
    
    public void typedLessThan() throws IOException
    {
        print( "typedLessThan", null );
    }   
    
    public void modulo() throws IOException
    {
        print( "modulo", null );
    }   
    
    public void bitAnd() throws IOException
    {
        print( "bitAnd", null );
    }   
    
    public void bitOr() throws IOException
    {
        print( "bitOr", null );
    }   
    
    public void bitXor() throws IOException
    {
        print( "bitXor", null );
    }   
    
    public void shiftLeft() throws IOException
    {
        print( "shiftLeft", null );
    }   
    
    public void shiftRight() throws IOException
    {
        print( "shiftRight", null );
    }   
    
    public void shiftRightUnsigned() throws IOException
    {
        print( "shiftRightUnsigned", null );
    }   
    
    public void decrement() throws IOException
    {
        print( "decrement", null );
    }   
    
    public void increment() throws IOException
    {
        print( "increment", null );
    }   
}

