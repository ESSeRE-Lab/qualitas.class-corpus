package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.enums.TokenID;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:35:37 PM
 */
public class AssignmentExpression extends ExpressionNode
    {
        public AssignmentExpression()
        {
        }
        public AssignmentExpression(int op, ExpressionNode variable, ExpressionNode rightSide)
        {
            this.Operator = op;
            this.Variable = variable;
            this.RightSide = rightSide;
        }
        int Operator;

        private ExpressionNode Variable;


        private ExpressionNode RightSide;


        public void ToSource(StringBuilder sb)
        {
            Variable.ToSource(sb);
            sb.append(" ");
            switch (Operator)
            {
                case TokenID.Equal:
                    sb.append("=");
                    break;
                case TokenID.PlusEqual:
                    sb.append("+=");
                    break;
                case TokenID.MinusEqual:
                    sb.append("-=");
                    break;
                case TokenID.StarEqual:
                    sb.append("*=");
                    break;
                case TokenID.SlashEqual:
                    sb.append("/=");
                    break;
                case TokenID.PercentEqual:
                    sb.append("%=");
                    break;
                case TokenID.BAndEqual:
                    sb.append("&=");
                    break;
                case TokenID.BOrEqual:
                    sb.append("|=");
                    break;
                case TokenID.BXorEqual:
                    sb.append("^=");
                    break;
                case TokenID.ShiftLeftEqual:
                    sb.append("<<=");
                    break;
                case TokenID.ShiftRightEqual:
                    sb.append(">>=");
                    break;
            }
            sb.append(" ");
            RightSide.ToSource(sb);
        }

    }
