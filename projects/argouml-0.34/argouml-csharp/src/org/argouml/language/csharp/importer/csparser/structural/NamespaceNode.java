package org.argouml.language.csharp.importer.csparser.structural;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.types.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:19:04 PM
 */
public class NamespaceNode extends BaseNode
	{
		public NodeCollection<NamespaceNode> Namespaces = new NodeCollection<NamespaceNode>();

		public NodeCollection<ClassNode> Classes = new NodeCollection<ClassNode>();


		public NodeCollection<EnumNode> Enums = new NodeCollection<EnumNode>();

		public NodeCollection<DelegateNode> Delegates = new NodeCollection<DelegateNode>();


		public NodeCollection<InterfaceNode> Interfaces = new NodeCollection<InterfaceNode>();


		public NodeCollection<StructNode> Structs = new NodeCollection<StructNode>();



		public IdentifierExpression Name;


        public  void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			if (Name != null)
			{
				sb.append("namespace ");
				Name.ToSource(sb);
				NewLine(sb);
				sb.append("{");
				indent++;
				NewLine(sb);
			}

			if (Namespaces != null)
				Namespaces.ToSource(sb);

			if(Interfaces != null)
				Interfaces.ToSource(sb);

			if (Classes != null)
				Classes.ToSource(sb);

			if (Structs != null)
				Structs.ToSource(sb);

			if (Delegates != null)
				Delegates.ToSource(sb);

			if (Enums != null)
				Enums.ToSource(sb);


			if (Name != null)
			{
				indent--;
				NewLine(sb);
				sb.append("}");
				NewLine(sb);
			}


        }

	}
