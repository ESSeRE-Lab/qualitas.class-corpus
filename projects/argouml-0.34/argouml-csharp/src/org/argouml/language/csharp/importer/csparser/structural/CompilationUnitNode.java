package org.argouml.language.csharp.importer.csparser.structural;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:17:02 PM
 */
public class CompilationUnitNode extends BaseNode
	{
		public NodeCollection<UsingDirectiveNode> UsingDirectives;
		public NodeCollection<AttributeNode> GlobalAttributes;
		public NodeCollection<NamespaceNode> Namespaces;
		public NamespaceNode DefaultNamespace;
		public IdentifierExpression defaultNamespaceName = new IdentifierExpression(new String[] { "DefaultNamespace" });

		public CompilationUnitNode()
		{
			UsingDirectives = new NodeCollection<UsingDirectiveNode>();
			GlobalAttributes = new NodeCollection<AttributeNode>();
			Namespaces = new NodeCollection<NamespaceNode>();

			DefaultNamespace = new NamespaceNode();
			DefaultNamespace.Name = defaultNamespaceName;
			Namespaces.add(DefaultNamespace);
		}







        public  void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
			}

			for(UsingDirectiveNode node :UsingDirectives)
			{
				node.ToSource(sb);
			}

			for (NamespaceNode node :Namespaces)
			{
				node.ToSource(sb);
			}
        }

	}