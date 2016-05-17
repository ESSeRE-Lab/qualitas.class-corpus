/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002, 2003 French National Institute For Research In Computer
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
 * Contributor(s): _________________________.
 */

package org.objectweb.cjdbc.scenario.tools.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.objectweb.cjdbc.common.i18n.Translate;

/**
 * This looks at the language file and the java code to gather information on
 * what translation tags are correct, missing ...
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class TranslationCheck
{
  static final String TRANSLATE_TAG        = "Translate.get(";
  static final String LOGGER_TAG           = "logger.";
  static final String LOGGER_TEST          = "logger.is";
  static final String EXCEPTION_TAG        = "Exception(";
  static final int    TRANSLATE_TAG_LENGTH = TRANSLATE_TAG.length();
  private String      translationFile      = Translate.CJDBC_LANGUAGE_FILE;
  private ArrayList   ignore;
  private final String userDir = System.getProperty("cjdbc.dir");

  int                 analysedXslFile      = 0;
  int                 analyseJavaFile      = 0;

  private ArrayList   xslKeys;
  private ArrayList   missingXslKeys;
  private ArrayList   usedXslKeys;
  private ArrayList   configurationKeys;
  private ArrayList   javaKeys;
  private ArrayList   invalidKeys;
  // when the key is present but no text is associated to it
  private ArrayList   missingTranslations;
  private ArrayList   missingLogger;
  private ArrayList   javaFiles;

  /**
   * This is a small class for storing invalid tag found in java files
   */
  class InvalidTag
  {
    String file;
    String tmp;
    String description;
    int    line;

    InvalidTag(String description, String file, String tmp, int line)
    {
      this.description = description;
      this.file = file;
      this.tmp = tmp;
      this.line = line;
    }
  }

  /**
   * Creates a new translation checker object. This starts analysing the java
   * code when instanciated
   */
  public TranslationCheck()
  {

    ignore = new ArrayList();
    configurationKeys = new ArrayList();
    javaKeys = new ArrayList();
    invalidKeys = new ArrayList();
    missingTranslations = new ArrayList();
    missingLogger = new ArrayList();
    xslKeys = new ArrayList();
    missingXslKeys = new ArrayList();
    usedXslKeys = new ArrayList();
    javaFiles = new ArrayList();

    ignore.add("Translate.java");
    getMissingTranslations();
    analyseXsl();
    analyseJava();
  }

  /**
   * Computes the missing translations in the configuration file
   * 
   * @return an <code>ArrayList</code> of tags where the associated
   *         translation is an empty string
   */
  private void getMissingTranslations()
  {
    ResourceBundle rb = ResourceBundle.getBundle(translationFile);
    Enumeration e = rb.getKeys();
    while (e.hasMoreElements())
    {
      String translation = (String) e.nextElement();
      if (translation.trim().startsWith("##"))
        continue;
      configurationKeys.add(translation);
      if (rb.getString(translation).equals(""))
        missingTranslations.add(translation);
    }
  }

  private void analyseXsl()
  {
    ResourceBundle keys = ResourceBundle.getBundle("c-jdbc-xsl");
    for (Enumeration e = keys.getKeys(); e.hasMoreElements(); xslKeys.add(e
        .nextElement()))
    {
      // the loop does everything
    }
    File f = new File(userDir + "/xml");
    File[] xsls = f.listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name)
      {
        if (name.endsWith(".xsl"))
          return true;
        else
          return false;
      }
    });
    for (int i = 0; i < xsls.length; i++)
      analyseXslFile(xsls[i]);
  }

  private void analyseXslFile(File f)
  {
    //System.out.println("Analyse XSL file:" + f.getName());
    analysedXslFile++;
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(f));
      String read = "";
      String oi18n = "<i18n>";
      String ci18n = "</i18n>";
      int oi18nl = oi18n.length();
      int ci18nl = oi18nl + 1;
      String i18n = "";
      while ((read = br.readLine()) != null)
      {
        int indexOpen = 0, indexClose = 0;
        while ((indexOpen = read.indexOf(oi18n)) != -1)
        {
          indexClose = read.indexOf(ci18n);
          i18n = read.substring(indexOpen + oi18nl, indexClose).trim();
          if (xslKeys.contains(i18n) == false)
          {
            missingXslKeys.add(i18n);
          }
          else
            usedXslKeys.add(i18n);
          read = read.substring(0, indexOpen) + i18n
              + read.substring(indexClose + ci18nl);
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }

  /**
   * Starts analysing the java code
   */
  private void analyseJava()
  {
    File f = new File(userDir + "/src/org");
    // use f as root directory
    searchForKeys(f);
  }

  /**
   * Recursivly searches the file target for java files
   * 
   * @param target file to search into
   */
  private void searchForKeys(File target)
  {
    if (target.isDirectory())
    {
      File[] list = target.listFiles();
      for (int i = 0; i < list.length; i++)
        searchForKeys(list[i]);
    }
    else
    {
      String path = target.getAbsolutePath();
      if (path.indexOf("CVS") == -1
          && (path.indexOf("controller") != -1 || path.indexOf("console") != -1))
      {
        if (target.getName().endsWith(".java"))
          searchJavaFile(target);
      }
    }
  }

  /**
   * Should indicate whether the tag is valid or not
   * 
   * @param tmp taken from java file after translate tag has been found
   * @return true if tmp contains no '+' sign, false otherwise We could use
   *         regexp in the near future for better guessing
   */
  private boolean isValidTranslateTag(String tmp)
  {
    return (tmp.indexOf('+') == -1);
  }

  /**
   * We found a valid tag so let's take the key from it
   * 
   * @param tmp string that could be a key
   * @return true if tmp was a key and was added to the javaKeys list, false if
   *         we should further process it.
   */
  private boolean processValidTag(String tmp)
  {
    if ((tmp.charAt(0) == '\"') && (tmp.charAt(tmp.length() - 1) == '\"'))
    {
      javaKeys.add(tmp.substring(1, tmp.length() - 1));
      return true;
    }
    else
      return false;
  }

  /**
   * We found an invalid tag, so let's store it.
   * 
   * @param target file where it was found
   * @param tmp the tag content
   * @param line where the invalid tag was found
   */
  private void processUnValidTag(String description, File target, String tmp,
      int line)
  {
    invalidKeys.add(new InvalidTag(description, target.getName(), tmp, line));
  }

  /**
   * We have found a java file. We first check it is not in the ignore list and
   * then we look for the Translate.get string
   * 
   * @param target file to process
   */
  private void searchJavaFile(File target)
  {
    if (ignore.contains(target.getName()))
      return;
    analyseJavaFile++;
    javaFiles.add(target.getName());

    String twoLinesBefore = "";
    String previousLine = "";
    String read = "";
    String tmp = "";
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(target));
      int index = -1;
      int line = 0;
      while ((read = br.readLine()) != null)
      {
        line++;
        // look for exception tag
        if (read.indexOf(EXCEPTION_TAG) != -1)
        {
          while (read.indexOf(';') == -1)
          {
            read = read.trim() + br.readLine().trim();
            line++;
          }
          if (read.indexOf(TRANSLATE_TAG) == -1)
          {
            if (read.indexOf(EXCEPTION_TAG + "\"") != -1)
            {
              if (previousLine.indexOf(TRANSLATE_TAG) == -1
                  && twoLinesBefore.indexOf(TRANSLATE_TAG) == -1)
                missingLogger.add(new InvalidTag("EXCEPTION", target.getName(),
                    System.getProperty("line.separator")
                        + twoLinesBefore.trim()
                        + System.getProperty("line.separator")
                        + previousLine.trim()
                        + System.getProperty("line.separator") + read.trim(),
                    line));
            }
            else
            {
              // Exception method so don't count it
            }
          }
        }

        // look for logger tag
        if (read.indexOf(LOGGER_TAG) != -1)
        {
          while (read.indexOf(';') == -1)
          {
            read = read.trim() + br.readLine().trim();
            line++;
          }
          if (read.indexOf(TRANSLATE_TAG) == -1
              && read.indexOf(LOGGER_TEST) == -1)
          {
            // Can be like:
            // String msg = Translate.get(...
            // so check the lines before
            if (previousLine.indexOf(TRANSLATE_TAG) == -1
                && twoLinesBefore.indexOf(TRANSLATE_TAG) == -1)
              missingLogger.add(new InvalidTag("LOGGER", target.getName(),
                  System.getProperty("line.separator") + twoLinesBefore.trim()
                      + System.getProperty("line.separator")
                      + previousLine.trim()
                      + System.getProperty("line.separator") + read.trim(),
                  line));
          }
        }

        // Look for translation tag
        if ((index = read.indexOf(TRANSLATE_TAG)) != -1)
        {
          while (read.indexOf(';') == -1)
          {
            read = read.trim() + br.readLine().trim();
            line++;
          }
          read = read.trim();
          index = read.indexOf(TRANSLATE_TAG);
          tmp = read.substring(index + TRANSLATE_TAG_LENGTH);
          tmp = tmp.trim();

          if (tmp.indexOf(",") != -1)
            tmp = tmp.substring(0, tmp.indexOf(","));
          if (tmp.lastIndexOf(')') > tmp.length() - 4)
          {
            try
            {
              tmp = tmp.substring(0, tmp.indexOf(')', tmp.length() - 4));
            }
            catch (Exception e)
            {
              System.out.println("-->Substring failed in :" + target.getName());
            }
          }

          boolean valid = isValidTranslateTag(tmp);

          if (valid && processValidTag(tmp))
          {
            // There is nothing to do. proceeValidTag add the tag if possible.
            // if failed, if to else section
          }
          else
            processUnValidTag("TRANSLATE", target, tmp, line);
        }
        twoLinesBefore = previousLine;
        previousLine = read;
      }
    }
    catch (Exception ignore)
    {
      ignore.printStackTrace();
    }
  }

  /**
   * @return Returns the configurationKeys.
   */
  public ArrayList getConfigurationKeys()
  {
    return configurationKeys;
  }

  /**
   * @return Returns the javaKeys.
   */
  public ArrayList getJavaKeys()
  {
    return javaKeys;
  }

  /**
   * @return Returns the invalidKeys.
   */
  public ArrayList getinvalidKeys()
  {
    return invalidKeys;
  }

  /**
   * We need to know if all the valid keys in the java code are in the
   * configuration file
   * 
   * @return true if all are there... will probably be false at this stage
   *         though !
   */
  public boolean isTranslationUpToDate()
  {
    return configurationKeys.containsAll(javaKeys);
  }

  /**
   * Display the state of translation work
   */
  public void displayTranslationState()
  {
    System.out.println("Translation up to date:" + isTranslationUpToDate());
    System.out.println("### UNUSED XSL KEYS FROM PROPERTIES ###");
    ArrayList xslNotUsed = (ArrayList) xslKeys.clone();
    xslNotUsed.removeAll(usedXslKeys);
    for (int i = 0; i < xslNotUsed.size(); i++)
      System.out.println(xslNotUsed.get(i));
    System.out.println("### MISSING XSL KEYS (" + missingXslKeys.size()
        + ") ###");
    for (int i = 0; i < missingXslKeys.size(); i++)
      System.out.println(missingXslKeys.get(i));
    System.out.println("### MISSING TRANSLATIONS ("
        + missingTranslations.size() + ") ###");
    for (int i = 0; i < missingTranslations.size(); i++)
      System.out.println(missingTranslations.get(i));
    System.out
        .println("### ANALYSED JAVA FILES (" + javaFiles.size() + ") ###");
    //for (int i = 0; i < javaFiles.size(); i++)
    //  System.out.println(javaFiles.get(i));
    System.out.println("### INVALID TAGS IN JAVA (" + invalidKeys.size()
        + ") ###");
    for (int i = 0; i < invalidKeys.size(); i++)
    {
      InvalidTag tag = (InvalidTag) (invalidKeys.get(i));
      System.out.println("[" + tag.file + ":line:" + tag.line + "]:" + tag.tmp);
    }
    System.out.println("### MISSING TAGS IN CONFIGURATION ###");
    int count = 0;
    for (int i = 0; i < javaKeys.size(); i++)
    {
      String key = (String) javaKeys.get(i);
      if (!configurationKeys.contains(key))
      {
        System.out.println(key);
        count++;
      }
    }
    System.out.println("\t ### TOTAL MISSING TAGS " + count);
    System.out.println("### MISSING TAGS IN JAVA ###");
    int count2 = 0;
    for (int i = 0; i < configurationKeys.size(); i++)
    {
      String key = (String) configurationKeys.get(i);
      if (!javaKeys.contains(key))
      {
        count2++;
        System.out.println(key);
      }
    }
    System.out.println("\t ### TOTAL MISSING TAGS " + count2);
    System.out.println("### MISSING LOGGERS IN JAVA (" + missingLogger.size()
        + ") ###");
    for (int i = 0; i < missingLogger.size(); i++)
    {
      InvalidTag tag = (InvalidTag) (missingLogger.get(i));
      System.out.println(" %%% [" + tag.description + "][" + tag.file
          + ":line:" + tag.line + "] %%%" + tag.tmp);
    }
    System.out.println("*******************************************");
    System.out.println("************** SUMMARY ********************");
    System.out.println("### NUMBER OF ANALYSED XSL FILES:" + analysedXslFile);
    System.out.println("### NUMBER OF ANALYSED JAVA FILES:" + analyseJavaFile);
    System.out.println("### UNUSED XSL KEYS FROM PROPERTIES ("
        + xslNotUsed.size() + ") ###");
    System.out.println("### MISSING XSL KEYS (" + missingXslKeys.size()
        + ") ###");
    System.out.println("### MISSING TRANSLATION STRINGS IN PROPERTY FILE ("
        + missingTranslations.size() + ") ###");
    System.out.println("### INVALID TAGS IN JAVA (" + invalidKeys.size()
        + ") ###");
    System.out.println("### MISSING CONFIGURATION TAGS " + count);
    System.out.println("### MISSING JAVA TAGS " + count2);
    System.out.println("### MISSING LOGGERS IN JAVA (" + missingLogger.size()
        + ") ###");
    System.out.println("*******************************************");
  }

}