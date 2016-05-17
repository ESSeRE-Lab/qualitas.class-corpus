/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;

import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.IfPointcut;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * (formals*): ... if(expr) ...
 * 
 * generates the following: public static final boolean ajc$if_N(formals*, [thisJoinPoints as needed]) { return expr; }
 * 
 * Here's the complicated bit, it deals with cflow: (a): ... this(a) && cflow(if (a == foo)) is an error. The way we capture this
 * is: We generate the ajc$if method with an (a) parameter, we let eclipse do the proper name binding. We then, as a post pass (that
 * we need to do anyway) look for the used parameters. If a is used, we signal an error because a was not one of the cflow
 * variables. XXX we'll do this part after we do cflow
 * 
 * The IfPointcut pcd then generates itself always as a dynamic test, it has to get the right parameters through any named pointcut
 * references...
 */
public class IfPseudoToken extends PseudoToken {
	public Expression expr;
	public MethodDeclaration testMethod;
	private IfPointcut pointcut;

	public IfPseudoToken(Parser parser, Expression expr) {
		super(parser, "if", false);
		this.expr = expr;
	}

	public Pointcut maybeGetParsedPointcut() {
		if (expr instanceof FalseLiteral) {
			return IfPointcut.makeIfFalsePointcut(Pointcut.SYMBOLIC);
		} else if (expr instanceof TrueLiteral) {
			return IfPointcut.makeIfTruePointcut(Pointcut.SYMBOLIC);
		} else {
			pointcut = new IfPointcut(new ResolvedMemberImpl(Member.METHOD, UnresolvedType.OBJECT, 0, "if_", "()V"), 0);
		}
		return pointcut;

	}

	/**
	 * enclosingDec is either AdviceDeclaration or PointcutDeclaration
	 */
	public void postParse(TypeDeclaration typeDec, MethodDeclaration enclosingDec) {
		// typeDec.scope.problemReporter().signalError(sourceStart, sourceEnd,
		// "if pcd is not implemented in 1.1alpha1");
		// XXX need to implement correctly
		if (pointcut == null)
			return;

		testMethod = makeMethod(enclosingDec.compilationResult, enclosingDec);
		AstUtil.addMethodDeclaration(typeDec, testMethod);
	}

	// XXX todo: make sure that errors in Arguments only get displayed once
	private MethodDeclaration makeMethod(CompilationResult result, MethodDeclaration enclosingDec) {
		MethodDeclaration ret = new IfMethodDeclaration(result, pointcut);
		ret.modifiers = ClassFileConstants.AccStatic | ClassFileConstants.AccFinal | ClassFileConstants.AccPublic;
		ret.returnType = AstUtil.makeTypeReference(TypeBinding.BOOLEAN);
		// create a more stable name 277508
		StringBuffer ifSelector = new StringBuffer();
		ifSelector.append("ajc$if$");
		ifSelector.append(Integer.toHexString(expr.sourceStart));

		// possibly even better logic for more reliable name:
		// if (enclosingDec instanceof AdviceDeclaration) {
		// // name is ajc$if$<adviceSequenceNumber>$<hashcodeOfIfExpressionInHex>
		// ifSelector.append("ajc$if$");
		// ifSelector.append(((AdviceDeclaration) enclosingDec).adviceSequenceNumberInType);
		// ifSelector.append("$").append(Integer.toHexString(expr.toString().hashCode()));
		// } else if (enclosingDec instanceof PointcutDeclaration) {
		// // name is pointcut selector then $if$<hashcodeOfIfExpressionInHex>
		// ifSelector.append(((PointcutDeclaration) enclosingDec).selector);
		// ifSelector.append("$if$");
		// ifSelector.append(Integer.toHexString(expr.toString().hashCode()));
		// } else {
		// throw new BCException("Unexpected enclosing declaration of " + enclosingDec + " for if pointcut designator");
		// }
		// hashcode of expression
		ret.selector = ifSelector.toString().toCharArray();
		ret.arguments = makeArguments(enclosingDec);
		ret.statements = new Statement[] { new ReturnStatement(expr, expr.sourceStart, expr.sourceEnd) };
		return ret;
	}

	private Argument[] makeArguments(MethodDeclaration enclosingDec) {
		Argument[] baseArguments = enclosingDec.arguments;
		int len = baseArguments.length;
		if (enclosingDec instanceof AdviceDeclaration) {
			len = ((AdviceDeclaration) enclosingDec).baseArgumentCount;
		}

		Argument[] ret = new Argument[len];
		for (int i = 0; i < len; i++) {
			Argument a = baseArguments[i];
			ret[i] = new Argument(a.name, AstUtil.makeLongPos(a.sourceStart, a.sourceEnd), a.type, Modifier.FINAL);
		}
		ret = AdviceDeclaration.addTjpArguments(ret);

		return ret;
	}

}
