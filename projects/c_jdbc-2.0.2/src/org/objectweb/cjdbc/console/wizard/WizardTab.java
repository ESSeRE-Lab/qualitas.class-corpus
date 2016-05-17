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

package org.objectweb.cjdbc.console.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.console.wizard.listeners.WizardListener;

/**
 * This is the asbtract class used to define tabs in the wizard. A tab is used
 * to write and fill the necessary configuration of a particular point of the
 * virtual database xml configuration file.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public abstract class WizardTab extends JPanel implements WizardListener
{
  protected WizardTabs            tabs;
  protected GridBagConstraints    constraints;
  protected static ResourceBundle types;

  /**
   * Creates a new <code>WizardTab</code> object
   * 
   * @param tabs tabs to display
   * @param name Wizard name
   */
  public WizardTab(WizardTabs tabs, String name)
  {
    super();
    this.tabs = tabs;
    this.setName(WizardTranslate.get(name));
    this.setLayout(new GridBagLayout());
    this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    constraints = new GridBagConstraints();
    constraints.gridy = 0;

    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
  }

  /**
   * @see org.objectweb.cjdbc.console.wizard.listeners.WizardListener#backendListChanged()
   */
  public void backendListChanged()
  {

  }

  /**
   * @see org.objectweb.cjdbc.console.wizard.listeners.WizardListener#distributionChanged()
   */
  public void distributionChanged()
  {
  }

  /**
   * @see org.objectweb.cjdbc.console.wizard.listeners.WizardListener#usersChanged()
   */
  public void usersChanged()
  {

  }

  /**
   * Get the Databases Types.
   * 
   * @return database types
   */
  public static final String[] getDatabasesTypes()
  {
    if (types == null)
      types = ResourceBundle.getBundle("database");
    String typestring = types.getString("database.types");

    StringTokenizer token = new StringTokenizer(typestring, ",");
    int tokens = token.countTokens();
    String[] databases = new String[tokens];
    int count = 0;
    while (token.hasMoreTokens())
    {
      databases[count] = token.nextToken();
      count++;
    }

    return databases;
  }

  /**
   * Show backend select dialog
   * 
   * @return the selected backend
   */
  public static String showBackendSelectDialog()
  {
    return (String) JOptionPane.showInputDialog(null, WizardTranslate
        .get("label.backend.select"), WizardTranslate
        .get("label.backend.select"), JOptionPane.QUESTION_MESSAGE, null,
        getDatabasesTypes(), null);
  }
}