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
 * Created on 16.02.2003
 *
 */
package at.bestsolution.drawswf.plugin.remotesave.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.MultipartPostMethod;

import at.bestsolution.drawswf.FlashGenerator;
import at.bestsolution.drawswf.plugin.remotesave.RemoteSavePlugin;
import at.bestsolution.drawswf.plugin.remotesave.util.RemoteSaveConfig;

/**
 * @author tom
 */
public class RemoteSaveDialog extends JDialog implements ActionListener
{
    private JTextField remote_uri_;
    private JTextField remote_pwd_;
    private JTextField remote_user_;
    private JTextField upload_name_;
    private static RemoteSaveDialog instance_ = null;

    private RemoteSaveDialog()
    {
        super(RemoteSavePlugin.getMainWindow(), RemoteSavePlugin.getI18n().getString("RemoteSaveTitle"));
        initComponents();
    }

    public static RemoteSaveDialog getInstance()
    {
        if (instance_ == null)
        {
            instance_ = new RemoteSaveDialog();
        }

        return instance_;
    }

    private void initComponents()
    {
        JPanel field_pane = new JPanel();
        JPanel button_pane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel root_pane = new JPanel(new BorderLayout());
        JLabel label;
        JButton button;
        RemoteSaveConfig remote_config = RemoteSaveConfig.getInstance();

        field_pane.setLayout(new GridLayout(4, 2));

        label = new JLabel(RemoteSavePlugin.getI18n().getString("RemoteSaveDialogRemoteURI"));
        remote_uri_ = new JTextField(remote_config.getProperty("remote_uri"));

        field_pane.add(label);
        field_pane.add(remote_uri_);

        label = new JLabel(RemoteSavePlugin.getI18n().getString("RemoteSaveDialogRemotePwd"));
        remote_pwd_ = new JTextField(remote_config.getProperty("remote_pwd"));

        field_pane.add(label);
        field_pane.add(remote_pwd_);

        label = new JLabel(RemoteSavePlugin.getI18n().getString("RemoteSaveDialogRemoteUser"));
        remote_user_ = new JTextField(remote_config.getProperty("remote_user"));

        field_pane.add(label);
        field_pane.add(remote_user_);

        label = new JLabel(RemoteSavePlugin.getI18n().getString("RemoteSaveDialogRemoteFileName"));
        upload_name_ = new JTextField("drawswf.swf");

        field_pane.add(label);
        field_pane.add(upload_name_);

        root_pane.add(field_pane, BorderLayout.NORTH);

        button = new JButton();
        button.setText(RemoteSavePlugin.getI18n().getString("RemoteSaveDialogSave"));
        button.setActionCommand("Save");
        button.addActionListener(this);

        button_pane.add(button);

        button = new JButton();
        button.setText(RemoteSavePlugin.getI18n().getString("RemoteSaveDialogCancel"));
        button.setActionCommand("Cancel");
        button.addActionListener(this);

        button_pane.add(button);

        root_pane.add(button_pane);

        getContentPane().add(root_pane);

        pack();
    }

    /* 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        hide();

        if (e.getActionCommand().equals("Save"))
        {
            RemoteSaveConfig remote_config = RemoteSaveConfig.getInstance();
            remote_config.setProperty("remote_uri", remote_uri_.getText());
            remote_config.setProperty("remote_pwd", remote_pwd_.getText());
            remote_config.setProperty("remote_user", remote_user_.getText());
            saveToRemoteHost(upload_name_.getText());
        }
    }

    private void saveToRemoteHost(String filename)
    {
        URL remote_url = null;

        Object[] speed_values = { "1", "2", "3", "4", "5", "10" };
        RemoteSaveConfig remote_config = RemoteSaveConfig.getInstance();

        String selected_value =
            (String) JOptionPane.showInputDialog(null, RemoteSavePlugin.getI18n().getString("RemoteSaveSaveSWFActionTitle"), RemoteSavePlugin.getI18n().getString("RemoteSaveSaveSWFActionTitle"), JOptionPane.INFORMATION_MESSAGE, null, speed_values, speed_values[1]);

        if (selected_value != null)
        {
            int speed = Integer.parseInt(selected_value);

            FlashGenerator generator = new FlashGenerator(RemoteSavePlugin.getDrawingPanel().getCanvasSize(), speed);
            File selected_file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
            generator.generateFile(selected_file.getPath(), RemoteSavePlugin.getDrawingPanel().getLines());

            try
            {
                remote_url = new URL(remote_config.getProperty("remote_uri"));
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }

            MultipartPostMethod file_post = new MultipartPostMethod();

            if (remote_url.getPath() == null)
            {
                file_post.setPath("/");
            }
            else
            {
                file_post.setPath(remote_url.getPath());
            }

            try
            {
                file_post.addParameter("swf", selected_file);
                HttpClient client = new HttpClient();
                client.getState().setCredentials("realm", new UsernamePasswordCredentials(remote_config.getProperty("remote_user"), remote_config.getProperty("remote_pwd")));
                HostConfiguration hc = new HostConfiguration();
                file_post.setDoAuthentication(true);
                hc.setHost(new URI(remote_url));
                client.setHostConfiguration(hc);
                int status = client.executeMethod(file_post);
                file_post.releaseConnection();

                if (status == 200)
                {
                    JOptionPane.showMessageDialog(
                        RemoteSavePlugin.getMainWindow(),
                        RemoteSavePlugin.getI18n().getString("RemoteSaveDialogRemoteSaveSuccess"),
                        "Done",
                        JOptionPane.INFORMATION_MESSAGE);
                }
                else
                {
                    JOptionPane.showMessageDialog(
                        RemoteSavePlugin.getMainWindow(),
                        RemoteSavePlugin.getI18n().getString("RemoteSaveDialogRemoteSaveFailure"),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (FileNotFoundException ex)
            {
                // todo - put real code here
                ex.printStackTrace();
            }
            catch (HttpException ex)
            {
                // todo - put real code here
                ex.printStackTrace();
            }
            catch (java.io.IOException ex)
            {
                // todo - put real code here
                ex.printStackTrace();
            }
        }
    }
}
