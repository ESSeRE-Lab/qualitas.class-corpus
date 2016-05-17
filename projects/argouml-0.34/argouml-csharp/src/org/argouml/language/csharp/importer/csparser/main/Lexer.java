package org.argouml.language.csharp.importer.csparser.main;

import org.argouml.language.csharp.importer.csparser.enums.TokenID;
import org.argouml.language.csharp.importer.csparser.collections.TokenCollection;
import org.argouml.uml.reveng.ImportInterface;


import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:52:00 PM
 */
public class Lexer {
    public static Hashtable<String, Integer> keywords = new Hashtable<String, Integer>();
    public int c;
    public int curLine = 0;
    public TokenCollection tokens;
    public Reader src;
    public List<String> StringLiterals;

    public boolean brokenInnerLoop = false;
    public boolean brokenInnerLoop1 = false;
    public boolean brokenInnerLoop2 = false;

    public String file;

    public Lexer(BufferedInputStream source, String filename) throws IOException, ImportInterface.ImportException {
        StringBuilder sb = new StringBuilder();
        int i;
        while ((i = source.read()) != -1) {
            sb.append((char) i);
        }

        file=filename;

        String str = removeGenerics(sb.toString());
//        if (hasGenerics(sb.toString())) {
//            throw new ImportInterface.ImportException("Classes with generics are still not supported :" + filename);
//        }

//        String str = sb.toString();

        src = new StringReader(str);

        keywords.put("byte", TokenID.Byte);
        keywords.put("bool", TokenID.Bool);
        keywords.put("char", TokenID.Char);
        keywords.put("double", TokenID.Double);
        keywords.put("decimal", TokenID.Decimal);
        keywords.put("float", TokenID.Float);
        keywords.put("int", TokenID.Int);
        keywords.put("long", TokenID.Long);
        keywords.put("object", TokenID.Object);
        keywords.put("sbyte", TokenID.SByte);
        keywords.put("string", TokenID.String);
        keywords.put("partial", TokenID.Partial);
        keywords.put("short", TokenID.Short);
        keywords.put("ushort", TokenID.UShort);
        keywords.put("ulong", TokenID.ULong);
        keywords.put("uint", TokenID.UInt);
        keywords.put("abstract", TokenID.Abstract);
        keywords.put("const", TokenID.Const);
        keywords.put("extern", TokenID.Extern);
        keywords.put("explicit", TokenID.Explicit);
        keywords.put("implicit", TokenID.Implicit);
        keywords.put("internal", TokenID.Internal);
        keywords.put("new", TokenID.New);
        keywords.put("out", TokenID.Out);
        keywords.put("override", TokenID.Override);
        keywords.put("private", TokenID.Private);
        keywords.put("public", TokenID.Public);
        keywords.put("protected", TokenID.Protected);
        keywords.put("ref", TokenID.Ref);
        keywords.put("readonly", TokenID.Readonly);
        keywords.put("static", TokenID.Static);
        keywords.put("sealed", TokenID.Sealed);
        keywords.put("volatile", TokenID.Volatile);
        keywords.put("virtual", TokenID.Virtual);
        keywords.put("class", TokenID.Class);
        keywords.put("delegate", TokenID.Delegate);
        keywords.put("enum", TokenID.Enum);
        keywords.put("interface", TokenID.Interface);
        keywords.put("struct", TokenID.Struct);
        keywords.put("as", TokenID.As);
        keywords.put("base", TokenID.Base);
        keywords.put("break", TokenID.Break);
        keywords.put("catch", TokenID.Catch);
        keywords.put("continue", TokenID.Continue);
        keywords.put("case", TokenID.Case);
        keywords.put("do", TokenID.Do);
        keywords.put("default", TokenID.Default);
        keywords.put("else", TokenID.Else);
        keywords.put("for", TokenID.For);
        keywords.put("foreach", TokenID.Foreach);
        keywords.put("finally", TokenID.Finally);
        keywords.put("fixed", TokenID.Fixed);
        keywords.put("goto", TokenID.Goto);
        keywords.put("if", TokenID.If);
        keywords.put("in", TokenID.In);
        keywords.put("is", TokenID.Is);
        keywords.put("lock", TokenID.Lock);
        keywords.put("return", TokenID.Return);
        keywords.put("stackalloc", TokenID.Stackalloc);
        keywords.put("switch", TokenID.Switch);
        keywords.put("sizeof", TokenID.Sizeof);
        keywords.put("throw", TokenID.Throw);
        keywords.put("try", TokenID.Try);
        keywords.put("typeof", TokenID.Typeof);
        keywords.put("this", TokenID.This);
        keywords.put("void", TokenID.Void);
        keywords.put("while", TokenID.While);
        keywords.put("checked", TokenID.Checked);
        keywords.put("event", TokenID.Event);
        keywords.put("namespace", TokenID.Namespace);
        keywords.put("operator", TokenID.Operator);
        keywords.put("params", TokenID.Params);
        keywords.put("unsafe", TokenID.Unsafe);
        keywords.put("unchecked", TokenID.Unchecked);
        keywords.put("using", TokenID.Using);

        //keywords.put( "assembly", TokenID.Assembly );
        //keywords.put( "property", TokenID.Property );
        //keywords.put( "method", TokenID.Method );
        //keywords.put( "field", TokenID.Field );
        //keywords.put( "param", TokenID.Param);
        //keywords.put( "type", TokenID.type);

        keywords.put("true", TokenID.TrueLiteral);
        keywords.put("false", TokenID.FalseLiteral);
        keywords.put("null", TokenID.NullLiteral);
    }

    private String removeGenerics(String str) throws IOException {
        String ori = str;
        int st = 0, ed = 0;
        int remLength = 0;
        int lastSt = 0;
        while (st > -1 && ed > -1) {
            st = str.indexOf("<", lastSt);
            lastSt=st+1;
            if (st != -1) {
                ed = str.indexOf(">", st);
                if (ed != -1 && st < ed - 1) {
                    lastSt = ed + 1;
                    String t = str.substring(st + 1, ed);
                    if (t.contains("=") || t.contains("&") || t.contains("|")
                            || t.contains("-") || t.contains("+")
                            || t.contains("/") || t.contains("*")
                            || t.contains("!")) {

                    } else {

                        String x = ori.substring(0, st - remLength);
                        x += ori.substring(ed + 1 - remLength);
                        ori = x;
                        remLength += (ed - st + 1);
                    }
                }
            }
        }
        return ori;
    }

    private Boolean hasGenerics(String str) throws IOException {
        int st = 0, ed = 0;
        int remLength = 0;
        int lastSt = 0;
        while (st > -1 && ed > -1) {
            st = str.indexOf("<", lastSt);
            if (st != -1) {
                ed = str.indexOf(">", st);
                if (ed != -1 && st < ed - 1) {
                    lastSt = ed + 1;
                    String t = str.substring(st + 1, ed);
                    if (t.contains("=") || t.contains("&") || t.contains("|")
                            || t.contains("-") || t.contains("+")
                            || t.contains("/") || t.contains("*")
                            || t.contains("!")) {

                    } else {

                        return true;
                    }
                }
            }
        }
        return false;
    }


    public TokenCollection lex() throws IOException, ImportInterface.ImportException {
        tokens = new TokenCollection();
        StringLiterals = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int loc = 0;

        c = src.read();
        readLoop:
        while (c != -1) {
            switch (c) {
                case -1: {
                    break;
                }

                case (int) '\t': {
                    //dont add whitespace tokens
                    c = src.read();
                    while (c == (int) '\t') {
                        c = src.read();
                    } // check for dups of \t
                    break;
                }
                case (int) ' ': {
                    //dont add tokens whitespace
                    c = src.read();
                    while (c == (int) ' ') {
                        c = src.read();
                    }// check for dups of ' '
                    break;
                }
                case (int) '\r': {
                    c = src.read();
                    if (c == (int) '\n')
                        c = src.read();
                    curLine++;
                    tokens.add(new Token(TokenID.Newline));
                    break;
                }
                case (int) '\n': {
                    c = src.read();
                    curLine++;
                    tokens.add(new Token(TokenID.Newline));
                    break;
                }

                case (int) '@':
                case (int) '\'':
                case (int) '"': {
                    boolean isVerbatim = false;
                    if (c == (int) '@') {
                        isVerbatim = true;
                        c = src.read(); // skip to follow quote
                    }
                    sb = new StringBuilder();
                    int quote = c;
                    boolean isSingleQuote = (c == (int) '\'');
                    c = src.read();
                    while (c != -1) {
                        if (c == (int) '\\' && !isVerbatim) // normal escaped chars
                        {
                            c = src.read();
                            switch (c) {
                                //'"\0abfnrtv
                                case -1: {
                                    brokenInnerLoop1 = true; //done-Txs
                                    break;
                                }
                                case 0: {
                                    sb.append('\0');
                                    c = src.read();
                                    break;
                                }
//                                case (int) 'a': {
//                                    sb.append('\a');
//                                    c = src.read();
//                                    break;
//                                }
                                case (int) 'b': {
                                    sb.append('\b');
                                    c = src.read();
                                    break;
                                }
                                case (int) 'f': {
                                    sb.append('\f');
                                    c = src.read();
                                    break;
                                }
                                case (int) 'n': {
                                    sb.append('\n');
                                    c = src.read();
                                    break;
                                }
                                case (int) 'r': {
                                    sb.append('\r');
                                    c = src.read();
                                    break;
                                }
                                case (int) 't': {
                                    sb.append('\t');
                                    c = src.read();
                                    break;
                                }
//                                case (int) 'v': {
//                                    sb.append('\v');
//                                    c = src.read();
//                                    break;
//                                }
                                case (int) '\\': {
                                    sb.append('\\');
                                    c = src.read();
                                    break;
                                }
                                case (int) '\'': {
                                    sb.append('\'');
                                    c = src.read();
                                    break;
                                }
                                case (int) '\"': {
                                    // StringLiterals are always stored as verbatim for now, so the double quote is needed
                                    sb.append("\"\"");
                                    c = src.read();
                                    break;
                                }
                                default: {
                                    sb.append((char) c);
                                    break;
                                }
                            }
                        } else if (c == (int) '\"') {
                            c = src.read();
                            // two double quotes are escapes for quotes in verbatim mode
                            if (c == (int) '\"' && isVerbatim)// verbatim escape
                            {
                                sb.append("\"\"");
                                c = src.read();
                            } else if (isSingleQuote) {
                                sb.append('\"');
                            } else {
                                break;
                            }
                        } else // non escaped
                        {
                            if (c == quote) {
                                break;
                            }
                            sb.append((char) c);
                            c = src.read();
                        }

                        if (brokenInnerLoop1) {
                            brokenInnerLoop1 = false;
                            brokenInnerLoop2 = true;
                            break;
                        }
                    }
                    if (brokenInnerLoop2) {
                        brokenInnerLoop1 = false;
                        brokenInnerLoop2 = false;
                        break;
                    }

                    if (c != -1) {
                        if (c == quote) {
                            c = src.read(); // skip last quote
                        }

                        loc = StringLiterals.size();
                        StringLiterals.add(sb.toString());
                        if (quote == '"')
                            tokens.add(new Token(TokenID.StringLiteral, loc));
                        else
                            tokens.add(new Token(TokenID.CharLiteral, loc));
                    }
                    break;
                }

                case (int) '!': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.NotEqual));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.Not));
                    }
                    break;
                }
                case (int) '#': {
                    // preprocessor
                    tokens.add(new Token(TokenID.Hash));
                    c = src.read();
                    break;
                }
                case (int) '$': {
                    tokens.add(new Token(TokenID.Dollar)); // this is error in C#
                    c = src.read();
                    break;
                }
                case (int) '%': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.PercentEqual));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.Percent));
                    }
                    break;
                }
                case (int) '&': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.BAndEqual));
                        c = src.read();
                    } else if (c == (int) '&') {
                        tokens.add(new Token(TokenID.And));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.BAnd));
                    }
                    break;
                }
                case (int) '(': {
                    tokens.add(new Token(TokenID.LParen));
                    c = src.read();
                    break;
                }
                case (int) ')': {
                    tokens.add(new Token(TokenID.RParen));
                    c = src.read();
                    break;
                }
                case (int) '*': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.StarEqual));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.Star));
                    }
                    break;
                }
                case (int) '+': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.PlusEqual));
                        c = src.read();
                    } else if (c == (int) '+') {
                        tokens.add(new Token(TokenID.PlusPlus));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.Plus));
                    }
                    break;
                }
                case (int) ',': {
                    tokens.add(new Token(TokenID.Comma));
                    c = src.read();
                    break;
                }
                case (int) '-': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.MinusEqual));
                        c = src.read();
                    } else if (c == (int) '-') {
                        tokens.add(new Token(TokenID.MinusMinus));
                        c = src.read();
                    } else if (c == (int) '>') {
                        tokens.add(new Token(TokenID.MinusGreater));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.Minus));
                    }
                    break;
                }
                case (int) '/': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.SlashEqual));
                        c = src.read();
                    } else if (c == (int) '/') {
                        c = src.read();
                        sb = new StringBuilder();
                        while (c != '\n' && c != '\r') {
                            sb.append((char) c);
                            c = src.read();
                        }
                        int index = this.StringLiterals.size();
                        this.StringLiterals.add(sb.toString());
                        tokens.add(new Token(TokenID.SingleComment, index));
                    } else if (c == (int) '*') {
                        c = src.read();
                        sb = new StringBuilder();
                        for (; ;) {
                            if (c == (int) '*') {
                                c = src.read();
                                if (c == -1 || c == (int) '/') {
                                    c = src.read();
                                    break;
                                } else {
                                    sb.append('*');
                                    sb.append((char) c);
                                    c = src.read();
                                }
                            } else if (c == -1) {
                                break;
                            } else {
                                sb.append((char) c);
                                c = src.read();
                            }
                        }
                        int index = this.StringLiterals.size();
                        this.StringLiterals.add(sb.toString());
                        tokens.add(new Token(TokenID.MultiComment, index));
                    } else {
                        tokens.add(new Token(TokenID.Slash));
                    }
                    break;
                }

                case (int) ':': {
                    c = src.read();
                    if (c == (int) ':') {
                        tokens.add(new Token(TokenID.ColonColon));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.Colon));
                    }
                    break;
                }
                case (int) ';': {
                    tokens.add(new Token(TokenID.Semi));
                    c = src.read();
                    break;
                }
                case (int) '<': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.LessEqual));
                        c = src.read();
                    } else if (c == (int) '<') {
                        c = src.read();
                        if (c == (int) '=') {
                            tokens.add(new Token(TokenID.ShiftLeftEqual));
                            c = src.read();
                        } else {
                            tokens.add(new Token(TokenID.ShiftLeft));
                        }
                    } else {
                        tokens.add(new Token(TokenID.Less));
                    }
                    break;
                }
                case (int) '=': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.EqualEqual));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.Equal));
                    }
                    break;
                }
                case (int) '>': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.GreaterEqual));
                        c = src.read();
                    } else if (c == (int) '>') {
                        c = src.read();
                        if (c == (int) '=') {
                            tokens.add(new Token(TokenID.ShiftRightEqual));
                            c = src.read();
                        } else {
                            tokens.add(new Token(TokenID.ShiftRight));
                        }
                    } else {
                        tokens.add(new Token(TokenID.Greater));
                    }
                    break;
                }
                case (int) '?': {
                    c = src.read();
                    if (c == (int) '?') {
                        tokens.add(new Token(TokenID.QuestionQuestion));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.Question));
                    }
                    break;
                }

                case (int) '[': {
                    tokens.add(new Token(TokenID.LBracket));
                    c = src.read();
                    break;
                }
                case (int) '\\': {
                    tokens.add(new Token(TokenID.BSlash));
                    c = src.read();
                    break;
                }
                case (int) ']': {
                    tokens.add(new Token(TokenID.RBracket));
                    c = src.read();
                    break;
                }
                case (int) '^': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.BXorEqual));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.Not));
                    }
                    break;
                }
                case (int) '`': {
                    tokens.add(new Token(TokenID.BSQuote));
                    c = src.read();
                    break;
                }
                case (int) '{': {
                    tokens.add(new Token(TokenID.LCurly));
                    c = src.read();
                    break;
                }
                case (int) '|': {
                    c = src.read();
                    if (c == (int) '=') {
                        tokens.add(new Token(TokenID.BOrEqual));
                        c = src.read();
                    } else if (c == (int) '|') {
                        tokens.add(new Token(TokenID.Or));
                        c = src.read();
                    } else {
                        tokens.add(new Token(TokenID.BOr));
                    }
                    break;
                }
                case (int) '}': {
                    tokens.add(new Token(TokenID.RCurly));
                    c = src.read();
                    break;
                }
                case (int) '~': {
                    tokens.add(new Token(TokenID.Tilde));
                    c = src.read();
                    break;
                }

                case (int) '0':
                case (int) '1':
                case (int) '2':
                case (int) '3':
                case (int) '4':
                case (int) '5':
                case (int) '6':
                case (int) '7':
                case (int) '8':
                case (int) '9':
                case (int) '.': {
                    sb = new StringBuilder();
                    int numKind = TokenID.IntLiteral; // default
                    boolean isReal = false;

                    // special case dot
                    if (c == (int) '.') {
                        c = src.read();
                        if (c < '0' || c > '9') {
                            tokens.add(new Token(TokenID.Dot));
                            break;
                        } else {
                            sb.append('.');
                            numKind = TokenID.RealLiteral;
                            isReal = true;
                        }
                    }
                    boolean isNum = true;
                    if (c == (int) '0') {
                        sb.append((char) c);
                        c = src.read();
                        if (c == (int) 'x' || c == (int) 'X') {
                            sb.append((char) c);
                            isNum = true;
                            while (isNum) {
                                c = src.read();
                                switch (c) {
                                    case (int) '0':
                                    case (int) '1':
                                    case (int) '2':
                                    case (int) '3':
                                    case (int) '4':
                                    case (int) '5':
                                    case (int) '6':
                                    case (int) '7':
                                    case (int) '8':
                                    case (int) '9':
                                    case (int) 'A':
                                    case (int) 'B':
                                    case (int) 'C':
                                    case (int) 'D':
                                    case (int) 'E':
                                    case (int) 'F':
                                    case (int) 'a':
                                    case (int) 'b':
                                    case (int) 'c':
                                    case (int) 'd':
                                    case (int) 'e':
                                    case (int) 'f': {
                                        sb.append((char) c);
                                        break;
                                    }
                                    default: {
                                        isNum = false;
                                        break;
                                    }
                                }
                            }
                            // find possible U and Ls
                            if (c == (int) 'l' || c == (int) 'L') {
                                sb.append((char) c);
                                c = src.read();
                                numKind = TokenID.LongLiteral;
                                if (c == (int) 'u' || c == (int) 'U') {
                                    sb.append((char) c);
                                    numKind = TokenID.ULongLiteral;
                                    c = src.read();
                                }
                            } else if (c == (int) 'u' || c == (int) 'U') {
                                sb.append((char) c);
                                numKind = TokenID.UIntLiteral;
                                c = src.read();
                                if (c == (int) 'l' || c == (int) 'L') {
                                    sb.append((char) c);
                                    numKind = TokenID.ULongLiteral;
                                    c = src.read();
                                }
                            }
                            //numKind = TokenID.HexLiteral;
                            loc = this.StringLiterals.size();
                            this.StringLiterals.add(sb.toString());
                            tokens.add(new Token(numKind, loc));
                            break; // done number, exits
                        }
                    }

                    // if we get here, it is non hex, but it might be just zero

                    // read number part
                    isNum = true;
                    while (isNum) {
                        switch (c) {
                            case (int) '0':
                            case (int) '1':
                            case (int) '2':
                            case (int) '3':
                            case (int) '4':
                            case (int) '5':
                            case (int) '6':
                            case (int) '7':
                            case (int) '8':
                            case (int) '9': {
                                sb.append((char) c);
                                c = src.read();
                                break;
                            }
                            case (int) '.': {
                                if (isReal) // only one dot allowed in numbers
                                {
                                    numKind = TokenID.RealLiteral;
                                    loc = this.StringLiterals.size();
                                    this.StringLiterals.add(sb.toString());
                                    tokens.add(new Token(numKind, loc));
                                    brokenInnerLoop1 = true;//done-Tsx
                                    break;
//                                    goto readLoop;
                                }

                                // might have 77.toString() construct
                                c = src.read();
                                if (c < (int) '0' || c > (int) '9') {
                                    loc = this.StringLiterals.size();
                                    this.StringLiterals.add(sb.toString());
                                    tokens.add(new Token(numKind, loc));
                                    brokenInnerLoop1 = true;//done-Tsx
                                    break;
//                                    goto readLoop;
                                } else {
                                    sb.append('.');
                                    sb.append((char) c);
                                    numKind = TokenID.RealLiteral;
                                    isReal = true;
                                }
                                c = src.read();
                                break;
                            }
                            default: {
                                isNum = false;
                                break;
                            }
                        }
                        if (brokenInnerLoop1) {
                            brokenInnerLoop1 = false;
                            brokenInnerLoop2 = true;
                            break;
                        }
                    }
                    if (brokenInnerLoop2) {
                        brokenInnerLoop1 = false;
                        brokenInnerLoop2 = false;
                        break;
                    }
                    // now test for letter endings

                    // first exponent
                    if (c == (int) 'e' || c == (int) 'E') {
                        numKind = TokenID.RealLiteral;
                        sb.append((char) c);
                        c = src.read();
                        if (c == '+' || c == '-') {
                            sb.append((char) c);
                            c = src.read();
                        }

                        isNum = true;
                        while (isNum) {
                            switch (c) {
                                case (int) '0':
                                case (int) '1':
                                case (int) '2':
                                case (int) '3':
                                case (int) '4':
                                case (int) '5':
                                case (int) '6':
                                case (int) '7':
                                case (int) '8':
                                case (int) '9': {
                                    sb.append((char) c);
                                    c = src.read();
                                    break;
                                }
                                default: {
                                    isNum = false;
                                    break;
                                }
                            }
                        }
                    } else if (c == (int) 'd' || c == (int) 'D' ||
                            c == (int) 'f' || c == (int) 'F' ||
                            c == (int) 'm' || c == (int) 'M') {
                        numKind = TokenID.RealLiteral;
                        sb.append((char) c);
                        c = src.read();
                    }
                    // or find possible U and Ls
                    else if (c == (int) 'l' || c == (int) 'L') {
                        sb.append((char) c);
                        numKind = TokenID.LongLiteral;
                        c = src.read();
                        if (c == (int) 'u' || c == (int) 'U') {
                            sb.append((char) c);
                            numKind = TokenID.ULongLiteral;
                            c = src.read();
                        }
                    } else if (c == (int) 'u' || c == (int) 'U') {
                        sb.append((char) c);
                        numKind = TokenID.UIntLiteral;
                        c = src.read();
                        if (c == (int) 'l' || c == (int) 'L') {
                            sb.append((char) c);
                            numKind = TokenID.ULongLiteral;
                            c = src.read();
                        }
                    }

                    loc = this.StringLiterals.size();
                    this.StringLiterals.add(sb.toString());
                    tokens.add(new Token(numKind, loc));
                    isNum = false;
                    break;
                }

                default: {
                    // todo: deal with unicode chars
                    // check if this is an identifier char
                    switch (c) {
                        case (int) 'a':
                        case (int) 'b':
                        case (int) 'c':
                        case (int) 'd':
                        case (int) 'e':
                        case (int) 'f':
                        case (int) 'g':
                        case (int) 'h':
                        case (int) 'i':
                        case (int) 'j':
                        case (int) 'k':
                        case (int) 'l':
                        case (int) 'm':
                        case (int) 'n':
                        case (int) 'o':
                        case (int) 'p':
                        case (int) 'q':
                        case (int) 'r':
                        case (int) 's':
                        case (int) 't':
                        case (int) 'u':
                        case (int) 'v':
                        case (int) 'w':
                        case (int) 'x':
                        case (int) 'y':
                        case (int) 'z':
                        case (int) 'A':
                        case (int) 'B':
                        case (int) 'C':
                        case (int) 'D':
                        case (int) 'E':
                        case (int) 'F':
                        case (int) 'G':
                        case (int) 'H':
                        case (int) 'I':
                        case (int) 'J':
                        case (int) 'K':
                        case (int) 'L':
                        case (int) 'M':
                        case (int) 'N':
                        case (int) 'O':
                        case (int) 'P':
                        case (int) 'Q':
                        case (int) 'R':
                        case (int) 'S':
                        case (int) 'T':
                        case (int) 'U':
                        case (int) 'V':
                        case (int) 'W':
                        case (int) 'X':
                        case (int) 'Y':
                        case (int) 'Z':
                        case (int) '_': {
                            sb = new StringBuilder();
                            sb.append((char) c);
                            c = src.read();
                            boolean endIdent = false;
                            boolean possibleKeyword = true;

                            while (c != -1 && !endIdent) {
                                switch (c) {
                                    case (int) 'a':
                                    case (int) 'b':
                                    case (int) 'c':
                                    case (int) 'd':
                                    case (int) 'e':
                                    case (int) 'f':
                                    case (int) 'g':
                                    case (int) 'h':
                                    case (int) 'i':
                                    case (int) 'j':
                                    case (int) 'k':
                                    case (int) 'l':
                                    case (int) 'm':
                                    case (int) 'n':
                                    case (int) 'o':
                                    case (int) 'p':
                                    case (int) 'q':
                                    case (int) 'r':
                                    case (int) 's':
                                    case (int) 't':
                                    case (int) 'u':
                                    case (int) 'v':
                                    case (int) 'w':
                                    case (int) 'x':
                                    case (int) 'y':
                                    case (int) 'z': {
                                        sb.append((char) c);
                                        c = src.read();
                                        break;
                                    }
                                    case (int) 'A':
                                    case (int) 'B':
                                    case (int) 'C':
                                    case (int) 'D':
                                    case (int) 'E':
                                    case (int) 'F':
                                    case (int) 'G':
                                    case (int) 'H':
                                    case (int) 'I':
                                    case (int) 'J':
                                    case (int) 'K':
                                    case (int) 'L':
                                    case (int) 'M':
                                    case (int) 'N':
                                    case (int) 'O':
                                    case (int) 'P':
                                    case (int) 'Q':
                                    case (int) 'R':
                                    case (int) 'S':
                                    case (int) 'T':
                                    case (int) 'U':
                                    case (int) 'V':
                                    case (int) 'W':
                                    case (int) 'X':
                                    case (int) 'Y':
                                    case (int) 'Z':
                                    case (int) '_':
                                    case (int) '0':
                                    case (int) '1':
                                    case (int) '2':
                                    case (int) '3':
                                    case (int) '4':
                                    case (int) '5':
                                    case (int) '6':
                                    case (int) '7':
                                    case (int) '8':
                                    case (int) '9': {
                                        possibleKeyword = false;
                                        sb.append((char) c);
                                        c = src.read();
                                        break;
                                    }
                                    default: {
                                        endIdent = true;
                                        break;
                                    }
                                }
                            }
                            String identText = sb.toString();
                            boolean isKeyword = possibleKeyword ? keywords.containsKey(identText) : false;
                            if (isKeyword) {
                                tokens.add(new Token(keywords.get(identText)));
                            } else {
                                loc = this.StringLiterals.size();
                                this.StringLiterals.add(identText);
                                tokens.add(new Token(TokenID.Ident, loc));
                            }
                            break;
                        }
                        default: {
                            // todo: if unicode char get ident

                            // non unicode
                            tokens.add(new Token(TokenID.Invalid));
                            c = src.read();
                            break;
                        }
                    }
                    break;
                }
            }

        }

        //isGeneric();

        return tokens;
    }


    public void isGeneric() throws ImportInterface.ImportException {
        boolean tx = false;
        int i = 0;
        int k=0;
        for (Token t : tokens) {
            k++;
            if (!tx) {
                if (t.id == TokenID.Less) {
                    tx = true;
                    i = 0;
                }
            }
            else{
                if(t.id == TokenID.Greater){
                    if(i==1){
                        throw new ImportInterface.ImportException("Generics found :"+ k +" "+file);

                    }
                }
                else{
                    i++;
                    if(i>1){
                        tx=true;
                    }
                }
            }
        }
    }



}
