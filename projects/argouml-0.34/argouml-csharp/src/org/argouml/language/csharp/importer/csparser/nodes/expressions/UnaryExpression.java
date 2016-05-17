package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.enums.TokenID;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:19:59 PM
 */
public class UnaryExpression extends ExpressionNode
    {
        private static HashMap<Integer, String> stringMap;

        public UnaryExpression()
        {
        }
        public UnaryExpression(int op)
        {
            this.Op = op;
        }
        public UnaryExpression(int op, ExpressionNode child)
        {
            this.Op = op;
            this.Child = child;
        }

        public int Op;
//        public TokenID Op
//        {
//            get { return Op; }
//            set
//            {
//                if (!stringMap.ContainsKey(Op))
//                {
//                    throw new ArgumentException("The TokenID " + Op + " does not represent a valid binary operator.");
//                }
//                Op = value;
//            }
//        }

        public ExpressionNode Child;


        public void ToSource(StringBuilder sb)
        {
            sb.append(stringMap.get(Op));
            Child.ToSource(sb);
        }

        void UnaryExpression()
        {
            stringMap = new  HashMap<Integer, String>();
            stringMap.put(TokenID.Tilde, "~");
            stringMap.put(TokenID.Minus, "-");
            stringMap.put(TokenID.Not, "!");
            stringMap.put(TokenID.Plus, "+");
            stringMap.put(TokenID.PlusPlus, "++");
            stringMap.put(TokenID.MinusMinus, "--");
        }
    }
