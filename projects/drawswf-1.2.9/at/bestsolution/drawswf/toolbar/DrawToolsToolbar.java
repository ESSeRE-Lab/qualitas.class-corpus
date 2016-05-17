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
 * Created on 22.02.2003
 *
 */
package at.bestsolution.drawswf.toolbar;

import java.awt.Color;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import at.bestsolution.drawswf.DrawObjectList;
import at.bestsolution.drawswf.IconProvider;
import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.actions.AbstractDrawAction;
import at.bestsolution.drawswf.actions.ObjectListAction;
import at.bestsolution.drawswf.actions.SetCanvasSizeAction;
import at.bestsolution.drawswf.actions.SetDrawing2GradientAction;
import at.bestsolution.drawswf.actions.SetDrawingModeAction;
import at.bestsolution.drawswf.actions.SetLineWidthAction;
import at.bestsolution.drawswf.drawobjects.DrawObjectFactory;
import at.bestsolution.ext.swing.AlphaColorJButton;
import at.bestsolution.ext.swing.GradientJRadioButton;

/**
 * @author tom
 */
public class DrawToolsToolbar extends JToolBar implements DrawToolbarInterface
{
    private JRadioButton[] toolbarRadioButtons_;
    private DrawObjectList draw_object_list_;
    protected static final String tool_bar_icon_path_ = "at/bestsolution/drawswf/images/24x24/";

    public DrawToolsToolbar()
    {
        super();
        //setFloatable(false);
        toolbarRadioButtons_ = new JRadioButton[DrawObjectFactory.MAX_OBJECTS + 2];
        initToolbar();
    }

    //----------------------------------------------------------------------------
    private void addSetColorAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator, String name,  Color color, int border_v)
    {
        AlphaColorJButton button = new AlphaColorJButton( IconProvider.getInstance(), name, color );
        button.setName(name);
        button.addChangeListener( MainWindow.getDrawingPanel() );
        button.setToolTipText(description);
        
        add( button );
    }

    //----------------------------------------------------------------------------
    private void addDrawObject(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator, int mode)
    {
        toolbarRadioButtons_[mode] = createToolbarRadioButton(description, icon_name, mode, mode);
        add(toolbarRadioButtons_[mode]);
    }

    //----------------------------------------------------------------------------
    private void addSetLineWidthAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
    {
        SetLineWidthAction line_width_action = new SetLineWidthAction(description, icon_name, MainWindow.getDrawingPanel());
        add(line_width_action);
    }

    //----------------------------------------------------------------------------
    private void addSetCanvasSizeAction(String displayedText, String description, String icon_name, int mnemonicKey, KeyStroke accelerator)
    {
        SetCanvasSizeAction canvas_action = new SetCanvasSizeAction(description, icon_name, MainWindow.getDrawingPanel(), MainWindow.MAIN_WINDOW);
        add(canvas_action);
    }

    //----------------------------------------------------------------------------
    private JRadioButton createToolbarRadioButton(String description, String icon_name, int drawing_mode, int buttonIndex)
    {
        JRadioButton button;
        ImageIcon icon;
        URL icon_url;

        SetDrawingModeAction draw_action = new SetDrawingModeAction(description, MainWindow.getDrawingPanel(), drawing_mode, MainWindow.MAIN_WINDOW, buttonIndex);
        icon_url = getClass().getClassLoader().getResource(MainWindow.getImagePath() + "24x24/" + icon_name);
        icon = new ImageIcon(icon_url);
        button = new JRadioButton();
        button.setAction(draw_action);
        button.setIcon(icon);

        icon_url = getClass().getClassLoader().getResource(MainWindow.getImagePath() + "24x24/pressed_" + icon_name);
        icon = new ImageIcon(icon_url);
        button.setSelectedIcon(icon);

        return button;
    }
 
	//	----------------------------------------------------------------------------
	private JRadioButton createGradientRadioButton(String description, int drawing_mode, int buttonIndex)
	{
		GradientJRadioButton button = new GradientJRadioButton( IconProvider.getInstance(), "fill_gradient" );
		button.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		button.addActionListener( new SetDrawing2GradientAction(MainWindow.getDrawingPanel(),drawing_mode,MainWindow.MAIN_WINDOW,buttonIndex,true) );
		button.addChangeListener( MainWindow.getDrawingPanel() );
		
//		SetDrawingModeAction draw_action = new SetDrawingModeAction(description, MainWindow.getDrawingPanel(), drawing_mode, MainWindow.MAIN_WINDOW, buttonIndex);
//		MultipleGradientPaint gradient = new LinearGradientPaint(JGradientChooser.START_,JGradientChooser.CENTER_,JGradientChooser.fractions_,JGradientChooser.colors,MultipleGradientPaint.NO_CYCLE,MultipleGradientPaint.SRGB);
//		button = new JRadioButton( new GradientIcon(gradient) );
//		button.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
//		button.setAction(draw_action);
//		
		
		
		return button;
	}

    //----------------------------------------------------------------------------
    private void initToolbar()
    {
        ButtonGroup tool_group;
        ResourceBundle international = MainWindow.getI18n();
        tool_group = new ButtonGroup();

        addDrawObject(
            international.getString("MainWindowToolsSubDrawingObjItemEdit"),
            international.getString("MainWindowToolsSubDrawingObjItemEditTooltip"),
            "arrow.png",
            international.getString("MainWindowToolsSubDrawingObjItemEditMn").charAt(0),
            null,
            DrawObjectFactory.MAX_OBJECTS);
        tool_group.add(toolbarRadioButtons_[DrawObjectFactory.MAX_OBJECTS]);
        toolbarRadioButtons_[DrawObjectFactory.MAX_OBJECTS].setSelected(true);

        addDrawObject(
            international.getString("MainWindowToolsSubDrawingObjItemLine"),
            international.getString("MainWindowToolsSubDrawingObjItemLineTooltip"),
            "free_line.png",
            international.getString("MainWindowToolsSubDrawingObjItemLineMn").charAt(0),
            null,
            DrawObjectFactory.LINE);
        tool_group.add(toolbarRadioButtons_[0]);

        addDrawObject(
            international.getString("MainWindowToolsSubDrawingObjItemSLine"),
            international.getString("MainWindowToolsSubDrawingObjItemSLineTooltip"),
            "straight_line.png",
            international.getString("MainWindowToolsSubDrawingObjItemSLineMn").charAt(0),
            null,
            DrawObjectFactory.STRAIGHT_LINE);
        tool_group.add(toolbarRadioButtons_[1]);

        addDrawObject(
            international.getString("MainWindowToolsSubDrawingObjItemRect"),
            international.getString("MainWindowToolsSubDrawingObjItemRectTooltip"),
            "rectangle.png",
            international.getString("MainWindowToolsSubDrawingObjItemRectMn").charAt(0),
            null,
            DrawObjectFactory.RECTANGLE);
        tool_group.add(toolbarRadioButtons_[2]);

        addDrawObject(
            international.getString("MainWindowToolsSubDrawingObjItemEll"),
            international.getString("MainWindowToolsSubDrawingObjItemEllTooltip"),
            "ellipse.png",
            international.getString("MainWindowToolsSubDrawingObjItemEllMn").charAt(0),
            null,
            DrawObjectFactory.ELLIPSE);
        tool_group.add(toolbarRadioButtons_[3]);

        addDrawObject(
            international.getString("MainWindowToolsSubDrawingObjItemText"),
            international.getString("MainWindowToolsSubDrawingObjItemTextTooltip"),
            "font.png",
            international.getString("MainWindowToolsSubDrawingObjItemTextMn").charAt(0),
            null,
            DrawObjectFactory.TEXT);
        tool_group.add(toolbarRadioButtons_[4]);

        addDrawObject(
            international.getString("MainWindowToolsSubDrawingObjItemPict"),
            international.getString("MainWindowToolsSubDrawingObjItemPictTooltip"),
            "picture.png",
            international.getString("MainWindowToolsSubDrawingObjItemPictMn").charAt(0),
            null,
            DrawObjectFactory.PICTURE);
        tool_group.add(toolbarRadioButtons_[5]);

//		toolbarRadioButtons_[DrawObjectFactory.MAX_OBJECTS+1] = createGradientRadioButton( "Gradient", DrawObjectFactory.MAX_OBJECTS+1,DrawObjectFactory.MAX_OBJECTS+1 );
//		add(toolbarRadioButtons_[DrawObjectFactory.MAX_OBJECTS+1]);
//		tool_group.add(toolbarRadioButtons_[DrawObjectFactory.MAX_OBJECTS+1]);

        addSeparator();

        addSetColorAction(
            international.getString("MainWindowToolsSubColorsItemPencolor"),
            international.getString("MainWindowToolsSubColorsItemPencolorTooltip"),
            "pen_color.png",
            international.getString("MainWindowToolsSubColorsItemPencolorMn").charAt(0),
            null,
            "pen_color", Color.BLACK, 7);
        addSetColorAction(
            international.getString("MainWindowToolsSubColorsItemFillcolor"),
            international.getString("MainWindowToolsSubColorsItemFillcolorTooltip"),
            "fill_color.png",
            international.getString("MainWindowToolsSubColorsItemFillcolorMn").charAt(0),
            null,
            "fill_color", Color.RED, 2);
        
        addSeparator();

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

        addSeparator();
    }

    public void changeDrawingType(int index)
    {
        toolbarRadioButtons_[index].setSelected(true);
    }
    
    /* (non-Javadoc)
     * @see at.bestsolution.drawswf.toolbar.DrawToolbarInterface#addGenericToolbarItem(at.bestsolution.drawswf.actions.AbstractDrawAction, int)
     */
    public void addGenericToolbarItem(AbstractDrawAction draw_action, int position)
    {
        // TODO Auto-generated method stub

    }

	public void setItemEnabled( String name, boolean enabled )
	{
		
	}

}
