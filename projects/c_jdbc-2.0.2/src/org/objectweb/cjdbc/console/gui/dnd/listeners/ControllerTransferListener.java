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

package org.objectweb.cjdbc.console.gui.dnd.listeners;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.JButton;

import org.objectweb.cjdbc.console.gui.CjdbcGui;

/**
 * This class defines a ControllerTransferListener. 
 * Listens for DnD on configuration files and on transfered backends from the backend panel
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class ControllerTransferListener extends AbstractGuiDropListener
{
  
  /**
   * 
   * Creates a new <code>ControllerTransferListener</code> object
   * 
   * @param gui link to the main gui
   */
  public ControllerTransferListener(CjdbcGui gui)
  {
    super(gui);
  }
  
  
  /**
   * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
   */
  public void drop(DropTargetDropEvent dtde)
  {
    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
    DropTarget target = ((DropTarget) dtde.getSource());
    Transferable transfer = dtde.getTransferable();
    
    JButton bo = (JButton) target.getComponent();
    try
    {
      // This is the flavor for backends 
      Object data = transfer.getTransferData(DataFlavor.plainTextFlavor);
      if(data==null)
      {
        // We have to transfer a configuration file
        data = transfer.getTransferData(DataFlavor.stringFlavor);
        gui.publicActionExecuteControllerDrop(data.toString(), bo.getText());
      }
      else
      {
        // we transfer a backend from one controller to another one
        gui.publicActionExecuteTransfer(data.toString(),bo.getText());
      }
      dtde.dropComplete(true);
    }
    catch (Exception e)
    {
      dtde.dropComplete(true);
      gui.appendDebugText("Failed to execute Drag and drop for targer:"
          + bo.getText());
    }
    finally
    {
      dtde.dropComplete(true);
      gui.publicActionRefreshCursorShape();
    }
  }
}
