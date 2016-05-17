package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:59:18 PM
 */
public class ConditionalExpression extends ExpressionNode
	{
		public ConditionalExpression()
		{
		}

		public ConditionalExpression(ExpressionNode test, ExpressionNode left, ExpressionNode right)
		{
			this.Test = test;
			this.Left = left;
			this.Right = right;
		}


		private ExpressionNode Test;


		protected ExpressionNode Left;


		protected ExpressionNode Right;
		

		public void ToSource(StringBuilder sb)
		{
			Test.ToSource(sb);
			sb.append(" ? ");
			Left.ToSource(sb);
			sb.append(" : ");
			Right.ToSource(sb);
		}
	}
