/******************************************************************************
 * Product: Compiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 3600 Bridge Parkway #102, Redwood City, CA 94065, USA      *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.apps;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.*;

import javax.swing.*;

import org.compiere.model.*;
import org.compiere.swing.*;
import org.compiere.util.*;
import org.apache.ecs.xhtml.*;

/**
 * 	Application Chat
 *
 *  @author Jorg Janke
 *  @version $Id: AChat.java,v 1.3 2006/07/30 00:51:27 jjanke Exp $
 */
public class AChat extends CDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *	Constructor.
	 *	loads Chat, if ID <> 0
	 *  @param frame frame
	 *  @param WindowNo window no
	 *  @param CM_Chat_ID chat
	 *  @param AD_Table_ID table
	 *  @param Record_ID record key
	 *  @param Description description
	 *  @param trx transaction
	 */
	public AChat (Frame frame, int WindowNo, int CM_Chat_ID,
		int AD_Table_ID, int Record_ID, String Description,
		Trx trx)
	{
		//Here Record Comments would be see as title of the Record Comments Window
		super (frame, Msg.getMsg(Env.getCtx(), "Chat") + " " + Description, true);
		//	needs to be modal otherwise APanel does not recongize change.
		log.config("ID=" + CM_Chat_ID
			+ ", Table=" + AD_Table_ID + ", Record=" + Record_ID);
		//
		try
		{
			staticInit();
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, "", ex);
		}
		//	Create Model
		if (CM_Chat_ID == 0)
			m_chat = new MChat (Env.getCtx(), AD_Table_ID, Record_ID, Description, trx);
		else
			m_chat = new MChat (Env.getCtx(), CM_Chat_ID, trx);
		loadChat();
		//
		try
		{
			AEnv.showCenterWindow(frame, this);
		}
		catch (Exception e)
		{
		}
		newText.requestFocus();
	}	//	Attachment

	/** Attachment				*/
	private MChat			m_chat;
	/**	Logger					*/
	private static CLogger log = CLogger.getCLogger(AChat.class);

	private CPanel 			mainPanel = new CPanel(new BorderLayout(5,5));
	private CTextPane		historyText = new CTextPane();
	private CTextArea		newText = new CTextArea();
	private ConfirmPanel	confirmPanel = new ConfirmPanel(true);

	/**
	 * 	Static Init.
	 *	@throws Exception
	 */
	private void staticInit () throws Exception
	{
		this.getContentPane().add(mainPanel);
		//
		historyText.setPreferredSize(new Dimension(350,300));
		historyText.setReadWrite(false);
		historyText.setMargin(new Insets(2,2,2,2));
		newText.setPreferredSize(new Dimension(350,200));
		newText.setMargin(new Insets(2,2,2,2));
		//
		JSplitPane textPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
			historyText, newText);
		mainPanel.add(textPane, BorderLayout.CENTER);

		//TODO 	Confidentiality

		//	South
		mainPanel.add(confirmPanel, BorderLayout.SOUTH);
		confirmPanel.addActionListener(this);
	}	//	staticInit

	/**
	 * 	Load Chat
	 */
	private void loadChat()
	{
		p history = m_chat.getHistory(X_CM_Chat.CONFIDENTIALTYPE_Internal);
		String text = null;
		if (history != null)
			text = m_chat.getHistory(X_CM_Chat.CONFIDENTIALTYPE_Internal).toString();
		historyText.setText(text);
	}	//	loadChat


	/**
	 * 	Action Performed
	 *	@param e event
	 */
	@Override
	public void actionPerformed (ActionEvent e)
	{
		if (e.getActionCommand().equals(ConfirmPanel.A_OK))
		{
			String data = newText.getText();
			if (data != null && data.length() > 0)
			{
				log.config(data);
				if (m_chat.get_ID() == 0)
					m_chat.save();
				MChatEntry entry = new MChatEntry(m_chat, data);
				entry.save();
			}	//	data to be saved
		}
		dispose();
	}	//	actionPerformed

}	//	AChat
