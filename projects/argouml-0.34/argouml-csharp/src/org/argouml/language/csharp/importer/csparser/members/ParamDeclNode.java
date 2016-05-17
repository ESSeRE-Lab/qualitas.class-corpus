package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:04:49 AM
 */
public class ParamDeclNode extends BaseNode
	{
		public long modifiers;


		public String name;


		public TypeNode type;


        public void ToSource(StringBuilder sb)
		{
			if (attributes != null)
			{
				attributes.ToSource(sb);
				this.NewLine(sb);
			}
			TraceModifiers(modifiers, sb);

			type.ToSource(sb);
			sb.append(" ");

			sb.append(name);

        }
	}