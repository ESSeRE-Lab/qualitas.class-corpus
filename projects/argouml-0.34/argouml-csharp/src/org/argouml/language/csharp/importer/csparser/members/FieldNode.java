package org.argouml.language.csharp.importer.csparser.members;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:17:51 AM
 */
public class FieldNode extends MemberNode
	{
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

			String comma = "";
			for (int i = 0; i < this.names.size(); i++)
			{
				sb.append(comma);
				comma = ", ";
				this.names.get(i).ToSource(sb);
			}

			if (this.Value != null)
			{
				sb.append(" = ");
				this.Value.ToSource(sb);
			}

			sb.append(";");
			this.NewLine(sb);
        }
	}
