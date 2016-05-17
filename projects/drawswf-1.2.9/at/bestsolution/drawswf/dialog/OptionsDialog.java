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
 * Created on 22.02.2003
 *
 */
package at.bestsolution.drawswf.dialog;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import at.bestsolution.drawswf.AbstractPlugin;
import at.bestsolution.drawswf.MainWindow;
import at.bestsolution.drawswf.util.DrawSWFConfig;
import at.bestsolution.util.BestsolutionConfiguration;
import at.bestsolution.util.BestsolutionConfigurationEditor;
import at.bestsolution.util.BestsolutionConfigurationEditorFactory;

/**
 * @author tom
 */
public class OptionsDialog extends JDialog implements MouseListener
{
    private DefaultMutableTreeNode main_config_;
    private JScrollPane editor_pane_;
    private JTree tree_;

    public OptionsDialog()
    {
        super(MainWindow.MAIN_WINDOW, "Options", true);
        init();
        pack();
        setSize(new Dimension(600, 300));
    }

    private void init()
    {
        DefaultMutableTreeNode root_node = new DefaultMutableTreeNode();

        main_config_ = new DefaultMutableTreeNode(DrawSWFConfig.getInstance());

        addPluginsConfig();

        root_node.add(main_config_);

        tree_ = new JTree(root_node);
        tree_.setRootVisible(false);
        tree_.addMouseListener(this);

        tree_.expandRow(0);

        JScrollPane tree_pane = new JScrollPane();
        tree_pane.setViewportView(tree_);

        editor_pane_ = new JScrollPane();

        JSplitPane split_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree_, editor_pane_);
        getContentPane().add(split_pane);
    }

    private void addPluginsConfig()
    {
        ArrayList plugins = MainWindow.MAIN_WINDOW.getPlugins();
        BestsolutionConfiguration config;

        for (int i = 0; i < plugins.size(); i++)
        {
            config = ((AbstractPlugin) plugins.get(i)).getConfig();
            if (config != null)
            {
                main_config_.add(new DefaultMutableTreeNode(config));
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree_.getLastSelectedPathComponent();
        
        if (node != null)
        {
            BestsolutionConfiguration config = (BestsolutionConfiguration) node.getUserObject();
            BestsolutionConfigurationEditor editor = BestsolutionConfigurationEditorFactory.getEditor(config);
            
            if( editor != null )
            {
                editor_pane_.setViewportView(editor);
                editor.setVisible(true);
            }
            
        }

    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e)
    {
    }
}
