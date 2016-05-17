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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/MainWindow.java,v 1.44 2003/04/11 08:20:03 tom Exp $
 */
 
package at.bestsolution.drawswf;

import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import at.bestsolution.drawswf.util.DrawSWFConfig;
import at.bestsolution.ext.awt.FontLoader;

import com.incors.plaf.kunststoff.KunststoffLookAndFeel;
import com.incors.plaf.kunststoff.KunststoffTheme;


/**
 * The main window of the application.
 */
public class MainWindow extends JFrame
{
    public static DrawingPanel drawing_panel_ = null;
    private DrawObjectList draw_object_list_;
    private static final String IMAGE_PATH = "at/bestsolution/drawswf/images/";
    private static DrawSWFConfig config_;
    private static ResourceBundle international_;
    public static MainWindow MAIN_WINDOW = null;
    private DrawMenuBar menu_;
    private DrawToolBar tool_bar_;
    private ArrayList plugins_;
    
    //----------------------------------------------------------------------------
    public MainWindow()
    {
        super( "Draw SWF Animation" );
        MAIN_WINDOW = this;
        plugins_ = null;
    }
    
    public ArrayList getPlugins()
    {
        return plugins_;
    }
    
    public static String getImagePath()
    {
        return IMAGE_PATH;
    }
    
    public static DrawSWFConfig getConfiguration()
    {
        return config_;
    }
    
    public static ResourceBundle getI18n()
    {
        return international_;
    }
    
    public static DrawingPanel getDrawingPanel()
    {
        return drawing_panel_;
    }
    
    public DrawMenuBar getDrawMenuBar()
    {
        return menu_;
    }
    
    public DrawToolBar getDrawToolbar()
    {
        return tool_bar_;
    }
    
    //----------------------------------------------------------------------------
    public void drawIt(SplashScreen splash_screen)
    {
        setI18n();

        setUI();
        splash_screen.progress( 10 );
        setIcon();
        splash_screen.progress( 10 );

        addWindowListener(
        new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        }
        );

        splash_screen.progress( 10 );

        draw_object_list_ = new DrawObjectList( this );

        splash_screen.progress( 10 );

        Dimension size = new Dimension( 800, 600 );
        drawing_panel_ = new DrawingPanel( size, draw_object_list_ );
        getContentPane().add( drawing_panel_, BorderLayout.CENTER );

        splash_screen.progress( 20 );

        menu_ = new DrawMenuBar( draw_object_list_ );
        tool_bar_ = new DrawToolBar( draw_object_list_ );

        setJMenuBar(menu_);
        getContentPane().add(tool_bar_, BorderLayout.NORTH);
        
        splash_screen.progress( 10 );

        pack();
        
        splash_screen.progress( 10 );
        
        setFrameToCenter();

        splash_screen.progress( 10 );

        draw_object_list_.show();

        PluginLoader loader = new PluginLoader();
        plugins_ = loader.loadPlugins();
        
        FontLoader.getInstance().addAdditionalPath( DrawSWFConfig.getInstance().getProperty("ttf_paths") );
        
        splash_screen.progress( 20 );
    }
    
    // ------------------------------------------------------------------
    private void setI18n()
    {
        DrawSWFConfig config = DrawSWFConfig.getInstance();
        
        if( config.getProperty("pluginpath") == null )
        {
            String path = System.getProperty("user.home")+ File.separator + ".drawswf"+File.separator+"plugin";
            config.setProperty("pluginpath", path);
            
            try
            {
                File dir = new File(path);
                
                if( ! dir.exists() )
                {
                    dir.mkdirs();
                }
                
                config.save();
            }
            catch( FileNotFoundException e )
            {
                e.printStackTrace();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            
        }
         
        Locale locale = new Locale(config.getProperty("language") );
        
        international_ = ResourceBundle.getBundle("DrawSWFBundle", locale );
    }
    
    //----------------------------------------------------------------------------
    private void setIcon()
    {
        Image icon;
        URL icon_url;
        
        icon_url = getClass().getClassLoader().getResource("at/bestsolution/drawswf/images/logo_icon.png");
        icon = Toolkit.getDefaultToolkit().getImage( icon_url );
        setIconImage( icon );
    }
    
    //----------------------------------------------------------------------------
    private void setUI()
    {
        try
        {
            KunststoffLookAndFeel kunststoffLnF = new KunststoffLookAndFeel();
            KunststoffLookAndFeel.setCurrentTheme( new KunststoffTheme() );
            UIManager.setLookAndFeel(kunststoffLnF);
        } 
        catch (UnsupportedLookAndFeelException ex)
        {
            // handle exception or not, whatever you prefer
        }
        // this line needs to be implemented in order to make JWS work properly
        UIManager.getLookAndFeelDefaults().put("ClassLoader", getClass().getClassLoader());
    }
    
    //----------------------------------------------------------------------------
    private void setFrameToCenter()
    {
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frame_size = getSize();
        if (frame_size.height > screen_size.height)
        {
            frame_size.height = screen_size.height;
        }
        if (frame_size.width > screen_size.width)
        {
            frame_size.width = screen_size.width;
        }
        setLocation((screen_size.width - frame_size.width) / 2, (screen_size.height - frame_size.height) / 2);
        
        int new_x = (screen_size.width - frame_size.width) / 2 - draw_object_list_.getWidth();
        
        if (new_x < 0)
        {
            new_x = 0;
        }
        
        draw_object_list_.setLocation(new_x, (screen_size.height - frame_size.height) / 2);
    }
    
    
    //----------------------------------------------------------------------------
    public void swapRadioButtons( String buttonType, int buttonIndex )
    {
        if( buttonType.equals("MenuBarButton") )
        {
            tool_bar_.changeDrawingType( buttonIndex );
        }
        else if( buttonType.equals("ToolBarButton") )
        {
            menu_.changeDrawingType( buttonIndex );
        }
    }
}
