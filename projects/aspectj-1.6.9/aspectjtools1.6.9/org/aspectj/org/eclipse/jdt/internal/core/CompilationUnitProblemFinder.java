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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.aspectj.org.eclipse.jdt.core.*;
import org.aspectj.org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.*;
import org.aspectj.org.eclipse.jdt.internal.compiler.Compiler;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.aspectj.org.eclipse.jdt.internal.core.util.CommentRecorderParser;
import org.aspectj.org.eclipse.jdt.internal.core.util.Util;

/**
 * Responsible for resolving types inside a compilation unit being reconciled,
 * reporting the discovered problems to a given IProblemRequestor.
 */
public class CompilationUnitProblemFinder extends Compiler {

	/**
	 * Answer a new CompilationUnitVisitor using the given name environment and compiler options.
	 * The environment and options will be in effect for the lifetime of the compiler.
	 * When the compiler is run, compilation results are sent to the given requestor.
	 *
	 *  @param environment org.aspectj.org.eclipse.jdt.internal.compiler.api.env.INameEnvironment
	 *      Environment used by the compiler in order to resolve type and package
	 *      names. The name environment implements the actual connection of the compiler
	 *      to the outside world (e.g. in batch mode the name environment is performing
	 *      pure file accesses, reuse previous build state or connection to repositories).
	 *      Note: the name environment is responsible for implementing the actual classpath
	 *            rules.
	 *
	 *  @param policy org.aspectj.org.eclipse.jdt.internal.compiler.api.problem.IErrorHandlingPolicy
	 *      Configurable part for problem handling, allowing the compiler client to
	 *      specify the rules for handling problems (stop on first error or accumulate
	 *      them all) and at the same time perform some actions such as opening a dialog
	 *      in UI when compiling interactively.
	 *      @see org.aspectj.org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies
	 * 
	 *	@param compilerOptions The compiler options to use for the resolution.
	 *      
	 *  @param requestor org.aspectj.org.eclipse.jdt.internal.compiler.api.ICompilerRequestor
	 *      Component which will receive and persist all compilation results and is intended
	 *      to consume them as they are produced. Typically, in a batch compiler, it is 
	 *      responsible for writing out the actual .class files to the file system.
	 *      @see org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult
	 *
	 *  @param problemFactory org.aspectj.org.eclipse.jdt.internal.compiler.api.problem.IProblemFactory
	 *      Factory used inside the compiler to create problem descriptors. It allows the
	 *      compiler client to supply its own representation of compilation problems in
	 *      order to avoid object conversions. Note that the factory is not supposed
	 *      to accumulate the created problems, the compiler will gather them all and hand
	 *      them back as part of the compilation unit result.
	 */
	protected CompilationUnitProblemFinder(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		CompilerOptions compilerOptions,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory) {

		super(environment,
			policy,
			compilerOptions,
			requestor,
			problemFactory
		);
	}

	/**
	 * Add additional source types
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		// ensure to jump back to toplevel type for first one (could be a member)
//		while (sourceTypes[0].getEnclosingType() != null)
//			sourceTypes[0] = sourceTypes[0].getEnclosingType();

		CompilationResult result =
			new CompilationResult(sourceTypes[0].getFileName(), 1, 1, this.options.maxProblemsPerUnit);

		// need to hold onto this
		CompilationUnitDeclaration unit =
			SourceTypeConverter.buildCompilationUnit(
				sourceTypes,//sourceTypes[0] is always toplevel here
				SourceTypeConverter.FIELD_AND_METHOD // need field and methods
				| SourceTypeConverter.MEMBER_TYPE // need member types
				| SourceTypeConverter.FIELD_INITIALIZATION, // need field initialization
				this.lookupEnvironment.problemReporter,
				result);

		if (unit != null) {
			this.lookupEnvironment.buildTypeBindings(unit, accessRestriction);
			this.lookupEnvironment.completeTypeBindings(unit);
		}
	}

	protected static CompilerOptions getCompilerOptions(Map settings, boolean creatingAST, boolean statementsRecovery) {
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		compilerOptions.performMethodsFullRecovery = statementsRecovery;
		compilerOptions.performStatementsRecovery = statementsRecovery;
		compilerOptions.parseLiteralExpressionsAsConstants = !creatingAST; /*parse literal expressions as constants only if not creating a DOM AST*/
		compilerOptions.storeAnnotations = creatingAST; /*store annotations in the bindings if creating a DOM AST*/
		return compilerOptions;
	}
	
	/*
	 *  Low-level API performing the actual compilation
	 */
	protected static IErrorHandlingPolicy getHandlingPolicy() {
		return DefaultErrorHandlingPolicies.proceedWithAllProblems();
	}

	/*
	 * Answer the component to which will be handed back compilation results from the compiler
	 */
	protected static ICompilerRequestor getRequestor() {
		return new ICompilerRequestor() {
			public void acceptResult(CompilationResult compilationResult) {
				// default requestor doesn't handle compilation results back
			}
		};
	}

	public static CompilationUnitDeclaration process(
		CompilationUnitDeclaration unit,
		ICompilationUnit unitElement, 
		char[] contents,
		Parser parser,
		WorkingCopyOwner workingCopyOwner,
		HashMap problems,
		boolean creatingAST,
		int reconcileFlags,
		IProgressMonitor monitor)
		throws JavaModelException {

		JavaProject project = (JavaProject) unitElement.getJavaProject();
		CancelableNameEnvironment environment = null;
		CancelableProblemFactory problemFactory = null;
		CompilationUnitProblemFinder problemFinder = null;
		try {
			environment = new CancelableNameEnvironment(project, workingCopyOwner, monitor);
			problemFactory = new CancelableProblemFactory(monitor);
			problemFinder = new CompilationUnitProblemFinder(
				environment,
				getHandlingPolicy(),
				getCompilerOptions(project.getOptions(true), creatingAST, ((reconcileFlags & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0)),
				getRequestor(),
				problemFactory);
			if (parser != null) {
				problemFinder.parser = parser;
			}
			PackageFragment packageFragment = (PackageFragment)unitElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
			char[][] expectedPackageName = null;
			if (packageFragment != null){
				expectedPackageName = Util.toCharArrays(packageFragment.names);
			}
			if (unit == null) {
				unit = problemFinder.resolve(
					new BasicCompilationUnit(
						contents,
						expectedPackageName,
						unitElement.getPath().toString(),
						unitElement),
					true, // verify methods
					true, // analyze code
					true); // generate code
			} else {
				problemFinder.resolve(
					unit,
					null, // no need for source
					true, // verify methods
					true, // analyze code
					true); // generate code
			}
			CompilationResult unitResult = unit.compilationResult;
			CategorizedProblem[] unitProblems = unitResult.getProblems();
			int length = unitProblems == null ? 0 : unitProblems.length;
			if (length > 0) {
				CategorizedProblem[] categorizedProblems = new CategorizedProblem[length];
				System.arraycopy(unitProblems, 0, categorizedProblems, 0, length);
				problems.put(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, categorizedProblems);
			}
			unitProblems = unitResult.getTasks();
			length = unitProblems == null ? 0 : unitProblems.length;
			if (length > 0) {
				CategorizedProblem[] categorizedProblems = new CategorizedProblem[length];
				System.arraycopy(unitProblems, 0, categorizedProblems, 0, length);
				problems.put(IJavaModelMarker.TASK_MARKER, categorizedProblems);
			}
			if (NameLookup.VERBOSE) {
				System.out.println(Thread.currentThread() + " TIME SPENT in NameLoopkup#seekTypesInSourcePackage: " + environment.nameLookup.timeSpentInSeekTypesInSourcePackage + "ms");  //$NON-NLS-1$ //$NON-NLS-2$
				System.out.println(Thread.currentThread() + " TIME SPENT in NameLoopkup#seekTypesInBinaryPackage: " + environment.nameLookup.timeSpentInSeekTypesInBinaryPackage + "ms");  //$NON-NLS-1$ //$NON-NLS-2$
			}
			return unit;
		} catch (OperationCanceledException e) {
			throw e;
		} catch(RuntimeException e) { 
			// avoid breaking other tools due to internal compiler failure (40334)
			String lineDelimiter = unitElement.findRecommendedLineSeparator();
			StringBuffer message = new StringBuffer("Exception occurred during problem detection:");  //$NON-NLS-1$ 
			message.append(lineDelimiter);
			message.append("----------------------------------- SOURCE BEGIN -------------------------------------"); //$NON-NLS-1$
			message.append(lineDelimiter);
			message.append(contents);
			message.append(lineDelimiter);
			message.append("----------------------------------- SOURCE END -------------------------------------"); //$NON-NLS-1$
			Util.log(e, message.toString());
			throw new JavaModelException(e, IJavaModelStatusConstants.COMPILER_FAILURE);
		} finally {
			if (environment != null)
				environment.monitor = null; // don't hold a reference to this external object
			if (problemFactory != null)
				problemFactory.monitor = null; // don't hold a reference to this external object
			// NB: unit.cleanUp() is done by caller
			if (problemFinder != null && !creatingAST)
				problemFinder.lookupEnvironment.reset();		
		}
	}

	public static CompilationUnitDeclaration process(
		ICompilationUnit unitElement, 
		char[] contents,
		WorkingCopyOwner workingCopyOwner,
		HashMap problems,
		boolean creatingAST,
		int reconcileFlags,
		IProgressMonitor monitor)
		throws JavaModelException {
			
		return process(null/*no CompilationUnitDeclaration*/, unitElement, contents, null/*use default Parser*/, workingCopyOwner, problems, creatingAST, reconcileFlags, monitor);
	}

	/* (non-Javadoc)
	 * Fix for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=60689.
	 * @see org.aspectj.org.eclipse.jdt.internal.compiler.Compiler#initializeParser()
	 */
	public void initializeParser() {
		this.parser = new CommentRecorderParser(this.problemReporter, this.options.parseLiteralExpressionsAsConstants);
	}
}	

