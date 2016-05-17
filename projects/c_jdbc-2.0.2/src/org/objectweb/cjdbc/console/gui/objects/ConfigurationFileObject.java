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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.io.File;
import java.io.IOException;

import org.objectweb.cjdbc.console.gui.constants.GuiCommands;
import org.objectweb.cjdbc.console.gui.constants.GuiIcons;
import org.objectweb.cjdbc.console.gui.dnd.listeners.ControllerTransferListener;

/**
 * This class defines a ConfigurationFileObject
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class ConfigurationFileObject extends AbstractGuiObject
    implements
      Transferable

{
  private File       filePath;
  private String     text;

  /**
   * Creates a new <code>ConfigurationFileObject</code> object
   * 
   * @param filePath the path of the file represented by this configuration
   *            object
   * @param listener for drag and drop
   */
  public ConfigurationFileObject(ControllerTransferListener listener,String filePath)
  {
    this(listener,new File(filePath));
  }

  /**
   * Creates a new <code>ConfigurationFileObject</code> object
   * 
   * @param filePath file represented by this configuration object
   * @param listener for drag and drop
   */
  public ConfigurationFileObject(ControllerTransferListener listener,File filePath)
  {
    super();
    //addMouseMotionListener(listener);
    //addMouseListener(listener);
    this.filePath = filePath;
    text = filePath.getName();
    setText(text);
    setBackground(Color.white);
    setIcon(GuiIcons.CONFIGURATION_FILE_OBJECT_ICON);
    setActionCommand(GuiCommands.COMMAND_SELECT_XML_FILE);
    
    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(this, // What component
        DnDConstants.ACTION_COPY_OR_MOVE, // What drag types?
        listener);// the listener
  }

  /**
   * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
   */
  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[]{};
  }

  /**
   * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
   */
  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    return true;
  }

  /**
   * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
   */
  public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException
  {
    if(flavor.equals(DataFlavor.stringFlavor))
      return filePath.getAbsolutePath();
    else 
      return null;
  }

  /**
   * Returns the filePath value.
   * 
   * @return Returns the filePath.
   */
  public String getFilePath()
  {
    return filePath.getAbsolutePath();
  }
}
