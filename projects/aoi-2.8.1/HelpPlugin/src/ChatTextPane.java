/*  ChatTextPane.java  */

package nik777.chat;

/*
 * ChatTextPane: text area widget to display chat history in
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
 *  org.jabber.applet.awt.ChatTextArea, portions of which are copyrighted by 
 *  the Jabber foundation as per the included notice below:
 *
 * Portions created by or assigned to Jabber.com, Inc. are
 * Copyright (c) 1999-2000 Jabber.com, Inc.  All Rights Reserved.  Contact
 * information for Jabber.com, Inc. is available at http://www.jabber.com/.
 * 
 */

import java.awt.*;
import java.util.*;
import java.awt.event.*;

import javax.swing.SwingUtilities;
import javax.swing.JTextPane;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.io.StringReader;
import java.io.Reader;
import java.io.IOException;

import nik777.xlate.StringEditor;

/**
 * MULTIPLE COLOR CHAT TEXT AREA
 *
 * This AWT TextArea like multi-color control is designed to be used as a Chat Window
 * that automatically aligns chat messages in a different column from the user name.  This
 * control allows you to to use a different color for the users name and message.
 *
 * This control can also act TextArea with different color fonts for each line as 
 * well as a as a normal TextArea.
 *
 * @author	Jonathan Paulson
 * @version 	$ 1.1.2 $
 */
 
public class ChatTextPane extends JTextPane
	implements ChangeListener, MouseWheelListener
{
    private final static int        NAME				=  0;
    private final static int        NAMECOLOR			=  1;
    private final static int        MSG					=  2;
    private final static int        MSGCOLOR			=  3;

	/*
	 *  NTJ - disable auto-images until image display sidget is complete
	 */

	// define the editing rules for incoming text, before display
	protected static final String urlEdit = 
	    "gs/</&lt;/gs/>/&gt;/" +
	    /*"gs;(^| )(http://.*?\\.(jpg|png|gif))( |$);$1<img src=\"$2\"/>$4;" +*/
	    "gs;(^| )(http://.*?)( |$);$1<a href=\"$2\">$2</a>$3;" /* +
		"gs;(^| )image://(.*?)( |$);$1<img src=\"http://$2\"/>$3;" */;

    private int						miRows;						// number of rows in the text area
    private int						miColumns;					// number of rows in the text area
	private int						miNbrLinesPerPage;			// how many lines of text can be seen on the control
	private int						miWidth, miHeight;			// size, minus the scrollbar
	private int						miNameColWidth;				// the width to use for the Name Column
	private int						miTopLine			= 0;	// what the index of the top line of text is
	private int						miFontHeight;				// font height
	private boolean					mbReformatLines		= true;	// If True, vector of lines will be reformatted
	private boolean					mbHangingIndent		= true;	// If True, Chat messages will wrap with hanging indent
	private Image					mImage;						// backing image
	private Graphics				mGraphics;					// backing graphics
	private Font					mFont;
	private FontMetrics				mFontMetrics;				// drawing font size
	private Scrollbar				mScrollbar;					// scrollbar at the right side
	private Vector					mvLines;					// Unformatted lines as they were received
	private Vector					mvFormatedLines;			// Viewable lines (word wrapped where necessary)

	// stringbuffer for formatting
	protected StringBuffer		fmtBuffer;

	// text pane
	protected JTextPane textPanel;

	// containing JScrollPane
	protected JScrollBar scroll = null;
	protected boolean scrollSet = false;

	// for content editing
	protected StringEditor editor;

	protected boolean wheelScrolling = false;
	protected boolean isAtBottom = true;

    /**
     * Constructs a new Color text area.
     */
    public ChatTextPane() {
		this("", 0, 0, false);
    }
    
    /**
     * Constructs a new Color text area with the specified text.
     *
     * @param     text the text to be displayed. 
     */
    public ChatTextPane(String text) {
		this(text, 0, 0, false);
    }
    		
    /**
     * Constructs a new Color text area. 
     * This text area is created a vertical scroll bar.
     *
     * @param rows				the number of rows
     * @param columns			the number of columns 
	 * @param hangingIndent		true if you want the chat messages to having a hanging indent
     */
    public ChatTextPane(int rows, int cols, boolean hangingIndent)	{
		this("", rows, cols, hangingIndent);
	}
    	
    /**
     * Constructs a new text area with the specified text,
     * and with the specified number of rows and columns.
     * This text area is created with both vertical and
     * horizontal scroll bars.
     * @param     text			the text to be displayed.
     * @param     rows			the number of rows.
     * @param     columns		the number of columns.
	 * @param hangingIndent		true if you want the chat messages to having a hanging indent
     */	
    public ChatTextPane(String Text, int rows, int cols, boolean hangingIndent)
	{
		miRows = rows;
		miColumns = cols;
		mbHangingIndent = hangingIndent;

		editor = new StringEditor(urlEdit);

		//setLayout(new BorderLayout());
		setEditable(false);
		setContentType("text/html");

		//add(textPanel);
		//validate();

		//Default Font
		mFont = new Font("Dialog", Font.PLAIN, 12);

		//setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		mScrollbar = new Scrollbar(Scrollbar.VERTICAL, 0, 1, 0, 0);

		/*
		add("East",mScrollbar);
		mScrollbar.setValues(0, 1, 0, 100);
		mScrollbar.setUnitIncrement(10); 
		mScrollbar.setUnitIncrement(30); 
		*/

		mvLines = new Vector();
		mvFormatedLines = new Vector();
		fmtBuffer = new StringBuffer(4096);

		//repaint();

		//Catch scroll events
		/*
		mScrollbar.addAdjustmentListener(
			new AdjustmentListener() { 
				public void adjustmentValueChanged(AdjustmentEvent e) { 
					computeScrollbar(false);
					repaint(); 						
				}
			}
		);
		*/

		if (Text.length() > 0)
			append(Text);
	}	
    
    /**
	 * Appends the given text to the text area's current text (using black)
	 * 
	 * @param str		the text to append. 
	 */	
	public void append(String str){
		//Append plan black text
		appendChatText(str, "000000", "", "");
	}

    /**
	 * Appends the given text to the text area's current text using the color specified
	 * 
	 * @param str		the text to append. 
	 * @param RGB		the RGB color in HEX format (example "00FF77")
	 */		
	public void append(String str, String RGB){
		//Append colored text
		appendChatText(str, RGB, "", "");
	}

    /**
	 * Appends the given chat message to the text area's current text with the message in
	 * its own column using the color specified
	 * 
	 * @param name		the person who said the message 
	 * @param msg		what the person said 
	 * @param RGB		the RGB color in HEX format (example "00FF77")
	 *					to use for the name and msg
	 */		
	public void appendChatText(String name, String msg, String RGB){
		//Append colored text
		appendChatText(name, RGB, msg, RGB);
	}

		
    /**
	 * Appends the given chat message to the text area's current text with the message in
	 * its own column using the colors specified
	 * 
	 * @param name		the person who said the message 
	 * @param nameRGB	the RGB color in HEX format (example "00FF77") to use for name
	 * @param msg		what the person said 
	 * @param msgRGB	the RGB color in HEX format (example "00FF77") to use for msg
	 */	
	public void appendChatText(String name, String nameRGB, String msg, String msgRGB){
		String line[] = new String[4];

		line[NAME] = name;
		line[NAMECOLOR] = nameRGB;
		line[MSG] = msg;
		line[MSGCOLOR] = msgRGB;
		
		mvLines.addElement(line);

		Rectangle rect = getBounds();

		if (rect.width > 0){
			//Wrap the element just added
			//wordWrap(mvLines.size() - 1);

			//New text, so adjust scrollbar
			//computeScrollbar(true);
			//repaint();
			render();
		}
	}

    /**
     * Gets the text that is presented by this text component. 
     */		
	public String getText()	{
		StringBuffer sBuffer = new StringBuffer(1024);
		String[] saNextLine;
		for (int i = 0; i<mvLines.size(); i++) {
			saNextLine = (String[])mvLines.elementAt(i);
			sBuffer.append(saNextLine[NAME])
				.append(" ").append(saNextLine[MSG]).append("\n");
		}
		return sBuffer.toString();
    }
    
    /**
     * Clear the contents of the text area
     */		
	public void clearText()	{
		mvLines.removeAllElements();
		mvFormatedLines.removeAllElements();
		//repaint();
    }

	/**
	 * Paints this component. This method is called when the contents of the component should be painted 
	 * in response to the component first being shown or damage needing repair. The clip rectangle in the 
	 * Graphics parameter will be set to the area which needs to be painted. 
	 * 
	 * @param g			The graphics context to use for painting.
	 */
	/*
	public void paint(Graphics g) {
		super.paint(g);

		if (mImage == null) {
	
			mImage = createImage(miWidth, miHeight);
			mGraphics = mImage.getGraphics();
			mGraphics.setFont(mFont);
			mFontMetrics = mGraphics.getFontMetrics();
			miFontHeight = mFontMetrics.getHeight();
			miNbrLinesPerPage = miHeight / miFontHeight;
            //g.draw3DRect(2,4,this.getBounds().width-4,this.getBounds().height-8, false);
			render();

		}

		//g.drawImage(mImage, 0, 0, this);
	}
	*/

	/**
	 * The AWT calls the update method in response to a call to repaint.
	 */	
	/*
	public void update() {
		update(this.getGraphics());
	}
	*/
	/**
	 * The AWT calls the update method in response to a call to repaint. The appearance of the 
	 * component on the screen has not changed since the last call to update or paint. You can 
	 * assume that the background is not cleared. 
	 * 
	 * @param g			the specified context to use for updating.
	 */
	/*
	public void update(Graphics g) {
		if (mFontMetrics != null) {
			render();
			//paint(g);
		}
	}
	*/

    /**
     * Returns the number of rows in the text area.
     *
     * @return		the number of columns in this text area. 
     */
	/*
    public int getRows() {
		return miRows;
    }
	*/
    /**
     * Returns the number of columns in the text area.
     *
     * @return		the number of columns in this text area. 
     */
	/*
    public int getColumns() {
		return miColumns;
    }
	*/

	/**
	 * Determines the preferred size of a text area with the specified number of rows and columns. 
	 * 
     * @param rows		the number of rows
     * @param columns	the number of columns 
     * @return			the preferred dimensions required to display the text area with 
     *					the specified number of rows and columns. 
	 */
	/*
    public Dimension getPreferredSize(int rows, int cols) {
		return super.getPreferredSize();
    }
	*/
	/**
	 * Determines the preferred size of this text area.  
	 * 
     * @return			the preferred dimensions needed for this text area
	 */
	/*
    public Dimension getPreferredSize()	{
		return ((miRows > 0) && (miColumns > 0)) ? getPreferredSize(miRows, miColumns) : super.getPreferredSize();
    }
	*/

    /**
     * Returns the specified minimum size Dimensions of the text area.
     *
     * @param rows		the minimum row size
     * @param cols		the minimum column size
     *
	 * @return			A dimension object indicating this component's minimum size. 
     */
	/*
    public Dimension getMinimumSize(int rows, int cols)	{
		return super.getMinimumSize();
    }
	*/

    /**
     * Returns the minimum size Dimensions of the text area.
     *
	 * @return			A dimension object indicating this component's minimum size.      
     */
	/*
    public Dimension getMinimumSize(){
		return super.getMinimumSize();
    }
	*/

	/**
	 * Moves and resizes this component. The new location of the top-left corner is specified by x and y, 
	 * and the new size is specified by width and height. (fired when resized)
	 * 
	 * @param x			The new x-coordinate of this component.
	 * @param y			The new y-coordinate of this component.
	 * @param w			The new width of this component. 
	 * @param h			The new height of this component. 
	 */
	/*
	public void setBounds(int x, int y, int w, int h){
		if (w != miWidth || h != miHeight) {  
			miWidth = w; 
			miHeight = h; 

			//Recalculate the size of name column
			setNameColumnWidth(miColSize, mbColSizeIsPercent);

			// Force creation of a new backing image and re-painting
			mImage = null;
			mbReformatLines = true;
			repaint();
		}
		super.setBounds(x, y, w, h);
	}
	*/

    /**
	 * Sets the font of this component. 
	 * 
	 * @param str		The font to become this component's font. 
	 */		
	public void setFont(Font font){
		mFont = font;
		repaint();
	}

    /**
	 * Sets the text that is presented by this text component to be the specified text. (using black)
	 * 
	 * @param str		new text for this component. 
	 */	
	public synchronized void setText(String str){
		setText(str, "000000");
	}

    /**
	 * Sets the text that is presented by this text component to be the specified text. 
	 * using the specified color.
	 * 
	 * @param str		new text for this component. 
	 * @param RGB		the RGB color in HEX format (example "00FF77")	 
	 */		
	public synchronized void setText(String str, String RGB){
		clearText();
		if (str.length() > 0)
			append(str, RGB);
	}
	
	
    /**
	 * Sets the width for the first column generally used for the name
	 * 
	 * @param width			Width or Width Percent depending on value of isPercent
	 * @param isPercent		If true, then width indicates a percent of the total (1-95)
	 */		
	private int			miColSize			= 20;	// width last time setNameColumnWidth called 
	private boolean		mbColSizeIsPercent	= true;	// isPercent last time setNameColumnWidth called 
	public synchronized void setNameColumnWidth(int width, boolean isPercent){
		miColSize = width;
		mbColSizeIsPercent = isPercent;
		
		if (isPercent){
			if (width < 0 || width > 95){
				width = 20;
			}
		
			//Make the number a percent
			double dNameColWidthPercent = width * .01;
			
			//Now set exact width
			miNameColWidth = (int)(miWidth * dNameColWidthPercent);
		}
		else{
			if (width < 0 || width > miWidth){
				//Must be positive value, set to 1/3 width
				width = (int) miWidth / 3;
			}
			miNameColWidth = width;
		}
	}
			
    /**
	 * Automatically wrap the text that is too long to display on one line
	 * 
	 * @param lineIndex		the vector index of the line to wrap 
	 */			
	private void wordWrap(int lineIndex) {
		String[] saNewLine = (String[])mvLines.elementAt(lineIndex);

		boolean bChatMsg = saNewLine[MSG].length() > 0;		

		try	{
			//If this is a chat msg then the username is stored in [NAME] and the msg is stored in {MSG]
			//otherwise the contents are stored in [NAME] and [MSG] is left blank
			StringTokenizer		tokenizer;
			StringBuffer		buffer;
			String				sNextWord		= new String();
			String				sChopped		= new String();
			boolean				bAddUserName	= true;
			boolean				bWasChatMsg		= false;
			int					iBodyWidth;
			
			if (bChatMsg){
				if (mbHangingIndent){
					// chat msg width is fixed:  calculated by subtracting the name column width and 5% for scroll bar
					iBodyWidth = (int)((miWidth - miNameColWidth) - 50);
				}
				else {
					// text comes right after name, so figure out how much room we have to work on
					// since name is variable length
					iBodyWidth = miWidth - (50 + mFontMetrics.stringWidth(saNewLine[NAME]));				
				}
			}
			else {
				// No user name so make width of msg is 95% of width (5% for scrollbar)
				iBodyWidth = (int)(miWidth - 50);
			}

			//Go thru the words one by one and make sure each word will fit inside the TextArea
			//If a word is found that doesn't fit, use chopWord to dice the word up.
			tokenizer = new StringTokenizer(bChatMsg ? saNewLine[MSG] : saNewLine[NAME]);
			while (tokenizer.hasMoreTokens()) {
				sNextWord = tokenizer.nextToken().toString();
				if (mFontMetrics.stringWidth(sNextWord) > iBodyWidth)
					sChopped += " " + chopWord(sNextWord, iBodyWidth);
				else
					sChopped += " " + sNextWord;
			}
 
			//No retokenize the string that has been chopped where necessary
			tokenizer = new StringTokenizer(sChopped);
			buffer = new StringBuffer(tokenizer.nextToken());

					
			while (tokenizer.hasMoreTokens()) { 
				sNextWord = tokenizer.nextToken();
				
				if (mFontMetrics.stringWidth(buffer.toString()) + mFontMetrics.stringWidth(sNextWord) > iBodyWidth) {
					//The line doesn't fit on one row, so we have to "wrap it" by creating a new line
					String NewLine[] = new String[4];
					NewLine[NAMECOLOR] = saNewLine[NAMECOLOR];
					NewLine[MSGCOLOR] = saNewLine[MSGCOLOR];
					if (!mbHangingIndent && bWasChatMsg){
						//2nd line will be stored in the [NAME] field, so color it as a [MSG]
						NewLine[NAMECOLOR] = saNewLine[MSGCOLOR];
					}
					
					//You wrap the body NewLine[MSG] for a chat message
					//if it's regular message (no user name) you have to wrap the NewLine[NAME]
					if (bChatMsg) {
						NewLine[NAME] = bAddUserName ? saNewLine[NAME] : "";
						NewLine[MSG] = buffer.toString(); 
						bWasChatMsg = true;
					}
					else { // wrap non chat message 
						NewLine[NAME] = buffer.toString();
						NewLine[MSG] = "";
					}
					mvFormatedLines.addElement(NewLine);

					buffer = new StringBuffer(sNextWord);
					bAddUserName = false;
					if (!mbHangingIndent && bWasChatMsg){
						//use full width and treat as non chat message (don't use Hanging Indents)
						iBodyWidth = (int)(miWidth - 50);
						bChatMsg = false;						
					}
				}
				else {
					buffer.append(" " + sNextWord);
				}
			}	

			String NewLine[] = new String[4];
			NewLine[NAMECOLOR] = saNewLine[NAMECOLOR];
			NewLine[MSGCOLOR] = saNewLine[MSGCOLOR];
			if (!mbHangingIndent && bWasChatMsg){
				//2nd line will be stored in the [NAME] field, so color it as a [MSG]
				NewLine[NAMECOLOR] = saNewLine[MSGCOLOR];
			}			
			
			if (bChatMsg) {
				NewLine[NAME] = bAddUserName ? saNewLine[NAME] : "";
				NewLine[MSG] = "" + buffer;
			}
			else { // wrap name 
				NewLine[NAME] = "" + buffer;			
				NewLine[MSG] = "";
			}

			mvFormatedLines.addElement(NewLine);
		}
		catch(Exception e) {
			//print it out without wordwrapping
			String UnformattedLine[] = new String[4];
			UnformattedLine[NAMECOLOR] = saNewLine[NAMECOLOR];
			UnformattedLine[MSGCOLOR] = saNewLine[MSGCOLOR];
			UnformattedLine[NAME] = saNewLine[NAME];
			UnformattedLine[MSG] = saNewLine[MSG];

			mvFormatedLines.addElement(UnformattedLine);
		}
    }

	/**
	 * This is called when a single word is too wide to fit on one line.  The word is
	 * is broken apart as many times as needed so that each piece will fit correctly
	 * inside the width of the Text Area
	 *
	 * @param	word		The word to chop up
	 * @param	bodyWidth	The width of the body of text within the TextArea
	 *
	 * @return	The word chopped up into bodyWidth sized pieces (separated by a space)
	 */    
	private String chopWord(String word, int bodyWidth){
		String	sChoppedWord = "";
				
		for (int i = 0; i < word.length(); ++i){
			//see how many letters of the word will fit on the first line
			if (mFontMetrics.stringWidth(sChoppedWord + word.substring(i, i + 1)) < bodyWidth){
				sChoppedWord += word.substring(i, i + 1);	
			}
			else{
				//chop of the beginning letters that fit 
				word = word.substring(i-1);
				//now recursively call chopWord to chop the rest
				sChoppedWord += " " + chopWord(word, bodyWidth);
				return sChoppedWord;
			}
		}
		return sChoppedWord;
	}

	/**
	 * Display the formatted lines of text
	 */	
	private void render() {
		if (mbReformatLines) {
			//Reformat the lines since the size of the control has changed
			/*
			mvFormatedLines.removeAllElements();
			for (int i = 0; i<mvLines.size(); i++) {
				wordWrap(i);
			}
			*/

			//computeScrollbar(true);
			mbReformatLines = false;
		}

		//miTopLine = mScrollbar.getValue();
		//System.out.println("miTopLine=" + miTopLine);

		//mGraphics.setColor(Color.white);
		//mGraphics.fillRect(0, 0, miWidth, miHeight);
		// set scroll if it has not been set

		// set up auto-scroll processing
		if (!scrollSet) {
			scrollSet = true;

			Component parent = getParent();
			while (parent != null && (!(parent instanceof JScrollPane)))
				parent = parent.getParent();

			if (parent != null) {
				JScrollPane pane = (JScrollPane) parent;
				pane.getViewport().addChangeListener(this);
				pane.addMouseWheelListener(this);

				scroll = ((JScrollPane) parent).getVerticalScrollBar();
			}
		}

		/*
		if (isAtBottom == true && scroll != null)
			scroll.setValue(scroll.getValue());
		*/

		fmtBuffer.setLength(0);
		fmtBuffer.append("<html><table>");

		for (int i=miTopLine; i<mvLines.size(); i++) {
			String[] saCurrentLine = (String[])mvLines.elementAt(i);
			//mGraphics.setColor(getColor(saCurrentLine[NAMECOLOR])); 			

			//mGraphics.drawString(saCurrentLine[NAME], 2, (i - miTopLine + 1) * miFontHeight);

			fmtBuffer.append("\n<tr valign=\"top\">")
				.append("<td");

			if (saCurrentLine[MSG].length() == 0)
				fmtBuffer.append(" colspan=\"2\"");
			else if (saCurrentLine[NAME].length() < 25)
				fmtBuffer.append(" nowrap=\"true\"");

			fmtBuffer.append("><font name=\"")
				.append(mFont.getFontName()).append("\" color=\"")
				.append(saCurrentLine[NAMECOLOR]).append("\">")
				.append(editor.edit(saCurrentLine[NAME]))
				.append("</font></td>");

			/*
			 *  maybe not... ??
			if (saCurrentLine[MSG].length() == 0) {
				//for non chat messages (no user name) don't erase with white rect
				continue;
			}
			*/

			fmtBuffer.append("<td><font name=\"")
				.append(mFont.getFontName()).append("\" color=\"")
				.append(saCurrentLine[MSGCOLOR]).append("\" >")
				.append(breakLongWords(editor.edit(saCurrentLine[MSG]), 30))
				.append("</font></td></tr>");

		}

		fmtBuffer.append("</table></html>");

		//System.out.println("chat=" + fmtBuffer.toString());

		try {
			read(new StringReader(fmtBuffer.toString()), null);
		} catch (IOException e) {}
	}

	/**
	 *  break long words up
	 */
	public String breakLongWords(String str, int len)
	{
		int max = str.length();

		// nothing to do?
		if (max <= len) return str;

		int i, from, to=0;
		char c;
		StringBuffer sb = new StringBuffer(max + 100);

		for (from = 0; to < max; from = to) {

			//System.out.println("from=" + from + "; to=" + to);

			c = str.charAt(from);

			to = from+1;

			// find end of next "word"
			while (to < max && " <>\"'".indexOf(str.charAt(to)) < 0) to++;

			//System.out.println("word (size=" + (to-from) + ")=" +
			//			   str.substring(from, to));


			// **DEBUG**
			//if (to == from) break;

			if (to-from > len) {
				System.out.println("word is too big");
				if ("'\"".indexOf(c) >= 0 && str.charAt(to) == c)
					sb.append(str.substring(from, to));
				else {
					//System.out.println("inserting <wbr> at " + to);
					to = from + len;
					
					// try to find a good place for the break
					for (i = 0; i < 5 && i < to-from; i++) {
						if (!Character.isLetterOrDigit(str.charAt(to-i))) {
							to -= i;
							break;
						}
					}

					sb.append(str.substring(from, to)).append("<wr>");
				}
			}
			else {
				// include whitespace
				while (to < max && Character.isWhitespace(str.charAt(to)))
					to++;

				sb.append(str.substring(from, to));
			}
		}

		if (from < max) sb.append(str.substring(from));

		//System.out.println("wordified: " + sb.toString());

		return sb.toString();
	}

	/**
	 *  display at bottom (again)
	 */
	public void isAtBottom(boolean tf)
	{ isAtBottom = tf; }

	/**
	 *  scroll to the bottom once the content has been changed
	 */
	public void stateChanged(ChangeEvent ev)
	{
		// keep scroll at the bottom
		if (isAtBottom == true && scroll != null
			&& (wheelScrolling == true 
				|| scroll.getValueIsAdjusting() == false)) {

			scroll.setValue(scroll.getMaximum() - scroll.getVisibleAmount());
			isAtBottom = true;
		}
		else 
			isAtBottom = (scroll.getValue() >=
						  (scroll.getMaximum() - scroll.getVisibleAmount()));

		System.out.println("change: isAtBottom=" + isAtBottom);

		wheelScrolling = false;

		/*
		 * This rigmarole is to reset the ValueIsAdjusting() value *after*
		 * the value has actually been set *sigh*
		 */
		/*
		if (wheelScrolling) {
			wheelScrolling = false;
			scroll.setValue(scroll.getValue());
		}
		else scroll.setValueIsAdjusting(false);
		*/

	}

	/**
	 */
	public void mouseWheelMoved(MouseWheelEvent ev)
	{
		if (scroll == null) return;

		wheelScrolling = true;
		//scroll.setValueIsAdjusting(true);

		int max = scroll.getMaximum() - scroll.getVisibleAmount();
		int pos = scroll.getValue() + ev.getUnitsToScroll();

		scroll.setValue(pos);

		isAtBottom = (pos >= max);
	}

	/**
	 * Compute the Scrollbar value
	 * 
     * @param scrollToBottom		true if you should scroll to bottom of the text area
	 */	
	void computeScrollbar(boolean scrollToBottom) {
		if (mFontMetrics == null) {
			return;		// not visible
		}

		int iSBTop;

		if (scrollToBottom) {
			iSBTop = mvFormatedLines.size() - miNbrLinesPerPage + 1;
		}
		else if (mvFormatedLines.size() > miNbrLinesPerPage) {
			iSBTop = mScrollbar.getValue();
		}
		else {
			iSBTop = 0;
		}
		
		mScrollbar.setValues(iSBTop, 10, 0, mvFormatedLines.size());
	}

	/**
	 * Converts a string formatted as "rrggbb" to an awt.Color object
	 *
     * @return			awt.Color object (black if invalid color)
	 */
    private Color getColor(String RGB) {
		int red;
		int green;
		int blue;
	
		try{
			red = (Integer.decode("0x" + RGB.substring(0,2))).intValue();
			green = (Integer.decode("0x" + RGB.substring(2,4))).intValue();
			blue = (Integer.decode("0x" + RGB.substring(4,6))).intValue();
			return new Color(red,green,blue);
		} 
		catch (Exception e){
			//Default to black on none RGB values
			return Color.black;
		}
    }	
}
