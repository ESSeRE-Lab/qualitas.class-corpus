package org.argouml.language.csharp.importer.csparser.nodes.expressions.primitive;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:12:05 PM
 */
public class StringPrimitive extends LiteralNode
	{
		public StringPrimitive(String value)
		{
			this.Value = value;
		}

		private String Value;

		private boolean IsVerbatim = true; // Strings are always lexed as verbatim for now

		public void ToSource(StringBuilder sb)
		{
			if (Value != null)
			{
				if (IsVerbatim)
				{
					sb.append("@");
				}
				sb.append("\"" + Value + "\"");
			}
		}
	}
