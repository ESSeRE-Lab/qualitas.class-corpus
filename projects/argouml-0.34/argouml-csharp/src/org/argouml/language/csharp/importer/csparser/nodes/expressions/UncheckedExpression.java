package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:24:08 PM
 */
public class UncheckedExpression extends PrimaryExpression
	{
		public UncheckedExpression()
		{
		}
		public UncheckedExpression(ExpressionNode expression)
		{
			this.Expression = expression;
		}

		private ExpressionNode Expression;


		public void ToSource(StringBuilder sb)
		{
			sb.append("unchecked(");
			Expression.ToSource(sb);
			sb.append(")");
		}
	}
