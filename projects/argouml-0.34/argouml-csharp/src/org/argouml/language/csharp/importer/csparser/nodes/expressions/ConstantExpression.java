package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:01:13 PM
 */
public class ConstantExpression extends BaseNode
	{
		public ExpressionNode Value;


        public void ToSource(StringBuilder sb)
        {
			if (Value != null)
			{
				Value.ToSource(sb);
			}
        }

	}
