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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/DrawSWF.java,v 1.6 2002/10/11 10:35:53 tom Exp $
 */

/*
 * DrawSWF.java
 *
 * Created on May 14, 2002, 9:17 AM
 */

import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.SplashScreen;

/**
 * The start class for the drawing editor
 *
 * @author  heli
 */
public class DrawSWF
{
    /**
     * The main routine where all begins
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        MainWindow main_window = new MainWindow();
        
        SplashScreen splash_screen = new SplashScreen( main_window, 0 );
        
        main_window.drawIt( splash_screen );
        
        main_window.setVisible(true);
        
        splash_screen.setVisible( false );
        splash_screen.dispose();
    }
}
