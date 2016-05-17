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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.console.wizard.WizardConstants;
import org.objectweb.cjdbc.console.wizard.WizardTab;
import org.objectweb.cjdbc.console.wizard.WizardTabs;
import org.objectweb.cjdbc.console.wizard.objects.User;

/**
 * This tab has al the fields for the authentication section of the virtual
 * database.
 * 
 * @see <code>WizardTab</code>
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class AuthenticationTab extends WizardTab
{
  /** User panel */
  public UserPanel users;
  /** Admin panel */
  public UserPanel admin;

  /**
   * Creates a new <code>AuthenticationTab</code> object
   * 
   * @param tabs the wizard tab
   */
  public AuthenticationTab(WizardTabs tabs)
  {
    super(tabs, WizardConstants.TAB_AUTHENTICATION);

    admin = new UserPanel("label.admin");
    this.add(admin, constraints);

    constraints.gridy = ++constraints.gridy;

    users = new UserPanel("label.users");
    this.add(users, constraints);
  }

  /**
   * Return the list of users
   * 
   * @return users list
   */
  public ArrayList getUsers()
  {
    JComboBox usersBox = users.getUsersCombo();
    return WizardConstants.getItemsFromCombo(usersBox);
  }

  /**
   * This class defines a UserPanel
   * 
   * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
   * @version 1.0
   */
  public class UserPanel extends JPanel
      implements
        ItemListener,
        ActionListener,
        FocusListener
  {
    private JComboBox  usersCombo;
    private JButton    buttonAdd;
    private JButton    buttonRemove;
    int                currentIndex = 0;
    private JTextField userName;
    private JTextField password;

    /**
     * Creates a new <code>UserPanel</code> object
     * 
     * @param title the panel title
     */
    public UserPanel(String title)
    {

      this.setBorder(BorderFactory.createTitledBorder(WizardTranslate
          .get(title)));
      this.setLayout(new GridBagLayout());
      GridBagConstraints localconstraints = new GridBagConstraints();
      localconstraints.fill = GridBagConstraints.HORIZONTAL;
      localconstraints.gridy = 0;
      localconstraints.weightx = 1.0;
      localconstraints.weighty = 1.0;

      localconstraints.gridwidth = 2;
      usersCombo = new JComboBox(new Object[]{});
      usersCombo.addItemListener(this);
      this.add(usersCombo, localconstraints);

      localconstraints.gridwidth = 1;

      localconstraints.gridy = ++localconstraints.gridy;
      localconstraints.gridx = 0;
      buttonAdd = new JButton(WizardTranslate.get("label.adduser"));
      buttonAdd.setActionCommand(WizardConstants.COMMAND_ADD_USER);
      buttonAdd.addActionListener(this);
      this.add(buttonAdd, localconstraints);
      localconstraints.gridx = 1;
      buttonRemove = new JButton(WizardTranslate.get("label.removeuser"));
      buttonRemove.setActionCommand(WizardConstants.COMMAND_REMOVE_USER);
      buttonRemove.addActionListener(this);
      this.add(buttonRemove, localconstraints);

      localconstraints.gridy = ++localconstraints.gridy;
      localconstraints.gridx = 0;
      this.add(new JLabel(WizardTranslate.get("label.username")),
          localconstraints);
      localconstraints.gridx = 1;
      userName = new JTextField();
      userName.addFocusListener(this);
      this.add(userName, localconstraints);

      localconstraints.gridy = ++localconstraints.gridy;
      localconstraints.gridx = 0;
      this.add(new JLabel(WizardTranslate.get("label.password")),
          localconstraints);
      localconstraints.gridx = 1;
      password = new JTextField();
      password.addFocusListener(this);
      this.add(password, localconstraints);
    }

    /**
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    public void focusGained(FocusEvent e)
    {

    }

    /**
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    public void focusLost(FocusEvent e)
    {
      if (e.getSource() == userName || e.getSource() == password)
      {
        User user = ((User) usersCombo.getSelectedItem());
        user.setUsername(userName.getText());
        user.setPassword(password.getText());
        usersCombo.repaint();
      }
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
      String command = e.getActionCommand();
      if (command.equals(WizardConstants.COMMAND_ADD_USER))
      {
        User user = new User();
        usersCombo.addItem(user);
        usersCombo.setSelectedItem(user);
        usersCombo.validate();
        tabs.usersChanged();
      }
      else if (command.equals(WizardConstants.COMMAND_REMOVE_USER))
      {
        User user = (User) usersCombo.getSelectedItem();
        usersCombo.removeItem(user);
        usersCombo.validate();
        usersCombo.repaint();
        tabs.usersChanged();
      }

    }

    /**
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent e)
    {
      User user = (User) usersCombo.getSelectedItem();
      if (user == null)
      {
        userName.setText("");
        password.setText("");
      }
      else
      {
        userName.setText(user.getUsername());
        password.setText(user.getPassword());
      }
    }

    /**
     * Returns the usersCombo value.
     * 
     * @return Returns the usersCombo.
     */
    public JComboBox getUsersCombo()
    {
      return usersCombo;
    }
  }
}