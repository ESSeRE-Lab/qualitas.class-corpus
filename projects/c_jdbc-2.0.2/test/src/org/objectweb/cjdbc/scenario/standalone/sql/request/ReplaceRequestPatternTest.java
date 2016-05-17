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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.standalone.sql.request;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.objectweb.cjdbc.controller.backend.rewriting.AbstractRewritingRule;
import org.objectweb.cjdbc.controller.backend.rewriting.PatternRewritingRule;
import org.objectweb.cjdbc.controller.backend.rewriting.ReplaceAllRewritingRule;
import org.objectweb.cjdbc.controller.backend.rewriting.SimpleRewritingRule;
import org.objectweb.cjdbc.scenario.templates.NoTemplate;
import org.objectweb.cjdbc.scenario.tools.util.MyBufferedReader;

/**
 * <code>ReplaceRequestPatternTest</code> test class.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class ReplaceRequestPatternTest extends NoTemplate
{
  /** File name containing the requests to test. */
  private static final String REPLACE_PATTERN_REQUESTS_FILE = getTextPath("replace-pattern-requests.txt");

  /** List of <code>ParsingResult</code> objects. */
  private ArrayList           results;

  private static boolean      inited                        = false;
  private int                 count                         = 0;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
  {
    synchronized (this)
    {
      if (inited)
        return;
      results = new ArrayList();
      String ruleType = null, matchPattern = null, replacePattern = null, originalQuery = null, replaceQuery = null;
      try
      {
        File file = new File(REPLACE_PATTERN_REQUESTS_FILE);
        MyBufferedReader in = new MyBufferedReader(new FileReader(file),
            "requests");

        String line;
        while ((line = in.readLine()) != null)
        {
          if (line.trim().equals("") || line.startsWith("//")
              || line.startsWith("#"))
            continue;

          ruleType = null;
          matchPattern = null;
          replacePattern = null;
          originalQuery = null;
          replaceQuery = null;

          // Get the score
          ruleType = line;
          matchPattern = in.readNextLine();
          replacePattern = in.readNextLine();
          originalQuery = in.readNextLine();

          // Get expected results for this request
          if (in.readNextLine().equals("true"))
          {
            // Query was a match on the rule
            replaceQuery = in.readNextLine();
            results.add(new ParsingResult(ruleType, matchPattern,
                replacePattern, originalQuery, replaceQuery));
          }
          else
          {
            // No match
            results.add(new ParsingResult(ruleType, matchPattern,
                replacePattern, originalQuery));
          }
        }
      }
      catch (IOException e)
      {
        String error = "An error occurs while parsing requests file: " + e;
        if (matchPattern != null && originalQuery != null)
          error += " (pattern: '" + matchPattern + "', query:'" + originalQuery
              + "')";
        fail(error);
      }
      inited = true;
    }

  }

  /**
   * org.objectweb.cjdbc.common.sql.CreateRequest#parse(DatabaseSchema, int,
   * boolean)
   */
  public void testParseRewriteRequest()
  {
    Iterator it = results.iterator();
    while (it.hasNext())
    {
      System.out.println("Test: " + count++);
      parse((ParsingResult) it.next(), false);
    }
  }

  /**
   * Perfoms the parsing test.
   * 
   * @param result expected result
   * @param isCaseSensitive <code>true</code> if the parsing must be case
   *          sensitive.
   */
  private void parse(ParsingResult result, boolean isCaseSensitive)
  {
    String ruleType = result.ruleType.trim();
    String matchPattern = result.matchPattern.trim();
    String originalQuery = result.originalQuery.trim();
    String replacePattern = result.replacePattern.trim();

    AbstractRewritingRule rule = null;
    if (ruleType.equalsIgnoreCase("PatternRewritingRule"))
      rule = new PatternRewritingRule(matchPattern, replacePattern, false,
          false);
    else if (ruleType.equalsIgnoreCase("ReplaceAllRewritingRule"))
      rule = new ReplaceAllRewritingRule(matchPattern, replacePattern, false,
          false);
    else if (ruleType.equalsIgnoreCase("SimpleRewritingRule"))
      rule = new SimpleRewritingRule(matchPattern, replacePattern, false, false);
    else
      throw new RuntimeException("Unexecpected Rule Type:" + ruleType);

    String rewritten = rule.rewrite(originalQuery);

    assertEquals("Matching result differs from expected", rule.hasMatched(),
        result.isMatched);

    if (rule.hasMatched())
    {
      System.out.println("RuleType:\t" + ruleType);
      System.out.println("Original:\t" + originalQuery);
      System.out.println("Rewritten:\t" + rewritten);
      System.out.println("Expected:\t" + result.replacedQuery);
      assertEquals("Replaced Query was different than expected", rewritten,
          result.replacedQuery);
    }
    else
    {
      assertEquals("Rule should NOT have matched but reported a success", rule
          .hasMatched(), result.isMatched);
    }
  }

  /**
   * Stores the expected result of the parsing call
   */
  protected class ParsingResult
  {
    String  ruleType;
    String  matchPattern;
    String  replacePattern;
    String  originalQuery;
    String  replacedQuery;
    boolean isMatched;

    /**
     * Create a result where the rule was matching
     * 
     * @param matchPattern string
     * @param replacePattern string
     * @param originalQuery string
     * @param replacedQuery string
     */
    ParsingResult(String ruleType, String matchPattern, String replacePattern,
        String originalQuery, String replacedQuery)
    {
      this.ruleType = ruleType;
      this.matchPattern = matchPattern;
      this.replacePattern = replacePattern;
      this.originalQuery = originalQuery;
      this.replacedQuery = replacedQuery;
      this.isMatched = true;
    }

    /**
     * Create a result where the rule was not matching and the original query
     * was left unchanged
     * 
     * @param matchPattern string
     * @param replacePattern string
     * @param originalQuery string
     */
    ParsingResult(String ruleType, String matchPattern, String replacePattern,
        String originalQuery)
    {
      this.ruleType = ruleType;
      this.matchPattern = matchPattern;
      this.replacePattern = replacePattern;
      this.originalQuery = originalQuery;
      this.replacedQuery = originalQuery;
      this.isMatched = false;
    }
  }
}