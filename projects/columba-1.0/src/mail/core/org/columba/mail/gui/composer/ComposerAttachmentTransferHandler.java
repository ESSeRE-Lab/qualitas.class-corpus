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
package org.columba.mail.gui.composer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.TransferHandler;


/**
 * TransferHandler to import attachments to an email.
 * This transfer handler can only import file lists from the file system.
 * This transfer handler is a not 100% standard way of implementing a TransferHandle,
 * and should only be used by components in the composer frame.
 * @author redsolo
 */
class ComposerAttachmentTransferHandler extends TransferHandler {

    private AttachmentController attachmentController;

    private static final Logger LOG = Logger.getLogger("org.columba.mail.gui.composer");

    /**
     * @param controller the attachment controller to add stuff into.
     */
    public ComposerAttachmentTransferHandler(AttachmentController controller) {
        attachmentController = controller;
    }

    /**
     * Returns true if the one of the flavors is the @link DataFlavor#javaFileListFlavor.
     * @param comp component that is queried if it can import one of the flavors.
     * @param transferFlavors data flavors that the DnD action supports.
     * @return true if the one of the flavors is the javaFileListFlavor data flavor.
     */
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        boolean canImport = false;
        for (int i = 0; (i < transferFlavors.length) && (!canImport); i++) {
            if (transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
                canImport = true;
            }
        }
        return canImport;
    }

    /**
     * Imports files into the Attachment controller.
     * @param comp the component that is importing the data.
     * @param data the data.
     * @return if the files were added to the attachment controller; false otherwise.
     */
    public boolean importData(JComponent comp, Transferable data) {
        boolean dataWasImported = false;

        try {
            List files = (List) data.getTransferData(DataFlavor.javaFileListFlavor);
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {

                attachmentController.addFileAttachment((File) iterator.next());
            }
            dataWasImported = true;
        } catch (UnsupportedFlavorException e) {
            LOG.warning("A transferable with unsupported flavors tried to import data into the attachment gui.");
        } catch (IOException e) {
            LOG.warning("The data that was DnD into the attachment was no longer available.");
        }
        return dataWasImported;
    }
}
