package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.collections.ExpressionList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:05:13 PM
 */
public class InvocationExpression extends PrimaryExpression
    {
        public InvocationExpression()
        {
        }
        public InvocationExpression(PrimaryExpression leftSide, ExpressionList argumentList)
        {
            this.LeftSide = leftSide;
            this.ArgumentList = argumentList;
        }

        private ExpressionNode LeftSide;


        private ExpressionList ArgumentList;


        public void ToSource(StringBuilder sb)
        {
            LeftSide.ToSource(sb);
            sb.append("(");
            ArgumentList.ToSource(sb);
            sb.append(")");
        }
    }
