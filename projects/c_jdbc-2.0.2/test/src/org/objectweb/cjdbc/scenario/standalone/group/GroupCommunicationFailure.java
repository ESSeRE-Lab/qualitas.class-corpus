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

package org.objectweb.cjdbc.scenario.standalone.group;

import junit.framework.TestCase;

import org.jgroups.Channel;
import org.jgroups.Event;
import org.jgroups.JChannel;
import org.jgroups.UpHandler;

/**
 * This class defines a GroupCommunicationFailure
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk</a>
 * @version 1.0
 */
public class GroupCommunicationFailure extends TestCase
{
  /**
   * Test close channel
   * 
   * @throws Exception if fails
   */
  public void testCloseChannel() throws Exception
  {
    System.setProperty("org.apache.commons.logging.Log",
        "org.apache.commons.logging.impl.SimpleLog");
    JGroupsChannelTest test = new JGroupsChannelTest();
    System.out.println("done:"+test.toString());
  }

  class JGroupsChannelTest implements UpHandler
  {

    JChannel jChannel = null;

    /**
     * 
     * Creates a new <code>JGroupsChannelTest</code> object
     * 
     *
     */
    public JGroupsChannelTest()
    {
      String props = "TCP(start_port=7800;bind_addr=localhost;loopback=true):"
          + "TCPGOSSIP(timeout=3000;initial_hosts=localhost[7500]num_initial_members=3):"
          + "FD(timeout=2000;max_tries=4):"
          + "VERIFY_SUSPECT(timeout=1500;down_thread=false;up_thread=false):"
          + "pbcast.NAKACK(gc_lag=100;retransmit_timeout=600,1200,2400,4800):"
          + "pbcast.GMS(print_local_addr=true;join_timeout=5000;join_retry_timeout=2000;"
          + "shun=true)";
      try
      {
        jChannel = new JChannel(props);
        jChannel.setOpt(Channel.LOCAL, Boolean.FALSE);
        jChannel.setOpt(Channel.VIEW, Boolean.TRUE);
        jChannel.setUpHandler(this);
        jChannel.connect("testGroup");

        System.out.println(jChannel.printProtocolSpec(true));

        jChannel.close();
      }
      catch (Exception e)
      {
        System.out.println("Error channel " + e);
      }
    }

    /**
     * 
     * @see org.jgroups.UpHandler#up(org.jgroups.Event)
     */
    public void up(Event evt)
    {
      if (Event.VIEW_CHANGE == evt.getType())
      {
        System.out.println("got view change");
        jChannel.close();
        System.out.println("channel closed");
      }
    }

  }

}