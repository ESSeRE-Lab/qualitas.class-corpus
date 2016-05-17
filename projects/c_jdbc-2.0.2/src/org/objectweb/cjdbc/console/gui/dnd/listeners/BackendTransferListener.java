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

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.objectweb.cjdbc.console.gui.CjdbcGui;

/**
 * This class defines a BackendTransferListener. Listens for DnD on backends
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BackendTransferListener extends AbstractGuiDropListener
{

  /**
   * Creates a new <code>BackendTransferListener.java</code> object
   * 
   * @param gui link to the main gui
   */
  public BackendTransferListener(CjdbcGui gui)
  {
    super(gui);
  }

  /**
   * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
   */
  public void drop(DropTargetDropEvent dtde)
  {
    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
    DropTarget target = dtde.getDropTargetContext().getDropTarget();
    Transferable transfer = dtde.getTransferable();
    
    
    Component comp = target.getComponent();
    try
    {
      Object data = transfer.getTransferData(DataFlavor.stringFlavor);
      if (comp instanceof JButton)
        gui.publicActionExecuteBackendDrop((JButton) comp, data.toString());
      else if (comp instanceof JPanel)
        gui.publicActionExecuteBackendDrop((JPanel) comp, data.toString());

    }
    catch (Exception e)
    {
      gui.appendDebugText("Failed to execute Drag and drop for target:"
          + comp.getName(),e);
    }
    finally
    {
      dtde.getDropTargetContext().removeNotify();
      dtde.dropComplete(true);
      gui.publicActionRefreshCursorShape();
    }
    dtde.getDropTargetContext().removeNotify();
    
    dtde.dropComplete(true);
    
  }
}
