package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.interfaces.IType;
import org.argouml.language.csharp.importer.csparser.enums.TokenID;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:19:56 PM
 */
public class IdentifierExpression extends PrimaryExpression implements IType
    {
        public IdentifierExpression()
        {
        }
        public IdentifierExpression(String[] identifier)
        {
            this.Identifier = identifier;
        }

        public int StartingPredefinedType = TokenID.Invalid;


        public boolean getStartsWithPredefinedType()
        {

                if (StartingPredefinedType == TokenID.Invalid)
                {
                    return false;
                }
                else
                {
                    return true;
                }
        }

        public String[] Identifier;


        public List<Integer> RankSpecifiers = new ArrayList<Integer>();


        public void ToSource(StringBuilder sb)
        {
            String dot = "";
            for (int i = 0; i < Identifier.length; i++)
            {
                sb.append(dot + Identifier[i]);
                dot = ".";
            }
        }
    }
