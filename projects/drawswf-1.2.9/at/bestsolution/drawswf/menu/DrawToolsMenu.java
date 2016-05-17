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

import java.awt.Color;
import java.net.URL;
import java.util.ResourceBundle;


import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.DrawObjectList;
import at.bestsolution.drawswf.IconProvider;
import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.actions.AbstractDrawAction;
import at.bestsolution.drawswf.actions.ObjectListAction;
import at.bestsolution.drawswf.actions.OpenOptionsDialogAction;
import at.bestsolution.drawswf.actions.SetCanvasSizeAction;
import at.bestsolution.drawswf.actions.SetDrawing2GradientAction;
import at.bestsolution.drawswf.actions.SetDrawingModeAction;
import at.bestsolution.drawswf.actions.SetLineWidthAction;
import at.bestsolution.drawswf.drawobjects.DrawObjectFactory;
import at.bestsolution.ext.swing.AlphaColorJMenuItem;
import at.bestsolution.ext.swing.GradientJRadioButtonMenuItem;

/**
 * @author tom
 */
public class DrawToolsMenu extends JMenu implements DrawMenuInterface
{
	private JRadioButtonMenuItem[] menubarRadioButtons_;
	private DrawObjectList draw_object_list_;
	protected static final String menu_bar_icon_path_ = "at/bestsolution/drawswf/images/16x16/";

	public DrawToolsMenu(String label, char mnemonic, DrawObjectList draw_object_list)
	{
		super(label);
		draw_object_list_ = draw_object_list;
		menubarRadioButtons_ = new JRadioButtonMenuItem[DrawObjectFactory.MAX_OBJECTS + 2];

		setMnemonic(mnemonic);
		initMenu();
	}

	//----------------------------------------------------------------------------
	private void initMenu()
	{
		JMenu submenu;
		JRadioButtonMenuItem rbMenuItem;
		ButtonGroup menu_group;

		ResourceBundle international = MainWindow.getI18n();

		submenu = new JMenu(international.getString("MainWindowToolsSubDrawingObj"));
		submenu.setMnemonic(international.getString("MainWindowToolsSubDrawingObjMn").charAt(0));

		menu_group = new ButtonGroup();

		addDrawObject(
			submenu,
			international.getString("MainWindowToolsSubDrawingObjItemEdit"),
			international.getString("MainWindowToolsSubDrawingObjItemEditTooltip"),
			"arrow.png",
			international.getString("MainWindowToolsSubDrawingObjItemEditMn").charAt(0),
			null,
			DrawObjectFactory.MAX_OBJECTS);
		menu_group.add(menubarRadioButtons_[DrawObjectFactory.MAX_OBJECTS]);
		menubarRadioButtons_[DrawObjectFactory.MAX_OBJECTS].setSelected(true);

		addDrawObject(
			submenu,
			international.getString("MainWindowToolsSubDrawingObjItemLine"),
			international.getString("MainWindowToolsSubDrawingObjItemLineTooltip"),
			"free_line.png",
			international.getString("MainWindowToolsSubDrawingObjItemLineMn").charAt(0),
			null,
			DrawObjectFactory.LINE);
		menu_group.add(menubarRadioButtons_[0]);

		addDrawObject(
			submenu,
			international.getString("MainWindowToolsSubDrawingObjItemSLine"),
			international.getString("MainWindowToolsSubDrawingObjItemSLineTooltip"),
			"straight_line.png",
			international.getString("MainWindowToolsSubDrawingObjItemSLineMn").charAt(0),
			null,
			DrawObjectFactory.STRAIGHT_LINE);
		menu_group.add(menubarRadioButtons_[1]);

		addDrawObject(
			submenu,
			international.getString("MainWindowToolsSubDrawingObjItemRect"),
			international.getString("MainWindowToolsSubDrawingObjItemRectTooltip"),
			"rectangle.png",
			international.getString("MainWindowToolsSubDrawingObjItemRectMn").charAt(0),
			null,
			DrawObjectFactory.RECTANGLE);
		menu_group.add(menubarRadioButtons_[2]);

		addDrawObject(
			submenu,
			international.getString("MainWindowToolsSubDrawingObjItemEll"),
			international.getString("MainWindowToolsSubDrawingObjItemEllTooltip"),
			"ellipse.png",
			international.getString("MainWindowToolsSubDrawingObjItemEllMn").charAt(0),
			null,
			DrawObjectFactory.ELLIPSE);
		menu_group.add(menubarRadioButtons_[3]);

		addDrawObject(
			submenu,
			international.getString("MainWindowToolsSubDrawingObjItemText"),
			international.getString("MainWindowToolsSubDrawingObjItemTextTooltip"),
			"font.png",
			international.getString("MainWindowToolsSubDrawingObjItemTextMn").charAt(0),
			null,
			DrawObjectFactory.TEXT);
		menu_group.add(menubarRadioButtons_[4]);

		addDrawObject(
			submenu,
			international.getString("MainWindowToolsSubDrawingObjItemPict"),
			international.getString("MainWindowToolsSubDrawingObjItemPictTooltip"),
			"picture.png",
			international.getString("MainWindowToolsSubDrawingObjItemPictMn").charAt(0),
			null,
			DrawObjectFactory.PICTURE);
		menu_group.add(menubarRadioButtons_[5]);

/*		menubarRadioButtons_[DrawObjectFactory.MAX_OBJECTS + 1] =
			createGradientRadioButton(
				international.getString("MainWindowToolsSubDrawingObjItemGradient"),
				international.getString("MainWindowToolsSubDrawingObjItemGradientTooltip"),
				DrawObjectFactory.MAX_OBJECTS + 1,
				international.getString("MainWindowToolsSubDrawingObjItemGradientMn").charAt(0),
				null,
				DrawObjectFactory.MAX_OBJECTS + 1);
		submenu.add(menubarRadioButtons_[DrawObjectFactory.MAX_OBJECTS + 1]);
		menu_group.add(menubarRadioButtons_[DrawObjectFactory.MAX_OBJECTS + 1]);
*/
		add(submenu);

		submenu = new JMenu(international.getString("MainWindowToolsSubColors"));
		submenu.setMnemonic(international.getString("MainWindowToolsSubColorsMn").charAt(0));
		addSetColorAction(
			submenu,
			international.getString("MainWindowToolsSubColorsItemPencolor"),
			international.getString("MainWindowToolsSubColorsItemPencolorTooltip"),
			"pen_color.png",
			international.getString("MainWindowToolsSubColorsItemPencolorMn").charAt(0),
			null,
			"pen_color",
			Color.BLACK,
			7);
		addSetColorAction(
			submenu,
			international.getString("MainWindowToolsSubColorsItemFillcolor"),
			international.getString("MainWindowToolsSubColorsItemFillcolorTooltip"),
			"fill_color.png",
			international.getString("MainWindowToolsSubColorsItemFillcolorMn").charAt(0),
			null,
			"fill_color",
			Color.RED,
			2);

		add(submenu);

		addSetLineWidthAction(
			international.getString("MainWindowToolsItemLinewidth"),
			international.getString("MainWindowToolsItemLinewidthTooltip"),
			"line_width.png",
			international.getString("MainWindowToolsItemLinewidthMn").charAt(0),
			null);
		addSetCanvasSizeAction(
			international.getString("MainWindowToolsItemCanvassize"),
			international.getString("MainWindowToolsItemCanvassizeTooltip"),
			"canvas_size.png",
			international.getString("MainWindowToolsItemCanvassizeMn").charAt(0),
			null);

		ObjectListAction object_list_action =
			new ObjectListAction(
				international.getString("MainWindowToolsItemObjList"),
				international.getString("MainWindowToolsItemObjListTooltip"),
				"open.png",
				MainWindow.getDrawingPanel(),
				international.getString("MainWindowToolsItemObjListMn").charAt(0),
				draw_object_list_);
		add(object_list_action);

		OpenOptionsDialogAction options_action =
			new OpenOptionsDialogAction(
				international.getString("MainWindowToolsItemOpenOptions"),
				international.getString("MainWindowToolsItemOpenOptionsTooltip"),
				"properties.gif",
				MainWindow.getDrawingPanel(),
				international.getString("MainWindowToolsItemOpenOptionsMn").charAt(0),
				null);
		add(options_action);
	}

	//----------------------------------------------------------------------------
	private void addSetColorAction(
		JMenu menu,
		String displayedText,
		String description,
		String icon_name,
		int mnemonicKey,
		KeyStroke accelerator,
		String name,
		Color color,
		int border_v)
	{

		// AlphaColorJMenuItem item = new AlphaColorJMenuItem(color,20,20,2,border_v);

		AlphaColorJMenuItem item = new AlphaColorJMenuItem(IconProvider.getInstance(), name, color);
		item.setText(displayedText);
		item.setToolTipText(description);
		item.setName(name);

		item.setMnemonic(mnemonicKey);
		item.setAccelerator(accelerator);
		item.addChangeListener(MainWindow.getDrawingPanel());

		menu.add(item);
	}

	//----------------------------------------------------------------------------
	private void addDrawObject(JMenu menu, String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator, int mode)
	{
		menubarRadioButtons_[mode] = createMenuRadioButton(displayedText, description, icon_name, mode, mnemonicKey, accelerator, mode);
		menu.add(menubarRadioButtons_[mode]);
	}

	//----------------------------------------------------------------------------
	private void addSetLineWidthAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
	{
		SetLineWidthAction line_width_action = new SetLineWidthAction(displayedText, description, icon_name, MainWindow.getDrawingPanel(), mnemonicKey, accelerator);
		add(line_width_action);
	}

	//----------------------------------------------------------------------------
	private void addSetCanvasSizeAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
	{
		SetCanvasSizeAction canvas_action =
			new SetCanvasSizeAction(displayedText, description, icon_name, MainWindow.getDrawingPanel(), MainWindow.MAIN_WINDOW, mnemonicKey, accelerator);
		add(canvas_action);
	}

	//----------------------------------------------------------------------------
	private JRadioButtonMenuItem createMenuRadioButton(
		String displayedText,
		String description,
		String icon_name,
		int drawing_mode,
		int mnemonicKey,
		KeyStroke accelerator,
		int buttonIndex)
	{

		ImageIcon icon;
		URL icon_url;
		JRadioButtonMenuItem rbMenuItem;

		icon_url = getClass().getClassLoader().getResource(MainWindow.getImagePath() + "16x16/" + icon_name);
		icon = new ImageIcon(icon_url);

		SetDrawingModeAction draw_action =
			new SetDrawingModeAction(displayedText, description, MainWindow.getDrawingPanel(), drawing_mode, mnemonicKey, accelerator, MainWindow.MAIN_WINDOW, buttonIndex);
		rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
		rbMenuItem.setAction(draw_action);
		rbMenuItem.setIcon(icon);

		return rbMenuItem;
	}

	//	----------------------------------------------------------------------------
	private JRadioButtonMenuItem createGradientRadioButton(String displayedText, String description, int drawing_mode, int mnemonicKey, KeyStroke accelerator, int buttonIndex)
	{
		GradientJRadioButtonMenuItem button = new GradientJRadioButtonMenuItem( IconProvider.getInstance(), "fill_gradient" );
		button.addActionListener(new SetDrawing2GradientAction(MainWindow.getDrawingPanel(), drawing_mode, MainWindow.MAIN_WINDOW, buttonIndex, true));
		// NOT NEEDED BECAUSE CHANGE LISTENER ALREADY REGISTERED
		// WITHIN TOOLBAR
		// button.addChangeListener(MainWindow.getDrawingPanel());

		return button;
	}

	public void changeDrawingType(int index)
	{
		menubarRadioButtons_[index].setSelected(true);
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
			System.err.println( "NOT IMPLEMENTED" );
	}

}
