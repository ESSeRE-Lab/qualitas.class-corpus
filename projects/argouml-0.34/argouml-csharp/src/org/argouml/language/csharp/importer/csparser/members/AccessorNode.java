package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:25:37 PM
 */
public class AccessorNode extends MemberNode {
    public String kind;


    public boolean isAbstractOrInterface = false;

    public BlockStatement statementBlock = new BlockStatement();


    public void ToSource(StringBuilder sb) {
        if (attributes != null) {
            attributes.ToSource(sb);
            this.NewLine(sb);
        }
        sb.append(kind);
        if (isAbstractOrInterface) {
            sb.append(";");
        } else {
            this.NewLine(sb);
            // statements
            this.statementBlock.ToSource(sb);
        }
    }

}
