/*
 * $Id: AxionException.java,v 1.2 2002/11/02 23:38:21 rwald Exp $
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

package org.axiondb;

/**
 * Root exception for Axion related or specific problems.
 *
 * @version $Revision: 1.2 $ $Date: 2002/11/02 23:38:21 $
 * @author Rodney Waldhoff
 */
public class AxionException extends Exception {
    /** Equivalent to {@link #AxionException(java.lang.String,java.lang.Throwable) AxionException(null,null)}. */
    public AxionException() {
        this(null, null);
    }

    /** Equivalent to {@link #AxionException(java.lang.String,java.lang.Throwable) AxionException(message,null)}. */
    public AxionException(String message) {
        this(message, null);
    }

    /** Equivalent to {@link #AxionException(java.lang.String,java.lang.Throwable) AxionException(null,nested)}. */
    public AxionException(Throwable nested) {
        this(null,nested);
    }

    /** 
     * Construct a new {@link AxionException} with the given
     * <i>message</i>, wrapping the given {@link Throwable}.
     * @param message my detailed message (possibly <code>null</code>)
     * @param nested a {@link Throwable} to wrap (possibly <code>null</code>)
     */
    public AxionException(String message, Throwable nested) {
        super(null == message ? 
              (null == nested ? null : nested.toString()) :
              (null == nested ? message : message + " (" + nested.toString() + ")"));
        _nested = nested;
    }

    /**
     * Return the {@link Throwable} I'm wrapping, if any.
     * @return the {@link Throwable} I'm wrapping, if any.
     */
    public Throwable getNestedThrowable() {
        return _nested;
    }

    private Throwable _nested = null;
}
