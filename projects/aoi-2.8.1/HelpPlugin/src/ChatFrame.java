/*  ChatFrame.java  */

package nik777.chat;

/*
 * ChatFrame: GUI frame for sending/displaying chat messages
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

/*
 *  This is a derivative work. The GUI design and base logic is based on
 *  org.jabber.applet.awt.MsgChat, portions of which are copyrighted by 
 *  the Jabber foundation as per the included notice below:
 *
 * Portions created by or assigned to Jabber.com, Inc. are
 * Copyright (c) 1999-2000 Jabber.com, Inc.  All Rights Reserved.  Contact
 * information for Jabber.com, Inc. is available at http://www.jabber.com/.
 * 
 */

import java.awt.*;
import java.awt.event.*;            // needed for focus manipulation
import java.util.Date;
import java.util.Calendar;          // used to set time of chat start
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Enumeration;

import java.net.URL;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTabbedPane;
import javax.swing.Box;
import javax.swing.BoxLayout;

import java.awt.Checkbox;

import javax.swing.ImageIcon;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

import javax.swing.text.*;

// NTJ: debug
import java.io.UnsupportedEncodingException;

import net.sourceforge.helpgui.util.BrowserControl;

import artofillusion.ui.Translate;

/**
 * Chat Window class.
 *
 * The UI consists of 
 * a) a label at the top of the window indicating person we're chatting with
 * b) a text area displaying history of the chat
 * c) a text field for user input to the chat session
 * d) a send button
 * e) an invite button
 * f) a record button
 */

public class ChatFrame extends Frame 
	implements ActionListener, FocusListener, KeyListener, WindowListener,
			   HyperlinkListener, Chat.Connector
{
    public static String iconsPath = "crystal";

    /** chat connection for sending/receiving messages */
    public Chat.Connector chat;
	protected int chatid = -1;

	/**  semaphore for notification of closing  */
	protected Object semaphore = null;

    /**
     * top panel used to display 'chatn with..'
     */
    private MyPanel pnlHeader = new MyPanel();
    private Label lblUsername = new Label("");  

    /**
     * chat history display area
     */
    //private ChatTextArea txtChatHistory;

	/**
	 * tabbed pane for displaying chat histories
	 */
	private JTabbedPane tabs;

	/**
	 *  icons for tabs
	 */
	private ImageIcon icoChat, icoBrowse, icoSettings;

	/**
	 *  check boxes
	 */
	private Checkbox browseBox, tabBox;

	/**
	 *  list of users in chat room
	 */
	private List userList;

    /**
     * new msg input area
     */
    private TextField txtSendMsg;
    
    /**
     * current state of window is iconified
     */
    private boolean bIconified = false;

    /**
     * Number of messages received while chat window has been iconified
     */
    private int nMsgCount = 0;
    
    /**
     * String containing my username
     */
    private String me;

	/** list of chat areas (one per conversation) */
	private ArrayList session;

    /**
     * date/time objects to track timestamps
     */
    private Date now = new Date();
    private long lastMsg = now.getTime();
    private DateFormat inFmt = new SimpleDateFormat("yyyyMMdd'T'hh:mm:ss z");
    private DateFormat outFmt =
	DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);

    private long maxIdle = 1800000;		 // 30 minutes in milliseconds

    /**
     * User we're chatting with
     */
    public String you;
    
    /**
     * if True we will save the chat history to be displayed later
     */
    public boolean doRecord = false;
        
    /**
     * Max capacity of String buffer for saving chat history.
     */
    protected static int SB_MAX_CAPACITY = 10000;

    /**
     * Newline for this platform.
     */
    protected static String NEWLINE = System.getProperty("line.separator");

    /**
     * String buffer for saving chat history.
     */
    private StringBuffer historyBuff = new StringBuffer( SB_MAX_CAPACITY );

    /**
     * Used to covert HTML to standard ASCII (for ' etc.)
     */
    //public StringCleanup moCleanup = new StringCleanup();
    
    /**
     * Background color of clients applet
     */
    public Color clientColor;     

    /**
     * Font of clients applet
     */
    public Font clientFont;
        
    /**
     * Background color of clients applet
     */
    private Hashtable mhashNameColors = new Hashtable();       

    /**
     * Label of the button with focus
     */
	private String				msButtonWithFocus		= "";

    /**
     * This is true when you start typing a reply and the other person is notified that you started typing
     */	
	//private boolean				mbNotifiedStartingReply = false;
    
    private boolean             bGroupChat = false;

	protected static boolean				browseExternal = false;

    //Colors choosen from http://www.geocities.com/~annabella/colornames.html
    private UniqueColors clr = new UniqueColors();

    private final static String         COLOR_MSG            = "000000";
    private final static String         COLOR_ACTION         = "8B008B";
    private final static String         COLOR_STATUS         = "2E8B57";

    private Vector mvChatHistory = new Vector(200, 50);

	protected String SAYS = " says";

    /**
    * Assign an unique color for everyone in the Chat room.
    */
	private String nameColor(String name){
	    String sColor = (String) mhashNameColors.get(name);
	    if (sColor == null){
			//name not found in the hash table, so add it
			sColor = clr.getNextRGB();
			mhashNameColors.put(name, new String(sColor));
	    }
	    return sColor;
	}
	
    /**
    * The MyPanel class is used so we can put some graphics elements
    * in a panel.
    */
    class MyPanel extends Panel
    {
        /**
         * paint method is used to display the top panel with some
         * basic graphics.  A label is displayed (with users name),
         * that's created in the window constructor.
         * @param g graphics handle for the object to be written on
         */
        public void paint(Graphics g)
        {
	    String title = Translate.text("HelpPlugin:channel");

            g.setColor(clientColor);        // same as chats background
                                            // draw rect around top panel
            g.draw3DRect(2,4,this.getBounds().width-4,this.getBounds().height-8, false);
                                            // compute size of 'Chat with'
            FontMetrics fnt = g.getFontMetrics();
            int nWidth = fnt.stringWidth(title);
            int nHeight = fnt.getDescent();
            g.setColor(clientColor);        // erase part of outline
            g.fillRect(5, 0, nWidth + 5, 18);   // display background for 'Chat with'

            g.setColor(Color.black);        // display 'Chat with'
            g.drawString(title, 8, 10);
        }                                   // end, paint routine
    }                                       // end, MyPanel class

	Button btnRecord = null;
	
    /**
     * Main chat constructor (doens't include an invite button)
     * 
     * @param itsMe owners name
     * @param you person chatting with itsMe/owner
     * @param clientColor Client window color
     * @param clientFont Client window font
     */
    public ChatFrame(Color clientColor, Font clientFont, String itsMe, String idyou, boolean bHeader)
    {
 		this (clientColor, clientFont, itsMe, idyou, bHeader, false);
	}
	
    /**
     * Main chat constructor (specify whether you want an invite button or not)
     * @param itsMe owners name
     * @param you person chatting with itsMe/owner
     * @param clientColor Client window color
     * @param clientFont Client window font
     * @param inviteButton if true, display the invite button
     */	
    public ChatFrame(Color clientColor, Font clientFont, String itsMe, String idYou, boolean bHeader, boolean inviteButton)
    {
        super();
        // get instance of Applet so we can call methods in this class from classes
        // defined in this file.
        //app = (JabberApplet) JabberApplet.getAppletInstance();
        //me = new JID(itsMe, idYou.getServer());                     // keep copy of my username

		//SAYS = " " + Translate.text("says");

        you = idYou;                 // keep copy of person we're chatting with
        this.clientColor = clientColor;
        this.setBackground(clientColor);
        this.clientFont = clientFont;
        this.setFont(clientFont);
        setBackground(new java.awt.Color(255,255,255));

        if (bHeader)
        {
            // Header information
            lblUsername.setAlignment(Label.CENTER);
            lblUsername.setText("          " + you + "          "); // set chat to name in header
																			 // pad with space so that you can see " is replying..."
            pnlHeader.add(lblUsername);             // reserve some space
            pnlHeader.setBackground(clientColor);
        }

		// get the icons
		icoChat  = new ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+iconsPath+"/chat.png"));
		icoBrowse  = new ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+iconsPath+"/web-browser.png"));
		icoSettings  = new ImageIcon(getClass().getResource("/net/sourceforge/helpgui/icons/"+iconsPath+"/settings.png"));


		// settings pane
		Box settingsPane = new Box(BoxLayout.X_AXIS);
		Box col1 = new Box(BoxLayout.Y_AXIS);
		Box col2 = new Box(BoxLayout.Y_AXIS);

		settingsPane.add(col1);
		settingsPane.add(col2);

		col1.add(new Label("open links in"));
		col2.add((browseBox = new Checkbox("browser", null, false)));

		col1.add(new Label("create new browser"));
		col2.add((tabBox = new Checkbox("tab", null, false)));

        // Chat history
        //ChatTextArea txtChatHistory = new ChatTextArea();
        ChatTextPane txtChatHistory = new ChatTextPane();
		txtChatHistory.addHyperlinkListener(this);

        txtChatHistory.setFont(clientFont);
		userList = new List();
		  //        String sTime = "Time: " + Calendar.getInstance().getTime();
		  String sTime = "On: " + outFmt.format(now);
        txtChatHistory.append(sTime, COLOR_STATUS);
        txtChatHistory.addFocusListener(this); // workaround to pass focus to the send field

		if (session == null) session = new ArrayList(16);
		session.add(txtChatHistory);

        chatHistory(sTime, COLOR_STATUS);

        // Send Msg field and button
        txtSendMsg = new TextField();
        txtSendMsg.addActionListener(this);    // send and add to history whenever <enter>
		txtSendMsg.addKeyListener(this);
		txtSendMsg.addFocusListener(this);
		
        //Button btnSend = new Button(Config.SEND);   // send button
		Button btnSend = new Button(Translate.text("Send"));   // send button
        btnSend.addActionListener(this);
        //btnSend.addKeyListener(this);
        btnSend.addFocusListener(this);


		// invite button
		Button btnInvite = new Button(Translate.text("Invite"));
		btnInvite.addActionListener(this);
		btnInvite.addFocusListener(this);
		
		// Record chat history button
        btnRecord = new Button(Translate.text("Log"));
        btnRecord.addActionListener(this);
		btnRecord.addFocusListener(this);
		
        //----------------------------------
        // Screen layout setup
        //----------------------------------
        int x = 0;
        int y = 0;
        setLayout(new GridBagLayout());

        //----------------------------------
        // add header
        //----------------------------------
        GridBagConstraints gbConstraints=new GridBagConstraints();
        gbConstraints.fill=GridBagConstraints.HORIZONTAL;
        gbConstraints.gridx = x;         // x location
        gbConstraints.gridy = y;         // y location
        
        bGroupChat = inviteButton;
        if (inviteButton)
			gbConstraints.gridwidth = 4;     // width		
		else
			gbConstraints.gridwidth = 3;     // width
			
        gbConstraints.gridheight = 1;    // height
        gbConstraints.ipadx = 0;         // no xtra padding
        gbConstraints.ipady = 0;         // no xtra padding
        gbConstraints.weightx = 1.0;     // gets no extra x when avail
        gbConstraints.weighty = 0.0;     // gets no extra y when avail
        
        if (bHeader)
            add (pnlHeader,gbConstraints);


        //----------------------------------
        // add text history
        //----------------------------------
        y++;

        gbConstraints.gridx = x;         // x location
        gbConstraints.gridy = y;         // y location
		gbConstraints.gridwidth -= 2;     // width
        gbConstraints.weightx = 1.0;     // gets xtra x when avail
        gbConstraints.weighty = 1.0;     // gets xtra y when avail
        gbConstraints.fill=GridBagConstraints.BOTH;

        //add (txtChatHistory,gbConstraints);
		tabs = new JTabbedPane();
        add(tabs, gbConstraints);

		// NTJ: disable settings TAB, and use dialog instead
		//tabs.addTab(null, icoSettings, settingsPane);
		tabs.addTab(null, icoChat, new WebTextPane(txtChatHistory));

		//tabs.setSelectedIndex(1);

		//----------------------------------
		// add user list
		//----------------------------------
		x += gbConstraints.gridwidth;
        //gbConstraints.gridx = gbConstraints.gridwidth-1;         // x location
        gbConstraints.gridx = x;         // x location
        gbConstraints.gridy = y;         // y location
		gbConstraints.gridwidth = 2;     // width
        gbConstraints.weightx = 0.0;     // gets xtra x when avail
        gbConstraints.weighty = 1.0;     // gets xtra y when avail
        gbConstraints.fill=GridBagConstraints.BOTH;
		add(userList, gbConstraints);

        gbConstraints.weightx = 0.0;     // gets no extra x when avail
        gbConstraints.weighty = 0.0;     // gets no extra y when avail

        //----------------------------------
        // add send field and button
        //----------------------------------
		x=0;
        y++;
        gbConstraints.gridx = x;         // x location
        gbConstraints.gridy = y;         // y location
        gbConstraints.gridwidth = 1;     // width
        gbConstraints.fill=GridBagConstraints.HORIZONTAL;
        gbConstraints.weightx = 1.0;     // gets no extra x when avail
        add (txtSendMsg,gbConstraints);

        x++;
        gbConstraints.gridx = x;         // x location
        gbConstraints.weightx = 0.0;     // gets no extra x when avail
        gbConstraints.gridwidth = 1;     // width
        gbConstraints.fill=GridBagConstraints.NONE;
        add (btnSend, gbConstraints);


		if (inviteButton)
		{
			x++;
			gbConstraints.gridx = x;         // x location
			gbConstraints.weightx = 0.0;     // gets no extra x when avail
			gbConstraints.fill=GridBagConstraints.NONE;
			add (btnInvite, gbConstraints);
		}

        x++;
        gbConstraints.gridx = x;         // x location
        gbConstraints.weightx = 0.0;     // gets no extra x when avail
        gbConstraints.fill=GridBagConstraints.NONE;
        add (btnRecord, gbConstraints);
		
        //SymWindow aSymWindow = new SymWindow();
        addWindowListener(this);
        SymComponent aSymComponent = new SymComponent();
        this.addComponentListener(aSymComponent);
        
        // need to pack in the constructor for linux.
        pack();
    }

	public Dimension getPreferredSize()
	{
	    return new Dimension(600, 450);
	}

	public void semaphore(Object semaphore)
	{ this.semaphore = semaphore; }


	//---------- Chat.Connector implementation ---------------
	/**
	 *  associate with a corresponding Connector
	 */
	public void open(Chat.Connector chat)
	{
		this.chat = chat;
		chat.open(this);
	}

	/**
	 *  disassociate from a connector
	 */
	public void close(Chat.Connector chat)
	{
		if (chat != null && chatid >= 0) chat.leave(chatid);
		this.chat = null;
	}

	/**
	 *  join the identified chat (channel, server, room, etc)
	 */
	public int join(String conn, String name, String password)
	{
		me = name;
		chatid = chat.join(conn, name, password);

		if (tabs != null) tabs.setToolTipTextAt(0, conn);

		return chatid;
	}

	/**
	 *  leave the identified chat *channel, server, room, etc)
	 */
	public void leave(int id)
	{ chat.leave(chatid); }

	/**
	 * add one or more users to the connection
	 */
	public void add(String conn, String name, String detials)
	{
		userList.add(name);
		userList.invalidate();
	}

	/*
	 *
	 */
	public void remove(String conn, String name)
	{
		userList.remove(name);
		userList.invalidate();
	}


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
	{ msgReceived(id, from, msg); }


	//---------- HyperlinkListener Implementation ------------

	/**
	 *  hyperlink clicked
	 */
	public void hyperlinkUpdate(HyperlinkEvent ev)
	{
		if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

			URL url = ev.getURL();

			// use external browser for this (all) links?
			if (browseExternal || "mailto".equals(url.getProtocol())) {
				BrowserControl.displayURL(url.toString());
				return;
			}

			WebTextPane text = new WebTextPane(url);

			tabs.addTab(null, icoBrowse, text, url.toString());
			tabs.setSelectedComponent(text);
		}
	}

	//---------- KeyListener Implementation ------------------
	
    /**
     * keyTyped
     */	
	public void keyTyped(KeyEvent evt) { }
	
    /**
     * keyPressed
     */	
	public void keyPressed(KeyEvent evt) 
	{
		//[Enter] hit, see if a button has focus, if it does
		//click it, otherwise click the default Send button
        if( evt.getKeyCode() != KeyEvent.VK_DELETE 
            && evt.getKeyCode() != KeyEvent.VK_ALT 
            && evt.getKeyCode() != KeyEvent.VK_CONTROL 
            && evt.isAltDown() && evt.isControlDown() )
        {
            //JabberApplet.getAppletInstance().toggleDebug();
        }

		if (evt.getKeyCode() == KeyEvent.VK_TAB) {
			
		}

		if (evt.getKeyCode() == KeyEvent.VK_ENTER ) 
		{
			if (msButtonWithFocus.length() > 0)
			{
				if (msButtonWithFocus.compareTo("Send") == 0)
				{
					msgSend();
				} 
				else if (msButtonWithFocus.compareTo("Invite") == 0)
				{
				    invite();
				} 
				else 
				{ //Button caption can be either Record or Stop Rec
					record(msButtonWithFocus);
				}
			}
			else
			{
				msgSend();
			}
		} 
		/*
		else 
		{	//TODO verify that this isn't being called when non alpha-numeric keys are pressed
			if (! mbNotifiedStartingReply) 
			{
			    if (bGroupChat == false)  // don't send when in groupchat
				    app.replyStarted(JIDYou);
				mbNotifiedStartingReply = true;
			}
		}
		*/
	}
	
    /**
     * keyReleased
     */	
	public void keyReleased(KeyEvent evt) 
	{ }
    
    /**
     * Button triggered.
     * @param ae Action event (not used)
     */
    public void actionPerformed(ActionEvent ae)
    {
		if (ae.getActionCommand().compareTo("Send") == 0)
		{
			msgSend();
		} 
		else if(ae.getActionCommand().compareTo("Invite") == 0)
		{
			invite();
		} 
		else 
		{ //Button caption can be either Record or Stop Rec
			record(ae.getActionCommand());
		}
    }

    /**
     * Display a textArea for the user to cut/paste from.
     */
    public void displayChatHistory(  )
    {
        TextArea ta = new TextArea(chatHistoryToString());
        final Dialog dialog = 
            new Dialog(this,"CHAT HISTORY: please cut and paste the message to a text document.", true);
        dialog.add(ta);
        dialog.addWindowListener( new WindowAdapter()
        {
            public void windowClosing( WindowEvent we )
            {
                dialog.dispose();
            }
        } );
        dialog.pack();
        dialog.show();
    }

  /**
	* msg received with no timestamp
	*/
    public void msgReceived(int sessn, String id, String sResponse)
    { msgReceived(sessn, id, sResponse, null); }

    /**
     * New groupchat msgReceived needs jid input since multiple users are shown in the same 
     * chat window.
     */
    public void msgReceived(int sessn, String id, String sResponse,
							String sTimestamp)
    {
		System.out.println("msgReceived: " + id + "; " + sResponse);

        //---------------------------------------------------
        // If chat windows iconified update the msg counter
        // and update icon displayed
        //---------------------------------------------------
        if (bIconified)                 // window iconified?
            nMsgCount++;                // update msg count
        else
            nMsgCount = 0;              // window showing, clear msg count

        updateCount(nMsgCount);    

		//ChatTextArea txtChatHistory;
		ChatTextPane txtChatHistory;

		if (sessn >= 0 && sessn < session.size())
			txtChatHistory= (ChatTextPane) session.get(sessn);
		else {
			System.out.println();
			txtChatHistory = (ChatTextPane) session.get(0);
		}

        if (id == null || id.length() == 0)  //groupchat
        {
            txtChatHistory.append(sResponse, COLOR_STATUS);
            chatHistory(sResponse, COLOR_STATUS);
        }
        else 
        {
			if (sResponse.startsWith("/me"))
			{	//Action Message
				//Remove "/me"
				//sResponse = moCleanup.Cleanup(sResponse.substring(3));
				sResponse = sResponse.substring(3);
				txtChatHistory.appendChatText("* ", COLOR_ACTION, id + " " + sResponse, COLOR_ACTION);
				chatHistory("*", COLOR_ACTION, id + " " + sResponse, COLOR_ACTION);
			}
			else
			{
			  // do we have a message timestamp...
			  if (sTimestamp != null && sTimestamp.length() > 0) {

				 try {

					// parse the timestamp in the GMT timezone
					now = inFmt.parse(sTimestamp + " GMT");
				 } catch (ParseException e) {
					System.out.println("timestamp parse error: " + e);
				 }
			  }
			  else {
				 now.setTime(System.currentTimeMillis());

				 System.out.println("now = " + now.toString());
			  }

			  System.out.println("now - lastMsg = " +
										Math.abs(now.getTime() - lastMsg));

					if (Math.abs(now.getTime() - lastMsg) > maxIdle) {
					  systemMsg("Sent: " + outFmt.format(now));
					}

					// remember the time of the last message
					lastMsg = now.getTime();

					//sResponse = moCleanup.Cleanup(sResponse);	
					txtChatHistory.appendChatText(id + SAYS, nameColor(id), sResponse, COLOR_MSG);
					chatHistory(id + SAYS, nameColor(id), sResponse, COLOR_MSG);
			}
        }
        if (!bGroupChat)
			lblUsername.setText(you);
    }

    /**
     * Display system msg
     */
    public void systemMsg(String sMsg)
    {
		for (int i = 0; i < session.size(); i++) {
			((ChatTextPane) session.get(i)).append(sMsg, COLOR_STATUS);
		}

		chatHistory(sMsg, COLOR_STATUS);
    }

    /**
     * Send indication user has started replying
     */
	public void msgReplyStarted(String id, String msg)
	{
		lblUsername.setText(you + " " + msg);
	}
	   
    /**
     * Change status of person we're chatting with.
     * @param sStatus New status of user we're chatting with.
     */
    public void changeStatus(String id, String sStatus)
    {
		/*
        if (id.length() == 0)  //groupchat
        {
            txtChatHistory.append(sStatus, COLOR_STATUS);
			chatHistory(sStatus, COLOR_STATUS);
        }
        else 
        {
            txtChatHistory.append(id + " has changed status to " + sStatus, COLOR_STATUS);
            chatHistory(id + " has changed status to " + sStatus, COLOR_STATUS);
        }
		*/
    }
   
    /**
     * Pop current window to top.  Used when corresponding roster list launches 
     * a chat that's already active (and user forgot about the active window).
     */
    protected void popTop()
    {
        this.toFront();
    }
    
    /**
     * FocusListener requires focusGained and focusLost
     * for reasons unbenownst to me requestFocus method
     * isn't working at startup, therefore when window
     * boots focus defaults to 'readonly' txtChatHistory.
     * Need to force it to txtSendMsg
     * @param fe not used
     */
    public void focusGained(FocusEvent fe)
    {
        if( fe.getComponent() instanceof Button )
        {
			//Save the text of the button with the focus
            msButtonWithFocus = ((Button)fe.getComponent()).getLabel();
        }
        else
        {
			// don't let focus stay on txtChatHistory
			// (only object with focus listener enabled)
			//txtChatHistory.transferFocus();
		}

		txtSendMsg.requestFocus();
    }

    /**
     * focus lost not used, just here to fill requirements of
     * abstract class.
     * @param fe not used
     */
    public void focusLost(FocusEvent fe)
    {
		msButtonWithFocus = "";
    }

    // Implement WindowListener
    /**
     * windowActivated - do nothing.
     * windowClosed - do nothing.
     * windowDeactivated - do nothing.
     * windowOpened - do nothing.
     */
    public void windowActivated(java.awt.event.WindowEvent event){}
    public void windowDeactivated(java.awt.event.WindowEvent event){}
    public void windowOpened(java.awt.event.WindowEvent event){}
    public void windowClosed(java.awt.event.WindowEvent event)
    {
	//leave(chatid);
	close(chat);
    }

    /**
     * windowClosing. Let the applet know and dispose of self.
     * @param event the closing event
     */
    public void windowClosing(java.awt.event.WindowEvent event)
    {
		System.out.println("chat window closing...");
		//leave(chatid);
		close(chat);
        setVisible(false);
        dispose();

		if (semaphore != null) {
			synchronized (semaphore) {
				semaphore.notifyAll();
			}
		}
    }

    /**
     * Chat window state changed to deiconified
     * @param event Deiconify event
     */
    public void windowDeiconified(java.awt.event.WindowEvent event)
    {
        Object object = event.getSource();
        if (object == ChatFrame.this)
        {
          ChatFrame_WindowDeiconified(event);
        }
    }

    /**
     * Chat window state changed to iconified
     * 
     * @param event Window iconified event
     */
    public void windowIconified(java.awt.event.WindowEvent event)
    {
        Object object = event.getSource();
        if (object == ChatFrame.this)
        {
            ChatFrame_WindowIconified(event);
        }
    }

    /**
     * Send message triggered.
     * Take from the send message field and add to chat history
     * and also send it to the antenna app to be sent on to server.
     */
    private void msgSend()
    {
        if (txtSendMsg.getText().length() > 0)
        {
			System.out.println("ChatFrame: atBottom = true");
			((ChatTextPane) session.get(0)).isAtBottom(true);

            String msg = txtSendMsg.getText();
            txtSendMsg.setText("");
            msgReceived(-1, me, msg);

				/*
				// NTJ: DEBUG
				try {
				  byte[] bytes = msg.getBytes("UTF8");
				  if (bytes.length != msg.length()) {
					 msgReceived(JIDMe, "Multi-byte chars detected: in=" +
									 msg.length() + "; out=" + bytes.length);
				  }
				  else {
					 msgReceived(JIDMe, "No multi-byte chars");
				  }
				}
				catch (java.io.UnsupportedEncodingException e) {
				  msgReceived(JIDMe, "Unsupported char encoding" + e);
				}
				*/

            //chat.accept(chatid, me, you, moCleanup.Cleanup(msg, false));
			chat.accept(chatid, me, you, msg);
        }
        txtSendMsg.requestFocus();     // keep focus in text input area
        nMsgCount = 0;
        updateCount(nMsgCount);    
        //mbNotifiedStartingReply = false;
    }

    /**
     * Invite button has been selected, display list of users to invite
     */
	public void inviteeSelected(String sXML)
	{
	    /*
		MiniXMLParser oParser = new MiniXMLParser();
		String sGroupChat = oParser.parseTag(sXML, "GroupChat");
		String sReason = oParser.parseTag(sXML, "Reason");
		String sInvitee = oParser.parseTag(sXML, "Invitee");
        int indexOfColon = sInvitee.indexOf(":");
        if( indexOfColon < 0 )
        {
	    //app.inviteUser(sGroupChat, sInvitee, sReason);
        }
        else
        {
            int i2 = 0;
            while( indexOfColon > -1 )
            {
                String invitee = sInvitee.substring(i2,indexOfColon);
		//app.inviteUser(sGroupChat, invitee, sReason);
                i2 = indexOfColon+1;
                indexOfColon = sInvitee.indexOf(":",i2);
            }
            // invite the last one, after the last colon.
            String invitee = sInvitee.substring(i2);
	    //  app.inviteUser(sGroupChat, invitee, sReason);
        }
	    */
	}
	
    /**
     * Invite users to groupchat
     */
	private void invite()
	{
	    /*
		InputDialog oInput = new InputDialog(this, clientColor, clientFont, "Invite to Chat");
        oInput.addHidden("GroupChat", you.getUsername());
        oInput.addQuestion("Reason for Invitation:", "Reason", "Join us in " +  you.getUsername());
        oInput.addList("Invite:", "Invitee", app.rosterListToXML(), 5,true);        
        oInput.addButton("OK", "inviteeSelected");	// Call inviteeSelected when OK clicked
        oInput.addButton("Cancel", "");				// When cancelled, no need to call anything
        oInput.inputbox();
	    */
	}
    /**
     * Record/Stop button pressed
     */
	private void record(String actionCommand)
	{
		if( actionCommand.equals(" Record ") )
		{
			doRecord = true;
			btnRecord.setLabel("Stop Rec");
            updateCount(nMsgCount);
		} 
		else if( actionCommand.equals("Stop Rec") )
		{
			btnRecord.setLabel(" Record ");
			doRecord = false;
            updateCount(nMsgCount);
			displayChatHistory();
		}	
	}
	    
    /**
     * Window iconified, set corresponding state variable
     * @param event window iconifiy event
     */
    private void ChatFrame_WindowIconified(java.awt.event.WindowEvent event)
    {
        bIconified = true;
        updateCount(nMsgCount);    
    }

    /**
     * Update non-seen message count
     */
    private void updateCount(int n)
    {
        String title = "";
        if (n == 0)
            title = you;
        else
            title = "(" + n + ") " + you;

        if( doRecord )
        {
            title += ", Recording...";
        }
        this.setTitle(title);
    }

    /**
     * Window deiconified, set window state variablle
     * @param event window deiconified event
     */
    private void ChatFrame_WindowDeiconified(java.awt.event.WindowEvent event)
    {
        bIconified = false;
        nMsgCount = 0;
        updateCount(nMsgCount);    
		txtSendMsg.requestFocus();
    }
    
    class SymComponent extends java.awt.event.ComponentAdapter
    {
        public void componentResized(java.awt.event.ComponentEvent event)
        {
            Object object = event.getSource();
            if (object == ChatFrame.this)
                ChatFrame_ComponentResized(event);
        }
    }

    /**
     * Window Resized, insure we're not too small
     * @param event Window resize event
     */
    public void ChatFrame_ComponentResized(java.awt.event.ComponentEvent event)
    {
        if (this.getSize().width < 200)
        {
            this.setSize(200, this.getSize().height);
        }
        if (this.getSize().height < 200)
        {
            this.setSize(this.getSize().width, 200);
        }
    }

    /**
     * Add a new status line to the Chat History
     * @param sMsg			What the person said
     * @param sMsgColor		Color to use for the message
     */	    
    private void chatHistory(String sMsg, String sMsgColor){
		chatHistory("", "", sMsg, sMsgColor);
	}
    
    /**
     * Add a new chat line to the Chat History
     * @param sName			Name of the person who said the message
     * @param sNameColor	Color to use for the name "rrggbb" format
     * @param sMsg			What the person said
     * @param sMsgColor		Color to use for the message
     */	    
    private void chatHistory(String sName, String sNameColor, String sMsg, String sMsgColor){
		if (!doRecord)
        {
			return;
        }
			
        // Check the capacity of the buffer to hold this line.
        // If it is not enough, launch the textArea to clear
        // up the buffer.
        int nameLen = 0;
        int msgLen = 0;
        if( sName != null )
            nameLen += sName.length();
        if( sMsg != null )
            msgLen += sMsg.length();
        if( (nameLen+msgLen+20+historyBuff.length()) > historyBuff.capacity() )
            displayChatHistory();

		//Don't indent status or action messages
		if ( sName.length() > 0 )
		{
			historyBuff.append(sName);
			// indent up to 20 total characters.
			int pad = 20 - nameLen;
			for( int i = 0; i < pad; i++ ){
			    historyBuff.append(" ");
			}
		}
        historyBuff.append(sMsg+NEWLINE);
    }

    /**
     * Construct a properly formed HTML page with a table containing the Chat History
     * @return HTML version of Chat History
     */	
	private String chatHistoryToString(){
        String s = historyBuff.toString();
		historyBuff = new StringBuffer(SB_MAX_CAPACITY);
		return s;
	}


}
