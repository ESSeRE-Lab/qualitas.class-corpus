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

package org.objectweb.cjdbc.console.gui.objects;

import java.awt.Color;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.SwingConstants;

import org.objectweb.cjdbc.common.util.Zipper;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.constants.GuiIcons;

/**
 * This class defines a DumpFileObject to represent graphically dump files
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class DumpFileObject extends AbstractGuiObject
{
  private File       dumpFile;
  private String     dumpFileName;
  private String     displayName;
  private DateFormat df;

  /**
   * Creates a new <code>DumpFileObject.java</code> object
   * 
   * @param dumpFile name of the dump file
   */
  public DumpFileObject(File dumpFile)
  {
    this.dumpFile = dumpFile;
    this.dumpFileName = dumpFile.getName();
    this.df = new SimpleDateFormat();
    this.setActionCommand(GuiConstants.BACKEND_STATE_RESTORE);
    setIcon(GuiIcons.DUMP_FILE_ICON);
    setBackground(Color.white);
    setDisplayName();
    setVerticalTextPosition(SwingConstants.BOTTOM);
    setFont(GuiConstants.CENTER_PANE_FONT);

    setToolTipText("File:" + dumpFileName + "\nLast Modified:"
        + df.format(new Date(dumpFile.lastModified())));
  }

  void setDisplayName()
  {
    displayName = dumpFileName.substring(0, dumpFileName.length()
        - Zipper.ZIP_EXT.length());
    int ind = displayName.indexOf('-');
    if (ind != -1)
    {
      setBorder(BorderFactory.createTitledBorder(GuiConstants.LINE_BORDER,
          displayName.substring(0, ind)));
      displayName = displayName.substring(ind + 1);
    }
    int index = displayName.indexOf(' ');
    if (index == -1)
      setText(displayName);
    else
      setText(displayName.substring(0, index));
  }

  /**
   * Returns the dumpFile value.
   * 
   * @return Returns the dumpFile.
   */
  public File getDumpFile()
  {
    return dumpFile;
  }

  /**
   * Return the dump file name.
   * 
   * @return the dump name
   */
  public String getDumpName()
  {
    String name = dumpFile.getName();
    return name.substring(0, name.indexOf(Zipper.ZIP_EXT));
  }
}