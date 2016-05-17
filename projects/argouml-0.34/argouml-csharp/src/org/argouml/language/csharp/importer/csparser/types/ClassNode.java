package org.argouml.language.csharp.importer.csparser.types;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;
import org.argouml.language.csharp.importer.csparser.members.*;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:27:14 PM
 */
public class ClassNode extends BaseNode
	{
		public long Modifiers;


		public IdentifierExpression Name;


		public List<TypeNode> BaseClasses =new ArrayList<TypeNode>();



		public NodeCollection<ConstantNode> Constants =new NodeCollection<ConstantNode>();

		public NodeCollection<FieldNode> Fields =new NodeCollection<FieldNode>();


		public NodeCollection<PropertyNode> Properties =new NodeCollection<PropertyNode>();

		public NodeCollection<ConstructorNode> Constructors =new NodeCollection<ConstructorNode>();


		public NodeCollection<DestructorNode> Destructors =new NodeCollection<DestructorNode>();


		public NodeCollection<MethodNode> Methods =new NodeCollection<MethodNode>();


		public NodeCollection<IndexerNode> Indexers =new NodeCollection<IndexerNode>();


		public NodeCollection<EventNode> Events = new NodeCollection<EventNode>();

		//private NodeCollection<FieldNode> members;
		//public NodeCollection<FieldNode> Members
		//{
		//    get { return members; }
		//    set { members = value; }
        //}

		public  void ToSource(StringBuilder sb)
		{
			WriteLocalSource(sb, "class");
		}
        protected void WriteLocalSource(StringBuilder sb, String kind)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			TraceModifiers(Modifiers, sb);

			sb.append(kind + " ");
			Name.ToSource(sb);
			sb.append(" ");

			if (BaseClasses != null && BaseClasses.size() > 1)
			{
				sb.append(": ");
				String comma = "";
				for(TypeNode bcls:BaseClasses)
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

			if(Constants != null)
			{
				Constants.ToSource(sb);
			}
			if(Fields != null)
			{
				Fields.ToSource(sb);
			}
			if(Properties != null)
			{
				Properties.ToSource(sb);
			}
			if (Constructors != null)
			{
				Constructors.ToSource(sb);
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
			if (Destructors != null)
			{
				Destructors.ToSource(sb);
			}

			indent--;
			this.NewLine(sb);
			sb.append("}");
			this.NewLine(sb);

        }

	}
