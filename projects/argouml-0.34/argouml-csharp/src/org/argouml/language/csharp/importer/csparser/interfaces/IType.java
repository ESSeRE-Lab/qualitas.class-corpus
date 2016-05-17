package org.argouml.language.csharp.importer.csparser.interfaces;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 1:49:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IType
    {
        public List<Integer> rankSpecifiers =new ArrayList<Integer>();

        void ToSource(StringBuilder sb);
    }