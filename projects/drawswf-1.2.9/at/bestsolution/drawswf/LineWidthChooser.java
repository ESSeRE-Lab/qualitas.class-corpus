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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/LineWidthChooser.java,v 1.3 2004/03/24 15:39:10 tom Exp $
 */

package at.bestsolution.drawswf;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author  heli
 */
public class LineWidthChooser extends JFrame implements ChangeListener
{
    private JSlider slider_;
    private DrawingPanel drawing_panel_;
    private BasicStroke stroke_;
    private JLinePanel line_panel_;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of LineWidthChooser */
    public LineWidthChooser(DrawingPanel drawing_panel)
    {
        super(MainWindow.getI18n().getString("LineWidthChooserTitle"));
        
        drawing_panel_ = drawing_panel;
        
        stroke_ = new BasicStroke(drawing_panel_.getPenSize(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        line_panel_ = new JLinePanel();
        getContentPane().add(line_panel_, BorderLayout.CENTER);
        
        slider_ = new JSlider(JSlider.HORIZONTAL, 1, 21, (int) drawing_panel_.getPenSize());
        slider_.addChangeListener(this);
        slider_.setMajorTickSpacing(5);
        slider_.setMajorTickSpacing(5);
        slider_.setMinorTickSpacing(1);
        slider_.setPaintTicks(true);
        slider_.setSnapToTicks(true);
        getContentPane().add(slider_, BorderLayout.SOUTH);
        
        pack();
    }
    
    //----------------------------------------------------------------------------
    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e  a ChangeEvent object
     */
    public void stateChanged(ChangeEvent e)
    {
        drawing_panel_.setPenSize((float) slider_.getValue());
        stroke_ = new BasicStroke(drawing_panel_.getPenSize(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        line_panel_.repaint();
    }
    
    //----------------------------------------------------------------------------
    private class JLinePanel extends JPanel
    {
        //----------------------------------------------------------------------------
        public JLinePanel()
        {
            super();
            
            Dimension size = new Dimension(80,80);
            setMinimumSize(size);
            setPreferredSize(size);
            setSize(size);
        }
        
        //----------------------------------------------------------------------------
        public void paintComponent(Graphics graphics)
        {
            Graphics2D g = (Graphics2D) graphics;
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.black);
            g.setStroke(stroke_);
            g.draw( new Line2D.Float(12.0f, 12.0f, getWidth() - 12.0f, getHeight() - 12.0f) );
        }
    }
}
