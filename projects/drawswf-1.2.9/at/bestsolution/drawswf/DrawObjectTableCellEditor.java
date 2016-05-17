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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/DrawObjectTableCellEditor.java,v 1.11 2003/04/11 12:23:13 tom Exp $
 */

package at.bestsolution.drawswf;

import java.awt.Color;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.util.Hashtable;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import at.bestsolution.drawswf.dialog.*;
import at.bestsolution.drawswf.drawobjects.DrawSWFFont;
import at.bestsolution.ext.swing.dialog.AlphaColorChooser;

/**
 *
 * @author  tom
 */
public class DrawObjectTableCellEditor extends DefaultCellEditor
{
    protected Hashtable delegates;
    protected Hashtable editors;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of DrawObjectTableModel */
    public DrawObjectTableCellEditor()
    {
        super( new JTextField() );
        
        editors   = new Hashtable();
        delegates = new Hashtable();
        
        editors.put("String", editorComponent);
        delegates.put("String", delegate);
        
        JButton button = new JButton();
        editors.put("Color", button );
        ColorDelegate color_delegate = new ColorDelegate( button );
        button.addActionListener(color_delegate);
        delegates.put("Color", color_delegate);
        
        button = new JButton();
        editors.put("DrawSWFFont", button );
        DrawSWFFontDelegate font_delegate = new DrawSWFFontDelegate( button );
        button.addActionListener(font_delegate);
        delegates.put("DrawSWFFont", font_delegate);
    }
    
    //----------------------------------------------------------------------------
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column )
    {
        //        System.out.println("value = " + value);
        
        if ( value instanceof Color )
        {
            editorComponent = (JButton) editors.get("Color");
            editorComponent.setBackground((Color) value);
            delegate = (EditorDelegate) delegates.get("Color");
            ( (ColorDelegate) delegate ).setTableModel(table.getModel(), row, column);
            delegate.setValue(value);
        }
        else if( value instanceof DrawSWFFont )
        {
            editorComponent = (JButton) editors.get("DrawSWFFont");
            ((JButton) editorComponent).setText(value.toString());
            delegate = (EditorDelegate) delegates.get("DrawSWFFont");
            ( (DrawSWFFontDelegate) delegate ).setTableModel(table.getModel(), row, column);
            delegate.setValue( value );
        }
        else
        {
            editorComponent = (JTextField) editors.get("String");
            delegate = (EditorDelegate) delegates.get("String");
            delegate.setValue(value);
        }
        
        return editorComponent;
    }
    
    //----------------------------------------------------------------------------
    protected class DrawSWFFontDelegate extends EditorDelegate implements ChangeListener
    {
        private TableModel model_;
        private FontDialog font_dialog_;
        private int row_;
        private int column_;
        private JButton button_;
        
        //----------------------------------------------------------------------------
        public DrawSWFFontDelegate( JButton button )
        {
            model_ = null;
            font_dialog_ = new FontDialog( MainWindow.MAIN_WINDOW );
            font_dialog_.removeAllChangeListeners();
            font_dialog_.addChangeListener( this );
            font_dialog_.setTextInputEnabled( false );
            button_ = button;
        }
        
        //----------------------------------------------------------------------------
        public void setValue(Object value)
        {
            font_dialog_.setDrawSWFFont( (DrawSWFFont)value );
        }
        
        //----------------------------------------------------------------------------
        public Object getCellEditorValue()
        {
            return font_dialog_.getDrawSWFFont();
        }
        
        //----------------------------------------------------------------------------
        public void actionPerformed(ActionEvent e)
        {
            font_dialog_.show();
        }
        
        //----------------------------------------------------------------------------
        public void setTableModel(TableModel table_model, int row, int column)
        {
            model_  = table_model;
            row_    = row;
            column_ = column;
        }
        
        //----------------------------------------------------------------------------
        public void stateChanged(ChangeEvent e)
        {
            if (model_ != null)
            {
                model_.setValueAt(font_dialog_.getDrawSWFFont(), row_, column_);
            }

            button_.setText( font_dialog_.getDrawSWFFont().toString() );
        }
    }
    
    //----------------------------------------------------------------------------
    protected class ColorDelegate extends EditorDelegate implements ChangeListener
    {
        private int row_;
        private int column_;
        private TableModel model_;
        private JButton button_;
        private Color color_;
        
        //----------------------------------------------------------------------------
        public ColorDelegate( JButton button )
        {
            AlphaColorChooser color_chooser = AlphaColorChooser.getInstance();
            color_chooser.setTitle("Edit Color");
            color_chooser.setChangeListener(this);
            color_chooser.setColor(Color.black);
            button_ = button;
            model_ = null;
        }
        
        //----------------------------------------------------------------------------
        public void setValue(Object value)
        {
            color_ = (Color) value;
            AlphaColorChooser color_chooser = AlphaColorChooser.getInstance();
            color_chooser.setTitle("Edit Color");
            color_chooser.setChangeListener(this);
            color_chooser.setColor(color_);
        }
        
        //----------------------------------------------------------------------------
        public Object getCellEditorValue()
        {
            return color_;
        }
        
        //----------------------------------------------------------------------------
        public void actionPerformed(ActionEvent e)
        {
            AlphaColorChooser color_chooser = AlphaColorChooser.getInstance();
            color_chooser.setTitle("Edit Color");
            color_chooser.setChangeListener(this);
            color_chooser.setColor(color_);
            color_chooser.show();
        }
        
        //----------------------------------------------------------------------------
        public void setTableModel(TableModel table_model, int row, int column)
        {
            model_  = table_model;
            row_    = row;
            column_ = column;
        }
        
        //----------------------------------------------------------------------------
        public void stateChanged(ChangeEvent e)
        {
            AlphaColorChooser color_chooser = AlphaColorChooser.getInstance();
            color_  = color_chooser.getColor();
            
            if (model_ != null)
            {
                
                model_.setValueAt(color_, row_, column_);
            }
            
            if( button_ != null )
            {
                button_.setBackground( color_ );
            }
        }
    }
}
