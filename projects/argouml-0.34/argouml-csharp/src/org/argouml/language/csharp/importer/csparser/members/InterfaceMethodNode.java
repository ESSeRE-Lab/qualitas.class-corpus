package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:28:27 AM
 */
public class InterfaceMethodNode extends MemberNode
	{
		public NodeCollection<ParamDeclNode> params =new NodeCollection<ParamDeclNode>();

		public void ToSource(StringBuilder sb)
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

			sb.append(");");
		}
	}
