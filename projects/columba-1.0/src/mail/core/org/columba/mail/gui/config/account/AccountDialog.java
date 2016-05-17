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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.help.HelpManager;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.OutgoingItem;
import org.columba.mail.folder.imap.IMAPRootFolder;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.mailchecking.MailCheckingManager;
import org.columba.mail.pop3.POP3Server;
import org.columba.mail.pop3.POP3ServerCollection;
import org.columba.mail.util.MailResourceLoader;

/**
 * Dialog for managing accounts and their settings.
 */
public class AccountDialog extends JDialog implements ActionListener {

    private AccountItem accountItem;

    private IdentityPanel identityPanel;

    private IncomingServerPanel incomingServerPanel;

    private OutgoingServerPanel outgoingServerPanel;

    private SecurityPanel securityPanel;

    private ReceiveOptionsPanel receiveOptionsPanel;

    private SpamPanel spamPanel;

    private JPanel selected = null;

    private JTabbedPane tp;
    private IFrameMediator mediator;

    
    public AccountDialog(IFrameMediator mediator, AccountItem item) {
        super(mediator.getView().getFrame(), true);


        this.mediator = mediator;
        setTitle(MailResourceLoader.getString("dialog", "account",
                "preferences_for")
                + " " + item.getName());
        this.accountItem = item;
        createPanels();
        initComponents();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    protected void createPanels() {
        identityPanel = new IdentityPanel(accountItem);

        receiveOptionsPanel = new ReceiveOptionsPanel(this, accountItem);

        incomingServerPanel = new IncomingServerPanel(this, accountItem,
                receiveOptionsPanel);

        outgoingServerPanel = new OutgoingServerPanel(this, accountItem);

        securityPanel = new SecurityPanel(accountItem.getPGPItem());

        spamPanel = new SpamPanel(mediator, accountItem);
    }

    protected void initComponents() {
        getContentPane().setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        //mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        tp = new JTabbedPane();
        tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tp.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));

        tp.add(MailResourceLoader.getString("dialog", "account", "identity"),
                identityPanel);

        //$NON-NLS-1$
        String incomingServerPanelTitle = MailResourceLoader.getString(
                "dialog", "account", "incomingserver");

        if (accountItem.isPopAccount()) {
            incomingServerPanelTitle += " (POP3)";
        } else {
            incomingServerPanelTitle += " (IMAP4)";
        }

        tp.add(incomingServerPanelTitle, incomingServerPanel);

        tp.add(MailResourceLoader.getString("dialog", "account",
                "receiveoptions"), receiveOptionsPanel);

        OutgoingItem smtpItem = accountItem.getSmtpItem();

        tp.add(MailResourceLoader.getString("dialog", "account",
                "outgoingserver"), outgoingServerPanel);

        //$NON-NLS-1$
        tp.add(MailResourceLoader.getString("dialog", "account", "security"),
                securityPanel);

        // @author: fdietz
        
        tp.add("Spam Filter", spamPanel);

        //$NON-NLS-1$
        mainPanel.add(tp, BorderLayout.CENTER);

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
        getRootPane().registerKeyboardAction(this, "CANCEL",
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        HelpManager.getInstance().enableHelpKey(getRootPane(),
                "configuring_columba");
    }

    protected JPanel createButtonPanel() {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());

        bottom.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        ButtonWithMnemonic cancelButton = new ButtonWithMnemonic(
                MailResourceLoader.getString("global", "cancel"));

        //$NON-NLS-1$ //$NON-NLS-2$
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$

        ButtonWithMnemonic okButton = new ButtonWithMnemonic(MailResourceLoader
                .getString("global", "ok"));

        //$NON-NLS-1$ //$NON-NLS-2$
        okButton.addActionListener(this);
        okButton.setActionCommand("OK"); //$NON-NLS-1$
        okButton.setDefaultCapable(true);
        getRootPane().setDefaultButton(okButton);

        ButtonWithMnemonic helpButton = new ButtonWithMnemonic(
                MailResourceLoader.getString("global", "help"));

        // associate with JavaHelp
        HelpManager.getInstance().enableHelpOnButton(helpButton,
                "configuring_columba");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 6, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(helpButton);

        bottom.add(buttonPanel, BorderLayout.EAST);

        return bottom;
    }

    /**
     * Check if user entered valid data in all panels
     * <p>
     * Note, that we also select the panel.
     * 
     * @return true, if data is valid. false, otherwise
     */
    protected boolean isFinished() {
        boolean result = identityPanel.isFinished();

        if (!result) {
            tp.setSelectedComponent(identityPanel);

            return false;
        }

        result = incomingServerPanel.isFinished();

        if (!result) {
            tp.setSelectedComponent(incomingServerPanel);

            return false;
        }

        result = outgoingServerPanel.isFinished();

        if (!result) {
            tp.setSelectedComponent(outgoingServerPanel);

            return false;
        }

        return true;
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("OK")) //$NON-NLS-1$
        {
            // check if the user entered valid data
            if (!isFinished()) { return; }

            identityPanel.updateComponents(false);
            incomingServerPanel.updateComponents(false);
            receiveOptionsPanel.updateComponents(false);
            outgoingServerPanel.updateComponents(false);
            securityPanel.updateComponents(false);
            spamPanel.updateComponents(false);

            if (accountItem.isPopAccount()) {
                int uid = accountItem.getUid();
                POP3Server server = POP3ServerCollection.getInstance().uidGet(uid);
                // update configuration
                server.updateConfig();
                
            } else {
                // update tree label
                int uid = accountItem.getUid();

                IMAPRootFolder folder = (IMAPRootFolder) FolderTreeModel.getInstance()
                        .getImapFolder(uid);
                folder.updateConfiguration();
            }

            // restart timer
            MailCheckingManager.getInstance()
                    .restartTimer(accountItem.getUid());

            // notify all observers
            MailCheckingManager.getInstance().update();

            setVisible(false);
        } else if (action.equals("CANCEL")) {
            setVisible(false);
        }
    }
}