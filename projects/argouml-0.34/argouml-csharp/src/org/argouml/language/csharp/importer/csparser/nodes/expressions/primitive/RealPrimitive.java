package org.argouml.language.csharp.importer.csparser.nodes.expressions.primitive;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:04:34 PM
 */
public class RealPrimitive extends LiteralNode
	{
		private String originalString;
		public RealPrimitive(String value)
		{
			this.originalString = value;
			char c = value.charAt(value.length()-1);
			switch(c)
			{
				case 'f':
				case 'F':
					IsFloat = true;
					value = value.substring(0, value.length() - 1);
					val = (double)Double.parseDouble(value);
					break;
				case 'd':
				case 'D':
					IsDouble = true;
					value = value.substring(0, value.length() - 1);
					val = Double.parseDouble(value);
					break;
				case 'm':
				case 'M':
					IsDecimal = true;
					value = value.substring(0, value.length() - 1);
					val =Double.parseDouble(value);
					break;
				default:
					val = Double.parseDouble(value);
					break;
			}
		}
		public RealPrimitive(double value)
		{
			IsDouble = true;
			this.val = value;
		}

		private double val;
		public double getValue()
		{
			return this.val;
		}
		private boolean IsFloat = false;

		private boolean IsDouble;


		private boolean IsDecimal;



		public void ToSource(StringBuilder sb)
		{
			sb.append(val);
			if (IsFloat)
			{
				sb.append("f");
			}
			else if (IsDouble)
			{
				sb.append("d");
			}
			else if (IsDecimal)
			{
				sb.append("m");
			}
		}
	}