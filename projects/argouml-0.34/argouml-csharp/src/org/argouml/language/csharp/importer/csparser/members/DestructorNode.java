package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:07:51 AM
 */
public class DestructorNode extends MemberNode
	{
		public BlockStatement statementBlock = new BlockStatement();


		public void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.modifiers, sb);

			sb.append("~");
			this.names.get(0).ToSource(sb);
			sb.append("()");
			this.NewLine(sb);

			statementBlock.ToSource(sb);
		}

	}
