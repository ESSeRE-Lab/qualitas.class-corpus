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
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.gui.objects.tooltips;

import org.objectweb.cjdbc.common.i18n.Translate;

/**
 * This class defines a BackendToolTip
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BackendToolTip
{
  private String[] data;

  /**
   * Creates a new <code>BackendToolTip.java</code> object
   * 
   * @param data from the virtual database
   */
  public BackendToolTip(String[] data)
  {
    this.data = data;
  }

  /**
   * Return a formatted tool tip for the backend
   * 
   * @return <code>String</code> for the tooltip content
   */
  public String getFormattedToolTip()
  {
    StringBuffer buffer = new StringBuffer();
    for (int i = 1; i < 14; i++)
    {
      buffer.append(Translate.get("console.infoviewer.backend.column." + i)
          + ":");
      buffer.append("\t\t" + data[i] + "\n");
    }
    return buffer.toString();
  }
}
