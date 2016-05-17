package org.argouml.language.csharp.importer.csparser.collections;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.BaseNode;
import org.argouml.language.csharp.importer.csparser.interfaces.ISourceCode;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 1:23:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeCollection<T> extends ArrayList<T> implements ISourceCode
{
    public void ToSource(StringBuilder sb)
        {
            for(T node:this)
            {
                BaseNode b=(BaseNode)node;
                b.ToSource(sb);
            }
        }
}