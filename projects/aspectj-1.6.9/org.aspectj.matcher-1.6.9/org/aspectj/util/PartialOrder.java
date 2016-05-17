/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.util;

import java.util.*;


/** This class implements a partial order
 *  
 * It includes routines for doing a topo-sort
 */

public class PartialOrder {
    
    /** All classes that want to be part of a partial order must implement
     *  PartialOrder.PartialComparable.
     */
    public static interface PartialComparable {
        /**
         * @returns <ul>
         *   <li>+1 if this is greater than other</li>
         *   <li>-1 if this is less than other</li>
         *   <li>0 if this is not comparable to other</li>
         *   </ul>
         *   
         * <b> Note: returning 0 from this method doesn't mean the same thing as returning 
         * 0 from java.util.Comparable.compareTo()</b>
         */
        public int compareTo(Object other);
        
        /**
         * This method can provide a deterministic ordering for elements that
         * are strictly not comparable.  If you have no need for this, this method
         * can just return 0 whenever called.
         */
        public int fallbackCompareTo(Object other);
    }
    
    private static class SortObject {
        PartialComparable object;
        List/*SortObject*/ smallerObjects = new LinkedList();
        List/*SortObject*/ biggerObjects  = new LinkedList();
        
        public SortObject(PartialComparable o) {
            object = o;
        }
        
        boolean hasNoSmallerObjects() { return smallerObjects.size() == 0; }
        
        boolean removeSmallerObject(SortObject o) {
            smallerObjects.remove(o);
            return hasNoSmallerObjects();
        }
        
        void addDirectedLinks(SortObject other) {
            int cmp = object.compareTo(other.object);
            if (cmp == 0) return;
            if (cmp > 0) {
                this.smallerObjects.add(other);
                other.biggerObjects.add(this);
            } else {
                this.biggerObjects.add(other);
                other.smallerObjects.add(this);
            }
        }
        
        public String toString() {
            return object.toString(); //+smallerObjects+biggerObjects;
        }
    }

    private static void addNewPartialComparable(List graph, PartialComparable o) {
        SortObject so = new SortObject(o);
        for (Iterator i = graph.iterator(); i.hasNext(); ) {
            SortObject other = (SortObject)i.next();
            so.addDirectedLinks(other);
        }
        graph.add(so);
    }
    
    private static void removeFromGraph(List graph, SortObject o) {
        for (Iterator i = graph.iterator(); i.hasNext(); ) {
            SortObject other = (SortObject)i.next();
            
            if (o == other) i.remove();
            //??? could use this to build up a new queue of objects with no
            //??? smaller ones
            other.removeSmallerObject(o);
        }
    }
    
    /**
     * @param objects must all implement PartialComparable
     * 
     * @returns the same members as objects, but sorted according to their partial
     *          order.  returns null if the objects are cyclical
     *          
     */
    public static List sort(List objects) {
        // lists of size 0 or 1 don't need any sorting
        if (objects.size() < 2) return objects;
        
        //??? we might want to optimize a few other cases of small size
        
        //??? I don't like creating this data structure, but it does give good
        //??? separation of concerns.
        List sortList = new LinkedList(); //objects.size());
        for (Iterator i = objects.iterator(); i.hasNext(); ) {
            addNewPartialComparable(sortList, (PartialComparable)i.next());
        }
        
        //System.out.println(sortList);
        
        // now we have built our directed graph
        // use a simple sort algorithm from here
        // can increase efficiency later
        // List ret = new ArrayList(objects.size());
        final int N = objects.size();
        for (int index = 0; index < N; index++) {
            //System.out.println(sortList);
            //System.out.println("-->" + ret); 
            
            SortObject leastWithNoSmallers = null;
            
            for (Iterator i = sortList.iterator(); i.hasNext(); ) {
                SortObject so = (SortObject)i.next();
                //System.out.println(so);
                if (so.hasNoSmallerObjects()) {
                    if (leastWithNoSmallers == null || 
                            so.object.fallbackCompareTo(leastWithNoSmallers.object) < 0)
                    {
                        leastWithNoSmallers = so;
                    }
                }
            }
            
            if (leastWithNoSmallers == null) return null;
            
            removeFromGraph(sortList, leastWithNoSmallers);
            objects.set(index, leastWithNoSmallers.object);
        }
        
        return objects;
    }
    
    /***********************************************************************************
    /* a minimal testing harness
    ***********************************************************************************/
    static class Token implements PartialComparable {
        private String s;
        
        Token(String s) {
            this.s = s;
        }
        
        public int compareTo(Object other) {
            Token t = (Token)other;
            
            int cmp = s.charAt(0) - t.s.charAt(0);
            if (cmp == 1) return 1;
            if (cmp == -1) return -1;
            return 0;
        }   
            
        public int fallbackCompareTo(Object other) {
            return -s.compareTo( ((Token)other).s );
        }
        
        public String toString() {
            return s;
        }
    }
    
    public static void main(String[] args) {
        List l = new ArrayList();
        l.add(new Token("a1"));
        l.add(new Token("c2"));
        l.add(new Token("b3"));
        l.add(new Token("f4"));
        l.add(new Token("e5"));
        l.add(new Token("d6"));
        l.add(new Token("c7"));
        l.add(new Token("b8"));
        
        l.add(new Token("z"));
        l.add(new Token("x"));
        
        l.add(new Token("f9"));
        l.add(new Token("e10"));
        l.add(new Token("a11"));
        l.add(new Token("d12"));
        l.add(new Token("b13"));
        l.add(new Token("c14"));
        
        System.out.println(l);
        
        sort(l);
        
        System.out.println(l);
    }
}
