package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 12:57:54 PM
 */
public class LabeledStatement extends StatementNode
	{
		public IdentifierExpression Name;


		public NodeCollection<StatementNode> Statements = new NodeCollection<StatementNode>();

		public void ToSource(StringBuilder sb)
		{
			Name.ToSource(sb);
			sb.append(" : ");
			Statements.get(0).ToSource(sb);
		}

	}
