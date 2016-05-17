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

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;

/**
 * This class defines a AbstractRewritingRule to rewrite SQL requests for a
 * specific backend.
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet</a>
 * @version 1.0
 */
public abstract class AbstractRewritingRule
{
  protected String  queryPattern;
  protected String  rewrite;
  protected boolean isCaseSensitive;
  protected boolean stopOnMatch;
  protected boolean hasMatched;

  /**
   * Creates a new <code>AbstractRewritingRule</code> object
   * 
   * @param queryPattern SQL pattern to match
   * @param rewrite rewritten SQL query
   * @param caseSensitive true if matching is case sensitive
   * @param stopOnMatch true if rewriting must stop after this rule if it
   *          matches.
   */
  public AbstractRewritingRule(String queryPattern, String rewrite,
      boolean caseSensitive, boolean stopOnMatch)
  {
    this.queryPattern = queryPattern;
    this.rewrite = rewrite;
    this.isCaseSensitive = caseSensitive;
    this.stopOnMatch = stopOnMatch;
    this.hasMatched = false;
  }

  /**
   * Returns true if the query given in the last call to rewrite has matched
   * this rule.
   * <p>1. call rewrite(query)
   * <p>2. call hasMatched() to know if query matched this rule.
   * 
   * @return true if the query matched this rule.
   * @see #rewrite(String)
   */
  public boolean hasMatched()
  {
    return hasMatched;
  }

  /**
   * Rewrite the given query according to the rule. Note that this method does
   * not check if the given query matches the rule or not. You must call
   * matches(String) before calling this method.
   * 
   * @param sqlQuery request to rewrite
   * @return the rewritten SQL query according to the rule.
   * @see AbstractRewritingRule#hasMatched
   */
  public abstract String rewrite(String sqlQuery);

  /**
   * Returns the isCaseSensitive value.
   * 
   * @return Returns the isCaseSensitive.
   */
  public boolean isCaseSensitive()
  {
    return isCaseSensitive;
  }

  /**
   * Returns the queryPattern value.
   * 
   * @return Returns the queryPattern.
   */
  public String getQueryPattern()
  {
    return queryPattern;
  }

  /**
   * Returns the rewrite value.
   * 
   * @return Returns the rewrite.
   */
  public String getRewrite()
  {
    return rewrite;
  }

  /**
   * Returns the stopOnMatch value.
   * 
   * @return Returns the stopOnMatch.
   */
  public boolean isStopOnMatch()
  {
    return stopOnMatch;
  }

  /**
   * Get xml information about this AbstractRewritingRule.
   * 
   * @return xml formatted information on this AbstractRewritingRule.
   */
  public String getXml()
  {
    return "<" + DatabasesXmlTags.ELT_RewritingRule + " "
        + DatabasesXmlTags.ATT_queryPattern + "=\"" + queryPattern + "\" "
        + DatabasesXmlTags.ATT_rewrite + "=\"" + rewrite + "\" "
        + DatabasesXmlTags.ATT_caseSensitive + "=\"" + isCaseSensitive + "\" "
        + DatabasesXmlTags.ATT_stopOnMatch + "=\"" + stopOnMatch + "\"/>";
  }

}
