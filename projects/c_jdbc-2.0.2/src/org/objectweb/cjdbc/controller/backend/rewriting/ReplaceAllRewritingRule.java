/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: c-jdbc@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.backend.rewriting;

/**
 * This class defines a ReplaceAllRewritingRule. Replace all instance of a
 * <code>String</code> token by another <code>String</code> token
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ReplaceAllRewritingRule extends AbstractRewritingRule
{

  /**
   * @see org.objectweb.cjdbc.controller.backend.rewriting.AbstractRewritingRule#rewrite(java.lang.String)
   */
  public String rewrite(String sqlQuery)
  {
    // Check first if it is a match
    int start;
    if (isCaseSensitive)
      start = sqlQuery.indexOf(queryPattern);
    else
      start = sqlQuery.toLowerCase().indexOf(queryPattern.toLowerCase());
    if (start == -1)
    { // No match
      hasMatched = false;
      return sqlQuery;
    }
    // Match, rewrite the query
    hasMatched = true;

    return replace(sqlQuery, queryPattern, rewrite);
  }

  /**
   * Creates a new <code>ReplaceAllRewritingRule.java</code> object
   * 
   * @param queryPattern SQL pattern to match
   * @param rewrite rewritten SQL query
   * @param caseSensitive true if matching is case sensitive
   * @param stopOnMatch true if rewriting must stop after this rule if it
   *          matches.
   */
  public ReplaceAllRewritingRule(String queryPattern, String rewrite,
      boolean caseSensitive, boolean stopOnMatch)
  {
    super(queryPattern, rewrite, caseSensitive, stopOnMatch);
  }

  private static String replace(String s, String oldText, String newText)
  {
    final int oldLength = oldText.length();
    final int newLength = newText.length();

    if (oldLength == 0)
      throw new IllegalArgumentException("cannot replace the empty string");

    if (oldText.equals(newText))
      return s;

    int i = 0;
    int x = 0;

    StringBuffer sb = new StringBuffer(s);

    while ((i = sb.indexOf(oldText, x)) > -1)
    {
      sb.delete(i, i + oldLength);
      sb.insert(i, newText);
      x = i + newLength;
    }

    return sb.toString();
  }
}