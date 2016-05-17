package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:16:26 AM
 */
public class EventNode extends MemberNode
	{
		public BlockStatement addBlock = new BlockStatement();

		public BlockStatement removeBlock = new BlockStatement();


        public void ToSource(StringBuilder sb)
        {
            // todo: eventnode to source

			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
        }
	}
