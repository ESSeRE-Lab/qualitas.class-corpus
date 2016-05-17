package org.argouml.language.csharp.importer.csparser.types;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;
import org.argouml.language.csharp.importer.csparser.members.InterfaceMethodNode;
import org.argouml.language.csharp.importer.csparser.members.InterfacePropertyNode;
import org.argouml.language.csharp.importer.csparser.members.InterfaceIndexerNode;
import org.argouml.language.csharp.importer.csparser.members.InterfaceEventNode;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:36:57 PM
 */
public class InterfaceNode extends BaseNode
	{
		public long Modifiers;

		public IdentifierExpression Name;

		public List<TypeNode> BaseClasses =new ArrayList<TypeNode>();

		public NodeCollection<InterfaceMethodNode> Methods =new NodeCollection<InterfaceMethodNode>();

		public NodeCollection<InterfacePropertyNode> Properties =new NodeCollection<InterfacePropertyNode>();


		public NodeCollection<InterfaceIndexerNode> Indexers =new NodeCollection<InterfaceIndexerNode>();


		public NodeCollection<InterfaceEventNode> Events =new NodeCollection<InterfaceEventNode>();


		public  void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			TraceModifiers(Modifiers, sb);

			sb.append("interface ");
			Name.ToSource(sb);
			sb.append(" ");

			if (BaseClasses != null && BaseClasses.size() > 1)
			{
				sb.append(": ");
				String comma = "";
				for (TypeNode bcls:BaseClasses)
				{
					sb.append(comma);
					comma = ", ";
					bcls.ToSource(sb);
				}
			}

			this.NewLine(sb);
			sb.append("{");
			indent++;
			this.NewLine(sb);

			if (Properties != null)
			{
				Properties.ToSource(sb);
			}
			if (Methods != null)
			{
				Methods.ToSource(sb);
			}
			if (Indexers != null)
			{
				Indexers.ToSource(sb);
			}
			if (Events != null)
			{
				Events.ToSource(sb);
			}

			indent--;
			this.NewLine(sb);
			sb.append("}");
			this.NewLine(sb);

		}
	}
