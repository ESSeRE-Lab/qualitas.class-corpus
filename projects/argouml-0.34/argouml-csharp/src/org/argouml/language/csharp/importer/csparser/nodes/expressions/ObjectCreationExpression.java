package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.collections.ExpressionList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:08:19 PM
 */
public class ObjectCreationExpression extends PrimaryExpression
    {
        public ObjectCreationExpression()
        {
        }
        public ObjectCreationExpression(ExpressionNode type)
        {
            this.Type = type;
        }
        public ObjectCreationExpression(ExpressionNode type, ExpressionList argumentList)
        {
            this.Type = type;
            this.ArgumentList = argumentList;
        }

        private ExpressionNode Type;


        private ExpressionList ArgumentList;


        public void ToSource(StringBuilder sb)
        {
            sb.append("new ");
            Type.ToSource(sb);
            sb.append("(");
            ArgumentList.ToSource(sb);
            sb.append(")");
        }
    }
