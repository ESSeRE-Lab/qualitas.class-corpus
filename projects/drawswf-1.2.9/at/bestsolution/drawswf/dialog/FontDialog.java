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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/dialog/FontDialog.java,v 1.6 2003/05/07 23:10:23 tom Exp $
 */

package at.bestsolution.drawswf.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import at.bestsolution.drawswf.IconProvider;
import at.bestsolution.drawswf.drawobjects.DrawSWFFont;
import at.bestsolution.drawswf.util.DrawSWFConfig;
import at.bestsolution.ext.awt.FontLoader;
import at.bestsolution.ext.swing.AlphaColorJButton;

/**
 *
 */
public class FontDialog extends JDialog implements ChangeListener
{
    protected int status_ = JOptionPane.CANCEL_OPTION;
    protected OpenList font_names_;
    protected OpenList font_sizes_;
    protected MutableAttributeSet attributes_;
    protected int effect_;
    private JRadioButton[] effect_buttons_;
    private DrawSWFFont draw_font_;

    protected JTextField text_field_;
    private static String[] sizes_ = { "8", "9", "10", "12", "14", "18", "24", "36", "48", "64", "96" };
    protected EventListenerList listener_list_ = new EventListenerList();
    private AlphaColorJButton color_button_;
    private FontDialog self_;

    //----------------------------------------------------------------------------
    /**
     * Constructor for FontDialog
     *
     * @param parent the parent frame needed by all dialogs
     * @param text the text we want to set.
     */
    public FontDialog(JFrame parent)
    {
        super(parent, "Font", true);

        effect_ = DrawSWFFont.NO_EFFECT;
        effect_buttons_ = new JRadioButton[3];
        draw_font_ = null;
        self_ = this;

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(createFontSelection());
        getContentPane().add(createFontEffects());
        getContentPane().add(createColorChoice());
        getContentPane().add(Box.createVerticalStrut(5));
        getContentPane().add(createTextField());
        getContentPane().add(createButtons());

        updatePreview();

        pack();
        Dimension d1 = getSize();
        Dimension d2 = parent.getSize();
        int x = Math.max((d2.width - d1.width) / 2, 0);
        int y = Math.max((d2.height - d1.height) / 2, 0);
        setBounds(x, y, d1.width, d1.height);
    }

    private JPanel createColorChoice()
    {
        JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT) );
        panel.setBorder(new TitledBorder(new EtchedBorder(), "Background"));
        
        if( DrawSWFConfig.getInstance().getProperty("independent_text_color").equals("true") )
        {
			color_button_ = new AlphaColorJButton( IconProvider.getInstance(), "text_color" );
        }
        else
        {
			color_button_ = new AlphaColorJButton( IconProvider.getInstance(), "fill_color" );
        }
        
        panel.add(color_button_);

        return panel;
    }

    //----------------------------------------------------------------------------
    private JPanel createFontEffects()
    {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 5));
        panel.setToolTipText("Effects are only visible in Flash.");
        panel.setBorder(new TitledBorder(new EtchedBorder(), "Effects"));

        ButtonGroup group = new ButtonGroup();
        ActionListener listener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                effect_ = Integer.parseInt(e.getActionCommand());
            }
        };

        effect_buttons_[0] = new JRadioButton("No effect");
        effect_buttons_[0].setSelected(true);
        effect_buttons_[0].setActionCommand("" + DrawSWFFont.NO_EFFECT);
        panel.add(effect_buttons_[0]);
        group.add(effect_buttons_[0]);
        effect_buttons_[0].addActionListener(listener);

        effect_buttons_[1] = new JRadioButton("Type chars");
        effect_buttons_[1].setActionCommand("" + DrawSWFFont.EFFECT_TYPE);
        panel.add(effect_buttons_[1]);
        group.add(effect_buttons_[1]);
        effect_buttons_[1].addActionListener(listener);

        effect_buttons_[2] = new JRadioButton("Fade in");
        effect_buttons_[2].setActionCommand("" + DrawSWFFont.EFFECT_FADE_IN);
        panel.add(effect_buttons_[2]);
        group.add(effect_buttons_[2]);
        effect_buttons_[2].addActionListener(listener);

        return panel;
    }

    //----------------------------------------------------------------------------
    private JPanel createFontSelection()
    {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 2));
        panel.setBorder(new TitledBorder(new EtchedBorder(), "Font"));

        FontLoader loader = FontLoader.getInstance();

        font_names_ = new OpenList(loader.getFontNames(), "Name:");
        panel.add(font_names_);

        font_sizes_ = new OpenList(sizes_, "Size:");
        panel.add(font_sizes_);

        ListSelectionListener lsel = new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                // System.out.println("MODIFICATION");
                updatePreview();
            }
        };

        font_sizes_.setSelected("24");
        font_names_.setSelected(loader.getFontNames()[0]);

        font_names_.addListSelectionListener(lsel);
        font_sizes_.addListSelectionListener(lsel);

        return panel;
    }

    //----------------------------------------------------------------------------
    private JPanel createButtons()
    {
        JPanel panel = new JPanel(new FlowLayout());
        JPanel inner_panel = new JPanel(new GridLayout(1, 2, 10, 2));
        JButton button = new JButton("OK");
        ActionListener action_listener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // System.out.println( "REMOVED" );
                color_button_.removeChangeListener( self_ );
                status_ = JOptionPane.OK_OPTION;
                draw_font_ = new DrawSWFFont(text_field_.getFont(), effect_, text_field_.getText(),color_button_.getColor());
                setVisible(false);
                fireStateChanged();
            }
        };
        
        button.addActionListener(action_listener);
        inner_panel.add(button);

        button = new JButton("Cancel");
        action_listener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                color_button_.removeChangeListener( self_ );
                status_ = JOptionPane.CANCEL_OPTION;
                setVisible(false);
            }
        };
        button.addActionListener(action_listener);
        inner_panel.add(button);
        panel.add(inner_panel);

        return panel;
    }

    //----------------------------------------------------------------------------
    private JPanel createTextField()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder(new EtchedBorder(), "Text"));
        text_field_ = new JTextField("Preview Font");
        text_field_.setBackground(Color.white);
        text_field_.setForeground(Color.black);
        text_field_.setOpaque(true);
        text_field_.setBorder(new LineBorder(Color.black));
        text_field_.setPreferredSize(new Dimension(120, 40));
        panel.add(text_field_, BorderLayout.CENTER);

        return panel;
    }

    //----------------------------------------------------------------------------
    public void setAttributes(AttributeSet a)
    {
        attributes_ = new SimpleAttributeSet(a);
        String name = StyleConstants.getFontFamily(a);
        font_names_.setSelected(name);
        int size = StyleConstants.getFontSize(a);
        font_sizes_.setSelectedInt(size);

        updatePreview();
    }

    //----------------------------------------------------------------------------
    public AttributeSet getAttributes()
    {
        if (attributes_ != null)
        {
            StyleConstants.setFontFamily(attributes_, font_names_.getSelected());
            StyleConstants.setFontSize(attributes_, font_sizes_.getSelectedInt());
        }

        return attributes_;
    }

    //----------------------------------------------------------------------------
    public int getOption()
    {
        return status_;
    }

    public void show()
    {
        draw_font_ = new DrawSWFFont(text_field_.getFont(), effect_, text_field_.getText(), color_button_.getColor() );
        color_button_.addChangeListener(self_);
        super.show();
    }

    //----------------------------------------------------------------------------
    public void setDrawSWFFont(DrawSWFFont font)
    {
        font_names_.setSelected(font.getAWTFont().getFontName());
        font_sizes_.setSelected(Integer.toString(font.getAWTFont().getSize()));
        effect_buttons_[font.getEffect()].setSelected(true);
        draw_font_ = font;
        text_field_.setText(font.getText());
        color_button_.setColor( font.getColor() );
        updatePreview();
    }

    //----------------------------------------------------------------------------
    public DrawSWFFont getDrawSWFFont()
    {
        return draw_font_;
    }

    public void setTextInputEnabled(boolean yes)
    {
        text_field_.setEditable(yes);
    }

    //----------------------------------------------------------------------------
    protected void updatePreview()
    {
        // FontContainer font = ((at.bestsolution.drawswf.fontdialog.FontContainer) font_names_.getSelectedObject());

        // System.out.println("FONT:" + font_names_.getSelected() );

        String name = font_names_.getSelected();
        int size = font_sizes_.getSelectedInt();

        if (size > 0 && name != null)
        {
            Font real_font = FontLoader.getInstance().getFont(name, Font.PLAIN, size);
            text_field_.setFont(real_font);
            text_field_.repaint();
        }

        draw_font_ = new DrawSWFFont(text_field_.getFont(), effect_, text_field_.getText(), color_button_.getColor() );
        fireStateChanged();
    }

    public void addChangeListener(ChangeListener l)
    {
        listener_list_.add(ChangeListener.class, l);
    }

    public EventListenerList removeAllChangeListeners()
    {
        EventListenerList list = listener_list_;
        listener_list_ = new EventListenerList();
        
        return list;
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created.
     * @see EventListenerList
     */
    protected void fireStateChanged()
    {
        // Guaranteed to return a non-null array
        Object[] listeners = listener_list_.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == ChangeListener.class)
            {
                ((ChangeListener) listeners[i + 1]).stateChanged(new ChangeEvent(this));
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        if( e.getSource() instanceof AlphaColorJButton )
        {
            draw_font_ = new DrawSWFFont(text_field_.getFont(), effect_, text_field_.getText(), ((AlphaColorJButton)e.getSource()).getColor() );
        }
        
        fireStateChanged();
    }

}
