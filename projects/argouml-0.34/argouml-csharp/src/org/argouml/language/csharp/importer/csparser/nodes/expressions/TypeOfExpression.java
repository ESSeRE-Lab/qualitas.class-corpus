package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:18:34 PM
 */
public class TypeOfExpression extends PrimaryExpression
	{
		public TypeOfExpression()
		{
		}
		public TypeOfExpression(ExpressionNode expression)
		{
			this.Expression = expression;
		}

		private ExpressionNode Expression;


		public void ToSource(StringBuilder sb)
		{
			sb.append("typeof(");
			Expression.ToSource(sb);
			sb.append(")");
		}
	}
