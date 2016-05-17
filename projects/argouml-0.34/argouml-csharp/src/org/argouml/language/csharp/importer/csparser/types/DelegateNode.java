package org.argouml.language.csharp.importer.csparser.types;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;
import org.argouml.language.csharp.importer.csparser.members.ParamDeclNode;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:33:08 PM
 */
public class DelegateNode extends BaseNode
	{
		public long Modifiers;

		public TypeNode Type;


		public IdentifierExpression Name;


		public NodeCollection<ParamDeclNode> Params =new NodeCollection<ParamDeclNode>();


		public  void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			TraceModifiers(Modifiers, sb);

			sb.append("delegate ");
			Type.ToSource(sb);
			sb.append(" ");
			Name.ToSource(sb);

			sb.append("(");
			if (Params != null)
			{
				String comma = "";
				for(ParamDeclNode pdn:Params)
				{
					sb.append(comma);
					comma = ", ";
					pdn.ToSource(sb);
				}
			}
			sb.append(");");
			this.NewLine(sb);

		}
	}
