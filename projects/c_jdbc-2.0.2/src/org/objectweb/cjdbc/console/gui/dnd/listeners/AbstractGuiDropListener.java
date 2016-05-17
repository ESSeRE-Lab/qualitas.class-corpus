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

import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;

/**
 * This class defines a AbstractGuiDropListener. This is mainly to hide all the
 * method we don't need to implement for all of the drop listeners.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class AbstractGuiDropListener implements DropTargetListener, // For
    // processing
    // drop
    // target
    // events
    DragSourceListener, // For processing drag source events
    MouseMotionListener, // For processing mouse drags
    MouseListener, // For processing mouse clicks
    DragGestureListener // For recognizing the start of drags
{

  CjdbcGui              gui;
  DragGestureRecognizer dgr;

  /**
   * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
   */
  public void dragGestureRecognized(DragGestureEvent e)
  {
    //System.out.println("Gesture recognized");
    if(dgr!=null)
    {
      dgr.resetRecognizer();
    }
    dgr = e.getSourceAsDragGestureRecognizer();
    Transferable transfer = (Transferable) e.getComponent();
    
    try
    {
      e.getDragSource().startDrag(e, GuiConstants.customCursor, transfer, this);
    }
    catch (Exception error)
    {
      //System.out.println("Got error while dragging resetting listener...");
      dgr.resetRecognizer();
      e.getDragSource().startDrag(e, GuiConstants.customCursor, transfer, this);
    }
  }

  /**
   * Creates a new <code>AbstractGuiDropListener.java</code> object
   * 
   * @param gui the main interface
   */
  public AbstractGuiDropListener(CjdbcGui gui)
  {
    this.gui = gui;
  }

  /**
   * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
   */
  public void dragEnter(DropTargetDragEvent dtde)
  {
    //System.out.println("Drag enter target");
  }

  /**
   * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
   */
  public void dragExit(DropTargetEvent dte)
  {
    //System.out.println("Drag exit target");
  }

  /**
   * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
   */
  public void dragOver(DropTargetDragEvent dtde)
  {
    //System.out.println("Drag over target");
  }

  /**
   * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
   */
  public void dropActionChanged(DropTargetDragEvent dtde)
  {
    //System.out.println("Drop action changed target");
  }

  /**
   * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
   */
  public void dragDropEnd(DragSourceDropEvent dsde)
  {
    //System.out.println("Drag drop end source");
    dgr.resetRecognizer();
    dgr = null;
    
    DragSourceContext dsc = dsde.getDragSourceContext();
    dsc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    gui.repaint();
    //gui.publicActionRefreshCursorShape();
  }

  /**
   * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
   */
  public void dragEnter(DragSourceDragEvent dsde)
  {
    //System.out.println("Drag enter source");
  }

  /**
   * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
   */
  public void dragExit(DragSourceEvent dse)
  {
    //System.out.println("Drag exit source");
  }

  /**
   * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
   */
  public void dragOver(DragSourceDragEvent dsde)
  {
    //System.out.println("Drag over source");
  }

  /**
   * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
   */
  public void dropActionChanged(DragSourceDragEvent dsde)
  {
    //System.out.println("Drop action changed source");
  }

  /**
   * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  public void mouseDragged(MouseEvent e)
  {

  }

  /**
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  public void mouseMoved(MouseEvent e)
  {

  }

  /**
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e)
  {

  }

  /**
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e)
  {

  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e)
  {

  }

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e)
  {
    
  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e)
  {

  }
}
