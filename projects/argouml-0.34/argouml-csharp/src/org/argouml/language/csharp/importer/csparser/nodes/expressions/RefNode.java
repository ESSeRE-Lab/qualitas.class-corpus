package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:16:39 PM
 */
public class RefNode extends ExpressionNode
	{
		public RefNode()
		{
		}
		public RefNode(ExpressionNode variableReference)
		{
			this.VariableReference = variableReference;
		}

		private ExpressionNode VariableReference;

		public  void ToSource(StringBuilder sb)
		{
			sb.append("ref ");
			VariableReference.ToSource(sb);
		}
	}
