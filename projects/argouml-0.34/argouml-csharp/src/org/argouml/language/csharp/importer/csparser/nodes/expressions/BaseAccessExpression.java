package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.collections.ExpressionList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:39:07 PM
 */
public class BaseAccessExpression extends PrimaryExpression
    {
        public BaseAccessExpression()
        {
        }
        public BaseAccessExpression(IdentifierExpression identifier)
        {
            this.Identifier = identifier;
        }
        public BaseAccessExpression(ExpressionList expressions)
        {
            this.Expressions = expressions;
        }


        private IdentifierExpression Identifier;


        private ExpressionList Expressions;


        public void ToSource(StringBuilder sb)
        {
            sb.append("base");
            if (Identifier != null)
            {
                sb.append(".");
                Identifier.ToSource(sb);
            }
            else if (Expressions != null)
            {
                sb.append("[");
                Expressions.ToSource(sb);
                sb.append("]");
            }
        }
    }
