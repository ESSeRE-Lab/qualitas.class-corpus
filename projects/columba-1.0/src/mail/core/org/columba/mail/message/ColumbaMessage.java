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
package org.columba.mail.message;

import org.columba.ristretto.io.Source;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.Message;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

/**
 * Adds Columba-specific features to the default {@link Message}object found in
 * the Ristretto API.
 * <p>
 * 
 * @author fdietz, tstich
 */
public class ColumbaMessage implements IColumbaMessage {

    protected IColumbaHeader columbaHeader;

    protected Message message;

    protected MimePart bodyPart;

    public ColumbaMessage() {
        this(new ColumbaHeader());
    }

    public ColumbaMessage(IColumbaHeader header) {
        columbaHeader = header;
        message = new Message();

    }

    public ColumbaMessage(Message m) {
        columbaHeader = new ColumbaHeader(m.getHeader());
        message = m;

    }

    public ColumbaMessage(Header header) {
        columbaHeader = new ColumbaHeader(header);
        message = new Message();
        message.setHeader(header);

    }

    public ColumbaMessage(IColumbaHeader h, Message m) {
        columbaHeader = h;

        columbaHeader.setHeader(m.getHeader());
        message = m;
    }

    public ColumbaMessage(ColumbaMessage m) {
        this.columbaHeader = m.columbaHeader;

        this.message = m.message;
        this.bodyPart = m.bodyPart;
    }

    public void setBodyPart(MimePart body) {
        bodyPart = body;
    }

    public void setUID(Object o) {
        if (o != null) {
            columbaHeader.getAttributes().put("columba.uid", o);
        } else {
            columbaHeader.getAttributes().put("columba.uid", "");
        }

        //uid = o;
    }

    public Object getUID() {
        return getHeader().getAttributes().get("columba.uid");
    }

    public MimeTree getMimePartTree() {
        return message.getMimePartTree();
    }

    public void setMimePartTree(MimeTree ac) {
        message.setMimePartTree(ac);
    }

    public void freeMemory() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.ristretto.message.Message#getHeader()
     */
    public IColumbaHeader getHeader() {
        return columbaHeader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.ristretto.message.Message#setHeader(org.columba.ristretto.message.Header)
     */
    public void setHeader(IColumbaHeader h) {
        columbaHeader = h;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.ristretto.message.Message#getBodyPart()
     */
    public MimePart getBodyPart() {
        return bodyPart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.ristretto.message.Message#getMimePart(int)
     */
    public MimePart getMimePart(int number) {
        return message.getMimePart(number);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.ristretto.message.Message#getMimePartCount()
     */
    public int getMimePartCount() {
        return message.getMimePartCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.ristretto.message.Message#getSource()
     */
    public Source getSource() {
        return message.getSource();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.ristretto.message.Message#setHeader(org.columba.ristretto.message.Header)
     */
    public void setHeader(Header h) {
        message.setHeader(h);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.ristretto.message.Message#setSource(org.columba.ristretto.message.io.Source)
     */
    public void setSource(Source source) {
        message.setSource(source);
    }

    public void close() {
        message.close();
    }
}