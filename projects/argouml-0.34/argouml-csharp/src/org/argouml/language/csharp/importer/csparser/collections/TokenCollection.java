package org.argouml.language.csharp.importer.csparser.collections;

import org.argouml.language.csharp.importer.csparser.main.Token;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:45:36 PM
 */
public class TokenCollection extends ArrayList<Token>
	{
		public  String ToString()
		{
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < this.size(); i++)
			{
				sb.append(i + ": " + this.get(i).id + "\n");
			}
			return sb.toString();
		}
	}
