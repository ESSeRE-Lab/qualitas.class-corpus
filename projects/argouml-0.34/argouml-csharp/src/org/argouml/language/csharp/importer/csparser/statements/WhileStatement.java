package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:13:29 PM
 */
public class WhileStatement extends StatementNode
	{
		public ExpressionNode Test;
        
        public BlockStatement Statements = new BlockStatement();

        public void ToSource(StringBuilder sb)
        {

        }

	}
