package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:06:03 PM
 */
public class ReturnStatement extends StatementNode
	{
		public ExpressionNode ReturnValue;


        public  void ToSource(StringBuilder sb)
		{
			sb.append("return");
			if (ReturnValue != null)
			{
				sb.append(" ");
				ReturnValue.ToSource(sb);
			}
			sb.append(";");
			this.NewLine(sb);
        }
	}
