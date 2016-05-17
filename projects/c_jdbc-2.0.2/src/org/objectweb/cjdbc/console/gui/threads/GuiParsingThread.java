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

package org.objectweb.cjdbc.console.gui.threads;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.objectweb.cjdbc.common.xml.XmlValidator;

/**
 * This class defines a GuiParsingThread
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class GuiParsingThread extends Thread
    implements
      KeyListener,
      CaretListener
{
  private JTextPane xmlTextPane;
  private JTextPane outputPane;

  //private DTDParser dtdparser;
  //private DTD dtd;

  /**
   * Creates a new <code>GuiParsingThread.java</code> object
   * 
   * @param xmlTextPane panel that contains the xml to parse
   */
  public GuiParsingThread(JTextPane xmlTextPane)
  {
    this.xmlTextPane = xmlTextPane;
    //xmlTextPane.addCaretListener(this);
    /*
     * try { this.dtdparser = new DTDParser(this.getClass().getResource(
     * "/c-jdbc-controller.dtd")); dtd = dtdparser.parse(); } catch (Exception
     * e) { e.printStackTrace(); }
     */
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    while (true)
    {
      synchronized (this)
      {
        try
        {
          wait();
        }
        catch (InterruptedException e)
        {
        }
        XmlValidator validator = new XmlValidator("c-jdbc-controller.dtd",
            xmlTextPane.getText());
        StringBuffer buffer = new StringBuffer();
        ArrayList exceptions = validator.getExceptions();
        for (int i = 0; i < exceptions.size(); i++)
          buffer.append(((Exception) (exceptions.get(i))).getMessage() + "\n");
        outputPane.setText(buffer.toString());
      }
    }
  }

  /**
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(KeyEvent e)
  {
    synchronized (this)
    {
      this.notify();
    }
  }

  /**
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e)
  {
  }

  /**
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped(KeyEvent e)
  {

  }

  /**
   * Set the output panel for this parsing thread
   * 
   * @param rightPane the pane to display the output
   */
  public void setOutputPane(JTextPane rightPane)
  {
    this.outputPane = rightPane;
  }

  /**
   * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
   */
  public void caretUpdate(CaretEvent e)
  {
    //    int pos = e.getDot();
    //    String string = xmlTextPane.getText();
    //    int low = string.indexOf("<", pos);
    //    int sup = string.indexOf(">", pos);
    //    int end = string.indexOf("/>", pos);
    //    int close = string.indexOf("</", pos);
    //
    //    System.out.println("low:" + low + ":sup:" + sup + ":end:" + end +
    //     ":close:"
    //        + close);
    //
    //    String element = null;
    //    if (low == -1 && end == -1 && close == -1)
    //    {
    //      //System.out.println("0");
    //      // end of the file last tag
    //      if (sup == -1)
    //        sup = string.length() - 2;
    //      int ind = sup;
    //      while (string.charAt(ind) != '/')
    //        ind--;
    //      element = string.substring(ind + 1, sup);
    //    }
    //    else if (lt(sup, low) && lt(sup, end) && lt(sup, close))
    //    {
    //      //System.out.println("1");
    //      // we search for an opening tag, like <tag>
    //      int ind = sup;
    //      while (string.charAt(ind) != '<')
    //        ind--;
    //      element = string.substring(ind + 1, string.indexOf(' ', ind));
    //    }
    //    else if (lt(end, sup) && lt(end, low) && lt(end, close))
    //    {
    //      //System.out.println("2");
    //      //we search for a standalone closed tag <tag/>
    //      int ind = end;
    //      while (string.charAt(ind) != '<')
    //        ind--;
    //      element = string.substring(ind + 1, string.indexOf(' ', ind));
    //    }
    //    else if (lt(low, sup) && lt(low, end) && lt(low, close))
    //    {
    //      //System.out.println("3");
    //      //we search for the next starting tag <tag>
    //      element = string.substring(low + 1, nextLow(string, low));
    //    }
    //    else if (lt(close, sup) && lt(close, end) && close == low)
    //    {
    //      //System.out.println("4");
    //      //we search for the name of the closing tag <tag>...</tag>
    //      int space = string.indexOf('>', close + 2);
    //      element = string.substring(close + 2, space);
    //    }
    //
    //    DTDElement elm = (DTDElement) dtd.elements.get(element);
    //    //System.out.println(element);
    //    
    //    Hashtable attributes = elm.attributes;
    //    Enumeration enume = attributes.keys();
    //    while (enume.hasMoreElements())
    //    {
    //      DTDAttribute att = (DTDAttribute) attributes.get(enume.nextElement());
    //      ////System.out.println(att.name+":"+att.defaultValue);
    //    }

  }

  //  private int nextLow(String string, int low)
  //  {
  //    int space = string.indexOf(' ', low);
  //    int ll = string.indexOf('>', low);
  //    int lc = string.indexOf('/', low);
  //    int ref = 0;
  //    if (lt(space, ll) && lt(space, lc))
  //      ref = space;
  //    else if (lt(ll, space) && lt(ll, lc))
  //      ref = ll;
  //    else if (lt(lc, space) && lt(lc, ll))
  //      ref = lc;
  //    return ref;
  //  }
  //
  //  private boolean lt(int l1, int l2)
  //  {
  //    if (l1 < 0)
  //      return false;
  //    else if (l2 < 0 || l1 < l2)
  //      return true;
  //    else
  //      return false;
  //  }
}