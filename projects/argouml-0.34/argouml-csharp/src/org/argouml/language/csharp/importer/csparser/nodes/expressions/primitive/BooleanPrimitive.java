package org.argouml.language.csharp.importer.csparser.nodes.expressions.primitive;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 5:16:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class BooleanPrimitive extends LiteralNode {
    public BooleanPrimitive(boolean value) {
        this.Value = value;
    }

    private boolean Value;

    public void ToSource(StringBuilder sb) {
        sb.append(Value + " ");
    }
}