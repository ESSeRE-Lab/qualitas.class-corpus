package org.argouml.language.csharp.importer.csparser.interfaces;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 12:24:24 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ISourceCode
	{
		/// <summary>
		/// Returns the source code representation of the node.
		/// </summary>
		/// <returns>Returns the source code representation of the node.</returns>
		void ToSource(StringBuilder sb);
	}