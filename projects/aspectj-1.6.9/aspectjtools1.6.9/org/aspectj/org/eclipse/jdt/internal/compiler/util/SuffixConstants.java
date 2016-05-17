/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.compiler.util;

public interface SuffixConstants {
	public final static String EXTENSION_class = "class"; //$NON-NLS-1$
	public final static String EXTENSION_CLASS = "CLASS"; //$NON-NLS-1$
	public final static String EXTENSION_java = "java"; //$NON-NLS-1$
	public final static String EXTENSION_JAVA = "JAVA"; //$NON-NLS-1$
	
	public final static String SUFFIX_STRING_class = "." + EXTENSION_class; //$NON-NLS-1$
	public final static String SUFFIX_STRING_CLASS = "." + EXTENSION_CLASS; //$NON-NLS-1$
	public final static String SUFFIX_STRING_java = "." + EXTENSION_java; //$NON-NLS-1$
	public final static String SUFFIX_STRING_JAVA = "." + EXTENSION_JAVA; //$NON-NLS-1$
	
	public final static char[] SUFFIX_class = SUFFIX_STRING_class.toCharArray();
	public final static char[] SUFFIX_CLASS = SUFFIX_STRING_CLASS.toCharArray();
	public final static char[] SUFFIX_java = SUFFIX_STRING_java.toCharArray();
	public final static char[] SUFFIX_JAVA = SUFFIX_STRING_JAVA.toCharArray();
	
	// AspectJ Extension
	public final static String EXTENSION_aj = "aj"; //$NON-NLS-1$
	public final static String EXTENSION_AJ = "AJ"; //$NON-NLS-1$
	public final static String SUFFIX_STRING_aj = "." + EXTENSION_aj; //$NON-NLS-1$
	public final static String SUFFIX_STRING_AJ = "." + EXTENSION_AJ; //$NON-NLS-1$
	public final static char[] SUFFIX_aj = SUFFIX_STRING_aj.toCharArray();
	public final static char[] SUFFIX_AJ = SUFFIX_STRING_AJ.toCharArray();
	// End AspectJ Extension

	public final static String EXTENSION_jar = "jar"; //$NON-NLS-1$
	public final static String EXTENSION_JAR = "JAR"; //$NON-NLS-1$
	public final static String EXTENSION_zip = "zip"; //$NON-NLS-1$
	public final static String EXTENSION_ZIP = "ZIP"; //$NON-NLS-1$

	public final static String SUFFIX_STRING_jar = "." + EXTENSION_jar; //$NON-NLS-1$
	public final static String SUFFIX_STRING_JAR = "." + EXTENSION_JAR; //$NON-NLS-1$
	public final static String SUFFIX_STRING_zip = "." + EXTENSION_zip; //$NON-NLS-1$
	public final static String SUFFIX_STRING_ZIP = "." + EXTENSION_ZIP; //$NON-NLS-1$

	public final static char[] SUFFIX_jar = SUFFIX_STRING_jar.toCharArray();
	public final static char[] SUFFIX_JAR = SUFFIX_STRING_JAR.toCharArray();
	public final static char[] SUFFIX_zip = SUFFIX_STRING_zip.toCharArray(); 
	public final static char[] SUFFIX_ZIP = SUFFIX_STRING_ZIP.toCharArray();
}
