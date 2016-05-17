package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:49:02 AM
 */
public class DoStatement extends StatementNode
	{
		public ExpressionNode Test;


		public BlockStatement Statements = new BlockStatement();


        public  void ToSource(StringBuilder sb)
        {

        }

	}
