package org.argouml.language.csharp.importer.csparser.collections;

import org.argouml.language.csharp.importer.csparser.enums.ParseState;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:41:52 PM
 */
public class ParseStateCollection extends ArrayList<ParseState>
	{
		public  String ToString()
		{
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < this.size(); i++)
			{
				sb.append(i + "\t" + this.get(i).toString() + "\n");
			}
			return sb.toString();
		}
	}
