package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:34:28 AM
 */
public class OperatorNode extends MemberNode
	{
		public OperatorNode()
		{
		}

		public int operator;


		public boolean isExplicit;

		public boolean isImplicit;


		public ParamDeclNode param1;

		public ParamDeclNode param2;


		public BlockStatement statements = new BlockStatement();


        public void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.modifiers, sb);

			if (isExplicit)
			{
				sb.append("explicit operator ");
				type.ToSource(sb);
			}
			else if (isImplicit)
			{
				sb.append("implicit operator ");
				type.ToSource(sb);
			}
			else
			{
				type.ToSource(sb);
				sb.append("operator " + operator + " ");
			}

			sb.append("(");
			if (param1 != null)
			{
				param1.ToSource(sb);
			}
			if (param2 != null)
			{
				sb.append(", ");
				param2.ToSource(sb);
			}
			sb.append(")");
			this.NewLine(sb);

			if (statements != null)
			{
				statements.ToSource(sb);
			}
			else
			{
				sb.append("{}");
			}

        }
	}