package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:58:23 PM
 */
public class CheckedExpression extends PrimaryExpression
	{
		public CheckedExpression()
		{
		}
		public CheckedExpression(ExpressionNode expression)
		{
			this.Expression = expression;
		}

		private ExpressionNode Expression;


		public void ToSource(StringBuilder sb)
		{
			sb.append("checked(");
			Expression.ToSource(sb);
			sb.append(")");
		}
	}
