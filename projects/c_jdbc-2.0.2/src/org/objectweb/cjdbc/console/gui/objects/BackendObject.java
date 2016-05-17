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
import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.swing.SwingConstants;

import org.objectweb.cjdbc.common.jmx.mbeans.DatabaseBackendMBean;
import org.objectweb.cjdbc.console.gui.CjdbcGui;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.constants.GuiIcons;
import org.objectweb.cjdbc.console.gui.dnd.listeners.BackendTransferListener;
import org.objectweb.cjdbc.console.gui.popups.BackendPopUpMenu;
import org.objectweb.cjdbc.console.jmx.RmiJmxClient;

/**
 * This class defines a BackendObject
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BackendObject extends AbstractGuiObject
    implements
      Transferable,
      Cloneable

{
  private String               backendState;
  private String               database;
  private String               controllerName;
  private DatabaseBackendMBean mbean;
  private String               user;
  private String               password;
  private CjdbcGui             gui;
  private RmiJmxClient         jmxClient;
  private BackendPopUpMenu menu;

  /**
   * Access the mbean from this bob object
   * 
   * @return <code>DatabaseBackendMBean</code> instance
   * @throws InstanceNotFoundException if fails
   * @throws IOException if fails
   */
  public DatabaseBackendMBean getMbean() throws InstanceNotFoundException,
      IOException
  {
    jmxClient = (RmiJmxClient) gui.getJmxClients().get(controllerName);
    user = gui.getGuiSession().getAuthenticatedDatabaseLogin(database);
    password = gui.getGuiSession().getAuthenticatedDatabasePassword(database);
    mbean = jmxClient.getDatabaseBackendProxy(database, getName(), user,
        password);
    return mbean;
  }

  /**
   * Creates a new <code>BackendObject.java</code> object
   * 
   * @param gui we are attached to
   * @param database the virtual database this backends belongs to
   * @param name the name of the backend of this backend object
   * @param listener backend transfer listener for DnD
   * @param controllerName the owner of this backend
   * @throws IOException if cannot access MBean
   * @throws InstanceNotFoundException if cannot locate MBean
   */
  public BackendObject(CjdbcGui gui, BackendTransferListener listener,
      String database, String name, String controllerName)
      throws InstanceNotFoundException, IOException
  {
    super();
    this.database = database;
    this.controllerName = controllerName;
    this.gui = gui;
    setText(name);
    setName(name);
    this.menu = new BackendPopUpMenu(gui, this);
    setBackground(Color.white);
    setVerticalTextPosition(SwingConstants.BOTTOM);

    getMbean();

    addMouseMotionListener(listener);
    addMouseListener(listener);
    addMouseListener(menu);

    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(this, // What component
        DnDConstants.ACTION_COPY_OR_MOVE, // What drag types?
        listener);// the listener
  }

  /**
   * Get the state of the backend
   * 
   * @return state of backend as defined in gui constants , null if unknown
   */
  public String getState()
  {
    return backendState;
  }

  /**
   * Set state of backend
   * 
   * @param state string description of the state
   */
  public void setState(String state)
  {
    this.backendState = state;
    if (state.equals(GuiConstants.BACKEND_STATE_ENABLED))
    {
      setIcon(GuiIcons.BACKEND_ENABLED_ICON);
      menu.setEnabled(true);
      menu.getBackendCheckpoint().setEnabled(false);
      menu.getBackendCreate().setEnabled(true);
      menu.getBackendRemove().setEnabled(false);
      
      menu.getBackendEnable().setEnabled(false);
      menu.getBackendDisable().setEnabled(true);
      menu.getBackendBackup().setEnabled(true);
      menu.getBackendRestore().setEnabled(false);
    }
    else if (state.equals(GuiConstants.BACKEND_STATE_DISABLED))
    {
      setIcon(GuiIcons.BACKEND_DISABLED_ICON);
      menu.getBackendCheckpoint().setEnabled(true);
      menu.getBackendCreate().setEnabled(true);
      menu.getBackendRemove().setEnabled(true);
      
      menu.getBackendEnable().setEnabled(true);
      menu.getBackendDisable().setEnabled(false);
      menu.getBackendBackup().setEnabled(true);
      menu.getBackendRestore().setEnabled(true);
    }
    else if (state.equals(GuiConstants.BACKEND_STATE_DISABLING))
    {
      setIcon(GuiIcons.BACKEND_DISABLING_ICON);
      menu.setEnabled(false);
    }
    else if (state.equals(GuiConstants.BACKEND_STATE_BACKUP))
    {
      setIcon(GuiIcons.BACKEND_BACKUP_ICON);
      menu.setEnabled(false);
    }
    else if (state.equals(GuiConstants.BACKEND_STATE_RESTORE))
    {
      setIcon(GuiIcons.BACKEND_RESTORE_ICON);
      menu.setEnabled(false);
    }
    else if (state.equals(GuiConstants.BACKEND_STATE_RECOVERY))
    {
      setIcon(GuiIcons.BACKEND_STATE_RECOVERY);
      menu.setEnabled(false);
    }
  }

  /**
   * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
   */
  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[]{DataFlavor.stringFlavor, DataFlavor.plainTextFlavor};
  }

  /**
   * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
   */
  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    if (flavor.equals(DataFlavor.stringFlavor)
        || flavor.equals(DataFlavor.plainTextFlavor))
      return true;
    else
      return false;
  }

  /**
   * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
   */
  public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException
  {
    return this.getText();
  }

  /**
   * Returns the database value.
   * 
   * @return Returns the database.
   */
  public String getDatabase()
  {
    return database;
  }

  /**
   * Returns the controllerName value.
   * 
   * @return Returns the controllerName.
   */
  public String getControllerName()
  {
    return controllerName;
  }

  /**
   * Sets the controllerName value.
   * 
   * @param controllerName The controllerName to set.
   */
  public void setControllerName(String controllerName)
  {
    this.controllerName = controllerName;
  }

  /**
   * @see java.awt.Component#setName(java.lang.String)
   */
  public void setName(String name)
  {
    super.setName(name);
    setText(name);
  }
  /**
   * Returns the jmxClient value.
   * 
   * @return Returns the jmxClient.
   */
  public RmiJmxClient getJmxClient()
  {
    return jmxClient;
  }
}