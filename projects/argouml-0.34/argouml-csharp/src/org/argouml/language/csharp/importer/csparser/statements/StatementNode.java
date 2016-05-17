package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:12:28 AM
 */
public class StatementNode extends BaseNode
    {
        public void ToSource(StringBuilder sb)
        {
			sb.append(";");
			this.NewLine(sb);
        }
	}
