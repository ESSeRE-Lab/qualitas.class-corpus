/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver;

import org.aspectj.weaver.ast.Var;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * Encapsulates operations that a world will need to support if it is actually going to modify bytecode rather than just match
 * against it. {@see BcelWeavingSupport}
 * 
 * @author Andy Clement
 */
public interface IWeavingSupport {

	public Advice createAdviceMunger(AjAttribute.AdviceAttribute attribute, Pointcut pointcut, Member signature,
			ResolvedType concreteAspect);

	public abstract ConcreteTypeMunger makeCflowStackFieldAdder(ResolvedMember cflowField);

	public abstract ConcreteTypeMunger makeCflowCounterFieldAdder(ResolvedMember cflowField);

	/**
	 * Register a munger for perclause @AJ aspect so that we add aspectOf(..) to them as needed
	 * 
	 * @see org.aspectj.weaver.bcel.BcelWorld#makePerClauseAspect(ResolvedType, org.aspectj.weaver.patterns.PerClause.Kind)
	 */
	public abstract ConcreteTypeMunger makePerClauseAspect(ResolvedType aspect, PerClause.Kind kind);

	public abstract ConcreteTypeMunger concreteTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType);

	public ConcreteTypeMunger createAccessForInlineMunger(ResolvedType inAspect);

	public Var makeCflowAccessVar(ResolvedType formalType, Member cflowField, int arrayIndex);
}
