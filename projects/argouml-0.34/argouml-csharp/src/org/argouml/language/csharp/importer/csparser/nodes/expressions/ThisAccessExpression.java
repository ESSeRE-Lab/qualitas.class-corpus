package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:17:32 PM
 */
public class ThisAccessExpression extends IdentifierExpression
	{
		public ThisAccessExpression()
		{
			this.Identifier = new String[]{"this"};
		}
		public  void ToSource(StringBuilder sb)
		{
			sb.append("this");
		}
	}
