/*
 * $Id: TimestampType.java,v 1.13 2003/07/08 23:03:55 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002 Axion Development Team.  All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above 
 *    copyright notice, this list of conditions and the following 
 *    disclaimer. 
 *   
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution. 
 *   
 * 3. The names "Tigris", "Axion", nor the names of its contributors may 
 *    not be used to endorse or promote products derived from this 
 *    software without specific prior written permission. 
 *  
 * 4. Products derived from this software may not be called "Axion", nor 
 *    may "Tigris" or "Axion" appear in their names without specific prior
 *    written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =======================================================================
 */

package org.axiondb.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.axiondb.DataType;

/**
 * A {@link DataType} representing a timestamp value.
 *
 * @version $Revision: 1.13 $ $Date: 2003/07/08 23:03:55 $
 * @author Chuck Burdick
 */
public class TimestampType extends BaseDataType {
    
    /**
     * irrespective of the JVM's Locale lets pick a Locale for use
     * on any JVM
     */
    public static final Locale LOCALE = Locale.UK;
    public static final TimeZone TIMEZONE = TimeZone.getTimeZone("GMT");
    
    protected static final DateFormat[] _fmts = new DateFormat[] {
        new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", LOCALE),
        new SimpleDateFormat("yyyy-MM-dd", LOCALE),
        new SimpleDateFormat("MM-dd-yyyy", LOCALE),
        DateFormat.getTimeInstance(DateFormat.SHORT, LOCALE) };
    
    static {
        for ( int i = 0; i < _fmts.length; i++ ) {
            _fmts[i].setTimeZone(TIMEZONE);
        }
    }
    
    public TimestampType() {
    }

    public int getJdbcType() {
        return java.sql.Types.TIMESTAMP;
    }

    public String getPreferredValueClassName() {
        return "java.lang.Long";
    }

    public String toString() {
        return "timestamp";
    }

    /**
     * Returns <code>true</code> iff <i>value</i> is <code>null</code>,
     * a <tt>Number</tt>, or a <tt>String</tt> 
     * that can be converted to a <tt>Long</tt>.
     */
    public boolean accepts(Object value) {
        if(null == value) {
            return true;
        } else if(value instanceof Long) {
            return true;
        } else if(value instanceof java.util.Date) {
            return true;
        } else if(value instanceof java.sql.Date) {
            return true;
        } else if(value instanceof java.sql.Time) {
            return true;
        } else if(value instanceof Timestamp) {
            return true;
        } else if(value instanceof String) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns an <tt>Byte</tt> converted from the given <i>value</i>,
     * or throws {@link IllegalArgumentException} if the given <i>value</i>
     * isn't {@link #accepts acceptable}.
     */
    public Object convert(Object value) throws IllegalArgumentException {
        if(null == value) {
            return null;
        } else if(value instanceof Long) {
            return new Timestamp(((Long)value).longValue());
        } else if(value instanceof Timestamp) {
            return value;
        } else if(value instanceof java.sql.Date) {
            return new Timestamp(((java.sql.Date)value).getTime());
        } else if(value instanceof java.sql.Time) {
            return new Timestamp(((java.sql.Time)value).getTime());
        } else if(value instanceof java.util.Date) {
            return new Timestamp(((java.util.Date)value).getTime());
        } else if(value instanceof String) {
            java.util.Date dVal = null;
            int i = 0;
            while (dVal == null && i < _fmts.length) {
                dVal = _fmts[i].parse((String)value, new ParsePosition(0));
                i++;
            }
            if (dVal == null) {
                throw new IllegalArgumentException("Can't parse " + (String)value + " into a Timestamp.");
            } else {
                return new Timestamp(dVal.getTime());
            }
        } else {
            throw new IllegalArgumentException("Can't convert " + value.getClass().getName() + " " + value + ".");
        }
    }

    public java.sql.Date toDate(Object value) throws SQLException {
        try {
            Timestamp tval = (Timestamp)convert(value);
            return new java.sql.Date(tval.getTime());
        } catch(IllegalArgumentException e) {
            throw new SQLException("Can't convert \"" + value + "\" to Date.");
        }
    }

    public java.sql.Timestamp toTimestamp(Object value) throws SQLException {
        try {
            Timestamp tval = (Timestamp)convert(value);
            return tval;
        } catch(IllegalArgumentException e) {
            throw new SQLException("Can't convert \"" + value + "\" to Timestamp.");
        }
    }

    public boolean supportsSuccessor() {
        return true;
    }

    public Object successor(Object value) throws IllegalArgumentException {
        Timestamp v = (Timestamp)value;
        Timestamp result = new Timestamp(v.getTime());
        if(v.getNanos() == 999999999) {
            result.setTime(result.getTime() + 1);
            result.setNanos(0);
        } else {
            result.setNanos(v.getNanos() + 1);
        }
        return result;
    }

    /**
     * @see #write
     */
    public Object read(DataInput in) throws IOException {
        Timestamp result = null;
        int nanos = in.readInt();
        if (Integer.MIN_VALUE != nanos) {
            long time = in.readLong();
            result = new Timestamp(time);
            result.setNanos(nanos);
        }
        return result;
    }

    /**
     * Writes the given <i>value</i> to the given
     * <code>DataOutput</code>.  <code>Null</code> values are written
     * as <code>Integer.MIN_VALUE</code>. All other values are written
     * directly with an <code>int</code> representing nanoseconds
     * first, and a <code>long</code> representing the time.
     *
     * @param value the value to write, which must be {@link #accepts acceptable}
     */
    public void write(Object value, DataOutput out) throws IOException {
        if(null == value) {
            out.writeInt(Integer.MIN_VALUE);
        } else {
            Timestamp val = (Timestamp)convert(value);
            out.writeInt(val.getNanos());
            out.writeLong(val.getTime());
        }
    }

    public DataType makeNewInstance() {
        return new TimestampType();
    }

    public Comparator getComparator() {
        return COMPARATOR;
    }
    
    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(Object left, Object right) {
            long leftmillis = getMillis(left);
            int leftnanos = getNanos(left);
            
            long rightmillis = getMillis(right);
            int rightnanos = getNanos(right);
            
            if(leftmillis == rightmillis) {
                if(leftnanos > rightnanos) {
                    return 1;
                } else if(leftnanos == rightnanos) {
                    return 0;
                } else {
                    return -1;
                }
            } else if(leftmillis > rightmillis) {
                return 1;
            } else {
                return -1;
            }
        }
        
        private long getMillis(Object obj) {
            return ((Date)obj).getTime();
        }

        private int getNanos(Object obj) {
            if(obj instanceof Timestamp) {
                return ((Timestamp)obj).getNanos();
            } else {
                return 0;
            }
        }
    };
    
}
