/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.core;

import org.aspectj.org.eclipse.jdt.internal.core.util.KeyKind;
import org.aspectj.org.eclipse.jdt.internal.core.util.KeyToSignature;

/**
 * Utility class to decode or create a binding key.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * 
 * @see org.aspectj.org.eclipse.jdt.core.dom.IBinding#getKey()
 * @since 3.1
 */
public final class BindingKey {
	
	private String key;
	
	/**
	 * Creates a new binding key.
	 * 
	 * @param key the key to decode
	 */
	public BindingKey(String key) {
		this.key = key;
	}
	
	/**
	 * Creates a new array type binding key from the given type binding key and the given array dimension.
	 * <p>
	 * For example:
	 * <pre>
	 * <code>
	 * createArrayTypeBindingKey("Ljava/lang/Object;", 1) -> "[Ljava/lang/Object;"
	 * createArrayTypeBindingKey("I", 2) -> "[[I"
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * @param typeKey the binding key of the given type
	 * @param arrayDimension the given array dimension
	 * @return a new array type binding key
	 */
	public static String createArrayTypeBindingKey(String typeKey, int arrayDimension) {
		// Note this implementation is heavily dependent on ArrayTypeBinding#computeUniqueKey() 
		StringBuffer buffer = new StringBuffer();
		while (arrayDimension-- > 0)
			buffer.append('[');
		buffer.append(typeKey);
		return buffer.toString();
	}
	
	/**
	 * Creates a new parameterized type binding key from the given generic type binding key and the given argument type binding keys.
	 * If the argument type keys array is empty, then a raw type binding key is created.
	 * <p>
	 * For example:
	 * <pre>
	 * <code>
	 * createParameterizedTypeBindingKey(
	 *     "Ljava/util/Map&lt;TK;TV;&gt;;", 
	 *     new String[] {"Ljava/lang/String;", "Ljava/lang/Object;"}) -&gt; 
	 *       "Ljava/util/Map&lt;Ljava/lang/String;Ljava/lang/Object;&gt;;"
	 * createParameterizedTypeBindingKey(
	 *     "Ljava/util/List&lt;TE;&gt;;", new String[] {}) -&gt; 
	 *       "Ljava/util/List&lt;&gt;;"
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * @param genericTypeKey the binding key of the generic type
	 * @param argumentTypeKeys the possibly empty list of binding keys of argument types
	 * @return a new parameterized type binding key
	 */
	public static String createParameterizedTypeBindingKey(String genericTypeKey, String[] argumentTypeKeys) {
		// Note this implementation is heavily dependent on ParameterizedTypeBinding#computeUniqueKey() and its subclasses
		StringBuffer buffer = new StringBuffer();
		buffer.append(Signature.getTypeErasure(genericTypeKey));
		buffer.insert(buffer.length()-1, '<');
		for (int i = 0, length = argumentTypeKeys.length; i < length; i++) {
			String argumentTypeKey = argumentTypeKeys[i];
			buffer.insert(buffer.length()-1, argumentTypeKey);
		}
		buffer.insert(buffer.length()-1, '>');
		return buffer.toString();
	}
	
	/**
	 * Creates a new type binding key from the given type name. The type name must be either 
	 * a fully qualified name, an array type name or a primitive type name. 
	 * If the type name is fully qualified, then it is expected to be dot-based. 
	 * Note that inner types, generic types and parameterized types are not supported.
	 * <p>
	 * For example:
	 * <pre>
	 * <code>
	 * createTypeBindingKey("int") -> "I"
	 * createTypeBindingKey("java.lang.String") -> "Ljava/lang/String;"
	 * createTypeBindingKey("boolean[]") -> "[Z"
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * @param typeName the possibly qualified type name
	 * @return a new type binding key
	 */
	public static String createTypeBindingKey(String typeName) {
		// Note this implementation is heavily dependent on TypeBinding#computeUniqueKey() and its subclasses
		return Signature.createTypeSignature(typeName.replace('.', '/'), true/*resolved*/);
	}
	
	/**
	 * Creates a new type variable binding key from the given type variable name and the given declaring key.
	 * The declaring key can either be a type binding key or a method binding key.
	 * <p>
	 * For example:
	 * <pre>
	 * <code>
	 * createTypeVariableBindingKey("T", "Ljava/util/List&lt;TE;&gt;;") -&gt; 
	 *   "Ljava/util/List&lt;TE;&gt;;:TT;"
	 * createTypeVariableBindingKey("SomeTypeVariable", "Lp/X;.foo()V") -&gt; 
	 *   "Lp/X;.foo()V:TSomeTypeVariable;"
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * @param typeVariableName the name of the given type variable
	 * @param declaringKey the binding key of the type or method the type variable belongs to
	 * @return a new type variable binding key
	 */
	public static String createTypeVariableBindingKey(String typeVariableName, String declaringKey) {
		// Note this implementation is heavily dependent on TypeVariableBinding#computeUniqueKey() 
		StringBuffer buffer = new StringBuffer();
		buffer.append(declaringKey);
		buffer.append(':');
		buffer.append('T');
		buffer.append(typeVariableName);
		buffer.append(';');
		return buffer.toString();
	}
	
	/**
	 * Creates a new wildcard type binding key from the given type binding key and the given wildcard kind
	 * (one of {@link Signature#C_STAR}, {@link Signature#C_SUPER}, or {@link Signature#C_EXTENDS}.
	 * If the wildcard is {@link Signature#C_STAR}, the given type binding key is ignored.
	 * <p>
	 * For example:
	 * <pre>
	 * <code>
	 * createWilcardTypeBindingKey(null, Signature.C_STAR) -&gt; "*"
	 * createWilcardTypeBindingKey("Ljava/util/List&lt;TE;&gt;;",
	 *    Signature.C_SUPER) -&gt; "-Ljava/util/List&lt;TE;&gt;;"
	 * createWilcardTypeBindingKey("Ljava/util/ArrayList;", Signature.C_EXTENDS) -&gt;
	 *    "+Ljava/util/ArrayList;"
	 * </code>
	 * </pre>
	 * </p>
	 *
	 * @param typeKey the binding key of the given type
	 * @param kind one of {@link Signature#C_STAR}, {@link Signature#C_SUPER}, or {@link Signature#C_EXTENDS}
	 * @return a new wildcard type binding key
	 */
	public static String createWilcardTypeBindingKey(String typeKey, char kind) {
		// Note this implementation is heavily dependent on WildcardBinding#computeUniqueKey() 
		switch (kind) {
			case Signature.C_STAR:
				return "*"; //$NON-NLS-1$
			case Signature.C_SUPER:
				return '-' + typeKey;
			case Signature.C_EXTENDS:
				return '+' + typeKey;
		}
		return null;
	}

	/**
	 * Returns the thrown exception signatures of the element represented by this binding key.
	 * If this binding key does not  represent a method or does not throw any exception,
	 * returns an empty array.
	 * 
	 * @return the thrown exceptions signatures
	 * @since 3.3
	 */
	public String[] getThrownExceptions() {
		KeyToSignature keyToSignature = new KeyToSignature(this.key, KeyToSignature.THROWN_EXCEPTIONS);
		keyToSignature.parse();
		return keyToSignature.getThrownExceptions();
	}

	/**
	 * Returns the type argument signatures of the element represented by this binding key.
	 * If this binding key doesn't represent a parameterized type or a parameterized method,
	 * returns an empty array.
	 * 
	 * @return the type argument signatures 
	 */
	public String[] getTypeArguments() {
		KeyToSignature keyToSignature = new KeyToSignature(this.key, KeyToSignature.TYPE_ARGUMENTS);
		keyToSignature.parse();
		return keyToSignature.getTypeArguments();
	}
	
	/**
	 * Returns whether this binding key represents a raw type.
	 * 
	 * @return whether this binding key represents a raw type
	 */
	public boolean isRawType() {
		KeyKind kind = new KeyKind(this.key);
		kind.parse();
		return (kind.flags & KeyKind.F_RAW_TYPE) != 0;
	}
	
	/**
	 * Returns whether this binding key represents a parameterized type, or if its declaring type is a parameterized type.
	 * 
	 * @return whether this binding key represents a parameterized type
	 */
	public boolean isParameterizedType() {
		KeyKind kind = new KeyKind(this.key);
		kind.parse();
		return (kind.flags & KeyKind.F_PARAMETERIZED_TYPE) != 0;
	}
	
	/**
	 * Returns whether this binding key represents a parameterized method, or if its declaring method is a parameterized method.
	 * 
	 * @return whether this binding key represents a parameterized method
	 */
	public boolean isParameterizedMethod() {
		KeyKind kind = new KeyKind(this.key);
		kind.parse();
		return (kind.flags & KeyKind.F_PARAMETERIZED_METHOD) != 0;
	}
	
	/**
	 * Transforms this binding key into a resolved signature.
	 * If this binding key represents a field, the returned signature is
	 * the declaring type's signature.
	 * 
	 * @return the resolved signature for this binding key
	 * @see Signature
	 * @since 3.2
	 */
	public String toSignature() {
		KeyToSignature keyToSignature = new KeyToSignature(this.key, KeyToSignature.SIGNATURE);
		keyToSignature.parse();
		return keyToSignature.signature.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.key;
	}
}
