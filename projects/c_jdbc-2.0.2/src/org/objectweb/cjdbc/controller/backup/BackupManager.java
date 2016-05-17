/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2005 Emic Networks.
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

package org.objectweb.cjdbc.controller.backup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.objectweb.cjdbc.common.exceptions.BackupException;
import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;

/**
 * This class defines a BackupManager that is responsible for registering
 * backupers and retrieving them as needed for backup/restore operations.
 * 
 * @author <a href="mailto:emmanuel.cecchet@emicnetworks.com">Emmanuel Cecchet</a>
 * @version 1.0
 */
public class BackupManager implements XmlComponent
{
  static Trace    logger = Trace.getLogger(BackupManager.class.getName());

  /**
   * This is a HashMap of backuperName -> Backuper HashMap<String,Backuper>
   */
  private HashMap backupers;

  /**
   * Creates a new <code>BackupManager</code> object
   */
  public BackupManager()
  {
    backupers = new HashMap();
  }

  /**
   * Retrieve a backuper given its name. If the backuper has not been registered
   * null is returned.
   * 
   * @param name the backuper to look for
   * @return the backuper or null if not found
   */
  public synchronized Backuper getBackuperByName(String name)
  {
    return (Backuper) backupers.get(name);
  }

  /**
   * Get the names of the <code>Backupers</code> available from this
   * <code>BackupManager</code>.
   * 
   * @return an (possibly 0-sized) array of <code>String</code> representing
   *         the name of the <code>Backupers</code>
   */
  public synchronized String[] getBackuperNames()
  {
    Set backuperNames = backupers.keySet();
    return (String[]) backuperNames.toArray(new String[backuperNames.size()]);
  }

  /**
   * Get the first backuper that supports the given dump format. If no backuper
   * supporting that format can be found, null is returned.
   * 
   * @param format the dump format that the backuper must handle
   * @return a backuper or null if not found
   */
  public synchronized Backuper getBackuperByFormat(String format)
  {
    if (format == null)
      return null;
    for (Iterator iter = backupers.values().iterator(); iter.hasNext();)
    {
      Backuper b = (Backuper) iter.next();
      if (format.equals(b.getDumpFormat()))
        return b;
    }
    return null;
  }

  /**
   * Register a new backuper under a logical name.
   * 
   * @param name backuper logical name
   * @param backuper the backuper instance
   * @throws BackupException if a backuper is null or a backuper has already
   *           been registered with the given name.
   */
  public synchronized void registerBackuper(String name, Backuper backuper)
      throws BackupException
  {
    if (backupers.containsKey(name))
      throw new BackupException(
          "A backuper has already been registered with name " + name);
    if (backuper == null)
      throw new BackupException(
          "Trying to register a null backuper under name " + name);

    if (logger.isInfoEnabled())
      logger.info("Registering backuper " + name + " to handle format "
          + backuper.getDumpFormat());

    backupers.put(name, backuper);
  }

  /**
   * Unregister a Backuper given its logical name.
   * 
   * @param name the name of the backuper to unregister
   * @return true if the backuper was removed successfully, false if it was not
   *         registered
   */
  public synchronized boolean unregisterBackuper(String name)
  {
    Object backuper = backupers.remove(name);

    if (logger.isInfoEnabled() && (backuper != null))
      logger.info("Unregistering backuper " + name + " that handled format "
          + ((Backuper) backuper).getDumpFormat());

    return backuper != null;
  }

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public synchronized String getXml()
  {
    StringBuffer sb = new StringBuffer("<" + DatabasesXmlTags.ELT_Backup + "> ");
    for (Iterator iter = backupers.keySet().iterator(); iter.hasNext();)
    {
      String backuperName = (String) iter.next();
      Backuper b = (Backuper) backupers.get(backuperName);
      sb.append("<" + DatabasesXmlTags.ELT_Backuper + " "
          + DatabasesXmlTags.ATT_backuperName + "=\"" + backuperName + "\" "
          + DatabasesXmlTags.ATT_className + "=\"" + b.getClass() + "\" "
          + DatabasesXmlTags.ATT_options + "=\"" + b.getOptions() + "\"  />");
    }
    sb.append("</" + DatabasesXmlTags.ELT_Backup + ">");
    return sb.toString();
  }
}