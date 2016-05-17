package org.argouml.language.csharp.importer.csparser.statements;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:42:02 AM
 */
public class BreakStatement extends StatementNode {

    public void ToSource(StringBuilder sb) {
        sb.append("break;");
    }
}
