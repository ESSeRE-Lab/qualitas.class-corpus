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
package org.aspectj.org.eclipse.jdt.internal.core;

import org.aspectj.org.eclipse.jdt.core.IJavaElement;
import org.aspectj.org.eclipse.jdt.core.IJavaElementDelta;
import org.aspectj.org.eclipse.jdt.core.IJavaProject;
import org.aspectj.org.eclipse.jdt.core.JavaModelException;

/**
 * Discards a working copy (decrement its use count and remove its working copy info if the use count is 0)
 * and signal its removal through a delta.
 */
public class DiscardWorkingCopyOperation extends JavaModelOperation {
	
	public DiscardWorkingCopyOperation(IJavaElement workingCopy) {
		super(new IJavaElement[] {workingCopy});
	}
	protected void executeOperation() throws JavaModelException {
		CompilationUnit workingCopy = getWorkingCopy();
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		int useCount = manager.discardPerWorkingCopyInfo(workingCopy);
		if (useCount == 0) {
			IJavaProject javaProject = workingCopy.getJavaProject();
			if (ExternalJavaProject.EXTERNAL_PROJECT_NAME.equals(javaProject.getElementName())) {
				manager.removePerProjectInfo((JavaProject) javaProject);
				manager.containerRemove(javaProject);
			}
			if (!workingCopy.isPrimary()) {
				// report removed java delta for a non-primary working copy
				JavaElementDelta delta = new JavaElementDelta(getJavaModel());
				delta.removed(workingCopy);
				addDelta(delta);
				removeReconcileDelta(workingCopy);
			} else {
				if (workingCopy.getResource().isAccessible()) {
					// report a F_PRIMARY_WORKING_COPY change delta for a primary working copy
					JavaElementDelta delta = new JavaElementDelta(getJavaModel());
					delta.changed(workingCopy, IJavaElementDelta.F_PRIMARY_WORKING_COPY);
					addDelta(delta);
				} else {
					// report a REMOVED delta
					JavaElementDelta delta = new JavaElementDelta(getJavaModel());
					delta.removed(workingCopy, IJavaElementDelta.F_PRIMARY_WORKING_COPY);
					addDelta(delta);
				}
			}
		}
	}
	/**
	 * Returns the working copy this operation is working on.
	 */
	protected CompilationUnit getWorkingCopy() {
		return (CompilationUnit)getElementToProcess();
	}
	/**
	 * @see JavaModelOperation#isReadOnly
	 */
	public boolean isReadOnly() {
		return true;
	}
}
