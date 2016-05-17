package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:45:57 AM
 */
public class CatchNode extends StatementNode
	{
		public TypeNode ClassType;


		public IdentifierExpression Identifier;

		public BlockStatement CatchBlock = new BlockStatement();

        public void ToSource(StringBuilder sb)
		{
			sb.append("catch");
			if (ClassType != null)
			{
				sb.append("(");
				ClassType.ToSource(sb);
				sb.append(")");
			}
			this.NewLine(sb);
			CatchBlock.ToSource(sb);
        }
	}
