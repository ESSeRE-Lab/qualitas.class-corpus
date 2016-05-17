package org.argouml.language.csharp.importer.csparser.structural;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:15:55 PM
 */
public class AttributeArgumentNode extends BaseNode
	{
		public IdentifierExpression ArgumentName;

		public ExpressionNode Expression;

		public  void ToSource(StringBuilder sb)
		{
			if (ArgumentName != null)
			{
				ArgumentName.ToSource(sb);
				sb.append("= ");
			}
			Expression.ToSource(sb);
		}
	}
