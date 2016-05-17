/*  Chat.java  */

package nik777.chat;

/*
 * <name>: <brief>
 *
 * Copyright (C) 200x <author> <location>
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


/**
 *  Self-configuring chat client.
 */

public class Chat
{
    public static final String VERSION = "1.0";

    public Chat()
    {}

    /**
     *  Chat.Connector interface
     */
    public static interface Connector
    {
	/**
	 *  associate with a corresponding Connector
	 */
	public void open(Chat.Connector chat);

	/**
	 *  disassociate from a connector
	 */
	public void close(Chat.Connector chat);

	/**
	 *  join the identified chat (channel, server, room, etc)
	 */
	public int join(String conn, String name, String password);

	/**
	 *  leave the identified chat *channel, server, room, etc)
	 */
	public void leave(int id);

	/**
	 *  add a user to a connection (chatroom, channel, forum, etc)
	 */
	public void add(String conn, String name, String details);

	/**
	 *  remove a user from a connection (chatroom, channel, forum, etc)
	 */
	public void remove(String conn, String name);

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
	public void accept(int id, String from, String to, String msg);
    }
}
