/*  UniqueColors.java  */

package nik777.chat;

/*
 * <name>: <brief>
 *
 * Copyright (C) 200x <author> <location>
 *
 * Author: Nik Trevallyn-Jones, nik777@users.sourceforge.net
 * $Id: Exp $
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See version 2 of the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with this program. If not, the license, including version 2, is available
 * from the GNU project, at http://www.gnu.org.
 */

/*
 *  This is a derivative work, based on the UniqueColors.java class from the
 *  JabberApplet project, the copyright of which is included below.
 */
/***************************************************************************
 * The contents of this file are subject to the Jabber Open Source License
 * Version 1.0 (the "License").  You may not copy or use this file, in
 * either source code or executable form, except in compliance with the 
 * License.  You may obtain a copy of the License at 
 * 
 * http://www.jabber.com/license/ or at http://www.opensource.org/.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the
 * License for the specific language governing rights and limitations 
 * under the License.
 * 
 * Copyrights
 * 
 * Portions created by or assigned to Jabber.com, Inc. are
 * Copyright (c) 1999-2000 Jabber.com, Inc.  All Rights Reserved.  Contact
 * information for Jabber.com, Inc. is available at http://www.jabber.com/.
 * 
 * Acknowledgements
 * 
 * Special thanks to the Jabber Open Source Contributors for their
 * suggestions and support of Jabber.
 ***************************************************************************/

/**
 * Create unique rgb colors.  Currently 12 unique colors are infinitely cycled thru.
 * 
 * @author David Scott
 */
public class UniqueColors {

    /**
     * array of r values Rgb to cycle thru
     */
    private String rVals[] = {"FF", "00", "33", "99"};

    /**
     * array of g values (rGb) to be cycled thru
     */
    private String gVals[] = {"33", "CC", "99"};

    /**
     * array of b values (rgB) to be cycled thru
     */
    private String bVals[] = {"00", "FF", "66", "CC"};

    /**
     * index into gVals array
     */
    private int gIndex = 0;

    /**
     * index into rVals and bVals array
     */
    private int rbIndex = -1;

    /**
     */
    public UniqueColors() {}

    /**
     * Get the next unique RGB value from the list
     * 
     * @return unique RGB value
     */
    public String getNextRGB() {
        rbIndex++;                  // outer loop changes the R and B values
        if (rbIndex > 3)            // loop complete?
        {
            rbIndex = 0;            // restart R/B values
            gIndex++;               // inner looo B values
            if (gIndex > 2)         // reached limit of G?
                gIndex = 0;         // start the G value from start
        }
        return( rVals[rbIndex] + gVals[gIndex] + bVals[rbIndex] );
    }
}
