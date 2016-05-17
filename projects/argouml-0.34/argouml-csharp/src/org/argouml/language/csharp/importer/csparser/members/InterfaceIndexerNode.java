package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:25:22 AM
 */
public class InterfaceIndexerNode extends MemberNode
	{
		public NodeCollection<ParamDeclNode> params =new NodeCollection<ParamDeclNode>();


		public boolean hasGetter;

		public boolean hasSetter;


		public void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.modifiers, sb);

			this.type.ToSource(sb);
			sb.append("this [");
			if (params != null)
			{
				params.ToSource(sb);
			}
			sb.append("]{");
			if (hasGetter)
			{
				sb.append("get;");
			}
			if (hasSetter)
			{
				sb.append("set;");
			}
			sb.append("}");
		}
	}
