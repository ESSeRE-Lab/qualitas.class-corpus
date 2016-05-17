/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.eval;

import org.eclipse.core.runtime.IProgressMonitor;
import org.aspectj.org.eclipse.jdt.core.JavaModelException;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.aspectj.org.eclipse.jdt.internal.core.util.Util;

/**
 * The skeleton of the class 'org.aspectj.org.eclipse.jdt.internal.eval.target.CodeSnippet'
 * used at compile time. Note that the method run() is declared to
 * throw Throwable so that the user can write a code snipet that
 * throws checked exceptio without having to catch those.
 */
public class CodeSnippetSkeleton implements IBinaryType, EvaluationConstants {
	IBinaryMethod[] methods = new IBinaryMethod[] {
		new BinaryMethodSkeleton(
			"<init>".toCharArray(), //$NON-NLS-1$
			"()V".toCharArray(), //$NON-NLS-1$
			new char[][] {},
			true
		),
		new BinaryMethodSkeleton(
			"run".toCharArray(), //$NON-NLS-1$
			"()V".toCharArray(), //$NON-NLS-1$
			new char[][] {"java/lang/Throwable".toCharArray()}, //$NON-NLS-1$
			false
		),
		new BinaryMethodSkeleton(
			"setResult".toCharArray(), //$NON-NLS-1$
			"(Ljava/lang/Object;Ljava/lang/Class;)V".toCharArray(), //$NON-NLS-1$
			new char[][] {},
			false
		)
	};

	public static class BinaryMethodSkeleton implements IBinaryMethod {
		char[][] exceptionTypeNames;
		char[] methodDescriptor;
		char[] selector;
		boolean isConstructor;
		
		public BinaryMethodSkeleton(char[] selector, char[] methodDescriptor, char[][] exceptionTypeNames, boolean isConstructor) {
			this.selector = selector;
			this.methodDescriptor = methodDescriptor;
			this.exceptionTypeNames = exceptionTypeNames;
			this.isConstructor = isConstructor;
		}
		public char[][] getExceptionTypeNames() {
			return this.exceptionTypeNames;
		}
		public char[] getMethodDescriptor() {
			return this.methodDescriptor;
		}
		public int getModifiers() {
			return ClassFileConstants.AccPublic;
		}
		public char[] getSelector() {
			return this.selector;
		}
		public boolean isClinit() {
			return false;
		}
		public boolean isConstructor() {
			return this.isConstructor;
		}
		public char[][] getArgumentNames() {
			return null;
		}
		public char[] getGenericSignature() {
			return null;
		}
		public long getTagBits() {
			return 0;
		}
		public IBinaryAnnotation[] getAnnotations() {
			return null;
		}
		public IBinaryAnnotation[] getParameterAnnotations(int index) {
			return null;
		}
		public Object getDefaultValue() {
			return null;
		}
}
	
/**
 * CodeSnippetSkeleton constructor comment.
 */
public CodeSnippetSkeleton() {
	super();
}
public char[] getEnclosingTypeName() {
	return null;
}
public IBinaryField[] getFields() {
	return null;
}
/**
 * @see org.aspectj.org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 */
public char[] getFileName() {
	return CharOperation.concat(CODE_SNIPPET_NAME, Util.defaultJavaExtension().toCharArray());
}
public char[] getGenericSignature() {
	return null;
}
public char[][] getInterfaceNames() {
	return null;
}
public IBinaryNestedType[] getMemberTypes() {
	return null;
}
public IBinaryMethod[] getMethods() {
	return this.methods;
}
public int getModifiers() {
	return ClassFileConstants.AccPublic;
}
public char[] getName() {
	return CODE_SNIPPET_NAME;
}
public char[] getSourceName() {
	return ROOT_CLASS_NAME;
}
public char[] getSuperclassName() {
	return null;
}
public boolean isAnonymous() {
	return false;
}
public boolean isBinaryType() {
	return true;
}
public boolean isLocal() {
	return false;
}
public boolean isMember() {
	return false;
}
public char[] sourceFileName() {
	return null;
}
public IBinaryAnnotation[] getAnnotations() {
	return null;
}
public long getTagBits() {
	return 0;
}
public String getJavadocContents(IProgressMonitor monitor, String defaultEncoding) throws JavaModelException {
	return null;
}
public String getJavadocContents() {
	return null;
}
public String getURLContents(String docUrlValue, String defaultEncoding) {
	return null;
}
}
