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

package org.objectweb.cjdbc.scenario.tools.util;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * This class defines a MemoryProfiler
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class MemoryProfiler extends Thread
    implements
      WindowListener,
      KeyListener
{

  boolean            useFrame = false;
  boolean            quit     = false;

  JFrame             frame;
  JTextField         freeMemory;
  JTextField         minMemory;
  JTextField         timeRunning;
  long               started;

  String             frees    = "";
  long               free     = 0;
  long               min      = 0;
  long               timeout  = 1000;
  private long       time;
  private String     stime;
  private JTextField refreshRate;
  private long       avg;
  private long       tot;
  private int        count;

  public void quit()
  {
    this.quit = true;
  }

  /**
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(KeyEvent e)
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e)
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped(KeyEvent e)
  {
    String s = refreshRate.getText();
    timeout = Long.parseLong(s) * 1000;
  }

  /**
   * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
   */
  public void windowActivated(WindowEvent e)
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
   */
  public void windowClosed(WindowEvent e)
  {
    this.useFrame = false;

  }

  /**
   * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
   */
  public void windowClosing(WindowEvent e)
  {
    this.useFrame = false;

  }

  /**
   * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
   */
  public void windowDeactivated(WindowEvent e)
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
   */
  public void windowDeiconified(WindowEvent e)
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
   */
  public void windowIconified(WindowEvent e)
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
   */
  public void windowOpened(WindowEvent e)
  {
    // TODO Auto-generated method stub

  }

  /**
   * Creates a new <code>MemoryProfiler</code> object
   */
  public MemoryProfiler()
  {
    if (useFrame)
    {
      frame = new JFrame("Values");
      GridLayout gridLayout = new GridLayout(4, 2);
      frame.getContentPane().setLayout(gridLayout);
      freeMemory = new JTextField();
      freeMemory.setEditable(false);
      minMemory = new JTextField();
      minMemory.setEditable(false);
      timeRunning = new JTextField();
      timeRunning.setEditable(false);
      refreshRate = new JTextField(String.valueOf(timeout / 1000));
      refreshRate.addKeyListener(this);
      frame.getContentPane().add(new JLabel("Free Memory"));
      frame.getContentPane().add(freeMemory);
      frame.getContentPane().add(new JLabel("Min Free Mem"));
      frame.getContentPane().add(minMemory);
      frame.getContentPane().add(new JLabel("Time Running"));
      frame.getContentPane().add(timeRunning);
      frame.getContentPane().add(new JLabel("Refresh Rate (in sec)"));
      frame.getContentPane().add(refreshRate);
      frame.setSize(200, 100);
      frame.pack();
      frame.validate();
      frame.setVisible(true);
      frame.addWindowListener(this);
    }
  }

  public void run()
  {
    started = System.currentTimeMillis();
    while (!quit)
    {
      synchronized (this)
      {
        try
        {
          wait(timeout);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
        updateMemoryValue();
      }
    }
  }

  private void updateMemoryValue()
  {
    free = (Runtime.getRuntime().freeMemory() / 1024);
    tot += free;
    count += 1;
    time = System.currentTimeMillis() - started;
    stime = String.valueOf(time / 1000);
    frees = String.valueOf(free);
    avg = tot / count;
    if (min == 0 || min > free)
      min = free;

    if (useFrame)
    {
      freeMemory.setText(frees);
      minMemory.setText(String.valueOf(min));
      timeRunning.setText(stime);
      frame.getContentPane().repaint();
    }
    else
      System.out.println("Memory: FREE " + free + " ko, MIN " + min
          + " ko, AVG " + avg + ". Running time is:" + stime + " s.");
  }

}