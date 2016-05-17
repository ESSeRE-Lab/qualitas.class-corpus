/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.core.jdom;

import java.util.Enumeration;

import org.aspectj.org.eclipse.jdt.core.jdom.*;

/**
 * SiblingEnumeration provides an enumeration on a linked list
 * of sibling DOM nodes.
 *
 * @see java.util.Enumeration
 * @deprecated The JDOM was made obsolete by the addition in 2.0 of the more
 * powerful, fine-grained DOM/AST API found in the 
 * org.aspectj.org.eclipse.jdt.core.dom package.
 */
/* package */ class SiblingEnumeration implements Enumeration {

	/**
	 * The current location in the linked list
	 * of DOM nodes.
	 */
	protected IDOMNode fCurrentElement;
/**
 * Creates an enumeration of silbings starting at the given node.
 * If the given node is <code>null</code> the enumeration is empty.
 */
SiblingEnumeration(IDOMNode child) {
	fCurrentElement= child;
}
/**
 * @see java.util.Enumeration#hasMoreElements()
 */
public boolean hasMoreElements() {
	return fCurrentElement != null;
}
/**
 * @see java.util.Enumeration#nextElement()
 */
public Object nextElement() {
	IDOMNode curr=  fCurrentElement;
	if (curr != null) {
		fCurrentElement= fCurrentElement.getNextNode();
	}
	return curr;
}
}
