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
 * Initial developer(s): Marc Wick.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.common.sql.filters;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;

import org.objectweb.cjdbc.common.util.Strings;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;

/**
 * This class defines a MacrosHandler
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class MacrosHandler implements XmlComponent
{
  /** Used when level is unknown */
  public static final int     UNKNOWN_INT_VALUE       = -1;
  /** Used when level is unknown */
  public static final String  UNKNOWN_STRING_VALUE    = "unknown";

  /** String for rand() macro */
  private static final String MACRO_RAND              = "rand()";

  /** Value if rand() macro should not be replaced */
  public static final int     RAND_OFF                = 0;
  /** Value if rand() macro should be replaced by an integer value */
  public static final int     RAND_INT                = 1;
  /** Value if rand() macro should be replaced by an long value */
  public static final int     RAND_LONG               = 2;
  /** Value if rand() macro should be replaced by an float value (default) */
  public static final int     RAND_FLOAT              = 3;
  /** Value if rand() macro should be replaced by an double value */
  public static final int     RAND_DOUBLE             = 4;

  private final Random        randGenerator           = new Random();

  private int                 replaceRand             = RAND_FLOAT;

  /** String for now() macro */
  private static final String MACRO_NOW               = "now()";
  /** String for current_date macro */
  private static final String MACRO_CURRENT_DATE      = "current_date";
  /** String for current_times macro */
  private static final String MACRO_CURRENT_TIME      = "current_time";
  /** String for timeofday() macro */
  private static final String MACRO_TIMEODFAY         = "timeofday()";
  /** String for current_timestamp macro */
  private static final String MACRO_CURRENT_TIMESTAMP = "current_timestamp";

  /** Value if a date macro should not be replaced */
  public static final int     DATE_OFF                = 0;
  /** Value if date macro should be replaced by an java.sql.Date value */
  public static final int     DATE_DATE               = 1;
  /** Value if date macro should be replaced by an java.sql.Time value */
  public static final int     DATE_TIME               = 2;
  /** Value if date macro should be replaced by an java.sql.Timestamp value */
  public static final int     DATE_TIMESTAMP          = 3;

  private long                clockResolution         = 0;
  private int                 now                     = DATE_TIMESTAMP;
  private int                 currentDate             = DATE_DATE;
  private int                 currentTime             = DATE_TIME;
  private int                 timeOfDay               = DATE_TIMESTAMP;
  private int                 currentTimestamp        = DATE_TIMESTAMP;

  private boolean             needsProcessing;
  private boolean             needsDateProcessing;

  /**
   * Creates a new <code>MacrosHandler</code> object
   * 
   * @param replaceRand replacement of rand() macro
   * @param clockResolution clock resolution for date macros
   * @param now replacement of now()
   * @param currentDate replacement of current_date
   * @param currentTime replacement of current_time
   * @param timeOfDay replacement of timeofday()
   * @param currentTimestamp replacement of current_timestamp
   */
  public MacrosHandler(int replaceRand, long clockResolution, int now,
      int currentDate, int currentTime, int timeOfDay, int currentTimestamp)
  {
    if ((replaceRand < RAND_OFF) || (replaceRand > RAND_DOUBLE))
      throw new RuntimeException("Invalid value for " + MACRO_RAND
          + " macro replacement (" + replaceRand + ")");
    this.replaceRand = replaceRand;
    if (clockResolution < 0)
      throw new RuntimeException(
          "Invalid negative value for clock resolution in date macros");
    this.clockResolution = clockResolution;
    if ((now < DATE_OFF) || (now > DATE_TIMESTAMP))
      throw new RuntimeException("Invalid value for " + MACRO_NOW
          + " macro replacement (" + now + ")");
    this.now = now;
    if ((currentDate < DATE_OFF) || (currentDate > DATE_DATE))
      throw new RuntimeException("Invalid value for " + MACRO_CURRENT_DATE
          + " macro replacement (" + currentDate + ")");
    this.currentDate = currentDate;
    if ((currentTime < DATE_OFF) || (currentTime > DATE_TIMESTAMP))
      throw new RuntimeException("Invalid value for " + MACRO_CURRENT_TIME
          + " macro replacement (" + currentTime + ")");
    this.currentTime = currentTime;
    if ((timeOfDay < DATE_OFF) || (timeOfDay > DATE_TIMESTAMP))
      throw new RuntimeException("Invalid value for " + MACRO_TIMEODFAY
          + " macro replacement (" + timeOfDay + ")");
    this.timeOfDay = timeOfDay;
    if ((currentTimestamp < DATE_OFF) || (currentTimestamp > DATE_TIMESTAMP))
      throw new RuntimeException("Invalid value for " + MACRO_CURRENT_TIMESTAMP
          + " macro replacement (" + currentTimestamp + ")");
    this.currentTimestamp = currentTimestamp;
    needsDateProcessing = (now + currentDate + timeOfDay + currentTimestamp) > 0;
    needsProcessing = needsDateProcessing || (replaceRand > 0);
  }

  /**
   * Convert the rand level from string (xml value) to integer
   * 
   * @param randLevel the rand level
   * @return an int corresponding to the string description
   */
  public static final int getIntRandLevel(String randLevel)
  {
    if (randLevel.equalsIgnoreCase(DatabasesXmlTags.VAL_off))
      return RAND_OFF;
    else if (randLevel.equalsIgnoreCase(DatabasesXmlTags.VAL_double))
      return RAND_DOUBLE;
    else if (randLevel.equalsIgnoreCase(DatabasesXmlTags.VAL_float))
      return RAND_FLOAT;
    else if (randLevel.equalsIgnoreCase(DatabasesXmlTags.VAL_int))
      return RAND_INT;
    else if (randLevel.equalsIgnoreCase(DatabasesXmlTags.VAL_long))
      return RAND_LONG;
    else
      return UNKNOWN_INT_VALUE;
  }

  /**
   * Return this <code>MacrosHandler</code> to the corresponding xml form
   * 
   * @return the XML representation of this element
   */
  public String getXml()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<" + DatabasesXmlTags.ELT_MacroHandling + " "
        + DatabasesXmlTags.ATT_rand + "=\"" + getStringRandLevel(replaceRand)
        + "\" " + DatabasesXmlTags.ATT_now + "=\"" + getStringDateLevel(now)
        + "\" " + DatabasesXmlTags.ATT_currentDate + "=\""
        + getStringDateLevel(currentDate) + "\" "
        + DatabasesXmlTags.ATT_currentTime + "=\""
        + getStringDateLevel(currentTime) + "\" "
        + DatabasesXmlTags.ATT_currentTimestamp + "=\""
        + getStringDateLevel(currentTimestamp) + "\" "
        + DatabasesXmlTags.ATT_timeOfDay + "=\""
        + getStringDateLevel(timeOfDay) + "\" "
        + DatabasesXmlTags.ATT_timeResolution + "=\"" + clockResolution + "\" "
        + "/>");
    return sb.toString();
  }

  /**
   * Convert the rand level from int (java code) to string (xml value)
   * 
   * @param randLevel the rand level
   * @return a string description corresponding to that level
   */
  public static final String getStringRandLevel(int randLevel)
  {
    switch (randLevel)
    {
      case RAND_OFF :
        return DatabasesXmlTags.VAL_off;
      case RAND_DOUBLE :
        return DatabasesXmlTags.VAL_double;
      case RAND_FLOAT :
        return DatabasesXmlTags.VAL_float;
      case RAND_INT :
        return DatabasesXmlTags.VAL_int;
      case RAND_LONG :
        return DatabasesXmlTags.VAL_long;
      default :
        return UNKNOWN_STRING_VALUE;
    }
  }

  /**
   * Convert the date level from string (xml value) to integer
   * 
   * @param dateLevel the date level
   * @return an int corresponding to the string description
   */
  public static final int getIntDateLevel(String dateLevel)
  {
    if (dateLevel.equals(DatabasesXmlTags.VAL_off))
      return DATE_OFF;
    else if (dateLevel.equals(DatabasesXmlTags.VAL_date))
      return DATE_DATE;
    else if (dateLevel.equals(DatabasesXmlTags.VAL_time))
      return DATE_TIME;
    else if (dateLevel.equals(DatabasesXmlTags.VAL_timestamp))
      return DATE_TIMESTAMP;
    else
      return UNKNOWN_INT_VALUE;
  }

  /**
   * Convert the date level from int (java code) to string (xml value)
   * 
   * @param dateLevel the date level
   * @return a string description corresponding to that level
   */
  public static final String getStringDateLevel(int dateLevel)
  {
    switch (dateLevel)
    {
      case DATE_OFF :
        return DatabasesXmlTags.VAL_off;
      case DATE_DATE :
        return DatabasesXmlTags.VAL_date;
      case DATE_TIME :
        return DatabasesXmlTags.VAL_time;
      case DATE_TIMESTAMP :
        return DatabasesXmlTags.VAL_timestamp;
      default :
        return UNKNOWN_STRING_VALUE;
    }
  }

  /**
   * Processes a date related macro using the given timestamp.
   * 
   * @param originalSql original SQL request
   * @param macroPattern macro text to look for
   * @param replacementPolicy DATE_DATE, DATE_TIME or DATE_TIMESTAMP
   * @param currentClock current time in ms
   * @param idxs quote indexes
   * @return new SQL statement
   */
  public String macroDate(String originalSql, String macroPattern,
      int replacementPolicy, long currentClock, Integer[] idxs)
  {
    if (idxs == null)
      idxs = getQuoteIndexes(originalSql);
    String lower = originalSql.toLowerCase();
    int idx = lower.indexOf(macroPattern.toLowerCase());
    if (idx == -1 || !shouldReplaceMacro(idx, idxs))
      return originalSql;

    String date;
    switch (replacementPolicy)
    {
      case DATE_DATE :
        date = "{d '" + new Date(currentClock).toString() + "'}";
        break;
      case DATE_TIME :
        date = "{t '" + new Time(currentClock).toString() + "'}";
        break;
      case DATE_TIMESTAMP :
        date = "{ts '" + new Timestamp(currentClock).toString() + "'}";
        break;
      default :
        throw new RuntimeException(
            "Unexpected replacement strategy for date macro ("
                + replacementPolicy + ")");
    }
    return Strings.replaceCasePreserving(originalSql, macroPattern, date);
  }

  /**
   * Replaces rand() with a randomized value.
   * 
   * @param originalSql original SQL request
   * @param idxs quote indexes
   * @return new SQL statement
   */
  public String macroRand(String originalSql, Integer[] idxs)
  {
    if (idxs == null)
      idxs = getQuoteIndexes(originalSql);
    String lower = originalSql.toLowerCase();
    int idx = lower.indexOf(MACRO_RAND);
    if (idx > 0)
    {
      String rand;
      StringBuffer sql = new StringBuffer(originalSql);
      int shift = 0;
      do
      {
        if (shouldReplaceMacro(idx, idxs))
        {
          switch (replaceRand)
          {
            case RAND_INT :
              rand = Integer.toString(randGenerator.nextInt());
              break;
            case RAND_LONG :
              rand = Long.toString(randGenerator.nextLong());
              break;
            case RAND_FLOAT :
              rand = Float.toString(randGenerator.nextFloat());
              break;
            case RAND_DOUBLE :
              rand = Double.toString(randGenerator.nextDouble());
              break;
            default :
              throw new RuntimeException(
                  "Unexpected replacement strategy for rand() macro ("
                      + replaceRand + ")");
          }
          sql = sql.replace(idx + shift, idx + shift + MACRO_RAND.length(),
              rand);
          shift += rand.length() - MACRO_RAND.length();
        }
        idx = lower.indexOf(MACRO_RAND, idx + MACRO_RAND.length());
      }
      while (idx > 0);
      return sql.toString();
    }
    else
    {
      return originalSql;
    }
  }

  /**
   * Processes all macros in the given request and returns a new String with the
   * processed macros. If no macro has to be processed, the original String is
   * returned.
   * 
   * @param sql SQL statement to process
   * @return processed statement
   */
  public final String processMacros(String sql)
  {
    if (!needsProcessing)
      return sql;
    if(sql==null)
      return null;
    Integer[] idxs = this.getQuoteIndexes(sql);
    if (replaceRand > RAND_OFF)
      sql = macroRand(sql, idxs);
    if (!needsDateProcessing)
      return sql;
    long currentClock = System.currentTimeMillis();
    if (clockResolution > 0)
      currentClock = currentClock - (currentClock % clockResolution);
    if (now > DATE_OFF)
      sql = macroDate(sql, MACRO_NOW, now, currentClock, idxs);
    if (currentDate > DATE_OFF)
      sql = macroDate(sql, MACRO_CURRENT_DATE, currentDate, currentClock, idxs);
    if (currentTimestamp > DATE_OFF)
      sql = macroDate(sql, MACRO_CURRENT_TIMESTAMP, currentTimestamp,
          currentClock, idxs);
    if (currentTime > DATE_OFF)
      sql = macroDate(sql, MACRO_CURRENT_TIME, currentTime, currentClock, idxs);
    if (timeOfDay > DATE_OFF)
      sql = macroDate(sql, MACRO_TIMEODFAY, timeOfDay, currentClock, idxs);

    return sql;
  }

  /**
   * Retrieve all the indexes of quotes in the string
   * 
   * @param sql the original query
   * @return an array of integer corresponding to the quote indexes
   */
  private Integer[] getQuoteIndexes(String sql)
  {
    ArrayList list = new ArrayList();
    for (int i = 0; i < sql.length(); i++)
    {
      char c = sql.charAt(i);
      if (c == '\'')
        list.add(new Integer(i));
    }
    Integer[] intlist = new Integer[list.size()];
    return (Integer[]) list.toArray(intlist);
  }

  /**
   * Should we replace a macro situated at index idx, knowing that the quotes
   * are at indexes list
   * 
   * @param idx the index of the macro
   * @param list the indexes of quotes
   * @return <code>true</code> if we should change the macro,
   *         <code>false</code> if the macro is within a string
   */
  private boolean shouldReplaceMacro(int idx, Integer[] list)
  {
    int count = 0;
    while (count < list.length && list[count].intValue() < idx)
    {
      count++;
    }
    return count % 2 == 0;
  }

}