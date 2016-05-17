package org.argouml.language.csharp.importer.csparser.types;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:35:28 PM
 */
public class EnumNode extends BaseNode
	{
		public long Modifiers;


		public IdentifierExpression Name;


		public TypeNode BaseClass;


		public ExpressionNode Value;


		public  void ToSource(StringBuilder sb)
		{
			//todo: enumNode to source

			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			// todo: enum members can have attributes
		}
	}
