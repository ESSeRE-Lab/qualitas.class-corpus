package org.argouml.language.csharp.importer.csparser.collections;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 12:22:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionList extends ExpressionNode {
    public ExpressionList() {
    }

    private NodeCollection<ExpressionNode> expressions = new NodeCollection<ExpressionNode>();

    public NodeCollection<ExpressionNode> getExpressions() {
        return expressions;
    }

    public void ToSource(StringBuilder sb) {
        String comma = "";
        for(ExpressionNode node: expressions)
        {
            sb.append(comma);
            comma = ", ";
            node.ToSource(sb);
        }
    }
}