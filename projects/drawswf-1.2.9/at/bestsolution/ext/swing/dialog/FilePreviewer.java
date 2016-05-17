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
 * FilePreviewer.java
 *
 * Created on 10. Jänner 2003, 15:04
 */

package at.bestsolution.ext.swing.dialog;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics;

import java.io.File;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;



/**
 *
 * @author  tom
 */
public class FilePreviewer extends JComponent implements PropertyChangeListener
{
    ImageIcon thumbnail_ = null;
    
    public FilePreviewer(JFileChooser fc)
    {
        setPreferredSize( new Dimension(100, 50) );
        fc.addPropertyChangeListener( this );
    }
    
    public void loadImage( File f )
    {
        if (f == null)
        {
            thumbnail_ = null;
        } 
        else
        {
            ImageIcon tmpIcon = new ImageIcon(f.getPath());
            
            if(tmpIcon.getIconWidth() > 90)
            {
                thumbnail_ = new ImageIcon( tmpIcon.getImage().getScaledInstance(90, -1, Image.SCALE_DEFAULT) );
            } 
            else
            {
                thumbnail_ = tmpIcon;
            }
        }
    }
    
    public void propertyChange(PropertyChangeEvent e)
    {
        String prop = e.getPropertyName();
        
        if(prop == JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
        {
            if( isShowing() )
            {
                loadImage((File) e.getNewValue());
                repaint();
            }
        }
    }
    
    public void paint(Graphics g)
    {
        if( thumbnail_ != null)
        {
            int x = getWidth()/2 - thumbnail_.getIconWidth()/2;
            int y = getHeight()/2 - thumbnail_.getIconHeight()/2;
            if(y < 0)
            {
                y = 0;
            }
            
            if(x < 5)
            {
                x = 5;
            }
            
            thumbnail_.paintIcon(this, g, x, y);
        }
    }
}