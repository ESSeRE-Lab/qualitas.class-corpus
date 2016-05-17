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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/PropertiesDialog.java,v 1.6 2003/03/07 10:52:37 tom Exp $
 */

package at.bestsolution.drawswf;

/*
 * PropertiesDialog.java
 *
 * Created on 27. September 2002, 10:47
 */

import java.lang.reflect.*;

import java.util.EventObject;
import java.util.Vector;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultCellEditor;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


/**
 *
 * @author  tom
 */
public class PropertiesDialog extends JPanel implements TableModelListener
{
    private JTable options_table_;
    private DefaultTableModel model_;
    private Object draw_object_;
    private Method[] set_methods_;
    private static Vector header_;
    private TableCellEditor editor_;
    private TableCellEditor no_editor_;
    private TableCellRenderer renderer_;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of PropertiesDialog */
    public PropertiesDialog( Object draw_object )
    {
        super( new BorderLayout() );
        
        draw_object_ = draw_object;
        
        header_ = new Vector();
        header_.add("Attribute");
        header_.add("Value" );

        renderer_    = new DrawObjectTableCellRenderer();
        editor_      = new DrawObjectTableCellEditor();
        no_editor_   = new DefaultCellEditor(new JTextField())
        {
            public boolean isCellEditable(EventObject anEvent)
            { return false; }
        };

        options_table_ = new JTable();
        
        model_ = new DefaultTableModel();
        model_.addTableModelListener( this );
        setDrawObject(draw_object);
        
        options_table_.setModel(model_);
        model_.addTableModelListener( options_table_ );
        options_table_.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        
        add( options_table_.getTableHeader(), BorderLayout.NORTH );
        add( options_table_, BorderLayout.CENTER );
        
        setSize( 100, 600 );
    }
    
    //----------------------------------------------------------------------------
    public void setDrawObject(Object draw_object)
    {
        draw_object_ = draw_object;
        model_.setDataVector(getListEntries(), header_);
        
        options_table_.setModel(model_);
        options_table_.getColumn("Attribute").setCellEditor( no_editor_ );
        options_table_.getColumn("Value").setCellEditor( editor_ );
        options_table_.getColumn("Value").setCellRenderer( renderer_ );
    }
    
    //----------------------------------------------------------------------------
    private Vector getListEntries()
    {
        Vector values = new Vector();
        Class c = draw_object_.getClass();
        Method[] the_methods = c.getMethods();
        set_methods_ = new Method[the_methods.length];
        
        int get_count = 0;
        
        for( int i = 0; i < the_methods.length; i++ )
        {
            String method_string = the_methods[i].getName();
            Class[] parameter_types = the_methods[i].getParameterTypes();
            
            if( method_string.indexOf("setProperty") != -1 )
            {
                Vector tmp_vector  = new Vector();
                String method_name = method_string.substring(11);
                try
                {
                    Method get_method  = c.getMethod( "getProperty"+method_name, null );
                    set_methods_[get_count++] = the_methods[i];
                    tmp_vector.add( method_name );
                    tmp_vector.add( get_method.invoke( draw_object_, null ) );
                }
                catch (NoSuchMethodException e)
                {
                    System.out.println(e);
                }
                catch (IllegalAccessException e)
                {
                    System.out.println(e);
                }
                catch (InvocationTargetException e)
                {
                    System.out.println(e);
                }
                values.add( tmp_vector );
            }
        }
        
        return values;
    }
    
    //----------------------------------------------------------------------------
    /** 
     * This fine grain notification tells listeners the exact range
     * of cells, rows, or columns that changed.
     */
    public void tableChanged(TableModelEvent e)
    {
        int modified_row = e.getFirstRow();
        
        if (modified_row >= 0)
        {
            Object[] arguments = new Object[] { model_.getValueAt( modified_row, 1 ) };

            try
            {
                set_methods_[modified_row].invoke( draw_object_, arguments );
            }
            catch( NumberFormatException ex )
            {
                JOptionPane.showMessageDialog(  options_table_,  "Not a number: " + arguments[0], "Error", JOptionPane.ERROR_MESSAGE );
            }
            catch( IllegalArgumentException ex )
            {
                System.out.println(ex);
            }
            catch ( IllegalAccessException ex )
            {
                System.out.println(ex);
            }
            catch ( InvocationTargetException ex )
            {
                System.out.println(ex);
            }
        }
    }
}
