package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:13:05 PM
 */
public class PostIncrementExpression extends PrimaryExpression
	{
		public PostIncrementExpression()
		{
		}
		public PostIncrementExpression(ExpressionNode expression)
		{
			this.Expression = expression;
		}

		private ExpressionNode Expression;


		public void ToSource(StringBuilder sb)
		{
			Expression.ToSource(sb);
			sb.append("++");
		}
	}
