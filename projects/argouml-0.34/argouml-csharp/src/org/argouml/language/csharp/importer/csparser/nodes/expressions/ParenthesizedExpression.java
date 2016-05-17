package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:11:23 PM
 */
public class ParenthesizedExpression extends PrimaryExpression
	{
		public ParenthesizedExpression()
		{
		}
		public ParenthesizedExpression(ExpressionNode expression)
		{
			this.Expression = expression;
		}
		public ExpressionNode Expression;

		public void ToSource(StringBuilder sb)
		{
			sb.append("(");
			Expression.ToSource(sb);
			sb.append(")");
		}
	}
