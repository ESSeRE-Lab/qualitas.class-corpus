package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.collections.ExpressionList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:33:43 PM
 */
public class ArrayInitializerExpression extends ExpressionNode
    {
        public ArrayInitializerExpression()
        {
        }

        public ExpressionList Expressions;


        public void ToSource(StringBuilder sb)
        {
            sb.append("{");
            Expressions.ToSource(sb);
            sb.append("}");
        }
    }
