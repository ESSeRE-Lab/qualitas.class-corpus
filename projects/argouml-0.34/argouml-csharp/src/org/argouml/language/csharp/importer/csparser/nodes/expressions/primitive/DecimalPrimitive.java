package org.argouml.language.csharp.importer.csparser.nodes.expressions.primitive;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 5:26:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DecimalPrimitive extends LiteralNode
	{
		public DecimalPrimitive(double value)
		{
			this.Value = value;
		}

		private double Value;


		public void ToSource(StringBuilder sb)
		{
			sb.append(Value + " ");
		}
	}
