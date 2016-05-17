package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:12:13 PM
 */
public class PostDecrementExpression extends PrimaryExpression
	{
		public PostDecrementExpression()
		{
		}
		public PostDecrementExpression(ExpressionNode expression)
		{
			this.Expression = expression;
		}

		private ExpressionNode Expression;


		public void ToSource(StringBuilder sb)
		{
			Expression.ToSource(sb);
			sb.append("--");
		}
	}
