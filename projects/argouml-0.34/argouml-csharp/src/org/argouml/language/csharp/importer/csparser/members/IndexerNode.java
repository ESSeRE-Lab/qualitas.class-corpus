package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:19:10 AM
 */
public class IndexerNode extends MemberNode
	{
		public TypeNode interfaceType;


		public NodeCollection<ParamDeclNode> params =new NodeCollection<ParamDeclNode>();

		public AccessorNode getter;


		public AccessorNode setter;


        public  void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			TraceModifiers(modifiers, sb);

			type.ToSource(sb);
			sb.append(" ");
			if (interfaceType != null)
			{
				interfaceType.ToSource(sb);
				sb.append(".");
			}
			sb.append("this[");
			if (params != null)
			{
				String comma = "";
				for (ParamDeclNode pdn: params)
				{
					sb.append(comma);
					comma = ", ";
					pdn.ToSource(sb);
				}
			}
			sb.append("]");

			// start block
			this.NewLine(sb);
			sb.append("{");
			indent++;
			this.NewLine(sb);

			if (getter != null)
			{
				getter.ToSource(sb);
			}
			if (setter != null)
			{
				setter.ToSource(sb);
			}

			indent--;
			this.NewLine(sb);
			sb.append("}");
			this.NewLine(sb);
        }
	}
