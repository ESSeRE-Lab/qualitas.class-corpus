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

package org.objectweb.cjdbc.console.gui.jtools;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 * This class defines a MultiLineToolTip
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author Zafir Anjum
 * @version 1.0
 */
public class JMultiLineToolTip extends JToolTip
{

  /**
   * Creates a new <code>JMultiLineToolTip.java</code> object
   */
  public JMultiLineToolTip()
  {
    updateUI();
  }

  /**
   * @see javax.swing.JComponent#updateUI()
   */
  public void updateUI()
  {
    setUI(MultiLineToolTipUI.createUI(this));
  }

  /**
   * Set number of columns for the tool tip
   * 
   * @param columns integer
   */
  public void setColumns(int columns)
  {
    this.columns = columns;
    this.fixedwidth = 0;
  }

  /**
   * getColumns method
   * 
   * @return integer
   */
  public int getColumns()
  {
    return columns;
  }

  /**
   * setFixedWidth
   * 
   * @param width value
   */
  public void setFixedWidth(int width)
  {
    this.fixedwidth = width;
    this.columns = 0;
  }

  /**
   * getFixedWidth definition.
   * 
   * @return integer
   */
  public int getFixedWidth()
  {
    return fixedwidth;
  }

  protected int columns    = 0;
  protected int fixedwidth = 0;
}

class MultiLineToolTipUI extends BasicToolTipUI
{
  static MultiLineToolTipUI  sharedInstance = new MultiLineToolTipUI();
  Font                       smallFont;
  static JToolTip            tip;
  protected CellRendererPane rendererPane;

  private static JTextArea   textArea;

  /**
   * Returns the shared <code>ComponentUI</code> instance
   */
  public static ComponentUI createUI()
  {
    return sharedInstance;
  }

  /**
   * Create a new <code>MultiLineToolTipUI</code>
   */
  public MultiLineToolTipUI()
  {
    super();
  }

  /**
   * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
   */
  public void installUI(JComponent c)
  {
    super.installUI(c);
    tip = (JToolTip) c;
    rendererPane = new CellRendererPane();
    c.add(rendererPane);
  }

  /**
   * @see javax.swing.plaf.ComponentUI#uninstallUI(javax.swing.JComponent)
   */
  public void uninstallUI(JComponent c)
  {
    super.uninstallUI(c);

    c.remove(rendererPane);
    rendererPane = null;
  }

  /**
   * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics,
   *      javax.swing.JComponent)
   */
  public void paint(Graphics g, JComponent c)
  {
    Dimension size = c.getSize();
    textArea.setBackground(c.getBackground());
    rendererPane.paintComponent(g, textArea, c, 1, 1, size.width - 1,
        size.height - 1, true);
  }

  /**
   * @see javax.swing.plaf.ComponentUI#getPreferredSize(javax.swing.JComponent)
   */
  public Dimension getPreferredSize(JComponent c)
  {
    String tipText = ((JToolTip) c).getTipText();
    if (tipText == null)
      return new Dimension(0, 0);
    textArea = new JTextArea(tipText);
    rendererPane.removeAll();
    rendererPane.add(textArea);
    textArea.setWrapStyleWord(true);
    int width = ((JMultiLineToolTip) c).getFixedWidth();
    int columns = ((JMultiLineToolTip) c).getColumns();

    if (columns > 0)
    {
      textArea.setColumns(columns);
      textArea.setSize(0, 0);
      textArea.setLineWrap(true);
      textArea.setSize(textArea.getPreferredSize());
    }
    else if (width > 0)
    {
      textArea.setLineWrap(true);
      Dimension d = textArea.getPreferredSize();
      d.width = width;
      d.height++;
      textArea.setSize(d);
    }
    else
      textArea.setLineWrap(false);

    Dimension dim = textArea.getPreferredSize();

    dim.height += 1;
    dim.width += 1;
    return dim;
  }

  /**
   * @see javax.swing.plaf.ComponentUI#getMinimumSize(javax.swing.JComponent)
   */
  public Dimension getMinimumSize(JComponent c)
  {
    return getPreferredSize(c);
  }

  /**
   * @see javax.swing.plaf.ComponentUI#getMaximumSize(javax.swing.JComponent)
   */
  public Dimension getMaximumSize(JComponent c)
  {
    return getPreferredSize(c);
  }
}