/* *******************************************************************
 * Copyright (c) 2005-2010 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class TypeFactory {

	/**
	 * Create a parameterized version of a generic type.
	 * 
	 * @param aGenericType
	 * @param someTypeParameters note, in the case of an inner type of a parameterized type, this parameter may legitimately be null
	 * @param inAWorld
	 * @return
	 */
	public static ReferenceType createParameterizedType(ResolvedType aBaseType, UnresolvedType[] someTypeParameters, World inAWorld) {
		ResolvedType baseType = aBaseType;
		if (!aBaseType.isGenericType()) {
			// try and find the generic type...
			if (someTypeParameters != null && someTypeParameters.length > 0) {
				if (!aBaseType.isRawType()) {
					throw new IllegalStateException("Expecting raw type, not: " + aBaseType);
				}
				baseType = baseType.getGenericType();
				if (baseType == null) {
					throw new IllegalStateException("Raw type does not have generic type set");
				}
			} // else if someTypeParameters is null, then the base type is allowed to be non-generic, it's an inner
		}
		ResolvedType[] resolvedParameters = inAWorld.resolve(someTypeParameters);
		ReferenceType pType = new ReferenceType(baseType, resolvedParameters, inAWorld);
		// pType.setSourceContext(aBaseType.getSourceContext());
		return (ReferenceType) pType.resolve(inAWorld);
	}

	/**
	 * Create an *unresolved* parameterized version of a generic type.
	 */
	public static UnresolvedType createUnresolvedParameterizedType(String sig, String erasuresig, UnresolvedType[] arguments) {
		return new UnresolvedType(sig, erasuresig, arguments);
	}

	// public static ReferenceType createRawType(
	// ResolvedType aBaseType,
	// World inAWorld
	// ) {
	// if (aBaseType.isRawType()) return (ReferenceType) aBaseType;
	// if (!aBaseType.isGenericType()) {
	// if (!aBaseType.isRawType()) throw new IllegalStateException("Expecting generic type");
	// }
	// ReferenceType rType = new ReferenceType(aBaseType,inAWorld);
	// //rType.setSourceContext(aBaseType.getSourceContext());
	// return (ReferenceType) rType.resolve(inAWorld);
	// }

	/**
	 * Creates a sensible unresolvedtype from some signature, for example: signature = LIGuard<TT;>; bound = toString=IGuard<T>
	 * sig=PIGuard<TT;>; sigErasure=LIGuard; kind=parameterized
	 */
	private static UnresolvedType convertSigToType(String aSignature) {
		UnresolvedType bound = null;
		int startOfParams = aSignature.indexOf('<');
		if (startOfParams == -1) {
			bound = UnresolvedType.forSignature(aSignature);
		} else {
			int endOfParams = aSignature.lastIndexOf('>');
			String signatureErasure = "L" + aSignature.substring(1, startOfParams) + ";";
			UnresolvedType[] typeParams = createTypeParams(aSignature.substring(startOfParams + 1, endOfParams));
			bound = new UnresolvedType("P" + aSignature.substring(1), signatureErasure, typeParams);
		}
		return bound;
	}

	/**
	 * Used by UnresolvedType.read, creates a type from a full signature.
	 */
	public static UnresolvedType createTypeFromSignature(String signature) {
		// if (signature.equals(ResolvedType.MISSING_NAME)) {
		// return ResolvedType.MISSING;
		// }

		char firstChar = signature.charAt(0);
		if (firstChar == 'P') {
			// parameterized type, calculate signature erasure and type parameters
			// (see pr122458) It is possible for a parameterized type to have *no* type parameters visible in its signature.
			// This happens for an inner type of a parameterized type which simply inherits the type parameters
			// of its parent. In this case it is parameterized but theres no < in the signature.
			int startOfParams = signature.indexOf('<');

			if (startOfParams == -1) {
				// Should be an inner type of a parameterized type - could assert there is a '$' in the signature....
				String signatureErasure = "L" + signature.substring(1);
				return new UnresolvedType(signature, signatureErasure, UnresolvedType.NONE);
			} else {
				int endOfParams = locateMatchingEndAngleBracket(signature, startOfParams);
				StringBuffer erasureSig = new StringBuffer(signature);
				erasureSig.setCharAt(0, 'L');
				while (startOfParams != -1) {
					erasureSig.delete(startOfParams, endOfParams + 1);
					startOfParams = locateFirstBracket(erasureSig);
					if (startOfParams != -1) {
						endOfParams = locateMatchingEndAngleBracket(erasureSig, startOfParams);
					}
				}

				String signatureErasure = erasureSig.toString();// "L" + erasureSig.substring(1);

				// the type parameters of interest are only those that apply to the 'last type' in the signature
				// if the signature is 'PMyInterface<String>$MyOtherType;' then there are none...
				String lastType = null;
				int nestedTypePosition = signature.indexOf("$", endOfParams); // don't look for $ INSIDE the parameters
				if (nestedTypePosition != -1) {
					lastType = signature.substring(nestedTypePosition + 1);
				} else {
					lastType = new String(signature);
				}
				startOfParams = lastType.indexOf("<");
				UnresolvedType[] typeParams = UnresolvedType.NONE;
				if (startOfParams != -1) {
					endOfParams = locateMatchingEndAngleBracket(lastType, startOfParams);
					typeParams = createTypeParams(lastType.substring(startOfParams + 1, endOfParams));
				}
				return new UnresolvedType(signature, signatureErasure, typeParams);
			}
			// can't replace above with convertSigToType - leads to stackoverflow
		} else if ((firstChar == '?' || firstChar == '*') && signature.length() == 1) {
			return WildcardedUnresolvedType.QUESTIONMARK;
		} else if (firstChar == '+') {
			// ? extends ...
			UnresolvedType upperBound = convertSigToType(signature.substring(1));
			WildcardedUnresolvedType wildcardedUT = new WildcardedUnresolvedType(signature, upperBound, null);
			return wildcardedUT;
		} else if (firstChar == '-') {
			// ? super ...
			UnresolvedType lowerBound = convertSigToType(signature.substring(1));
			WildcardedUnresolvedType wildcardedUT = new WildcardedUnresolvedType(signature, null, lowerBound);
			return wildcardedUT;
		} else if (firstChar == 'T') {
			String typeVariableName = signature.substring(1);
			if (typeVariableName.endsWith(";")) {
				typeVariableName = typeVariableName.substring(0, typeVariableName.length() - 1);
			}
			return new UnresolvedTypeVariableReferenceType(new TypeVariable(typeVariableName));
		} else if (firstChar == '[') {
			int dims = 0;
			while (signature.charAt(dims) == '[') {
				dims++;
			}
			UnresolvedType componentType = createTypeFromSignature(signature.substring(dims));
			return new UnresolvedType(signature, signature.substring(0, dims) + componentType.getErasureSignature());
		} else if (signature.length() == 1) { // could be a primitive
			switch (firstChar) {

			case 'V':
				return ResolvedType.VOID;
			case 'Z':
				return ResolvedType.BOOLEAN;
			case 'B':
				return ResolvedType.BYTE;
			case 'C':
				return ResolvedType.CHAR;
			case 'D':
				return ResolvedType.DOUBLE;
			case 'F':
				return ResolvedType.FLOAT;
			case 'I':
				return ResolvedType.INT;
			case 'J':
				return ResolvedType.LONG;
			case 'S':
				return ResolvedType.SHORT;
			}
		} else if (firstChar == '@') {
			// missing type
			return ResolvedType.MISSING;
		}
		return new UnresolvedType(signature);
	}

	private static int locateMatchingEndAngleBracket(CharSequence signature, int startOfParams) {
		if (startOfParams == -1) {
			return -1;
		}
		int count = 1;
		int idx = startOfParams;
		int max = signature.length();
		while (idx < max) {
			char ch = signature.charAt(++idx);
			if (ch == '<') {
				count++;
			} else if (ch == '>') {
				if (count == 1) {
					break;
				}
				count--;
			}
		}
		return idx;
	}

	private static int locateFirstBracket(StringBuffer signature) {
		int idx = 0;
		int max = signature.length();
		while (idx < max) {
			if (signature.charAt(idx) == '<') {
				return idx;
			}
			idx++;
		}
		return -1;
	}

	private static UnresolvedType[] createTypeParams(String typeParameterSpecification) {
		String remainingToProcess = typeParameterSpecification;
		List types = new ArrayList();
		while (remainingToProcess.length() != 0) {
			int endOfSig = 0;
			int anglies = 0;
			boolean sigFound = false; // OPTIMIZE can this be done better?
			for (endOfSig = 0; (endOfSig < remainingToProcess.length()) && !sigFound; endOfSig++) {
				char thisChar = remainingToProcess.charAt(endOfSig);
				switch (thisChar) {
				case '<':
					anglies++;
					break;
				case '>':
					anglies--;
					break;
				case '[':
					if (anglies == 0) {
						// the next char might be a [ or a primitive type ref (BCDFIJSZ)
						int nextChar = endOfSig + 1;
						while (remainingToProcess.charAt(nextChar) == '[') {
							nextChar++;
						}
						if ("BCDFIJSZ".indexOf(remainingToProcess.charAt(nextChar)) != -1) {
							// it is something like [I or [[S
							sigFound = true;
							endOfSig = nextChar;
							break;
						}
					}
					break;
				case ';':
					if (anglies == 0) {
						sigFound = true;
						break;
					}
				}
			}
			types.add(createTypeFromSignature(remainingToProcess.substring(0, endOfSig)));
			remainingToProcess = remainingToProcess.substring(endOfSig);
		}
		UnresolvedType[] typeParams = new UnresolvedType[types.size()];
		types.toArray(typeParams);
		return typeParams;
	}

	// OPTIMIZE improve all this signature processing stuff, use char arrays, etc

	/**
	 * Create a signature then delegate to the other factory method. Same input/output: baseTypeSignature="LSomeType;" arguments[0]=
	 * something with sig "Pcom/Foo<Ljava/lang/String;>;" signature created = "PSomeType<Pcom/Foo<Ljava/lang/String;>;>;"
	 */
	public static UnresolvedType createUnresolvedParameterizedType(String baseTypeSignature, UnresolvedType[] arguments) {
		StringBuffer parameterizedSig = new StringBuffer();
		parameterizedSig.append(ResolvedType.PARAMETERIZED_TYPE_IDENTIFIER);
		parameterizedSig.append(baseTypeSignature.substring(1, baseTypeSignature.length() - 1));
		if (arguments.length > 0) {
			parameterizedSig.append("<");
			for (int i = 0; i < arguments.length; i++) {
				parameterizedSig.append(arguments[i].getSignature());
			}
			parameterizedSig.append(">");
		}
		parameterizedSig.append(";");
		return createUnresolvedParameterizedType(parameterizedSig.toString(), baseTypeSignature, arguments);
	}
}
