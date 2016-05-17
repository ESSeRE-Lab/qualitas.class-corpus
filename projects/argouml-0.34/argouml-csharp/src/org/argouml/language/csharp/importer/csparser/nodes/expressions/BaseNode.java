package org.argouml.language.csharp.importer.csparser.nodes.expressions;

import org.argouml.language.csharp.importer.csparser.interfaces.ISourceCode;
import org.argouml.language.csharp.importer.csparser.enums.Modifier;
import org.argouml.language.csharp.importer.csparser.structural.AttributeNode;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 12:23:54 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseNode implements ISourceCode
    {
        private final String tabChar = "    ";

        protected static int indent = 0;
        public NodeCollection<AttributeNode> attributes = new NodeCollection<AttributeNode>();


        /// <summary>
        /// Returns the source code representation of the node.
        /// </summary>
        /// <returns>Returns the source code representation of the node.</returns>
        public  void ToSource(StringBuilder sb)
        {
        }

        protected void NewLine(StringBuilder sb)
        {
            sb.append("\n");
            for (int i = 0; i < indent; i++)
            {
                sb.append(tabChar);
            }
        }
        protected void AddTab(StringBuilder sb)
        {
            sb.append(tabChar);
        }

        protected void TraceDottedIdent(String[] target, StringBuilder sb)
        {
            String dot = "";
            for(String s:target)
            {
                sb.append(dot + s);
                dot = ".";
            }
        }

        public void TraceModifiers(long modifiers, StringBuilder sb)
        {
            if ((modifiers & Modifier.New) == Modifier.New)
            {
                sb.append("new ");
            }
            if ((modifiers & Modifier.Public) == Modifier.Public)
            {
                sb.append("public ");
            }
            if ((modifiers & Modifier.Protected) == Modifier.Protected)
            {
                sb.append("protected ");
            }
            if ((modifiers & Modifier.Internal) == Modifier.Internal)
            {
                sb.append("internal ");
            }
            if ((modifiers & Modifier.Private) == Modifier.Private)
            {
                sb.append("private ");
            }
            if ((modifiers & Modifier.Abstract) == Modifier.Abstract)
            {
                sb.append("abstract ");
            }
            if ((modifiers & Modifier.Partial) == Modifier.Partial)
            {
                sb.append("partial ");
            }
            if ((modifiers & Modifier.Sealed) == Modifier.Sealed)
            {
                sb.append("sealed ");
            }
            if ((modifiers & Modifier.Static) == Modifier.Static)
            {
                sb.append("static ");
            }
            if ((modifiers & Modifier.Virtual) == Modifier.Virtual)
            {
                sb.append("virtual ");
            }
            if ((modifiers & Modifier.Override) == Modifier.Override)
            {
                sb.append("override ");
            }
            if ((modifiers & Modifier.Extern) == Modifier.Extern)
            {
                sb.append("extern ");
            }
            if ((modifiers & Modifier.Readonly) == Modifier.Readonly)
            {
                sb.append("readonly ");
            }
            if ((modifiers & Modifier.Volatile) == Modifier.Volatile)
            {
                sb.append("volatile ");
            }
            if ((modifiers & Modifier.Ref) == Modifier.Ref)
            {
                sb.append("ref ");
            }
            if ((modifiers & Modifier.Out) == Modifier.Out)
            {
                sb.append("out ");
            }
            if ((modifiers & Modifier.Params) == Modifier.Params)
            {
                sb.append("params ");
            }
            if ((modifiers & Modifier.Assembly) == Modifier.Assembly)
            {
                sb.append("assembly ");
            }
            if ((modifiers & Modifier.Field) == Modifier.Field)
            {
                sb.append("field ");
            }
            if ((modifiers & Modifier.Event) == Modifier.Event)
            {
                sb.append("event ");
            }
            if ((modifiers & Modifier.Method) == Modifier.Method)
            {
                sb.append("method ");
            }
            if ((modifiers & Modifier.Param) == Modifier.Param)
            {
                sb.append("param ");
            }
            if ((modifiers & Modifier.Property) == Modifier.Property)
            {
                sb.append("property ");
            }
            if ((modifiers & Modifier.Return) == Modifier.Return)
            {
                sb.append("return ");
            }
            if ((modifiers & Modifier.Type) == Modifier.Type)
            {
                sb.append("type ");
            }
            if ((modifiers & Modifier.Module) == Modifier.Module)
            {
                sb.append("module ");
            }
        }
    }