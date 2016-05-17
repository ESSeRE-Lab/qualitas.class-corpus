package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:09:34 PM
 */
public class TryStatement extends StatementNode
	{
		public BlockStatement TryBlock = new BlockStatement();

		public NodeCollection<CatchNode> CatchBlocks = new NodeCollection<CatchNode>();

		public FinallyNode FinallyBlock;


		public  void ToSource(StringBuilder sb)
		{
			sb.append("try");
			this.NewLine(sb);
			TryBlock.ToSource(sb);
			for(CatchNode cb:CatchBlocks)
			{
				cb.ToSource(sb);
			}

			if (FinallyBlock != null)
			{
				FinallyBlock.ToSource(sb);
			}
		}
	}
