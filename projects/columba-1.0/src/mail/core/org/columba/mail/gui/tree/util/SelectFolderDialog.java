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
package org.columba.mail.gui.tree.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.folder.IFolder;
import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.gui.frame.TreeViewOwner;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.gui.tree.ISelectFolderDialog;
import org.columba.mail.gui.tree.command.CreateAndSelectSubFolderCommand;
import org.columba.mail.util.MailResourceLoader;
import org.frapuccino.swing.SortedJTree;
import org.frapuccino.swing.SortedTreeModelDecorator;

/**
 * Select folder dialog.
 * 
 * @author fdietz
 */
public class SelectFolderDialog extends JDialog implements ActionListener,
		TreeSelectionListener, ISelectFolderDialog {

	private String name;

	private boolean bool = false;

	protected JTree tree;

	protected JButton okButton;

	protected JButton newButton;

	protected IMailFolder selectedFolder;

	private ButtonWithMnemonic cancelButton;

	private IFrameMediator mediator;

	public SelectFolderDialog(JFrame parent) {
		super(parent, true);

		setTitle(MailResourceLoader.getString("dialog", "folder",
				"select_folder"));

		name = new String("name");

		initComponents();

		layoutComponents();

		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(this, "CANCEL",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public SelectFolderDialog(IFrameMediator mediator) {
		super(mediator.getView().getFrame(), true);

		this.mediator = mediator;

		setTitle(MailResourceLoader.getString("dialog", "folder",
				"select_folder"));

		name = new String("name");

		initComponents();

		layoutComponents();

		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(this, "CANCEL",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	protected void layoutComponents() {
		JPanel contentPane = (JPanel) getContentPane();
		contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));

		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(150, 300));
		contentPane.add(scrollPane);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(17, 0, 0, 0));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0));
		buttonPanel.add(okButton);
		buttonPanel.add(newButton);

		buttonPanel.add(cancelButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
	}

	protected void initComponents() {
		// get global sorting state

		// bug #999969 (fdietz): classcast exception
		// if mediator contains a JTree
		if (mediator instanceof TreeViewOwner) {

			TreeModel t = ((TreeViewOwner) mediator).getTreeController()
					.getModel();
			// if mediator contains a sortable treemodel
			if (t instanceof SortedTreeModelDecorator) {
				// sorting is enabled
				SortedTreeModelDecorator treemodel = (SortedTreeModelDecorator) t;
				Comparator c = treemodel.getSortingComparator();

				tree = new SortedJTree(FolderTreeModel.getInstance());
				// apply sorting state
				SortedTreeModelDecorator m = (SortedTreeModelDecorator) tree
						.getModel();
				m.setSortingComparator(c);
			} else {
				// sorting is disabled
				tree = new SortedJTree(FolderTreeModel.getInstance());
			}
		} else {
			// sorting is disabled
			tree = new SortedJTree(FolderTreeModel.getInstance());
		}

		tree.expandRow(0);
		tree.expandRow(1);
		tree.putClientProperty("JTree.lineStyle", "Angled");
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);

		// default selection is local Inbox
		selectedFolder = FolderTreeModel.getInstance().getFolder(101);
		tree.setSelectionPath(new TreePath(selectedFolder.getPath()));

		// add selection listener
		tree.addTreeSelectionListener(this);

		FolderTreeCellRenderer renderer = new FolderTreeCellRenderer();
		tree.setCellRenderer(renderer);

		okButton = new ButtonWithMnemonic(MailResourceLoader
				.getString("", "ok"));
		okButton.setEnabled(true);
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);

		newButton = new ButtonWithMnemonic(MailResourceLoader.getString(
				"dialog", "folder", "new_folder"));
		newButton.setEnabled(true);
		newButton.setActionCommand("NEW");
		newButton.addActionListener(this);

		cancelButton = new ButtonWithMnemonic(MailResourceLoader.getString("",
				"cancel"));
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);

	}

	public boolean success() {
		return bool;
	}

	public IFolder getSelectedFolder() {
		return selectedFolder;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("OK")) {
			// name = textField.getText();
			bool = true;

			dispose();
		} else if (action.equals("CANCEL")) {
			bool = false;

			dispose();
		} else if (action.equals("NEW")) {
			CreateFolderDialog dialog = new CreateFolderDialog(mediator, tree
					.getSelectionPath());

			String name;

			if (dialog.success()) {
				// ok pressed
				name = dialog.getName();
			} else {
				// cancel pressed
				return;
			}

			MailFolderCommandReference r = new MailFolderCommandReference(
					dialog.getSelected());
			r.setFolderName(name);

			CommandProcessor.getInstance().addOp(
					new CreateAndSelectSubFolderCommand(tree, r));
		}
	}

	/**
	 * ***************************** tree selection listener
	 * *******************************
	 */
	public void valueChanged(TreeSelectionEvent e) {
		IMailFolder node = (IMailFolder) tree.getLastSelectedPathComponent();

		if (node == null) {
			return;
		}

		selectedFolder = node;
		
		if (node.supportsAddMessage()) {
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false);
		}
	}
}