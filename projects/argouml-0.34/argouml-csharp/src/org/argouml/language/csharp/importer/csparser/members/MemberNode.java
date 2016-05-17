package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.ConstantExpression;
import org.argouml.language.csharp.importer.csparser.enums.Modifier;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 8, 2008
 * Time: 11:05:15 PM
 */
public abstract class MemberNode extends BaseNode
	{
		public long modifiers;


		public List<IdentifierExpression> names = new ArrayList<IdentifierExpression>();

		public TypeNode type;


		public ConstantExpression Value;
	}
