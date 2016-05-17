package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:32:31 AM
 */
public class MethodNode extends MemberNode
	{
		public NodeCollection<ParamDeclNode> params =new NodeCollection<ParamDeclNode>();


		public BlockStatement statementBlock = new BlockStatement();


		public  void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.modifiers, sb);

			this.type.ToSource(sb);
			sb.append(" ");

			this.names.get(0).ToSource(sb);
			sb.append("(");

			if (params != null)
			{
				String comma = "";
				for (int i = 0; i < params.size(); i++)
				{
					sb.append(comma);
					comma = ", ";
					params.get(i).ToSource(sb);
				}
			}

			sb.append(")");
			this.NewLine(sb);

			statementBlock.ToSource(sb);
		}
	}