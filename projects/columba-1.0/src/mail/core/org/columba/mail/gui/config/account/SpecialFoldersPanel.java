// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.config.account;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.base.CheckBoxWithMnemonic;
import org.columba.core.gui.base.LabelWithMnemonic;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.config.SpecialFoldersItem;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.gui.tree.util.SelectFolderDialog;
import org.columba.mail.gui.tree.util.TreeNodeList;
import org.columba.mail.util.MailResourceLoader;

public class SpecialFoldersPanel extends DefaultPanel implements ActionListener {
	private JLabel trashLabel;

	private JButton trashButton;

	private JLabel draftsLabel;

	private JButton draftsButton;

	private JLabel templatesLabel;

	private JButton templatesButton;

	private JLabel sentLabel;

	private JButton sentButton;

	private JLabel inboxLabel;

	private JButton inboxButton;

	private JCheckBox defaultAccountCheckBox;

	private SpecialFoldersItem item;

	private AccountItem accountItem;

	private IFrameMediator mediator;

	public SpecialFoldersPanel(IFrameMediator mediator,
			AccountItem accountItem, SpecialFoldersItem item) {
		super();

		this.mediator = mediator;
		this.item = item;
		this.accountItem = accountItem;

		initComponents();

		updateComponents(true);
	}

	protected String getPath(String uid) {
		Integer u = new Integer(uid);

		IMailbox f = (IMailbox) FolderTreeModel.getInstance().getFolder(
				u.intValue());

		if (f == null) {
			return ""; //$NON-NLS-1$
		}

		return f.getTreePath();
	}

	protected String getUid(String treePath) {
		TreeNodeList list = new TreeNodeList(treePath);
		IMailbox f = (IMailbox) FolderTreeModel.getInstance().getFolder(list);

		if (f == null) {
			return ""; //$NON-NLS-1$
		}

		Integer i = new Integer(f.getUid());

		return i.toString();
	}

	protected boolean isPopAccount() {
		return accountItem.isPopAccount();
	}

	protected void updateComponents(boolean b) {
		if (b) {
			if (!isPopAccount()) {
				trashButton
						.setText(getPath(item.get(SpecialFoldersItem.TRASH)));
			}

			draftsButton.setText(getPath(item.get(SpecialFoldersItem.DRAFTS)));
			templatesButton.setText(getPath(item
					.get(SpecialFoldersItem.TEMPLATES)));
			sentButton.setText(getPath(item.get(SpecialFoldersItem.SENT)));

			if (isPopAccount()) {
				inboxButton
						.setText(getPath(item.get(SpecialFoldersItem.INBOX)));
			}

			defaultAccountCheckBox.setSelected(item
					.getBoolean(SpecialFoldersItem.USE_DEFAULT_ACCOUNT));

			defaultAccountCheckBox.setEnabled(MailConfig.getInstance()
					.getAccountList().getDefaultAccountUid() == accountItem
					.getInteger(SpecialFoldersItem.UID));

			if (defaultAccountCheckBox.isEnabled()
					&& defaultAccountCheckBox.isSelected()) {
				showDefaultAccountWarning();
			} else {
				layoutComponents();
			}
		} else {
			if (!isPopAccount()) {
				item.setString(SpecialFoldersItem.TRASH, getUid(trashButton
						.getText()));
			}

			item.setString(SpecialFoldersItem.DRAFTS, getUid(draftsButton
					.getText()));
			item.setString(SpecialFoldersItem.TEMPLATES, getUid(templatesButton
					.getText()));
			item.setString(SpecialFoldersItem.SENT,
					getUid(sentButton.getText()));

			if (isPopAccount()) {
				item.setString(SpecialFoldersItem.INBOX, getUid(inboxButton
						.getText()));
			}

			item.setBoolean(SpecialFoldersItem.USE_DEFAULT_ACCOUNT,
					defaultAccountCheckBox.isSelected());
		}
	}

	protected void layoutComponents() {
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		GridBagLayout mainLayout = new GridBagLayout();
		GridBagConstraints mainConstraints = new GridBagConstraints();

		mainConstraints.anchor = GridBagConstraints.NORTHWEST;
		mainConstraints.fill = GridBagConstraints.HORIZONTAL;
		mainConstraints.weightx = 1.0;

		setLayout(mainLayout);

		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.insets = new Insets(0, 10, 5, 0);
		mainLayout.setConstraints(defaultAccountCheckBox, mainConstraints);
		add(defaultAccountCheckBox);

		JPanel folderPanel = new JPanel();
		Border b1 = BorderFactory.createEtchedBorder();
		Border b2 = BorderFactory.createTitledBorder(b1, MailResourceLoader
				.getString("dialog", "account", "account_information"));

		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border border = BorderFactory.createCompoundBorder(b2, emptyBorder);
		folderPanel.setBorder(border);

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		folderPanel.setLayout(layout);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;

		if (isPopAccount()) {
			c.weightx = 0.1;
			c.gridwidth = GridBagConstraints.RELATIVE;
			layout.setConstraints(inboxLabel, c);
			folderPanel.add(inboxLabel);

			c.gridwidth = GridBagConstraints.REMAINDER;
			c.weightx = 0.9;
			layout.setConstraints(inboxButton, c);
			folderPanel.add(inboxButton);
		}

		c.weightx = 0.1;
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(draftsLabel, c);
		folderPanel.add(draftsLabel);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(draftsButton, c);
		folderPanel.add(draftsButton);

		c.weightx = 0.1;
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(templatesLabel, c);
		folderPanel.add(templatesLabel);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(templatesButton, c);
		folderPanel.add(templatesButton);

		c.weightx = 0.1;
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(sentLabel, c);
		folderPanel.add(sentLabel);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.9;
		layout.setConstraints(sentButton, c);
		folderPanel.add(sentButton);

		if (!isPopAccount()) {
			c.weightx = 0.1;
			c.gridwidth = GridBagConstraints.RELATIVE;
			layout.setConstraints(trashLabel, c);
			folderPanel.add(trashLabel);
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.weightx = 0.9;
			layout.setConstraints(trashButton, c);
			folderPanel.add(trashButton);
		}

		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.insets = new Insets(0, 0, 0, 0);
		mainLayout.setConstraints(folderPanel, mainConstraints);
		add(folderPanel);

		mainConstraints.gridheight = GridBagConstraints.REMAINDER;
		mainConstraints.weighty = 1.0;
		mainConstraints.fill = GridBagConstraints.VERTICAL;

		Component vglue = Box.createVerticalGlue();
		mainLayout.setConstraints(vglue, mainConstraints);
		add(vglue);
	}

	protected void showDefaultAccountWarning() {
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		GridBagLayout mainLayout = new GridBagLayout();
		GridBagConstraints mainConstraints = new GridBagConstraints();

		setLayout(mainLayout);

		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.anchor = GridBagConstraints.NORTHWEST;
		mainConstraints.weightx = 1.0;
		mainConstraints.insets = new Insets(0, 10, 5, 0);
		mainLayout.setConstraints(defaultAccountCheckBox, mainConstraints);
		add(defaultAccountCheckBox);

		mainConstraints = new GridBagConstraints();
		mainConstraints.weighty = 1.0;
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;

		/*
		 * mainConstraints.fill = GridBagConstraints.BOTH;
		 * mainConstraints.insets = new Insets(0, 0, 0, 0);
		 * mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		 * mainConstraints.weightx = 1.0; mainConstraints.weighty = 1.0;
		 */
		JLabel label = new JLabel(MailResourceLoader.getString("dialog",
				"account", "using_default_account_settings"));
		Font newFont = label.getFont().deriveFont(Font.BOLD);
		label.setFont(newFont);
		mainLayout.setConstraints(label, mainConstraints);
		add(label);
	}

	protected void initComponents() {
		defaultAccountCheckBox = new CheckBoxWithMnemonic(MailResourceLoader
				.getString("dialog", "account", "use_default_account_settings"));

		// defaultAccountCheckBox.setEnabled(false);
		defaultAccountCheckBox.setActionCommand("DEFAULT_ACCOUNT");
		defaultAccountCheckBox.addActionListener(this);

		if (isPopAccount()) {
			inboxLabel = new LabelWithMnemonic(MailResourceLoader.getString(
					"dialog", "account", "inbox_folder")); //$NON-NLS-1$
			inboxButton = new JButton();
			inboxButton.setActionCommand("INBOX"); //$NON-NLS-1$
			inboxButton.addActionListener(this);
			inboxLabel.setLabelFor(inboxButton);
		}

		draftsLabel = new LabelWithMnemonic(MailResourceLoader.getString(
				"dialog", "account", "drafts_folder")); //$NON-NLS-1$
		draftsButton = new JButton();
		draftsButton.setActionCommand("DRAFTS"); //$NON-NLS-1$
		draftsButton.addActionListener(this);
		draftsLabel.setLabelFor(draftsButton);

		templatesLabel = new LabelWithMnemonic(MailResourceLoader.getString(
				"dialog", "account", "templates_folder")); //$NON-NLS-1$
		templatesButton = new JButton();
		templatesButton.setActionCommand("TEMPLATES"); //$NON-NLS-1$
		templatesButton.addActionListener(this);
		templatesLabel.setLabelFor(templatesButton);

		sentLabel = new LabelWithMnemonic(MailResourceLoader.getString(
				"dialog", "account", "sent_folder")); //$NON-NLS-1$		
		sentButton = new JButton();
		sentButton.setActionCommand("SENT"); //$NON-NLS-1$
		sentButton.addActionListener(this);
		sentLabel.setLabelFor(sentButton);

		if (!isPopAccount()) {
			trashLabel = new LabelWithMnemonic(MailResourceLoader.getString(
					"dialog", "account", "trash_folder")); //$NON-NLS-1$			
			trashButton = new JButton();
			trashButton.setActionCommand("TRASH"); //$NON-NLS-1$
			trashButton.addActionListener(this);
			trashLabel.setLabelFor(trashButton);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("TRASH")) //$NON-NLS-1$
		{
			SelectFolderDialog dialog = new SelectFolderDialog(mediator);

			if (dialog.success()) {
				IMailFolder selectedFolder = (IMailFolder) dialog
						.getSelectedFolder();
				String path = selectedFolder.getTreePath();

				trashButton.setText(path);

				// int uid = selectedFolder.getUid();
				// item.setTrash( new Integer(uid).toString() );
			}
		} else if (action.equals("INBOX")) //$NON-NLS-1$
		{
			SelectFolderDialog dialog = new SelectFolderDialog(mediator);

			if (dialog.success()) {
				IMailFolder selectedFolder = (IMailFolder) dialog
						.getSelectedFolder();
				String path = selectedFolder.getTreePath();

				inboxButton.setText(path);

				// int uid = selectedFolder.getUid();
				// item.setInbox( new Integer(uid).toString() );
			}
		} else if (action.equals("DRAFTS")) //$NON-NLS-1$
		{
			SelectFolderDialog dialog = new SelectFolderDialog(mediator);

			if (dialog.success()) {
				IMailFolder selectedFolder = (IMailFolder) dialog
						.getSelectedFolder();
				String path = selectedFolder.getTreePath();

				draftsButton.setText(path);

				// int uid = selectedFolder.getUid();
				// item.setDrafts( new Integer(uid).toString() );
			}
		} else if (action.equals("TEMPLATES")) //$NON-NLS-1$
		{
			SelectFolderDialog dialog = new SelectFolderDialog(mediator);

			if (dialog.success()) {
				IMailFolder selectedFolder = (IMailFolder) dialog
						.getSelectedFolder();
				String path = selectedFolder.getTreePath();

				templatesButton.setText(path);

				// int uid = selectedFolder.getUid();
				// item.setTemplates( new Integer(uid).toString() );
			}
		} else if (action.equals("SENT")) //$NON-NLS-1$
		{
			SelectFolderDialog dialog = new SelectFolderDialog(mediator);

			if (dialog.success()) {
				IMailFolder selectedFolder = (IMailFolder) dialog
						.getSelectedFolder();
				String path = selectedFolder.getTreePath();

				sentButton.setText(path);

				// int uid = selectedFolder.getUid();
				// item.setSent( new Integer(uid).toString() );
			}
		} else if (action.equals("DEFAULT_ACCOUNT")) {
			removeAll();

			if (defaultAccountCheckBox.isSelected()) {
				showDefaultAccountWarning();
			} else {
				layoutComponents();
			}

			revalidate();
		}
	}

	public boolean isFinished() {
		boolean result = true;

		return result;
	}
}