/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: c-jdbc@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Emmanuel Cecchet.
 * Contributor(s): Mathieu Peltier.
 */

package org.objectweb.cjdbc.console.views;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import org.objectweb.cjdbc.common.i18n.Translate;

/**
 * Graphical SQL statistics viewer. Quick and dirty implementation.
 * 
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public abstract class InfoViewer
{

  private InfoTableSorter sorter;
  private JPanel          panel;
  private JFrame          frame;
  private InfoTableModel  model;

  // Info Options
  private String[]        columnNames;
  protected String        frameTitle;
  protected String        infoViewerMenuBarString;
  protected String        actionToolTipText;
  protected String        actionErrorMessage;
  protected String        actionSuccessMessage;
  protected String        tableHeaderToolTipText;

  private Object[][]      data;

  /**
   * Create a InfoViewer
   * 
   * @param data Stats to display in the table
   */
  public InfoViewer(Object[][] data)
  {
    if (data != null)
    {
      this.data = getDataTypes(data);
      columnNames = getColumnNames();
      setLabels();
    }
  }

  /**
   * Subclasses should overide this method to get coherent sorting
   * 
   * @param stats to display
   * @return same sized objects array but with proper types default is strings
   *         only
   */
  protected abstract Object[][] getDataTypes(Object[][] stats);

  /**
   * Get column names
   * 
   * @return a array of strings
   */
  public abstract String[] getColumnNames();

  /**
   * Return the list of traceable data for this viewer
   * 
   * @return an array of names
   */
  public int[] getTraceableColumns()
  {
    return new int[0];
  }

  /**
   * Set the labels for the frame
   */
  public abstract void setLabels();

  /**
   * Update the data in the InfoTableModel and refresh the frame
   * 
   * @param data fresh and new
   */
  public void updateData(Object[][] data)
  {
    this.data = getDataTypes(data);
    if (frame != null)
    {
      frame.repaint();
      frame.setVisible(true);
      model.setData(data);
    }
    else
    {
      createAndShowGUI();
    }
  }

  /**
   * For thread safety, this method should be invoked from the event-dispatching
   * thread.
   */
  private void createAndShowGUI()
  {
    panel = new JPanel(new GridLayout(1, 0));
    model = new InfoTableModel(data);

    sorter = new InfoTableSorter(model);
    JTable table = new JTable(sorter); //NEW
    sorter.addMouseListenerToHeaderInTable(table); //ADDED
    // THIS
    table.setPreferredScrollableViewportSize(new Dimension(640, 200));
    table.getColumnModel().getColumn(0).setPreferredWidth(340);
    for (int i = 1; i < columnNames.length; i++)
      table.getColumnModel().getColumn(i).setPreferredWidth(50);

    //Set up tool tips for column headers.
    table.getTableHeader().setToolTipText(tableHeaderToolTipText);

    //Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(table);

    //Add the scroll pane to this panel.
    panel.add(scrollPane);

    //Create and set up the window.
    frame = new JFrame(frameTitle);
    frame.setJMenuBar(new InfoViewerMenuBar());
    frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    panel.setOpaque(true); // content panes must be opaque
    frame.setContentPane(panel);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Display text. This allows to show text without loading graphic contents
   * 
   * @param data to display
   * @return a formatted <code>String</code>
   */
  public String displayText(Object[][] data)
  {
    this.data = getDataTypes(data);
    columnNames = getColumnNames();
    setLabels();
    return displayText(getDataTypes(data));
  }

  /**
   * Format data for text consoles
   * 
   * @param data to display
   * @return a formatted string with tabs and end of line
   */
  public String displayText(String[][] data)
  {
    if (data == null)
    {
      return "";
    }

    /* Lasse's version starts here */

    // constants used for formatting the output
    final String columnPadding = "    ";
    final String nameValueSeparator = ":  ";

    // holds the column names
    String[] columns = getColumnNames();

    // solve the maximum length for column names
    // TODO: refactor this into its own method
    int maxNameLength = 0;
    for (int i = 0; i < columns.length; i++)
    {
      maxNameLength = Math.max(maxNameLength, columns[i].length());
    }

    // solve the maximum length for column values
    // TODO: refactor this into its own method
    int maxValueLength = 0;
    for (int i = 0; i < data.length; i++)
    {
      for (int j = 0; j < data[i].length; j++)
      {
        maxValueLength = Math.max(maxValueLength, data[i][j].length());
      }
    }

    // construct a separator line based on maximum column and value lengths
    // TODO: extract numbers into constants and this block into a new method
    char[] separator = new char[columnPadding.length() + maxNameLength
        + nameValueSeparator.length() + maxValueLength + 1]; /*
                                                              * the newline
                                                              * character
                                                              */
    for (int i = 0; i < separator.length; i++)
    {
      separator[i] = '-';
    }
    separator[separator.length - 1] = '\n';

    // loop through all the data and print padded lines into the StringBuffer
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < data.length; i++)
    {
      sb.append(separator);
      for (int j = 0; j < data[i].length; j++)
      {
        // create the padding needed for this particular column
        // TODO: extract this into its own method
        char[] namePadding = new char[maxNameLength - columns[j].length()];
        for (int x = 0; x < namePadding.length; x++)
        {
          namePadding[x] = ' ';
        }

        sb.append(columnPadding);
        sb.append(columns[j]);
        sb.append(nameValueSeparator);
        sb.append(namePadding);
        sb.append(data[i][j]);
        sb.append("\n");
      }
      if (i + 1 == data.length)
      {
        sb.append(separator);
      }
    }
    return sb.toString();
  }

  /**
   * Create the GUI and show it.
   */
  public void display()
  {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        createAndShowGUI();
      }
    });
  }

  /** <code>SQLStatViewer</code> menu bar. */
  protected final class InfoViewerMenuBar extends JMenuBar
  {

    /** Creates an new <code>SQLStatViewerMenuBar</code> menu bar. */
    private InfoViewerMenuBar()
    {
      JMenu menu = new JMenu(infoViewerMenuBarString);
      JMenuItem menuItem = new JMenuItem(new ExportAction());
      menuItem.setText("Save As...");
      menuItem.setMnemonic('S');
      menuItem.setAccelerator(KeyStroke
          .getKeyStroke('s', ActionEvent.CTRL_MASK));
      menu.add(menuItem);
      add(menu);
    }
  }

  /** <code>InfoViewer</code> export action. */
  protected class ExportAction extends AbstractAction
  {

    protected static final String SEPARATOR = "\t";
    protected File                outputFile;

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
      // Open file
      JFileChooser chooser = new JFileChooser(outputFile);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setApproveButtonText("Export");
      chooser.setApproveButtonMnemonic('s');
      chooser.setApproveButtonToolTipText(actionToolTipText);
      chooser.setDialogTitle("Choose the file name");

      if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
      {
        outputFile = chooser.getSelectedFile();
        if (outputFile != null)
        {
          // Export data
          try
          {
            PrintStream out = new PrintStream(new FileOutputStream(outputFile));
            int columnNumber, rowNumber;
            columnNumber = sorter.getColumnCount();
            rowNumber = sorter.getRowCount();
            for (int i = 0; i < rowNumber; i++)
            {
              for (int j = 0; j < columnNumber; j++)
              {
                out.print(sorter.getValueAt(i, j));
                out.print(SEPARATOR);
              }
              out.println();
            }
            out.close();
          }
          catch (Exception ex)
          {
            JOptionPane.showMessageDialog(frame, Translate.get(
                actionErrorMessage, ex), "Unexpected Error",
                JOptionPane.ERROR_MESSAGE);
            return;
          }
          JOptionPane.showMessageDialog(frame, Translate.get(
              actionSuccessMessage, outputFile), "Action Performed",
              JOptionPane.INFORMATION_MESSAGE);
        }
      }
    }
  }

  /**
   * This class defines a InfoTableModel
   * 
   * @version 1.0
   */
  class InfoTableModel extends AbstractTableModel
  {
    private Object[][] data;

    /**
     * Creates a new <code>InfoTableModel</code> object
     * 
     * @param stats <code>Object[][]</code> instance with data
     */
    public InfoTableModel(Object[][] stats)
    {
      this.data = stats;
    }

    /**
     * Set the data of this <tt>InfoTableModel</tt>
     * 
     * @param data <code>Object[][]</code> instance with data
     */
    public void setData(Object[][] data)
    {
      this.data = data;
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
      return columnNames.length;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
      return data.length;
    }

    /**
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getColumnName(int col)
    {
      return columnNames[col];
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col)
    {
      return data[row][col];
    }

    /**
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the last column would
     * contain text ("true"/"false"), rather than a check box.
     */
    public Class getColumnClass(int c)
    {
      return getValueAt(0, c).getClass();
    }

    /**
     * Don't need to implement this method unless your table's editable.
     */
    public boolean isCellEditable(int row, int col)
    {
      return false;
    }
  }

  /**
   * @return Returns the frameTitle.
   */
  public String getFrameTitle()
  {
    return frameTitle;
  }

  /**
   * @return Returns the data.
   */
  public Object[][] getData()
  {
    return data;
  }
}