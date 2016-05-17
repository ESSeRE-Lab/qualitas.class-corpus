package org.argouml.language.csharp.importer.csparser.preprocessornodes;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:41:05 AM
 */
public class PPIfNode extends PPNode
	{
		public PPIfNode()
		{
		}
		public PPIfNode(ExpressionNode expression)
		{
			this.Expression = expression;
		}
		public ExpressionNode Expression;

	}
