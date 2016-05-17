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
 *  $Header: /usr/bestsolution/cvsroot/java/draw_swf/at/bestsolution/drawswf/AnimationThread.java,v 1.2 2002/10/02 13:12:23 heli Exp $
 */

package at.bestsolution.drawswf;

/**
 *
 * @author  tom
 */
public class AnimationThread extends Thread
{
    private DrawingPanel panel_;
    
    //----------------------------------------------------------------------------
    /** Creates a new instance of AnimationThread */
    public AnimationThread( DrawingPanel panel )
    {
        panel_   = panel;
        setPriority(Thread.MIN_PRIORITY);
    }
    
    //----------------------------------------------------------------------------
    public void run()
    {
        panel_.paintLines();
        panel_.finishedAnimation();
    }
}
