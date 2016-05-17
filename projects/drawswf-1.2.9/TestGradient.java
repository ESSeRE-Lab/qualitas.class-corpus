import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import at.bestsolution.ext.swing.dialog.JGradientChooser;

/*
 * Created on 05.04.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */

/**
 * @author tom
 */
public class TestGradient extends JFrame
{
    public TestGradient()
    {
        super("Test-Gradient");
        addWindowListener(
                new WindowAdapter()
                {
                    public void windowClosing(WindowEvent e)
                    {
                        System.exit(0);
                    }
                }
                );
    }
    
    private void showDialog()
    {
        JGradientChooser gradient_dia = JGradientChooser.getInstance();
        gradient_dia.show();
    }
    
    public static void main(String[] args)
    {
        TestGradient gradient = new TestGradient();
        gradient.show();
        gradient.showDialog();
        System.out.println("Exiting");
    }
}
