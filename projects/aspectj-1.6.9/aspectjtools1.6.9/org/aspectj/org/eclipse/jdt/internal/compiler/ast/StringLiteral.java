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
package org.aspectj.org.eclipse.jdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class StringLiteral extends Literal {

	char[] source;
	int lineNumber;

	public StringLiteral(char[] token, int start, int end, int lineNumber) {

		this(start,end);
		this.source = token;
		this.lineNumber = lineNumber - 1; // line number is 1 based 
	}

	public StringLiteral(int s, int e) {

		super(s,e);
	}

	public void computeConstant() {
	
		constant = StringConstant.fromValue(String.valueOf(source));
	}

	public ExtendedStringLiteral extendWith(CharLiteral lit){

		//add the lit source to mine, just as if it was mine
		return new ExtendedStringLiteral(this,lit);
	}

	public ExtendedStringLiteral extendWith(StringLiteral lit){

		//add the lit source to mine, just as if it was mine
		return new ExtendedStringLiteral(this,lit);
	}

	/**
	 *  Add the lit source to mine, just as if it was mine
	 */
	public StringLiteralConcatenation extendsWith(StringLiteral lit) {
		return new StringLiteralConcatenation(this, lit);
	}
	/**
	 * Code generation for string literal
	 */ 
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

		int pc = codeStream.position;
		if (valueRequired)
			codeStream.ldc(constant.stringValue());
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public TypeBinding literalType(BlockScope scope) {

		return scope.getJavaLangString();
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {
	
		// handle some special char.....
		output.append('\"');
		for (int i = 0; i < source.length; i++) {
			switch (source[i]) {
				case '\b' :
					output.append("\\b"); //$NON-NLS-1$
					break;
				case '\t' :
					output.append("\\t"); //$NON-NLS-1$
					break;
				case '\n' :
					output.append("\\n"); //$NON-NLS-1$
					break;
				case '\f' :
					output.append("\\f"); //$NON-NLS-1$
					break;
				case '\r' :
					output.append("\\r"); //$NON-NLS-1$
					break;
				case '\"' :
					output.append("\\\""); //$NON-NLS-1$
					break;
				case '\'' :
					output.append("\\'"); //$NON-NLS-1$
					break;
				case '\\' : //take care not to display the escape as a potential real char
					output.append("\\\\"); //$NON-NLS-1$
					break;
				default :
					output.append(source[i]);
			}
		}
		output.append('\"'); 
		return output;
	}

	public char[] source() {

		return source;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
