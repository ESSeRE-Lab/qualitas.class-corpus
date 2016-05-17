package org.argouml.language.csharp.importer;

import org.argouml.uml.reveng.ImportInterface;
import org.argouml.uml.reveng.ImportSettings;
import org.argouml.uml.reveng.FileImportUtils;
import org.argouml.uml.reveng.ImporterManager;
import org.argouml.language.csharp.importer.csparser.main.Lexer;
import org.argouml.language.csharp.importer.csparser.main.Parser;
import org.argouml.language.csharp.importer.csparser.main.FeatureNotSupportedException;
import org.argouml.language.csharp.importer.csparser.collections.TokenCollection;
import org.argouml.language.csharp.importer.csparser.structural.CompilationUnitNode;

import org.argouml.kernel.Project;
import org.argouml.taskmgmt.ProgressMonitor;
import org.argouml.i18n.Translator;
import org.argouml.util.SuffixFilter;

import org.apache.log4j.Logger;

import java.util.*;
import java.io.*;
// $Id: CSharpImport.java 280 2010-01-11 21:16:34Z linus $
// Copyright (c) 1996-2007 The Regents of the University of California. All
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * This is the main class for C# reverse engineering.
 *
 * @author Thilina Hasantha <thilina.hasantha@gmail.com>
 */
public class CSharpImport implements ImportInterface {

    /**
     * logger
     */
    private static final Logger LOG = Logger.getLogger(CSharpImport.class);

    /**
     * New model elements that were added
     */
    private Collection newElements;

    private List parsedElements = new ArrayList();

    /*
    * @see org.argouml.uml.reveng.ImportInterface#parseFiles(org.argouml.kernel.Project, java.util.Collection, org.argouml.uml.reveng.ImportSettings, org.argouml.application.api.ProgressMonitor)
    */
    public Collection parseFiles(Project p, Collection files,
                                 ImportSettings settings, ProgressMonitor monitor)
            throws ImportException {

        newElements = new HashSet();
        parsedElements = new ArrayList();
        monitor.updateMainTask(Translator.localize("dialog.import.pass1"));

        monitor.setMaximumProgress(files.size() * 3);
        doImportPass(p, files, settings, monitor, 0, 0);
        parseElements(p, settings, monitor, files.size(), 1);
        monitor.close();
        return newElements;
    }


    private void doImportPass(Project p, Collection files,
                              ImportSettings settings, ProgressMonitor monitor, int startCount,
                              int pass) throws ImportException {

        int count = startCount;
        for (Iterator it = files.iterator(); it.hasNext();) {
            if (monitor.isCanceled()) {
                monitor.updateSubTask(
                        Translator.localize("dialog.import.cancelled"));
                return;
            }
            Object file = it.next();
            if (file instanceof File) {
                System.out.println(((File)file).getAbsolutePath());
                parseFile(p, (File) file, settings, pass);
                monitor.updateProgress(count++);
                monitor.updateSubTask(Translator.localize(
                        "dialog.import.parsingAction",
                        new Object[]{((File) file).getAbsolutePath()}));
            } else {
                throw new ImportException("Object isn't a file " + file);
            }
        }


    }


    private void parseElements(Project p,
                               ImportSettings settings, ProgressMonitor monitor, int startCount,
                               int pass) throws ImportException {


        CSModeller cm=new CSModeller(p,settings);
        cm.model(parsedElements,monitor,startCount); 
        newElements.addAll(cm.getNewElements());



    }


    /**
     * Do a single import pass of a single file.
     *
     * @param p        the project
     * @param f        the source file
     * @param settings the user provided import settings
     * @param pass     current import pass - 0 = single pass, 1 = pass 1 of 2, 2 =
     *                 pass 2 of 2
     */
    private void parseFile(Project p, File f, ImportSettings settings, int pass)
            throws ImportException {


        try {
            BufferedInputStream bs = new BufferedInputStream(new FileInputStream(f));
            Lexer l = new Lexer(bs, f.getAbsolutePath());
            TokenCollection toks = l.lex();
            Parser px = new Parser();
            CompilationUnitNode cu = px.parse(toks, l.StringLiterals);
            parsedElements.add(cu);
        } catch (FeatureNotSupportedException e) {
            //throw new ImportException("Error parsing file: " + f.getAbsolutePath() + " due to: "+e.getMessage());
            System.out.println("Error parsing file: " + f.getAbsolutePath() + " due to: "+e.getMessage());
        } catch (Exception e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            throw new ImportException("Error parsing file: " + f.getAbsolutePath());
            System.out.println("Error parsing file: " + f.getAbsolutePath());
        }

    }


    public static final SuffixFilter CS_FILE_FILTER = new
        SuffixFilter("cs", "CSharp");
    /*
     * @see org.argouml.uml.reveng.ImportInterface#getSuffixFilters()
     */
    public SuffixFilter[] getSuffixFilters() {
        SuffixFilter[] result = {CS_FILE_FILTER};
        return result;
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#isParseable(java.io.File)
     */
    public boolean isParseable(File file) {
        return FileImportUtils.matchesSuffix(file, getSuffixFilters());
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getName()
     */
    public String getName() {
        return " CSharp";
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getInfo(int)
     */
    public String getInfo(int type) {
        switch (type) {
            case DESCRIPTION:
                return "This is a module for import from CSharp files.";
            case AUTHOR:
                return "Thilina Hasantha";
            case VERSION:
                return "1.0";
            default:
                return null;
        }
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#disable()
     */
    public boolean disable() {
        // We are permanently enabled
        return false;
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#enable()
     */
    public boolean enable() {
        init();
        return true;
    }

    /**
     * Enable the importer.
     */
    public void init() {
        ImporterManager.getInstance().addImporter(this);
    }

    /*
    * @see org.argouml.uml.reveng.ImportInterface#getImportSettings()
    */
    public List getImportSettings() {
        return null;
    }

}
