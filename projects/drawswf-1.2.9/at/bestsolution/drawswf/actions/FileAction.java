/*
 *  Copyright (c) 2002
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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/actions/FileAction.java,v 1.12 2003/06/08 11:34:12 tom Exp $
 */
package at.bestsolution.drawswf.actions;

import java.awt.event.ActionEvent;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.drawswf.GenericFileFilter;

/**
 *
 * @author  heli
 */
public class FileAction extends AbstractDrawAction
{
    private static JFileChooser file_chooser_;
    private boolean load_;
    private boolean append_;
    
    //----------------------------------------------------------------------------
    public FileAction(String description, String icon_name, DrawingPanel drawing_panel, boolean load, boolean append)
    {
        super(description, tool_bar_icon_path + icon_name, drawing_panel);
        load_ = load;
        append_ = append;
    }
    
    //----------------------------------------------------------------------------
    public FileAction(String displayedText, String description, String icon_name, DrawingPanel drawing_panel, boolean load, int mnemonicKey, KeyStroke accelerator, boolean append)
    {
        super( displayedText, description, menu_bar_icon_path + icon_name, drawing_panel, mnemonicKey, accelerator );
        load_ = load;
        append_ = append;
    }
    
    //----------------------------------------------------------------------------
    private File checkFileExtension(File file)
    {
        if ( !file.exists() && !file.getName().toLowerCase().endsWith(".drawswf"))
        {
          file = new File(file.getAbsolutePath() + ".drawSWF");
        }
      
        return file;
    }
  
    //----------------------------------------------------------------------------
    public void actionPerformed(ActionEvent action_event)
    {
        if ( file_chooser_ == null )
        {
            file_chooser_ = new JFileChooser();
            file_chooser_.addChoosableFileFilter(new GenericFileFilter("DrawSWF Animation (*.drawSWF)", "drawswf"));
        }

        if (load_ == true)
        {
            if ( file_chooser_.showOpenDialog(drawing_panel_) == JFileChooser.APPROVE_OPTION )
            {
                drawing_panel_.loadAnimation( file_chooser_.getSelectedFile(), append_ );
            }			
        }
        else
        {
            if ( file_chooser_.showSaveDialog(drawing_panel_) == JFileChooser.APPROVE_OPTION )
            {
                File selected_file = checkFileExtension(file_chooser_.getSelectedFile());
                drawing_panel_.saveAnimation( selected_file );
            }
        }
        
    }
}
