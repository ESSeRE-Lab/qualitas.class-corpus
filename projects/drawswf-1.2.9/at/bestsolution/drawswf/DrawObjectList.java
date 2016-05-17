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
 * DrawObjectList.java
 *
 * Created on 22. September 2002, 19:33
 */

package at.bestsolution.drawswf;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JDialog;
import javax.swing.JSplitPane;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import at.bestsolution.drawswf.drawobjects.DrawObject;

/**
 *
 * @author  tom
 */
public class DrawObjectList extends JDialog implements ListSelectionListener, ActionListener
{
    private DefaultListModel list_model_;
    //    private LinkedList draw_list_;
    private JList draw_object_list_;
    private JScrollPane scroll_pane2_;
    private PropertiesDialog properties_;
    private static final String icon_path = "at/bestsolution/drawswf/images/24x24/";

    //----------------------------------------------------------------------------
    /** Creates a new instance of DrawObjectList */
    public DrawObjectList(MainWindow main_window)
    {
        super(main_window, MainWindow.getI18n().getString("DrawObjectList") );

        properties_ = null;
        //        draw_list_ = new LinkedList();

        addComponents();
        setSize(200, 600);
    }

    //----------------------------------------------------------------------------
    public void addDrawObject(DrawObject draw_object)
    {
        list_model_.addElement(draw_object.getName());
        draw_object_list_.setSelectedValue(draw_object.getName(), true);
    }

    //----------------------------------------------------------------------------
    public void removeDrawObject(DrawObject draw_object)
    {
        list_model_.removeElement(draw_object.getName());
    }

    public void moveDrawObject(int selected_index, int amount)
    {
        Object tmp = list_model_.get(selected_index);
        Object tmp2 = list_model_.set(selected_index + amount, tmp);
        list_model_.set(selected_index, tmp2);
        draw_object_list_.setSelectedIndex(selected_index + amount);
    }

    //----------------------------------------------------------------------------
    public void clearDrawObjects()
    {
        scroll_pane2_.setViewportView(new JPanel());
        // draw_list_.clear();
        list_model_.clear();
    }

    //----------------------------------------------------------------------------
    private void addComponents()
    {
        list_model_ = new DefaultListModel();
        draw_object_list_ = new JList(list_model_);
        draw_object_list_.addListSelectionListener(this);

        JScrollPane scroll_pane1 = new JScrollPane();
        JPanel panel = new JPanel(new BorderLayout());
        scroll_pane1.setViewportView(draw_object_list_);
        scroll_pane1.setMinimumSize(new Dimension(100, 120));
        panel.add(scroll_pane1, BorderLayout.CENTER);

        JPanel button_panel = new JPanel(new GridLayout(1, 3, 2, 2));

        ImageIcon icon;
        URL icon_url;

        icon_url = getClass().getClassLoader().getResource(icon_path + "up.gif");
        icon = new ImageIcon(icon_url);
        JButton button = new JButton(icon);
        button.setActionCommand("up");
        button.addActionListener(this);
        button_panel.add(button);

        icon_url = getClass().getClassLoader().getResource(icon_path + "down.gif");
        icon = new ImageIcon(icon_url);
        button = new JButton(icon);
        button.setActionCommand("down");
        button.addActionListener(this);
        button_panel.add(button);

        icon_url = getClass().getClassLoader().getResource(icon_path + "delete.png");
        icon = new ImageIcon(icon_url);
        button = new JButton(icon);
        button.setActionCommand("delete");
        button.addActionListener(this);
        button_panel.add(button);

        panel.add(button_panel, BorderLayout.SOUTH);

        scroll_pane2_ = new JScrollPane();
        scroll_pane2_.setMinimumSize(new Dimension(100, 120));

        JSplitPane split_pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panel, scroll_pane2_);
        split_pane.setDividerLocation(400);

        getContentPane().add(split_pane);

    }

    //----------------------------------------------------------------------------
    /**
     * Called whenever the value of the selection changes.
     * @param e the event that characterizes the change.
     *
     */
    public void valueChanged(ListSelectionEvent e)
    {
        if ((MainWindow.getDrawingPanel().getDrawingList().size() > 0) && (draw_object_list_.getSelectedIndex() != -1))
        {
            if (properties_ == null)
            {
                properties_ = new PropertiesDialog(MainWindow.getDrawingPanel().getDrawingList().get(draw_object_list_.getSelectedIndex()));
            }
            else
            {
                properties_.setDrawObject(MainWindow.getDrawingPanel().getDrawingList().get(draw_object_list_.getSelectedIndex()));
            }

			MainWindow.getDrawingPanel().setSelectedDrawingObject( draw_object_list_.getSelectedIndex() );

            scroll_pane2_.setViewportView(properties_);
        }
        else
        {
            scroll_pane2_.setViewportView(new JPanel());
        }
    }

    //----------------------------------------------------------------------------
    public void setSelectedObject(DrawObject new_focus)
    {
        draw_object_list_.setSelectedValue(new_focus.getName(), true);
    }

    //----------------------------------------------------------------------------
    public void updateObject(DrawObject draw_object)
    {
        properties_.setDrawObject(draw_object);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        int selected_index = draw_object_list_.getSelectedIndex();

        if (selected_index >= 0)
        {
            if (e.getActionCommand().equals("up"))
            {
                MainWindow.getDrawingPanel().moveDrawingObject(selected_index, -1);
            }
            else if (e.getActionCommand().equals("down"))
            {
                MainWindow.getDrawingPanel().moveDrawingObject(selected_index, 1);
            }
            else
            {
                MainWindow.getDrawingPanel().moveDrawingObject(selected_index, 0);
            }
        }
    }

}
