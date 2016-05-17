/*
 * Created on May 27, 2003
 *
 */
package org.axiondb.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author mdelagrange
 *
 */
public class Utf8InputStreamConverter extends InputStream {
    
    // this class will require modification
    // if we need non-ascii conversions
    
    private String _targetEncoding = null;
    private InputStream _utf8Stream = null;

    /**
     * Currently only supports "US-ASCII"
     * 
     * @param targetEncoding "US-ASCII"
     * @throws UnsupportedEncodingException
     */
    public Utf8InputStreamConverter(InputStream utf8Stream, String targetEncoding) throws UnsupportedEncodingException {
        if (targetEncoding.equals("US-ASCII") == false) {
            throw new UnsupportedEncodingException(targetEncoding);
        }
        
        _targetEncoding = targetEncoding;
        _utf8Stream = utf8Stream;
    }

    /**
     * Returns a byte encoded as ASCII.  If non-ASCII characters are encountered in the
     * underlying UTF-8 stream, an IOException is thrown.
     * 
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        int theByte = _utf8Stream.read();
        
        if (theByte > 127) {
            throw new IOException("Could not convert stream from UTF-8 to " + _targetEncoding);
        }
        
        return theByte;
    }

}
