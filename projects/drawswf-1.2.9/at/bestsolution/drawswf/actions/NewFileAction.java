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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/actions/NewFileAction.java,v 1.2 2003/06/08 11:54:18 tom Exp $
 */
package at.bestsolution.drawswf.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.drawswf.AboutWindow;
import at.bestsolution.drawswf.GenericFileFilter;
import at.bestsolution.drawswf.MainWindow;

/**
 *
 * @author  heli
 */
public class NewFileAction extends AbstractDrawAction
{
	private AboutWindow about_window_;
	private JFileChooser file_chooser_ = null;

	//----------------------------------------------------------------------------
	public NewFileAction(String description, String icon_name, DrawingPanel drawing_panel)
	{
		super(description, tool_bar_icon_path + icon_name, drawing_panel);
		about_window_ = null;
	}

	//----------------------------------------------------------------------------
	public NewFileAction(String displayedText, String description, String icon_name, DrawingPanel drawing_panel, int mnemonicKey, KeyStroke accelerator)
	{
		super(displayedText, description, menu_bar_icon_path + icon_name, drawing_panel, mnemonicKey, accelerator);
	}

	//	----------------------------------------------------------------------------
	private File checkFileExtension(File file)
	{
		if (!file.exists() && !file.getName().toLowerCase().endsWith(".drawswf"))
		{
			file = new File(file.getAbsolutePath() + ".drawSWF");
		}

		return file;
	}

	//----------------------------------------------------------------------------
	public void actionPerformed(ActionEvent action_event)
	{
		if (JOptionPane.showConfirmDialog(MainWindow.MAIN_WINDOW, "Save before closing?", "Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0)
		{
			if( DrawingPanel.getStorageFile() != null )
			{
				drawing_panel_.saveAnimation();
			}
			else
			{
				if (file_chooser_ == null)
				{
					file_chooser_ = new JFileChooser();
					file_chooser_.addChoosableFileFilter(new GenericFileFilter("DrawSWF Animation (*.drawSWF)", "drawswf"));
				}

				if (file_chooser_.showSaveDialog(drawing_panel_) == JFileChooser.APPROVE_OPTION)
				{
					File selected_file = checkFileExtension(file_chooser_.getSelectedFile());
					drawing_panel_.saveAnimation(selected_file);
				}
			}
		}

		drawing_panel_.clear4newFile();
	}
}
