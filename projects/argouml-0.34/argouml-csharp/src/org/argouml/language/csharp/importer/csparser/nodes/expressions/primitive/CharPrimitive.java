package org.argouml.language.csharp.importer.csparser.nodes.expressions.primitive;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 5:19:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class CharPrimitive extends LiteralNode
	{
		public CharPrimitive(char value)
		{
			this.Value = value;
		}
		public CharPrimitive(String value)
		{
			if (value.length() == 1)
			{
				this.Value = value.charAt(0);
			}
			else
			{
				//throw new Exception("char primitive Value is not a char");
			}
		}

		private char Value;


		public void ToSource(StringBuilder sb)
		{
			sb.append("'" + Value + "'");
		}
	}
