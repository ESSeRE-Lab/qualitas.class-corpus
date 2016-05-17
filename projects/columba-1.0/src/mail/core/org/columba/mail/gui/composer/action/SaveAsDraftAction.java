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
package org.columba.mail.gui.composer.action;

import java.awt.event.ActionEvent;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.mail.command.ComposerCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.SpecialFoldersItem;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.command.ExpungeFolderCommand;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.composer.command.SaveMessageCommand;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.message.Flags;


/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SaveAsDraftAction extends AbstractColumbaAction {
    public SaveAsDraftAction(IFrameMediator frameMediator) {
        super(frameMediator,
            MailResourceLoader.getString("menu", "composer",
                "menu_file_savedraft"));

        // tooltip text
        putValue(SHORT_DESCRIPTION,
            MailResourceLoader.getString("menu", "composer",
                "menu_file_savedraft").replaceAll("&", ""));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        final ComposerController composerController = (ComposerController) getFrameMediator();

        // view data ->model
        composerController.updateComponents(false);
        
        ComposerModel model = ((ComposerModel) composerController.getModel());

        // get selected account
        AccountItem item = model.getAccountItem();
        // get "Drafts" folder of account
        SpecialFoldersItem folderItem = item.getSpecialFoldersItem();
        String str = folderItem.get("drafts");
        int destUid = Integer.parseInt(str);
        IMailbox destFolder = (IMailbox) FolderTreeModel.getInstance().getFolder(destUid);
   
        // check if we are currently editing a draft message
        if ( model.getMessage().getHeader().getFlags().getDraft() ) {
        	// -> we need to replace old message
        	
        	// delete source message
        	MailFolderCommandReference r = model.getSourceReference();
        	if( r != null) {
        		r.setMarkVariant(MarkMessageCommand.MARK_AS_EXPUNGED);
        		CommandProcessor.getInstance().addOp(new MarkMessageCommand(r));
        		CommandProcessor.getInstance().addOp(new ExpungeFolderCommand(r));
        	}
        }
        
        // mark as read, mark as draft
        Flags flags = new Flags();
        flags.setSeen(true);
        flags.setDraft(true);
        model.getMessage().getHeader().setFlags(flags);
        
        // create command reference
        ComposerCommandReference r =  new ComposerCommandReference(composerController, destFolder);
        r.setAppendSignature(false);
        
        // create command
        SaveMessageCommand c = new SaveMessageCommand(r);

        CommandProcessor.getInstance().addOp(c);
    }
}
