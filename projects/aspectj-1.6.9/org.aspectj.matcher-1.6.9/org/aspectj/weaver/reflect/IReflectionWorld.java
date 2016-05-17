/* *******************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *    Ron Bodkin     initial implementation 
 * ******************************************************************/
 package org.aspectj.weaver.reflect;

import org.aspectj.weaver.ResolvedType;

public interface IReflectionWorld {
	public AnnotationFinder getAnnotationFinder();
	public ResolvedType resolve(Class aClass);
}
