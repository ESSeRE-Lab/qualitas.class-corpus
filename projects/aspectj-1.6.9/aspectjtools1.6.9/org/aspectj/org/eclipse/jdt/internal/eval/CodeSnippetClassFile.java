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

import org.aspectj.org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.StackMapFrameCodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

public class CodeSnippetClassFile extends ClassFile {
/**
 * CodeSnippetClassFile constructor comment.
 * @param aType org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding
 * @param enclosingClassFile org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile
 * @param creatingProblemType boolean
 */
public CodeSnippetClassFile(
	org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding aType,
	org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile enclosingClassFile,
	boolean creatingProblemType) {
	/**
	 * INTERNAL USE-ONLY
	 * This methods creates a new instance of the receiver.
	 *
	 * @param aType org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding
	 * @param enclosingClassFile org.aspectj.org.eclipse.jdt.internal.compiler.codegen.ClassFile
	 * @param creatingProblemType <CODE>boolean</CODE>
	 */
	this.referenceBinding = aType;
	initByteArrays();
	// generate the magic numbers inside the header
	this.header[this.headerOffset++] = (byte) (0xCAFEBABEL >> 24);
	this.header[this.headerOffset++] = (byte) (0xCAFEBABEL >> 16);
	this.header[this.headerOffset++] = (byte) (0xCAFEBABEL >> 8);
	this.header[this.headerOffset++] = (byte) (0xCAFEBABEL >> 0);

	this.targetJDK = this.referenceBinding.scope.compilerOptions().targetJDK;
	this.header[this.headerOffset++] = (byte) (targetJDK >> 8); // minor high
	this.header[this.headerOffset++] = (byte) (targetJDK >> 0); // minor low
	this.header[this.headerOffset++] = (byte) (targetJDK >> 24); // major high
	this.header[this.headerOffset++] = (byte) (targetJDK >> 16); // major low

	this.constantPoolOffset = this.headerOffset;
	this.headerOffset += 2;
	this.constantPool = new ConstantPool(this);
	int accessFlags = aType.getAccessFlags();
	
	if (!aType.isInterface()) { // class or enum
		accessFlags |= ClassFileConstants.AccSuper;
	}
	if (aType.isNestedType()) {
		if (aType.isStatic()) {
			// clear Acc_Static
			accessFlags &= ~ClassFileConstants.AccStatic;
		}
		if (aType.isPrivate()) {
			// clear Acc_Private and Acc_Public
			accessFlags &= ~(ClassFileConstants.AccPrivate | ClassFileConstants.AccPublic);
		}
		if (aType.isProtected()) {
			// clear Acc_Protected and set Acc_Public
			accessFlags &= ~ClassFileConstants.AccProtected;
			accessFlags |= ClassFileConstants.AccPublic;
		}
	}
	// clear Acc_Strictfp
	accessFlags &= ~ClassFileConstants.AccStrictfp;

	this.enclosingClassFile = enclosingClassFile;
	// now we continue to generate the bytes inside the contents array
	this.contents[this.contentsOffset++] = (byte) (accessFlags >> 8);
	this.contents[this.contentsOffset++] = (byte) accessFlags;
	int classNameIndex = this.constantPool.literalIndexForType(aType);
	this.contents[this.contentsOffset++] = (byte) (classNameIndex >> 8);
	this.contents[this.contentsOffset++] = (byte) classNameIndex;
	int superclassNameIndex;
	if (aType.isInterface()) {
		superclassNameIndex = this.constantPool.literalIndexForType(ConstantPool.JavaLangObjectConstantPoolName);
	} else {
		superclassNameIndex =
			(aType.superclass == null ? 0 : this.constantPool.literalIndexForType(aType.superclass));
	}
	this.contents[this.contentsOffset++] = (byte) (superclassNameIndex >> 8);
	this.contents[this.contentsOffset++] = (byte) superclassNameIndex;
	ReferenceBinding[] superInterfacesBinding = aType.superInterfaces();
	int interfacesCount = superInterfacesBinding.length;
	this.contents[this.contentsOffset++] = (byte) (interfacesCount >> 8);
	this.contents[this.contentsOffset++] = (byte) interfacesCount;
	for (int i = 0; i < interfacesCount; i++) {
		int interfaceIndex = this.constantPool.literalIndexForType(superInterfacesBinding[i]);
		this.contents[this.contentsOffset++] = (byte) (interfaceIndex >> 8);
		this.contents[this.contentsOffset++] = (byte) interfaceIndex;
	}
	this.produceAttributes = this.referenceBinding.scope.compilerOptions().produceDebugAttributes;
	this.creatingProblemType = creatingProblemType;
	if (this.targetJDK >= ClassFileConstants.JDK1_6) {
		this.codeStream = new StackMapFrameCodeStream(this);
		this.produceAttributes |= ClassFileConstants.ATTR_STACK_MAP;
	} else {
		this.codeStream = new CodeStream(this);
	}
	// retrieve the enclosing one guaranteed to be the one matching the propagated flow info
	// 1FF9ZBU: LFCOM:ALL - Local variable attributes busted (Sanity check)
	if (this.enclosingClassFile == null) {
		this.codeStream.maxFieldCount = aType.scope.referenceType().maxFieldCount;
	} else {
		ClassFile outermostClassFile = this.outerMostEnclosingClassFile();
		this.codeStream.maxFieldCount = outermostClassFile.codeStream.maxFieldCount;
	}
}
/**
 * INTERNAL USE-ONLY
 * Request the creation of a ClassFile compatible representation of a problematic type
 *
 * @param typeDeclaration org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration
 * @param unitResult org.aspectj.org.eclipse.jdt.internal.compiler.CompilationUnitResult
 */
public static void createProblemType(TypeDeclaration typeDeclaration, CompilationResult unitResult) {
	SourceTypeBinding typeBinding = typeDeclaration.binding;
	ClassFile classFile = new CodeSnippetClassFile(typeBinding, null, true);

	// inner attributes
	if (typeBinding.isNestedType()) {
		classFile.recordInnerClasses(typeBinding);
	}

	// add its fields
	FieldBinding[] fields = typeBinding.fields();
	if ((fields != null) && (fields != Binding.NO_FIELDS)) {
		classFile.addFieldInfos();
	} else {
		// we have to set the number of fields to be equals to 0
		classFile.contents[classFile.contentsOffset++] = 0;
		classFile.contents[classFile.contentsOffset++] = 0;
	}
	// leave some space for the methodCount
	classFile.setForMethodInfos();
	// add its user defined methods
	int problemsLength;
	CategorizedProblem[] problems = unitResult.getErrors();
	if (problems == null) {
		problems = new CategorizedProblem[0];
	}
	CategorizedProblem[] problemsCopy = new CategorizedProblem[problemsLength = problems.length];
	System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
	AbstractMethodDeclaration[] methodDecls = typeDeclaration.methods;
	if (methodDecls != null) {
		if (typeBinding.isInterface()) {
			// we cannot create problem methods for an interface. So we have to generate a clinit
			// which should contain all the problem
			classFile.addProblemClinit(problemsCopy);
			for (int i = 0, length = methodDecls.length; i < length; i++) {
				AbstractMethodDeclaration methodDecl = methodDecls[i];
				MethodBinding method = methodDecl.binding;
				if (method == null || method.isConstructor()) continue;
				classFile.addAbstractMethod(methodDecl, method);
			}		
		} else {
			for (int i = 0, length = methodDecls.length; i < length; i++) {
				AbstractMethodDeclaration methodDecl = methodDecls[i];
				MethodBinding method = methodDecl.binding;
				if (method == null) continue;
				if (method.isConstructor()) {
					classFile.addProblemConstructor(methodDecl, method, problemsCopy);
				} else {
					classFile.addProblemMethod(methodDecl, method, problemsCopy);
				}
			}
		}
		// add abstract methods
		classFile.addDefaultAbstractMethods();
	}
	// propagate generation of (problem) member types
	if (typeDeclaration.memberTypes != null) {
		for (int i = 0, max = typeDeclaration.memberTypes.length; i < max; i++) {
			TypeDeclaration memberType = typeDeclaration.memberTypes[i];
			if (memberType.binding != null) {
				ClassFile.createProblemType(memberType, unitResult);
			}
		}
	}
	classFile.addAttributes();
	unitResult.record(typeBinding.constantPoolName(), classFile);
}
}
