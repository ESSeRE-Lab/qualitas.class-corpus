package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:15:29 PM
 */
public class ArgumentNode extends BaseNode
	{
		public Boolean isRef;


		public Boolean isOut;
		

		public ExpressionNode expression;

        public void ToSource(StringBuilder sb)
		{
			if (isRef)
			{
				sb.append("ref ");
			}
			else if (isOut)
			{
				sb.append("out ");
			}

			expression.ToSource(sb);

        }


	}
