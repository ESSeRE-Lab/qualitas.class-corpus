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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/AboutWindow.java,v 1.13 2004/05/05 08:09:42 tom Exp $
 */

package at.bestsolution.drawswf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;

import java.net.URL;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;

/**
 *
 * @author  heli
 */
public class AboutWindow extends JFrame
{
    public static final String VERSION = "1.2.8";
    // TODO: has to be made in swing because of performance...
    public static final String INFO_MESSAGE = "<html><table><tr><td>DrawSWF " + VERSION + "</td><td align='right'>http://drawswf.sf.net</td></tr><tr><td>&copy; 2002 by bestsolution.at </td><td>http://www.bestsolution.at</td></tr></table></html>";
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of AboutWindow */
    public AboutWindow()
    {
        super("DrawSWF");
        
        JTabbedPane tabbed_pane = new JTabbedPane();
        
        tabbed_pane.addTab("About", createAboutLabel());
        tabbed_pane.addTab("License", createLicenseArea());
        tabbed_pane.addTab("Used Libraries", createLibraryArea());
        
        getContentPane().add(tabbed_pane, BorderLayout.CENTER);
        pack();
        
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screen_size.getWidth() - getWidth()) / 2, (int) (screen_size.getHeight() - getHeight()) / 2);
    }
    
    //----------------------------------------------------------------------------
    private JScrollPane createLicenseArea()
    {
        URL license_url = getClass().getClassLoader().getResource("GPL_V2");
        JEditorPane license_area = new JEditorPane();
        license_area.setEditable(false);
        JScrollPane scroll_pane = new JScrollPane(license_area);
        scroll_pane.setPreferredSize(new Dimension(400,300));
        
        try
        {
            license_area.setPage(license_url);
        }
        catch (IOException e)
        {
        }
        
        return scroll_pane;
    }
    
    //----------------------------------------------------------------------------
    private JLabel createAboutLabel()
    {
        URL logo_url = getClass().getClassLoader().getResource("at/bestsolution/drawswf/images/logo.png");
        
        JLabel about_label = new JLabel(INFO_MESSAGE, new ImageIcon(logo_url), JLabel.CENTER);
        about_label.setBackground(Color.white);
        about_label.setOpaque(true);
        about_label.setHorizontalTextPosition(JLabel.CENTER);
        about_label.setVerticalTextPosition(JLabel.BOTTOM);
        
        return about_label;
    }

    //----------------------------------------------------------------------------
    private JLabel createLibraryArea()
    {
        String message =      "<html>For the look and feel we use the Kunststoff library<br>"
                            + "from http://www.incors.org (license is LGPL).<br><br>"
                            + "And for the flash output we use JavaSWF2, which will<br>"
                            + "hopefully be available soon at http://javaswf.sf.net<br>"
                            + "(now: http://www.anotherbigidea.com/javaswf/index.html<br>"
                            + "and is licensed under BSD-license).<br><br>The icons are provided by http://sourceforge.net/projects/icon-collection/.<br><br> We also use the gradient paints provided by the batik project<br> http://xml.apache.org/batik/ licensed under<br> 'Apache Software License 1.1'</html>";
        
        JLabel about_label = new JLabel(message);
        about_label.setBackground(Color.white);
        about_label.setOpaque(true);
        about_label.setHorizontalTextPosition(JLabel.CENTER);
        about_label.setVerticalTextPosition(JLabel.BOTTOM);
        
        return about_label;
    }
}
