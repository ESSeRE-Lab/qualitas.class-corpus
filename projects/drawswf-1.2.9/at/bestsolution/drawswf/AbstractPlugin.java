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

package at.bestsolution.drawswf;

import at.bestsolution.util.BestsolutionConfiguration;

/**
 * @author tom
 */
public abstract class AbstractPlugin
{
    protected PluginLoader loader_;
    protected static DrawingPanel drawing_panel_;
    protected static MainWindow main_window_;

    public AbstractPlugin()
    {

    }

    public void init(PluginLoader loader, DrawingPanel drawing_panel, MainWindow main_window)
    {
        loader_ = loader;
        drawing_panel_ = drawing_panel;
        main_window_ = main_window;
    }

    public abstract void loadSelf();

    public static DrawingPanel getDrawingPanel()
    {
        return drawing_panel_;
    }

    public static MainWindow getMainWindow()
    {
        return main_window_;
    }

    public BestsolutionConfiguration getConfig()
    {
        return null;
    }
}
