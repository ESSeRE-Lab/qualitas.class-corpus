package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 12:56:23 PM
 */
public class IfStatement extends StatementNode
	{
		public ExpressionNode Test;


		public BlockStatement Statements = new BlockStatement();

		public BlockStatement ElseStatements=new BlockStatement();

        public  void ToSource(StringBuilder sb)
        {
			sb.append("if(");
			if (Test != null)
			{
				Test.ToSource(sb);
			}
			sb.append(")");
			this.NewLine(sb);
			this.Statements.ToSource(sb);

			if (this.ElseStatements != null)
			{
				sb.append("else ");
				ElseStatements.ToSource(sb);
			}

        }

	}
