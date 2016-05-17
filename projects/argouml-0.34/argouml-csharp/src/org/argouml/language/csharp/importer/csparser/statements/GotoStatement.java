package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 12:55:06 PM
 */
public class GotoStatement extends StatementNode
	{
		public boolean IsCase = false;

		public boolean IsDefaultCase = false;


		public ExpressionNode Target;


        public  void ToSource(StringBuilder sb)
        {

        }
	}