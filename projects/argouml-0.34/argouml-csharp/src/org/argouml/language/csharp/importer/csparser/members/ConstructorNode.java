package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ArgumentNode;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 8, 2008
 * Time: 11:10:45 PM
 */
public class ConstructorNode extends MemberNode
	{
		public boolean hasThis;


		public boolean hasBase;


		public NodeCollection<ArgumentNode> thisBaseArgs;

		public NodeCollection<ParamDeclNode> params;

		public BlockStatement statementBlock = new BlockStatement();


		public boolean isStaticConstructor = false;


        public  void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.modifiers, sb);

			if (isStaticConstructor)
			{
				sb.append("static ");
			}

			this.names.get(0).ToSource(sb);
			sb.append("(");

			String comma = "";
			if (params != null)
			{
				for (int i = 0; i < params.size(); i++)
				{
					sb.append(comma);
					comma = ", ";
					params.get(i).ToSource(sb);
				}
			}
			sb.append(")");

			// possible :this or :base
			if (hasBase)
			{
				sb.append(" : base(");
			}
			else if (hasThis)
			{
				sb.append(" : this(");
			}
			if (hasBase || hasThis)
			{
				if (thisBaseArgs != null)
				{
					comma = "";
					for (int i = 0; i < thisBaseArgs.size(); i++)
					{
						sb.append(comma);
						comma = ", ";
						thisBaseArgs.get(i).ToSource(sb);
					}
				}
				sb.append(")");
			}

			// start block
			this.NewLine(sb);

			statementBlock.ToSource(sb);

        }

	}
