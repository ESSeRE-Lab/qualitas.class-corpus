/*  ComponentsWindow.java  */

package nik777.aoi;

/*
 * ComponentsWindow: A window/dialog which builds its display from an array
 *			of coponents.
 *
 * Copyright (C) 200x <author> <location>
 *
 * Author: Nik Trevallyn-Jones, nik777@users.sourceforge.net
 * $Id: Exp $
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See version 2 of the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with this program. If not, version 2 of the license is available
 * from the GNU project, at http://www.gnu.org.
 */

/*
 * This is a derivative work, based (strongly) on the ComponentsDialog class
 * in Art Of Illusion, which is
 * Copyright (C) 1999,2000,2002-2004 by Peter Eastman.
 */

import artofillusion.ui.Translate;
import artofillusion.ui.UIUtilities;

import buoy.event.*;
import buoy.widget.*;
import java.awt.*;

/** 
 * A ComponentsWindow is a modal dialog containing a line of text, and one or
 * more Widgets for the user to edit.  Each Widget has a label next to it. 
 * At the bottom are two buttons labeled OK and Cancel.
 */
   
public class ComponentsWindow extends BDialog
{
    protected Widget comp[];
    protected boolean ok;
    protected Runnable okCallback, cancelCallback;
    protected BButton okButton, cancelButton;

    /**
     *  Create a modal dialog containing a set of labeled components.
     *
     *  @param parent       the parent of the dialog
     *  @param prompt       a text string to appear at the top of the dialog
     *  @param components   the list of components to display
     *  @param labels       the list of labels for each component
     */
    public ComponentsWindow(WindowWidget parent, String prompt,
			    Widget components[], String labels[])
    { this(parent, prompt, components, labels, null, null); }

    /**
     *  Create a non-modal dialog containing a set of labeled components.
     *
     *  @param parent       the parent of the dialog
     *  @param prompt       a text string to appear at the top of the dialog
     *  @param components   the list of components to display
     *  @param labels       the list of labels for each component
     *  @param onOK         a callback to execute when the user clicks OK
     *  @param onCancel     a callback to execute when the user clicks Cancel
     */
    public ComponentsWindow(WindowWidget parent, String prompt,
			    Widget components[], String labels[],
			    Runnable onOK, Runnable onCancel)
    {
	super(parent, (onOK == null && onCancel == null));
	init(prompt, components, labels, parent, onOK, onCancel);
    }

    /**
     */
    public ComponentsWindow(String prompt, Widget components[],
			    String labels[])
    {
	super();
	init(prompt, components, labels, null, null, null);
    }

    /**
     */
    public ComponentsWindow(String prompt, Widget components[],
			    String labels[], Runnable onOK, Runnable onCancel)
    {
	super();
	init(prompt, components, labels, null, onOK, onCancel);
    }

    /**
     *  initialise the display
     */
    protected void init(String prompt, Widget components[], String labels[],
			WindowWidget parent, Runnable onOK, Runnable onCancel)
    {
	if (onOK == null && onCancel == null) setModal(true);

	comp = components;
	okCallback = onOK;
	cancelCallback = onCancel;
	BorderContainer content = new BorderContainer();
	setContent(content);
	content.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(10, 10, 10, 10), null));
	content.add(new BLabel(prompt), BorderContainer.NORTH);
    
	// Add the Widgets.
	FormContainer center = new FormContainer(new double [] {0.0, 1.0}, new double [components.length]);
	content.add(center, BorderContainer.CENTER);
	for (int i = 0; i < components.length; i++) {
	    if (labels[i] == null)
		center.add(components[i], 0, i, 2, 1, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(2, 0, 2, 0), null));
	    else {
		center.add(new BLabel(labels[i]), 0, i, new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE, new Insets(2, 0, 2, 5), null));
		center.add(components[i], 1, i, new LayoutInfo(LayoutInfo.WEST, LayoutInfo.BOTH, new Insets(2, 0, 2, 0), null));
	    }
	    components[i].addEventLink(KeyPressedEvent.class, this, "keyPressed");
	}
    
	// Add the buttons at the bottom.
	RowContainer buttons = new RowContainer();
	content.add(buttons, BorderContainer.SOUTH);
	buttons.add(okButton = Translate.button("ok", this, "buttonPressed"));
	buttons.add(cancelButton = Translate.button("cancel", this, "buttonPressed"));
	okButton.addEventLink(KeyPressedEvent.class, this, "keyPressed");
	cancelButton.addEventLink(KeyPressedEvent.class, this, "keyPressed");
	addEventLink(WindowClosingEvent.class, new Object() {
		void processEvent() {
		    ok = false;
		    closeWindow();
		}
	    } );
	setDefaultButton(okButton);
	pack();
	setResizable(false);

	if (parent != null) UIUtilities.centerDialog(this, parent);

	setVisible(true);
    }
  
    /**
     *  Return true if the user clicked OK, false if they clicked Cancel.
     */
    public boolean clickedOk()
    { return ok; }
  
    /**
     * Set whether the OK button is enabled.
     */
    public void setOkEnabled(boolean enabled)
    { okButton.setEnabled(enabled); }

    protected void buttonPressed(CommandEvent e)
    {
	String command = e.getActionCommand();

	if (command.equals("cancel"))
	    ok = false;
	else
	    ok = true;
	closeWindow();
    }
  
    protected void closeWindow()
    {
	if (ok && okCallback != null)
	    okCallback.run();
	if (!ok && cancelCallback != null)
	    cancelCallback.run();

	dispose();

	for (int i = 0; i < comp.length; i++)
	    comp[i].removeEventLink(KeyPressedEvent.class, this);
    }
    
  /**
   * Pressing Return and Escape are equivalent to clicking OK and Cancel.
   */
    protected void keyPressed(KeyPressedEvent ev)
    {
	int code = ev.getKeyCode();
	if (code == KeyPressedEvent.VK_ESCAPE)
	    closeWindow();
    }
}
