package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.interfaces.IType;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:18:02 PM
 */
public class TypeNode extends PrimaryExpression implements IType
    {
        public TypeNode()
        {
        }
        public TypeNode(IdentifierExpression identifier)
        {
            this.Identifier = identifier;
        }

        public IdentifierExpression Identifier;


        public List<Integer> RankSpecifiers = new ArrayList<Integer>();

        public void ToSource(StringBuilder sb)
        {
            Identifier.ToSource(sb);

            if (RankSpecifiers.size() > 0)
            {
                for(int val:RankSpecifiers)
                {
                    sb.append("[");
                    for (int i = 0; i < val; i++)
                    {
                        sb.append(",");
                    }
                    sb.append("]");
                }
            }
        }
    }
