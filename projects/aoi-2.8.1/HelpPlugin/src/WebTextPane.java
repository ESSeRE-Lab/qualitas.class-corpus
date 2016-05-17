/*  WebTextPane  */

package nik777.chat;

/*
 * WebTextPane: A text pane for displaying web content
 *
 * Copyright (C) 2007 Nik Trevallyn-Jones, Sydney, Australia.
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
 * with this program. If not, version 2 of the license is available
 * from the GNU project, at http://www.gnu.org.
 */

import artofillusion.ui.Translate;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.BorderLayout;

import java.awt.print.PrinterJob;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import buoy.widget.*;
import buoy.widget.*;

import java.util.HashMap;
import java.util.ArrayList;

import java.net.URL;

import java.io.*;

import net.sourceforge.helpgui.*;
import net.sourceforge.helpgui.gui.*;
import net.sourceforge.helpgui.util.BrowserControl;

/**
 */

public class WebTextPane extends Container
    implements HyperlinkListener, ActionListener
{
    protected static HashMap cache = new HashMap(1024);
    protected static ArrayList cacheIndex = new ArrayList(1024);

    protected JToolBar toolbar;
    protected JTextPane text;

    protected JButton jbPrev, jbNext, jbHome, jbPrint, jbBookmarks, jbCancel,
	jbSave, jbSettings;

    protected ArrayList history;

    protected int pos = -1;
    protected int maxCache = 1024;
    //    protected boolean browseExternal = false, browseTab = true;
    protected boolean caching = false;

    protected static String[] selections = new String[] {
	Translate.text("HelpPlugin:Ok"),
	Translate.text("HelpPlugin:Save"),
	Translate.text("HelpPlugin:Cancel")
    };

    public WebTextPane()
    {
	history = null;
	text = null;

	init();
    }

    public WebTextPane(JTextPane txtPane)
    {
	history = null;
	
	text = txtPane;
	init();
    }

    public WebTextPane(URL url)
    {
	history = new ArrayList(100);
	text = null;

	init();
	display(url);
    }

    protected void init()
    {
	setLayout(new BorderLayout());	

	//Construct a toolbar
	toolbar = new JToolBar();
	toolbar.setRollover(true);
	toolbar.setFloatable(false);
	toolbar.setBorderPainted(true);

	//Construct the buttons
	jbPrev  = new TestRolloverButton(new ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+ChatFrame.iconsPath+"/previous.gif")));
	jbNext  = new TestRolloverButton(new ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+ChatFrame.iconsPath+"/next.gif")));
	jbHome  = new TestRolloverButton(new ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+ChatFrame.iconsPath+"/home.gif")));
	jbBookmarks = new TestRolloverButton(new ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+ChatFrame.iconsPath+"/addbookmarks.gif")));
	jbPrint = new TestRolloverButton(new ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+ChatFrame.iconsPath+"/print.gif")));
	jbSave = new TestRolloverButton(new  ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+ChatFrame.iconsPath+"/filesave.png")));
	jbCancel = new TestRolloverButton(new ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+ChatFrame.iconsPath+"/button_cancel.png")));
	jbSettings = new TestRolloverButton(new ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+ChatFrame.iconsPath+"/settings.png")));
	

	jbPrev.addActionListener(this);
	jbNext.addActionListener(this);
	jbHome.addActionListener(this);
	jbPrint.addActionListener(this);
	jbBookmarks.addActionListener(this);
	jbSave.addActionListener(this);
	jbCancel.addActionListener(this);
	jbSettings.addActionListener(this);
		
	//Add buttons to toolbar
	toolbar.add(jbPrint);

	if (history != null) {
	    toolbar.add(jbPrev);
	    toolbar.add(jbNext);
	    toolbar.add(jbHome);
	    //toolbar.add(jbBookmarks);
	    toolbar.add(jbSettings);
	    toolbar.add(jbCancel);
	}
	else {
	    toolbar.add(jbSave);
	    toolbar.add(jbSettings);
	}

	add(toolbar, BorderLayout.NORTH);

	if (text == null) {
	    text = new JTextPane();
	    text.setEditable(false);
	    text.setContentType("text/html");
	    if (history != null) text.addHyperlinkListener(this);
	}

	JScrollPane scroll = new JScrollPane(text);
	scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	//	scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	
	add(scroll, BorderLayout.CENTER);
    }

    /**
     *  Setting this to <i>true</i> causes all URLs to be sent to an external
     *  web-browser for display.
     */
    /*
    public void setBrowseExternal(boolean tf)
    { browseExternal = tf; }
    */

    /**
     */
    public void setCache(boolean tf, int max)
    {
	caching = tf;
	if (max >= 0) maxCache = Math.max(64, max);
    }

    /**
     */
    public void home()
    {
	pos = 0;
	display((URL) history.get(pos));
    }

    /**
     */
    public void prev()
    {
	//System.out.println("prev; pos=" + pos + "/" + history.size());
	if (pos > 0 && history.size() > pos)
	    display((URL) history.get(--pos));
    }

    /**
     */
    public void next()
    {
	//System.out.println("next; pos=" + pos + "/" + history.size());
	if (pos < history.size()-1)
	    display((URL) history.get(++pos));
    }

    /**
     */
    public void print()
    {
	try {
//	    text.print();
	} catch (Exception e) {
	    System.out.println("WebTextPane.print: " + e);
	}
    }

    /**
     *
     */
    public void save()
    {
	File home = new File(System.getProperty("user.home"));
	Writer out = null;

	JFileChooser chooser = new JFileChooser();
	chooser.setSelectedFile(new File(home, "chatlog.html"));

	if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
	    try {
		out = new FileWriter(chooser.getSelectedFile());
		text.write(out);
		out.flush();
	    } catch (IOException e) {
		System.out.println("WebTextPane.save: " + e);
	    }
	    finally {
		try {
		    out.close();
		} catch (IOException e) {}
	    }
	}
    }

    public void settings()
    {
	
	BCheckBox browseBox = new
	    BCheckBox(Translate.text("HelpPlugin:browseExternal"),
		      ChatFrame.browseExternal);

	//BCheckBox tabBox = new
	//  BCheckBox(Translate.text("HelpPlugin:browseTab"), browseTab);

	Widget widgets[] = new Widget[] { browseBox /*, tabBox */ };

	int sel = (new BStandardDialog(Translate.text("HelpPlugin:Settings"),
				       widgets, BStandardDialog.PLAIN))
	    .showOptionDialog(null, selections, selections[0]);

	if (sel == 2) return;		// cancel

	ChatFrame.browseExternal = browseBox.getState();
	//browseTab = tabBox.getState();

	System.out.println("setting browseExternal=" +
			   ChatFrame.browseExternal);

	// save to prefs, if requested
	if (sel == 1) {
	}
    }

    public void actionPerformed(ActionEvent e)
    {
	if (e.getSource() instanceof JButton) {
	    if(e.getSource().equals(jbPrev)) 
		prev();
	    else if(e.getSource().equals(jbNext)) 
		next();
	    else if(e.getSource().equals(jbHome))
		home();
	    else if(e.getSource().equals(jbPrint)) 
		print();
	    else if(e.getSource().equals(jbCancel))
		getParent().remove(this);
	    else if (e.getSource().equals(jbSave))
		save();
	    else if (e.getSource().equals(jbSettings))
		settings();
	    //else if(e.getSource().equals(jbBookmarks)) 
		//addBookMarks();
	}
	/*else if (e.getSource() instanceof JMenuItem) {
	    String arg = e.getActionCommand();
	    if (arg.equals(lang.getText("previous"))) 
		helpView.previousPage();
	    else if (arg.equals(lang.getText("next"))) 
		helpView.nextPage();
	    else if (arg.equals(lang.getText("home"))) 
		helpView.goHome();
	    else if (arg.equals(lang.getText("print"))) 
		helpView.print();
	    else if (arg.equals(lang.getText("quit"))) 
		quit();
	    else if (arg.equals(lang.getText("addBookmarks"))) 
		addBookMarks();
	    else helpView.updatePage (PageBookMarks.getInstance().getBookMark((JMenuItem)e.getSource()), true);
	    }
	*/
    }

    /**
     */
    public void hyperlinkUpdate(HyperlinkEvent ev)
    {
	if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	    pos = -1;
	    display(ev.getURL());
	}
    }

    /**
     */
    public void display(URL url)
    {
	String urlKey = url.toString();

	// use external browser for this (all) links?
	if (ChatFrame.browseExternal || "mailto".equals(url.getProtocol())) {
	    BrowserControl.displayURL(url.toString());
	    return;
	}


	if (pos == -1) {
	    if (history.size() > 100)
		for (int i = 0; i < 10; i++) history.remove(0);

	    history.add(url);
	    pos = history.size()-1;
	}

	String content = (String) cache.get(urlKey);

	try {
	    if (content != null)
		text.read(new StringReader(content), null);

	    else {
		text.setPage(url);

		if (caching) {
		    System.out.println("WebTextPane.cache: " + text.getText());
		    cache.put(urlKey, text.getText());
		    cacheIndex.add(urlKey);
		}
	    }
	} catch (Exception e) {
	    System.out.println("WebTextPane.display: " + e);
	    cache.remove(urlKey);
	}
    }
}
