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

package org.columba.mail.folder.mailboximport;

import java.io.File;

import javax.swing.JOptionPane;

import org.columba.api.command.IWorkerStatusController;
import org.columba.api.plugin.IExtensionInterface;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.folder.IMailbox;
import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.io.SourceInputStream;

/**
 * This is the base class for mailbox importers.
 */
public abstract class AbstractMailboxImporter implements IExtensionInterface{
    public static final int TYPE_FILE = 0;
    public static final int TYPE_DIRECTORY = 1;

    protected IMailbox destinationFolder;
    protected File[] sourceFiles;
    protected int counter = 0;

    public AbstractMailboxImporter(IMailbox destinationFolder, File[] sourceFiles) {
        this();
        setDestinationFolder(destinationFolder);
        setSourceFiles(sourceFiles);
    }

    /**
     * Default constructor
     */
    public AbstractMailboxImporter() {}

    /**
     * Override this method to specify type.
     * the wizard dialog will open the correct file/directory dialog automatically
     */
    public int getType() {
        return TYPE_FILE;
    }

    /**
     * Override this method to do the actual import work. In here, the messages
     * should be read and passed to the folder using saveMessage.
     */
    public abstract void importMailboxFile(File file,
        IWorkerStatusController worker, IMailbox destFolder)
        throws Exception;

    /**
     * Override this method to provide an adequate description to the user.
     */
    public abstract String getDescription();

    /*********** intern methods (no need to overwrite these) ****************/
    /**
     * Sets the source files/directories.
     */
    public void setSourceFiles(File[] files) {
        this.sourceFiles = files;
    }

    /**
     * Set the destination folder.
     */
    public void setDestinationFolder(IMailbox folder) {
        destinationFolder = folder;
    }

    /**
     * Returns the number of successfully imported messages so far.
     */
    public int getCount() {
        return counter;
    }

    /**
     * This method calls your overridden importMailbox(File)-method
     * and handles exceptions.
     */
    public void run(IWorkerStatusController worker) {
        //TODO (@author fdietz): i18n
        worker.setDisplayText("Importing messages...");

        importMailbox(worker);

        if (getCount() == 0) {
            //TODO (@author fdietz): i18n
            JOptionPane.showMessageDialog(null,
                "Message import failed! No messages were added to the folder.\n" +
                "This means that the parser didn't throw any exception even if " +
                "it didn't recognize the mailbox format or the messagebox simply " +
                "didn't contain any messages.",
                "Warning", JOptionPane.WARNING_MESSAGE);

            return;
        } else {
            //TODO (@author fdietz): i18n
            JOptionPane.showMessageDialog(null,
                "Message import was successful!", "Information",
                JOptionPane.INFORMATION_MESSAGE,
                ImageLoader.getImageIcon("stock_dialog_info_48.png"));
        }
    }

    /**
     * Import all mailbox files in Columba. This method makes use of the
     * importMailbox method you have to override and simply iterates over all
     * given files/directories.
     *
     * @param worker
     */
    public void importMailbox(IWorkerStatusController worker) {
        File[] listing = getSourceFiles();

        for (int i = 0; i < listing.length; i++) {
            if (worker.cancelled()) {
                return;
            }

            try {
                importMailboxFile(listing[i], worker, getDestinationFolder());
            } catch (Exception ex) {
                //TODO (@author fdietz): i18n
                int result = JOptionPane.showConfirmDialog(
                		FrameManager.getInstance().getActiveFrame(),
                    "An error occured while importing a message. Try again?",
                    "Retry message import?", 
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    i--;
                } else if (result == JOptionPane.CANCEL_OPTION) {
                    worker.cancel();
                }
            }
        }
    }

    /**
     * Use this method to save a message in the specified destination folder.
     */
    protected void saveMessage(String rawString, IWorkerStatusController worker,
    		IMailbox destFolder) throws Exception {
        /*
         * *20031231, karlpeder* Using InputStream instead of rawString
         * directly. Ensures size is set correctly by addMessage (bug #843657)
         */

        SourceInputStream in = new SourceInputStream(new CharSequenceSource(
                    rawString));
        destFolder.addMessage(in);
        in.close();

        counter++;

        //TODO (@author fdietz): i18n
        worker.setDisplayText("Importing messages: " + getCount());
    }

    /**
     * Returns the folder new messages will be added to.
     */
    public IMailbox getDestinationFolder() {
        return destinationFolder;
    }

    /**
     * Returns the source files/directories new messages will be read from.
     */
    public File[] getSourceFiles() {
        return sourceFiles;
    }
}
