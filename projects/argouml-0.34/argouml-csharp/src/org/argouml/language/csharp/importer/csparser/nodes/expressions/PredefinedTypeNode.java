package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.interfaces.IType;
import org.argouml.language.csharp.importer.csparser.interfaces.IMemberAccessible;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:13:48 PM
 */
public class PredefinedTypeNode extends ExpressionNode implements IType, IMemberAccessible
    {
        public PredefinedTypeNode(String type)
        {
            this.Type = type;
        }

        private String Type;


        private List<Integer> RankSpecifiers = new ArrayList<Integer>();

        public void ToSource(StringBuilder sb)
        {
            sb.append(Type);
        }
    }
