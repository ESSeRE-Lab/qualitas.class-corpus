/*
 * $Id: BaseAxionCommand.java,v 1.10 2003/05/01 16:39:00 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002-2003 Axion Development Team.  All rights reserved.
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

package org.axiondb.engine.commands;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.axiondb.AxionCommand;
import org.axiondb.AxionException;
import org.axiondb.BindVariable;
import org.axiondb.ComparisonOperator;
import org.axiondb.Database;
import org.axiondb.Function;
import org.axiondb.LeafWhereNode;
import org.axiondb.Selectable;
import org.axiondb.WhereNode;
import org.axiondb.engine.ClearBindVariableWhereNodeVisitor;
import org.axiondb.engine.FlattenWhereNodeVisitor;

/**
 * Abstract base {@link AxionCommand} implementation.
 *
 * @version $Revision: 1.10 $ $Date: 2003/05/01 16:39:00 $
 * @author Rodney Waldhoff 
 * @author Chuck Burdick
 */
public abstract class BaseAxionCommand implements AxionCommand {

    /**
     * If subclasses create a {@link org.axiondb.jdbc.AxionResultSet}
     * upon execution, they should set it here so that they can
     * support {@link #execute}.
     * @see #getResultSet
     */
    protected void setResultSet(ResultSet rset) {
        _rset = rset;
    }

    public ResultSet getResultSet() {
        return _rset;
    }
    
    /**
     * If sublasses return a number of rows effected, then upon
     * execution, they should set that number here so it can support
     * {@link #execute}
     */
    protected void setEffectedRowCount(int count) {
        _rowCount = count;
    }

    public int getEffectedRowCount() {
        return _rowCount;
    }

    /**
     * Sets the <i>value</i> of the <i>i</i><sup>th</sup>
     * bind variable within this command.
     * @param index the one-based index of the variable
     * @param value the value to bind the variable to
     */
    public void bind(int index, Object value) throws AxionException {
        int origindex = index;
        for(Iterator iter = getBindVariableIterator(); iter.hasNext(); ) {            
            BindVariable var = (BindVariable)(iter.next());
            if(index == 1) {
                var.setValue(value);
                return;
            } else {
                index--;
            }
        }
        throw new AxionException("BindVariable " + origindex + " not found.");
    }

    /**
     * Clears all bind variables within this command.
     */
    public void clearBindings() throws AxionException { 
        for(Iterator iter = getBindVariableIterator(); iter.hasNext(); ) {
            BindVariable var = (BindVariable)(iter.next());
            var.clearBoundValue();
        }
    }

    /** Clear all {@link BindVariable} bindings within the given {@link WhereNode node}. */ 
    protected void clearBindings(WhereNode node) {
        if(null != node) {
            CLEAR_BINDINGS_VISITOR.visit(node);
        }
    }
    
    /** Throws an {@link AxionException} if the given {@link Database} is read-only. */
    protected void assertNotReadOnly(Database db) throws AxionException {
        if(db.isReadOnly()) {
            throw new AxionException("The database is read only.");
        }
    }

    /** 
     * Returns an {@link Iterator} over all my {@link BindVariable}s, 
     * in the proper order.  Default impl returns empty iterator.
     */
    protected Iterator getBindVariableIterator() {
        return Collections.EMPTY_SET.iterator();
    }

    public static void appendBindVariables(Selectable sel, List list) {
        if(sel instanceof BindVariable) {
            list.add(sel);
        } else if(sel instanceof Function) {
            Function fn = (Function)sel;
            for(int i=0;i<fn.getArgumentCount();i++) {
                appendBindVariables(fn.getArgument(i),list);
            }
        }
    }  
      
    public static void appendBindVariables(WhereNode where, List list) {
        if(null == where) {
            return;
        }
        CollectBindVariablesWhereNodeVisitor visitor = new CollectBindVariablesWhereNodeVisitor();
        visitor.visit(where);
        list.addAll(visitor.getList());
    }  
    
    /**
     * Decomposes the given {@link WhereNode} into a {@link Set}
     * of nodes that were originally joined by ANDs, and adds to this
     * set predicates that are implied by the original tree (for example,
     * given <tt>A = 1</tt> and <tt>A = B</tt>, we can infer <tt>B = 1</tt>.)   
     */
    protected Set processWhereTree(WhereNode tree) {
        Set set = null;
        // flatten into an ANDed set
        if(null == tree) {
            return Collections.EMPTY_SET;
        } else {
            FlattenWhereNodeVisitor visitor = new FlattenWhereNodeVisitor();
            set = visitor.getNodes(tree);
        }

        Set joins = new HashSet();
        Set assigns = new HashSet();
        
        for(Iterator iter = set.iterator();iter.hasNext();) {
            WhereNode node = (WhereNode)(iter.next());
            if(node instanceof LeafWhereNode) {
                LeafWhereNode leaf = (LeafWhereNode)node;
                if(leaf.isColumnColumn()) {
                    joins.add(leaf);
                } else if(leaf.isColumnLiteral()) {
                    assigns.add(leaf);
                }
            }
        }

        for(Iterator jiter = joins.iterator();jiter.hasNext();) {
            LeafWhereNode join = (LeafWhereNode)(jiter.next());
            if(ComparisonOperator.EQUAL.equals(join.getOperator())) {
                for(Iterator aiter = assigns.iterator();aiter.hasNext();) {
                    LeafWhereNode assign = (LeafWhereNode)(aiter.next());
                    if(join.getLeft().equals(assign.getLeft())) {
                        WhereNode node = new LeafWhereNode(join.getRight(),assign.getOperator(),assign.getRight());
                        /*
                        if(_log.isDebugEnabled()) {
                            _log.debug("Adding " + node + " to WHERE because I found " + join + " AND " + assign);
                        }
                        */
                        set.add(node);
                    } else if(join.getLeft().equals(assign.getRight())) {
                        WhereNode node = new LeafWhereNode(assign.getLeft(),assign.getOperator(),join.getRight());
                        /*
                        if(_log.isDebugEnabled()) {
                            _log.debug("Adding " + node + " to WHERE because I found " + join + " AND " + assign);
                        }
                        */
                        set.add(node);
                    } else if(join.getRight().equals(assign.getLeft())) {
                        WhereNode node = new LeafWhereNode(join.getLeft(),assign.getOperator(),assign.getRight());
                        /*
                        if(_log.isDebugEnabled()) {
                            _log.debug("Adding " + node + " to WHERE because I found " + join + " AND " + assign);
                        }
                        */
                        set.add(node);
                    } else if(join.getRight().equals(assign.getRight())) {
                        WhereNode node = new LeafWhereNode(assign.getLeft(),assign.getOperator(),join.getLeft());
                        /*
                        if(_log.isDebugEnabled()) {
                            _log.debug("Adding " + node + " to WHERE because I found " + join + " AND " + assign);
                        }
                        */
                        set.add(node);
                    }
                }
            }
        }
        return set;
    }

    private ResultSet _rset = null;
    private int _rowCount = -1;

    /** A singleton instance of the stateless {@link ClearBindVariableWhereNodeVisitor}. */
    private static final ClearBindVariableWhereNodeVisitor CLEAR_BINDINGS_VISITOR = new ClearBindVariableWhereNodeVisitor(); 
}
