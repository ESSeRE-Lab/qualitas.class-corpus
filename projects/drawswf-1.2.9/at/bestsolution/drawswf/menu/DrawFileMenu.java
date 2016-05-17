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
 * Created on 21.02.2003
 *
 */
package at.bestsolution.drawswf.menu;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.actions.AbstractDrawAction;
import at.bestsolution.drawswf.actions.ExitAction;
import at.bestsolution.drawswf.actions.FileAction;
import at.bestsolution.drawswf.actions.GraphicTemplateAction;
import at.bestsolution.drawswf.actions.NewFileAction;
import at.bestsolution.drawswf.actions.SaveAction;
import at.bestsolution.drawswf.actions.SaveSWFAction;

/**
 * @author tom
 */
public class DrawFileMenu extends JMenu implements DrawMenuInterface 
{
	private JMenuItem save_action_;
	 
	public DrawFileMenu(String label, char mnemonic)
	{
		super(label);
		setMnemonic(mnemonic);
		initMenu();
	}

	//	----------------------------------------------------------------------------
	private void addNewFileAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
	{
		NewFileAction new_file_action = new NewFileAction(displayedText, description, icon_name, MainWindow.getDrawingPanel(), mnemonicKey, accelerator);
		add(new_file_action);
	}

	//----------------------------------------------------------------------------
	private void addFileAction(String displayedText, String description, String icon_name, boolean load, int mnemonicKey, KeyStroke accelerator, boolean append)
	{
		FileAction load_action = new FileAction(displayedText, description, icon_name, MainWindow.getDrawingPanel(), load, mnemonicKey, accelerator, append);
		add(load_action);
	}

	private void addSaveAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
	{
		SaveAction save_action = new SaveAction(displayedText, description, icon_name, MainWindow.getDrawingPanel(), mnemonicKey, accelerator);
		save_action_ = add(save_action);
		save_action_.setEnabled(false);
	}

	//----------------------------------------------------------------------------
	private void addGraphicTemplateAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
	{
		GraphicTemplateAction template_action = new GraphicTemplateAction(displayedText, description, icon_name, MainWindow.getDrawingPanel(), mnemonicKey, accelerator);
		add(template_action);
	}

	//----------------------------------------------------------------------------
	private void addSaveSWFAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
	{
		SaveSWFAction save_swf_action = new SaveSWFAction(displayedText, description, icon_name, MainWindow.getDrawingPanel(), mnemonicKey, accelerator);
		add(save_swf_action);
	}

	//  ----------------------------------------------------------------------------
	private void initMenu()
	{
		ResourceBundle international = MainWindow.getI18n();

		addNewFileAction(
			international.getString("MainWindowFileItemNew"),
			international.getString("MainWindowFileItemNewTooltip"),
			"new.png",
			international.getString("MainWindowFileItemNewMn").charAt(0),
			KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		addFileAction(
			international.getString("MainWindowFileItemOpen"),
			international.getString("MainWindowFileItemOpenTooltip"),
			"open.png",
			true,
			international.getString("MainWindowFileItemOpenMn").charAt(0),
			KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
			false);

		addFileAction(
			international.getString("MainWindowFileItemAppend"),
			international.getString("MainWindowFileItemAppendTooltip"),
			"append.png",
			true,
			international.getString("MainWindowFileItemAppendMn").charAt(0),
			null,
			true);

		addFileAction(
			international.getString("MainWindowFileItemSaveAs"),
			international.getString("MainWindowFileItemSaveAsTooltip"),
			"saveas.png",
			false,
			international.getString("MainWindowFileItemSaveAsMn").charAt(0),
			null,
			false);

		addSaveAction(
					international.getString("MainWindowFileItemSave"),
					international.getString("MainWindowFileItemSaveTooltip"),
					"save.png",
					international.getString("MainWindowFileItemSaveMn").charAt(0),
					KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));


		addGraphicTemplateAction(
			international.getString("MainWindowFileItemLoadTemp"),
			international.getString("MainWindowFileItemLoadTempTooltip"),
			"graphic_template.png",
			international.getString("MainWindowFileItemLoadTempMn").charAt(0),
			null);

		addSaveSWFAction(
			international.getString("MainWindowFileItemExport"),
			international.getString("MainWindowFileItemExportTooltip"),
			"export.png",
			international.getString("MainWindowFileItemExportMn").charAt(0),
			KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));

		addSeparator();

		ExitAction exit_action =
			new ExitAction(
				international.getString("MainWindowFileItemExit"),
				international.getString("MainWindowFileItemExitTooltip"),
				"exit.png",
				MainWindow.getDrawingPanel(),
				international.getString("MainWindowFileItemExitMn").charAt(0),
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		add(exit_action);
	}

	/* (non-Javadoc)
	 * @see at.bestsolution.drawswf.menu.DrawMenuInterface#addGenericMenuItem(at.bestsolution.drawswf.actions.AbstractDrawAction)
	 */
	public void addGenericMenuItem(AbstractDrawAction draw_action, int position)
	{
		insert(draw_action, position);
	}

	public void setItemEnabled( String name, boolean enabled )
	{
		if( name.equals( "save_action" ) )
		{
			save_action_.setEnabled( enabled );
		}
		else
		{
			System.err.println( "NOT IMPLEMENTED" );
		}
	}

}
