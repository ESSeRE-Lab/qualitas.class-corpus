/* $Id: IDLFileImport.java 231 2010-01-12 18:36:00Z linus $
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tfmorris
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 2004-2008 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.uml.reveng.idl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.argouml.kernel.Project;
import org.argouml.taskmgmt.ProgressMonitor;
import org.argouml.uml.reveng.FileImportUtils;
import org.argouml.uml.reveng.ImportInterface;
import org.argouml.uml.reveng.ImportSettings;
import org.argouml.uml.reveng.ImporterManager;
import org.argouml.uml.reveng.SettingsTypes;
import org.argouml.util.SuffixFilter;

import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * This is the main class for the IDL import.
 *
 * @author Andreas Rueckert a_rueckert@gmx.net
 */
public class IDLFileImport implements ImportInterface {

    /////////////////////////////////////////////////////////
    // Instance variables

    // The current project.
    private Project currentProject = null;

    /**
     * Default constructor.
     */
    public IDLFileImport() {
        super();
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#parseFiles(org.argouml.kernel.Project, java.util.Collection, org.argouml.uml.reveng.ImportSettings, org.argouml.application.api.ProgressMonitor)
     */
    public Collection parseFiles(Project p, Collection files,
            ImportSettings settings, ProgressMonitor monitor)
        throws ImportException {

        currentProject = p;
        Collection newElements = new HashSet();
        monitor.setMaximumProgress(files.size());
        int count = 1;
        for (Iterator it = files.iterator(); it.hasNext();) {
            File file = (File) it.next();
            String fileName = file.getName();
            try {
                newElements.addAll(
                        parseFile(new FileInputStream(file), fileName));
            } catch (FileNotFoundException e) {
                throw new ImportException("File not found: " + fileName, e);
            }
            monitor.updateProgress(count++);
        }
        return newElements;
    }

    /**
     * This method parses a single IDL source file.
     *
     * @param is The InputStream for the file to parse.
     * @param fileName The name of the parsed file.
     * @throws ImportException 
     */
    public Collection parseFile(InputStream is, String fileName)
        throws ImportException {

	int lastSlash = fileName.lastIndexOf('/');
	if (lastSlash != -1) {
	    fileName = fileName.substring(lastSlash + 1);
	}

	IDLParser parser =
	    new IDLParser(new IDLLexer(new BufferedInputStream(is)));

	// Create a modeller for the parser
	Modeler modeller = new Modeler(currentProject.getModel(), fileName);
	// start parsing at the specification rule
	try {
	    parser.specification(modeller);
	} catch (RecognitionException e) {
            throw new ImportException("File: " + fileName , e);
	} catch (TokenStreamException e) {
            throw new ImportException("File: " + fileName , e);
	}
        return modeller.getNewElements();
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#enable()
     */
    public boolean enable() {
        ImporterManager.getInstance().addImporter(this);
        return true;
    }
    
    /*
     * @see org.argouml.moduleloader.ModuleInterface#disable()
     */
    public boolean disable() {
        return true;
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getName()
     */
    public String getName() {
        return "IDL";
    }
    
    /*
     * @see org.argouml.moduleloader.ModuleInterface#getInfo(int)
     */
    public String getInfo(int type) {
        switch (type) {
        case DESCRIPTION:
            return "Java import from IDL files";
        case AUTHOR:
            return "Andreas Rueckert";
        case VERSION:
            return "0.2 - $Id: IDLFileImport.java 231 2010-01-12 18:36:00Z linus $";
        default:
            return null;
        }
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#getSuffixFilters()
     */
    public SuffixFilter[] getSuffixFilters() {
	SuffixFilter[] result = {
	    // TODO: I18N
            new SuffixFilter("idl", "Interface Definition Language file"),
	};
	return result;
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#isParseable(java.io.File)
     */
    public boolean isParseable(File file) {
        return FileImportUtils.matchesSuffix(file, getSuffixFilters());
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#getImportSettings()
     */
    public List<SettingsTypes.Setting> getImportSettings() {
        return null;
    }
}
