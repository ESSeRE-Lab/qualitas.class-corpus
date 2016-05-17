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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.console.wizard.tab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.console.wizard.WizardConstants;
import org.objectweb.cjdbc.console.wizard.WizardTab;
import org.objectweb.cjdbc.console.wizard.WizardTabs;
import org.objectweb.cjdbc.console.wizard.objects.Backend;
import org.objectweb.cjdbc.console.wizard.objects.ConnectionInfo;
import org.objectweb.cjdbc.console.wizard.objects.ConnectionParameterDialog;
import org.objectweb.cjdbc.console.wizard.objects.ConnectionTypeInfo;
import org.objectweb.cjdbc.console.wizard.objects.User;

/**
 * This tab has the required field to define backend information and their
 * related connection managers
 * 
 * @see <code>WizardTab</code>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class BackendTab extends WizardTab
    implements
      ItemListener,
      FocusListener,
      ActionListener
{
  /** Combo boxes for backends */
  public JComboBox           backendsCombo;
  private JTextField         backendName;
  private JTextField         backendUrl;
  private JButton            buttonAdd;
  private JButton            buttonRemove;
  private JTextField         backendDriver;
  private JTextField         backendDriverPath;
  private JTextField         backendStatement;
  private JCheckBox          gatherSystemTables;
  private JComboBox          dynamicPrecision;
  private JComboBox          users;
  private GridBagConstraints connectionconstraints;
  private JPanel             connections;
  private JTextField         rLogin;
  private JTextField         rPassword;
  private JTextField         urlparameters;
  private JComboBox          connectiontype;
  private JButton            connectionParameter;

  /**
   * Creates a new <code>BackendTab</code> object
   * 
   * @param tabs wizard tabs
   */
  public BackendTab(WizardTabs tabs)
  {
    super(tabs, WizardConstants.TAB_BACKENDS);

    ///////////////////////////////////////////////////////////////////////////
    // list panel
    ///////////////////////////////////////////////////////////////////////////
    JPanel list = new JPanel();
    list.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.list")));
    list.setLayout(new GridBagLayout());
    GridBagConstraints listconstraints = new GridBagConstraints();
    listconstraints.fill = GridBagConstraints.HORIZONTAL;

    listconstraints.weightx = 1.0;
    listconstraints.weighty = 1.0;
    listconstraints.gridy = 0;
    listconstraints.gridwidth = 2;

    backendsCombo = new JComboBox(new Object[]{});
    backendsCombo.addItemListener(this);

    list.add(backendsCombo, listconstraints);

    listconstraints.gridy = ++listconstraints.gridy;
    listconstraints.gridwidth = 1;

    listconstraints.gridx = 0;
    buttonAdd = new JButton(WizardTranslate.get("label.addbackend"));
    buttonAdd.setActionCommand(WizardConstants.COMMAND_ADD_BACKEND);
    buttonAdd.addActionListener(this);
    list.add(buttonAdd, listconstraints);

    listconstraints.gridx = 1;
    buttonRemove = new JButton(WizardTranslate.get("label.removebackend"));
    buttonRemove.setActionCommand(WizardConstants.COMMAND_REMOVE_BACKEND);
    buttonRemove.addActionListener(this);
    list.add(buttonRemove, listconstraints);

    this.add(list, constraints);

    constraints.gridy = ++constraints.gridy;

    ///////////////////////////////////////////////////////////////////////////
    // general panel
    ///////////////////////////////////////////////////////////////////////////
    JPanel general = new JPanel();
    general.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.general")));
    general.setLayout(new GridBagLayout());
    GridBagConstraints generalconstraints = new GridBagConstraints();
    generalconstraints.fill = GridBagConstraints.HORIZONTAL;
    generalconstraints.weightx = 1.0;

    generalconstraints.gridy = ++generalconstraints.gridy;
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.backendName")),
        generalconstraints);
    generalconstraints.gridx = 1;
    backendName = new JTextField();
    backendName.addFocusListener(this);
    general.add(backendName, generalconstraints);

    generalconstraints.gridy = ++generalconstraints.gridy;
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.backendUrl")),
        generalconstraints);
    generalconstraints.gridx = 1;
    backendUrl = new JTextField();
    backendUrl.addFocusListener(this);
    general.add(backendUrl, generalconstraints);

    generalconstraints.gridy = ++generalconstraints.gridy;
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.backendDriver")),
        generalconstraints);
    generalconstraints.gridx = 1;
    backendDriver = new JTextField();
    backendDriver.addFocusListener(this);
    general.add(backendDriver, generalconstraints);

    generalconstraints.gridy = ++generalconstraints.gridy;
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.backendDriverPath")),
        generalconstraints);
    generalconstraints.gridx = 1;
    backendDriverPath = new JTextField();
    backendDriverPath.addFocusListener(this);
    general.add(backendDriverPath, generalconstraints);

    generalconstraints.gridy = ++generalconstraints.gridy;
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.backendStatement")),
        generalconstraints);
    generalconstraints.gridx = 1;
    backendStatement = new JTextField();
    backendStatement.addFocusListener(this);
    general.add(backendStatement, generalconstraints);

    generalconstraints.gridy = ++generalconstraints.gridy;
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.gatherSystemTables")),
        generalconstraints);
    generalconstraints.gridx = 1;
    gatherSystemTables = new JCheckBox();
    gatherSystemTables.setName("label.gatherSystemTables");
    gatherSystemTables.addFocusListener(this);
    general.add(gatherSystemTables, generalconstraints);

    generalconstraints.gridy = ++generalconstraints.gridy;
    dynamicPrecision = new JComboBox(WizardConstants.DYNAMIC_PRECISION);
    dynamicPrecision.setSelectedItem(WizardConstants.DEFAULT_DYNAMIC_PRECISION);
    dynamicPrecision.addItemListener(this);
    dynamicPrecision.addFocusListener(this);
    generalconstraints.gridx = 0;
    general.add(new JLabel(WizardTranslate.get("label.dynamicPrecision")),
        generalconstraints);
    generalconstraints.gridx = 1;
    general.add(dynamicPrecision, generalconstraints);

    this.add(general, constraints);

    constraints.gridy = ++constraints.gridy;

    ///////////////////////////////////////////////////////////////////////////
    // Connection managers
    ///////////////////////////////////////////////////////////////////////////

    connections = new JPanel();
    connections.setBorder(BorderFactory.createTitledBorder(WizardTranslate
        .get("label.connections")));
    connections.setLayout(new GridBagLayout());
    connectionconstraints = new GridBagConstraints();
    connectionconstraints.fill = GridBagConstraints.HORIZONTAL;
    connectionconstraints.weightx = 1.0;

    // users list
    setUsersComboBox();

    // rLogin
    connectionconstraints.gridy = ++connectionconstraints.gridy;
    connectionconstraints.gridx = 0;
    connections.add(new JLabel(WizardTranslate.get("label.rLogin")),
        connectionconstraints);
    connectionconstraints.gridx = 1;
    rLogin = new JTextField();
    rLogin.addFocusListener(this);
    connections.add(rLogin, connectionconstraints);

    // rPassword
    connectionconstraints.gridy = ++connectionconstraints.gridy;
    connectionconstraints.gridx = 0;
    connections.add(new JLabel(WizardTranslate.get("label.rPassword")),
        connectionconstraints);
    connectionconstraints.gridx = 1;
    rPassword = new JTextField();
    rPassword.addFocusListener(this);
    connections.add(rPassword, connectionconstraints);

    // urlparameters
    connectionconstraints.gridy = ++connectionconstraints.gridy;
    connectionconstraints.gridx = 0;
    connections.add(new JLabel(WizardTranslate.get("label.urlparameters")),
        connectionconstraints);
    connectionconstraints.gridx = 1;
    urlparameters = new JTextField();
    urlparameters.addFocusListener(this);
    connections.add(urlparameters, connectionconstraints);

    // connectiontype
    connectionconstraints.gridy = ++connectionconstraints.gridy;
    connectiontype = new JComboBox(WizardConstants.CONNECTION_MANAGERS);
    connectionconstraints.gridx = 0;
    connectiontype.addFocusListener(this);
    connectiontype.addItemListener(this);
    connections.add(new JLabel(WizardTranslate.get("label.connectiontype")),
        connectionconstraints);
    connectionconstraints.gridx = 1;
    connections.add(connectiontype, connectionconstraints);

    // edit connection parameters
    connectionconstraints.gridy = ++connectionconstraints.gridy;
    connectionParameter = new JButton(WizardTranslate
        .get("label.edit.connection.parameters"));
    connectionParameter
        .setActionCommand(WizardConstants.COMMAND_EDIT_CONNECTION_PARAM);
    connectionconstraints.gridx = 1;
    connectionParameter.setEnabled(false);
    connectionParameter.addActionListener(this);
    connections.add(connectionParameter, connectionconstraints);

    this.add(connections, constraints);

  }

  /**
   * Set the user combo box
   */
  public void setUsersComboBox()
  {
    if (users != null)
      connections.remove(users);
    users = new JComboBox(tabs.getUsers().toArray());
    users.addItemListener(this);
    connectionconstraints.gridy = 0;
    connectionconstraints.gridx = 0;
    connections.add(new JLabel(WizardTranslate.get("label.users")),
        connectionconstraints);
    connectionconstraints.gridx = 1;
    connections.add(users, connectionconstraints);
  }

  /**
   * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
   */
  public void focusGained(FocusEvent e)
  {

  }

  /**
   * @see org.objectweb.cjdbc.console.wizard.listeners.WizardListener#usersChanged()
   */
  public void usersChanged()
  {
    setUsersComboBox();
  }

  /**
   * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
   */
  public void focusLost(FocusEvent e)
  {
    Backend backend = ((Backend) backendsCombo.getSelectedItem());
    if (backend == null)
      return;
    backend.setName(backendName.getText());
    backend.setUrl(backendUrl.getText());
    backend.setConnectionTestStatement(backendStatement.getText());
    backend.setDriver(backendDriver.getText());
    backend.setDriverPath(backendDriverPath.getText());
    String string = (String) dynamicPrecision.getSelectedItem();
    backend.setDynamicPrecision(string);
    if (gatherSystemTables.isSelected())
      backend.setGatherSystemTables("true");
    else
      backend.setGatherSystemTables(null);

    setBackendInfo();
    backendsCombo.repaint();
  }

  private void setBackendInfo()
  {
    Backend backend = ((Backend) backendsCombo.getSelectedItem());

    if (backend == null)
      return;
    User user = (User) users.getSelectedItem();
    if (user != null)
    {
      Hashtable con = backend.getConnectionManagers();
      ConnectionInfo info = (ConnectionInfo) con.get(user);

      if (info == null)
        info = new ConnectionInfo();
      info.setRLogin(rLogin.getText());
      info.setRPassword(rPassword.getText());
      info.setUrlParameters(urlparameters.getText());
      ConnectionTypeInfo cinfo = info.getConnectionTypeInfo();
      if (cinfo == null)
      {
        new ConnectionTypeInfo();
      }
      cinfo.setType((String) connectiontype.getSelectedItem());
      info.setConnectionTypeInfo(cinfo);
      con.put(user, info);
      backend.setConnectionManagers(con);
    }
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e)
  {
    String command = e.getActionCommand();

    if (command.equals(WizardConstants.COMMAND_ADD_BACKEND))
    {

      String select = WizardTab.showBackendSelectDialog();
      Backend backend = new Backend();

      if (select != null)
      {
        backend.setName(select + backendsCombo.getItemCount());
        backend.setUrl(types.getString(select + ".url"));
        backend.setDriver(types.getString(select + ".driver"));
        backend.setConnectionTestStatement(types.getString(select
            + ".statement"));
      }

      backendsCombo.addItem(backend);
      backendsCombo.setSelectedItem(backend);
      backendsCombo.validate();
      tabs.backendListChanged();
    }
    else if (command.equals(WizardConstants.COMMAND_REMOVE_BACKEND))
    {
      Backend backend = (Backend) backendsCombo.getSelectedItem();
      backendsCombo.removeItem(backend);
      backendsCombo.validate();
      backendsCombo.repaint();
      tabs.backendListChanged();
    }
    else if (command.equals(WizardConstants.COMMAND_EDIT_CONNECTION_PARAM))
    {
      Backend backend = (Backend) backendsCombo.getSelectedItem();
      User user = (User) users.getSelectedItem();
      if (backend == null || user == null)
        return;

      Hashtable managers = backend.getConnectionManagers();
      ConnectionInfo info = ((ConnectionInfo) managers.get(user));
      ConnectionTypeInfo infoType;
      if (info == null)
      {
        info = new ConnectionInfo();
        infoType = new ConnectionTypeInfo();
        infoType.setType((String) connectiontype.getSelectedItem());
        info.setConnectionTypeInfo(infoType);
        managers.put(user, info);
      }
      else
        infoType = info.getConnectionTypeInfo();

      ConnectionParameterDialog cpd = new ConnectionParameterDialog(infoType);

      infoType.setValues(cpd.getValues());
      info.setConnectionTypeInfo(infoType);
      managers.put(user, info);
      backend.setConnectionManagers(managers);
    }

  }

  /**
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged(ItemEvent e)
  {
    JComponent comp = (JComponent) e.getSource();
    if (comp == dynamicPrecision)
      return;

    Backend backend = (Backend) backendsCombo.getSelectedItem();
    // selected user changed load it from backend
    User user = (User) users.getSelectedItem();
    if (user == null || backend == null)
      return;

    Hashtable con = backend.getConnectionManagers();
    ConnectionInfo info = (ConnectionInfo) con.get(user);

    if (info == null)
    {
      info = new ConnectionInfo();
    }

    if (comp == connectiontype)
    {
      int selectedIndex = connectiontype.getSelectedIndex();
      if (selectedIndex == 0)
        connectionParameter.setEnabled(false);
      else
        connectionParameter.setEnabled(true);
      connectionParameter.repaint();
      return;
    }

    rLogin.setText(info.getRLogin());
    rPassword.setText(info.getRPassword());
    urlparameters.setText(info.getUrlParameters());
    connectiontype.setSelectedItem(info.getConnectionTypeInfo().getType());

    if (backend == null)
    {
      backendName.setText("");
      backendUrl.setText("");
      backendDriver.setText("");
      backendDriverPath.setText("");
      backendStatement.setText("");
      gatherSystemTables.setSelected(false);
      dynamicPrecision
          .setSelectedItem(WizardConstants.DEFAULT_DYNAMIC_PRECISION);
    }
    else
    {
      backendName.setText(backend.getName());
      backendUrl.setText(backend.getUrl());
      backendDriver.setText(backend.getDriver());
      backendDriverPath.setText(backend.getDriverPath());
      backendStatement.setText(backend.getConnectionTestStatement());
      gatherSystemTables.setSelected(backend.getGatherSystemTables() != null);
      if (backend.getDynamicPrecision() == null)
        dynamicPrecision
            .setSelectedItem(WizardConstants.DEFAULT_DYNAMIC_PRECISION);
      else
        dynamicPrecision.setSelectedItem(backend.getDynamicPrecision());
    }

  }

  /**
   * Get the list of backends
   * 
   * @return <tt>ArrayList</tt> of <tt>Backend</tt> objects
   */
  public ArrayList getBackends()
  {
    return WizardConstants.getItemsFromCombo(this.backendsCombo);
  }
}