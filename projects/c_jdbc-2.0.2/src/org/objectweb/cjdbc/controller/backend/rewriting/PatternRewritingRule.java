/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
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

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * This class defines a PatternRewritingRule
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class PatternRewritingRule extends AbstractRewritingRule
{
  private char     tokenDelimiter = '?';
  private String[] patternArray;
  private String[] rewriteArray;

  /**
   * Creates a new <code>PatternRewritingRule.java</code> object
   * 
   * @param queryPattern SQL pattern to match
   * @param rewrite rewritten SQL query
   * @param caseSensitive true if matching is case sensitive
   * @param stopOnMatch true if rewriting must stop after this rule if it
   *          matches.
   */
  public PatternRewritingRule(String queryPattern, String rewrite,
      boolean caseSensitive, boolean stopOnMatch)
  {
    super(queryPattern, rewrite, caseSensitive, stopOnMatch);

    // Parse queryPattern and rewrite to extract the parameters ?1 ?2 ...
    StringTokenizer patternTokenizer = new StringTokenizer(queryPattern, String
        .valueOf(tokenDelimiter), true);
    patternArray = new String[patternTokenizer.countTokens()];
    int i = 0;
    try
    {
      do
      {
        patternArray[i] = patternTokenizer.nextToken();
        if (patternArray[i].charAt(0) == tokenDelimiter)
        { // We found a delimiter (?)
          String nextToken = patternTokenizer.nextToken();
          // Add the parameter number (only works with 1 digit parameters)
          //For example ?124 will be recognized as ?1
          patternArray[i] += nextToken.charAt(0);
          i++;
          if (nextToken.length() > 1)
            // This is the next token
            patternArray[i] = nextToken.substring(1);
        }
        i++;
      }
      while (patternTokenizer.hasMoreTokens());
    }
    catch (RuntimeException e)
    {
      throw new RuntimeException("Malformed query pattern: " + queryPattern);
    }
    StringTokenizer rewriteTokenizer = new StringTokenizer(rewrite, String
        .valueOf(tokenDelimiter), true);
    rewriteArray = new String[rewriteTokenizer.countTokens()];
    i = 0;
    try
    {
      do
      {
        rewriteArray[i] = rewriteTokenizer.nextToken();
        if (rewriteArray[i].charAt(0) == tokenDelimiter)
        { // We found a delimiter (?)
          String nextToken = rewriteTokenizer.nextToken();
          // Add the parameter number (only works with 1 digit parameters)
          //For example ?124 will be recognized as ?1
          rewriteArray[i] += nextToken.charAt(0);
          i++;
          if (nextToken.length() > 1)
            // This is the next token
            rewriteArray[i] = nextToken.substring(1);
        }
        i++;
      }
      while (rewriteTokenizer.hasMoreTokens());
    }
    catch (RuntimeException e1)
    {
      throw new RuntimeException("Malformed rewrite element: " + rewrite);
    }
  }

  /**
   * @see org.objectweb.cjdbc.controller.backend.rewriting.AbstractRewritingRule#rewrite(java.lang.String)
   */
  public String rewrite(String sqlQuery)
  {
    Hashtable tokens = null; // Parameters value in the query
    String lastParameter = null;
    String currentToken;
    int oldIndex = 0;
    int newIndex = 0;

    // Check for match and collect parameters into tokens
    for (int i = 0; i < patternArray.length; i++)
    {
      currentToken = patternArray[i];
      if (currentToken == null)
        break; // Last token was a parameter
      if (currentToken.charAt(0) == tokenDelimiter)
      { // A new parameter is expected
        lastParameter = currentToken;
        continue;
      }
      // Here is the value of the parameter
      newIndex = sqlQuery.indexOf(currentToken, oldIndex);
      if (newIndex == -1)
      { // No match
        hasMatched = false;
        return sqlQuery;
      }

      if (lastParameter != null)
      { // Add the parameter value
        if (tokens == null)
          tokens = new Hashtable();
        tokens.put(lastParameter, sqlQuery.substring(oldIndex, newIndex));
      }
      oldIndex = newIndex + currentToken.length();
    }
    // Last parameter
    if (newIndex < sqlQuery.length())
    {
      if (tokens != null)
      {
        if (tokens.containsKey(lastParameter))
        { // No match on the end of the pattern
          hasMatched = false;
          return sqlQuery;
        }
        else
          tokens.put(lastParameter, sqlQuery.substring(oldIndex));
      }
      // Here, we probably had a match without parameters. What's the point?
    }

    hasMatched = true;

    StringBuffer rewrittenQuery = new StringBuffer();
    for (int i = 0; i < rewriteArray.length; i++)
    {
      currentToken = rewriteArray[i];
      if (currentToken == null)
        break; // Last token was a parameter
      if (currentToken.charAt(0) != tokenDelimiter)
        rewrittenQuery.append(currentToken);
      else
        rewrittenQuery.append(tokens.get(currentToken));
    }
    return rewrittenQuery.toString();
  }
}