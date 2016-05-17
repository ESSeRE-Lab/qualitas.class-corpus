package org.argouml.language.csharp.importer.csparser.members;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:24:43 AM
 */
public class InterfaceEventNode extends MemberNode
	{
		public void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.modifiers, sb);

			sb.append("event ");
			this.type.ToSource(sb);

			sb.append(" ");
			this.names.get(0).ToSource(sb);

			sb.append(";");
		}
	}