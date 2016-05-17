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
package org.aspectj.org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipFile;

import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.aspectj.org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.util.Util;

public class FileSystem implements INameEnvironment, SuffixConstants {
	public interface Classpath {
		char[][][] findTypeNames(String qualifiedPackageName);
		NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName);
		NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly);
		boolean isPackage(String qualifiedPackageName);
		/**
		 * This method resets the environment. The resulting state is equivalent to
		 * a new name environment without creating a new object.
		 */
		void reset();
		/**
		 * Return a normalized path for file based classpath entries. This is an absolute path
		 * ending with a file separator for directories, an absolute path deprived from the '.jar'
		 * (resp. '.zip') extension for jar (resp. zip) files.
		 * @return a normalized path for file based classpath entries
		 */
		char[] normalizedPath();
		/**
		 * Return the path for file based classpath entries. This is an absolute path
		 * ending with a file separator for directories, an absolute path including the '.jar'
		 * (resp. '.zip') extension for jar (resp. zip) files.
		 * @return the path for file based classpath entries
		 */
		String getPath();
		/**
		 * Initialize the entry
		 */
		void initialize() throws IOException;
	}

	/**
	 * This class is defined how to normalize the classpath entries.
	 * It removes duplicate entries.
	 */
	public static class ClasspathNormalizer {
		/**
		 * Returns the normalized classpath entries (no duplicate).
		 * <p>The given classpath entries are FileSystem.Classpath. We check the getPath() in order to find
		 * duplicate entries.</p>
		 *
		 * @param classpaths the given classpath entries
		 * @return the normalized classpath entries
		 */
		public static ArrayList normalize(ArrayList classpaths) {
			ArrayList normalizedClasspath = new ArrayList();
			HashSet cache = new HashSet();
			for (Iterator iterator = classpaths.iterator(); iterator.hasNext(); ) {
				FileSystem.Classpath classpath = (FileSystem.Classpath) iterator.next();
				String path = classpath.getPath();
				if (!cache.contains(path)) {
					normalizedClasspath.add(classpath);
					cache.add(path);
				}
			}
			return normalizedClasspath;
		}
	}

	Classpath[] classpaths;
	Set knownFileNames;

/*
	classPathNames is a collection is Strings representing the full path of each class path
	initialFileNames is a collection is Strings, the trailing '.java' will be removed if its not already.
*/
public FileSystem(String[] classpathNames, String[] initialFileNames, String encoding, int mode) { // New AspectJ Extension - extra int flag for mode, was 'public FileSystem(String[] classpathNames, String[] initialFileNames, String encoding) {'
	final int classpathSize = classpathNames.length;
	this.classpaths = new Classpath[classpathSize];
	int counter = 0;
	for (int i = 0; i < classpathSize; i++) {
		Classpath classpath = getClasspath(classpathNames[i], encoding, null,mode); // New AspectJ Extension - pass extra mode
		if (classpath==null) continue; // AspectJ Extension
		try {
			classpath.initialize();
			this.classpaths[counter++] = classpath;
		} catch (IOException e) {
			// ignore
		}
	}
	if (counter != classpathSize) {
		System.arraycopy(this.classpaths, 0, (this.classpaths = new Classpath[counter]), 0, counter);
	}
	initializeKnownFileNames(initialFileNames);
}
FileSystem(Classpath[] paths, String[] initialFileNames) {
	final int length = paths.length;
	int counter = 0;
	this.classpaths = new FileSystem.Classpath[length];
	for (int i = 0; i < length; i++) {
		final Classpath classpath = paths[i];
		try {
			classpath.initialize();
			this.classpaths[counter++] = classpath;
		} catch(IOException exception) {
			// ignore
		}
	}
	if (counter != length) {
		// should not happen
		System.arraycopy(this.classpaths, 0, (this.classpaths = new FileSystem.Classpath[counter]), 0, counter);
	}
	initializeKnownFileNames(initialFileNames);
}
public static Classpath getClasspath(String classpathName, String encoding, AccessRuleSet accessRuleSet) {
	return getClasspath(classpathName, encoding, false, accessRuleSet, null);
}
//New AspectJ Extension

// Uses the mode rather than a boolean, so we can specify JUST binary (ClasspathLocation.BINARY)
public static Classpath getClasspath(String classpathName, String encoding, AccessRuleSet accessRuleSet,int mode) {
	return getClasspath(classpathName, encoding, mode, accessRuleSet, null);
}
// Reworking of constructor, the original one that takes a boolean now delegates to the new one.
// Original ctor declaration was:
// public static Classpath getClasspath(String classpathName, String encoding,
// 		boolean isSourceOnly, AccessRuleSet accessRuleSet,
// 		String destinationPath) {
public static Classpath getClasspath(String classpathName, String encoding,
		boolean isSourceOnly, AccessRuleSet accessRuleSet,
		String destinationPath) {
	return getClasspath(classpathName,encoding,isSourceOnly ? ClasspathLocation.SOURCE :ClasspathLocation.SOURCE|ClasspathLocation.BINARY,accessRuleSet,destinationPath);
}

public static Classpath getClasspath(String classpathName, String encoding,
		int mode, AccessRuleSet accessRuleSet,
		String destinationPath) {
	// End AspectJ Extension
	Classpath result = null;
	File file = new File(convertPathSeparators(classpathName));
	if (file.isDirectory()) {
		if (file.exists()) {
			result = new ClasspathDirectory(file, encoding,
// New AspectJ Extension
// old code:
//					isSourceOnly ? ClasspathLocation.SOURCE :
//						ClasspathLocation.SOURCE | 
//						ClasspathLocation.BINARY,
// new code:
					mode,
// End AspectJ Extension
					accessRuleSet,
					destinationPath == null || destinationPath == Main.NONE ?
						destinationPath : // keep == comparison valid
						convertPathSeparators(destinationPath));
		}
	} else {
		String lowercaseClasspathName = classpathName.toLowerCase();
		/// AspectJ Extension - check if the file is a zip rather than just using suffix (pr186673)
		// old code:
		// if (lowercaseClasspathName.endsWith(SUFFIX_STRING_jar)
		//  || lowercaseClasspathName.endsWith(SUFFIX_STRING_zip)) {
		// new code:
		boolean isZip = false;
		try {
			ZipFile zf = new ZipFile(file);
			zf.close();
			isZip = true;
		} catch (Exception e) {
			// this means it is not a valid Zip 
		}
		if (isZip) {
		// End AspectJ Extension
		// New AspectJ Extension - use mode instead of flag
		// old code:
		//if (isSourceOnly) {
		// new code:
		if ((mode & ClasspathLocation.BINARY)==0) {
		// End AspectJ Extension
				// source only mode
				result = new ClasspathSourceJar(file, true, accessRuleSet,
					encoding,
					destinationPath == null || destinationPath == Main.NONE ?
						destinationPath : // keep == comparison valid
						convertPathSeparators(destinationPath));
			} else if (destinationPath == null) {
				// class file only mode
				result = new ClasspathJar(file, true, accessRuleSet, null);
			}
		}
	}
	return result;
}
private void initializeKnownFileNames(String[] initialFileNames) {
	if (initialFileNames == null) {
		this.knownFileNames = new HashSet(0);
		return;
	}
	this.knownFileNames = new HashSet(initialFileNames.length * 2);
	for (int i = initialFileNames.length; --i >= 0;) {
		char[] fileName = initialFileNames[i].toCharArray();
		char[] matchingPathName = null;
		final int lastIndexOf = CharOperation.lastIndexOf('.', fileName);
		if (lastIndexOf != -1) {
			fileName = CharOperation.subarray(fileName, 0, lastIndexOf);
		}
		CharOperation.replace(fileName, '\\', '/');
		for (int j = 0; j < classpaths.length; j++){
			char[] matchCandidate = this.classpaths[j].normalizedPath();
			if (this.classpaths[j] instanceof  ClasspathDirectory &&
					CharOperation.prefixEquals(matchCandidate, fileName) &&
					(matchingPathName == null ||
							matchCandidate.length < matchingPathName.length))
				matchingPathName = matchCandidate;
		}
		if (matchingPathName == null) {
			this.knownFileNames.add(new String(fileName)); // leave as is...
		} else {
			this.knownFileNames.add(new String(CharOperation.subarray(fileName, matchingPathName.length, fileName.length)));
		}
		matchingPathName = null;
	}
}
public void cleanup() {
	for (int i = 0, max = this.classpaths.length; i < max; i++)
		this.classpaths[i].reset();
}
private static String convertPathSeparators(String path) {
	return File.separatorChar == '/'
		? path.replace('\\', '/')
		 : path.replace('/', '\\');
}
private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName, boolean asBinaryOnly){
	if (this.knownFileNames.contains(qualifiedTypeName)) return null; // looking for a file which we know was provided at the beginning of the compilation

	String qualifiedBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
	String qualifiedPackageName =
		qualifiedTypeName.length() == typeName.length
			? Util.EMPTY_STRING
			: qualifiedBinaryFileName.substring(0, qualifiedTypeName.length() - typeName.length - 1);
	String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
	NameEnvironmentAnswer suggestedAnswer = null;
	if (qualifiedPackageName == qp2) {
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			NameEnvironmentAnswer answer = this.classpaths[i].findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly);
			if (answer != null) {
				if (!answer.ignoreIfBetter()) {
					if (answer.isBetter(suggestedAnswer))
						return answer;
				} else if (answer.isBetter(suggestedAnswer))
					// remember suggestion and keep looking
					suggestedAnswer = answer;
			}
		}
	} else {
		String qb2 = qualifiedBinaryFileName.replace('/', File.separatorChar);
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			Classpath p = this.classpaths[i];
			NameEnvironmentAnswer answer = (p instanceof ClasspathJar)
				? p.findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly)
				: p.findClass(typeName, qp2, qb2, asBinaryOnly);
			if (answer != null) {
				if (!answer.ignoreIfBetter()) {
					if (answer.isBetter(suggestedAnswer))
						return answer;
				} else if (answer.isBetter(suggestedAnswer))
					// remember suggestion and keep looking
					suggestedAnswer = answer;
			}
		}
	}
	if (suggestedAnswer != null)
		// no better answer was found
		return suggestedAnswer;
	return null;
}
public NameEnvironmentAnswer findType(char[][] compoundName) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1],
			false);
	return null;
}
public char[][][] findTypeNames(char[][] packageName) {
	char[][][] result = null;
	if (packageName != null) {
		String qualifiedPackageName = new String(CharOperation.concatWith(packageName, '/'));
		String qualifiedPackageName2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
		if (qualifiedPackageName == qualifiedPackageName2) {
			for (int i = 0, length = this.classpaths.length; i < length; i++) {
				char[][][] answers = this.classpaths[i].findTypeNames(qualifiedPackageName);
				if (answers != null) {
					// concat with previous answers
					if (result == null) {
						result = answers;
					} else {
						int resultLength = result.length;
						int answersLength = answers.length;
						System.arraycopy(result, 0, (result = new char[answersLength + resultLength][][]), 0, resultLength);
						System.arraycopy(answers, 0, result, resultLength, answersLength);
					}
				}
			}
		} else {
			for (int i = 0, length = this.classpaths.length; i < length; i++) {
				Classpath p = this.classpaths[i];
				char[][][] answers = (p instanceof ClasspathJar)
					? p.findTypeNames(qualifiedPackageName)
					: p.findTypeNames(qualifiedPackageName2);
				if (answers != null) {
					// concat with previous answers
					if (result == null) {
						result = answers;
					} else {
						int resultLength = result.length;
						int answersLength = answers.length;
						System.arraycopy(result, 0, (result = new char[answersLength + resultLength][][]), 0, resultLength);
						System.arraycopy(answers, 0, result, resultLength, answersLength);
					}
				}
			}
		}
	}
	return result;
}
public NameEnvironmentAnswer findType(char[][] compoundName, boolean asBinaryOnly) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1],
			asBinaryOnly);
	return null;
}
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName,
			false);
	return null;
}
public boolean isPackage(char[][] compoundName, char[] packageName) {
	String qualifiedPackageName = new String(CharOperation.concatWith(compoundName, packageName, '/'));
	String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
	if (qualifiedPackageName == qp2) {
		for (int i = 0, length = this.classpaths.length; i < length; i++)
			if (this.classpaths[i].isPackage(qualifiedPackageName))
				return true;
	} else {
		for (int i = 0, length = this.classpaths.length; i < length; i++) {
			Classpath p = this.classpaths[i];
			if ((p instanceof ClasspathJar) ? p.isPackage(qualifiedPackageName) : p.isPackage(qp2))
				return true;
		}
	}
	return false;
}
}
