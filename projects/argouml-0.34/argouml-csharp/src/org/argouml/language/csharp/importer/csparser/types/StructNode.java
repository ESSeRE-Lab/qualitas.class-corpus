package org.argouml.language.csharp.importer.csparser.types;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:40:39 PM
 */
public class StructNode extends ClassNode {
    public void ToSource(StringBuilder sb) {
        WriteLocalSource(sb, "struct");
    }
}
