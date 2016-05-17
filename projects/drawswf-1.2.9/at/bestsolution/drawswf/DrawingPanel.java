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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/DrawingPanel.java,v 1.53 2003/06/08 11:54:18 tom Exp $
 */

package at.bestsolution.drawswf;

import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.MediaTracker;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;

import at.bestsolution.drawswf.drawobjects.*;
import at.bestsolution.ext.swing.AlphaColorButtonI;
import at.bestsolution.ext.swing.GradientButtonI;

/**
 * @author  heli
 */
public class DrawingPanel extends JPanel implements MouseListener, MouseMotionListener, ChangeListener
{
	private BasicStroke stroke_;
	private Color pen_color_;
	private Color fill_color_;
	private LinkedList draw_list_;
	private LinkedList redo_list_;
	private volatile boolean replay_;
	private Dimension size_;
	private double scale_;
	private int aspect_width_;
	private int aspect_height_;
	private int offset_x_;
	private int offset_y_;
	private Image background_image_;
	private static final Color BACKGROUND_COLOR = Color.white;
	private int drawing_mode_;
	private DrawObject draw_object_;
	private AnimationThread animation_thread_;
	private DrawObjectList draw_object_list_;
	private Point drag_start_;
	private Point gradient_start_;
	private MultipleGradientPaint gradient_;
	private static File storage_file_;

	//----------------------------------------------------------------------------
	/**
	 * Creates a new instance of DrawingPanel
	 *
	 * @param size the initial size.
	 */
	public DrawingPanel(Dimension size, DrawObjectList draw_object_list)
	{
		super();

		animation_thread_ = null;
		draw_object_list_ = draw_object_list;

		size_ = size;
		aspect_width_ = 4;
		aspect_height_ = 3;
		scale_ = 1.0;

		drawing_mode_ = DrawObjectFactory.MAX_OBJECTS; // this is also the edit mode
		setMinimumSize(size_);
		setPreferredSize(size_);
		setSize(size_);

		stroke_ = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		pen_color_ = Color.black;
		fill_color_ = Color.red;

		draw_list_ = new LinkedList();
		redo_list_ = new LinkedList();
		replay_ = false;

		addMouseListener(this);
		addMouseMotionListener(this);
		background_image_ = null;
	}

	//----------------------------------------------------------------------------
	public void loadAnimation(File file, boolean append)
	{
		LinkedList new_list = DrawObjectFactory.loadFile(file, this);
		int i;
		int aspect_width = 4;
		int aspect_height = 3;

		if (new_list != null)
		{
			if (append == false)
			{
				setStorageFile(file);

				draw_list_.clear();
				draw_object_list_.clearDrawObjects();

				try
				{
					System.out.println("Set canvas to " + new_list.get(0) + "x" + new_list.get(1));
					aspect_width = Integer.parseInt(new_list.get(0).toString()) / 200;
					aspect_height = Integer.parseInt(new_list.get(1).toString()) / 200;
				}
				catch (NumberFormatException e)
				{}

				setCanvasSize(aspect_width, aspect_height);
			}

			for (i = 2; i < new_list.size(); i++)
			{
				draw_list_.add(new_list.get(i));
				draw_object_list_.addDrawObject((DrawObject) new_list.get(i));
			}

			repaint();
		}
	}

	//----------------------------------------------------------------------------
	public void saveAnimation(File file)
	{
		setStorageFile(file);
		DrawObjectFactory.saveFile(draw_list_, file, this);
	}

	//----------------------------------------------------------------------------
	public void saveAnimation()
	{
		DrawObjectFactory.saveFile(draw_list_, getStorageFile(), this);
	}

	//----------------------------------------------------------------------------
	public void setDrawingMode(int drawing_mode)
	{
		drawing_mode_ = drawing_mode;
	}

	//----------------------------------------------------------------------------
	public void setBackgroundImage(File image_file)
	{
		if ((image_file == null) || (!image_file.exists()))
		{
			background_image_ = null;
		}
		else
		{
			background_image_ = Toolkit.getDefaultToolkit().getImage(image_file.getPath());
			MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(background_image_, 1);
			try
			{
				// wait til image is loaded.
				tracker.waitForID(1);
			}
			catch (InterruptedException e)
			{
				background_image_ = null;
			}
		}

		repaint();
	}

	//----------------------------------------------------------------------------
	public void setCanvasSize(int aspect_width, int aspect_height)
	{
		aspect_width_ = aspect_width;
		aspect_height_ = aspect_height;
		repaint();
	}

	//----------------------------------------------------------------------------
	public Dimension getCanvasSize()
	{
		return new Dimension(aspect_width_ * 200, aspect_height_ * 200);
	}

	//----------------------------------------------------------------------------
	public LinkedList getLines()
	{
		return draw_list_;
	}

	//----------------------------------------------------------------------------
	public void clearDrawing()
	{
		while (!draw_list_.isEmpty())
		{
			redo_list_.add(draw_list_.getLast());
			draw_object_list_.removeDrawObject((DrawObject) draw_list_.removeLast());
		}

		draw_object_ = null;
		repaint();
	}

	public void clear4newFile()
	{
		while (!draw_list_.isEmpty())
		{
			draw_object_list_.removeDrawObject((DrawObject) draw_list_.removeLast());
		}
		
		redo_list_ = new LinkedList();
		draw_object_ = null;
		
		MainWindow.MAIN_WINDOW.getDrawMenuBar().setEnabled("file", "save_action", false);
		MainWindow.MAIN_WINDOW.getDrawToolbar().setEnabled("file", "save_action", false);
		storage_file_ = null;
		
		repaint();		
	}

	//----------------------------------------------------------------------------
	public void redoLastDrawObject()
	{
		if (redo_list_.size() > 0)
		{
			draw_list_.add(redo_list_.getLast());
			draw_object_list_.addDrawObject((DrawObject) redo_list_.removeLast());
			repaint();
		}
	}

	//----------------------------------------------------------------------------
	public void clearLastDrawObject()
	{
		if (draw_list_.size() > 0)
		{
			if (draw_object_ == draw_list_.getLast())
			{
				draw_object_ = null;
			}

			redo_list_.add(draw_list_.getLast());
			draw_object_list_.removeDrawObject((DrawObject) draw_list_.removeLast());
			repaint();
		}
	}

	public void moveDrawingObject(int selected_index, int amount)
	{
		if (amount != 0)
		{
			if (!(selected_index + amount >= draw_list_.size() || selected_index + amount < 0))
			{
				Object tmp = draw_list_.get(selected_index);
				Object tmp2 = draw_list_.set(selected_index + amount, tmp);
				draw_list_.set(selected_index, tmp2);
				draw_object_list_.moveDrawObject(selected_index, amount);
				repaint();
			}
		}
		else
		{
			draw_object_list_.removeDrawObject((DrawObject) draw_list_.remove(selected_index));
			repaint();
		}
	}

	public LinkedList getDrawingList()
	{
		return draw_list_;
	}

	//----------------------------------------------------------------------------
	public void finishedAnimation()
	{
		animation_thread_ = null;
		replay_ = false;
	}

	//----------------------------------------------------------------------------
	public void replay(boolean start)
	{
		if ((start == true) && (animation_thread_ == null))
		{
			replay_ = true;
			animation_thread_ = new AnimationThread(this);
			//SwingUtilities.invokeLater(animation_thread_);
			animation_thread_.start();
		}
		else if (start == false)
		{
			replay_ = false;
		}
	}

	//----------------------------------------------------------------------------
	private void pause(long milli)
	{
		try
		{
			Thread.sleep(milli);
		}
		catch (InterruptedException e)
		{}
	}

	//----------------------------------------------------------------------------
	private int calculateCanvasSizeUnit()
	{
		int width = getWidth();
		int height = getHeight();
		int unit;

		if ((width / aspect_width_) < (height / aspect_height_))
		{
			unit = width / aspect_width_;
		}
		else
		{
			unit = height / aspect_height_;
		}

		return unit;
	}

	//----------------------------------------------------------------------------
	public int getCanvasWidth()
	{
		return aspect_width_ * 200;
	}

	//----------------------------------------------------------------------------
	public int getCanvasHeight()
	{
		return aspect_height_ * 200;
	}

	//----------------------------------------------------------------------------
	private void initCanvas(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int unit = calculateCanvasSizeUnit();
		int width = unit * aspect_width_;
		int height = unit * aspect_height_;
		offset_x_ = (getWidth() - width) / 2;
		offset_y_ = (getHeight() - height) / 2;

		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		if (background_image_ == null)
		{
			g.setColor(BACKGROUND_COLOR);
			g.fillRect(offset_x_, offset_y_, width, height);
		}
		else
		{
			g.drawImage(background_image_, offset_x_, offset_y_, width, height, null);
		}

		scale_ = unit / 200.0;

		g.translate(offset_x_, offset_y_);
		g.scale(scale_, scale_);
	}

	//----------------------------------------------------------------------------
	protected void paintLines()
	{
		paintLines((Graphics2D) getGraphics());
	}

	//----------------------------------------------------------------------------
	public boolean isReplay()
	{
		return replay_;
	}

	//----------------------------------------------------------------------------
	private void paintLines(Graphics2D g)
	{
		DrawObject draw_object;

		initCanvas(g);

		for (int count = 0; count < draw_list_.size(); count++)
		{
			draw_object = (DrawObject) draw_list_.get(count);
			draw_object.drawObject(g, this);
		}
	}

	//----------------------------------------------------------------------------
	public void paintComponent(Graphics g)
	{
		paintLines((Graphics2D) g);
	}

	//----------------------------------------------------------------------------
	public float getPenSize()
	{
		return stroke_.getLineWidth();
	}

	//----------------------------------------------------------------------------
	public void setPenSize(float pen_size)
	{
		stroke_ = new BasicStroke(pen_size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}

	//----------------------------------------------------------------------------
	public Color getPenColor()
	{
		return pen_color_;
	}

	//----------------------------------------------------------------------------
	public void setPenColor(Color pen_color)
	{
		pen_color_ = pen_color;
	}

	//----------------------------------------------------------------------------
	public Color getFillColor()
	{
		return fill_color_;
	}

	//----------------------------------------------------------------------------
	public void setFillColor(Color fill_color)
	{
		fill_color_ = fill_color;
	}

	//----------------------------------------------------------------------------
	public void mouseClicked(MouseEvent event)
	{}

	//----------------------------------------------------------------------------
	public void mouseEntered(MouseEvent event)
	{}

	//----------------------------------------------------------------------------
	public void mouseExited(MouseEvent event)
	{}

	//----------------------------------------------------------------------------
	private void selectObject(MouseEvent event)
	{
		DrawObject draw_object;
		DrawObject result = draw_object_;
		Point p = new Point((int) ((event.getX() - offset_x_) / scale_), (int) ((event.getY() - offset_y_) / scale_));

		ListIterator iterator = draw_list_.listIterator();
		while (iterator.hasNext())
		{
			draw_object = (DrawObject) iterator.next();

			if (draw_object.contains(p) && (result != draw_object))
			{
				result = draw_object;
			}
		}

		draw_object_ = result;

		if (draw_object_ != null)
		{
			draw_object_list_.setSelectedObject(draw_object_);
		}
	}

	//----------------------------------------------------------------------------
	private void dragObject(MouseEvent event)
	{
		if ((drawing_mode_ == DrawObjectFactory.MAX_OBJECTS) && (draw_object_ != null))
		{
			Graphics2D g = (Graphics2D) getGraphics();
			g.translate(offset_x_, offset_y_);
			g.scale(scale_, scale_);
			g.setXORMode(Color.white);
			draw_object_.drawObject(g, this);

			double x = ((event.getX() - drag_start_.getX()) / scale_);
			double y = ((event.getY() - drag_start_.getY()) / scale_);
			drag_start_ = event.getPoint();

			draw_object_.move(x, y);
			draw_object_.drawObject(g, this);
			draw_object_list_.updateObject(draw_object_);
		}
	}

	//----------------------------------------------------------------------------
	public void mousePressed(MouseEvent event)
	{
		if (drawing_mode_ == DrawObjectFactory.MAX_OBJECTS)
		{
			drag_start_ = event.getPoint();
			selectObject(event);
		}
		else if (drawing_mode_ == DrawObjectFactory.MAX_OBJECTS + 1)
		{
			gradient_start_ = new Point(event.getX() - offset_x_, event.getY() - offset_y_);
		}
		else
		{
			Graphics2D g = (Graphics2D) getGraphics();
			g.translate(offset_x_, offset_y_);
			g.scale(scale_, scale_);

			draw_object_ = DrawObjectFactory.createObject(drawing_mode_, pen_color_, fill_color_, stroke_);

			if (draw_object_ != null)
			{
				int x = (int) ((event.getX() - offset_x_) / scale_);
				int y = (int) ((event.getY() - offset_y_) / scale_);
				draw_object_.mousePressed(x, y, g);
			}
		}

		// if we do something we cannot undo an undo
		redo_list_.clear();
	}

	private void drawGradientLine(MouseEvent event)
	{
		if ((drawing_mode_ == DrawObjectFactory.MAX_OBJECTS + 1) && (draw_object_ != null))
		{
			Graphics2D g = (Graphics2D) getGraphics();
			g.translate(offset_x_, offset_y_);
			g.scale(scale_, scale_);

			MultipleGradientPaint gradient;

			double x = ((event.getX() - offset_x_) / scale_);
			double y = ((event.getY() - offset_y_) / scale_);

			if (gradient_ instanceof LinearGradientPaint)
			{
				gradient =
					new LinearGradientPaint(
						gradient_start_,
						new Point2D.Double(x, y),
						gradient_.getFractions(),
						gradient_.getColors(),
						gradient_.getCycleMethod(),
						gradient_.getColorSpace());
			}
			else
			{
				gradient =
					new RadialGradientPaint(
						gradient_start_,
						(float) gradient_start_.distance(new Point2D.Double(x, y)),
						gradient_start_,
						gradient_.getFractions(),
						gradient_.getColors(),
						gradient_.getCycleMethod(),
						gradient_.getColorSpace());
			}

			draw_object_.setGradient(gradient);

			draw_object_.drawObject(g, this);
		}
	}

	//----------------------------------------------------------------------------
	public void mouseReleased(MouseEvent event)
	{
		if (drawing_mode_ == DrawObjectFactory.MAX_OBJECTS)
		{
			if (draw_object_ != null)
			{
				draw_object_list_.updateObject(draw_object_);
			}

			repaint();
		}
		else if (drawing_mode_ == DrawObjectFactory.MAX_OBJECTS + 1)
		{
			repaint();
		}
		else
		{
			Graphics2D g = (Graphics2D) getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.translate(offset_x_, offset_y_);
			g.scale(scale_, scale_);

			if (draw_object_ != null)
			{
				int x = (int) ((event.getX() - offset_x_) / scale_);
				int y = (int) ((event.getY() - offset_y_) / scale_);

				if (draw_object_.mouseReleased(x, y, g))
				{
					draw_list_.add(draw_object_);
					draw_object_list_.addDrawObject(draw_object_);
				}
				else
				{
					draw_object_ = null;
				}
			}
		}
	}

	//----------------------------------------------------------------------------
	public void mouseDragged(MouseEvent event)
	{
		if (drawing_mode_ == DrawObjectFactory.MAX_OBJECTS)
		{
			dragObject(event);
		}
		else if (drawing_mode_ == DrawObjectFactory.MAX_OBJECTS + 1)
		{
			drawGradientLine(event);
		}
		else
		{
			Graphics2D g = (Graphics2D) getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.translate(offset_x_, offset_y_);
			g.scale(scale_, scale_);

			if (draw_object_ != null)
			{
				int x = (int) ((event.getX() - offset_x_) / scale_);
				int y = (int) ((event.getY() - offset_y_) / scale_);
				draw_object_.mouseDragged(x, y, g);
			}
		}
	}

	//----------------------------------------------------------------------------
	public void mouseMoved(MouseEvent event)
	{}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() instanceof AlphaColorButtonI)
		{
			AlphaColorButtonI button = (AlphaColorButtonI) e.getSource();

			if (button.getName().equals("pen_color"))
			{
				setPenColor(button.getColor());
			}
			else
			{
				setFillColor(button.getColor());
			}
		}
		else if (e.getSource() instanceof GradientButtonI)
		{
			System.out.println("SETTING GRADIENT");
			GradientButtonI button = (GradientButtonI) e.getSource();
			gradient_ = button.getGradient();
		}
	}

	public void setSelectedDrawingObject(int index)
	{
		draw_object_ = (DrawObject) draw_list_.get(index);
	}

	public static void setStorageFile(File storage_file)
	{
		MainWindow.MAIN_WINDOW.getDrawMenuBar().setEnabled("file", "save_action", true);
		MainWindow.MAIN_WINDOW.getDrawToolbar().setEnabled("file", "save_action", true);
		storage_file_ = storage_file;
	}

	public static File getStorageFile()
	{
		return storage_file_;
	}
}
