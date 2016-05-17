package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.interfaces.IType;
import org.argouml.language.csharp.importer.csparser.interfaces.IMemberAccessible;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:06:21 PM
 */
public class MemberAccessExpression extends PrimaryExpression implements IType
    {
        public MemberAccessExpression()
        {
        }
        public MemberAccessExpression(IMemberAccessible left, IdentifierExpression identifier)
        {
            this.Left = left;
            this.Identifier = identifier;
        }

        private IdentifierExpression Identifier;


        private IMemberAccessible Left;


        private List<Integer> RankSpecifiers = new ArrayList<Integer>();


        public void ToSource(StringBuilder sb)
        {
            Left.ToSource(sb);
            sb.append(".");
            Identifier.ToSource(sb);
        }
    }
