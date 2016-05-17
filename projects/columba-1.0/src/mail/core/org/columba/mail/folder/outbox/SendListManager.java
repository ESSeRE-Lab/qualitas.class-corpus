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
package org.columba.mail.folder.outbox;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.mail.composer.SendableMessage;


/**
 * Keeps a list of {@SenableMessage} objects.
 *
 *
 * @author fdietz
 */
public class SendListManager {
	
	private static final java.util.logging.Logger LOG = 
        java.util.logging.Logger.getLogger("org.columba.mail.folder.outbox"); //$NON-NLS-1$
	
    private List sendAccounts;
    private int counter;
    private boolean mutex;

    public SendListManager() {
        sendAccounts = new Vector();
        counter = 0;

        mutex = false;
    }

    private synchronized void getMutex() {
        while (mutex) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        mutex = true;
    }

    private synchronized void releaseMutex() {
        mutex = false;
        notify();
    }

    public void add(SendableMessage message) {
        getMutex();

        SendList actList;
        counter++;

        LOG.info("SMTP_SEND::Adding message in sendlistManager"); //$NON-NLS-1$

        for (Iterator it = sendAccounts.iterator(); it.hasNext();) {
            actList = (SendList) it.next();

            // for( int i=0; i<sendAccounts.size(); i++)
            // {
            // actList = (SendList) sendAccounts.get(i);
            if (message.getAccountUid() == actList.getAccountUid()) {
                actList.add(message);
                releaseMutex();

                return;
            }
        }

        sendAccounts.add(new SendList(message));

        releaseMutex();
    }

    public boolean hasMoreMessages() {
        getMutex();

        boolean output = (counter > 0);

        releaseMutex();

        return output;
    }

    public int count() {
        int output;

        LOG.info("DEBUG"); //$NON-NLS-1$

        getMutex();

        output = counter;

        releaseMutex();

        return output;
    }

    public Object getNextUid() {
        getMutex();

        SendList actList = (SendList) sendAccounts.get(0);
        Object output = actList.getFirst().getUID();

        counter--;

        if (actList.count() == 0) {
            sendAccounts.remove(0);
        }

        releaseMutex();

        return output;
    }

    public SendableMessage getNextMessage() {
        getMutex();

        SendList actList = (SendList) sendAccounts.get(0);
        SendableMessage output = actList.getFirst();

        counter--;

        if (actList.count() == 0) {
            sendAccounts.remove(0);
        }

        releaseMutex();

        return output;
    }
}


class SendList {
    private Vector messages;
    private int accountUid;

    public SendList(SendableMessage message) {
        this.accountUid = message.getAccountUid();

        messages = new Vector();
        messages.add(message);
    }

    public int getAccountUid() {
        return accountUid;
    }

    public void add(SendableMessage message) {
        messages.add(message);
    }

    public SendableMessage getFirst() {
        return (SendableMessage) messages.remove(0);
    }

    public SendableMessage get(int index) {
        return (SendableMessage) messages.get(index);
    }

    public int count() {
        return messages.size();
    }
}
