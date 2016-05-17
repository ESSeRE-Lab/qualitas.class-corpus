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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/DrawObjectTableCellRenderer.java,v 1.7 2003/06/07 20:39:46 tom Exp $
 */

package at.bestsolution.drawswf;

import javax.swing.table.DefaultTableCellRenderer;

import javax.swing.JTable;
import javax.swing.UIManager;

import java.awt.Component;
import java.awt.Color;


/**
 *
 * @author  tom
 */
public class DrawObjectTableCellRenderer extends DefaultTableCellRenderer
{
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of DrawObjectTableCellRenderer */
    public DrawObjectTableCellRenderer()
    {
        super();
    }
    
    //----------------------------------------------------------------------------
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        //        System.out.println("value = " + value);
        
        if ( value instanceof Color )
        {
            setBackground((Color)value);
            setText( "" );
        }
        else
        {
            setBackground( Color.WHITE );
            setValue( value );
        }
        
        if (hasFocus)
        {
            setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
            
            if (table.isCellEditable(row, column))
            {
                super.setForeground( UIManager.getColor("Table.focusCellForeground") );
            }
        }
        else
        {
            setBorder(noFocusBorder);
        }
        
        return this;
    }
}
