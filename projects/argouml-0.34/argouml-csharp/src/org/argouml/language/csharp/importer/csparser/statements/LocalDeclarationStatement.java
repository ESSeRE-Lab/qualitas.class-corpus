package org.argouml.language.csharp.importer.csparser.statements;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ExpressionNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;
import org.argouml.language.csharp.importer.csparser.interfaces.IType;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:02:15 PM
 */
public class LocalDeclarationStatement extends ExpressionNode
	{
		public LocalDeclarationStatement()
		{
		}
		public LocalDeclarationStatement(IType type, IdentifierExpression identifier, ExpressionNode rightSide)
		{
			this.Type = type;
			this.Identifiers.add(identifier);
			this.RightSide = rightSide;
		}

		public IType Type;


		public List<IdentifierExpression> Identifiers = new ArrayList<IdentifierExpression>();

		public ExpressionNode RightSide;


		public boolean IsConstant = false;


		public  void ToSource(StringBuilder sb)
		{
			if (IsConstant)
			{
				sb.append("const ");
			}
			Type.ToSource(sb);
			sb.append(" ");
			String comma = "";
			for (int i = 0; i < Identifiers.size(); i++)
			{
				sb.append(comma);
				Identifiers.get(i).ToSource(sb);
				comma = ", ";
			}
			if (RightSide != null)
			{
				sb.append(" = ");
				RightSide.ToSource(sb);
			}
		}
	}
