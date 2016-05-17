package org.argouml.language.csharp.importer.csparser.members;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:37:27 AM
 */
public class PropertyNode extends MemberNode
	{
		public AccessorNode getter;


		public AccessorNode setter;

        public  void ToSource(StringBuilder sb)
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
			indent++;
			this.NewLine(sb);

			if (getter != null)
			{
				getter.ToSource(sb);
			}
			if (setter != null)
			{
				setter.ToSource(sb);
			}

			indent--;
			this.NewLine(sb);
			sb.append("}");
			this.NewLine(sb);
        }

	}