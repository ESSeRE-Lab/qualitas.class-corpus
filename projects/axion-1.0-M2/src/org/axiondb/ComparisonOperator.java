/*
 * $Id: ComparisonOperator.java,v 1.6 2002/12/16 22:18:29 rwald Exp $
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

import java.util.Comparator;

/**
 * A comparison operator, like <code>==</code>
 * or <code>&gt;=</code>.
 *
 * @version $Revision: 1.6 $ $Date: 2002/12/16 22:18:29 $
 * @author Chuck Burdick
 * @author Rodney Waldhoff
 * @author Rob Oxspring
 */
public abstract class ComparisonOperator {
    /**
     * Private constructor.
     * All instances are singletons, and enumerated in this class.
     */
    private ComparisonOperator(String name) {
        _id = _nextId++;
        _name = name;
    }

    public String toString() {
        return _name;
    }

    public boolean equals(Object that) {
        // XXX ANSWER ME XXX
        // would (this == that) suffice?
        if(that instanceof ComparisonOperator) {
            ComparisonOperator thatop = (ComparisonOperator)that;
            return (thatop._id == this._id);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return _id;
    }

    /**
     * Evaluate this comparision against the given Objects
     * using the given Comparator.
     */
    public abstract boolean compare(Object left, Object right, Comparator comparator);
    
    /** 
     * Return the equivalent ComparisonOperator if my arguments 
     * were reversed. In other words, 
     * <code>op.compare(<i>a</i>,<i>b</i>,<i>comparator</i>)</code> is <code>true</code> iff
     * <code>op.flip().compare(<i>b</i>,<i>a</i>,<i>comparator</i>)</code> is also <code>true</code>.
     */
    public abstract ComparisonOperator flip();

    /** 
     * The equality comparison (<code>==</code>). 
     */
    public static final ComparisonOperator EQUAL = new ComparisonOperator("==") {

        /** 
         * Returns <code>true</code> iff neither <i>left</i> nor <i>right</i>
         * is <code>null</code> and 
         * <code><i>comparator.{@link Comparator#compare compare(<i>left</i>,<i>right</i>)}</code>
         * returns <code>0</code>.
         */
        public boolean compare(Object left, Object right, Comparator comparator) {
            if(null == left || null == right) {
                return false;
            } else {
                return comparator.compare(left,right) == 0;
            }
        }

        /** Returns {@link ComparisonOperator#EQUAL}. */
        public ComparisonOperator flip() {
            return EQUAL;
        }
    };

    /** 
     * The inequality comparison (<code>!=</code>). 
     */
    public static final ComparisonOperator NOT_EQUAL = new ComparisonOperator("!=") {

        /** 
         * Returns <code>true</code> iff neither <i>left</i> nor <i>right</i>
         * is <code>null</code> and 
         * <code><i>comparator.{@link Comparator#compare compare(<i>left</i>,<i>right</i>)}</code>
         * returns a non-zero value.
         */
        public boolean compare(Object left, Object right, Comparator comparator) {
            if(null == left || null == right) {
                return false;
            } else {
                return comparator.compare(left,right) != 0;
            }
        }

        /** Returns {@link ComparisonOperator#NOT_EQUAL}. */
        public ComparisonOperator flip() {
            return NOT_EQUAL;
        }
    };

    /** 
     * The strictly greater than comparison (<code>&gt;</code>). 
     */
    public static final ComparisonOperator GREATER_THAN = new ComparisonOperator(">") {

        /** 
         * Returns <code>true</code> iff neither <i>left</i> nor <i>right</i>
         * is <code>null</code> and 
         * <code><i>comparator.{@link Comparator#compare compare(<i>left</i>,<i>right</i>)}</code>
         * returns a positive value.
         */
        public boolean compare(Object left, Object right, Comparator comparator) {
            if(null == left || null == right) {
                return false;
            } else {
                return comparator.compare(left,right) > 0;
            }
        }

        /** Returns {@link ComparisonOperator#LESS_THAN}. */
        public ComparisonOperator flip() {
            return LESS_THAN;
        }
    };

    /** 
     * The strictly less than comparison (<code>&lt;</code>). 
     */
    public static final ComparisonOperator LESS_THAN = new ComparisonOperator("<") {

        /** 
         * Returns <code>true</code> iff neither <i>left</i> nor <i>right</i>
         * is <code>null</code> and 
         * <code><i>comparator.{@link Comparator#compare compare(<i>left</i>,<i>right</i>)}</code>
         * returns a negative value.
         */
        public boolean compare(Object left, Object right, Comparator comparator) {
            if(null == left || null == right) {
                return false;
            } else {
                return comparator.compare(left,right) < 0;
            }
        }
        
        /** Returns {@link ComparisonOperator#GREATER_THAN}. */
        public ComparisonOperator flip() {
            return GREATER_THAN;
        }
    };

    /** 
     * The greater than or equality comparison (<code>&gt;=</code>). 
     */
    public static final ComparisonOperator GREATER_THAN_OR_EQUAL = new ComparisonOperator(">=") {

        /** 
         * Returns <code>true</code> iff neither <i>left</i> nor <i>right</i>
         * is <code>null</code> and 
         * <code><i>comparator.{@link Comparator#compare compare(<i>left</i>,<i>right</i>)}</code>
         * returns a non-negative value.
         */
        public boolean compare(Object left, Object right, Comparator comparator) {
            if(null == left || null == right) {
                return false;
            } else {
                return comparator.compare(left,right) >= 0;
            }
        }
        
        /** Returns {@link ComparisonOperator#LESS_THAN_OR_EQUAL}. */
        public ComparisonOperator flip() {
            return LESS_THAN_OR_EQUAL;
        }
    };

    /** 
     * The less than or equality comparison (<code>&lt;=</code>). 
     */
    public static final ComparisonOperator LESS_THAN_OR_EQUAL = new ComparisonOperator("<=") {

        /** 
         * Returns <code>true</code> iff neither <i>left</i> nor <i>right</i>
         * is <code>null</code> and 
         * <code><i>comparator.{@link Comparator#compare compare(<i>left</i>,<i>right</i>)}</code>
         * returns a non-positive value.
         */
        public boolean compare(Object left, Object right, Comparator comparator) {
            if(null == left || null == right) {
                return false;
            } else {
                return comparator.compare(left,right) <= 0;
            }
        }

        /** Returns {@link ComparisonOperator#GREATER_THAN_OR_EQUAL}. */
        public ComparisonOperator flip() {
            return GREATER_THAN_OR_EQUAL;
        }
    };

    /**
     * The <code>NULL</code> comparison (<code>IS NULL</code>).
     */
    public static final ComparisonOperator IS_NULL = new ComparisonOperator("IS NULL") {

        /** 
         * Returns <code>true</code> iff <i>left</i> is <code>null</code>.
         * The other parameters are ignored.
         * 
         * @param left the object to check against null
         * @param right ignored
         * @param comparator ignored.
         */
        public boolean compare(Object left, Object right, Comparator comparator) {
            return null == left;
        }
        
        /** Returns {@link ComparisonOperator#IS_NULL}. */
        public ComparisonOperator flip() {
            return IS_NULL;
        }
    };

    /**
     * The <code>NOT NULL</code> comparison (<code>IS NOT NULL</code>).
     */
    public static final ComparisonOperator IS_NOT_NULL = new ComparisonOperator("IS NOT NULL") {

        /** 
         * Returns <code>true</code> iff neither <i>left</i> is not <code>null</code>.
         * The other parameters are ignored.
         * 
         * @param left the object to check against null
         * @param right ignored
         * @param comparator ignored.
         */
        public boolean compare(Object left, Object right, Comparator comparator) {
            return null != left;
        }

        /** Returns {@link ComparisonOperator#IS_NOT_NULL}. */
        public ComparisonOperator flip() {
            return IS_NOT_NULL;
        }
    };

    private final int _id;
    private String _name;
    private static int _nextId = 0;

}