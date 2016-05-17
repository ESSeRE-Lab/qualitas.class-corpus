package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.members.ParamDeclNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:51:43 AM
 */
public class ForEachStatement extends StatementNode
	{
		public ParamDeclNode Iterator;

		public ExpressionNode Collection;


		public BlockStatement Statements = new BlockStatement();


        public  void ToSource(StringBuilder sb)
        {

        }

	}