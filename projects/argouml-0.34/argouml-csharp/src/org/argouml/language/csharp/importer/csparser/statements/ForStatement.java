package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 12:53:14 PM
 */
public class ForStatement extends StatementNode
	{
		public NodeCollection<ExpressionNode> Init = new NodeCollection<ExpressionNode>();

		public NodeCollection<ExpressionNode> Test = new NodeCollection<ExpressionNode>();

		public NodeCollection<ExpressionNode> Inc = new NodeCollection<ExpressionNode>();


		public BlockStatement Statements = new BlockStatement();


        public  void ToSource(StringBuilder sb)
        {
			sb.append("for(");
			Init.ToSource(sb);
			sb.append("; ");
			Test.ToSource(sb);
			sb.append("; ");
			Inc.ToSource(sb);
			sb.append(")");
			this.NewLine(sb);
			Statements.ToSource(sb);

        }

	}
