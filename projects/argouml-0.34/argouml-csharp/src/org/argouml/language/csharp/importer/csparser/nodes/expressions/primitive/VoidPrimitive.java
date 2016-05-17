package org.argouml.language.csharp.importer.csparser.nodes.expressions.primitive;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:14:40 PM
 */
public class VoidPrimitive extends LiteralNode
	{
		public void ToSource(StringBuilder sb)
		{
			sb.append("void ");
		}
	}