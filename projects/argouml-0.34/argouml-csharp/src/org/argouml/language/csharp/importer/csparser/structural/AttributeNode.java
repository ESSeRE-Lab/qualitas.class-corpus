package org.argouml.language.csharp.importer.csparser.structural;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;
import org.argouml.language.csharp.importer.csparser.enums.Modifier;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;


/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 12:28:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class AttributeNode extends BaseNode {

    public long Modifiers;


    public IdentifierExpression Name;


    public NodeCollection<AttributeArgumentNode> Arguments =new NodeCollection<AttributeArgumentNode>();

    public void ToSource(StringBuilder sb) {
        sb.append("[");
        if (Modifiers != Modifier.Empty) {
            this.TraceModifiers(this.Modifiers, sb);
            sb.append(": ");
        }
        Name.ToSource(sb);

        if (Arguments != null) {
            sb.append("(");
            String comma = "";
            for (int i = 0; i < Arguments.size(); i++) {
                sb.append(comma);
                Arguments.get(i).ToSource(sb);
                comma = ", ";
            }
            sb.append(")");
        }
        sb.append("]");
    }
}
