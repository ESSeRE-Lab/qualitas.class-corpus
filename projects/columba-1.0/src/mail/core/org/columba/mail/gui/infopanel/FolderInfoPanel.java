//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.gui.infopanel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.selection.ISelectionListener;
import org.columba.api.selection.SelectionChangedEvent;
import org.columba.core.gui.frame.ContainerInfoPanel;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.event.IFolderEvent;
import org.columba.mail.folder.event.IFolderListener;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.gui.tree.util.FolderTreeCellRenderer;
import org.columba.ristretto.message.MailboxInfo;

/**
 * Shows summary information of the currently selected folder.
 * 
 * @author fdietz
 * 
 */
public class FolderInfoPanel extends ContainerInfoPanel implements
		ISelectionListener, IFolderListener {
	private JLabel leftLabel;

	private JLabel readLabel;

	private JLabel unreadLabel;

	private JLabel recentLabel;

	private JPanel rightPanel;

	private MailboxInfo info;

	private IMailFolder folder;

	public FolderInfoPanel(IFrameMediator controller) {
		super();

	}

	public void initComponents() {
		super.initComponents();

		leftLabel = new JLabel("Total: Unseen:");
		leftLabel.setForeground(UIManager.getColor("List.selectionForeground"));
		leftLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		leftLabel.setFont(font);
		leftLabel.setIconTextGap(10);

		leftLabel.setText("Folder");

		gridbagConstraints.gridx = 0;
		gridbagConstraints.weightx = 0.0;
		gridbagConstraints.anchor = GridBagConstraints.WEST;

		gridbagLayout.setConstraints(leftLabel, gridbagConstraints);
		panel.add(leftLabel);

		Component box = Box.createHorizontalGlue();
		gridbagConstraints.gridx = 1;
		gridbagConstraints.weightx = 1.0;
		gridbagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridbagLayout.setConstraints(box, gridbagConstraints);
		panel.add(box);

		gridbagConstraints.gridx = 2;
		gridbagConstraints.weightx = 0.0;
		gridbagConstraints.fill = GridBagConstraints.NONE;

		gridbagConstraints.anchor = GridBagConstraints.EAST;

		GridBagLayout layout = new GridBagLayout();
		rightPanel = new JPanel();
		rightPanel.setLayout(layout);
		rightPanel.setOpaque(false);
		gridbagLayout.setConstraints(rightPanel, gridbagConstraints);
		panel.add(rightPanel);

		readLabel = new JLabel();

		readLabel.setFont(font);
		readLabel.setForeground(UIManager.getColor("List.selectionForeground"));
		readLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		unreadLabel = new JLabel();

		unreadLabel.setFont(font);
		unreadLabel.setForeground(UIManager
				.getColor("List.selectionForeground"));
		unreadLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		recentLabel = new JLabel();
		recentLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		recentLabel.setFont(font);
		recentLabel.setForeground(UIManager
				.getColor("List.selectionForeground"));
		recentLabel.setIconTextGap(10);

		gridbagConstraints = new GridBagConstraints();
		gridbagConstraints.gridx = 0;
		gridbagConstraints.weightx = 0.0;
		gridbagConstraints.anchor = GridBagConstraints.SOUTH;
		gridbagConstraints.insets = new Insets(0, 0, 0, 0);

		gridbagConstraints.gridx = 0;
		gridbagConstraints.insets = new Insets(0, 0, 0, 0);
		gridbagConstraints.anchor = GridBagConstraints.WEST;
		layout.setConstraints(readLabel, gridbagConstraints);
		rightPanel.add(readLabel);

		gridbagConstraints.gridx = 1;
		gridbagConstraints.insets = new Insets(0, 0, 0, 0);
		gridbagConstraints.anchor = GridBagConstraints.WEST;
		layout.setConstraints(unreadLabel, gridbagConstraints);
		rightPanel.add(unreadLabel);

		gridbagConstraints.gridx = 2;
		gridbagConstraints.insets = new Insets(0, 0, 0, 0);
		layout.setConstraints(recentLabel, gridbagConstraints);
		rightPanel.add(recentLabel);
	}

	public void resetRenderer() {
		if (folder != null)
			folder.removeFolderListener(this);
		initComponents();
	}

	public void setFolder(IMailFolder newFolder) {
		if (!(newFolder instanceof IMailFolder))
			return;

		if (folder != null)
			folder.removeFolderListener(this);

		info = ((IMailbox) newFolder).getMessageFolderInfo();

		if (info == null) {
			return;
		}
		this.folder = newFolder;

		leftLabel.setIcon(FolderTreeCellRenderer
				.getFolderIcon(newFolder, false));

		update();

		newFolder.addFolderListener(this);
	}

	private void update() {
		info = ((IMailbox) folder).getMessageFolderInfo();
		int total = info.getExists();
		int unread = info.getUnseen();
		int recent = info.getRecent();

		leftLabel.setText(folder.getName() + " ( total: " + total + " )");
		unreadLabel.setText(" unread: " + unread);
		readLabel.setText(" read: " + (total - unread) + "  ");

		if (recent > 0) {
			recentLabel.setText(" recent: " + recent);
		} else {
			recentLabel.setText("");
		}
	}

	public void selectionChanged(SelectionChangedEvent e) {
		TreeSelectionChangedEvent treeEvent = (TreeSelectionChangedEvent) e;

		// we are only interested in folders containing messages
		// meaning of instance AbstractMessageFolder and not of instance
		// FolderTreeNode
		// -> casting here to Folder
		if (treeEvent.getSelected()[0] != null
				&& treeEvent.getSelected()[0] instanceof IMailbox) {
			setFolder((IMailbox) treeEvent.getSelected()[0]);
		} else {
			resetRenderer();
		}
	}

	public void messageAdded(IFolderEvent e) {
	}

	public void messageRemoved(IFolderEvent e) {
	}

	public void messageFlagChanged(IFolderEvent e) {
	}

	public void folderPropertyChanged(IFolderEvent e) {
		Runnable doWorkRunnable = new Runnable() {
			public void run() {
				update();
			}
		};
		SwingUtilities.invokeLater(doWorkRunnable);

	}

	public void folderAdded(IFolderEvent e) {
	}

	public void folderRemoved(IFolderEvent e) {
	}
}
