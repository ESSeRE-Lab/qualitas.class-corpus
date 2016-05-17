package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:50:02 AM
 */
public class ExpressionStatement extends StatementNode
	{
		public ExpressionStatement()
		{
		}
		public ExpressionStatement(ExpressionNode expression)
		{
			this.Expression = expression;
		}

		private ExpressionNode Expression;


		public void ToSource(StringBuilder sb)
		{
			Expression.ToSource(sb);
			sb.append(";");
			this.NewLine(sb);
		}
	}