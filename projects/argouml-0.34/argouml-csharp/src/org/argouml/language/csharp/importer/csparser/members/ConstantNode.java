package org.argouml.language.csharp.importer.csparser.members;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 8, 2008
 * Time: 11:07:47 PM
 */
public class ConstantNode extends MemberNode
    {
        public void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.modifiers, sb);
			sb.append("const ");

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
