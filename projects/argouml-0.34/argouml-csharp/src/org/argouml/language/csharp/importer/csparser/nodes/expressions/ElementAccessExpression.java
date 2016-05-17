package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.collections.ExpressionList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:02:33 PM
 */
public class ElementAccessExpression extends PrimaryExpression
    {
        public ElementAccessExpression()
        {
        }
        public ElementAccessExpression(PrimaryExpression leftSide)
        {
            this.LeftSide = leftSide;
        }
        public ElementAccessExpression(PrimaryExpression leftSide, ExpressionList expressions)
        {
            this.LeftSide = leftSide;
            this.Expressions = expressions;
        }


        private PrimaryExpression LeftSide;


        private ExpressionList Expressions;


        public void ToSource(StringBuilder sb)
        {
            LeftSide.ToSource(sb);

            sb.append("[");
            Expressions.ToSource(sb);
            sb.append("]");
        }
    }
