/* *******************************************************************
 * Copyright (c) 2006-2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.Set;

/**
 * Simple representation of an annotation that the weaver can work with.
 * 
 * @author AndyClement
 */
public interface AnnotationAJ {

	public static final AnnotationAJ[] EMPTY_ARRAY = new AnnotationAJ[0];

	/**
	 * @return the signature for the annotation type, eg. Lcom/foo/MyAnno;
	 */
	public String getTypeSignature();

	/**
	 * @return the type name for the annotation, eg. com.foo.MyAnno
	 */
	public String getTypeName();

	/**
	 * @return the type of the annotation
	 */
	public ResolvedType getType();

	/**
	 * return true if this annotation can target an annotation type
	 */
	public boolean allowedOnAnnotationType();

	/**
	 * @return true if this annotation can be put on a field
	 */
	public boolean allowedOnField();

	/**
	 * @return true if this annotation can target a 'regular' type. A 'regular' type is enum/class/interface - it is *not*
	 *         annotation.
	 */
	public boolean allowedOnRegularType();

	/**
	 * @return for the @target annotation, this will return a set of the element-types it can be applied to. For other annotations ,
	 *         it returns the empty set.
	 */
	public Set getTargets();

	/**
	 * @param name the name of the value
	 * @return true if there is a value with that name
	 */
	public boolean hasNamedValue(String name);

	/**
	 * @param name the name of the annotation field
	 * @param value the value of the annotation field
	 * @return true if there is a value with the specified name and value
	 */
	public boolean hasNameValuePair(String name, String value);

	/**
	 * @return String representation of the valid targets for this annotation, eg. "{TYPE,FIELD}"
	 */
	public String getValidTargets();

	/**
	 * @return String form of the annotation and any values, eg. @Foo(a=b,c=d)
	 */
	public String stringify();

	/**
	 * @return true if this annotation is marked with @target
	 */
	public boolean specifiesTarget();

	/**
	 * @return true if the annotation is marked for runtime visibility
	 */
	public boolean isRuntimeVisible();

	/**
	 * Determine the string representation of the value of a field. For example in @SuppressAjWarnings({"adviceDidNotMatch"}) the
	 * return value for getStringFormOfValue("value") would be "[adviceDidNotMatch]".
	 * 
	 * @param name the name of the annotation field being looked up
	 * @return string representation of the value of that field, may be null if no such field set
	 */
	public String getStringFormOfValue(String name);

}
