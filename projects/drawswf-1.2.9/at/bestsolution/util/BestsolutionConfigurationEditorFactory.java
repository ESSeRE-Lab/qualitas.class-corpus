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
 * Created on 06.03.2003
 *
 */
package at.bestsolution.util;

import java.util.Hashtable;
import at.bestsolution.util.BestsolutionConfigurationEditor;

/**
 * @author tom
 */
public class BestsolutionConfigurationEditorFactory
{
    private static Hashtable editors_ = new Hashtable();
    
    public static BestsolutionConfigurationEditor getEditor( BestsolutionConfiguration config )
    {
        BestsolutionConfigurationEditor editor;
        
        if( ! editors_.containsKey(config) )
        {
            editor = new BestsolutionConfigurationEditor();
            editor.setConfiguration(config);
            editors_.put(config,editor);
        }
        else
        {
            editor = (BestsolutionConfigurationEditor)editors_.get(config);
        }
        
        return editor;
    }
}
