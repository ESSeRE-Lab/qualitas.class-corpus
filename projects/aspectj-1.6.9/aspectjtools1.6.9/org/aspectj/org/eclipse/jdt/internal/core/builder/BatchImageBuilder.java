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
package org.aspectj.org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.aspectj.org.eclipse.jdt.core.JavaCore;
import org.aspectj.org.eclipse.jdt.core.compiler.*;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.core.util.Messages;
import org.aspectj.org.eclipse.jdt.internal.core.util.Util;

import java.util.*;

public class BatchImageBuilder extends AbstractImageBuilder {

	IncrementalImageBuilder incrementalBuilder; // if annotations or secondary types have to be processed after the compile loop
	ArrayList secondaryTypes; // qualified names for all secondary types found during batch compile
	StringSet typeLocatorsWithUndefinedTypes; // type locators for all source files with errors that may be caused by 'not found' secondary types

protected BatchImageBuilder(JavaBuilder javaBuilder, boolean buildStarting) {
	super(javaBuilder, buildStarting, null);
	this.nameEnvironment.isIncrementalBuild = false;
	this.incrementalBuilder = null;
	this.secondaryTypes = null;
	this.typeLocatorsWithUndefinedTypes = null;
}

public void build() {
	if (JavaBuilder.DEBUG)
		System.out.println("FULL build"); //$NON-NLS-1$

	try {
		notifier.subTask(Messages.bind(Messages.build_cleaningOutput, this.javaBuilder.currentProject.getName()));
		JavaBuilder.removeProblemsAndTasksFor(javaBuilder.currentProject);
		cleanOutputFolders(true);
		notifier.updateProgressDelta(0.05f);

		notifier.subTask(Messages.build_analyzingSources); 
		ArrayList sourceFiles = new ArrayList(33);
		addAllSourceFiles(sourceFiles);
		notifier.updateProgressDelta(0.10f);

		if (sourceFiles.size() > 0) {
			SourceFile[] allSourceFiles = new SourceFile[sourceFiles.size()];
			sourceFiles.toArray(allSourceFiles);

			notifier.setProgressPerCompilationUnit(0.75f / allSourceFiles.length);
			workQueue.addAll(allSourceFiles);
			compile(allSourceFiles);

			if (this.typeLocatorsWithUndefinedTypes != null)
				if (this.secondaryTypes != null && !this.secondaryTypes.isEmpty())
					rebuildTypesAffectedBySecondaryTypes();
			if (this.incrementalBuilder != null)
				this.incrementalBuilder.buildAfterBatchBuild();
		}

		if (javaBuilder.javaProject.hasCycleMarker())
			javaBuilder.mustPropagateStructuralChanges();
	} catch (CoreException e) {
		throw internalException(e);
	} finally {
		cleanUp();
	}
}

protected void acceptSecondaryType(ClassFile classFile) {
	if (this.secondaryTypes != null)
		this.secondaryTypes.add(classFile.fileName());
}

protected void cleanOutputFolders(boolean copyBack) throws CoreException {
	boolean deleteAll = JavaCore.CLEAN.equals(
		javaBuilder.javaProject.getOption(JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, true));
	if (deleteAll) {
		if (this.javaBuilder.participants != null)
			for (int i = 0, l = this.javaBuilder.participants.length; i < l; i++)
				this.javaBuilder.participants[i].cleanStarting(this.javaBuilder.javaProject);

		ArrayList visited = new ArrayList(sourceLocations.length);
		for (int i = 0, l = sourceLocations.length; i < l; i++) {
			notifier.subTask(Messages.bind(Messages.build_cleaningOutput, this.javaBuilder.currentProject.getName())); 
			ClasspathMultiDirectory sourceLocation = sourceLocations[i];
			if (sourceLocation.hasIndependentOutputFolder) {
				IContainer outputFolder = sourceLocation.binaryFolder;
				if (!visited.contains(outputFolder)) {
					visited.add(outputFolder);
					IResource[] members = outputFolder.members(); 
					for (int j = 0, m = members.length; j < m; j++) {
						IResource member = members[j];
						if (!member.isDerived()) {
							member.accept(
								new IResourceVisitor() {
									public boolean visit(IResource resource) throws CoreException {
										resource.setDerived(true);
										return resource.getType() != IResource.FILE;
									}
								}
							);
						}
						member.delete(IResource.FORCE, null);
					}
				}
				notifier.checkCancel();
				if (copyBack)
					copyExtraResourcesBack(sourceLocation, true);
			} else {
				boolean isOutputFolder = sourceLocation.sourceFolder.equals(sourceLocation.binaryFolder);
				final char[][] exclusionPatterns =
					isOutputFolder
						? sourceLocation.exclusionPatterns
						: null; // ignore exclusionPatterns if output folder == another source folder... not this one
				final char[][] inclusionPatterns =
					isOutputFolder
						? sourceLocation.inclusionPatterns
						: null; // ignore inclusionPatterns if output folder == another source folder... not this one
				sourceLocation.binaryFolder.accept(
					new IResourceProxyVisitor() {
						public boolean visit(IResourceProxy proxy) throws CoreException {
							if (proxy.getType() == IResource.FILE) {
								if (org.aspectj.org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(proxy.getName())) {
									IResource resource = proxy.requestResource();
									if (exclusionPatterns != null || inclusionPatterns != null)
										if (Util.isExcluded(resource.getFullPath(), inclusionPatterns, exclusionPatterns, false))
											return false;
									resource.delete(IResource.FORCE, null);
								}
								return false;
							}
							if (exclusionPatterns != null && inclusionPatterns == null) // must walk children if inclusionPatterns != null
								if (Util.isExcluded(proxy.requestFullPath(), null, exclusionPatterns, true))
									return false;
							notifier.checkCancel();
							return true;
						}
					},
					IResource.NONE
				);
				notifier.checkCancel();
			}
			notifier.checkCancel();
		}
	} else if (copyBack) {
		for (int i = 0, l = sourceLocations.length; i < l; i++) {
			ClasspathMultiDirectory sourceLocation = sourceLocations[i];
			if (sourceLocation.hasIndependentOutputFolder)
				copyExtraResourcesBack(sourceLocation, false);
			notifier.checkCancel();
		}
	}
}

protected void cleanUp() {
	this.incrementalBuilder = null;
	this.secondaryTypes = null;
	this.typeLocatorsWithUndefinedTypes = null;
	super.cleanUp();
}

protected void compile(SourceFile[] units, SourceFile[] additionalUnits, boolean compilingFirstGroup) {
	if (additionalUnits != null && this.secondaryTypes == null)
		this.secondaryTypes = new ArrayList(7);
	super.compile(units, additionalUnits, compilingFirstGroup);
}

protected void copyExtraResourcesBack(ClasspathMultiDirectory sourceLocation, final boolean deletedAll) throws CoreException {
	// When, if ever, does a builder need to copy resources files (not .java or .class) into the output folder?
	// If we wipe the output folder at the beginning of the build then all 'extra' resources must be copied to the output folder.

	notifier.subTask(Messages.build_copyingResources); 
	final int segmentCount = sourceLocation.sourceFolder.getFullPath().segmentCount();
	final char[][] exclusionPatterns = sourceLocation.exclusionPatterns;
	final char[][] inclusionPatterns = sourceLocation.inclusionPatterns;
	final IContainer outputFolder = sourceLocation.binaryFolder;
	final boolean isAlsoProject = sourceLocation.sourceFolder.equals(javaBuilder.currentProject);
	sourceLocation.sourceFolder.accept(
		new IResourceProxyVisitor() {
			public boolean visit(IResourceProxy proxy) throws CoreException {
				IResource resource = null;
				switch(proxy.getType()) {
					case IResource.FILE :
						if (org.aspectj.org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(proxy.getName()) ||
							org.aspectj.org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(proxy.getName())) return false;

						resource = proxy.requestResource();
						if (javaBuilder.filterExtraResource(resource)) return false;
						if (exclusionPatterns != null || inclusionPatterns != null)
							if (Util.isExcluded(resource.getFullPath(), inclusionPatterns, exclusionPatterns, false))
								return false;

						IPath partialPath = resource.getFullPath().removeFirstSegments(segmentCount);
						IResource copiedResource = outputFolder.getFile(partialPath);
						if (copiedResource.exists()) {
							if (deletedAll) {
								IResource originalResource = findOriginalResource(partialPath);
								String id = originalResource.getFullPath().removeFirstSegments(1).toString();
								createProblemFor(
									resource,
									null,
									Messages.bind(Messages.build_duplicateResource, id), 
									javaBuilder.javaProject.getOption(JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE, true));
								return false;
							}
							copiedResource.delete(IResource.FORCE, null); // last one wins
						}
						createFolder(partialPath.removeLastSegments(1), outputFolder); // ensure package folder exists
						resource.copy(copiedResource.getFullPath(), IResource.FORCE | IResource.DERIVED, null);
						Util.setReadOnly(copiedResource, false); // just in case the original was read only
						return false;
					case IResource.FOLDER :
						resource = proxy.requestResource();
						if (javaBuilder.filterExtraResource(resource)) return false;
						if (isAlsoProject && isExcludedFromProject(resource.getFullPath())) return false; // the sourceFolder == project
						if (exclusionPatterns != null && inclusionPatterns == null) // must walk children if inclusionPatterns != null
							if (Util.isExcluded(resource.getFullPath(), null, exclusionPatterns, true))
								return false;
				}
				return true;
			}
		},
		IResource.NONE
	);
}

protected IResource findOriginalResource(IPath partialPath) {
	for (int i = 0, l = sourceLocations.length; i < l; i++) {
		ClasspathMultiDirectory sourceLocation = sourceLocations[i];
		if (sourceLocation.hasIndependentOutputFolder) {
			IResource originalResource = sourceLocation.sourceFolder.getFile(partialPath);
			if (originalResource.exists()) return originalResource;
		}
	}
	return null;
}

protected void processAnnotationResults(CompilationParticipantResult[] results) {
	// to compile the compilation participant results, we need to incrementally recompile all affected types
	// whenever the generated types are initially added or structurally changed
	if (this.incrementalBuilder == null)
		this.incrementalBuilder = new IncrementalImageBuilder(this);
	this.incrementalBuilder.processAnnotationResults(results);
}

protected void rebuildTypesAffectedBySecondaryTypes() {
	// to compile types that could not find 'missing' secondary types because of multiple
	// compile groups, we need to incrementally recompile all affected types as if the missing
	// secondary types have just been added, see bug 146324
	if (this.incrementalBuilder == null)
		this.incrementalBuilder = new IncrementalImageBuilder(this);

	for (int i = this.secondaryTypes.size(); --i >=0;) {
		char[] secondaryTypeName = (char[]) this.secondaryTypes.get(i);
		IPath path = new Path(null, new String(secondaryTypeName));
		this.incrementalBuilder.addDependentsOf(path, false);
	}
	this.incrementalBuilder.addAffectedSourceFiles(
		this.incrementalBuilder.qualifiedStrings,
		this.incrementalBuilder.simpleStrings,
		this.typeLocatorsWithUndefinedTypes);
}

protected void storeProblemsFor(SourceFile sourceFile, CategorizedProblem[] problems) throws CoreException {
	if (sourceFile == null || problems == null || problems.length == 0) return;

	for (int i = problems.length; --i >= 0;) {
		CategorizedProblem problem = problems[i];
		if (problem != null && problem.getID() == IProblem.UndefinedType) {
			if (this.typeLocatorsWithUndefinedTypes == null)
				this.typeLocatorsWithUndefinedTypes = new StringSet(3);
			this.typeLocatorsWithUndefinedTypes.add(sourceFile.typeLocator());
			break;
		}
	}

	super.storeProblemsFor(sourceFile, problems);
}

public String toString() {
	return "batch image builder for:\n\tnew state: " + newState; //$NON-NLS-1$
}
}
