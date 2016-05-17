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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): 
 */

package org.objectweb.cjdbc.console.monitoring;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.objectweb.cjdbc.common.jmx.mbeans.DataCollectorMBean;
import org.objectweb.cjdbc.common.monitor.AbstractDataCollector;

/**
 * Encapsulate the JFreeChart classes and methods to provide easiness of
 * configuration for a monitoring graph. This <code>MonitoringGraph</code>
 * also contains the thread that retrieves the information at the moment.
 * <p>
 * TODO: Should take out this <code>Thread</code> so that it connects to a
 * monitoring repository.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 */
public class MonitoringGraph extends Thread
{
  /*
   * Graph Components
   */
  private AbstractDataCollector collector;
  private JFreeChart            chart;
  private ChartPanel            panel;
  private XYSeries              series;
  private XYSeriesCollection    dataset;
  private DataCollectorMBean    jmxClient;
  private JFrame                frame;

  /*
   * Graph settings
   */
  private int                   frameHeight           = 250;
  private int                   frameWidth            = 600;
  private long                  frequency             = 1000;
  private long                  displayFrequency      = 10;
  private long                  poolingSpeed          = 1;
  private long                  timeStarted;
  private int                   timeFrame             = 10;
  private long                  repeat                = -1;
  private boolean               stop                  = false;
  private boolean               framed                = true;
  private boolean               saveOnFinish          = false;
  private boolean               display               = true;
  private String                text                  = "";

  // Buffer values
  private long                  displayFrequencyCount = 0;
  private XYSeries              buffer;

  /**
   * Creates a new <code>MonitoringGraph</code> object
   * 
   * @param collector An <code>AbstractDataCollector</code> object
   * @param jmxClient a <code>DataCollectorJmxClient</code> object
   */
  public MonitoringGraph(AbstractDataCollector collector,
      DataCollectorMBean jmxClient)
  {
    this.collector = collector;
    this.jmxClient = jmxClient;

    buffer = new XYSeries("Buffer");

    series = new XYSeries(collector.getTargetName());
    series.setMaximumItemCount(timeFrame);
    dataset = new XYSeriesCollection(series);
    chart = ChartFactory.createXYLineChart(collector.getDescription(),
        "Time (Started at:"
            + new SimpleDateFormat("HH:mm:ss").format(new Date()) + ")", "",
        dataset, PlotOrientation.VERTICAL, true, false, false);
    panel = new ChartPanel(chart);
    panel.setSize(frameWidth, frameHeight);

    if (display)
      display();
  }

  /**
   * Display the graph on screen
   */
  public void display()
  {
    chart.setBorderVisible(false);
    panel.setVisible(true);
    if (framed)
    {
      frame = new JFrame(collector.getDescription());
      frame.getContentPane().add(panel);
      frame.setSize(new java.awt.Dimension(frameWidth, frameHeight));
      RefineryUtilities.centerFrameOnScreen(frame);
      frame.setVisible(true);
      frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }
  }

  /**
   * Add a data entry to the current serie. Bufferize given the buffer series
   * size
   * 
   * @param newValue new data to add
   */
  public void addData(long newValue)
  {
    long now = (System.currentTimeMillis() / 1000) - timeStarted;
    if (displayFrequency == 0)
    {
      series.add(now, newValue);
    }
    else if (displayFrequencyCount < displayFrequency)
    {
      displayFrequencyCount++;
      buffer.add(now, newValue);
    }
    else
    {
      int count = buffer.getItemCount();
      for (int i = 0; i < count; i++)
      {
        series.add(buffer.getDataItem(i));
      }
      buffer = new XYSeries("buffer");
      displayFrequencyCount = 0;
    }
  }

  /**
   * Save the graph into a file
   * 
   * @throws IOException if an error occurs
   */
  public void saveAs() throws IOException
  {
    String fileName = collector.getTargetName() + "-"
        + new SimpleDateFormat().format(new Date());
    ChartUtilities.saveChartAsJPEG(new File(fileName), this.chart, frameWidth,
        frameHeight);
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    timeStarted = System.currentTimeMillis() / 1000;
    int count = 0;
    while (repeat == -1 || count < repeat)
    {
      count++;
      synchronized (this)
      {
        try
        {
          addData(jmxClient.retrieveData(collector));
          wait(frequency);
          if (stop)
            break;

          if (display == true && panel.isShowing() == false)
          {
            if (panel.getParent().isShowing() == false)
              stop = true;
          }

        }
        catch (Exception e)
        {
          stop = true;
          throw new RuntimeException(e.getMessage());
        }
      }
    }
    if (saveOnFinish)
    {
      try
      {
        saveAs();
      }
      catch (Exception e)
      {
        //ignore
      }
    }
  }

  /**
   * @return Returns the collector.
   */
  public AbstractDataCollector getCollector()
  {
    return collector;
  }

  /**
   * @param collector The collector to set.
   */
  public void setCollector(AbstractDataCollector collector)
  {
    this.collector = collector;
  }

  /**
   * @return Returns the frame.
   */
  public JFrame getFrame()
  {
    return frame;
  }

  /**
   * @param frame The frame to set.
   */
  public void setFrame(JFrame frame)
  {
    this.frame = frame;
  }

  /**
   * @return Returns the framed.
   */
  public boolean getFramed()
  {
    return framed;
  }

  /**
   * @param framed The framed to set.
   */
  public void setFramed(boolean framed)
  {
    this.framed = framed;
  }

  /**
   * @return Returns the frequency.
   */
  public long getFrequency()
  {
    return frequency;
  }

  /**
   * @param frequency The frequency to set.
   */
  public void setFrequency(long frequency)
  {
    this.frequency = frequency;
  }

  /**
   * @return Returns the repeat.
   */
  public long getRepeat()
  {
    return repeat;
  }

  /**
   * @param repeat The repeat to set.
   */
  public void setRepeat(long repeat)
  {
    this.repeat = repeat;
  }

  /**
   * @return Returns the stop.
   */
  public boolean getStop()
  {
    return stop;
  }

  /**
   * @param stop The stop to set.
   */
  public void setStop(boolean stop)
  {
    this.stop = stop;
  }

  /**
   * @return Returns the timeFrame.
   */
  public int getTimeFrame()
  {
    return timeFrame;
  }

  /**
   * @param timeFrame The timeFrame to set.
   */
  public void setTimeFrame(int timeFrame)
  {
    this.timeFrame = timeFrame;
    series.setMaximumItemCount(timeFrame);
  }

  /**
   * @return Returns the chart.
   */
  public JFreeChart getChart()
  {
    return chart;
  }

  /**
   * @return Returns the panel.
   */
  public ChartPanel getPanel()
  {
    return panel;
  }

  /**
   * @return Returns the timeStarted.
   */
  public long getTimeStarted()
  {
    return timeStarted;
  }

  /**
   * @return Returns the display.
   */
  public boolean getDisplay()
  {
    return display;
  }

  /**
   * @param display The display to set.
   */
  public void setDisplay(boolean display)
  {
    this.display = display;
  }

  /**
   * @return Returns the frameHeight.
   */
  public int getFrameHeight()
  {
    return frameHeight;
  }

  /**
   * @param frameHeight The frameHeight to set.
   */
  public void setFrameHeight(int frameHeight)
  {
    this.frameHeight = frameHeight;
  }

  /**
   * @return Returns the frameWidth.
   */
  public int getFrameWidth()
  {
    return frameWidth;
  }

  /**
   * @param frameWidth The frameWidth to set.
   */
  public void setFrameWidth(int frameWidth)
  {
    this.frameWidth = frameWidth;
  }

  /**
   * @return Returns the saveOnFinish.
   */
  public boolean getSaveOnFinish()
  {
    return saveOnFinish;
  }

  /**
   * @param saveOnFinish The saveOnFinish to set.
   */
  public void setSaveOnFinish(boolean saveOnFinish)
  {
    this.saveOnFinish = saveOnFinish;
  }

  /**
   * @return Returns the text.
   */
  public String getText()
  {
    return text;
  }

  /**
   * @param text The text to set.
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   * @return Returns the poolingSpeed.
   */
  public long getPoolingSpeed()
  {
    return poolingSpeed;
  }

  /**
   * @param poolingSpeed The poolingSpeed to set.
   */
  public void setPoolingSpeed(long poolingSpeed)
  {
    this.poolingSpeed = poolingSpeed;
  }

  /**
   * @return Returns the displayFrequency.
   */
  public long getDisplayFrequency()
  {
    return displayFrequency;
  }

  /**
   * @param displayFrequency The displayFrequency to set.
   */
  public void setDisplayFrequency(long displayFrequency)
  {
    this.displayFrequency = displayFrequency;
  }
}