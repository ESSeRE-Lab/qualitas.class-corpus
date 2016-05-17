package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:05:00 PM
 */
public class LockStatement extends StatementNode
	{
		public ExpressionNode Target;

		public BlockStatement Statements = new BlockStatement();


        public  void ToSource(StringBuilder sb)
        {
			sb.append("lock(");
			Target.ToSource(sb);
			sb.append(")");
			Statements.ToSource(sb);
        }
	}
