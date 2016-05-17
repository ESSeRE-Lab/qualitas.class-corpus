package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:07:22 PM
 */
public class SwitchStatement extends StatementNode
	{
		public ExpressionNode Test;


		public NodeCollection<CaseNode> Cases = new NodeCollection<CaseNode>();


        public  void ToSource(StringBuilder sb)
		{
			sb.append("switch(");
			Test.ToSource(sb);
			sb.append(")");

			this.NewLine(sb);
			sb.append("{");
			indent++;

			for (int i = 0; i < Cases.size(); i++)
			{
				Cases.get(i).ToSource(sb);
			}
			indent--;
			this.NewLine(sb);
			sb.append("}");
			this.NewLine(sb);
        }
	}