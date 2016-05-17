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
package at.bestsolution.drawswf.plugin.remotesave;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.KeyStroke;

import at.bestsolution.drawswf.AbstractPlugin;
import at.bestsolution.drawswf.DrawingPanel;
import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.PluginLoader;
import at.bestsolution.drawswf.plugin.remotesave.action.RemoteSaveAction;
import at.bestsolution.drawswf.util.DrawSWFConfig;
import at.bestsolution.util.BestsolutionConfiguration;
import at.bestsolution.drawswf.plugin.remotesave.util.RemoteSaveConfig;
/**
 * @author tom
 */
public class RemoteSavePlugin extends AbstractPlugin
{
    private static DrawSWFConfig main_configuration_;
    private static ResourceBundle international_;

    public RemoteSavePlugin()
    {

    }

    public void init(PluginLoader loader, DrawingPanel drawing_panel, MainWindow main_window)
    {
        super.init(loader, drawing_panel, main_window);
        setI18n();
    }

    public static ResourceBundle getI18n()
    {
        return international_;
    }

    public static DrawSWFConfig getDrawSWFConfiguration()
    {
        return main_configuration_;
    }

    /* (non-Javadoc)
     * @see at.bestsolution.drawswf.AbstractPlugin#loadSelf()
     */
    public void loadSelf()
    {
        RemoteSaveAction action =
            new RemoteSaveAction(
                international_.getString("MainWindowRemoteExport"),
                international_.getString("MainWindowRemoteExportTooltip"),
                "export_remote.png",
                getDrawingPanel(),
                international_.getString("MainWindowRemoteExportMn").charAt(0),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));

        loader_.addToMenu("file", action, 5);
    }

    //  ------------------------------------------------------------------
    private void setI18n()
    {
        Locale locale = new Locale("en");
        
        main_configuration_ = loader_.getConfig();

        if (main_configuration_ != null)
        {
            locale = new Locale(main_configuration_.getProperty("language"));
        }

        international_ = ResourceBundle.getBundle("at/bestsolution/drawswf/plugin/remotesave/RemoteSavePluginBundle", locale );
    }

    public BestsolutionConfiguration getConfig()
    {
        return RemoteSaveConfig.getInstance();
    }
}
