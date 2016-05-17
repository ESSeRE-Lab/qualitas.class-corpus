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
package org.columba.mail.gui.message.command;

import java.io.File;
import java.util.logging.Logger;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.base.Semaphore;
import org.columba.core.util.TempFileStore;
import org.columba.ristretto.message.MimeHeader;


/**
 * A command that saves an attachment to a temp folder.
 *
 * @author redsolo
 */
public class SaveAttachmentTemporaryCommand extends SaveAttachmentCommand {

    private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.message.attachment.command");

    /** The file that the attachment was saved too. */
    private File tempAttachmentFile;

    private Semaphore commandSemaphore;

    /**
     * @param reference Command reference.
     */
    public SaveAttachmentTemporaryCommand(ICommandReference reference) {
        super(reference);
        commandSemaphore = new Semaphore(true);
    }

    /** {@inheritDoc} */
    protected File getDestinationFile(MimeHeader header) {

        tempAttachmentFile = null;
        String filename = getFilename(header);
        if (filename != null) {
            tempAttachmentFile = TempFileStore.createTempFile(filename);
        }
        return tempAttachmentFile;
    }

    /** {@inheritDoc} */
    public void execute(IWorkerStatusController worker) throws Exception {
        super.execute(worker);
        commandSemaphore.release();
    }

    /**
     * Returns the temporary file that the attachment was saved to.
     * If its null, then the file hasnt been saved.
     * @return Returns the tempAttachmentFile.
     */
    public File getTempAttachmentFile() {
        return tempAttachmentFile;
    }

    /**
     * Waits until the command has completed saving the file to the temporary folder.
     */
    public void waitForCommandToComplete() {
        try {
            commandSemaphore.waitUntilReleased();
        } catch (InterruptedException e) {
            LOG.warning("The thread waiting for the Save Attachment Temporary command was interrupted.");
        }
    }
}
