package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.enums.TokenID;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:44:04 PM
 */
public class BinaryExpression extends ExpressionNode
    {
        public static HashMap<Integer, String> stringMap;

        public BinaryExpression() {
            init();
        }

        public BinaryExpression(int op)
        {
            this.Op = new Integer(op) ;
            init();

        }
        public BinaryExpression(int op, ExpressionNode left)
        {
            this.Op = new Integer(op);
            this.Left = left;
            init();

        }
        public BinaryExpression(int op, ExpressionNode left, ExpressionNode right)
        {
            this.Op =new Integer(op);
            this.Left = left;
            this.Right = right; // Right must be 'type'
            init();

        }
        public Integer Op;
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

        public ExpressionNode Left;


        public ExpressionNode Right;


        public void ToSource(StringBuilder sb)
        {
            Left.ToSource(sb);
            sb.append(" " + stringMap.get(Op) + " ");
            Right.ToSource(sb);
        }

        public void init()
        {
            stringMap = new  HashMap<Integer, String>();
            stringMap.put(new Integer(TokenID.Not),"!");
            stringMap.put(new Integer(TokenID.Percent), "%");
            stringMap.put(new Integer(TokenID.BAnd), "&");
            stringMap.put(new Integer(TokenID.BOr), "|");
            stringMap.put(new Integer(TokenID.Star), "*");
            stringMap.put(new Integer(TokenID.Plus), "+");
            stringMap.put(new Integer(TokenID.Minus), "-");
            stringMap.put(new Integer(TokenID.Slash), "/");
            stringMap.put(new Integer(TokenID.Less), "<");
            stringMap.put(new Integer(TokenID.Equal), "=");
            stringMap.put(new Integer(TokenID.Greater), ">");

            stringMap.put(new Integer(TokenID.PlusPlus), "++");
            stringMap.put(new Integer(TokenID.MinusMinus), "--");
            stringMap.put(new Integer(TokenID.And), "&&");
            stringMap.put(new Integer(TokenID.Or), "||");
            stringMap.put(new Integer(TokenID.EqualEqual), "==");
            stringMap.put(new Integer(TokenID.NotEqual), "!=");
            stringMap.put(new Integer(TokenID.LessEqual), "<=");
            stringMap.put(new Integer(TokenID.GreaterEqual), ">=");
            stringMap.put(new Integer(TokenID.ShiftLeft), "<<");
            stringMap.put(new Integer(TokenID.ShiftRight), ">>");

            stringMap.put(new Integer(TokenID.Is), "is");
            stringMap.put(new Integer(TokenID.As), "as");

            stringMap.put(new Integer(TokenID.MinusGreater), "->");
        }
    }

