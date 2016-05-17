/*

   Derby - Class org.apache.derby.impl.sql.compile.QueryTreeNodeVector

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package	org.apache.derby.impl.sql.compile;

import org.apache.derby.iapi.services.sanity.SanityManager;

import org.apache.derby.iapi.sql.compile.Visitor;
import org.apache.derby.iapi.sql.compile.Visitable;
import org.apache.derby.iapi.error.StandardException;

import java.util.Enumeration;
import java.util.Vector;

/**
 * QueryTreeNodeVector is the root class for all lists of query tree nodes.
 * It provides a wrapper for java.util.Vector. All
 * lists of query tree nodes inherit from QueryTreeNodeVector.
 *
 */

abstract class QueryTreeNodeVector extends QueryTreeNode
{
	private Vector			v = new Vector();

	public final int size()
	{
		return v.size();
	}

	QueryTreeNode elementAt(int index)
	{
		return (QueryTreeNode) v.elementAt(index);
	}

	final void addElement(QueryTreeNode qt)
	{
		v.addElement(qt);
	}

	final void removeElementAt(int index)
	{
		v.removeElementAt(index);
	}

	final void removeElement(QueryTreeNode qt)
	{
		v.removeElement(qt);
	}

	final Object remove(int index)
	{
		return((QueryTreeNode) (v.remove(index)));
	}

	final int indexOf(QueryTreeNode qt)
	{
		return v.indexOf(qt);
	}

	final void setElementAt(QueryTreeNode qt, int index)
	{
		v.setElementAt(qt, index);
	}

	void destructiveAppend(QueryTreeNodeVector qtnv)
	{
		nondestructiveAppend(qtnv);
		qtnv.removeAllElements();
	}

	void nondestructiveAppend(QueryTreeNodeVector qtnv)
	{
		int qtnvSize = qtnv.size();
		for (int index = 0; index < qtnvSize; index++)
		{
			v.addElement(qtnv.elementAt(index));
		}
	}

	final void removeAllElements()
	{
		v.removeAllElements();
	}

	final void insertElementAt(QueryTreeNode qt, int index)
	{
		v.insertElementAt(qt, index);
	}


	/**
	 * Prints the sub-nodes of this object.  See QueryTreeNode.java for
	 * how tree printing is supposed to work.
	 * @param depth		The depth to indent the sub-nodes
	 */
	public void printSubNodes(int depth) {
		if (SanityManager.DEBUG) {
			for (int index = 0; index < size(); index++) {
				debugPrint(formatNodeString("[" + index + "]:", depth));
				QueryTreeNode elt = (QueryTreeNode)elementAt(index);
				elt.treePrint(depth);
			}
		}
	}


	/**
	 * Accept the visitor for all visitable children of this node.
	 * 
	 * @param v the visitor
	 *
	 * @exception StandardException on error
	 */
	void acceptChildren(Visitor v)
		throws StandardException
	{
		super.acceptChildren(v);

		int size = size();
		for (int index = 0; index < size; index++)
		{
			setElementAt((QueryTreeNode)((QueryTreeNode) elementAt(index)).accept(v), index);
		}
	}
}
