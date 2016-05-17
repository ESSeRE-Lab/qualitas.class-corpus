/* $Id: FromNode.java,v 1.2 2003/03/27 19:14:04 rwald Exp $
 * =======================================================================
 * Copyright (c) 2003 Axion Development Team.  All rights reserved.
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
 * Structure presenting the <code>FROM</code> clause.
 * @version $Revision: 1.2 $ $Date: 2003/03/27 19:14:04 $
 * @author Amrish Lal
 *
 */
public class FromNode {
 

    public FromNode() {

    }

    /**
     * Set the left input
     * @param table that is set as left input.
     */
    public void setLeft(TableIdentifier table) {
        _left = table;
    }

    /**
     * Set the left input
     * @param join that is set is left input
     */
    public void setLeft(FromNode join) {
        _left = join;
    }

    /**
     * Set the right input
     * @param table that is set as right input.
     */
    public void setRight(TableIdentifier table) {
        _right = table;
    }

    /**
     * Set the right input
     * @param join that is set as right input.
     */
    public void setRight(FromNode join) {
        _right = join;
    }

    /**
     * Set the type of join.
     * @param type integer value representing join type (INNER, LEFT OUTER, RIGHT OUTER)
     */
    public void setType(int type) {
        _type = type;
    }

    /**
     * Set the join condition
     * @param type condition Join condition.
     */
    public void setCondition(WhereNode condition) {
        _condition = condition;
    }

    /**
     * Get the Left input
     * @return Object of type {@link FromNode} or {@link TableIdenfier}
     */
    public Object getLeft() {
        return _left;
    }

    /**
     * get the right input
     * @return Object of type {@link FromNode} or {@link TableIdenfier}
     */
    public Object getRight() {
        return _right;
    }

    /**
     * get the type of the join
     * @return inteter indicating type (UNDEFINED, LEFT OUTER, RIGHT OUTER, INNER)
     */
    public int getType() {
        return _type;
    }

    /**
     * get the join condition
     * @return join condition.
     */
    public WhereNode getCondition() {
        return _condition;
    }

    public void setDisplayPrefix(String prefix) {
        _prefix = prefix;
    }

    public String toString() {
        String result = "\n";
        result += _prefix + "Type : " + this.getType() + "\n";
        if (_left instanceof TableIdentifier) {
            TableIdentifier table = (TableIdentifier) _left;
            result += _prefix + "Left : " + " TableIdentifier " + table.getTableName() + "\n";
        }
        if (_left instanceof FromNode) {
            FromNode node = (FromNode) _left;
            node.setDisplayPrefix(_prefix + "\t");
            result += _prefix + "Left : " + " FromNode " + node.toString() + "\n";
        }
        
        if (_right instanceof TableIdentifier) {
            TableIdentifier table = (TableIdentifier) _right;
            result += _prefix + "Right: " + " TableIdentifier " + table.getTableName() + "\n";            
        }

        if (_right instanceof FromNode) {
            FromNode node = (FromNode) _right;
            node.setDisplayPrefix(_prefix + "\t");
            result += _prefix + "Right: " + " FromNode " + node.toString() + "\n";
        }
        return (result);
    }

    /**
     * Number of tables in this FromNode and its children.
     * @return table count.
     */
    public int getTableCount() {
        int count = 0;
        if (_left == null && _right == null) {
            return (0);
        }

        if (_left instanceof TableIdentifier) {
            count++;
        }

        if (_left instanceof FromNode) {
            count += ((FromNode)_left).getTableCount();
        }

        if (_right instanceof TableIdentifier) {
            count++;
        }

        if (_right instanceof FromNode) {
            count += ((FromNode)_right).getTableCount();
        }
        return (count);
    }

    /**
     * Array of tables in this FromNode or its children. Array is
     * devleoped by preorder traversal of the FromNode tree.
     * @return Array of {@link TableIdentifier}
     */
    public TableIdentifier[] toTableArray() {
        int tableCount = getTableCount();
        int pos = 0;
        if (tableCount == 0) {
            return (null);
        }
        
        TableIdentifier[] tables = new TableIdentifier[getTableCount()];
        toTableArray(tables, pos);
        return (tables);
    }

    private int toTableArray(TableIdentifier[] tables, int pos) {
        if (_left instanceof TableIdentifier) {
            tables[pos]= ((TableIdentifier) _left);
            pos++;
        }

        if (_left instanceof FromNode) {
            pos = ((FromNode) _left).toTableArray(tables, pos);
        }

        if (_right instanceof TableIdentifier) {
            tables[pos] = ((TableIdentifier) _right);
            pos++;
        }

        if (_right instanceof FromNode) {
            pos = ((FromNode) _right).toTableArray(tables, pos);
        }
        return (pos);
    }


    /**
     * Join Type is undefined
     */
    public static final int TYPE_UNDEFINED = -1;

    /**
     * No Join
     */
    public static final int TYPE_SINGLE    = 0;

    /**
     * Inner join.
     */
    public static final int TYPE_INNER     = 1;

    /**
     * Left outer join
     */
    public static final int TYPE_LEFT      = 2;

    /**
     * Right outer join
     */
    public static final int TYPE_RIGHT     = 3;
    
    /**
     * Full outer join (unsupported)
     */
    public static final int TYPE_FULL      = 4;

    /**
     * Left input table identifier or FromNode.
     */
    private Object _left = null;

    /**
     * Right input table identifier or FromNode.
     */
    private Object _right = null;

    /**
     * Join type
     */
    private int _type = TYPE_UNDEFINED;

    /**
     * Join condition
     */
    private WhereNode _condition = null;

    private String _prefix = "";
}