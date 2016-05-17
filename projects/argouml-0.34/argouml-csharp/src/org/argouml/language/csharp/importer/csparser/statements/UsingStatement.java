package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:11:44 PM
 */
public class UsingStatement extends StatementNode
	{
		public ExpressionNode Resource;

		public BlockStatement Statements = new BlockStatement();


        public  void ToSource(StringBuilder sb)
		{
			sb.append("using(");
			Resource.ToSource(sb);
			sb.append(")");
			Statements.ToSource(sb);
        }
	}
