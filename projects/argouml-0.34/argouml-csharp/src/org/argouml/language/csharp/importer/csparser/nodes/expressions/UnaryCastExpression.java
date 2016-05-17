package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.interfaces.IType;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:19:22 PM
 */
public class UnaryCastExpression extends UnaryExpression
    {
        public UnaryCastExpression()
        {
        }
        public UnaryCastExpression(IType type, ExpressionNode child)
        {
            this.Type = type;
            this.Child = child;
        }
        public IType Type;


        public  void ToSource(StringBuilder sb)
        {
            sb.append("(");
            Type.ToSource(sb);
            sb.append(")");
            Child.ToSource(sb);
        }

    }
