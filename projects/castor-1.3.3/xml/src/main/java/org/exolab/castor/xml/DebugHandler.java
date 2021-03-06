/*
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio, Inc.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio, Inc. Exolab is a registered
 *    trademark of Intalio, Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id: DebugHandler.java 6784 2007-01-29 03:29:17Z ekuns $
 */
package org.exolab.castor.xml;

import java.io.PrintWriter;
import java.io.Writer;

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;

/**
 * A Simple SAX1 DocumentHandler that intercepts SAX events and prints them to
 * the console. This class is not used during normal Castor operation, but
 * exists so that during debugging one can replace a normal DocumentHandler with
 * this one (which will proxy to the correct DocumentHandler).
 * <p>
 * FIXME:  As Castor moves internally to the SAX2 interface, this class should
 * also be updated for SAX2.
 *
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision: 6784 $ $Date: 2003-03-03 00:05:44 -0700 (Mon, 03 Mar 2003) $
 */
public class DebugHandler implements DocumentHandler {

    /** The writer to report events to. */
    private Writer          _out     = null;
    /** The DocumentHandler to forward events to. */
    private DocumentHandler _handler = null;

    /**
     * Creates a new DebugHandler which forwards events to the given document
     * handler.
     *
     * @param handler the DocumentHandler to forward events to
     */
    public DebugHandler(final DocumentHandler handler) {
        this(handler, null);
    }

    /**
     * Creates a new DebugHandler which forwards events to the given document
     * handler.
     *
     * @param handler the DocumentHandler to forward events to
     * @param out the Writer to print debug information to
     */
    public DebugHandler(final DocumentHandler handler, final Writer out) {
        if (out == null) {
            this._out = new PrintWriter(System.out);
        }
        this._handler = handler;
    }

    /**
     * {@inheritDoc}
     * Proxies the org.sax.xml.DocumentHandler#characters(char[], int, int)
     * request to the proxy that is provided, printing debugging information
     * before doing the proxy.
     */
    public void characters(final char[] ch, final int start, final int length) throws org.xml.sax.SAXException {
        try {
            _out.write(ch, start, length);
            _out.flush();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }

        if (_handler != null) {
            _handler.characters(ch, start, length);
        }
    }

    /**
     * {@inheritDoc} Proxies the org.sax.xml.DocumentHandler#endDocument()
     * request to the proxy that is provided, printing debugging information
     * before doing the proxy.
     */
    public void endDocument() throws org.xml.sax.SAXException {
        try {
            _out.write("#endDocument\n");
            _out.flush();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }

        if (_handler != null) {
            _handler.endDocument();
        }
    }

    /**
     * {@inheritDoc} Proxies the org.sax.xml.DocumentHandler#endElement(String)
     * request to the proxy that is provided, printing debugging information
     * before doing the proxy.
     */
    public void endElement(final String name) throws org.xml.sax.SAXException {
        try {
            _out.write("</");
            _out.write(name);
            _out.write(">\n");
            _out.flush();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }

        if (_handler != null) {
            _handler.endElement(name);
        }
    }

    /**
     * {@inheritDoc} Proxies the
     * org.sax.xml.DocumentHandler#ignorableWhitespace(char[], int, int) request
     * to the proxy that is provided, printing debugging information before
     * doing the proxy.
     */
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws org.xml.sax.SAXException {
        if (_handler != null) {
            _handler.ignorableWhitespace(ch, start, length);
        }
    }

    /**
     * {@inheritDoc} Proxies the
     * org.sax.xml.DocumentHandler#processingInstruction(String, String) request
     * to the proxy that is provided, printing debugging information before
     * doing the proxy.
     */
    public void processingInstruction(final String target, final String data) throws org.xml.sax.SAXException {
        try {
            _out.write("--#processingInstruction\n");
            _out.write("target: ");
            _out.write(target);
            _out.write(" data: ");
            _out.write(data);
            _out.write('\n');
            _out.flush();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }

        if (_handler != null) {
            _handler.processingInstruction(target, data);
        }
    }

    /**
     * {@inheritDoc} Proxies the
     * org.sax.xml.DocumentHandler#setDocumentLocator(Locator) request to the
     * proxy that is provided, printing debugging information before doing the
     * proxy.
     */
    public void setDocumentLocator(final Locator locator) {
        if (_handler != null) {
            _handler.setDocumentLocator(locator);
        }
    }

    /**
     * {@inheritDoc} Proxies the org.sax.xml.DocumentHandler#startDocument()
     * request to the proxy that is provided, printing debugging information
     * before doing the proxy.
     */
    public void startDocument() throws org.xml.sax.SAXException {
        try {
            _out.write("#startDocument\n");
            _out.flush();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }

        if (_handler != null) {
            _handler.startDocument();
        }
    }

    /**
     * {@inheritDoc} Proxies the
     * org.sax.xml.DocumentHandler#startElement(String, AttributeList) request
     * to the proxy that is provided, printing debugging information before
     * doing the proxy.
     */
    public void startElement(final String name, final AttributeList atts) throws org.xml.sax.SAXException {
        try {
            _out.write('<');
            _out.write(name);
            if (atts != null && atts.getLength() > 0) {
                for (int i = 0; i < atts.getLength(); i++) {
                    _out.write(' ');
                    _out.write(atts.getName(i));
                    _out.write("=\"");
                    _out.write(atts.getValue(i));
                    _out.write("\"");
                }
            }
            _out.write(">\n");
            _out.flush();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }

        if (_handler != null) {
            _handler.startElement(name, atts);
        }
    }

}
