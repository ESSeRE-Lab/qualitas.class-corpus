/*
 * Created on 08.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package at.bestsolution.ext.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.ext.awt.MultipleGradientPaint;

import at.bestsolution.ext.swing.dialog.JGradientChooser;
import at.bestsolution.ext.swing.icon.GradientIcon;
import at.bestsolution.ext.swing.icon.IIconFactory;

/**
 * @author tom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GradientJRadioButtonMenuItem extends JRadioButtonMenuItem implements ChangeListener, ActionListener, GradientButtonI
{
	private GradientIcon gradient_icon_;
	private String title_ = "Choose Color";

	public GradientJRadioButtonMenuItem(MultipleGradientPaint gradient)
	{
		this(gradient, 20, 20, 0, 0);
	}

	public GradientJRadioButtonMenuItem(MultipleGradientPaint gradient, int width, int height, int border_x, int border_y)
	{
		super();
		gradient_icon_ = new GradientIcon(gradient);
		gradient_icon_.addChangeListener(this);
		setIcon(gradient_icon_);
		addActionListener(this);
	}

	public GradientJRadioButtonMenuItem(IIconFactory icon_provider, String name, MultipleGradientPaint gradient)
	{
		this(icon_provider, name);
		gradient_icon_.setGradient(gradient);
	}

	public GradientJRadioButtonMenuItem(IIconFactory icon_provider, String name)
	{
		super();
		gradient_icon_ = icon_provider.getGradientIcon(name);
		gradient_icon_.addChangeListener(this);
		setIcon(gradient_icon_);
		addActionListener(this);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		fireStateChanged();
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		JGradientChooser chooser = JGradientChooser.getInstance();
		chooser.setTitle( title_ );
		chooser.setChangeListenerIcon( gradient_icon_ );
		
		chooser.show(); 
	}

	/* (non-Javadoc)
	 * @see at.bestsolution.ext.swing.GradientButtonI#getGradient()
	 */
	public MultipleGradientPaint getGradient()
	{
		return gradient_icon_.getGradient();
	}

}
