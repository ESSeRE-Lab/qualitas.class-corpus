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
 * Created on 04.04.2003
 *
 */
package at.bestsolution.ext.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.ext.awt.MultipleGradientPaint;

import at.bestsolution.ext.swing.*;

/**
 * @author tom
 */
public class JGradientChooser extends JDialog implements ChangeListener
{
	public static final Point2D.Float START_ = new Point2D.Float(20f,20f);
	public static final Point2D.Float END_  = new Point2D.Float(180f,180f);
	public static final Point2D.Float CENTER_ = new Point2D.Float(100f,100f);

	public static float[] fractions_ = {0.0f,1.0f};
	public static Color[] colors = { Color.WHITE, Color.BLACK };
	public static JGradientChooser instance_ = null;

	private ChangeListener listener_icon_; 
	private MultipleGradientPaint gradient_paint_;

    protected JGradientChooser(String title)
    {
        super();
        setTitle(title);
        setModal(false);
        drawSurface();
        pack();
    }

	public static JGradientChooser getInstance()
	{
		if( instance_ == null )
		{
			instance_ = new JGradientChooser("Choose Gradient");
		}
		
		return instance_;
	}

    private void drawSurface()
    {
        JPanel button_panel;
        getContentPane().setLayout(new BorderLayout(10, 10));

        JPanel control_panel = new JPanel();
        control_panel.setLayout(new GridLayout(2,2));

        String[] elements = { "linear", "radial" };

        JComboBox combo1 = new JComboBox(elements);
        JLabel label = new JLabel("Gradienttype: ");

        JPanel combo_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        control_panel.add(label);
        control_panel.add(combo1);

        String[] elements1 = { "no cycle", "reflect", "repeat" };

        JComboBox combo2 = new JComboBox(elements1);
        label = new JLabel("Repeat: ");

        control_panel.add(label);
        control_panel.add(combo2);

        getContentPane().add(control_panel, BorderLayout.NORTH);

        AlphaColorJButton button_color1 = new AlphaColorJButton(colors[0]);

        button_panel = new JPanel();
        button_panel.add(button_color1);

        getContentPane().add(button_panel, BorderLayout.WEST);

        AlphaColorJButton button_color2 = new AlphaColorJButton(colors[1]);

        button_panel = new JPanel();
        button_panel.add(button_color2);

        getContentPane().add(button_panel, BorderLayout.EAST);

		PreviewPanel preview_panel = new PreviewPanel(button_color1, button_color2,combo1,combo2);
		preview_panel.setChangeListener(this);

        getContentPane().add(preview_panel, BorderLayout.CENTER);

        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

	//----------------------------------------------------------------------------
	/**
	 * Invoked when the target of the listener has changed its state.
	 *
	 * @param e  a ChangeEvent object
	 */
	public void stateChanged(ChangeEvent e)
	{	
		gradient_paint_ = ((PreviewPanel)e.getSource()).getGradient();
		
		if (listener_icon_ != null)
		{
			listener_icon_.stateChanged( new ChangeEvent(this) );
		}
	}

	public void setChangeListenerIcon( ChangeListener listener_icon )
	{
		listener_icon_ = listener_icon;
	}

    public MultipleGradientPaint getGradient()
    {
        return gradient_paint_;
    }
}
