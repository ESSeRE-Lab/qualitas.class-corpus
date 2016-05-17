package org.argouml.language.csharp.importer.csparser.enums;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7; 2008
 * Time: 12:44:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class TokenID
	{
		public final static int Whitespace		= 0x00;
		public final static int Newline			= 0x01;
		public final static int SingleComment	= 0x02;
	    public final static int MultiComment	= 0x03;
		public final static int DocComment		= 0x04;

		public final static int Ident			= 0x05;
		public final static int TrueLiteral		= 0x06;
		public final static int FalseLiteral	= 0x07;
		public final static int NullLiteral		= 0x08;

        //newly added
        public final static int Partial			= 0x09; // "partial"
        //newly added
        //SByteLiteral	= 0x09; // not used
		//ByteLiteral	= 0x0A;
		//ShortLiteral	= 0x0B;
		//UShortLiteral	= 0x0C;
		public final static int HexLiteral		= 0x0C;
		public final static int IntLiteral		= 0x0D;
		public final static int UIntLiteral		= 0x0F;
		public final static int LongLiteral		= 0x10;
		public final static int ULongLiteral	= 0x11;

		public final static int DecimalLiteral	= 0x12;
		public final static int RealLiteral		= 0x13;

		public final static int CharLiteral		= 0x14;
		public final static int StringLiteral	= 0x15;

		public final static int ColonColon		= 0x1F; // "??"
		public final static int QuestionQuestion= 0x20; // "::"

		public final static int Not				= (int)'!'; // 0x21
		public final static int Quote			= (int)'"'; // 0x22
		public final static int Hash			= (int)'#'; // 0x23
		public final static int Dollar			= (int)'$'; // 0x24
		public final static int Percent			= (int)'%'; // 0x25
		public final static int BAnd			= (int)'&'; // 0x26
		public final static int SQuote			= (int)'\''; // 0x27
		public final static int LParen			= (int)'('; // 0x28
		public final static int RParen			= (int)')'; // 0x29
		public final static int Star			= (int)'*'; // 0x2A
		public final static int Plus			= (int)'+'; // 0x2Bz
		public final static int Comma			= (int)','; // 0x2C
		public final static int Minus			= (int)'-'; // 0x2D
		public final static int Dot				= (int)'.'; // 0x2E
		public final static int Slash			= (int)'/'; // 0x2F

		public final static int PlusPlus		= 0x30; // "++"
		public final static int MinusMinus		= 0x31; // "--"
		public final static int And				= 0x32; // "&&"
		public final static int Or				= 0x33; // "||"
		public final static int MinusGreater	= 0x34; // "->"
		public final static int EqualEqual		= 0x35; // "=="
		public final static int NotEqual		= 0x36; // "!="
		public final static int LessEqual		= 0x37; // "<="
		public final static int GreaterEqual	= 0x38; // ">="
		public final static int PlusEqual		= 0x39; // "+="

		public final static int Colon			= (int)':'; // 0x3A
		public final static int Semi			= (int)';'; // 0x3B
		public final static int Less			= (int)'<'; // 0x3C
		public final static int Equal			= (int)'='; // 0x3D
		public final static int Greater			= (int)'>'; // 0x3E
		public final static int Question		= (int)'?'; // 0x3F
		//At				= (int)'@'; // 0x40

		public final static int MinusEqual		= 0x41; // "-="
		public final static int StarEqual		= 0x42; // "*="
		public final static int SlashEqual		= 0x43; // "/="
		public final static int PercentEqual	= 0x44; // "%="
		public final static int BAndEqual		= 0x45; // "&="
		public final static int BOrEqual		= 0x46; // "|="
		public final static int BXorEqual		= 0x47; // "^="
		public final static int ShiftLeft		= 0x48; // "<<"
		public final static int ShiftLeftEqual	= 0x49; // "<<="
		public final static int ShiftRight		= 0x4A; // ">>"
		public final static int ShiftRightEqual	= 0x4B; // ">>="

		public final static int Byte			= 0x4C; // "public final static int"
		public final static int Bool			= 0x4D; // "bool"
		public final static int Char			= 0x4E; // "char"
		public final static int Double			= 0x4F; // "double"
		public final static int Decimal			= 0x50; // "decimal"
		public final static int Float			= 0x51; // "float"
		public final static int Int				= 0x52; // "public final static int"
		public final static int Long			= 0x53; // "long"
		public final static int Object			= 0x54; // "object"
		public final static int SByte			= 0x55; // "spublic final static int"
		public final static int String			= 0x56; // "string"
		public final static int Short			= 0x57; // "short"
		public final static int UShort			= 0x58; // "ushort"
		public final static int ULong			= 0x59; // "ulong"
		public final static int UInt			= 0x5A; // "upublic final static int"

		public final static int LBracket		= (int)'['; // 0x5B
		public final static int BSlash			= (int)'\\'; // 0x5C
		public final static int RBracket		= (int)']'; // 0x5D
		public final static int BXor			= (int)'^'; // 0x5E
									//'_'; 0x5F
		public final static int BSQuote			= (int)'`'; // 0x60

		public final static int Abstract		= 0x61; // "abstract"
		public final static int Const			= 0x62; // "const"
		public final static int Extern			= 0x63; // "extern"
		public final static int Explicit		= 0x64; // "explicit"
		public final static int Implicit		= 0x65; // "implicit"
		public final static int Internal		= 0x66; // "public final static internal"
		public final static int New				= 0x67; // "new"
		public final static int Out				= 0x68; // "out"
		public final static int Override		= 0x69; // "override"
		public final static int Private			= 0x6A; // "private"
		public final static int Public			= 0x6B; // "public"
		public final static int Protected		= 0x6C; // "protected"
		public final static int Ref				= 0x6D; // "ref"
		public final static int Readonly		= 0x6E; // "readonly"
		public final static int Static			= 0x6F; // "static"
		public final static int Sealed			= 0x70; // "sealed"
		public final static int Volatile		= 0x71; // "volatile"
		public final static int Virtual			= 0x72; // "virtual"
		public final static int Class			= 0x73; // "class"
		public final static int Delegate		= 0x74; // "delegate"
		public final static int Enum			= 0x75; // "enum"
		public final static int Interface		= 0x76; // "public final static interface"
		public final static int Struct			= 0x77; // "struct"
		public final static int As				= 0x78; // "as"
		public final static int Base			= 0x79; // "base"
		public final static int Break			= 0x7A; // "break"

		public final static int LCurly			= (int)'{'; // 0x7B
		public final static int BOr				= (int)'|'; // 0x7C
		public final static int RCurly			= (int)'}'; // 0x7D
		public final static int Tilde			= (int)'~'; // 0x7E

		public final static int Catch			= 0x7F; // "catch"
		public final static int Continue		= 0x80; // "continue"
		public final static int Case			= 0x81; // "case"
		public final static int Do				= 0x82; // "do"
		public final static int Default			= 0x83; // "default"
		public final static int Else			= 0x84; // "else"
		public final static int For				= 0x85; // "for"
		public final static int Foreach			= 0x86; // "foreach"
		public final static int Finally			= 0x87; // "finally"
		public final static int Fixed			= 0x88; // "fixed"
		public final static int Goto			= 0x89; // "goto"
		public final static int If				= 0x8A; // "if"
		public final static int In				= 0x8B; // "in"
		public final static int Is				= 0x8C; // "is"
		public final static int Lock			= 0x8D; // "lock"
		public final static int Return			= 0x8E; // "return"
		public final static int Stackalloc		= 0x8F; // "stackalloc"
		public final static int Switch			= 0x90; // "switch"
		public final static int Sizeof			= 0x91; // "sizeof"
		public final static int Throw			= 0x92; // "throw"
		public final static int Try				= 0x93; // "try"
		public final static int Typeof			= 0x94; // "typeof"
		public final static int This			= 0x95; // "this"
		public final static int Void			= 0x96; // "void"
		public final static int While			= 0x97; // "while"
		public final static int Checked			= 0x98; // "checked"
		public final static int Event			= 0x99; // "event"
		public final static int Namespace		= 0x9A; // "namespace"
		public final static int Operator		= 0x9B; // "operator"
		public final static int Params			= 0x9C; // "params"
		public final static int Unsafe			= 0x9D; // "unsafe"
		public final static int Unchecked		= 0x9E; // "unchecked"
		public final static int Using			= 0x9F; // "using"

		// (not keywords)

		public final static int Assembly 		= 0xA0; // "assembly"
		public final static int Property		= 0xA1; // "property"
		public final static int Method			= 0xA2; // "method"
		public final static int Field			= 0xA3; // "field"
		public final static int Param			= 0xA4; // "param"
		public final static int Type 			= 0xA5; // "type"

		public final static int PpDefine		= 0xE0; // preproc Define
		public final static int PpUndefine		= 0xE1; // preproc Undefine
		public final static int PpIf			= 0xE2; // preproc If
		public final static int PpElif			= 0xE3; // preproc Elif
		public final static int PpElse			= 0xE4; // preproc Else
		public final static int PpEndif			= 0xE5; // preproc Endif
		public final static int PpLine			= 0xE6; // preproc Line
		public final static int PpError			= 0xE7; // preproc Error
		public final static int PpWarning		= 0xE8; // preproc Warning
		public final static int PpRegion		= 0xE9; // preproc Region
		public final static int PpEndregion		= 0xEA; // preproc Endregion
		public final static int PpPragma		= 0xEB; // preproc Pragma

		public final static int Eof				= 0xFE; // error token
		public final static int Invalid			= 0xFF; // error token



        public static String getFieldName(int x){
            String s="";
            switch(x){
                case TokenID.Bool:
                    s= "Bool";
                    break;
                case TokenID.Byte:
                    s= "Base";
                    break;
                case TokenID.Char:
                    s= "Char";
                    break;
                case TokenID.Decimal:
                    s= "Decimal";
                    break;
                case TokenID.Double:
                    s= "Double";
                    break;
                case TokenID.Float:
                    s= "Float";
                    break;
                case TokenID.Int:
                    s= "Int";
                    break;
                case TokenID.Long:
                    s= "Long";
                    break;
                case TokenID.Object:
                    s= "Base";
                    break;
                case TokenID.SByte:
                    s= "SByte";
                    break;
                case TokenID.Short:
                    s= "Short";
                    break;
                case TokenID.String:
                    s= "String";
                    break;
                case TokenID.UInt:
                    s= "UInt";
                    break;
                case TokenID.ULong:
                    s= "ULong";
                    break;
                case TokenID.UShort:
                    s= "UShort";
                    break;
                case TokenID.Void:
                    s= "Void";
                    break;
                case TokenID.This:
                    s= "This";
                    break;
                case TokenID.Base:
                    s= "Base";
                    break;
                case TokenID.If:
                    s= "If";
                    break;
                case TokenID.Else:
                    s= "Else";
                    break;
                default:
                    return "";
            }

            return s.toLowerCase();
        }

    }
