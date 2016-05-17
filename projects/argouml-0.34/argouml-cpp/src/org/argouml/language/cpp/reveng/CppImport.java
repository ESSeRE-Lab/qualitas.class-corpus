/* $Id: CppImport.java 373 2010-01-12 18:05:38Z linus $
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    euluis
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 2005-2009 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
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

package org.argouml.language.cpp.reveng;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.io.File;
import java.io.Reader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.anarres.cpp.CppReader;
import org.anarres.cpp.Preprocessor;
import org.apache.log4j.Logger;
import org.argouml.configuration.Configuration;
import org.argouml.configuration.ConfigurationKey;
import org.argouml.kernel.Project;
import org.argouml.profile.ProfileException;
import org.argouml.taskmgmt.ProgressMonitor;
import org.argouml.uml.reveng.FileImportUtils;
import org.argouml.uml.reveng.ImportInterface;
import org.argouml.uml.reveng.ImportSettings;
import org.argouml.uml.reveng.ImporterManager;
import org.argouml.util.SuffixFilter;

/**
 * Implementation of the reverse engineering interface of ArgoUML,
 * <code>ImportInterface</code>, for the C++ module.
 *
 * FIXME i18n support?!
 *
 * TODO: when the module is ready for prime time, remove the warning.
 *
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.19.2
 */
public class CppImport implements ImportInterface {

    /** logger */
    private static final Logger LOG = Logger.getLogger(CppImport.class);

    /**
     * Flag for warning the user about the limitations of the C++ module. The
     * default value is true, which means that if there isn't the respective
     * property, the user will be warned.
     */
    private boolean userWarning = Configuration.getBoolean(KEY_USER_WARNING,
        true);

    /**
     * Configuration key for the user warning.
     */
    private static final ConfigurationKey KEY_USER_WARNING = Configuration
            .makeKey("cpp", "reveng", "user", "warning");


    /**
     * New top level model elements created during this reverse engineering
     * session.
     */
    private Collection newElements;
    
    /**
     * Default constructor.
     */
    public CppImport() {
        super();
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#parseFiles(org.argouml.kernel.Project, java.util.Collection, org.argouml.uml.reveng.ImportSettings, org.argouml.application.api.ProgressMonitor)
     */
    public Collection parseFiles(Project p, Collection files,
            ImportSettings settings, ProgressMonitor monitor)
        throws ImportException {

        LOG.warn("Not fully implemented yet!");
        warnUser(monitor);

        newElements = new HashSet();
        monitor.setMaximumProgress(files.size());
        int count = 1;
        for (Iterator it = files.iterator(); it.hasNext();) {
            Object file = it.next();
            if (!(file instanceof File)) {
                throw new ImportException("Invalid argument - not a file: "
                        + file);
            }
            parseFile(p, (File) file, settings);
            monitor.updateProgress(count++);
        }
        return newElements;
    }

    /*
     * Parse a single file
     */
    private void parseFile(Project p, File f, ImportSettings settings)
        throws ImportException {

        Reader fileReader;
        Preprocessor preprocessor;
        try {
            preprocessor = new Preprocessor(f); // Create a new preprocessor 
                                                // for the input file.
            fileReader = new CppReader(preprocessor);
        } catch (IOException e) {
            throw new ImportException("Error opening file " + f, e);
        }
        try {
            Modeler modeler = createModeler(p);
            CPPLexer lexer = new CPPLexer(fileReader);
            CPPParser parser = new CPPParser(lexer);
            try {
                parser.translation_unit(modeler);
            } catch (Exception e) {
                throw new ImportException("Error parsing " + f, e);
            }
            newElements.addAll(modeler.getNewElements());
        } finally {
            try {
		fileReader.close();
            } catch (IOException e) {
                LOG.error("Error on closing file " + f, e);
            }
        }
    }
    
    private static class ModelerInvocationHandler implements InvocationHandler {

        static final Logger LOG = Logger.getLogger(Modeler.class);
        
        private Modeler modeler;
        
        ModelerInvocationHandler(Modeler theModeler) {
            this.modeler = theModeler;
        }
        
        public Object invoke(Object proxy, Method method, 
            Object[] args) {
            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("Entered ");
            debugInfo.append(method.getName());
            if (args != null && args.length > 0) {
                debugInfo.append(", with args: ");
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    debugInfo.append(arg);
                    if (i < args.length - 1) {
                        debugInfo.append("; ");
                    }
                }
            }
            debugInfo.append(".");
            LOG.debug(debugInfo);
            try {
                return method.invoke(modeler, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private Modeler createModeler(Project p) throws ImportException {
        try {
            Modeler modeler = new ModelerImpl(p);
            if (LOG.isDebugEnabled()) {
                InvocationHandler handler = new ModelerInvocationHandler(
                    modeler);
                modeler = (Modeler) Proxy.newProxyInstance(
                    Modeler.class.getClassLoader(),
                    new Class[] {Modeler.class}, handler);
            }
            return modeler;
        } catch (ProfileException e) {
            throw new ImportException(
                "Exception thrown while creating the C++ modeler.", e);
        }
    }

    /**
     * <p>
     * Show a dialog box to the user, warning that the C++ reveng is still very
     * limited. The list of obvious limitations must be shown. The user is given
     * the option of, by default, not seeing the warning again.
     * </p>
     * <p>TODO: i18n, or not to-do?... This warning is temporary, and it will
     * change often in the future - hopefully removing limitations - so, would
     * the effort of i18n pay off? I don't think so.
     * </p>
     * @param monitor The ProgressMonitor enables us to show the feedback to
     * the user without depending on the GUI.
     */
    private void warnUser(ProgressMonitor monitor) {
        final String lineSepAndListIndent = System
                .getProperty("line.separator")
            + "    * ";
        String warnMsg = "Its known limits are: "
            + lineSepAndListIndent
            + "very few C++ constructs are supported, e.g., enums, unions, "
            + "templates, etc, aren't;"
            + lineSepAndListIndent
            + "no support for non-member variables and functions;"
            + lineSepAndListIndent
            + "no integration with the C++ generator => RTE won't work!;"
            + lineSepAndListIndent + "no operator overload support;"
            + lineSepAndListIndent
            + "very immature, certainly this list needs to grow!";
        Configuration.setBoolean(KEY_USER_WARNING, userWarning);
        LOG.debug("userWarning = " + userWarning);
        // Even if the user didn't turn off the warning, we won't show it to
        // him again in this ArgoUML run.
        userWarning = false;
        monitor.notifyMessage("C++ Import Limitations",
                "The C++ reverse engineering module is pre-alpha stage.",
                warnMsg);
    }

    /*
     * The suffix filters for C++ files. Header sufixes are left out, since the
     * module should deal with files that originate translation units.
     */
    private static final SuffixFilter[] CPP_SUFFIX_FILTERS = {
        new SuffixFilter("cxx", "C++ source files"),
        new SuffixFilter("c++", "C++ source files"),
        new SuffixFilter("C++", "C++ source files"),
        new SuffixFilter("CPP", "C++ source files"),
        new SuffixFilter("cpp", "C++ source files"), };

    /*
     * @see org.argouml.uml.reveng.ImportInterface#getSuffixFilters()
     */
    public SuffixFilter[] getSuffixFilters() {
        return CPP_SUFFIX_FILTERS;
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#isParseable(java.io.File)
     */
    public boolean isParseable(File file) {
        return FileImportUtils.matchesSuffix(file, getSuffixFilters());
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
        // Nothing to do here either
        return true;
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getName()
     */
    public String getName() {
        return "C++";
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getInfo(int)
     */
    public String getInfo(int type) {
        switch (type) {
        case AUTHOR:
            return "Luis Sergio Oliveira (euluis)";
        case DESCRIPTION:
            return "C++ reverse engineering support";
        case VERSION:
            return "0.01";
        default:
            return null;
        }
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#getImportSettings()
     */
    public List getImportSettings() {
        return null;
    }
}
