/*  ActiveLink.java  */

package nik777.aoi;

/*
 * ActiveLink: A component that can be embedded as a link in a document
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


import java.io.*;

import java.awt.event.*;
import javax.swing.*;

import bsh.*;


/**
 *  A lightweight component that can be embedded as a link in a document.
 */
public class ActiveLink extends JButton implements ActionListener
{
    String script;
    String arg;

    protected static final String getPlugin = "getPlugin(String name) { plugins = ModellingApp.getPlugins(); for (i = 0; i < plugins.length; i++) { if (plugins[i].getClass().getName().equals(name)) return plugins[i]; }}";

    public ActiveLink()
    {
	addActionListener(this);
	//System.out.println("ActiveLink created...");
    }

    public void setScript(String script)
    { this.script = script; }

    public String getScript()
    { return script; }

    public void setArg(String arg)
    { this.arg = arg; }

    public String getArg()
    { return arg; }

    public void actionPerformed(ActionEvent ev)
    {
	if (script != null && script.length() > 0) {

	    // indirect script reference
	    if (script.charAt(0) == '@') {

		try {
		    InputStream in = getClass().getClassLoader()
			.getResourceAsStream(script.substring(1));

		    if (in != null) {
			ByteArrayOutputStream bas =
			    new ByteArrayOutputStream();

			int len = 0;
			byte[] buff = new byte[1024];
			do {
			    len = in.read(buff, 0, buff.length);
			    if (len > 0) bas.write(buff, 0, len);
			} while (len >= 0);

			if (bas.size() > 0)
			    script = new String(bas.toByteArray());
		    }
		} catch (Exception e) {
		    System.out.println("Error reading script: " + script +
				       "\n\t" + e);
		}
	    }

	    try {
		Interpreter interpreter = new Interpreter();
		interpreter.eval("import artofillusion.*;");
		//interpreter.eval(getPlugin);
		interpreter.set("link", this);
		interpreter.set("event", ev);
		interpreter.set("arg", arg);

		interpreter.eval(script);
	    } catch (Exception e) {
		System.out.println("ActiveLink: script error " + e);
	    }
	}
    }
}
