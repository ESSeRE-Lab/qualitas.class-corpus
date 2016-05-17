package org.argouml.language.csharp.importer.csparser.nodes.expressions.primitive;

import org.argouml.language.csharp.importer.csparser.enums.IntegralType;
import org.argouml.language.csharp.importer.csparser.enums.NumberStyles;
import org.argouml.language.csharp.importer.csparser.util.StringUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 5:28:05 PM
 */
public class IntegralPrimitive extends LiteralNode
    {
        private String originalString;
        private IntegralType integralType;
        public IntegralPrimitive(String value, IntegralType integralType)
        {
            this.originalString = value;
            this.integralType = integralType;

            switch (integralType)
            {
                case SByte:
                case Byte:
                case Short:
                case Int:
                    break;

                case UShort:
                    value = StringUtil.removeChar(value,new char[]{'U' ,'u'});
                    break;

                case UInt:
                    value = StringUtil.removeChar(value,new char[]{'U' ,'u'});
                    break;

                case Long:
                    value = StringUtil.removeChar(value,new char[]{'L' ,'l'});
                    break;

                case ULong:
                    value = StringUtil.removeChar(value,new char[]{'L' ,'l','U','u'});
                    value = StringUtil.removeChar(value,new char[]{'L' ,'l','U','u'});
                    break;

                default:
                    //throw new Exception("Illegal Integral type");
            }

            int radix = 10;
            int style = NumberStyles.Integer;
            if (value.toLowerCase().startsWith("0x"))
            {
                radix = 16;
                style = NumberStyles.HexNumber;
                value = value.substring(2);
            }
            // negation is wrapped in a unaryNegationNode so no need to account for negative values
            //try
            //{
            this.Value = Long.parseLong(value, radix);
//            this.Value = Long.parseLong(value, style);
            //this.Value = Convert.ToUInt64(Value, radix);

            //}
            //catch (OverflowException)
            //{
            //    ConsoleWr
            //}
            //catch (FormatException)
            //{
            //}

        }

        private long Value;



        public IntegralType getIntegralType()
        {
            return integralType;
        }

        public void ToSource(StringBuilder sb)
        {
            sb.append(Value);
        }
    }
