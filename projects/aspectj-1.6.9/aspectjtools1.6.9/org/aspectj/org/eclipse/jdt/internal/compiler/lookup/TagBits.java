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
package org.aspectj.org.eclipse.jdt.internal.compiler.lookup;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;

public interface TagBits {
    
	// Tag bits in the tagBits int of every TypeBinding
	long IsArrayType = ASTNode.Bit1;
	long IsBaseType = ASTNode.Bit2;
	long IsNestedType = ASTNode.Bit3;
	long IsMemberType = ASTNode.Bit4;
	long MemberTypeMask = IsNestedType | IsMemberType;
	long IsLocalType = ASTNode.Bit5;
	long LocalTypeMask = IsNestedType | IsLocalType;
	long IsAnonymousType = ASTNode.Bit6;
	long AnonymousTypeMask = LocalTypeMask | IsAnonymousType;
	long IsBinaryBinding = ASTNode.Bit7;
	
	long HasInconsistentHierarchy = ASTNode.Bit8; // for binary type binding only
	
	// for the type cycle hierarchy check used by ClassScope
	long BeginHierarchyCheck = ASTNode.Bit9;  // type
	long EndHierarchyCheck = ASTNode.Bit10; // type
	long ContainsNestedTypesInSignature = ASTNode.Bit10; // method
	long HasParameterAnnotations = ASTNode.Bit11; // method
	
	// test bit to see if default abstract methods were computed
	long KnowsDefaultAbstractMethods = ASTNode.Bit11; // type

	long IsArgument = ASTNode.Bit11; // local
	long ClearPrivateModifier = ASTNode.Bit11; // constructor binding

	// test bits to see if parts of binary types are faulted
	long AreFieldsSorted = ASTNode.Bit13;
	long AreFieldsComplete = ASTNode.Bit14; // sorted and all resolved
	long AreMethodsSorted = ASTNode.Bit15;
	long AreMethodsComplete = ASTNode.Bit16; // sorted and all resolved

	// test bit to avoid asking a type for a member type (includes inherited member types)
	long HasNoMemberTypes = ASTNode.Bit17;

	// test bit to identify if the type's hierarchy is inconsistent
	long HierarchyHasProblems = ASTNode.Bit18;

	// test bit to identify if the type's type variables have been connected
	long TypeVariablesAreConnected = ASTNode.Bit19;

	// set for parameterized type with successfull bound check
	long PassedBoundCheck = ASTNode.Bit23;
	
	// set for parameterized type NOT of the form X<?,?>
	long IsBoundParameterizedType = ASTNode.Bit24; 
	
	// used by BinaryTypeBinding
	long HasUnresolvedTypeVariables = ASTNode.Bit25;
	long HasUnresolvedSuperclass = ASTNode.Bit26;
	long HasUnresolvedSuperinterfaces = ASTNode.Bit27;
	long HasUnresolvedEnclosingType = ASTNode.Bit28;
	long HasUnresolvedMemberTypes = ASTNode.Bit29;

	long HasTypeVariable = ASTNode.Bit30; // set either for type variables (direct) or parameterized types indirectly referencing type variables
	long HasDirectWildcard = ASTNode.Bit31; // set for parameterized types directly referencing wildcards
	
	// for the annotation cycle hierarchy check used by ClassScope
	long BeginAnnotationCheck = ASTNode.Bit32L;
	long EndAnnotationCheck = ASTNode.Bit33L;
	
	// standard annotations
	// 9-bits for targets
	long AnnotationResolved = ASTNode.Bit34L;
	long DeprecatedAnnotationResolved = ASTNode.Bit35L;
	long AnnotationTarget = ASTNode.Bit36L; // @Target({}) only sets this bit
	long AnnotationForType = ASTNode.Bit37L;
	long AnnotationForField = ASTNode.Bit38L;
	long AnnotationForMethod = ASTNode.Bit39L;
	long AnnotationForParameter = ASTNode.Bit40L;
	long AnnotationForConstructor = ASTNode.Bit41L;
	long AnnotationForLocalVariable = ASTNode.Bit42L;
	long AnnotationForAnnotationType = ASTNode.Bit43L;
	long AnnotationForPackage = ASTNode.Bit44L;
	long AnnotationTargetMASK = AnnotationTarget
				| AnnotationForType | AnnotationForField
				| AnnotationForMethod | AnnotationForParameter
				| AnnotationForConstructor | AnnotationForLocalVariable
				| AnnotationForAnnotationType | AnnotationForPackage;
	// 2-bits for retention (should check (tagBits & RetentionMask) == RuntimeRetention
	long AnnotationSourceRetention = ASTNode.Bit45L;
	long AnnotationClassRetention = ASTNode.Bit46L;
	long AnnotationRuntimeRetention = AnnotationSourceRetention | AnnotationClassRetention;
	long AnnotationRetentionMASK = AnnotationSourceRetention | AnnotationClassRetention | AnnotationRuntimeRetention;
	// marker annotations
	long AnnotationDeprecated = ASTNode.Bit47L;
	long AnnotationDocumented = ASTNode.Bit48L;
	long AnnotationInherited = ASTNode.Bit49L;
	long AnnotationOverride = ASTNode.Bit50L;
	long AnnotationSuppressWarnings = ASTNode.Bit51L;
	long AllStandardAnnotationsMask = AnnotationTargetMASK | AnnotationRetentionMASK | AnnotationDeprecated | AnnotationDocumented | AnnotationInherited |  AnnotationOverride | AnnotationSuppressWarnings;
	
	long DefaultValueResolved = ASTNode.Bit52L;
	
	// set when type contains non-private constructor(s)
	long HasNonPrivateConstructor = ASTNode.Bit53L;
}
