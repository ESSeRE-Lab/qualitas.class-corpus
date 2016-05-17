package org.argouml.language.csharp.importer.csparser.preprocessornodes;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:39:23 AM
 */
public class PPDefineNode extends PPNode
	{
		public PPDefineNode()
		{
		}
		public PPDefineNode(IdentifierExpression identifier)
		{
			this.Identifier = identifier;
		}
		public IdentifierExpression Identifier;

	}
