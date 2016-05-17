package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.collections.ExpressionList;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:17:11 PM
 */
public class ArrayCreationExpression extends PrimaryExpression
    {
        public TypeNode Type;

        public ExpressionList RankSpecifier;


        public List<Integer> AdditionalRankSpecifiers = new ArrayList<Integer>();


        public ArrayInitializerExpression Initializer;


        public void ToSource(StringBuilder sb)
        {
            sb.append("new ");
            Type.ToSource(sb);

            sb.append("[");
            if (RankSpecifier != null)
            {
                RankSpecifier.ToSource(sb);
            }
            sb.append("]");

            if (AdditionalRankSpecifiers != null)
            {
                for (int i = 0; i < AdditionalRankSpecifiers.size(); i++)
                {
                    sb.append("[");
                    for (int j = 0; j < AdditionalRankSpecifiers.get(j) ; j++)
                    {
                        sb.append(",");
                    }
                    sb.append("]");
                }
            }
            if (Initializer != null)
            {
                Initializer.ToSource(sb);
            }
        }
    }
