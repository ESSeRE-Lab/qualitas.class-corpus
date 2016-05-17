package org.argouml.language.csharp.importer.csparser.nodes.expressions.primitive;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:03:53 PM
 */
public class NullPrimitive extends LiteralNode
	{
		public void ToSource(StringBuilder sb)
		{
			sb.append("null");
		}
	}
