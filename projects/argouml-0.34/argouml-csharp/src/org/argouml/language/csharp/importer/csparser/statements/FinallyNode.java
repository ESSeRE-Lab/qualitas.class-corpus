package org.argouml.language.csharp.importer.csparser.statements;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:50:51 AM
 */
public class FinallyNode extends StatementNode
	{
		public BlockStatement FinallyBlock = new BlockStatement();


        public  void ToSource(StringBuilder sb)
        {
			sb.append("finally");
			this.NewLine(sb);
			FinallyBlock.ToSource(sb);
        }
	}
