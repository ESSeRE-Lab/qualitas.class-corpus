package org.argouml.language.csharp.importer.csparser.main;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:47:34 PM
 */
public class Token {
		public int id;
		public int data; // index into data table

		public Token(int id)
		{
			this.id = id;
			this.data = -1;
		}
		public Token(int id, int data)
		{
			this.id = id;
			this.data = data;
		}

		public  String ToString()
		{
			return String.valueOf(this.id);
		}

//    public boolean equals(Object obj) {
//        Token t=(Token)obj;
//        if(t.id==id && t.data )
//    }
}
