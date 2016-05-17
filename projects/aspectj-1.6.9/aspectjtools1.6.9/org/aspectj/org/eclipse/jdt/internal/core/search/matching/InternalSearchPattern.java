/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.core.runtime.*;
import org.aspectj.org.eclipse.jdt.core.IJavaElement;
import org.aspectj.org.eclipse.jdt.core.search.*;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.aspectj.org.eclipse.jdt.internal.compiler.util.Util;
import org.aspectj.org.eclipse.jdt.internal.core.index.*;
import org.aspectj.org.eclipse.jdt.internal.core.search.*;

/**
 * Internal search pattern implementation
 */
public abstract class InternalSearchPattern {

	/**
	 *  The focus element (used for reference patterns)
	 */
	IJavaElement focus;

	int kind;
	boolean mustResolve = true;
	
	void acceptMatch(String relativePath, String containerPath, SearchPattern pattern, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope) {

		if (scope instanceof JavaSearchScope) {
			JavaSearchScope javaSearchScope = (JavaSearchScope) scope;
			// Get document path access restriction from java search scope
			// Note that requestor has to verify if needed whether the document violates the access restriction or not
			AccessRuleSet access = javaSearchScope.getAccessRuleSet(relativePath, containerPath);
			if (access != JavaSearchScope.NOT_ENCLOSED) { // scope encloses the document path
				String documentPath = documentPath(containerPath, relativePath);
				if (!requestor.acceptIndexMatch(documentPath, pattern, participant, access)) 
					throw new OperationCanceledException();
			}
		} else {
			String documentPath = documentPath(containerPath, relativePath);
			if (scope.encloses(documentPath)) 
				if (!requestor.acceptIndexMatch(documentPath, pattern, participant, null)) 
					throw new OperationCanceledException();
			
		}
	}
	SearchPattern currentPattern() {
		return (SearchPattern) this;
	}
	String documentPath(String containerPath, String relativePath) {
		String separator = Util.isArchiveFileName(containerPath) ? IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR : "/"; //$NON-NLS-1$
		StringBuffer buffer = new StringBuffer(containerPath.length() + separator.length() + relativePath.length());
		buffer.append(containerPath);
		buffer.append(separator);
		buffer.append(relativePath);
		return buffer.toString();
	}
	/**
	 * Query a given index for matching entries. Assumes the sender has opened the index and will close when finished.
	 */
	void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor monitor) throws IOException {
		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
		try {
			index.startQuery();
			SearchPattern pattern = currentPattern();
			EntryResult[] entries = ((InternalSearchPattern)pattern).queryIn(index);
			if (entries == null) return;
		
			SearchPattern decodedResult = pattern.getBlankPattern();
			String containerPath = index.containerPath;
			for (int i = 0, l = entries.length; i < l; i++) {
				if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
		
				EntryResult entry = entries[i];
				decodedResult.decodeIndexKey(entry.getWord());
				if (pattern.matchesDecodedKey(decodedResult)) {
					// TODO (kent) some clients may not need the document names
					String[] names = entry.getDocumentNames(index);
					for (int j = 0, n = names.length; j < n; j++)
						acceptMatch(names[j], containerPath, decodedResult, requestor, participant, scope);
				}
			}
		} finally {
			index.stopQuery();
		}
	}
	boolean isPolymorphicSearch() {
		return false;
	}
	EntryResult[] queryIn(Index index) throws IOException {
		SearchPattern pattern = (SearchPattern) this;
		return index.query(pattern.getIndexCategories(), pattern.getIndexKey(), pattern.getMatchRule());
	}

}
