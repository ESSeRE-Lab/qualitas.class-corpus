/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on 2004-03-11
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectj.org.eclipse.jdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.*;

public class MarkerAnnotation extends Annotation {
	
	public MarkerAnnotation(TypeReference type, int sourceStart) {
		this.type = type;
		this.sourceStart = sourceStart;
		this.sourceEnd = type.sourceEnd;
	}
	
	/**
	 * @see org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation#memberValuePairs()
	 */
	public MemberValuePair[] memberValuePairs() {
		return NoValuePairs;
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
