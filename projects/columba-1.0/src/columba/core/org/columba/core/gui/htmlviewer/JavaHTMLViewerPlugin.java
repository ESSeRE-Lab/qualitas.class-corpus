package org.columba.core.gui.htmlviewer;

import java.awt.Insets;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.columba.core.io.DiskIO;

public class JavaHTMLViewerPlugin extends JTextPane implements
		IHTMLViewerPlugin {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.htmlviewer");

	private HTMLEditorKit htmlEditorKit;

	private AsynchronousHTMLDocument doc;
	
	
	public JavaHTMLViewerPlugin() {
		super();

		setMargin(new Insets(5, 5, 5, 5));
		setEditable(false);

		htmlEditorKit = new HTMLEditorKit();
		setEditorKit(htmlEditorKit);

		setContentType("text/html");

	}

	/*
	public void view(String htmlSource) {

		setText(htmlSource);

		postView();
	}*/

	private void postView() {
		// setup base url in order to be able to display images
		// in html-component
		URL baseUrl = DiskIO.getResourceURL("org/columba/core/images/");

		((HTMLDocument) getDocument()).setBase(baseUrl);

		// scroll window to the beginning
		setCaretPosition(0);
	}

	public void view(String text) {
		if( text == null) return;
		
		doc = new AsynchronousHTMLDocument();
		
        Reader rd = new StringReader(text);
        try {
			htmlEditorKit.read(rd, doc, 0);		
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setDocument(doc);
		
		postView();		
	}
	
	public JComponent getView() {
		return this;
	}

	/**
	 * Setting HTMLDocument to be an asynchronize model.
	 * <p>
	 * JTextPane therefore uses a background thread to display the message. This
	 * dramatically improves the performance of displaying a message.
	 * <p>
	 * Trick is to overwrite the getAsynchronousLoadPriority() to return a
	 * decent value.
	 * 
	 * @author fdietz
	 */
	public class AsynchronousHTMLDocument extends HTMLDocument {

		/**
		 * 
		 */
		public AsynchronousHTMLDocument() {
			super();
			putProperty("IgnoreCharsetDirective", new Boolean(true));
		}

		/**
		 * From the JDK1.4 reference:
		 * <p>
		 * This may load either synchronously or asynchronously depending upon
		 * the document returned by the EditorKit. If the Document is of type
		 * AbstractDocument and has a value returned by
		 * AbstractDocument.getAsynchronousLoadPriority that is greater than or
		 * equal to zero, the page will be loaded on a separate thread using
		 * that priority.
		 * 
		 * @see javax.swing.text.AbstractDocument#getAsynchronousLoadPriority()
		 */
		public int getAsynchronousLoadPriority() {
			return 10;
		}

		public String getTextWithLineBreaks(int start, int end) throws BadLocationException {
			StringBuffer result = new StringBuffer(end - start);
			ElementIterator iter = new ElementIterator(this);

			// First find the beginning element
			for (iter.next(); iter.current() != null; iter.next()) {
				Element e = iter.current();
				if (e.isLeaf() && (e.getStartOffset() >= start || e.getEndOffset() >= start) && e.getStartOffset() <= end) { 
					Object a = e.getAttributes().getAttribute(StyleContext.NamedStyle.NameAttribute);					
					if( a == HTML.Tag.CONTENT) {
						int as = Math.max(e.getStartOffset(), start);
						int ae = Math.min(e.getEndOffset(),  end);
						result.append(super.getText(as, ae-as));
					} 
					if( a == HTML.Tag.BR ) {					
						result.append("\n");
					}
				}
			}

			return result.toString();
		}
	}

	public boolean initialized() {
		return true;
	}

	/**
	 * @see javax.swing.text.JTextComponent#getSelectedText()
	 */
	public String getSelectedText() {
		try {
			return doc.getTextWithLineBreaks(getSelectionStart(),getSelectionEnd());
		} catch (BadLocationException e) {
			return "";
		}
	}

}
