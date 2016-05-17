/*
 * $Id: BTree.java,v 1.22 2003/05/16 20:22:59 rwald Exp $
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

package org.axiondb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.IntListIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A B-Tree for integers, based on the implementation described
 * in "Introduction to Algorithms" by Cormen, Leiserson and Rivest (CLR).
 * 
 * @version $Revision: 1.22 $ $Date: 2003/05/16 20:22:59 $
 * @author Chuck Burdick
 * @author Dave Pekarek Krohn
 */
public class BTree {

    // constructors
    // ------------------------------------------------------------------------

    /**
     * Create or load a new root node
     */
    public BTree(File idxDir, String idxName, int degree) throws IOException {
        this(idxDir, idxName, degree, null);
    }

    /**
     * Create or load a node
     */
    public BTree(File idxDir, String idxName, int degree, BTree root) throws IOException {
        _idxDir = idxDir;
        _idxName = idxName;
        _degree = degree; // t
        _maxCap = (2 * _degree) - 1;
        _keys = new ArrayIntList(_maxCap);
        _vals = new ArrayIntList(_maxCap);
        _childIds = new ArrayIntList(_maxCap + 1);
        _loadedChildren = new HashMap();

        if (root != null) {
            //Child node need to allocate the next in the sequence
            _root = root;
        } else {
            //Root node need to load the counter file
            _root = this;

            boolean treeExists = (null != idxDir) && getCounterFile().exists();
            if (treeExists) {
                setCounter(0);
                setFileId(getCounter());

                loadIdxCtr();
                read();
            } else {
                allocate();
            }
        }
    }

    /**
     * Create or load a new node
     */
    public BTree(File idxDir, String idxName, int degree, BTree root, boolean shouldCreate) throws IOException {
        this(idxDir, idxName, degree, root);
        if (shouldCreate) {
            allocate();
        }
    }

    /**
     * Read in an existing node file
     */
    public BTree(File idxDir, String idxName, int degree, int fileId, BTree root)
        throws IOException {
        this(idxDir, idxName, degree, root);
        setFileId(fileId);
        read();
    }

    // public methods
    // ------------------------------------------------------------------------

    public IntListIterator valueIterator() throws IOException {
        return new IntIteratorIntListIterator(new BTreeValueIterator(this));
    }

    public IntListIterator valueIteratorGreaterThanOrEqualTo(int fromkey) throws IOException {
        StateStack stack = new StateStack();
        for(BTree node = this; null != node;) {            
            int i = 0;
            while(i < node.size() && fromkey > node.getKey(i)) {
                i++;                
            }
            stack.push(node,true,i);
            node = node.getChildOrNull(i);
        }
        return new IntIteratorIntListIterator(new BTreeValueIterator(stack));
    }

    public IntListIterator valueIteratorGreaterThan(int fromkey) throws IOException {
        return valueIteratorGreaterThanOrEqualTo(fromkey + 1);
    }

    // -----------------------------------------------------------------------------------------------

    public int size() {
        return getKeys().size();
    }

    public void insert(int key, int value) throws IOException {
        if (size() == _maxCap) {
            // grow and rotate tree
            if (_log.isDebugEnabled()) {
                _log.debug(
                    "Node " + _fileId + " reached max capacity. Splitting and rotating.");
            }

            _log.debug("Creating new child node");
            BTree child = new BTree(_idxDir, getName(), _degree, _root, true);

            _log.debug("Transferring all data to child");
            child.getKeys().addAll(_keys);
            child.getValues().addAll(_vals);
            if (getChildIds().size() > 0) {
                child.addChildrenFrom(this);
                _log.debug("Transferred children to child");
            }

            _log.debug("Emptying my data");
            getKeys().clear();
            getValues().clear();
            getChildIds().clear();

            _log.debug("Attaching new child");
            addChild(0, child);

            _log.debug("Subdividing child into 2 children");
            subdivideChild(0, child);
        }
        insertNotfull(key, value);
    }

    public void delete(int key) throws IOException {
        //Comments refer to the cases described in CLR (19.3)
        if (_log.isDebugEnabled()) {
            _log.debug("Deleting: " + key);
        }
        if (size() <= 0) {
            _log.warn("keys: " + getKeys());
            _log.warn("values: " + getValues());
            _log.warn("childids: " + getChildIds());
            _log.warn("Tree is empty: " + this);
            return;
        }
        int i = findNearestKeyBelow(key);
        if ((i < 0 && getKey(i + 1) != key) || getKey(i) != key) {
            _log.debug("Case 3");
            //Case 3           
            int pivotLoc = i + 1;
            if (!isLeaf() && getChild(pivotLoc).getKeys().size() < _degree) {
                if (pivotLoc > 0 && getChild(pivotLoc - 1).getKeys().size() >= _degree) {
                    _log.debug("Case 3a, left");
                    //Case 3a, borrow-left
                    borrowLeft(pivotLoc);
                    getChild(pivotLoc).delete(key);
                } else if (
                    pivotLoc + 1 < getChildIds().size()
                        && getChild(pivotLoc + 1).getKeys().size() >= _degree) {
                    _log.debug("Case 3a, right");
                    //Case 3a, borrow-right
                    borrowRight(pivotLoc);
                    getChild(pivotLoc).delete(key);
                } else {
                    _log.debug("Case 3b");
                    //Case 3b

                    // if the key is on the far right, then we need to merge the last two nodes
                    int mergeLoc;
                    if (pivotLoc < getKeys().size()) {
                        mergeLoc = pivotLoc;
                    } else {
                        mergeLoc = pivotLoc - 1;
                    }

                    if (_log.isDebugEnabled()) {
                        _log.debug("Tree before merge: " + this);
                        _log.debug("Merge location: " + mergeLoc);
                    }

                    mergeChildren(mergeLoc, getKey(mergeLoc));

                    if (_log.isDebugEnabled()) {
                        _log.debug("Tree after merge: " + this);
                    }

                    maybeCollapseTree();

                    delete(key);
                }
            } else {
                getChild(i + 1).delete(key);
            }
        } else {
            if (isLeaf()) {
                _log.debug("Case 1");
                //Case 1
                getKeys().removeElementAt(i);
                getValues().removeElementAt(i);
                if (_log.isDebugEnabled()) {
                    _log.debug("Node " + _fileId + " deleted key " + key);
                }
            } else {
                _log.debug("Case 2");
                //Case 2
                if (getChild(i).size() >= _degree) {
                    _log.debug("Case 2a");
                    //Case 2a, move predecessor up
                    int[] keyParam = new int[1];
                    int[] valueParam = new int[1];
                    if (_log.isDebugEnabled()) {
                        _log.debug("Left child: " + getChild(i));
                    }
                    getChild(i).getRightMost(keyParam, valueParam);
                    getKeys().set(i, keyParam[0]);
                    getValues().set(i, valueParam[0]);
                    getChild(i).delete(keyParam[0]);
                } else if (getChild(i + 1).size() >= _degree) {
                    _log.debug("Case 2b");
                    //Case 2b, move successor up
                    int[] keyParam = new int[1];
                    int[] valueParam = new int[1];
                    if (_log.isDebugEnabled()) {
                        _log.debug("Right child: " + getChild(i + 1));
                    }
                    getChild(i + 1).getLeftMost(keyParam, valueParam);
                    getKeys().set(i, keyParam[0]);
                    getValues().set(i, valueParam[0]);
                    getChild(i + 1).delete(keyParam[0]);
                } else {
                    _log.debug("Case 2c");
                    //Case 2c, merge nodes
                    mergeChildren(i, key);

                    //Now delete from the newly merged node
                    getChild(i).delete(key);

                    maybeCollapseTree();
                }
            }
        }
    }

    public IntListIterator getAll(int key) throws IOException {
        IntListIteratorChain chain = new IntListIteratorChain();
        getAll(key,chain);
        return chain;
    }

    public IntListIterator getAllTo(int key) throws IOException {
        IntListIteratorChain chain = new IntListIteratorChain();
        getAllTo(key,chain);
        return chain;
    }
    
    /**
     * Uses the shortest path to a matching entry and returns its
     * value. Not necessarily the least value, the first entered, or
     * the leftmost.
     */
    public Integer get(int key) throws IOException {
        int i = findNearestKeyAbove(key);
        if ((i < size()) && (key == getKey(i))) {
            return new Integer(getValues().get(i));
        } else if (!isLeaf()) {
            // recurse to children
            return getChild(i).get(key);
        } else {
            return null;
        }
    }

    public IntListIterator getAllFrom(int key) throws IOException {
        IntListIteratorChain chain = new IntListIteratorChain();
        getAllFrom(key,chain);
        return chain;
    }
    
    /**
     * Saves the tree.  It saves the counter file, writes out the node
     * and then calls save recursively through the tree.
     */
    public void save(File dataDirectory) throws IOException {
        _idxDir = dataDirectory;
        save();
    }

    public void save() throws IOException {
        if (isRoot()) {
            saveIdxCtr();
        }
        write();
        for (int i = 0; i < getChildIds().size(); i++) {
            getChild(i).save(_idxDir);
        }
    }

    public void replaceId(int key, int oldId, int newId) throws IOException {
        int i = findNearestKeyAbove(key);
        boolean valSet = false;
        while ((i < size()) && key == getKey(i)) {
            if (!isLeaf()) {
                getChild(i).replaceId(key, oldId, newId);
            }
            if (getValue(i) == oldId) {
                setValue(i, newId);
                valSet = true;
                break;
            }
            i++;
        }
        if (!valSet && !isLeaf()) {
            getChild(i).replaceId(key, oldId, newId);
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("(id");
        buf.append(_fileId);
        buf.append(",");
        buf.append("(");
        for (int i = 0; i < size() + 1; i++) {
            if (!isLeaf()) {
                try {
                    buf.append(getChild(i).toString());
                } catch (IOException e) {
                    _log.error("Cannot retrieve child", e);
                }
                if (i < size()) {
                    buf.append(",");
                }
            }
            if (i < size()) {
                buf.append(getKey(i));
                buf.append("->");
                buf.append(getValues().get(i));
                if (!isLeaf() || i < (size() - 1)) {
                    buf.append(",");
                }
            }
        }
        buf.append(")");
        buf.append(")");
        return buf.toString();
    }


    // private methods
    // ------------------------------------------------------------------------

    /**
     * Sets the fileId.
     */
    private void allocate() {
        setFileId(getRoot().getCounter());
        getRoot().setCounter(getFileId() + 1);
    }

    /**
     * Loads the counter file if it exists.
     * This sets the counter.  Only the root
     * should ever call this.
     */
    private void loadIdxCtr() throws IOException {
        File idxCtr = getCounterFile();
        if (!isRoot()) {
            //If this isn't the root, then there is a problem.
            _log.warn("Non root attempting to load counter file");
            return;
        }
        FileInputStream fin = null;
        ObjectInputStream in = null;
        try {
            fin = new FileInputStream(idxCtr);
            in = new ObjectInputStream(fin);
            setCounter(in.readInt());
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                in.close();
            } catch (Exception e) {}
            try {
                fin.close();
            } catch (Exception e) {}
        }
    }

    /**
     * Saves out the counter file.  This should only
     * ever be called by the root.
     */
    private void saveIdxCtr() throws IOException {
        File idxCtr = getCounterFile();
        if (!isRoot()) {
            //If this isn't the root, then there is a problem.
            _log.warn("Non root attempting to save counter file");
            return;
        }
        FileOutputStream fout = null;
        ObjectOutputStream out = null;
        if (!idxCtr.exists()) {
            idxCtr.createNewFile();
        }
        // increment counter
        try {
            fout = new FileOutputStream(idxCtr);
            out = new ObjectOutputStream(fout);
            out.writeInt(getCounter());
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                out.close();
            } catch (Exception e) {}
            try {
                fout.close();
            } catch (Exception e) {}
        }
    }

    private File getFileById(int fileid) {
        return new File(_idxDir, getName() + "." + fileid);
    }

    private File getCounterFile() {
        return new File(_idxDir, _idxName + ".ctr");
    }

    String getName() {
        return _idxName;
    }

    private IntList getKeys() {
        return _keys;
    }

    private void setKeys(IntList keys) {
        _keys = keys;
    }

    private IntList getValues() {
        return _vals;
    }

    private void setValues(IntList vals) {
        _vals = vals;
    }

    int getValue(int index) {
        return _vals.get(index);
    }

    private void setValue(int index, int val) {
        _vals.set(index, val);
    }

    private IntList getChildIds() {
        return _childIds;
    }

    private Map getLoadedChildren() {
        return _loadedChildren;
    }

    private int getCounter() {
        if (isRoot()) {
            return _counter;
        } else {
            return getRoot().getCounter();
        }
    }

    private void setCounter(int counter) {
        if (isRoot()) {
            _counter = counter;
        } else {
            getRoot().setCounter(counter);
        }
    }

    int getFileId() {
        return _fileId;
    }

    private void setFileId(int fileId) {
        _fileId = fileId;
    }

    private BTree getRoot() {
        return _root;
    }

    boolean isLeaf() {
        return (getChildIds().size() == 0);
    }

    private boolean isRoot() {
        return (getRoot() == this);
    }

    private void borrowLeft(int borrowLoc) throws IOException {
        BTree leftSibling = getChild(borrowLoc - 1);
        BTree rightSibling = getChild(borrowLoc);

        if (_log.isDebugEnabled()) {
            _log.debug("Doing borrow left at: " + borrowLoc);
            _log.debug("Left sibling: " + leftSibling);
            _log.debug("Right sibling: " + rightSibling);
        }

        //Add the upper key to as the first entry of the right sibling
        rightSibling.getKeys().add(0, getKey(borrowLoc - 1));
        rightSibling.getValues().add(0, getValues().get(borrowLoc - 1));

        //Make the upper node's key the last key from the left sibling
        getKeys().set(
            borrowLoc - 1,
            leftSibling.getKey(leftSibling.getKeys().size() - 1));
        getValues().set(
            borrowLoc - 1,
            leftSibling.getValues().get(leftSibling.getValues().size() - 1));

        //If the siblings aren't leaves, move the last child from the left to be the first on the right
        if (!leftSibling.isLeaf()) {
            rightSibling.addChild(
                0,
                leftSibling.getChild(leftSibling.getChildIds().size() - 1));
        }

        //Remove the last entry of the left sibling (now moved to upper node)
        leftSibling.getKeys().removeElementAt(leftSibling.getKeys().size() - 1);
        leftSibling.getValues().removeElementAt(leftSibling.getValues().size() - 1);
        if (!leftSibling.isLeaf()) {
            leftSibling.getChildIds().removeElementAt(
                leftSibling.getChildIds().size() - 1);
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Left sibling after: " + leftSibling);
            _log.debug("Right sibling after: " + rightSibling);
        }
    }

    private void borrowRight(int borrowLoc) throws IOException {
        BTree leftSibling = getChild(borrowLoc);
        BTree rightSibling = getChild(borrowLoc + 1);

        if (_log.isDebugEnabled()) {
            _log.debug("Doing borrow right at: " + borrowLoc);
            _log.debug("Left sibling: " + leftSibling);
            _log.debug("Right sibling: " + rightSibling);
        }

        //Add the upper key to as the last entry of the left sibling
        leftSibling.getKeys().add(getKey(borrowLoc));
        leftSibling.getValues().add(getValues().get(borrowLoc));

        //Make the upper node's key the first key from the right sibling
        getKeys().set(borrowLoc, rightSibling.getKey(0));
        getValues().set(borrowLoc, rightSibling.getValues().get(0));

        //If the siblings aren't leaves, move the first child from the right to be the last on the left
        if (!leftSibling.isLeaf()) {
            leftSibling.addChild(rightSibling.getChild(0));
        }

        //Remove the first entry of the right sibling (now moved to upper node)
        rightSibling.getKeys().removeElementAt(0);
        rightSibling.getValues().removeElementAt(0);
        if (!rightSibling.isLeaf()) {
            rightSibling.getChildIds().removeElementAt(0);
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Left sibling after: " + leftSibling);
            _log.debug("Right sibling after: " + rightSibling);
        }
    }

    private void mergeChildren(int mergeLoc, int key) throws IOException {
        if (_log.isDebugEnabled()) {
            _log.debug("Merging children at: " + mergeLoc);
        }
        BTree leftChild = getChild(mergeLoc);
        BTree rightChild = getChild(mergeLoc + 1);
        if (_log.isDebugEnabled()) {
            _log.debug("Left child: " + leftChild);
            _log.debug("Right child: " + rightChild);
        }

        //Move the key down to the left child
        leftChild.getKeys().add(key);
        leftChild.getValues().add(getValues().get(mergeLoc));

        //Copy the keys and values from the right to the left
        leftChild.getKeys().addAll(rightChild.getKeys());
        leftChild.getValues().addAll(rightChild.getValues());

        //If not a leaf copy the child pointers from right to left
        if (!leftChild.isLeaf()) {
            leftChild.addChildrenFrom(rightChild);
        }

        //Now remove the item from the upper node (since it's been put in left child)
        getKeys().removeElementAt(mergeLoc);
        getValues().removeElementAt(mergeLoc);
        getChildIds().removeElementAt(mergeLoc + 1);

        rightChild.getKeys().clear();
        rightChild.getValues().clear();
        rightChild.getChildIds().clear();

        if (_log.isDebugEnabled()) {
            _log.debug("Left child after: " + leftChild);
            _log.debug("Right child after: " + rightChild);
        }
    }

    private void maybeCollapseTree() throws IOException {
        if (!isLeaf() && getKeys().size() <= 0) {
            _log.debug("Collapsing tree");
            BTree nodeToPromote = getChild(0);
            setKeys(nodeToPromote.getKeys());
            setValues(nodeToPromote.getValues());
            getChildIds().clear();
            addChildrenFrom(nodeToPromote);

            if (_log.isDebugEnabled()) {
                _log.debug("Tree after collapse: " + this);
            }
        }
    }

    /**
     * Finds and deletes the left most value from this subtree.
     * The key and value for the node is returned in the
     * parameters.  This also does the replacement as it unwraps.
     * 
     */
    private void getLeftMost(int[] keyParam, int valueParam[]) throws IOException {
        if (isLeaf()) {
            keyParam[0] = getKey(0);
            valueParam[0] = getValues().get(0);
        } else {
            getChild(0).getLeftMost(keyParam, valueParam);
        }
    }

    /**
     * Finds and deletes the right most value from this subtree.
     * The key and value for the node is returned in the
     * parameters.  This also does the replacement as it unwraps.
     * 
     */
    private void getRightMost(int[] keyParam, int valueParam[]) throws IOException {
        if (isLeaf()) {
            int max = size() - 1;
            keyParam[0] = getKey(max);
            valueParam[0] = getValues().get(max);
        } else {
            int max = getChildIds().size() - 1;
            getChild(max).getRightMost(keyParam, valueParam);
        }
    }

    private void insertNotfull(int key, int value) throws IOException {
        int i = findNearestKeyBelow(key);
        if (isLeaf()) {
            if (_log.isDebugEnabled()) {
                _log.debug("Node " + _fileId + " adding key " + key);
            }
            getKeys().add(i + 1, key);
            getValues().add(i + 1, value);
        } else {
            // recurse
            if (_log.isDebugEnabled()) {
                _log.debug("Node " + _fileId + " is internal so adding to child");
            }
            i++;
            BTree child = getChild(i);
            if (child.size() == _maxCap) {
                subdivideChild(i, child);
                if (key > getKey(i)) {
                    i++;
                }
            }
            getChild(i).insertNotfull(key, value);
        }
    }
    
    private void getAll(int key, IntListIteratorChain chain) throws IOException {
        int start = findNearestKeyAbove(key);
        if(isLeaf()) {
            int stop;
            for(stop = start; stop < size() && key == getKey(stop); stop++) { };
            chain.addIterator(getValues().subList(start,stop).listIterator());
        } else {
            int i = start;
            for(; i < size() && key == getKey(i); i++) {
                getChild(i).getAll(key,chain);
                chain.addIterator(getValues().get(i));
            }
            getChild(i).getAll(key,chain);
        }
    }

    private void getAllTo(int key, IntListIteratorChain chain) throws IOException {
        if(isLeaf()) {
            int endpoint = getKeys().indexOf(key);
            if(-1 != endpoint) {
                chain.addIterator(getValues().subList(0,endpoint).listIterator());
            } else {
                chain.addIterator(getValues().listIterator());
            }
        } else {
            // else we need to interleave my child nodes as well
            for(int i = 0; i < size() + 1; i++) {
                getChild(i).getAllTo(key,chain);
                if (i < size() && key > getKey(i)) {
                    chain.addIterator(getValues().get(i));
                } else { 
                    break;
                }
            }
        }
    }

    private void getAllFrom(int key, IntListIteratorChain chain) throws IOException {       
        int start = findNearestKeyAbove(key);
        if(isLeaf()) {
            chain.addIterator(getValues().subList(start,size()).listIterator());
        } else {
            for(int i = start; i < size() + 1; i++) {
                getChild(i).getAllFrom(key,chain);
                if(i < size()) {
                    chain.addIterator(getValues().get(i));
                } else {
                    break;
                }
            }
        }        
    }

    private void subdivideChild(int pivot, BTree child) throws IOException {
        if (_log.isDebugEnabled()) {
            _log.debug("Parent keys: " + getKeys().toString());
            _log.debug("Child keys: " + child.getKeys().toString());
        }
        int i = 0; // prepare index for loops below

        _log.debug("Create new child to take excess data");
        BTree fetus = new BTree(_idxDir, getName(), _degree, _root, true);
        addChild(pivot + 1, fetus);

        _log.debug("Transfer (t-1) vals from existing child to new child");
        IntList sub = child.getKeys().subList(_degree, _maxCap);
        if (_log.isDebugEnabled()) {
            _log.debug("Moving keys " + sub.toString());
        }
        fetus.getKeys().addAll(sub);
        sub = child.getValues().subList(_degree, _maxCap);
        fetus.getValues().addAll(sub);
        if (!child.isLeaf()) {
            _log.debug("Transfer t children from existing child to new child");
            sub = child.getChildIds().subList(_degree, _maxCap + 1);
            fetus.addChildren(sub, child.getLoadedChildren());
            for (i = _maxCap; i >= _degree; i--) {
                child.getChildIds().removeElementAt(i);
            }
        }

        _log.debug("Add new pivot key that divides children");
        int pivotKey = child.getKey(_degree - 1);
        if (_log.isDebugEnabled()) {
            _log.debug("Pivot key: " + pivotKey);
        }
        int pivotVal = child.getValues().get(_degree - 1);
        getKeys().add(pivot, pivotKey);
        getValues().add(pivot, pivotVal);

        _log.debug("Trim child to new size");
        for (i = (_maxCap - 1); i > (_degree - 2); i--) {
            child.getKeys().removeElementAt(i);
            child.getValues().removeElementAt(i);
        }
    }

    /**
     * Writes the node file out.  This is differentiated from save in
     * that it doesn't save the entire tree or the counter file.
     */
    private void write() throws IOException {
        File idxFile = getFileById(getFileId());
        if (idxFile == null) {
            throw new NullPointerException("BTree must be allocated before writing out to disk");
        }
        if (!idxFile.exists()) {
            idxFile.createNewFile();
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Writing out file " + idxFile);
            _log.debug(toString());
        }
        FileOutputStream fout = new FileOutputStream(idxFile);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeInt(size());
        for (int i = 0; i < size(); i++) {
            out.writeInt(getKey(i));
            out.writeInt(getValues().get(i));
        }
        out.writeInt(getChildIds().size());
        for (int i = 0; i < getChildIds().size(); i++) {
            out.writeInt(getChildIds().get(i));
        }
        out.flush();
        out.close();
        fout.close();
    }

    /**
     * Reads in the node.  This doesn't read in the entire subtree,
     * which happens incrementally as files are needed.
     */
    private void read() throws IOException {
        File idxFile = getFileById(getFileId());
        if (_log.isDebugEnabled()) {
            _log.debug("Reading in file " + idxFile);
        }
        FileInputStream fin = new FileInputStream(idxFile);
        ObjectInputStream in = new ObjectInputStream(fin);
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            getKeys().add(in.readInt());
            getValues().add(in.readInt());
        }
        size = in.readInt();
        for (int i = 0; i < size; i++) {
            getChildIds().add(in.readInt());
        }
        in.close();
        fin.close();
    }

    boolean hasChild(int index) throws IOException {
        return (null != getChildOrNull(index));
    }

    private BTree getChildOrNull(int index) throws IOException {
        if (index >= getChildIds().size()) {
            return null;
        } else {
            Integer fileid = new Integer(getChildIds().get(index));
            if (_loadedChildren.get(fileid) == null) {
                _loadedChildren.put(
                    fileid,
                    new BTree(_idxDir, getName(), _degree, fileid.intValue(), _root));
            }
            return (BTree)_loadedChildren.get(fileid);
        }
    }

    /** @deprecated */
    BTree getChild(int index) throws IOException {
        BTree value = getChildOrNull(index);
        if(null == value) {
            throw new IOException("Node " + _fileId + " has no child at index " + index);
        } else {
            return value;
        }
    }

    private void addChild(BTree child) {
        getChildIds().add(child.getFileId());
        _loadedChildren.put(new Integer(child.getFileId()), child);
    }

    private void addChild(int index, BTree child) {
        getChildIds().add(index, child.getFileId());
        _loadedChildren.put(new Integer(child.getFileId()), child);
    }

    private void addChildrenFrom(BTree tree) {
        addChildren(tree.getChildIds(), tree.getLoadedChildren());
    }

    private void addChildren(IntList childIds, Map children) {
        IntIterator iter = childIds.iterator();
        while (iter.hasNext()) {
            int fileid = iter.next();
            addChild((BTree)children.get(new Integer(fileid)));
        }
    }

    boolean isValid() throws IOException {
        return isValid(true);
    }

    private boolean isValid(boolean isRoot) throws IOException {
        //Check to make sure that the node isn't an empty branch
        if (!isLeaf() && (size() == 0)) {
            _log.warn("INVALID: " + this.toString());
            _log.warn("Node has no keys and " + getChildIds().size() + " children");
            return false;
        }
        //Check to make sure that the node has enough children
        if (!isRoot && getKeys().size() < _degree - 1) {
            _log.warn("INVALID: " + this.toString());
            _log.warn(
                "Node has only " + getKeys().size() + " keys for a degree of " + _degree);
            return false;
        }
        //Check to make sure that there aren't too many children for the number of entries
        if (!isLeaf()
            && (getChildIds().size() != getKeys().size() + 1
                || getKeys().size() != getValues().size())) {
            _log.warn("INVALID: " + this.toString());
            _log.warn("child ids: " + getChildIds().size());
            _log.warn("keys: " + getKeys().size());
            _log.warn("values: " + getValues().size());
            return false;
        }
        //Check all of the children
        if (!isLeaf()) {
            for (int i = 0; i < getChildIds().size(); i++) {
                if (!getChild(i).isValid(false)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int findNearestKeyBelow(int key) {
        //Short circuit
        if (size() == 0) {
            return -1;
        } else if (getKey(size() - 1) <= key) {
            return size() - 1;
        } else if (getKey(0) > key) {
            return -1;
        }

        // do a binary search for the key
        // on exit from this loop, cur will either be:
        // (a) the rightmost index of the given key within this
        // node, or 
        // (b) the position in which the given key would be inserted
        //     in this list if it was present
        //  
        int cur = 0;
        {
            int high = size();
            int low = 0;

            while (low < high) {
                cur = (high + low) / 2;
                if (getKey(cur) == key) {
                    //We found it now move to the last
                    for (;(cur < size()) && (key == getKey(cur)); cur++);
                    break;
                } else if (getKey(cur) < key) {
                    if (low == cur) {
                        low++;
                    } else {
                        low = cur;
                    }
                } else { // if(getKey(cur) > key)
                    high = cur;
                }
            }
        }

        //Now go to the nearest if there are multiple entries
        for (;(cur >= 0) && key < getKey(cur); cur--);

        return cur;
    }

    private int getKey(int pos) {
        return getKeys().get(pos);
    }

    private int findNearestKeyAbove(int key) {
        int high = size();
        int low = 0;
        int cur = 0;

        //Short circuit
        if (size() == 0) {
            return 0;
        } else if (getKey(size() - 1) < key) {
            return size();
        } else if (getKey(0) >= key) {
            return 0;
        }

        while (low < high) {
            cur = (high + low) / 2;
            if (high == low) {
                cur = low;
                break;
            } else if (getKey(cur) == key) {
                //We found it now move to the first
                for (;(cur > 0) && (key == getKey(cur)); cur--);
                break;
            } else if (high - low == 1) {
                cur = high;
                break;
            } else if (getKey(cur) < key) {
                if (low == cur) {
                    low++;
                } else {
                    low = cur;
                }
            } else { // if(getKey(cur) > key)
                high = cur;
            }
        }

        //Now go to the nearest if there are multiple entries
        for (;(cur < size()) && (key > getKey(cur)); cur++);

        return cur;
    }
    
    // attributes
    // ------------------------------------------------------------------------
    private static Log _log = LogFactory.getLog(BTree.class);
    private int _degree = 1000;
    private int _maxCap = (2 * _degree) - 1;
    private int _counter = 0; //Only used if object is root
    private int _fileId = 0; //The id that will be used for the file that is written
    private IntList _keys = null;
    private IntList _vals = null;
    private IntList _childIds = null;
    private Map _loadedChildren = null;
    private File _idxDir = null;
    private BTree _root = null;
    private String _idxName = null;


    // inner classes
    // ------------------------------------------------------------------------
    
    private static class BTreeValueIterator implements IntIterator {
        
        BTreeValueIterator(BTree node) throws IOException {
            _stack = new StateStack();
            _stack.push(node,false,0);            
        }
        
        BTreeValueIterator(StateStack stack) throws IOException {
            _stack = stack;
        }
        
        public boolean hasNext() {
            while(true) {
                if(_stack.isEmpty()) {
                    return false;
                } else {
                    State state = _stack.peek();
                    if((state.node.isLeaf() || state.visitedChildren) && state.position >= state.node.size()) {
                        _stack.pop();
                    } else {
                        return true;
                    }
                }
            }
        }

        public int next() {
            try {
                while(true) {
                    if(_stack.isEmpty()) {
                        throw new NoSuchElementException();
                    } else {
                        State state = _stack.pop();
                        if(!state.visitedChildren) {
                            state.visitedChildren = true;
                            _stack.push(state);
                            if(state.node.hasChild(state.position)) {
                                _stack.push(state.node.getChild(state.position),false,0);                    
                            }
                        } else if(state.position < state.node.size()) {
                            int value = state.node.getValue(state.position);
                            state.position++;
                            state.visitedChildren = false;
                            _stack.push(state);
                            return value; 
                        } else {
                            // do nothing, I've already popped
                        }
                    }
                }
            } catch(IOException e) {
                // @TODO: do something smarter
                throw new RuntimeException(e.toString());
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private StateStack _stack = null;
    }

    private static class StateStack {
        StateStack() {
        }

        boolean isEmpty() {
            return _nodes.isEmpty();
        }
        
        void push(State state) {
            _nodes.add(state);
        }

        void push(BTree tree, boolean visitedChildren, int position) {
            push(new State(tree,visitedChildren,position));
        }
        
        State pop() {
            return (State)(_nodes.remove(_nodes.size()-1));
        }

        State peek() {
            return (State)_nodes.get(_nodes.size()-1);
        }

        public String toString() {
            return _nodes.toString();
        }
        
        private List _nodes = new ArrayList();
         
    }

    private static class State {
        State(BTree n, boolean visited, int pos) {
            node = n;
            visitedChildren = visited;
            position = pos;
        }
        
        public String toString() {
            return "<" + node.getFileId() + "," + visitedChildren + "," + position + ">";    
        }
        
        BTree node = null;
        boolean visitedChildren = false;
        int position = 0;
    }

}
