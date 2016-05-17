package org.argouml.language.csharp.importer.csparser.structural;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.IdentifierExpression;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:23:26 PM
 */
public class UsingDirectiveNode extends BaseNode
	{
		public boolean IsAlias = false;

		public IdentifierExpression Target;




        private IdentifierExpression aliasName;

         public IdentifierExpression getAliasName() {
            return aliasName;
        }

        public void setAliasName(IdentifierExpression aliasName) {
            IsAlias = true;
            this.aliasName = aliasName;
        }




        public  void ToSource(StringBuilder sb)
        {
			sb.append("using ");

			// Target
			Target.ToSource(sb);

			if (IsAlias)
			{
				sb.append(" = ");
				aliasName.ToSource(sb);
			}
			sb.append(";");
			this.NewLine(sb);
        }
	}
