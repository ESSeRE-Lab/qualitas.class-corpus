/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.core;


/**
 * Represents a type parameter defined by a type of a method
 * in a compilation unit or a class file.
 * <p>
 * Type parameters are obtained using {@link IType#getTypeParameter(String)} and
 * {@link IMethod#getTypeParameter(String)}.
 * </p><p>
 * Note that type parameters are not children of their declaring type or method. To get a list
 * of the type parameters use {@link IType#getTypeParameters()} for a type and use
 * {@link IMethod#getTypeParameters()} for a method.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @since 3.1
 */
public interface ITypeParameter extends IJavaElement, ISourceReference {

	/**
	 * Returns the names of the class and interface bounds of this type parameter. Returns an empty
	 * array if this type parameter has no bounds. A bound name is the name as it appears in the
	 * source (without the <code>extends</code> keyword) if the type parameter comes from a
	 * compilation unit. It is the dot-separated fully qualified name of the bound if the type
	 * parameter comes from a class file.
	 * 
	 * @return the names of the bounds
	 * @throws JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	String[] getBounds() throws JavaModelException;
	
	/**
	 * Returns the declaring member of this type parameter. This can be either an <code>IType</code>
	 * or an <code>IMethod</code>.
	 * <p>
	 * This is a handle-only method.
	 * </p>
	 * 
	 * @return the declaring member of this type parameter.
	 */
	IMember getDeclaringMember();
	
	/**
	 * Returns the source range of this type parameter's name,
	 * or <code>null</code> if this type parameter does not have
	 * associated source code (for example, in a binary type).
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 * @return the source range of this type parameter's name,
	 * or <code>null</code> if this type parameter does not have
	 * associated source code (for example, in a binary type)
	 */
	ISourceRange getNameRange() throws JavaModelException;
}
