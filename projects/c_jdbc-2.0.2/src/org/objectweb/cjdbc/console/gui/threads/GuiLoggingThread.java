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

import java.net.Socket;

import javax.swing.JTextArea;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Layout;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.net.SocketNode;
import org.objectweb.cjdbc.console.gui.jtools.JTextAreaWriter;

/**
 * This class defines a GuiLoggingThread
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class GuiLoggingThread extends Thread
{
  private Socket     logSocket;
  private SocketNode node;
  private JTextArea  loggingTextPane;
  private String     host;
  private Thread nodeThread;

  /**
   * Creates a new <code>GuiLoggingThread.java</code> object
   * 
   * @param loggingTextPane the text area where to store output
   * @param host where the log server is running
   */
  public GuiLoggingThread(JTextArea loggingTextPane, String host)
  {
    super();
    this.host = host;
    this.loggingTextPane = loggingTextPane;
  }

  /**
   * Returns the host value.
   * 
   * @return Returns the host.
   */
  public String getHost()
  {
    return host;
  }
  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      BasicConfigurator.configure();
      JTextAreaWriter writer = new JTextAreaWriter(loggingTextPane);
      Layout layout = new PatternLayout("%d %-5p %c{3} %m\n");
      WriterAppender appender = new WriterAppender(layout, writer);
      appender.setImmediateFlush(true);
      LogManager.getRootLogger().removeAllAppenders();
      LogManager.getRootLogger().addAppender(appender);
      logSocket = new Socket(host, 9010);
      node = new SocketNode(logSocket, LogManager.getLoggerRepository());
      nodeThread = new Thread(node);
      nodeThread.start();
    }
    catch (Exception e)
    {
      // ignore
    }
  }

  /**
   * Quitting
   * 
   * 
   */
  public void quit()
  {
    if(nodeThread!=null)
      nodeThread.interrupt();
  }
}