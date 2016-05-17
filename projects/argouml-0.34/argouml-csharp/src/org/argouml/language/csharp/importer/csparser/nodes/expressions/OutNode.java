package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:10:36 PM
 */
public class OutNode extends ExpressionNode
	{
		public OutNode()
		{
		}
		public OutNode(ExpressionNode variableReference)
		{
			this.VariableReference = variableReference;
		}

		private ExpressionNode VariableReference;


		public void ToSource(StringBuilder sb)
		{
			sb.append("out ");
			VariableReference.ToSource(sb);
		}
	}
