package org.argouml.language.csharp.importer.csparser.main;

import org.argouml.language.csharp.importer.csparser.collections.ExpressionList;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.collections.ParseStateCollection;
import org.argouml.language.csharp.importer.csparser.collections.TokenCollection;
import org.argouml.language.csharp.importer.csparser.enums.IntegralType;
import org.argouml.language.csharp.importer.csparser.enums.Modifier;
import org.argouml.language.csharp.importer.csparser.enums.PreprocessorID;
import org.argouml.language.csharp.importer.csparser.enums.TokenID;
import org.argouml.language.csharp.importer.csparser.interfaces.IMemberAccessible;
import org.argouml.language.csharp.importer.csparser.interfaces.IType;
import org.argouml.language.csharp.importer.csparser.members.*;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.*;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.primitive.*;
import org.argouml.language.csharp.importer.csparser.preprocessornodes.PPDefineNode;
import org.argouml.language.csharp.importer.csparser.preprocessornodes.PPEndIfNode;
import org.argouml.language.csharp.importer.csparser.preprocessornodes.PPNode;
import org.argouml.language.csharp.importer.csparser.statements.*;
import org.argouml.language.csharp.importer.csparser.structural.*;
import org.argouml.language.csharp.importer.csparser.types.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 2:31:45 PM
 */
public class Parser {
    boolean success = true;
    public static Token EOF = new Token(org.argouml.language.csharp.importer.csparser.enums.TokenID.Eof);
    private static Hashtable<Integer, Long> modMap;
    private static Hashtable<String, Byte> preprocessor;
    private static Hashtable<String, Byte> ppDefs = new Hashtable<String, Byte>();
    private static int[] precedence;

    private CompilationUnitNode cu;
    private TokenCollection tokens;
    private List<String> strings;

    private Stack<NamespaceNode> namespaceStack;
    private Stack<ClassNode> typeStack;
    private org.argouml.language.csharp.importer.csparser.collections.Stack<ExpressionNode> exprStack;
    public ParseStateCollection CurrentState;
    private InterfaceNode curInterface;
    private Token curtok;
    private long curmods;
    private NodeCollection<AttributeNode> curAttributes;
    private int index = 0;
    private boolean isLocalConst = false;
    private int lineCount = 1;
    private boolean ppCondition = false;
    private boolean inPPDirective = false;


    public Parser() {
        modMap = new Hashtable<Integer, Long>();
        modMap.put(TokenID.New, Modifier.New);
        modMap.put(TokenID.Public, Modifier.Public);
        modMap.put(TokenID.Partial, Modifier.Partial);
        modMap.put(TokenID.Protected, Modifier.Protected);
        modMap.put(TokenID.Internal, Modifier.Internal);
        modMap.put(TokenID.Private, Modifier.Private);
        modMap.put(TokenID.Abstract, Modifier.Abstract);
        modMap.put(TokenID.Sealed, Modifier.Sealed);
        modMap.put(TokenID.Static, Modifier.Static);
        modMap.put(TokenID.Virtual, Modifier.Virtual);
        modMap.put(TokenID.Override, Modifier.Override);
        modMap.put(TokenID.Extern, Modifier.Extern);
        modMap.put(TokenID.Readonly, Modifier.Readonly);
        modMap.put(TokenID.Volatile, Modifier.Volatile);
        modMap.put(TokenID.Ref, Modifier.Ref);
        modMap.put(TokenID.Out, Modifier.Out);
        modMap.put(TokenID.Assembly, Modifier.Assembly);
        modMap.put(TokenID.Field, Modifier.Field);
        modMap.put(TokenID.Event, Modifier.Event);
        modMap.put(TokenID.Method, Modifier.Method);
        modMap.put(TokenID.Param, Modifier.Param);
        modMap.put(TokenID.Property, Modifier.Property);
        modMap.put(TokenID.Return, Modifier.Return);
        modMap.put(TokenID.Type, Modifier.Type);

        // all default to zero
        precedence = new int[0xFF];

        // these start at 80 for no paticular reason
        precedence[(int) TokenID.LBracket] = 0x90;

        precedence[(int) TokenID.LParen] = 0x80;
        precedence[(int) TokenID.Star] = 0x7F;
        precedence[(int) TokenID.Slash] = 0x7F;
        precedence[(int) TokenID.Percent] = 0x7F;
        precedence[(int) TokenID.Plus] = 0x7E;
        precedence[(int) TokenID.Minus] = 0x7E;
        precedence[(int) TokenID.ShiftLeft] = 0x7D;
        precedence[(int) TokenID.ShiftRight] = 0x7D;
        precedence[(int) TokenID.Less] = 0x7C;
        precedence[(int) TokenID.Greater] = 0x7C;
        precedence[(int) TokenID.LessEqual] = 0x7C;
        precedence[(int) TokenID.GreaterEqual] = 0x7C;
        precedence[(int) TokenID.EqualEqual] = 0x7B;
        precedence[(int) TokenID.NotEqual] = 0x7B;
        precedence[(int) TokenID.BAnd] = 0x7A;
        precedence[(int) TokenID.BXor] = 0x79;
        precedence[(int) TokenID.BOr] = 0x78;
        precedence[(int) TokenID.And] = 0x77;
        precedence[(int) TokenID.Or] = 0x76;


        preprocessor = new Hashtable<String, Byte>();

        preprocessor.put("define", PreprocessorID.Define);
        preprocessor.put("undef", PreprocessorID.Undef);
        preprocessor.put("if", PreprocessorID.If);
        preprocessor.put("elif", PreprocessorID.Elif);
        preprocessor.put("else", PreprocessorID.Else);
        preprocessor.put("endif", PreprocessorID.Endif);
        preprocessor.put("line", PreprocessorID.Line);
        preprocessor.put("error", PreprocessorID.Error);
        preprocessor.put("warning", PreprocessorID.Warning);
        preprocessor.put("region", PreprocessorID.Region);
        preprocessor.put("endregion", PreprocessorID.Endregion);
        preprocessor.put("pragma", PreprocessorID.Pragma);
    }

    public CompilationUnitNode parse(TokenCollection tokens, List<String> strings) throws FeatureNotSupportedException {
        this.tokens = tokens;
        this.strings = strings;
        curmods = Modifier.Empty;
        curAttributes = new NodeCollection<AttributeNode>();

        CurrentState = new ParseStateCollection();

        cu = new CompilationUnitNode();
        namespaceStack = new Stack<NamespaceNode>();
        namespaceStack.push(cu.DefaultNamespace);
        typeStack = new Stack<ClassNode>();

        exprStack = new org.argouml.language.csharp.importer.csparser.collections.Stack<ExpressionNode>();

        // begin parse
        advance();
        parseNamespaceOrTypes();

        return cu;
    }

    private void parseNamespaceOrTypes() throws FeatureNotSupportedException {
        while (!curtok.equals(EOF)) {
            // todo: account for assembly attributes
            parsePossibleAttributes(true);
            if (curAttributes.size() > 0) {
                for (AttributeNode an : curAttributes) {
                    cu.attributes.add(an);
                }
                curAttributes.clear();
            }

            // can be usingDirectives, globalAttribs, or NamespaceMembersDecls
            // NamespaceMembersDecls include namespaces, class, struct, interface, enum, delegate
            switch (curtok.id) {
                case TokenID.Using:
                    // using directive
                    parseUsingDirectives();
                    break;

                case TokenID.New:
                case TokenID.Public:
                case TokenID.Protected:
                case TokenID.Partial:
                case TokenID.Static:
                case TokenID.Internal:
                case TokenID.Private:
                case TokenID.Abstract:
                case TokenID.Sealed:
                    //parseTypeModifier();
                    curmods |= modMap.get(curtok.id);
                    advance();
                    break;

                case TokenID.Namespace:
                    parseNamespace();
                    break;

                case TokenID.Class:
                    parseClass();
                    break;

                case TokenID.Struct:
                    parseStruct();
                    break;

                case TokenID.Interface:
                    parseInterface();
                    break;

                case TokenID.Enum:
                    parseEnum();
                    break;

                case TokenID.Delegate:
                    parseDelegate();
                    break;

                case TokenID.Semi:
                    advance();
                    break;

                default:
                    return;
            }
        }
    }

    private void parseUsingDirectives() throws FeatureNotSupportedException {
        do {
            advance();
            UsingDirectiveNode node = new UsingDirectiveNode();

            IdentifierExpression nameOrAlias = parseQualifiedIdentifier();
            if (curtok.id == TokenID.Equal) {
                advance();
                IdentifierExpression target = parseQualifiedIdentifier();
                node.setAliasName(nameOrAlias);
                node.Target = target;
            } else {
                node.Target = nameOrAlias;
            }
            AssertAndAdvance(TokenID.Semi);

            cu.UsingDirectives.add(node);

        } while (curtok.id == TokenID.Using);
    }

    private PPNode parsePreprocessorDirective() throws FeatureNotSupportedException {
        PPNode result = null;
        int startLine = lineCount;

        inPPDirective = true;
        advance(); // over hash

        IdentifierExpression ie = parseIdentifierOrKeyword();
        String ppKind = ie.Identifier[0];

        byte id = PreprocessorID.Empty;
        if (preprocessor.containsKey(ppKind)) {
            id = preprocessor.get(ppKind);
        } else {
            ReportError("Preprocessor directive must be valid identifier, rather than \"" + ppKind + "\".");
        }

        switch (id) {
            case PreprocessorID.Define:
                // conditional-symbol pp-newline
                IdentifierExpression def = parseIdentifierOrKeyword();
                if (!ppDefs.containsKey(def.Identifier[0])) {
                    ppDefs.put(def.Identifier[0], PreprocessorID.Empty);
                }
                result = new PPDefineNode(def);
                break;
            case PreprocessorID.Undef:
                // conditional-symbol pp-newline
                IdentifierExpression undef = parseIdentifierOrKeyword();
                if (ppDefs.containsKey(undef.Identifier[0])) {
                    ppDefs.remove(undef.Identifier[0]);
                }
                result = new PPDefineNode(undef);
                break;
            case PreprocessorID.If:
                // pp-expression pp-newline conditional-section(opt)
                if (curtok.id == TokenID.LParen) {
                    advance();
                }
                int startCount = lineCount;
                ppCondition = false;

                // todo: account for true, false, ||, &&, ==, !=, !
                IdentifierExpression ifexpr = parseIdentifierOrKeyword();
                if (ppDefs.containsKey(ifexpr.Identifier[0])) {
                    ppCondition = true;
                }
                //result = new PPIfNode(ParseExpressionToNewline());
                if (curtok.id == TokenID.RParen) {
                    advance();
                }
                if (ppCondition == false) {
                    // skip this block
                    SkipToElseOrEndIf();
                }
                break;
            case PreprocessorID.Elif:
                // pp-expression pp-newline conditional-section(opt)
                SkipToEOL(startLine);
                break;
            case PreprocessorID.Else:
                // pp-newline conditional-section(opt)
                if (ppCondition == true) {
                    // skip this block
                    SkipToElseOrEndIf();
                }
                break;
            case PreprocessorID.Endif:
                // pp-newline
                result = new PPEndIfNode();
                ppCondition = false;
                break;
            case PreprocessorID.Line:
                // line-indicator pp-newline
                SkipToEOL(startLine);
                break;
            case PreprocessorID.Error:
                // pp-message
                SkipToEOL(startLine);
                break;
            case PreprocessorID.Warning:
                // pp-message
                SkipToEOL(startLine);
                break;
            case PreprocessorID.Region:
                // pp-message
                SkipToEOL(startLine);
                break;
            case PreprocessorID.Endregion:
                // pp-message
                SkipToEOL(startLine);
                break;
            case PreprocessorID.Pragma:
                // pp-message
                SkipToEOL(startLine);
                break;
            default:
                break;
        }
        inPPDirective = false;
        return result;
    }

    private void parsePossibleAttributes(boolean isGlobal) throws FeatureNotSupportedException {
        while (curtok.id == TokenID.LBracket) {

            advance(); // advance over LBracket token
            curmods = parseAttributeModifiers();

            if (isGlobal && curmods == Modifier.GlobalAttributeMods) {
                // nothing to check, globally positioned attributes can still apply to namespaces etc
            } else {
                long attribMask = ~(Modifier.AttributeMods);
                if (((long) curmods & attribMask) != (long) Modifier.Empty)
                    ReportError("Attribute contains illegal modifiers.");
            }

            long curAttribMods = curmods;
            curmods = Modifier.Empty;

            if (curAttribMods != Modifier.Empty) {
                AssertAndAdvance(TokenID.Colon);
            }

            AttributeNode node = new AttributeNode();
            curAttributes.add(node);
            node.Modifiers = curAttribMods;

            while (curtok.id != TokenID.RBracket && curtok.id != TokenID.Eof) {
                node.Name = parseQualifiedIdentifier();

                if (curtok.id == TokenID.LParen) {
                    // has attribute arguments
                    advance(); // over lparen

                    // named args are ident = expr
                    // positional args are just expr
                    while (curtok.id != TokenID.RParen && curtok.id != TokenID.Eof) {
                        AttributeArgumentNode aNode = new AttributeArgumentNode();

                        if (tokens.size() > index + 2 &&
                                curtok.id == TokenID.Ident &&
                                tokens.get(index).id == TokenID.Equal) {
                            // named argument
                            aNode.ArgumentName = parseQualifiedIdentifier();
                            advance(); // over '='
                        }
                        aNode.Expression = ParseExpression();
                        node.Arguments.add(aNode);

                        if (curtok.id == TokenID.Comma) {
                            advance(); // over comma
                        }
                    }
                    AssertAndAdvance(TokenID.RParen);  // over rparen
                    if (tokens.size() > index + 2 &&
                            curtok.id == TokenID.Comma &&
                            tokens.get(index).id != TokenID.RBracket) {
                        advance(); // over comma
                        node = new AttributeNode();
                        curAttributes.add(node);
                        node.Modifiers = curAttribMods;
                    }
                }
                if (curtok.id == TokenID.Comma) {
                    // comma can hang a t end like enums
                    advance();
                }
            }
            AssertAndAdvance(TokenID.RBracket); // over rbracket
        }
    }

    private void parseNamespace() throws FeatureNotSupportedException {
        if (curmods != Modifier.Empty)
            ReportError("Namespace can not contain modifiers");

        NamespaceNode node = new NamespaceNode();
        if (cu.Namespaces.size() == 1 && cu.Namespaces.get(0) == cu.DefaultNamespace) {
            cu.Namespaces.clear();
        }

        cu.Namespaces.add(node);
        namespaceStack.push(node);

        advance(); // advance over Namespace token
        node.Name = parseQualifiedIdentifier();

        AssertAndAdvance(TokenID.LCurly);

        parseNamespaceOrTypes();

        AssertAndAdvance(TokenID.RCurly);
        namespaceStack.pop();
    }

    // types
    private void parseClass() throws FeatureNotSupportedException {
        long classMask = ~((long) Modifier.ClassMods);
        if (((long) curmods & classMask) != (long) Modifier.Empty)
            ReportError("Class contains illegal modifiers.");

        ClassNode node = new ClassNode();
        typeStack.push(node);
        namespaceStack.peek().Classes.add(node);

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        node.Modifiers = curmods;
        curmods = Modifier.Empty;

        advance(); // advance over Class token
        node.Name = parseQualifiedIdentifier();

        if (curtok.id == TokenID.Colon) // for base members
        {
            advance();
            node.BaseClasses.add(parseType());
            while (curtok.id == TokenID.Comma) {
                advance();
                node.BaseClasses.add(parseType());
            }
        }
        AssertAndAdvance(TokenID.LCurly);

        while (curtok.id != TokenID.RCurly) // guard for empty
        {
            parseClassMember();
        }

        AssertAndAdvance(TokenID.RCurly);

        typeStack.pop();

    }

    private void parseInterface() throws FeatureNotSupportedException {

        InterfaceNode node = new InterfaceNode();
        namespaceStack.peek().Interfaces.add(node);
        curInterface = node;

        long interfaceMask = ~(long) Modifier.InterfaceMods;
        if (((long) curmods & interfaceMask) != (long) Modifier.Empty)
            ReportError("Interface contains illegal modifiers");

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        node.Modifiers = curmods;
        curmods = Modifier.Empty;

        advance(); // advance over Interface token
        node.Name = parseQualifiedIdentifier();

        if (curtok.id == TokenID.Colon) // for base members
        {
            advance();
            node.BaseClasses.add(parseType());
            while (curtok.id == TokenID.Comma) {
                advance();
                node.BaseClasses.add(parseType());
            }
        }
        AssertAndAdvance(TokenID.LCurly);

        while (curtok.id != TokenID.RCurly) // guard for empty
        {
            parseInterfaceMember();
        }

        AssertAndAdvance(TokenID.RCurly);

        curInterface = null;

    }

    private void parseStruct() throws FeatureNotSupportedException {
        StructNode node = new StructNode();
        typeStack.push(node);
        namespaceStack.peek().Structs.add(node);

        long structMask = ~(long) Modifier.StructMods;
        if (((long) curmods & structMask) != (long) Modifier.Empty)
            ReportError("Struct contains illegal modifiers");

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        node.Modifiers = curmods;
        curmods = Modifier.Empty;

        advance(); // advance over Struct token
        node.Name = parseQualifiedIdentifier();

        if (curtok.id == TokenID.Colon) // for base members
        {
            advance();
            node.BaseClasses.add(parseType());
            while (curtok.id == TokenID.Comma) {
                advance();
                node.BaseClasses.add(parseType());
            }
        }
        AssertAndAdvance(TokenID.LCurly);

        while (curtok.id != TokenID.RCurly) // guard for empty
        {
            parseClassMember();
        }

        AssertAndAdvance(TokenID.RCurly);

        typeStack.pop();
    }

    private void parseEnum() throws FeatureNotSupportedException {
        EnumNode node = new EnumNode();
        // todo: this needs to have any nested class info, or go in potential container class
        namespaceStack.peek().Enums.add(node);

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        long enumMask = ~(long) Modifier.EnumMods;
        if (((long) curmods & enumMask) != (long) Modifier.Empty)
            ReportError("Enum contains illegal modifiers");

        node.Modifiers = curmods;
        curmods = Modifier.Empty;

        advance(); // advance over Enum token
        node.Name = parseQualifiedIdentifier();

        if (curtok.id == TokenID.Colon) // for base type
        {
            advance();
            node.BaseClass = parseType();
        }
        AssertAndAdvance(TokenID.LCurly);

        while (curtok.id != TokenID.RCurly) // guard for empty
        {
            parseEnumMember();
        }

        AssertAndAdvance(TokenID.RCurly);
        if (curtok.id == TokenID.Semi) {
            advance();
        }
    }

    private void parseDelegate() throws FeatureNotSupportedException {
        DelegateNode node = new DelegateNode();
        namespaceStack.peek().Delegates.add(node);

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        long delegateMask = ~(long) Modifier.DelegateMods;
        if (((long) curmods & delegateMask) != (long) Modifier.Empty)
            ReportError("Delegate contains illegal modifiers");

        node.Modifiers = curmods;
        curmods = Modifier.Empty;

        advance(); // advance over delegate token
        node.Type = parseType();
        node.Name = parseQualifiedIdentifier();
        node.Params = parseParamList();

        AssertAndAdvance(TokenID.Semi);
    }

    // members
    private void parseClassMember() throws FeatureNotSupportedException {
        // const field method property event indexer operator ctor ~ctor cctor typedecl
        parsePossibleAttributes(false);
        parseModifiers();
        switch (curtok.id) {
            case TokenID.Class:
                parseClass();
                break;

            case TokenID.Interface:
                parseInterface();
                break;

            case TokenID.Struct:
                parseStruct();
                break;

            case TokenID.Enum:
                parseEnum();
                break;

            case TokenID.Delegate:
                parseDelegate();
                break;

            case TokenID.Const:
                parseConst();
                break;

            case TokenID.Event:
                parseEvent();
                break;

            case TokenID.Tilde:
                parseDestructor();
                break;

            case TokenID.Explicit:
            case TokenID.Implicit:
                parseOperatorDecl(null);
                break;

            default:
                TypeNode type = parseType();
                if (type == null) {
                    ReportError("Expected type or ident in member definition");
                }
                switch (curtok.id) {
                    case TokenID.Operator:
                        parseOperatorDecl(type);
                        break;
                    case TokenID.LParen:
                        parseCtor(type);
                        break;
                    case TokenID.This: // can be iface.this too, see below
                        parseIndexer(type, null);
                        break;
                    default:
                        IdentifierExpression name2 = parseQualifiedIdentifier();
                        if (name2 == null) {
                            ReportError("Expected name or ident in member definition");
                        }
                        switch (curtok.id) {
                            case TokenID.This:
                                parseIndexer(type, name2);
                                break;
                            case TokenID.Comma:
                            case TokenID.Equal:
                            case TokenID.Semi:
                                parseField(type, name2);
                                break;
                            case TokenID.LParen:
                                parseMethod(type, name2);
                                break;
                            case TokenID.LCurly:
                                parseProperty(type, name2);
                                break;
                            default:
                                ReportError("Invalid member syntax");
                                break;
                        }
                        break;
                }
                break;
        }
    }

    private void parseInterfaceMember() throws FeatureNotSupportedException {
        parsePossibleAttributes(false);

        parseModifiers();
        switch (curtok.id) {
            case TokenID.Event:
                // event
                InterfaceEventNode node = new InterfaceEventNode();
                curInterface.Events.add(node);

                if (curAttributes.size() > 0) {
                    node.attributes = curAttributes;
                    curAttributes = new NodeCollection<AttributeNode>();
                }

                node.modifiers = curmods;
                curmods = Modifier.Empty;
                AssertAndAdvance(TokenID.Event);
                node.type = parseType();
                node.names.add(parseQualifiedIdentifier());
                AssertAndAdvance(TokenID.Semi);

                break;
            default:
                TypeNode type = parseType();
                if (type == null) {
                    ReportError("Expected type or ident in interface member definition.");
                }
                switch (curtok.id) {
                    case TokenID.This:
                        // interface indexer
                        InterfaceIndexerNode iiNode = new InterfaceIndexerNode();
                        if (curAttributes.size() > 0) {
                            iiNode.attributes = curAttributes;
                            curAttributes = new NodeCollection<AttributeNode>();
                        }
                        iiNode.type = type;
                        advance(); // over 'this'
                        iiNode.params = parseParamList(TokenID.LBracket, TokenID.RBracket);

                        //Boolean hasGetter = false;
                        //Boolean hasSetter = false;
                        Boolean[] bx = new Boolean[2];

                        parseInterfaceAccessors(bx);
                        iiNode.hasGetter = bx[0];
                        iiNode.hasSetter = bx[1];
                        break;

                    default:
                        IdentifierExpression name = parseQualifiedIdentifier();
                        if (name == null) {
                            ReportError("Expected name or ident in member definition.");
                        }
                        switch (curtok.id) {
                            case TokenID.LParen:
                                // method
                                InterfaceMethodNode mnode = new InterfaceMethodNode();
                                curInterface.Methods.add(mnode);

                                if (curAttributes.size() > 0) {
                                    mnode.attributes = curAttributes;
                                    curAttributes = new NodeCollection<AttributeNode>();
                                }

                                mnode.modifiers = curmods;
                                curmods = Modifier.Empty;

                                mnode.names.add(name);
                                mnode.type = type;
                                mnode.params = parseParamList();

                                AssertAndAdvance(TokenID.Semi);
                                break;

                            case TokenID.LCurly:
                                // property
                                InterfacePropertyNode pnode = new InterfacePropertyNode();
                                curInterface.Properties.add(pnode);

                                // these are the prop nodes
                                if (curAttributes.size() > 0) {
                                    pnode.attributes = curAttributes;
                                    curAttributes = new NodeCollection<AttributeNode>();
                                }

                                pnode.modifiers = curmods;
                                curmods = Modifier.Empty;

                                pnode.names.add(name);
                                pnode.type = type;

                                bx = new Boolean[2];

                                parseInterfaceAccessors(bx);

                                parseInterfaceAccessors(bx);
                                pnode.hasGetter = bx[0];
                                pnode.hasSetter = bx[1];

                                if (curtok.id == TokenID.Semi) {
                                    AssertAndAdvance(TokenID.Semi);
                                }
                                break;

                            default:
                                ReportError("Invalid interface member syntax.");
                                break;
                        }
                        break;
                }
                break;
        }
    }

    private void parseCtor(TypeNode type) throws FeatureNotSupportedException {
        ConstructorNode node = new ConstructorNode();

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        if ((curmods & Modifier.Static) != Modifier.Empty) {
            node.isStaticConstructor = true;
            curmods = curmods & ~Modifier.Static;
        }
        long mask = ~(long) Modifier.ConstructorMods;
        if (((long) curmods & mask) != (long) Modifier.Empty)
            ReportError("constructor declaration contains illegal modifiers");

        typeStack.peek().Constructors.add(node);
        //node.attributes.add(curAttributes);
        //curAttributes.Clear();
        node.modifiers = curmods;
        curmods = Modifier.Empty;

        node.type = type;
        node.names.add(typeStack.peek().Name);

        // starts at LParen
        node.params = parseParamList();

        if (curtok.id == TokenID.Colon) {
            advance();
            if (curtok.id == TokenID.Base) {
                advance();
                node.hasBase = true;
                node.thisBaseArgs = parseArgs();
            } else if (curtok.id == TokenID.This) {
                advance();
                node.hasThis = true;
                node.thisBaseArgs = parseArgs();
            } else {
                RecoverFromError("constructor requires this or base calls after colon", TokenID.Base);
            }
        }
        parseBlock(node.statementBlock);
    }

    private void parseDestructor() throws FeatureNotSupportedException {
        advance(); // over tilde

        DestructorNode node = new DestructorNode();

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }
        long mask = ~(long) Modifier.DestructorMods;
        if (((long) curmods & mask) != (long) Modifier.Empty)
            ReportError("destructor declaration contains illegal modifiers");

        typeStack.peek().Destructors.add(node);

        node.modifiers = curmods;
        curmods = Modifier.Empty;
        if (curtok.id == TokenID.Ident) {
            node.names.add(parseQualifiedIdentifier());
        } else {
            ReportError("Destructor requires identifier as name.");
        }
        // no args in dtor
        AssertAndAdvance(TokenID.LParen);
        AssertAndAdvance(TokenID.RParen);

        parseBlock(node.statementBlock);
    }

    private void parseOperatorDecl(TypeNode type) throws FeatureNotSupportedException {
        OperatorNode node = new OperatorNode();

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        long mask = ~(long) Modifier.OperatorMods;
        if (((long) curmods & mask) != (long) Modifier.Empty)
            ReportError("operator declaration contains illegal modifiers");

        node.modifiers = curmods;
        curmods = Modifier.Empty;

        if (type == null && curtok.id == TokenID.Explicit) {
            advance();
            node.isExplicit = true;
            AssertAndAdvance(TokenID.Operator);
            type = parseType();
        } else if (type == null && curtok.id == TokenID.Implicit) {
            advance();
            node.isImplicit = true;
            AssertAndAdvance(TokenID.Operator);
            type = parseType();
        } else {
            AssertAndAdvance(TokenID.Operator);
            node.operator = curtok.id;
            advance();
        }
        NodeCollection<ParamDeclNode> paramList = parseParamList();
        if (paramList.size() == 0 || paramList.size() > 2) {
            ReportError("operator declarations must only have one or two parameters.");
        }
        node.param1 = paramList.get(0);
        if (paramList.size() == 2) {
            node.param2 = paramList.get(1);
        }
        parseBlock(node.statements);
    }

    private void parseIndexer(TypeNode type, IdentifierExpression interfaceType) throws FeatureNotSupportedException {
        IndexerNode node = new IndexerNode();
        typeStack.peek().Indexers.add(node);

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        long mask = ~(long) Modifier.IndexerMods;
        if (((long) curmods & mask) != (long) Modifier.Empty)
            ReportError("indexer declaration contains illegal modifiers");


        node.modifiers = curmods;
        curmods = Modifier.Empty;

        node.type = type;
        if (interfaceType != null) {
            node.interfaceType = new TypeNode(interfaceType);
        }

        AssertAndAdvance(TokenID.This);
        node.params = parseParamList(TokenID.LBracket, TokenID.RBracket);

        // parse accessor part
        AssertAndAdvance(TokenID.LCurly);
        if (curtok.id != TokenID.Ident) {
            RecoverFromError("At least one get or set required in accessor", curtok.id);
        }
        boolean parsedGet = false;
        if (strings.get(curtok.data).equals("get")) {
            node.getter = parseAccessor();
            parsedGet = true;
        }
        if (curtok.id == TokenID.Ident && strings.get(curtok.data).equals("set")) {
            node.getter = parseAccessor();
        }
        // get might follow set
        if (!parsedGet && curtok.id == TokenID.Ident && strings.get(curtok.data).equals("get")) {
            node.getter = parseAccessor();
        }
        AssertAndAdvance(TokenID.RCurly);
    }

    private void parseMethod(TypeNode type, IdentifierExpression name) throws FeatureNotSupportedException {
        long mask = ~(long) Modifier.MethodMods;
        if (((long) curmods & mask) != (long) Modifier.Empty)
            ReportError("method declaration contains illegal modifiers");

        MethodNode node = new MethodNode();
        typeStack.peek().Methods.add(node);

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        node.modifiers = curmods;
        curmods = Modifier.Empty;

        node.type = type;
        node.names.add(name);

        // starts at LParen
        node.params = parseParamList();

        parseBlock(node.statementBlock);

    }

    private void parseField(TypeNode type, IdentifierExpression name) throws FeatureNotSupportedException {
        long mask = ~(long) Modifier.FieldMods;
        if (((long) curmods & mask) != (long) Modifier.Empty)
            ReportError("field declaration contains illegal modifiers");

        FieldNode node = new FieldNode();
        typeStack.peek().Fields.add(node);
        node.modifiers = curmods;
        curmods = Modifier.Empty;

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        node.type = type;
        node.names.add(name);

        //eg: int ok = 0, error, xx = 0;
        if (curtok.id == TokenID.Equal) {
            advance();
            node.Value = parseConstExpr();
            if (curtok.id == TokenID.Comma) {
                node = new FieldNode();
                typeStack.peek().Fields.add(node);
                node.modifiers = curmods;
                node.type = type;
            }
        }

        while (curtok.id == TokenID.Comma) {
            advance(); // over comma
            IdentifierExpression ident = parseQualifiedIdentifier();
            node.names.add(ident);
            if (curtok.id == TokenID.Equal) {
                advance();
                node.Value = parseConstExpr();

                if (curtok.id == TokenID.Comma) {
                    node = new FieldNode();
                    typeStack.peek().Fields.add(node);
                    node.modifiers = curmods;
                    node.type = type;
                }
            }
        }

        if (curtok.id == TokenID.Semi) {
            advance();
        }


    }

    private void parseProperty(TypeNode type, IdentifierExpression name) throws FeatureNotSupportedException {
        long mask = ~(long) Modifier.PropertyMods;
        if (((long) curmods & mask) != (long) Modifier.Empty)
            ReportError("field declaration contains illegal modifiers");

        PropertyNode node = new PropertyNode();
        typeStack.peek().Properties.add(node);

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        node.modifiers = curmods;
        curmods = Modifier.Empty;

        node.type = type;
        node.names.add(name);

        // opens on lcurly
        AssertAndAdvance(TokenID.LCurly);

        // todo: AddNode attributes to get and setters
        parsePossibleAttributes(false);

        if (curAttributes.size() > 0) {
            //node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        if (curtok.id != TokenID.Ident) {
            RecoverFromError("At least one get or set required in accessor", curtok.id);
        }

        boolean parsedGet = false;
        if (strings.get(curtok.data).equals("get")) {
            node.getter = parseAccessor();
            parsedGet = true;
        }

        // todo: AddNode attributes to get and setters
        parsePossibleAttributes(false);

        if (curAttributes.size() > 0) {
            //node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        if (curtok.id == TokenID.Ident && strings.get(curtok.data).equals("set")) {
            node.setter = parseAccessor();
        }

        // todo: AddNode attributes to get and setters
        parsePossibleAttributes(false);

        if (curAttributes.size() > 0) {
            //node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        // get might follow set
        if (!parsedGet && curtok.id == TokenID.Ident && strings.get(curtok.data).equals("get")) {
            node.getter = parseAccessor();
        }

        AssertAndAdvance(TokenID.RCurly);
    }

    private void parseEvent() throws FeatureNotSupportedException {
        long mask = ~(long) Modifier.EventMods;
        if (((long) curmods & mask) != (long) Modifier.Empty)
            ReportError("Event contains illegal modifiers");

        EventNode node = new EventNode();
        typeStack.peek().Events.add(node);

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        node.modifiers = curmods;
        curmods = Modifier.Empty;

        advance(); // advance over event keyword

        node.type = parseType();

        if (curtok.id != TokenID.Ident)
            ReportError("Expected event member name.");

        while (curtok.id == TokenID.Ident) {
            node.names.add(parseQualifiedIdentifier());
        }
        if (curtok.id == TokenID.LCurly) {
            advance(); // over lcurly
            // todo: may be attributes
            if (curtok.id != TokenID.Ident) {
                ReportError("Event accessor requires add or remove clause.");
            }
            String curAccessor = strings.get(curtok.data);
            advance(); // over ident
            if (curAccessor.equals("add")) {
                parseBlock(node.addBlock);
                if (curtok.id == TokenID.Ident && strings.get(curtok.data).equals("remove")) {
                    advance(); // over ident
                    parseBlock(node.removeBlock);
                } else {
                    ReportError("Event accessor expected remove clause.");
                }
            } else if (curAccessor.equals("remove")) {
                parseBlock(node.removeBlock);
                if (curtok.id == TokenID.Ident && strings.get(curtok.data).equals("add")) {
                    advance(); // over ident
                    parseBlock(node.addBlock);
                } else {
                    ReportError("Event accessor expected add clause.");
                }
            } else {
                ReportError("Event accessor requires add or remove clause.");
            }
        } else {
            AssertAndAdvance(TokenID.Semi);
        }


    }

    private void parseConst() throws FeatureNotSupportedException {
        long mask = ~(long) Modifier.ConstantMods;
        if (((long) curmods & mask) != (long) Modifier.Empty)
            ReportError("const declaration contains illegal modifiers");

        ConstantNode node = new ConstantNode();
        typeStack.peek().Constants.add(node);

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        node.modifiers = curmods;
        curmods = Modifier.Empty;

        advance(); // advance over const keyword

        node.type = parseType();

        boolean hasEqual = false;
        node.names.add(parseQualifiedIdentifier());
        if (curtok.id == TokenID.Equal) {
            advance();
            hasEqual = true;
        }
        while (curtok.id == TokenID.Comma) {
            advance();
            node.names.add(parseQualifiedIdentifier());
            if (curtok.id == TokenID.Equal) {
                advance();
                hasEqual = true;
            } else {
                hasEqual = false;
            }
        }

        if (hasEqual) {
            node.Value = parseConstExpr();
        }

        AssertAndAdvance(TokenID.Semi);
    }

    private EnumNode parseEnumMember() throws FeatureNotSupportedException {
        EnumNode result = new EnumNode();

        parsePossibleAttributes(false);

        if (curAttributes.size() > 0) {
            result.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        if (curtok.id != TokenID.Ident) {
            ReportError("Enum members must be legal identifiers.");
        }
        String name = strings.get(curtok.data);
        result.Name = new IdentifierExpression(new String[]{name});
        advance();

        if (curtok.id == TokenID.Equal) {
            advance();
            result.Value = ParseExpression();
        }
        if (curtok.id == TokenID.Comma) {
            advance();
        }
        return result;

    }

    // member helpers
    private NodeCollection<ParamDeclNode> parseParamList() throws FeatureNotSupportedException {
        // default is parens, however things like indexers use square brackets
        return parseParamList(TokenID.LParen, TokenID.RParen);
    }

    private NodeCollection<ParamDeclNode> parseParamList(int openToken, int closeToken) throws FeatureNotSupportedException {
        AssertAndAdvance(openToken);
        if (curtok.id == closeToken) {
            advance();
            return null;
        }
        NodeCollection<ParamDeclNode> result = new NodeCollection<ParamDeclNode>();
        boolean isParams = false;
        boolean hasComma = false;
        do {
            ParamDeclNode node = new ParamDeclNode();
            result.add(node);
            isParams = false;

            parsePossibleAttributes(false);

            if (curtok.id == TokenID.Ref) {
                node.modifiers |= Modifier.Ref;
                advance();
            } else if (curtok.id == TokenID.Out) {
                node.modifiers |= Modifier.Out;
                advance();
            } else if (curtok.id == TokenID.Params) {
                isParams = true;
                node.modifiers |= Modifier.Params;
                advance();
            }

            node.type = parseType();

            if (isParams) {
                // ensure is array type
            }

            if (curtok.id == TokenID.Ident) {
                node.name = strings.get(curtok.data);
                advance();
            }

            hasComma = false;
            if (curtok.id == TokenID.Comma) {
                advance();
                hasComma = true;
            }
        }
        while (!isParams && hasComma);

        AssertAndAdvance(closeToken);

        return result;
    }

    private ParamDeclNode parseParamDecl() throws FeatureNotSupportedException {

        ParamDeclNode node = new ParamDeclNode();

        parsePossibleAttributes(false);

        if (curAttributes.size() > 0) {
            node.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        node.type = parseType();

        if (curtok.id == TokenID.Ident) {
            node.name = strings.get(curtok.data);
            advance();
        } else {
            RecoverFromError("Expected arg name.", TokenID.Ident);
        }
        return node;
    }

    private NodeCollection<ArgumentNode> parseArgs() throws FeatureNotSupportedException {
        AssertAndAdvance(TokenID.LParen);
        if (curtok.id == TokenID.RParen) {
            advance();
            return null;
        }
        boolean hasComma = false;
        NodeCollection<ArgumentNode> result = new NodeCollection<ArgumentNode>();
        do {
            ArgumentNode node = new ArgumentNode();
            result.add(node);

            if (curtok.id == TokenID.Ref) {
                node.isRef = true;
                advance();
            } else if (curtok.id == TokenID.Out) {
                node.isOut = true;
                advance();
            }
            node.expression = ParseExpression();

            hasComma = false;
            if (curtok.id == TokenID.Comma) {
                advance();
                hasComma = true;
            }
        }
        while (hasComma);

        AssertAndAdvance(TokenID.RParen);

        return result;
    }

    private AccessorNode parseAccessor() throws FeatureNotSupportedException {
        AccessorNode result = new AccessorNode();

        parsePossibleAttributes(false);

        if (curAttributes.size() > 0) {
            result.attributes = curAttributes;
            curAttributes = new NodeCollection<AttributeNode>();
        }

        String kind = "";
        if (curtok.id == TokenID.Ident) {
            kind = strings.get(curtok.data);
        } else {
            RecoverFromError("Must specify accessor kind in accessor.", curtok.id);
        }

        result.kind = kind;
        advance();
        if (curtok.id == TokenID.Semi) {
            result.isAbstractOrInterface = true;
            advance(); // over semi
        } else {
            parseBlock(result.statementBlock);
        }
        return result;
    }

    private ConstantExpression parseConstExpr() throws FeatureNotSupportedException {
        ConstantExpression node = new ConstantExpression();
        node.Value = ParseExpression();

        return node;
    }

    private void parseModifiers() throws FeatureNotSupportedException {
        while (!curtok.equals(EOF)) {
            switch (curtok.id) {
                case TokenID.New:
                case TokenID.Public:
                case TokenID.Protected:
                case TokenID.Internal:
                case TokenID.Private:
                case TokenID.Abstract:
                case TokenID.Sealed:
                case TokenID.Static:
                case TokenID.Virtual:
                case TokenID.Override:
                case TokenID.Extern:
                case TokenID.Readonly:
                case TokenID.Volatile:
                case TokenID.Ref:
                case TokenID.Out:
                    //Newly added
                case TokenID.Partial:
                    //case TokenID.Assembly:
                    //case TokenID.Field:
                    //case TokenID.Event:
                    //case TokenID.Method:
                    //case TokenID.Param:
                    //case TokenID.Property:
                    //case TokenID.Return:
                    //case TokenID.type:

                    long mod = (long) modMap.get(curtok.id);
                    if (((long) curmods & mod) > 0) {
                        ReportError("Duplicate modifier.");
                    }
                    curmods |= mod;
                    advance();
                    break;


                default:
                    return;
            }
        }
    }

    private long parseAttributeModifiers() throws FeatureNotSupportedException {
        long result = Modifier.Empty;
        String curIdent = "";
        boolean isMod = true;
        while (isMod) {
            switch (curtok.id) {
                case TokenID.Ident:
                    curIdent = strings.get(curtok.data);

                    if (curIdent.equals("field")) {
                        result |= Modifier.Field;
                    } else if (curIdent.equals("method")) {
                        result |= Modifier.Method;
                    } else if (curIdent.equals("param")) {
                        result |= Modifier.Param;
                    } else if (curIdent.equals("property")) {
                        result |= Modifier.Property;
                    } else if (curIdent.equals("type")) {
                        result |= Modifier.Type;
                    } else if (curIdent.equals("module")) {
                        result |= Modifier.Module;
                    } else if (curIdent.equals("assembly")) {
                        result |= Modifier.Assembly;
                    } else {
                        isMod = false;
                    }
                    advance();

//                        switch (curIdent)
//						{
//							case "field":
//								result |= Modifier.Field;
//								advance();
//								break;
//							case "method":
//								result |= Modifier.Method;
//								advance();
//								break;
//							case "param":
//								result |= Modifier.Param;
//								advance();
//								break;
//							case "property":
//								result |= Modifier.Property;
//								advance();
//								break;
//							case "type":
//								result |= Modifier.type;
//								advance();
//								break;
//							case "module":
//								result |= Modifier.Module;
//								advance();
//								break;
//							case "assembly":
//								result |= Modifier.Assembly;
//								advance();
//								break;
//							default:
//								isMod = false;
//								break;
//						}
                    break;

                case TokenID.Return:
                    result |= Modifier.Return;
                    advance();
                    break;

                case TokenID.Event:
                    result |= Modifier.Event;
                    advance();
                    break;

                default:
                    isMod = false;
                    break;

            }
        }
        return result;
    }

    private TypeNode parseType() throws FeatureNotSupportedException {
        IdentifierExpression idPart = parseQualifiedIdentifier();
        TypeNode result = new TypeNode(idPart);

        // now any 'rank only' specifiers (without size decls)
        while (curtok.id == TokenID.LBracket) {
            if (index < tokens.size() &&
                    tokens.get(index).id != TokenID.RBracket &&
                    tokens.get(index).id != TokenID.Comma) {
                // anything with size or accessor decls has own node type
                break;
            }
            advance(); // over lbracket
            int commaCount = 0;
            while (curtok.id == TokenID.Comma) {
                commaCount++;
                advance();
            }
            result.RankSpecifiers.add(commaCount);
            AssertAndAdvance(TokenID.RBracket);
        }

        return result;
    }

    private IdentifierExpression parseQualifiedIdentifier() throws FeatureNotSupportedException {
        IdentifierExpression result = new IdentifierExpression();
        List<String> qualName = new ArrayList<String>();
        switch (curtok.id) {
            case TokenID.Ident:
                qualName.add(strings.get(curtok.data));
                advance();
                break;

            case TokenID.Bool:
            case TokenID.Byte:
            case TokenID.Char:
            case TokenID.Decimal:
            case TokenID.Double:
            case TokenID.Float:
            case TokenID.Int:
            case TokenID.Long:
            case TokenID.Object:
            case TokenID.SByte:
            case TokenID.Short:
            case TokenID.String:
            case TokenID.UInt:
            case TokenID.ULong:
            case TokenID.UShort:
            case TokenID.Void:
            case TokenID.This:
            case TokenID.Base:

                //String predef = Enum.getName(TokenID.Invalid.GetType(), curtok.id).ToLower();
                qualName.add(TokenID.getFieldName(curtok.id));
                result.StartingPredefinedType = curtok.id;
                advance();
                break;

            default:
                RecoverFromError(TokenID.Ident);
                break;
        }

        while (curtok.id == TokenID.Dot) {
            advance();
            if (curtok.id == TokenID.Ident) {
                qualName.add(strings.get(curtok.data));
                advance();
            } else if (curtok.id == TokenID.This) {
                // this is an indexer with a prepended interface, do nothing (but consume dot)
            } else {
                RecoverFromError(TokenID.Ident);
            }
        }

//			result.Identifier = (String[])qualName.toArray(String[] result.Identifier);
        result.Identifier = new String[qualName.toArray().length];
        qualName.toArray(result.Identifier);
        return result;
    }

    private IdentifierExpression parseIdentifierOrKeyword() throws FeatureNotSupportedException {
        IdentifierExpression result = new IdentifierExpression();
        switch (curtok.id) {
            case TokenID.Ident:
                result.Identifier = new String[]{strings.get(curtok.data)};
                advance();
                break;

            case TokenID.If:
            case TokenID.Else:
            case TokenID.Bool:
            case TokenID.Byte:
            case TokenID.Char:
            case TokenID.Decimal:
            case TokenID.Double:
            case TokenID.Float:
            case TokenID.Int:
            case TokenID.Long:
            case TokenID.Object:
            case TokenID.SByte:
            case TokenID.Short:
            case TokenID.String:
            case TokenID.UInt:
            case TokenID.ULong:
            case TokenID.UShort:
            case TokenID.Void:
            case TokenID.This:
            case TokenID.Base:
                String predef = TokenID.getFieldName(curtok.id);//Enum.GetName(TokenID.Invalid.GetType(), curtok.id).ToLower();
                result.Identifier = new String[]{predef};
                result.StartingPredefinedType = curtok.id;
                advance();
                break;

            default:
                RecoverFromError(TokenID.Ident);
                break;
        }
        return result;
    }

    private void parseInterfaceAccessors(Boolean[] bx) throws FeatureNotSupportedException {
        AssertAndAdvance(TokenID.LCurly); // LCurly

        // the get and set can also have attributes
        parsePossibleAttributes(false);

        if (curtok.id == TokenID.Ident && strings.get(curtok.data).equals("get")) {
            if (curAttributes.size() > 0) {
                // todo: store get/set attributes on InterfacePropertyNode
                // pnode.getAttributes = curAttributes;
                curAttributes = new NodeCollection<AttributeNode>();
            }

            bx[0] = true;
            advance();
            AssertAndAdvance(TokenID.Semi);
            if (curtok.id == TokenID.Ident) {
                if (strings.get(curtok.data).equals("set")) {
                    bx[1] = true;
                    advance();
                    AssertAndAdvance(TokenID.Semi);
                } else {
                    RecoverFromError("Expected set in interface property def.", curtok.id);
                }
            }
        } else if (curtok.id == TokenID.Ident && strings.get(curtok.data).equals("set")) {
            if (curAttributes.size() > 0) {
                // todo: store get/set attributes on InterfacePropertyNode
                // pnode.setAttributes = curAttributes;
                curAttributes = new NodeCollection<AttributeNode>();
            }
            bx[1] = true;
            advance();
            AssertAndAdvance(TokenID.Semi);
            if (curtok.id == TokenID.Ident) {
                if (strings.get(curtok.data).equals("get")) {
                    bx[0] = true;
                    advance();
                    AssertAndAdvance(TokenID.Semi);
                } else {
                    RecoverFromError("Expected get in interface property def.", curtok.id);
                }
            }
        } else {
            RecoverFromError("Expected get or set in interface property def.", curtok.id);
        }

        AssertAndAdvance(TokenID.RCurly);
    }

    // statements
    private void parseStatement(NodeCollection<StatementNode> node) throws FeatureNotSupportedException {
        // label		ident	: colon
        // localDecl	type	: ident
        // block		LCurly
        // empty		Semi
        // expression
        //	-invoke		pexpr	: LParen
        //	-objCre		new		: type
        //	-assign		uexpr	: assignOp
        //	-postInc	pexpr	: ++
        //	-postDec	pexpr	: --
        //	-preInc		++		: uexpr
        //	-preDec		--		: uexpr
        //
        // selection	if		: LParen
        //				switch	: LParen
        //
        // iteration	while	: LParen
        //				do		: LParen
        //				for		: LParen
        //				foreach	: LParen
        //
        // jump			break	: Semi
        //				continue: Semi
        //				goto	: ident | case | default
        //				return	: expr
        //				throw	: expr
        //
        // try			try		: block
        // checked		checked	: block
        // unchecked	unchecked : block
        // lock			lock	: LParen
        // using		using	: LParen
        switch (curtok.id) {
            case TokenID.LCurly:    // block
                BlockStatement newBlock = new BlockStatement();
                node.add(newBlock);
                parseBlock(newBlock);
                break;
            case TokenID.Semi:        // empty statement
                advance();
                node.add(new StatementNode());
                break;
            case TokenID.If:        // If statement
                node.add(parseIf());
                break;
            case TokenID.Switch:    // Switch statement
                node.add(parseSwitch());
                break;
            case TokenID.While:        // While statement
                node.add(parseWhile());
                break;
            case TokenID.Do:        // Do statement
                node.add(parseDo());
                break;
            case TokenID.For:        // For statement
                node.add(parseFor());
                break;
            case TokenID.Foreach:    // Foreach statement
                node.add(parseForEach());
                break;
            case TokenID.Break:        // Break statement
                node.add(parseBreak());
                break;
            case TokenID.Continue:    // Continue statement
                node.add(parseContinue());
                break;
            case TokenID.Goto:        // Goto statement
                node.add(parseGoto());
                break;
            case TokenID.Return:    // Return statement
                node.add(parseReturn());
                break;
            case TokenID.Throw:        // Throw statement
                node.add(parseThrow());
                break;
            case TokenID.Try:        // Try statement
                node.add(parseTry());
                break;
            case TokenID.Checked:    // Checked statement
                node.add(parseChecked());
                break;
            case TokenID.Unchecked:    // Unchecked statement
                node.add(parseUnchecked());
                break;
            case TokenID.Lock:        // Lock statement
                node.add(parseLock());
                break;
            case TokenID.Using:        // Using statement
                node.add(ParseUsing());
                break;

            case TokenID.Const:
                isLocalConst = true;
                advance();
                break;

            case TokenID.Bool:
            case TokenID.Byte:
            case TokenID.Char:
            case TokenID.Decimal:
            case TokenID.Double:
            case TokenID.Float:
            case TokenID.Int:
            case TokenID.Long:
            case TokenID.Object:
            case TokenID.SByte:
            case TokenID.Short:
            case TokenID.String:
            case TokenID.UInt:
            case TokenID.ULong:
            case TokenID.UShort:

            case TokenID.StringLiteral:
            case TokenID.HexLiteral:
            case TokenID.IntLiteral:
            case TokenID.UIntLiteral:
            case TokenID.LongLiteral:
            case TokenID.ULongLiteral:
            case TokenID.TrueLiteral:
            case TokenID.FalseLiteral:
            case TokenID.NullLiteral:
            case TokenID.LParen:
            case TokenID.DecimalLiteral:
            case TokenID.RealLiteral:
            case TokenID.CharLiteral:
            case TokenID.PlusPlus:    // PreInc statement
            case TokenID.MinusMinus:// PreDec statement
            case TokenID.This:
            case TokenID.Base:
            case TokenID.New:        // creation statement
                ExpressionStatement enode = new ExpressionStatement(ParseExpression());
                node.add(enode);
                if (curtok.id == TokenID.Semi) {
                    advance();
                }
                break;

            case TokenID.Ident:
                if (tokens.size() > index + 1 && tokens.get(index).id == TokenID.Colon) {
                    LabeledStatement lsnode = new LabeledStatement();
                    lsnode.Name = parseQualifiedIdentifier();
                    AssertAndAdvance(TokenID.Colon);
                    parseStatement(lsnode.Statements);
                    node.add(lsnode);
                } else {
                    ExpressionStatement inode = new ExpressionStatement(ParseExpression());
                    node.add(inode);
                }
                if (curtok.id == TokenID.Semi) {
                    advance();
                }
                break;

            case TokenID.Unsafe:
                // preprocessor directives
                ParseUnsafeCode();
                break;

            default:
                System.out.println("Unhandled case in statement parsing: \"" + curtok.id + "\" in line: " + lineCount);
                // this is almost always an expression
                ExpressionStatement dnode = new ExpressionStatement(ParseExpression());
                node.add(dnode);
                if (curtok.id == TokenID.Semi) {
                    advance();
                }
                break;
        }
    }

    private void parseBlock(BlockStatement node) throws FeatureNotSupportedException {
        parseBlock(node, false);
    }

    private void parseBlock(BlockStatement node, boolean isCase) throws FeatureNotSupportedException {
        if (curtok.id == TokenID.LCurly) {
            advance(); // over lcurly
            while (curtok.id != TokenID.Eof && curtok.id != TokenID.RCurly) {
                parseStatement(node.Statements);
            }
            AssertAndAdvance(TokenID.RCurly);
        } else if (isCase) {
            // case stmts can have multiple lines without curlies, ugh
            // break can be omitted if it is unreachable code, double ugh
            // this becomes impossible to trace without code analysis of course, so look for 'case' or '}'

            while (curtok.id != TokenID.Eof && curtok.id != TokenID.Case && curtok.id != TokenID.Default && curtok.id != TokenID.RCurly) {
                parseStatement(node.Statements);
            }
            //boolean endsOnReturn = false;
            //while (curtok.id != TokenID.Eof && !endsOnReturn)
            //{
            //    TokenID startTok = curtok.id;
            //    if (startTok == TokenID.Return	||
            //        startTok == TokenID.Goto	||
            //        startTok == TokenID.Throw	||
            //        startTok == TokenID.Break)
            //    {
            //        endsOnReturn = true;
            //    }

            //    parseStatement(node.statements);

            //    // doesn't have to end on return or goto
            //    if (endsOnReturn && (startTok == TokenID.Return	|| startTok == TokenID.Goto	|| startTok == TokenID.Throw))
            //    {
            //        if (curtok.id == TokenID.Break)
            //        {
            //            parseStatement(node.statements);
            //        }
            //    }
            //}
        } else {
            parseStatement(node.Statements);
        }

    }

    private IfStatement parseIf() throws FeatureNotSupportedException {
        IfStatement node = new IfStatement();
        advance(); // advance over IF

        AssertAndAdvance(TokenID.LParen);
        node.Test = ParseExpression();
        AssertAndAdvance(TokenID.RParen);

        parseBlock(node.Statements);

        if (curtok.id == TokenID.Else) {
            advance(); // advance of else
            parseBlock(node.ElseStatements);
        }
        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private SwitchStatement parseSwitch() throws FeatureNotSupportedException {
        SwitchStatement node = new SwitchStatement();
        advance(); // advance over SWITCH

        AssertAndAdvance(TokenID.LParen);
        node.Test = ParseExpression();
        AssertAndAdvance(TokenID.RParen);

        AssertAndAdvance(TokenID.LCurly);
        while (curtok.id == TokenID.Case || curtok.id == TokenID.Default) {
            node.Cases.add(parseCase());
        }

        AssertAndAdvance(TokenID.RCurly);

        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private CaseNode parseCase() throws FeatureNotSupportedException {
        CaseNode node = new CaseNode();
        boolean isDefault = (curtok.id == TokenID.Default);
        advance(); // advance over CASE or DEFAULT

        if (!isDefault) {
            node.Ranges.add(ParseExpression());
        } else {
            node.IsDefaultCase = true;
        }
        AssertAndAdvance(TokenID.Colon);

        // may be multiple cases, but must be at least one
        while (curtok.id == TokenID.Case || curtok.id == TokenID.Default) {
            isDefault = (curtok.id == TokenID.Default);
            advance(); // advance over CASE or DEFAULT
            if (!isDefault) {
                node.Ranges.add(ParseExpression());
            } else {
                node.IsDefaultCase = true;
            }
            AssertAndAdvance(TokenID.Colon);
        }
        if (curtok.id != TokenID.LCurly) {
            node.StatementBlock.setHasBraces(false);
        }
        parseBlock(node.StatementBlock, true);
        return node;
    }

    private WhileStatement parseWhile() throws FeatureNotSupportedException {
        WhileStatement node = new WhileStatement();
        advance(); // advance over While

        AssertAndAdvance(TokenID.LParen);
        node.Test = ParseExpression();
        AssertAndAdvance(TokenID.RParen);

        parseBlock(node.Statements);

        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private DoStatement parseDo() throws FeatureNotSupportedException {
        DoStatement node = new DoStatement();
        advance(); // advance over DO

        parseBlock(node.Statements);

        AssertAndAdvance(TokenID.While); // advance over While

        AssertAndAdvance(TokenID.LParen);
        node.Test = ParseExpression();
        AssertAndAdvance(TokenID.RParen);

        AssertAndAdvance(TokenID.Semi); // not optional on DO

        return node;
    }

    private ForStatement parseFor() throws FeatureNotSupportedException {
        ForStatement node = new ForStatement();
        advance(); // advance over FOR

        AssertAndAdvance(TokenID.LParen);

        if (curtok.id != TokenID.Semi) {
            node.Init.add(ParseExpression());
            while (curtok.id == TokenID.Comma) {
                AssertAndAdvance(TokenID.Comma);
                node.Init.add(ParseExpression());
            }
        }
        AssertAndAdvance(TokenID.Semi);

        if (curtok.id != TokenID.Semi) {
            node.Test.add(ParseExpression());
            while (curtok.id == TokenID.Comma) {
                AssertAndAdvance(TokenID.Comma);
                node.Test.add(ParseExpression());
            }
        }
        AssertAndAdvance(TokenID.Semi);

        if (curtok.id != TokenID.RParen) {
            node.Inc.add(ParseExpression());
            while (curtok.id == TokenID.Comma) {
                AssertAndAdvance(TokenID.Comma);
                node.Inc.add(ParseExpression());
            }
        }
        AssertAndAdvance(TokenID.RParen);
        parseBlock(node.Statements);

        if (curtok.id == TokenID.Semi) {
            advance();
        }
        return node;
    }

    private ForEachStatement parseForEach() throws FeatureNotSupportedException {
        ForEachStatement node = new ForEachStatement();
        advance(); // advance over FOREACH

        AssertAndAdvance(TokenID.LParen);
        node.Iterator = parseParamDecl();
        AssertAndAdvance(TokenID.In);
        node.Collection = ParseExpression();
        AssertAndAdvance(TokenID.RParen);

        //node.statements = parseBlock().statements;

        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private BreakStatement parseBreak() throws FeatureNotSupportedException {
        BreakStatement node = new BreakStatement();
        advance(); // advance over BREAK

        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private ContinueStatement parseContinue() throws FeatureNotSupportedException {
        ContinueStatement node = new ContinueStatement();
        advance(); // advance over Continue

        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private GotoStatement parseGoto() throws FeatureNotSupportedException {
        advance();
        GotoStatement gn = new GotoStatement();
        if (curtok.id == TokenID.Case) {
            advance();
            gn.IsCase = true;
        } else if (curtok.id == TokenID.Default) {
            advance();
            gn.IsDefaultCase = true;
        }
        if (!gn.IsDefaultCase) {
            gn.Target = ParseExpression();
        }
        AssertAndAdvance(TokenID.Semi);
        return gn;
    }

    private ReturnStatement parseReturn() throws FeatureNotSupportedException {
        ReturnStatement node = new ReturnStatement();
        advance(); // advance over Return

        if (curtok.id == TokenID.Semi) {
            advance();
        } else {
            node.ReturnValue = ParseExpression();
            AssertAndAdvance(TokenID.Semi);
        }
        return node;
    }

    private ThrowNode parseThrow() throws FeatureNotSupportedException {
        ThrowNode node = new ThrowNode();
        advance(); // advance over Throw

        if (curtok.id != TokenID.Semi) {
            node.ThrowExpression = ParseExpression();
        }

        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private TryStatement parseTry() throws FeatureNotSupportedException {
        TryStatement node = new TryStatement();
        advance(); // advance over Try
        parseBlock(node.TryBlock);
        while (curtok.id == TokenID.Catch) {
            CatchNode cn = new CatchNode();
            node.CatchBlocks.add(cn);

            advance(); // over catch
            if (curtok.id == TokenID.LParen) {
                advance(); // over lparen
                cn.ClassType = parseType();

                if (curtok.id == TokenID.Ident) {
                    cn.Identifier = new IdentifierExpression(new String[]{strings.get(curtok.data)});
                    advance();
                }
                AssertAndAdvance(TokenID.RParen);
                parseBlock(cn.CatchBlock);
            } else {
                parseBlock(cn.CatchBlock);
                break; // must be last catch block if not a specific catch clause
            }
        }
        if (curtok.id == TokenID.Finally) {
            advance(); // over finally
            FinallyNode fn = new FinallyNode();
            node.FinallyBlock = fn;
            parseBlock(fn.FinallyBlock);
        }

        if (curtok.id == TokenID.Semi) {
            advance();
        }
        return node;
    }

    private CheckedStatement parseChecked() throws FeatureNotSupportedException {
        CheckedStatement node = new CheckedStatement();
        advance(); // advance over Checked

        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private UncheckedStatement parseUnchecked() throws FeatureNotSupportedException {
        UncheckedStatement node = new UncheckedStatement();
        advance(); // advance over Uncecked

        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private LockStatement parseLock() throws FeatureNotSupportedException {
        LockStatement node = new LockStatement();
        advance(); // advance over Lock

        AssertAndAdvance(TokenID.LParen);
        node.Target = ParseExpression();
        AssertAndAdvance(TokenID.RParen);
        parseBlock(node.Statements);

        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private UsingStatement ParseUsing() throws FeatureNotSupportedException {
        UsingStatement node = new UsingStatement();
        advance(); // advance over Using

        AssertAndAdvance(TokenID.LParen);
        node.Resource = ParseExpression();
        AssertAndAdvance(TokenID.RParen);
        parseBlock(node.Statements);

        if (curtok.id == TokenID.Semi)
            advance();
        return node;
    }

    private void ParseUnsafeCode() throws FeatureNotSupportedException {
        // todo: fully parse unsafe code

        advance(); // over 'unsafe'
        AssertAndAdvance(TokenID.LCurly);

        int lcount = 1;
        while (curtok.id != TokenID.Eof && lcount != 0) {
            advance();
            if (curtok.id == TokenID.RCurly) {
                lcount--;
            } else if (curtok.id == TokenID.LCurly) {
                lcount++;
            }
        }
        if (curtok.id != TokenID.Eof) {
            advance(); // over RCurly
        }
    }

    // expressions
    private ExpressionNode ParseExpression(int endToken) throws FeatureNotSupportedException {
        int id = curtok.id;
        while (id != endToken && id != TokenID.Eof &&
                id != TokenID.Semi && id != TokenID.RParen &&
                id != TokenID.Comma && id != TokenID.Colon) {
            ParseExpressionSegment();
            id = curtok.id;
        }
        return exprStack.pop();
    }

    private ExpressionNode ParseExpression() throws FeatureNotSupportedException {
        int id = curtok.id;
        while (id != TokenID.Eof && id != TokenID.RCurly &&
                id != TokenID.Semi && id != TokenID.RParen &&
                id != TokenID.Comma && id != TokenID.Colon) {
            ParseExpressionSegment();
            id = curtok.id;
        }
        return exprStack.pop();
    }

    private void ParseExpressionSegment() throws FeatureNotSupportedException {
        // arraycre		new			: type : [{
        // literal		(lit)
        // simpleName	ident
        // parenExpr	LParen		: expr
        // memAccess	pexpr		: Dot
        //				pdefType	: Dot
        // invoke		pexpr		: LParen
        // elemAccess	noArrCreExpr: LBracket
        // thisAccess	this
        // baseAccess	base		: Dot
        //				base		: LBracket
        // postInc		pexpr		: ++
        // postDec		pexpr		: --
        // objCre		new			: type : LParen
        // delgCre		new			: delgType : LParen
        // typeof		typeof		: LParen
        // checked		checked		: LParen
        // unchecked	unchecked	: LParen
        ExpressionNode tempNode = null;
        int startToken = curtok.id;
        switch (curtok.id) {
            case TokenID.NullLiteral:
                exprStack.push(new NullPrimitive());
                advance();
                break;

            case TokenID.TrueLiteral:
                exprStack.push(new BooleanPrimitive(true));
                advance();
                ParseContinuingPrimary();
                break;

            case TokenID.FalseLiteral:
                exprStack.push(new BooleanPrimitive(false));
                advance();
                ParseContinuingPrimary();
                break;

            case TokenID.IntLiteral:
                exprStack.push(new IntegralPrimitive(strings.get(curtok.data), IntegralType.Int));
                advance();
                ParseContinuingPrimary();
                break;
            case TokenID.UIntLiteral:
                exprStack.push(new IntegralPrimitive(strings.get(curtok.data), IntegralType.UInt));
                advance();
                ParseContinuingPrimary();
                break;
            case TokenID.LongLiteral:
                exprStack.push(new IntegralPrimitive(strings.get(curtok.data), IntegralType.Long));
                advance();
                ParseContinuingPrimary();
                break;
            case TokenID.ULongLiteral:
                exprStack.push(new IntegralPrimitive(strings.get(curtok.data), IntegralType.ULong));
                advance();
                ParseContinuingPrimary();
                break;

            case TokenID.RealLiteral:
                exprStack.push(new RealPrimitive(strings.get(curtok.data)));
                advance();
                ParseContinuingPrimary();
                break;

            case TokenID.CharLiteral:
                exprStack.push(new CharPrimitive(strings.get(curtok.data)));
                advance();
                ParseContinuingPrimary();
                break;

            case TokenID.StringLiteral:
                String sval = strings.get(curtok.data);
                exprStack.push(new StringPrimitive(sval));
                advance();
                ParseContinuingPrimary();
                break;

            case TokenID.Bool:
            case TokenID.Byte:
            case TokenID.Char:
            case TokenID.Decimal:
            case TokenID.Double:
            case TokenID.Float:
            case TokenID.Int:
            case TokenID.Long:
            case TokenID.Object:
            case TokenID.SByte:
            case TokenID.Short:
            case TokenID.String:
            case TokenID.UInt:
            case TokenID.ULong:
            case TokenID.UShort:
                IdentifierExpression qe = parseQualifiedIdentifier();
                exprStack.push(qe);
                ParseContinuingPrimary();
                break;

            case TokenID.Plus:
                tempNode = ConsumeBinary(startToken);
                if (tempNode != null) {
                    exprStack.push(new UnaryExpression(startToken, tempNode)); // unary
                }
                break;
            case TokenID.Minus:
                tempNode = ConsumeBinary(startToken);
                if (tempNode != null) {
                    exprStack.push(new UnaryExpression(startToken, tempNode)); // unary
                }
                break;

            case TokenID.Is:
            case TokenID.As:
            case TokenID.Star:
            case TokenID.Slash:
            case TokenID.Percent:
            case TokenID.ShiftLeft:
            case TokenID.ShiftRight:
            case TokenID.Less:
            case TokenID.Greater:
            case TokenID.LessEqual:
            case TokenID.GreaterEqual:
            case TokenID.EqualEqual:
            case TokenID.NotEqual:
            case TokenID.BAnd:
            case TokenID.BXor:
            case TokenID.BOr:
            case TokenID.And:
            case TokenID.Or:
                ConsumeBinary(startToken);
                break;


            case TokenID.Not:
            case TokenID.Tilde:
            case TokenID.PlusPlus:
            case TokenID.MinusMinus:
                ConsumeUnary(startToken);
                break;

            case TokenID.Question:
                ExpressionNode condTest = exprStack.pop();
                advance();
                ExpressionNode cond1 = ParseExpression(TokenID.Colon);
                AssertAndAdvance(TokenID.Colon);
                ExpressionNode cond2 = ParseExpression();

                exprStack.push(new ConditionalExpression(condTest, cond1, cond2));
                break;
                // keywords
            case TokenID.Ref:
                advance();
                ParseExpressionSegment();
                exprStack.push(new RefNode(exprStack.pop()));
                break;

            case TokenID.Out:
                advance();
                ParseExpressionSegment();
                exprStack.push(new OutNode(exprStack.pop()));
                break;

            case TokenID.This:
                exprStack.push(parseQualifiedIdentifier());
                ParseContinuingPrimary();
                break;

            case TokenID.Void:
                // this can happen in typeof(void), nothing can follow
                advance();
                exprStack.push(new VoidPrimitive());
                break;

            case TokenID.Base:
                advance();
                int newToken = curtok.id;
                if (newToken == TokenID.Dot) {
                    advance();
                    String baseIdent = strings.get(curtok.data);
                    IdentifierExpression ide = new IdentifierExpression(new String[]{baseIdent});
                    advance();
                    exprStack.push(new BaseAccessExpression(ide));

                } else if (newToken == TokenID.LBracket) {
                    advance();
                    ExpressionList el = ParseExpressionList(TokenID.RBracket);
                    exprStack.push(new BaseAccessExpression(el));
                }
                ParseContinuingPrimary();
                break;

            case TokenID.Typeof:
                advance();
                AssertAndAdvance(TokenID.LParen);
                exprStack.push(new TypeOfExpression(ParseExpression(TokenID.RParen)));
                AssertAndAdvance(TokenID.RParen);
                ParseContinuingPrimary();
                break;

            case TokenID.Checked:
                advance();
                AssertAndAdvance(TokenID.LParen);
                ParseExpressionSegment();
                exprStack.push(new CheckedExpression(exprStack.pop()));
                AssertAndAdvance(TokenID.RParen);
                ParseContinuingPrimary();
                break;

            case TokenID.Unchecked:
                advance();
                AssertAndAdvance(TokenID.LParen);
                ParseExpressionSegment();
                exprStack.push(new UncheckedExpression(exprStack.pop()));
                AssertAndAdvance(TokenID.RParen);
                ParseContinuingPrimary();
                break;

            case TokenID.Equal:
            case TokenID.PlusEqual:
            case TokenID.MinusEqual:
            case TokenID.StarEqual:
            case TokenID.SlashEqual:
            case TokenID.PercentEqual:
            case TokenID.BAndEqual:
            case TokenID.BOrEqual:
            case TokenID.BXorEqual:
            case TokenID.ShiftLeftEqual:
            case TokenID.ShiftRightEqual:
                int op = curtok.id;
                advance();


                if (exprStack.size() > 0 && !(exprStack.peek() instanceof PrimaryExpression) && !(exprStack.peek() instanceof UnaryCastExpression)) {
                    ReportError("Left hand side of assignment must be a variable.");
                }
                ExpressionNode assignVar = exprStack.pop();
                ExpressionNode rightSide = ParseExpression();
                exprStack.push(new AssignmentExpression(op, assignVar, rightSide));
                break;


            case TokenID.LCurly:
                advance();
                ArrayInitializerExpression aie = new ArrayInitializerExpression();
                exprStack.push(aie);
                aie.Expressions = ParseExpressionList(TokenID.RCurly);
                break;

            case TokenID.New:
                advance();

                TypeNode newType = parseType();
                if (curtok.id == TokenID.LParen) {
                    advance();
                    ExpressionList newList = ParseExpressionList(TokenID.RParen);
                    exprStack.push(new ObjectCreationExpression(newType, newList));
                } else if (curtok.id == TokenID.LBracket) {
                    ParseArrayCreation(newType);
                }
                ParseContinuingPrimary();
                break;

            case TokenID.Ident:

                //test for local decl
                boolean isDecl = isAfterType();
                if (isDecl) {
                    ParseLocalDeclaration();
                } else {
                    exprStack.push(parseQualifiedIdentifier());
                    ParseContinuingPrimary();
                }
                break;

            case TokenID.LParen:
                advance();
                ParseCastOrGroup();
                break;

            default:
                //Thilina
                AssertAndAdvance(TokenID.CharLiteral);
                //RecoverFromError("Unhandled case in ParseExpressionSegment", curtok.id); // todo: fill out error report
                break;
        }
    }

    private void ConsumeUnary(int startOp) throws FeatureNotSupportedException {
        advance();
        ParseExpressionSegment();
        while (precedence[(int) curtok.id] > precedence[(int) startOp]) {
            ParseExpressionSegment();
        }
        UnaryExpression uNode = new UnaryExpression(startOp);
        uNode.Child = exprStack.pop();
        exprStack.push(uNode);
    }

    private ExpressionNode ConsumeBinary(int startOp) throws FeatureNotSupportedException {
        ExpressionNode result = null;
        if ((exprStack.size() == 0 || precedence[(int) tokens.get(index - 2).id] > 0)) {
            // assert +,-,!,~,++,--,cast
            advance();
            ParseExpressionSegment();
            while (precedence[(int) curtok.id] > precedence[(int) startOp]) {
                ParseExpressionSegment();
            }
            result = exprStack.pop(); // this signals it was a unary operation
        } else {
            advance();
            BinaryExpression bNode = new BinaryExpression(startOp);
            bNode.Left = exprStack.pop();
            exprStack.push(bNode); // push node
            ParseExpressionSegment(); // right side
            // consume now or let next op consume?
            while (precedence[(int) curtok.id] > precedence[(int) startOp]) {
                ParseExpressionSegment();
            }
            bNode.Right = exprStack.pop();
        }
        return result;
    }

    private boolean isAfterType() {
        boolean result = false;
        if (exprStack.size() > 0) {
            if (exprStack.peek() instanceof IdentifierExpression) {
                IdentifierExpression ie = (IdentifierExpression) exprStack.pop();
                exprStack.push(new TypeNode(ie));
                result = true;
            } else
            if (exprStack.peek() instanceof TypeNode || exprStack.peek() instanceof MemberAccessExpression)// PrimaryExpressionNode)//
            {
                result = true;
            }
        }
        return result;
    }

    private ExpressionList ParseExpressionList(int termChar) throws FeatureNotSupportedException {
        ExpressionList list = new ExpressionList();
        int id = curtok.id;
        while (id != TokenID.Eof && id != termChar) {
            while (id != TokenID.Eof && id != termChar && id != TokenID.Comma) {
                ParseExpressionSegment();
                id = curtok.id;
            }

            if (curtok.id == TokenID.Comma) {
                advance(); // over comma
            }
            list.getExpressions().add(exprStack.pop());
            id = curtok.id;
        }
        if (curtok.id == termChar) {
            advance();
        }
        return list;
    }

    private void ParseLocalDeclaration() throws FeatureNotSupportedException {
        IdentifierExpression declIdentifier = parseQualifiedIdentifier();
        IType type = (IType) exprStack.pop();
        LocalDeclarationStatement lnode = new LocalDeclarationStatement();
        lnode.Identifiers.add(declIdentifier);

        if (isLocalConst) {
            lnode.IsConstant = true;
        }
        isLocalConst = false;
        lnode.Type = type;

        // a using statement can hold a local decl without a semi, thus the rparen
        while (curtok.id != TokenID.Eof && curtok.id != TokenID.Semi && curtok.id != TokenID.RParen) {
            while (curtok.id == TokenID.Comma) {
                advance(); // over comma
                declIdentifier = parseQualifiedIdentifier();
                lnode.Identifiers.add(declIdentifier);
            }
            if (curtok.id == TokenID.Equal) {
                advance(); // over equal
                lnode.RightSide = ParseExpression(TokenID.Comma);

                if (curtok.id == TokenID.Comma) {
                    exprStack.push(lnode);
                    lnode = new LocalDeclarationStatement();
                    lnode.Type = type;
                }
            }
        }
        exprStack.push(lnode);
    }

    private void ParseCastOrGroup() throws FeatureNotSupportedException {
        ExpressionNode interior = ParseExpression();
        AssertAndAdvance(TokenID.RParen);
        int rightTok = curtok.id;

        // check if this is terminating - need better algorithm here :(
        // todo: this can probably be simplified (and correctified!) with new expression parsing style
        if (!(interior instanceof IType) ||
                rightTok == TokenID.Semi ||
                rightTok == TokenID.RParen ||
                rightTok == TokenID.RCurly ||
                rightTok == TokenID.RBracket ||
                rightTok == TokenID.Comma) {
            // was group for sure
            exprStack.push(new ParenthesizedExpression(interior));
            ParseContinuingPrimary();
        } else {
            // push a pe just in case upcoming is binary expr
            ParenthesizedExpression pe = new ParenthesizedExpression();
            exprStack.push(pe);

            // find out what is on right
            ParseExpressionSegment();
            ExpressionNode peek = exprStack.peek();

            if (peek instanceof PrimaryExpression || peek instanceof UnaryExpression) {
                // cast
                UnaryCastExpression castNode = new UnaryCastExpression();
                castNode.Type = (IType) interior;
                castNode.Child = exprStack.pop();
                // need to pop off the 'just in case' pe
                exprStack.pop();
                exprStack.push(castNode);
            } else {
                // group
                pe.Expression = interior;
                ParseContinuingPrimary();
            }
        }
    }

    private void ParseArrayCreation(TypeNode type) throws FeatureNotSupportedException {
        ArrayCreationExpression arNode = new ArrayCreationExpression();
        exprStack.push(arNode);

        arNode.Type = type;
        int nextToken = TokenID.Invalid;
        if (tokens.size() > index) {
            nextToken = tokens.get(index).id;
        }
        // this tests for literal size declarations on first rank specifiers
        if (nextToken != TokenID.Invalid && nextToken != TokenID.Comma && nextToken != TokenID.RBracket) {
            advance(); // over lbracket
            arNode.RankSpecifier = ParseExpressionList(TokenID.RBracket);
        }
        // now any 'rank only' specifiers (without size decls)
        while (curtok.id == TokenID.LBracket) {
            advance(); // over lbracket
            int commaCount = 0;
            while (curtok.id == TokenID.Comma) {
                commaCount++;
                advance();
            }
            arNode.AdditionalRankSpecifiers.add(commaCount);
            AssertAndAdvance(TokenID.RBracket);
        }
        if (curtok.id == TokenID.LCurly) {
            advance();
            arNode.Initializer = new ArrayInitializerExpression();
            arNode.Initializer.Expressions = ParseExpressionList(TokenID.RCurly);
        }
    }

    private void ParseContinuingPrimary() throws FeatureNotSupportedException {
        boolean isContinuing = curtok.id == TokenID.LBracket || curtok.id == TokenID.Dot || curtok.id == TokenID.LParen;
        while (isContinuing) {
            switch (curtok.id) {
                case TokenID.Dot:
                    ParseMemberAccess();
                    break;
                case TokenID.LParen:
                    ParseInvocation();
                    break;
                case TokenID.LBracket:
                    isContinuing = ParseElementAccess();
                    break;
                default:
                    isContinuing = false;
                    break;
            }
            if (isContinuing) {
                isContinuing = curtok.id == TokenID.LBracket || curtok.id == TokenID.Dot || curtok.id == TokenID.LParen;
            }
        }
        // can only be one at end
        if (curtok.id == TokenID.PlusPlus) {
            advance();
            exprStack.push(new PostIncrementExpression(exprStack.pop()));
        } else if (curtok.id == TokenID.MinusMinus) {
            advance();
            exprStack.push(new PostDecrementExpression(exprStack.pop()));
        }
    }

    private void ParseMemberAccess() throws FeatureNotSupportedException {
        advance(); // over dot
        if (curtok.id != TokenID.Ident) {
            ReportError("Right side of member access must be identifier");
        }
        IdentifierExpression identifier = parseQualifiedIdentifier();
        if (exprStack.size() > 0 && exprStack.peek() instanceof IMemberAccessible) {
            IMemberAccessible ima = (IMemberAccessible) exprStack.pop();
            exprStack.push(new MemberAccessExpression(ima, identifier));
        } else {
            ReportError("Left side of member access must be PrimaryExpression or PredefinedType.");
        }
    }

    private void ParseInvocation() throws FeatureNotSupportedException {
        advance(); // over lparen

        PrimaryExpression leftSide = (PrimaryExpression) exprStack.pop();
        ExpressionList list = ParseExpressionList(TokenID.RParen);
        exprStack.push(new InvocationExpression(leftSide, list));
    }

    private boolean ParseElementAccess() throws FeatureNotSupportedException {
        boolean isElementAccess = true;
        advance(); // over lbracket
        ExpressionNode type = exprStack.pop(); // the caller pushed, so must have at least one element

        // case one is actaully a type decl (like T[,,]), not element access (like T[2,4])
        // in this case we need to push the type, and abort parsing the continuing
        if (curtok.id == TokenID.Comma || curtok.id == TokenID.RBracket) {
            isElementAccess = false;
            if (type instanceof IdentifierExpression) {
                // needs t oconvert to typeNode
                IdentifierExpression ie = (IdentifierExpression) type;
                TypeNode tp = new TypeNode(ie);
                exprStack.push(tp);
                ParseArrayRank(tp);
            }
        } else {
            // element access case
            if (type instanceof PrimaryExpression) {
                PrimaryExpression tp = (PrimaryExpression) type;
                ExpressionList el = ParseExpressionList(TokenID.RBracket);
                exprStack.push(new ElementAccessExpression(tp, el));
            } else {
                ReportError("Left side of Element Access must be primary expression.");
            }
        }

        return isElementAccess;
    }

    private void ParseArrayRank(TypeNode type) throws FeatureNotSupportedException {
        // now any 'rank only' specifiers (without size decls)
        boolean firstTime = true;
        while (curtok.id == TokenID.LBracket || firstTime) {
            if (!firstTime) {
                advance();
            }
            firstTime = false;
            int commaCount = 0;
            while (curtok.id == TokenID.Comma) {
                commaCount++;
                advance();
            }
            type.RankSpecifiers.add(commaCount);
            AssertAndAdvance(TokenID.RBracket);
        }
    }

    // utility
    private void RecoverFromError(int id) throws FeatureNotSupportedException {
        RecoverFromError("", id);
    }

    private void RecoverFromError(String message, int id) throws FeatureNotSupportedException {
        String msg = "Error: Expected " + id + " found: " + curtok.id;
        if (message != null)
            msg = message + msg;

        ReportError(msg);

        if (id == 20) {
            success = false;
            throw new FeatureNotSupportedException("Delegates not supported");
        }
        advance();
//            AssertAndAdvance(id);
    }

    private void ReportError(String message) {
        System.out.println(message + " in token " + index + " [" + curtok.id + "]");
    }

    private void AssertAndAdvance(int id) throws FeatureNotSupportedException {
        if (curtok.id != id) {
            RecoverFromError(id);
        }
        advance();
    }

    private void advance() throws FeatureNotSupportedException {
        boolean skipping = true;
        do {
            if (index < tokens.size()) {
                curtok = tokens.get(index);
            } else {
                curtok = EOF;
            }

            index++;

            switch (curtok.id) {
                case TokenID.SingleComment:
                    break;
                case TokenID.MultiComment:
                    String[] s = strings.get(curtok.data).split("\n");
                    lineCount += s.length - 1;
                    break;

                case TokenID.Newline:
                    lineCount++;
                    break;

                case TokenID.Hash:
                    // preprocessor directives
                    if (!inPPDirective) {
                        parsePreprocessorDirective();
                        if (curtok.id != TokenID.Newline &&
                                curtok.id != TokenID.SingleComment &&
                                curtok.id != TokenID.MultiComment &&
                                curtok.id != TokenID.Hash) {
                            skipping = false;
                        } else if (curtok.id == TokenID.Hash) {
                            index--;
                        }
                    } else {
                        skipping = false;
                    }
                    break;

                default:
                    skipping = false;
                    break;
            }
        } while (skipping);
    }


    private void SkipToEOL(int startLine) {
        if (lineCount > startLine) {
            return;
        }
        boolean skipping = true;
        do {
            if (index < tokens.size()) {
                curtok = tokens.get(index);
            } else {
                curtok = EOF;
                skipping = false;
            }
            index++;

            if (curtok.id == TokenID.Newline) {
                lineCount++;
                skipping = false;
            }
        } while (skipping);
    }

    private void SkipToNextHash() {
        boolean skipping = true;
        do {
            if (index < tokens.size()) {
                curtok = tokens.get(index);
            } else {
                curtok = EOF;
                skipping = false;
            }
            index++;

            if (curtok.id == TokenID.Hash) {
                skipping = false;
            } else if (curtok.id == TokenID.Newline) {
                lineCount++;
            }
        } while (skipping);
    }

    private void SkipToElseOrEndIf() {
        // advance to elif, else, or endif
        int endCount = 1;
        boolean firstPassHash = curtok.id == TokenID.Hash;
        while (endCount > 0) {
            if (!firstPassHash) {
                SkipToNextHash();
            }
            firstPassHash = false;

            if (!(index < tokens.size())) {
                break;
            }
            if (tokens.get(index).id == TokenID.Ident) {
                String sKind = strings.get(tokens.get(index).data);
                if (sKind.equals("endif")) {
                    endCount--;
                } else if (sKind.equals("elif")) {
                    if (endCount == 1) {
                        break;
                    }
                }
            } else if (tokens.get(index).id == TokenID.If) {
                endCount++;
            } else if (tokens.get(index).id == TokenID.Else) {
                if (endCount == 1) {
                    break;
                }
            } else {
                break;
            }
        }
    }


    public boolean isSuccess() {
        return success;
    }

}
