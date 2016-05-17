//The contents of this file are subject to the Mozilla Public License Version
//1.1
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
//Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.core.gui.profiles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.DoubleClickListener;
import org.columba.core.gui.util.DialogHeaderPanel;
import org.columba.core.help.HelpManager;
import org.columba.core.resourceloader.GlobalResourceLoader;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.xml.XmlElement;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Profile chooser dialog.
 * <p>
 * User can choose a profile from a list. Add a new profile or edit and existing
 * profiles's properties.
 * <p>
 * Additionally, the user can choose to hide this dialog on next startup.
 * 
 * @author fdietz
 */
public class ProfileManagerDialog extends JDialog implements ActionListener,
		ListSelectionListener {
	private static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";

	protected JButton okButton;

	protected JButton helpButton;

	protected JButton addButton;

	protected JButton editButton;

	protected JButton removeButton;

	protected JButton importButton;

	protected JButton exportButton;

	//protected JButton defaultButton;
	private DefaultListModel model;

	protected JList list;

	protected String selection;

	protected JLabel nameLabel;

	protected JCheckBox checkBox;

	private IFrameMediator mediator;

	public ProfileManagerDialog(IFrameMediator mediator)
			throws HeadlessException {
		super(mediator.getView().getFrame(), GlobalResourceLoader.getString(
				RESOURCE_PATH, "profiles", "manager.title"), true);
		this.mediator = mediator;

		initComponents();

		layoutComponents();

		pack();

		setLocationRelativeTo(null);
		setVisible(true);
	}

	protected void layoutComponents() {

		getContentPane().add(createPanel(), BorderLayout.CENTER);

		JPanel bottomPanel = createBottomPanel();
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		getContentPane().add(
				new DialogHeaderPanel(GlobalResourceLoader.getString(
						RESOURCE_PATH, "profiles", "header_title"),
						GlobalResourceLoader.getString(RESOURCE_PATH,
								"profiles", "header_description"), ImageLoader
								.getImageIcon("system-config-users-32.png")),
				BorderLayout.NORTH);
	}

	private JPanel createPanel() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:GROW(1.0),3DLU,FILL:DEFAULT:NONE",
				"CENTER:DEFAULT:NONE,1DLU,FILL:DEFAULT:GROW(1.0),3DLU,CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		jpanel1.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		jpanel1.setLayout(formlayout1);

		JLabel jlabel1 = new JLabel();
		jlabel1.setText("Profiles:");
		jpanel1.add(jlabel1, cc.xy(1, 1));

		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(250, 150));
		jpanel1.add(scrollPane, cc.xy(1, 3));

		jpanel1.add(checkBox, cc.xy(1, 5));

		jpanel1.add(createPanel1(), new CellConstraints(3, 3, 1, 1,
				CellConstraints.DEFAULT, CellConstraints.TOP));

		return jpanel1;
	}

	private JPanel createPanel1() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:NONE",
				"CENTER:DEFAULT:NONE,3DLU,CENTER:DEFAULT:NONE,3DLU,CENTER:DEFAULT:NONE,3DLU, CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		jpanel1.add(addButton, cc.xy(1, 1));

		jpanel1.add(editButton, cc.xy(1, 3));

		jpanel1.add(removeButton, cc.xy(1, 5));

		return jpanel1;
	}

	/**
	 * @return
	 */
	private JPanel createBottomPanel() {
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 6, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		buttonPanel.add(okButton);

		buttonPanel.add(helpButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		return bottomPanel;
	}

	protected void initComponents() {
		addButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "profiles", "add"));
		addButton.setActionCommand("ADD");
		addButton.addActionListener(this);
		addButton.setEnabled(false);

		editButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "profiles", "edit"));
		editButton.setActionCommand("EDIT");
		editButton.addActionListener(this);
		editButton.setEnabled(false);

		removeButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "profiles", "remove"));
		removeButton.setActionCommand("REMOVE");
		removeButton.addActionListener(this);
		removeButton.setEnabled(false);

		importButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "profiles", "import"));
		importButton.setActionCommand("IMPORT");
		importButton.addActionListener(this);

		exportButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
				RESOURCE_PATH, "profiles", "export"));
		exportButton.setActionCommand("EXPORT");
		exportButton.addActionListener(this);
		exportButton.setEnabled(false);

		nameLabel = new JLabel("Choose Profile:");

		checkBox = new JCheckBox("Always ask on startup.");
		checkBox.setSelected(ProfileManager.getInstance().isAlwaysAsk());
		checkBox.setActionCommand("CHECKBOX");
		checkBox.addActionListener(this);

		okButton = new ButtonWithMnemonic(GlobalResourceLoader.getString("",
				"", "close"));
		okButton.setActionCommand("CLOSE");
		okButton.addActionListener(this);

		helpButton = new ButtonWithMnemonic(GlobalResourceLoader.getString("",
				"", "help"));

		// associate with JavaHelp
		HelpManager.getInstance().enableHelpOnButton(helpButton,
				"extending_columba_2");
		HelpManager.getInstance().enableHelpKey(getRootPane(),
				"extending_columba_2");

		XmlElement profiles = ProfileManager.getInstance().getProfiles();
		model = new DefaultListModel();
		model.addElement("Default");

		for (int i = 0; i < profiles.count(); i++) {
			XmlElement p = profiles.getElement(i);
			String name = p.getAttribute("name");
			model.addElement(name);
		}

		list = new JList(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);
		list.addMouseListener(new DoubleClickListener() {
			public void doubleClick(MouseEvent e) {
				actionPerformed(new ActionEvent(list, 0, "EDIT"));
			}
		});

		String selected = ProfileManager.getInstance().getSelectedProfile();
		if (selected != null) {
			list.setSelectedValue(selected, true);
		}

		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(this, "CLOSE",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("CLOSE")) {
			ProfileManager.getInstance().setAlwaysAsk(isAlwaysAskSelected());

			setVisible(false);
		} else if (action.equals("CHECKBOX")) {

		} else if (action.equals("ADD")) {
			JFileChooser fc = new JFileChooser();
			fc.setMultiSelectionEnabled(true);
			// bug #996381 (fdietz), directories only!!
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setFileHidingEnabled(false);

			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File location = fc.getSelectedFile();
				Profile p = new Profile(location.getName(), location);
				// add profile to profiles.xml
				ProfileManager.getInstance().addProfile(p);

				// add to listmodel
				model.addElement(p.getName());
				// select new item
				list.setSelectedValue(p.getName(), true);
			}
		} else if (action.equals("EDIT")) {
			String inputValue = JOptionPane.showInputDialog(
					GlobalResourceLoader.getString(RESOURCE_PATH, "profiles",
							"enter_name"), selection);

			if (inputValue == null) {
				return;
			}

			// rename profile in profiles.xml
			ProfileManager.getInstance().renameProfile(selection, inputValue);

			// modify listmodel
			model.setElementAt(inputValue, model.indexOf(selection));

			selection = inputValue;
		} else if (action.equals("REMOVE")) {
			if (ProfileManager.getInstance().getCurrentProfile().getName()
					.equals(selection)) {
				// can't delete currently running profile
				JOptionPane.showMessageDialog(this, GlobalResourceLoader
						.getString(RESOURCE_PATH, "profiles", "errDelete.msg"),
						GlobalResourceLoader.getString(RESOURCE_PATH,
								"profiles", "errDelete.title"),
						JOptionPane.ERROR_MESSAGE);
			} else {
				Profile p = ProfileManager.getInstance().getProfileForName(
						selection);
				if (p != null) {
					int n = JOptionPane.showConfirmDialog(this,
							GlobalResourceLoader.getString(RESOURCE_PATH,
									"profiles", "confirmDelete.msg"),
							GlobalResourceLoader.getString(RESOURCE_PATH,
									"profiles", "confirmDelete.title"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (n == JOptionPane.NO_OPTION) {
						return;
					}

					ProfileManager.getInstance().removeProfile(selection);
					model.removeElement(selection);

				}
			}
		} else if (action.equals("IMPORT")) {
			// TODO (@author fdietz): add import feature
			/*
			 * JFileChooser chooser = new JFileChooser();
			 * chooser.addChoosableFileFilter(new FileFilter() { public boolean
			 * accept(File file) { return file.isDirectory() ||
			 * file.getName().toLowerCase().endsWith(".zip"); }
			 * 
			 * public String getDescription() { return "Columba Profile
			 * Archive"; } }); chooser.setAcceptAllFileFilterUsed(false);
			 * 
			 * int result = chooser.showOpenDialog(this);
			 * 
			 * if (result == JFileChooser.APPROVE_OPTION) { File file =
			 * chooser.getSelectedFile(); }
			 */
		} else if (action.equals("EXPORT")) {
			// TODO (@author fdietz): add export feature
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		boolean enabled = !list.isSelectionEmpty();
		addButton.setEnabled(enabled);

		exportButton.setEnabled(enabled);

		// get current list selection
		selection = (String) list.getSelectedValue();

		// user's can't delete default account
		if ((selection != null) && (!selection.equals("Default"))) {
			removeButton.setEnabled(true);
			editButton.setEnabled(true);
		} else {
			removeButton.setEnabled(false);
			editButton.setEnabled(false);
		}
	}

	/**
	 * @return The selection.
	 */
	public String getSelection() {
		return selection;
	}

	public boolean isAlwaysAskSelected() {
		return checkBox.isSelected();
	}
}