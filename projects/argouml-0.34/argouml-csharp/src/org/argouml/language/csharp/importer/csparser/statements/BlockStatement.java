package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:09:22 AM
 */
public class BlockStatement extends StatementNode
	{


        private boolean hasBraces = true;


        public void setHasBraces(boolean val){
            hasBraces=val;
        }
        public boolean isHasBraces() {
            if(Statements.size() != 1)
                hasBraces=true;
            return hasBraces;
        }


        public NodeCollection<StatementNode> Statements = new NodeCollection<StatementNode>();


        public void ToSource(StringBuilder sb)
        {
			if (hasBraces)
			{
				sb.append("{");
				indent++;
				this.NewLine(sb);
			}
			else if(Statements.size() == 1)
			{
				// only a case stmt can have more than one stmt without braces, and it special cases this
				AddTab(sb);
			}

			if (Statements != null)
			{
				Statements.ToSource(sb);
			}

			if (hasBraces)
			{
				indent--;
				this.NewLine(sb);
				sb.append("}");
			}
			this.NewLine(sb);
        }
	}