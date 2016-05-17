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
package org.aspectj.org.eclipse.jdt.internal.codeassist.complete;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnMarkerAnnotationName extends MarkerAnnotation {
	public CompletionOnMarkerAnnotationName(TypeReference type, int sourceStart){
		super(type, sourceStart);
	}
	
	public TypeBinding resolveType(BlockScope scope) {
		if(type instanceof QualifiedTypeReference) {
			QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference) type;
			Binding binding = scope.parent.getTypeOrPackage(qualifiedTypeReference.tokens); // step up from the ClassScope
			if (!binding.isValidBinding()) {
				scope.problemReporter().invalidType(this, (TypeBinding) binding);
				throw new CompletionNodeFound();
			}
			throw new CompletionNodeFound(this, binding, scope);
		}
		throw new CompletionNodeFound(this, null, scope);
	}
}
