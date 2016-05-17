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
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.controller.backend.rewriting;

/**
 * This class defines a SimpleRewritingRule
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class SimpleRewritingRule extends AbstractRewritingRule
{

  private int queryPatternLength;

  /**
   * Creates a new <code>SimpleRewritingRule.java</code> object
   * 
   * @param queryPattern SQL pattern to match
   * @param rewrite rewritten SQL query
   * @param caseSensitive true if matching is case sensitive
   * @param stopOnMatch true if rewriting must stop after this rule if it
   *          matches.
   */
  public SimpleRewritingRule(String queryPattern, String rewrite,
      boolean caseSensitive, boolean stopOnMatch)
  {
    super(queryPattern, caseSensitive ? rewrite : rewrite.toLowerCase(),
        caseSensitive, stopOnMatch);
    queryPatternLength = queryPattern.length();
  }

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
    if (start == 0)
    {
      if (queryPatternLength < sqlQuery.length())
        // Match at the beginning of the pattern
        return rewrite + sqlQuery.substring(queryPatternLength);
      else
        // The query was exactly the pattern
        return rewrite;
    }
    else
    {
      if (start + queryPatternLength < sqlQuery.length())
        return sqlQuery.substring(0, start) + rewrite
            + sqlQuery.substring(start + queryPatternLength);
      else
        // Match at the end of the pattern
        return sqlQuery.substring(0, start) + rewrite;
    }
  }

}