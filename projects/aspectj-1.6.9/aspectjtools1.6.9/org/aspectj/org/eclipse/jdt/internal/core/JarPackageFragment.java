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
package org.aspectj.org.eclipse.jdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.aspectj.org.eclipse.jdt.core.IClassFile;
import org.aspectj.org.eclipse.jdt.core.ICompilationUnit;
import org.aspectj.org.eclipse.jdt.core.IJarEntryResource;
import org.aspectj.org.eclipse.jdt.core.IJavaElement;
import org.aspectj.org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.aspectj.org.eclipse.jdt.core.JavaModelException;
import org.aspectj.org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.aspectj.org.eclipse.jdt.internal.core.util.Util;

/**
 * A package fragment that represents a package fragment found in a JAR.
 *
 * @see org.aspectj.org.eclipse.jdt.core.IPackageFragment
 */
class JarPackageFragment extends PackageFragment implements SuffixConstants {
/**
 * Constructs a package fragment that is contained within a jar or a zip.
 */
protected JarPackageFragment(PackageFragmentRoot root, String[] names) {
	super(root, names);
}
/**
 * Compute the children of this package fragment. Children of jar package fragments
 * can only be IClassFile (representing .class files).
 */
protected boolean computeChildren(OpenableElementInfo info, ArrayList entryNames) {
	if (entryNames != null && entryNames.size() > 0) {
		ArrayList vChildren = new ArrayList();
		for (Iterator iter = entryNames.iterator(); iter.hasNext();) {
			String child = (String) iter.next();
			IClassFile classFile = getClassFile(child);
			vChildren.add(classFile);
		}
		IJavaElement[] children= new IJavaElement[vChildren.size()];
		vChildren.toArray(children);
		info.setChildren(children);
	} else {
		info.setChildren(NO_ELEMENTS);
	}
	return true;
}
/**
 * Compute all the non-java resources according to the entry name found in the jar file.
 */
/* package */ void computeNonJavaResources(String[] resNames, JarPackageFragment pkg, JarPackageFragmentInfo info, String zipName) {
	if (resNames == null) {
		info.setNonJavaResources(null);
		return;
	}
	int max = resNames.length;
	if (max == 0) {
	    info.setNonJavaResources(JavaElementInfo.NO_NON_JAVA_RESOURCES);
	} else {
		HashMap jarEntries = new HashMap(); // map from IPath to IJarEntryResource
		HashMap childrenMap = new HashMap(); // map from IPath to ArrayList<IJarEntryResource>
		ArrayList topJarEntries = new ArrayList();
		for (int i = 0; i < max; i++) {
			String resName = resNames[i];
			// consider that a .java file is not a non-java resource (see bug 12246 Packages view shows .class and .java files when JAR has source)
			if (!Util.isJavaLikeFileName(resName)) {
				IPath filePath = new Path(resName);
				IPath childPath = filePath.removeFirstSegments(this.names.length);
				JarEntryFile file = new JarEntryFile(filePath.lastSegment());
				jarEntries.put(childPath, file);
				if (childPath.segmentCount() == 1) {
					file.setParent(pkg);
					topJarEntries.add(file);
				} else {
					IPath parentPath = childPath.removeLastSegments(1);
					while (parentPath.segmentCount() > 0) {
						ArrayList parentChildren = (ArrayList) childrenMap.get(parentPath);
						if (parentChildren == null) {
							Object dir = new JarEntryDirectory(parentPath.lastSegment());
							jarEntries.put(parentPath, dir);
							childrenMap.put(parentPath, parentChildren = new ArrayList());
							parentChildren.add(childPath);
							if (parentPath.segmentCount() == 1) {
								topJarEntries.add(dir);
								break;
							}
							childPath = parentPath;
							parentPath = childPath.removeLastSegments(1);
						} else {
							parentChildren.add(childPath);
							break; // all parents are already registered
						}
					}
				}
			}
		}
		Iterator entries = childrenMap.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			IPath entryPath = (IPath) entry.getKey();
			ArrayList entryValue =  (ArrayList) entry.getValue();
			JarEntryDirectory jarEntryDirectory = (JarEntryDirectory) jarEntries.get(entryPath);
			int size = entryValue.size();
			IJarEntryResource[] children = new IJarEntryResource[size];
			for (int i = 0; i < size; i++) {
				JarEntryResource child = (JarEntryResource) jarEntries.get(entryValue.get(i));
				child.setParent(jarEntryDirectory);
				children[i] = child;
			}
			jarEntryDirectory.setChildren(children);
			if (entryPath.segmentCount() == 1) {
				jarEntryDirectory.setParent(pkg);
			}
		}
		Object[] res = topJarEntries.toArray(new Object[topJarEntries.size()]);
		info.setNonJavaResources(res);
	}
}
/**
 * Returns true if this fragment contains at least one java resource.
 * Returns false otherwise.
 */
public boolean containsJavaResources() throws JavaModelException {
	return ((JarPackageFragmentInfo) getElementInfo()).containsJavaResources();
}
/**
 * @see org.aspectj.org.eclipse.jdt.core.IPackageFragment
 */
public ICompilationUnit createCompilationUnit(String cuName, String contents, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/**
 * @see JavaElement
 */
protected Object createElementInfo() {
	return null; // not used for JarPackageFragments: info is created when jar is opened
}
/*
 * @see JavaElement#generateInfos
 */
protected void generateInfos(Object info, HashMap newElements, IProgressMonitor pm) throws JavaModelException {
	// Open my jar: this creates all the pkg infos
	Openable openableParent = (Openable)this.parent;
	if (!openableParent.isOpen()) {
		openableParent.generateInfos(openableParent.createElementInfo(), newElements, pm);
	}
}
/**
 * @see org.aspectj.org.eclipse.jdt.core.IPackageFragment
 */
public IClassFile[] getClassFiles() throws JavaModelException {
	ArrayList list = getChildrenOfType(CLASS_FILE);
	IClassFile[] array= new IClassFile[list.size()];
	list.toArray(array);
	return array;
}
/**
 * A jar package fragment never contains compilation units.
 * @see org.aspectj.org.eclipse.jdt.core.IPackageFragment
 */
public ICompilationUnit[] getCompilationUnits() {
	return NO_COMPILATION_UNITS;
}
/**
 * A package fragment in a jar has no corresponding resource.
 *
 * @see IJavaElement
 */
public IResource getCorrespondingResource() {
	return null;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
public Object[] getNonJavaResources() throws JavaModelException {
	if (this.isDefaultPackage()) {
		// We don't want to show non java resources of the default package (see PR #1G58NB8)
		return JavaElementInfo.NO_NON_JAVA_RESOURCES;
	} else {
		return this.storedNonJavaResources();
	}
}
/**
 * Jars and jar entries are all read only
 */
public boolean isReadOnly() {
	return true;
}
protected Object[] storedNonJavaResources() throws JavaModelException {
	return ((JarPackageFragmentInfo) getElementInfo()).getNonJavaResources();
}
}
