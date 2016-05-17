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
 * Created on 27.02.2003
 *
 */
package at.bestsolution.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import at.bestsolution.drawswf.MainWindow;

/**
 * @author tom
 */
public class BestsolutionConfigurationEditor extends JPanel implements ActionListener
{
    protected BestsolutionConfiguration config_ = null;
    private JTable table_;
    private Hashtable properties_ = null;

    public BestsolutionConfigurationEditor()
    {
        super();
        setLayout(new BorderLayout());
        properties_ = new Hashtable();
    }

    public void setConfiguration(BestsolutionConfiguration config)
    {
        config_ = config;
        initComponents();
    }

    private void initComponents()
    {
        removeAll();

        String attribute = MainWindow.getI18n().getString("BestsolutionConfigurationEditorAttribute");
        
        Vector header = new Vector();
        header.add(attribute);
        header.add(MainWindow.getI18n().getString("BestsolutionConfigurationEditorValue"));

        Vector data = new Vector();
        Vector row;
        String key;

        Enumeration props = config_.getProperties();

        while (props.hasMoreElements())
        {
            row = new Vector();
            key = props.nextElement().toString();
            row.add(key);
            row.add(config_.getProperty(key));
            data.add(row);
        }

        table_ = new JTable(data, header);

        DefaultCellEditor no_editor = new DefaultCellEditor(new JTextField())
        {
            public boolean isCellEditable(EventObject anEvent)
            {
                return false;
            }
        };

        table_.getColumn(attribute).setCellEditor(no_editor);

        add(table_.getTableHeader(), BorderLayout.NORTH);
        add(table_, BorderLayout.CENTER);

        JPanel button_panel = new JPanel(new FlowLayout());

        JButton button = new JButton(MainWindow.getI18n().getString("BestsolutionConfigurationEditorApply"));
        button.setActionCommand("ok");
        button.addActionListener(this);
        button_panel.add(button);

        button = new JButton(MainWindow.getI18n().getString("BestsolutionConfigurationEditorCancel"));
        button.setActionCommand("cancel");
        button.addActionListener(this);
        button_panel.add(button);

        add(button_panel, BorderLayout.SOUTH);
    }

    private void applyProperties()
    {
        
        TableModel model = table_.getModel();
        if (table_.getCellEditor() != null)
        {
            table_.getCellEditor().stopCellEditing();
        }
         
        for(int i = 0; i < model.getRowCount(); i++)
        {
            config_.setProperty(model.getValueAt(i,0).toString(), model.getValueAt(i,1).toString());
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals("ok"))
        {
            applyProperties();

            try
            {
                config_.save();
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(MainWindow.MAIN_WINDOW, ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        setVisible( false );
    }

}
