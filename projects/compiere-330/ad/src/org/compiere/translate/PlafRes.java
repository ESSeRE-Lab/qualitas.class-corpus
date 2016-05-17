/******************************************************************************
 * Product: Compiere ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 3600 Bridge Parkway #102, Redwood City, CA 94065, USA      *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.translate;

import java.util.*;

/**
 *  Translation Texts for Look & Feel
 *
 *  @author     Jorg Janke
 *  @version    $Id: PlafRes.java,v 1.2 2006/07/30 00:52:24 jjanke Exp $
 */
public class PlafRes extends ListResourceBundle
{
	/** The data    */
	static final Object[][] contents = new String[][]
	{
	{ "BackColType",            "Background Color Type" },
	{ "BackColType_Flat",       "Flat" },
	{ "BackColType_Gradient",   "Gradient" },
	{ "BackColType_Lines",      "Lines" },
	{ "BackColType_Texture",    "Texture" },
	//
	{ "LookAndFeelEditor",      "Look & Feel Editor" },
	{ "LookAndFeel",            "Look & Feel" },
	{ "Theme",                  "Theme" },
	{ "EditCompiereTheme",      "Edit Compiere Theme" },
	{ "SetDefault",             "Default Background" },
	{ "SetDefaultColor",        "Background Color" },
	{ "ColorBlind",             "Color Deficiency" },
	{ "Example",                "Example" },
	{ "Reset",                  "Reset" },
	{ "OK",                     "OK" },
	{ "Cancel",                 "Cancel" },
	//
	{ "CompiereThemeEditor",    "Compiere Theme Editor" },
	{ "MetalColors",            "Metal Colors" },
	{ "CompiereColors",         "Compiere Colors" },
	{ "CompiereFonts",          "Compiere Fonts" },
	{ "Primary1Info",           "Shadow, Separator" },
	{ "Primary1",               "Primary 1" },
	{ "Primary2Info",           "Focus Line, Selected Menu" },
	{ "Primary2",               "Primary 2" },
	{ "Primary3Info",           "Table Selected Row, Selected Text, ToolTip Background" },
	{ "Primary3",               "Primary 3" },
	{ "Secondary1Info",         "Border Lines" },
	{ "Secondary1",             "Secondary 1" },
	{ "Secondary2Info",         "Inactive Tabs, Pressed Fields, Inactive Border + Text" },
	{ "Secondary2",             "Secondary 2" },
	{ "Secondary3Info",         "Background" },
	{ "Secondary3",             "Secondary 3" },
	//
	{ "ControlFontInfo",        "Control Font" },
	{ "ControlFont",            "Label Font" },
	{ "SystemFontInfo",         "Tool Tip, Tree nodes" },
	{ "SystemFont",             "System Font" },
	{ "UserFontInfo",           "User Entered Data" },
	{ "UserFont",               "Field Font" },
//	{ "SmallFontInfo",          "Reports" },
	{ "SmallFont",              "Small Font" },
	{ "WindowTitleFont",        "Title Font" },
	{ "MenuFont",               "Menu Font" },
	//
	{ "MandatoryInfo",          "Mandatory Field Background" },
	{ "Mandatory",              "Mandatory" },
	{ "ErrorInfo",              "Error Field Background" },
	{ "Error",                  "Error" },
	{ "InfoInfo",               "Info Field Background" },
	{ "Info",                   "Info" },
	{ "WhiteInfo",              "Lines" },
	{ "White",                  "White" },
	{ "BlackInfo",              "Lines, Text" },
	{ "Black",                  "Black" },
	{ "InactiveInfo",           "Inactive Field Background" },
	{ "Inactive",               "Inactive" },
	{ "TextOKInfo",             "OK Text Foreground" },
	{ "TextOK",                 "Text - OK" },
	{ "TextIssueInfo",          "Error Text Foreground" },
	{ "TextIssue",              "Text - Error" },
	//
	{ "FontChooser",            "Font Chooser" },
	{ "Fonts",                  "Fonts" },
	{ "Plain",                  "Plain" },
	{ "Italic",                 "Italic" },
	{ "Bold",                   "Bold" },
	{ "BoldItalic",             "Bold & Italic" },
	{ "Name",                   "Name" },
	{ "Size",                   "Size" },
	{ "Style",                  "Style" },
	{ "TestString",             "This is just a Test! The quick brown Fox is doing something. 12,3456.78 LetterLOne = l1 LetterOZero = O0" },
	{ "FontString",             "Font" },
	//
	{ "CompiereColorEditor",    "Compiere Color Editor" },
	{ "CompiereType",           "Color Type" },
	{ "GradientUpperColor",     "Gradient Upper Color" },
	{ "GradientLowerColor",     "Gradient Lower Color" },
	{ "GradientStart",          "Gradient Start" },
	{ "GradientDistance",       "Gradient Distance" },
	{ "TextureURL",             "Texture URL" },
	{ "TextureAlpha",           "Texture Alpha" },
	{ "TextureTaintColor",      "Texture Taint Color" },
	{ "LineColor",              "Line Color" },
	{ "LineBackColor",          "Background Color" },
	{ "LineWidth",              "Line Width" },
	{ "LineDistance",           "Line Distance" },
	{ "FlatColor",              "Flat Color" }
	};

	/**
	 * Get Contents
	 * @return contents
	 */
	@Override
	public Object[][] getContents()
	{
		return contents;
	}
}   //  Res
