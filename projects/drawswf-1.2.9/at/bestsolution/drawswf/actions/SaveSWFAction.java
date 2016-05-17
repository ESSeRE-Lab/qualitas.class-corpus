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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/actions/SaveSWFAction.java,v 1.12 2004/03/24 15:39:10 tom Exp $
 */
package at.bestsolution.drawswf.actions;

import java.awt.event.ActionEvent;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.drawswf.GenericFileFilter;
import at.bestsolution.drawswf.FlashGenerator;

/**
 *
 * @author  heli
 */
public class SaveSWFAction extends AbstractDrawAction
{
    private static JFileChooser file_chooser_;
    
    //----------------------------------------------------------------------------
    public SaveSWFAction(String description, String icon_name, DrawingPanel drawing_panel)
    {
        super(description, tool_bar_icon_path + icon_name, drawing_panel);
    }
    
    //----------------------------------------------------------------------------
    public SaveSWFAction(String displayedText, String description, String icon_name, DrawingPanel drawing_panel, int mnemonicKey, KeyStroke accelerator)
    {
        super(displayedText, description, menu_bar_icon_path + icon_name, drawing_panel, mnemonicKey, accelerator);
    }
    
    //----------------------------------------------------------------------------
    private File checkFileExtension(File file)
    {
        if ( !file.exists() && !file.getName().toLowerCase().endsWith(".swf"))
        {
            file = new File(file.getAbsolutePath() + ".swf");
        }
        
        return file;
    }
    
    //----------------------------------------------------------------------------
    public void actionPerformed(ActionEvent action_event)
    {
        Object[] speed_values=
        { "1", "2", "3", "4", "5", "10"};
        String selected_value = (String) JOptionPane.showInputDialog(null, MainWindow.getI18n().getString("SaveSWFActionTitle"), MainWindow.getI18n().getString("SaveSWFActionTitle"), JOptionPane.INFORMATION_MESSAGE, null, speed_values, speed_values[1]);
        
        if (selected_value != null)
        {
            int speed = Integer.parseInt(selected_value);
            
            if ( file_chooser_ == null )
            {
                file_chooser_ = new JFileChooser();
                file_chooser_.addChoosableFileFilter(new GenericFileFilter("Flash Animation (*.swf)", "swf"));
            }
            if ( file_chooser_.showSaveDialog(drawing_panel_) == JFileChooser.APPROVE_OPTION )
            {
                FlashGenerator generator = new FlashGenerator(drawing_panel_.getCanvasSize(), speed);
                File selected_file = checkFileExtension(file_chooser_.getSelectedFile());
                generator.generateFile( selected_file.getPath(), drawing_panel_.getLines() );
            }
        }
    }
}
