package org.argouml.language.csharp.importer.csparser.members;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:30:32 AM
 */
public class InterfacePropertyNode extends MemberNode
	{
		public boolean hasGetter;

		public boolean hasSetter;


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

			this.names.get(0).ToSource(sb);

			// start block
			this.NewLine(sb);
			sb.append("{");

			if (hasGetter)
			{
				sb.append("get; ");
			}
			if (hasSetter)
			{
				sb.append("set; ");
			}

			sb.append("}");
			this.NewLine(sb);
		}
	}
