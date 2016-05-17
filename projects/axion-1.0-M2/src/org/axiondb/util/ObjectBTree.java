/*
 * $Id: ObjectBTree.java,v 1.14 2003/07/11 17:04:06 rwald Exp $
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.IntListIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// XXX FIX ME XXX: factor out a base parent class?

/**
 * A B-Tree for <code>Object</code>s, based on the implementation described
 * in "Introduction to Algorithms" by Cormen, Leiserson and Rivest (CLR).
 * 
 * (Based on BTree, written by Chuck Burdick and Dave Pekarek Krohn.)
 * 
 * @version $Revision: 1.14 $ $Date: 2003/07/11 17:04:06 $
 * @author Dave Pekarek Krohn
 */
public class ObjectBTree {
    private static Log _log = LogFactory.getLog(ObjectBTree.class);
    private int _degree = 1000;
    private int _maxCap = (2 * _degree) - 1;
    private int _counter = 0; //Only used if object is root
    private int _fileId = 0; //The id that will be used for the file that is written
    private StringBuffer _buf = new StringBuffer(30);
    private ArrayList _keys = null;
    private IntList _vals = null;
    private IntList _childIds = null;
    private Map _loadedChildren = null;
    private File _idxDir = null;
    private ObjectBTree _root = null;
    private String _idxName = null;
    private Comparator _comparator = null;

    // constructors --------------------------------------------------------------------------------

    /**
     * Create or load a new root node
     */
    public ObjectBTree(File idxDir, String idxName, int degree, Comparator comp) throws IOException, ClassNotFoundException {
        this(idxDir, idxName, degree, comp, null);
    }

    /**
     * Create or load a node
     */
    public ObjectBTree(File idxDir, String idxName, int degree, Comparator comp, ObjectBTree root) throws IOException, ClassNotFoundException {
        _idxDir = idxDir;
        _idxName = idxName;
        _degree = degree; // t
        _maxCap = (2 * _degree) - 1;
        _keys = new ArrayList(_maxCap);
        _vals = new ArrayIntList(_maxCap);
        _childIds = new ArrayIntList(_maxCap + 1);
        _loadedChildren = new HashMap();
        _comparator = comp;

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
    public ObjectBTree(File idxDir, String idxName, int degree, Comparator comp, ObjectBTree root, boolean shouldCreate) throws IOException, ClassNotFoundException {
        this(idxDir, idxName, degree, comp, root);
        if (shouldCreate) {
            allocate();
        }
    }

    /**
     * Read in an existing node file
     */
    public ObjectBTree(File idxDir, String idxName, int degree, int fileId, Comparator comp, ObjectBTree root) throws IOException, ClassNotFoundException {
        this(idxDir, idxName, degree, comp, root);
        setFileId(fileId);
        read();
    }

    // public methods --------------------------------------------------------------------------------

    /**
     * Sets the fileId.
     */
    public void allocate() {
        setFileId(getRoot().getCounter());
        getRoot().setCounter(getFileId() + 1);
    }

    /**
     * Loads the counter file if it exists.
     * This sets the counter.  Only the root
     * should ever call this.
     */
    public void loadIdxCtr() throws IOException {
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
    public void saveIdxCtr() throws IOException {
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

    public File getFileById(int fileid) {
        _buf.setLength(0);
        _buf.append(getName());
        _buf.append('.');
        _buf.append(fileid);
        return new File(_idxDir, _buf.toString());
    }

    public File getCounterFile() {
        return new File(_idxDir, _idxName + ".ctr");
    }

    public String getName() {
        return _idxName;
    }

    public ArrayList getKeys() {
        return _keys;
    }

    public void setKeys(ArrayList keys) {
        _keys = keys;
    }

    public IntList getValues() {
        return _vals;
    }

    public void setValues(IntList vals) {
        _vals = vals;
    }

    public int getValue(int index) {
        return _vals.get(index);
    }

    public void setValue(int index, int val) {
        _vals.set(index, val);
    }

    public IntList getChildIds() {
        return _childIds;
    }

    public void setChildIds(IntList childIds) {
        _childIds = childIds;
    }

    public Map getLoadedChildren() {
        return _loadedChildren;
    }

    public int getCounter() {
        if (isRoot()) {
            return _counter;
        } else {
            return getRoot().getCounter();
        }
    }

    public void setCounter(int counter) {
        if (isRoot()) {
            _counter = counter;
        } else {
            getRoot().setCounter(counter);
        }
    }

    public int getFileId() {
        return _fileId;
    }

    public void setFileId(int fileId) {
        _fileId = fileId;
    }

    public ObjectBTree getRoot() {
        return _root;
    }

    public boolean isLeaf() {
        return (getChildIds().size() == 0);
    }

    public boolean isRoot() {
        return (getRoot() == this);
    }

    public int size() {
        return getKeys().size();
    }

    public void insert(Object key, int value) throws IOException, ClassNotFoundException {
        if (size() == _maxCap) {
            // grow and rotate tree
            if (_log.isDebugEnabled()) {
                _log.debug(
                    "Node " + _fileId + " reached max capacity. Splitting and rotating.");
            }

            _log.debug("Creating new child node");
            ObjectBTree child = new ObjectBTree(_idxDir, getName(), _degree, _comparator, _root, true);

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

    public void delete(Object key) throws IOException, ClassNotFoundException {
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
        if ((i < 0 && isNotEqual(getKeys().get(i + 1), key))
            || isNotEqual(getKeys().get(i), key)) {
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

                    mergeChildren(mergeLoc, getKeys().get(mergeLoc));

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
                getKeys().remove(i);
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
                    Object[] keyParam = new Object[1];
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
                    Object[] keyParam = new Object[1];
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

    public void borrowLeft(int borrowLoc) throws IOException, ClassNotFoundException {
        ObjectBTree leftSibling = getChild(borrowLoc - 1);
        ObjectBTree rightSibling = getChild(borrowLoc);

        if (_log.isDebugEnabled()) {
            _log.debug("Doing borrow left at: " + borrowLoc);
            _log.debug("Left sibling: " + leftSibling);
            _log.debug("Right sibling: " + rightSibling);
        }

        //Add the upper key to as the first entry of the right sibling
        rightSibling.getKeys().add(0, getKeys().get(borrowLoc - 1));
        rightSibling.getValues().add(0, getValues().get(borrowLoc - 1));

        //Make the upper node's key the last key from the left sibling
        getKeys().set(
            borrowLoc - 1,
            leftSibling.getKeys().get(leftSibling.getKeys().size() - 1));
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
        leftSibling.getKeys().remove(leftSibling.getKeys().size() - 1);
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

    public void borrowRight(int borrowLoc) throws IOException, ClassNotFoundException {
        ObjectBTree leftSibling = getChild(borrowLoc);
        ObjectBTree rightSibling = getChild(borrowLoc + 1);

        if (_log.isDebugEnabled()) {
            _log.debug("Doing borrow right at: " + borrowLoc);
            _log.debug("Left sibling: " + leftSibling);
            _log.debug("Right sibling: " + rightSibling);
        }

        //Add the upper key to as the last entry of the left sibling
        leftSibling.getKeys().add(getKeys().get(borrowLoc));
        leftSibling.getValues().add(getValues().get(borrowLoc));

        //Make the upper node's key the first key from the right sibling
        getKeys().set(borrowLoc, rightSibling.getKeys().get(0));
        getValues().set(borrowLoc, rightSibling.getValues().get(0));

        //If the siblings aren't leaves, move the first child from the right to be the last on the left
        if (!leftSibling.isLeaf()) {
            leftSibling.addChild(rightSibling.getChild(0));
        }

        //Remove the first entry of the right sibling (now moved to upper node)
        rightSibling.getKeys().remove(0);
        rightSibling.getValues().removeElementAt(0);
        if (!rightSibling.isLeaf()) {
            rightSibling.getChildIds().removeElementAt(0);
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Left sibling after: " + leftSibling);
            _log.debug("Right sibling after: " + rightSibling);
        }
    }

    public void mergeChildren(int mergeLoc, Object key) throws IOException, ClassNotFoundException {
        if (_log.isDebugEnabled()) {
            _log.debug("Merging children at: " + mergeLoc);
        }
        ObjectBTree leftChild = getChild(mergeLoc);
        ObjectBTree rightChild = getChild(mergeLoc + 1);
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
        getKeys().remove(mergeLoc);
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

    public void maybeCollapseTree() throws IOException, ClassNotFoundException {
        if (!isLeaf() && getKeys().size() <= 0) {
            _log.debug("Collapsing tree");
            ObjectBTree nodeToPromote = getChild(0);
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
    public void getLeftMost(Object[] keyParam, int valueParam[])
        throws IOException, ClassNotFoundException {
        if (isLeaf()) {
            keyParam[0] = getKeys().get(0);
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
    public void getRightMost(Object[] keyParam, int valueParam[])
        throws IOException, ClassNotFoundException {
        if (isLeaf()) {
            int max = size() - 1;
            keyParam[0] = getKeys().get(max);
            valueParam[0] = getValues().get(max);
        } else {
            int max = getChildIds().size() - 1;
            getChild(max).getRightMost(keyParam, valueParam);
        }
    }

    public void insertNotfull(Object key, int value)
        throws IOException, ClassNotFoundException {
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
            ObjectBTree child = getChild(i);
            if (child.size() == _maxCap) {
                subdivideChild(i, child);
                if (isGreaterThan(key, getKeys().get(i))) {
                    i++;
                }
            }
            getChild(i).insertNotfull(key, value);
        }
    }
    
    public IntListIterator getAll(Object key) throws IOException, ClassNotFoundException {
        IntListIteratorChain chain = new IntListIteratorChain();
        getAll(key,chain);
        return chain;
    }

    private void getAll(Object key, IntListIteratorChain chain) throws IOException, ClassNotFoundException {
        int start = findNearestKeyAbove(key);
        if(isLeaf()) {
            int stop;
            for(stop = start; stop < size() && isEqual(key,getKeys().get(stop)); stop++) { };
            chain.addIterator(getValues().subList(start,stop).listIterator());
        } else {
            int i = start;
            for(; i < size() && isEqual(key,getKeys().get(i)); i++) {
                getChild(i).getAll(key,chain);
                chain.addIterator(getValues().get(i));
            }
            getChild(i).getAll(key,chain);
        }
    }

    public IntListIterator getAllTo(Object key) throws IOException, ClassNotFoundException {
        IntListIteratorChain chain = new IntListIteratorChain();
        getAllTo(key,chain);
        return chain;
    }

    private void getAllTo(Object key, IntListIteratorChain chain) throws IOException, ClassNotFoundException {
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
                if (i < size() && isGreaterThan(key,getKeys().get(i))) {
                    chain.addIterator(getValues().get(i));
                } else { 
                    break;
                }
            }
        }
    }

    public IntListIterator getAllFrom(Object key) throws IOException, ClassNotFoundException {
        IntListIteratorChain chain = new IntListIteratorChain();
        getAllFrom(key,chain);
        return chain;
    }

    private void getAllFrom(Object key, IntListIteratorChain chain) throws IOException, ClassNotFoundException {       
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

    /**
     * Uses the shortest path to a matching entry and returns its
     * value. Not necessarily the least value, the first entered, or
     * the leftmost.
     */
    public Integer get(Object key) throws IOException, ClassNotFoundException {
        Integer result = null;
        int i = findNearestKeyAbove(key);
        if ((i < size()) && (isEqual(key, getKeys().get(i)))) {
            result = new Integer(getValues().get(i));
        } else if (!isLeaf()) {
            // recurse to children
            result = getChild(i).get(key);
        }
        return result;
    }

    public void subdivideChild(int pivot, ObjectBTree child)
        throws IOException, ClassNotFoundException {
        if (_log.isDebugEnabled()) {
            _log.debug("Parent keys: " + getKeys().toString());
            _log.debug("Child keys: " + child.getKeys().toString());
        }
        int i = 0; // prepare index for loops below

        _log.debug("Create new child to take excess data");
        ObjectBTree fetus = new ObjectBTree(_idxDir, getName(), _degree, _comparator, _root, true);
        addChild(pivot + 1, fetus);

        _log.debug("Transfer (t-1) vals from existing child to new child");
        {
            List sub = child.getKeys().subList(_degree, _maxCap);
            if (_log.isDebugEnabled()) {
                _log.debug("Moving keys " + sub.toString());
            }
            fetus.getKeys().addAll(sub);
        }
        IntList sub = child.getValues().subList(_degree, _maxCap);
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
        Object pivotKey = child.getKeys().get(_degree - 1);
        if (_log.isDebugEnabled()) {
            _log.debug("Pivot key: " + pivotKey);
        }
        int pivotVal = child.getValues().get(_degree - 1);
        getKeys().add(pivot, pivotKey);
        getValues().add(pivot, pivotVal);

        _log.debug("Trim child to new size");
        for (i = (_maxCap - 1); i > (_degree - 2); i--) {
            child.getKeys().remove(i);
            child.getValues().removeElementAt(i);
        }
    }

    /**
     * Writes the node file out.  This is differentiated from save in
     * that it doesn't save the entire tree or the counter file.
     */
    public void write() throws IOException {
        File idxFile = getFileById(getFileId());
        if (idxFile == null) {
            throw new NullPointerException("ObjectBTree must be allocated before writing out to disk");
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
            out.writeObject(getKeys().get(i));
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
     * Saves the tree.  It saves the counter file, writes out the node
     * and then calls save recursively through the tree.
     */
    public void save(File dataDirectory) throws IOException, ClassNotFoundException {
        _idxDir = dataDirectory;
        save();
    }

    public void save() throws IOException, ClassNotFoundException {
        if (isRoot()) {
            saveIdxCtr();
        }
        write();
        for (int i = 0; i < getChildIds().size(); i++) {
            getChild(i).save(_idxDir);
        }
    }

    /**
     * Reads in the node.  This doesn't read in the entire subtree,
     * which happens incrementally as files are needed.
     */
    public void read() throws IOException, ClassNotFoundException {
        File idxFile = getFileById(getFileId());
        if (_log.isDebugEnabled()) {
            _log.debug("Reading in file " + idxFile);
        }
        FileInputStream fin = new FileInputStream(idxFile);
        ObjectInputStream in = new ObjectInputStream(fin);
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            getKeys().add(in.readObject());
            getValues().add(in.readInt());
        }
        size = in.readInt();
        for (int i = 0; i < size; i++) {
            getChildIds().add(in.readInt());
        }
        in.close();
        fin.close();
    }

    public String getChildName(int index) {
        return getChildName(getName(), index);
    }

    public String getChildName(String baseName, int index) {
        _buf.setLength(0);
        _buf.append(baseName);
        _buf.append(".");
        _buf.append(index);
        return _buf.toString();
    }

    public ObjectBTree getChild(int index) throws IOException, ClassNotFoundException {
        if (index >= getChildIds().size()) {
            throw new IOException("Node " + _fileId + " has no child at index " + index);
        } else {
            Integer fileid = new Integer(getChildIds().get(index));
            if (_loadedChildren.get(fileid) == null) {
                _loadedChildren.put(
                    fileid,
                    new ObjectBTree(
                        _idxDir,
                        getName(),
                        _degree,
                        fileid.intValue(),
                        _comparator,
                        _root));
            }
            return (ObjectBTree)_loadedChildren.get(fileid);
        }
    }

    public void addChild(ObjectBTree child) {
        getChildIds().add(child.getFileId());
        _loadedChildren.put(new Integer(child.getFileId()), child);
    }

    public void addChild(int index, ObjectBTree child) {
        getChildIds().add(index, child.getFileId());
        _loadedChildren.put(new Integer(child.getFileId()), child);
    }

    public void addChildrenFrom(ObjectBTree tree) {
        addChildren(tree.getChildIds(), tree.getLoadedChildren());
    }

    public void addChildren(IntList childIds, Map children) {
        IntIterator iter = childIds.iterator();
        while (iter.hasNext()) {
            int fileid = iter.next();
            addChild((ObjectBTree)children.get(new Integer(fileid)));
        }
    }

    public boolean isValid() throws IOException, ClassNotFoundException {
        return isValid(true);
    }

    public boolean isValid(boolean isRoot) throws IOException, ClassNotFoundException {
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

    public void replaceId(Object key, int oldId, int newId)
        throws ClassNotFoundException, IOException {
        int i = findNearestKeyAbove(key);
        boolean valSet = false;
        while ((i < size()) && isEqual(key, getKeys().get(i))) {
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
        buf.append("{");
        buf.append(_fileId);
        buf.append(":");
        buf.append("[");
        for (int i = 0; i < size() + 1; i++) {
            if (!isLeaf()) {
                try {
                    buf.append(getChild(i).toString());
                } catch (IOException e) {
                    _log.error("Cannot retrieve child", e);
                } catch (ClassNotFoundException e) {
                    _log.error("Cannot retrieve child", e);
                }
                if (i < size()) {
                    buf.append(",");
                }
            }
            if (i < size()) {
                buf.append(getKeys().get(i));
                buf.append("/");
                buf.append(getValues().get(i));
                if (!isLeaf() || i < (size() - 1)) {
                    buf.append(",");
                }
            }
        }
        buf.append("]");
        buf.append("}");
        return buf.toString();
    }

    // private methods -------------------------------------------------------------

    private int compare(Object x, Object y) {
        return _comparator.compare(x,y);
    }

    private boolean isEqual(Object x, Object y) {
        return compare(x,y) == 0;
    }

    private boolean isNotEqual(Object x, Object y) {
        return compare(x,y) != 0;
    }

    private boolean isGreaterThan(Object x, Object y) {
        return compare(x,y) > 0;
    }

    private boolean isGreaterThanOrEqual(Object x, Object y) {
        return compare(x,y) >= 0;
    }

    private boolean isLessThan(Object x, Object y) {
        return compare(x,y) < 0;
    }

    private int findNearestKeyBelow(Object key) {
        int high = size();
        int low = 0;
        int cur = 0;

        //Short circuit
        if (size() == 0) {
            return -1;
        } else if (compare(getKeys().get(size() - 1), key) <= 0) {
            return size() - 1;
        } else if (isGreaterThan(getKeys().get(0), key)) {
            return -1;
        }

        while (low < high) {
            cur = (high + low) / 2;
            int comp = compare(key, getKeys().get(cur));
            if (0 == comp) {
                //We found it now move to the last
                for (;(cur < size()) && isEqual(key, getKeys().get(cur)); cur++);
                break;
            } else if (comp > 0) {
                if (low == cur) {
                    low++;
                } else {
                    low = cur;
                }
            } else { // comp < 0
                high = cur;
            }
        }

        //Now go to the nearest if there are multiple entries
        for (;(cur >= 0) && (isLessThan(key, getKeys().get(cur))); cur--);

        return cur;
    }

    private int findNearestKeyAbove(Object key) {
        int high = size();
        int low = 0;
        int cur = 0;

        //Short circuit
        if (size() == 0) {
            return 0;
        } else if (isLessThan(getKeys().get(size() - 1), key)) {
            return size();
        } else if (isGreaterThanOrEqual(getKeys().get(0), key)) {
            return 0;
        }

        while (low < high) {
            cur = (high + low) / 2;
            int comp = compare(key, getKeys().get(cur));
            if (high == low) {
                cur = low;
                break;
            } else if (comp == 0) {
                //We found it now move to the first
                for (;(cur > 0) && (isEqual(key, getKeys().get(cur))); cur--);
                break;
            } else if (high - low == 1) {
                cur = high;
                break;
            } else if (comp > 0) {
                if (low == cur) {
                    low++;
                } else {
                    low = cur;
                }
            } else { // comp < 0
                high = cur;
            }
        }

        //Now go to the nearest if there are multiple entries
        for (;(cur < size()) && isGreaterThan(key, getKeys().get(cur)); cur++) {};

        return cur;
    }
}
