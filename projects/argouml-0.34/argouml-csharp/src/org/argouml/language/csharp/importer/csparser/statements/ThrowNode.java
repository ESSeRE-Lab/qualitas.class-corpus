package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:08:37 PM
 */
public class ThrowNode extends StatementNode
	{
		public ExpressionNode ThrowExpression;


        public  void ToSource(StringBuilder sb)
        {
			sb.append("throw ");
			if (ThrowExpression != null)
			{
				ThrowExpression.ToSource(sb);
			}
			sb.append(";");
        }
	}
