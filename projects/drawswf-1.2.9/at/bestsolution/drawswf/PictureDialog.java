/*
 *  Copyright (c) 2003
 *  bestsolution EDV Systemhaus GmbH,
 *  http://www.bestsolution.at
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

/*
 * PictureDialog.java
 *
 * Created on 10. Jänner 2003, 15:15
 */

package at.bestsolution.drawswf;

import javax.swing.JFileChooser;

import at.bestsolution.ext.swing.dialog.FilePreviewer;

/**
 *
 * @author  tom
 */
public class PictureDialog extends JFileChooser
{
    /** Creates a new instance of PictureDialog */
    public PictureDialog()
    {
        super();
        
        FilePreviewer previewer = new FilePreviewer(this);
        setAccessory( previewer );
        
        GenericFileFilter filter = new GenericFileFilter( MainWindow.getI18n().getString("PictureDialog"), new String[]{"png", "gif", "jpg"} );
        addChoosableFileFilter( filter );
    }
}
