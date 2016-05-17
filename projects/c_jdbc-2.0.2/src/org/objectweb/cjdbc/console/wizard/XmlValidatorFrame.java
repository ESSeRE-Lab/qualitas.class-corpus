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

import java.awt.Color;
import java.awt.HeadlessException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.console.gui.constants.GuiConstants;
import org.objectweb.cjdbc.console.gui.jtools.JTextAreaWriter;

/**
 * Used to report results of validating a database xml configuration file.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class XmlValidatorFrame extends JFrame
{

  final String    eol = System.getProperty("line.separator");
  JTextArea       report;
  JTextAreaWriter writer;

  /**
   * Creates a new <code>XmlValidatorFrame</code> object
   * 
   * @param fileName the file to validate
   * @throws java.awt.HeadlessException if an error occurs
   */
  public XmlValidatorFrame(String fileName) throws HeadlessException
  {
    super(WizardTranslate.get("init.validator.frame"));
    GuiConstants.centerComponent(this, WizardConstants.VALIDATOR_WIDTH,
        WizardConstants.VALIDATOR_HEIGHT);
    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    report = new JTextArea();
    writer = new JTextAreaWriter(report);
    report.setForeground(new Color(9498256));
    this.getContentPane().add(report);
    this.pack();
    this.setVisible(true);
    writeLine(WizardTranslate.get("init.validator.echo", fileName));
  }

  /**
   * Append a line to the text area
   * 
   * @param s the string to append
   */
  public void writeLine(String s)
  {
    try
    {
      writer.write(s + eol);
      writer.flush();
      report.repaint();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Set Warning color on the report
   */
  public void setWarning()
  {
    report.setForeground(new Color(16758465));
  }

}