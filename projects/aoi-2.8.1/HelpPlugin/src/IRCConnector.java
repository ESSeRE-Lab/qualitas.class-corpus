/*  IRCConnector.java  */

package nik777.chat;

/*
 * IRCConnector: IRC connector for the Chat system.
 *
 * Copyright (C) 2006 Nik Trevallyn-Jones, Sydney, Australia
 *
 * Author: Nik Trevallyn-Jones, nik777@users.sourceforge.net
 * $Id: Exp $
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See version 2 of the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with this program. If not, the license, including version 2, is available
 * from the GNU project, at http://www.gnu.org.
 */

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

/**
 */
public class IRCConnector extends PircBot implements Chat.Connector
{
    protected String me;
    protected int chatid;
    protected Chat.Connector local;
    protected int userCount = 0;

    protected String host = "irc.freenode.net";
    protected int port = 6667;

    protected ArrayList user;
    protected HashMap chat;

    /*
     *  The Chat.Connector interface
     */

    /**
     *  associate with a corresponding Connector
     */
    public void open(Chat.Connector conn)
    { local = conn; }

    /**
     *  disassociate from a corresponding Connector
     */
    public void close(Chat.Connector conn)
    { local = null; }

    /**
     *  join the identified connection
     *  (channel, server, room, etc) as the specified user.
     */
    public int join(String conn, String name, String password)
    {
	me = name;

	try {
	    // call PircBot setVersion method
	    setVersion("ArtOfIllusion IRC client v" + Chat.VERSION);

	    // call the PircBot setName method
	    setName(name);

	    // call PircBot connect method
	    connect(host, port, password);

	    // call PircBot joinChannel method
	    joinChannel(conn);

	} catch (Exception e) {
	    System.out.println("IRCConncetor.join: " + e);
	    return -1;
	}

	return 1;
    }

    /**
     *  leave the identified chat *channel, server, room, etc)
     */
    public void leave(int id)
    {
	quitServer();
	disconnect();
	dispose();
    }

    /**
     */
    public void add(String conn, String name, String details)
    {}

    public void remove(String conn, String name)
    {}

    /**
     *  accept a message
     *
     *  If this connector is a receiving connector, then this
     *  method is called to make the connector accept a newly
     *  received message.
     *
     *  If this connector is a sending connector, then this method is
     *  called to send a message.
     */
    public void accept(int id, String from, String to, String msg)
    {
	// send server messages using the PircBot sendAction method
	if (to == null || msg.trim().startsWith("/")) {
	    System.out.println("action: " + msg);
	    msg = msg.trim();
	    int cut = msg.indexOf(' ');
	    if (cut > 0)
		sendAction(to, msg.substring(cut+1));
	    else
		sendAction(to, msg);
	}
	// call the PircBot sendMessage method
	else sendMessage(to, msg);
    }

    /*
     *  The PircBot interface.
     */

    /**
     *  handle the initial user list
     */
    protected void onUserList(String channel, User[] users)
    {
	System.out.println("receiving USER list of " + users.length +
			   " entries");

	for (int i = 0; i < users.length; i++) {
	    if (!users[i].getNick().equals(me)) {
		local.add(channel, users[i].getNick(), null);
		userCount++;
	    }
	}
    }

    /**
     *  handle new registrations
     */
    protected void onJoin(String channel, String sender, String login,
			  String hostname)
    {
	System.out.println("new user: " + sender);
	local.add(channel, sender, "User " + sender +
		  "\n(" + login + "@" + hostname + ")");

	userCount++;
    }

    /**
     *
     */
    protected void onQuit(String nick, String login, String hostname,
			  String reason)
    {
	userCount--;
	local.remove(null, nick);
    }

    protected void onMessage(String channel, String sender,
			     String login, String hostname, String message)
    {
	/*
	if (userCount <= 1) {
	    // try to get the users...
	    User[] online = getUsers(channel);

	    onUserList(channel, online);
	}
	*/

	//System.out.println("onMessage: " + sender + " says " + message);
	local.accept(chatid, sender, me, message);
    }

    protected void onPrivateMessage(String sender, String login,
				    String hostname, String message)
    {
	Integer sessn = (Integer) chat.get(sender);
	if (sessn == null) {
	    sessn = new Integer(chat.size()+1);
	    chat.put(sender, sessn);
	}

	//System.out.println("onMessage: " + sender + " says " + message);
	local.accept(sessn.intValue(), sender, me, message);
    }

    protected void onTopic(String channel, String topic, String author,
			   long date, boolean changed)
    {
	System.out.println("onTopic...");

	local.accept(chatid, null, me, topic);
    }

    protected void onAction(String sender, String login, String hostname,
			    String target, String action)
    {
	System.out.println("Action: from=" + sender + "; to=" + target +
			   "; action=" + action);

	local.accept(chatid, sender, me, "/me " + action);
    }

}
