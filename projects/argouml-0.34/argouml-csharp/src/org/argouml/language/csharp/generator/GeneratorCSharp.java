/* $Id: GeneratorCSharp.java 280 2010-01-11 21:16:34Z linus $
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mvw
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 1996-2009 The Regents of the University of California. All
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

package org.argouml.language.csharp.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.argouml.model.Model;
import org.argouml.moduleloader.ModuleInterface;
import org.argouml.uml.DocumentationManager;
import org.argouml.uml.UUIDHelper;
import org.argouml.uml.generator.CodeGenerator;
import org.argouml.uml.generator.GeneratorHelper;
import org.argouml.uml.generator.GeneratorManager;
import org.argouml.uml.generator.Language;
import org.argouml.uml.generator.SourceUnit;
import org.argouml.uml.generator.TempFileUtils;

import tudresden.ocl.parser.node.AConstraintBody;

/**
 * C# (Csharp) code generator
 */
public class GeneratorCSharp implements CodeGenerator, ModuleInterface {
    private static final boolean VERBOSE = false;
    /**
     * System dependent line separator
     */
    private static final String LINE_SEPARATOR =
	System.getProperty("line.separator"); //$NON-NLS-1$
    /**
     * System dependent file separator
     */
    private static final String FILE_SEPARATOR =
    	System.getProperty("file.separator"); //$NON-NLS-1$
    /**
     * Human readable language name
     */
    private static final String LANGUAGE_NAME = "CSharp";
    /**
     * Defines indentation width
     */
    private static final String INDENT = "    ";
    
    private static Section sect;

    /**
     * The module logger
     */
    private static final Logger LOG = Logger.getLogger(GeneratorCSharp.class);

    /**
     * The singleton.
     */
    private static final GeneratorCSharp INSTANCE = new GeneratorCSharp();
    
    /**
     * The language name.
     */
    private Language myLang;

    /**
     * Get this object.
     *
     * @return The one and only instance.
     */
    public static GeneratorCSharp getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor.
     */
    public GeneratorCSharp() {
        myLang = GeneratorHelper.makeLanguage(LANGUAGE_NAME,
                ResourceLoaderWrapper.lookupIconResource(
                        LANGUAGE_NAME + "Notation"));
    }

    /**
     * Generates a file for the classifier.
     * Returns the full path name of the the generated file.
     */
    private static String generateFile(Object cls, String path) {
	sect = new Section();

	String name = Model.getFacade().getName(cls);
	if (name == null || name.length() == 0)  {
	    return null;
	}
	String filename = name + ".cs";
	if (!path.endsWith(FILE_SEPARATOR)) {
	    path += FILE_SEPARATOR;
	}

        String packagePath = "";
        Object parent = 
            Model.getFacade().getNamespace(Model.getFacade().getNamespace(cls));
        if (parent != null) {
            packagePath = 
                Model.getFacade().getName(Model.getFacade().getNamespace(cls));
        }
	while (parent != null) {
	    // omit root package name; it's the model's root
	    if (Model.getFacade().getNamespace(parent) != null) {
	        packagePath = 
	            Model.getFacade().getName(parent) + "." + packagePath;
	    }
	    parent = Model.getFacade().getNamespace(parent);
	}

	int lastIndex = -1;
	do {
	    File f = new File(path);
	    if (!f.isDirectory()) {
		if (!f.mkdir()) {
		    LOG.debug(" could not make directory " + path);
		    return null;
		}
	    }
	    if (lastIndex == packagePath.length()) {
	        break;
	    }
	    int index = packagePath.indexOf(".", lastIndex + 1);
	    if (index == -1) {
	        index = packagePath.length();
	    }
	    path +=
	        packagePath.substring(lastIndex + 1, index) + FILE_SEPARATOR;
	    lastIndex = index;
	} while (true);
	String pathname = path + filename;
	LOG.debug("-----" + pathname + "-----");

	//String pathname = path + filename;
	// TODO: package, project basepath, tagged values to configure
	File f = new File(pathname);
	if (f.exists()) {
	    LOG.debug("Generating (updated) " + f.getPath());
	    sect.read(pathname);
	} else {
	    LOG.debug("Generating (new) " + f.getPath());
	}
	String header = INSTANCE.generateHeader(cls, pathname, packagePath);
	String src = INSTANCE.generateClassifier(cls);
	if (packagePath.length() > 0) {
	    src += "\n}";
	}
	BufferedWriter fos = null;
	try {
            // TODO: This is using the default platform character encoding
            // specifying an encoding will produce more predictable results
	    fos = new BufferedWriter(new FileWriter(f));
	    fos.write(header);
	    fos.write(src);
	} catch (IOException exp) {
	    // IO Problem.
	} finally {
	    try {
		if (fos != null) {
		    fos.close();
		}
	    } catch (IOException exp) {
		LOG.debug("FAILED: " + f.getPath());
	    }
	}

	sect.write(pathname, INDENT);
	LOG.debug("written: " + pathname);


	File f1 = new File(pathname + ".bak");
	if (f1.exists()) {
	    f1.delete();
	}

	File f2 = new File(pathname);
	if (f2.exists()) {
	    f2.renameTo(new File(pathname + ".bak"));
	}

	File f3 = new File(pathname + ".out");
	if (f3.exists()) {
	    f3.renameTo(new File(pathname));
	}

	LOG.debug("----- end updating -----");
	return pathname;
    }


    /**
     * Generate the module header.
     * 
     * @param cls classifer to generate module for
     * @param pathname path for this source module
     * @param packagePath path for containing package
     * @return String containing header
     */
    private String generateHeader(
            Object cls,
            String pathname,
            String packagePath) {
	String s = "";
	// TODO: add user-defined copyright / module header
	s += "// FILE: " + pathname.replace('\\', '/') + "\n\n";

        s += generateImports(cls, packagePath);

	if (packagePath.length() > 0) {
	    s += "namespace " + packagePath + " {\n";
	}

	s += "\n";

	// check if the class has a base class
	// String baseClass = generateGeneralzation(cls.getGeneralizations());

	// check if the class has dependencies
	//   {
	//       Collection col = cls.getAssociationEnds();
	//           Iterator itr = col.iterator();
	//           while (itr.hasNext()) {
	//               MAssociationEnd ae = (MAssociationEnd) itr.next();
	//               MAssociation a = ae.getAssociation();
	//               ae = ae.getOppositeEnd();
	//               if (ae.isNavigable()) {
	//                   MClassifier cls2 = ae.getType();
	//                   String name = cls2.getName();
	//                   String name2 = cls.getName();
	//                   if (name != name2){
	//                     s += "require_once \"" + name + ".php\";\n";
	//                   }
	//               }
	//           }
	//   }

	//   {
	//       Collection col = cls.getClientDependencies();
	//       LOG.debug("col: " + col);
	//           Iterator itr = col.iterator();
	//           while (itr.hasNext()) {
	//               MDependency dep = (MDependency) itr.next();
	//               Collection clients_col = dep.getSuppliers();
	//               Iterator itr2 = clients_col.iterator();
	//               while (itr2.hasNext()){
	//                   String name =
	//                       ((MClassifier) itr2.next()).getName();
	//                   s += "require_once \"" + name + ".php\";\n";
	//               }
	//           }
	//   }

	return s;
    }


    private String generateSubmachine(Object m) {
        Object c = Model.getFacade().getSubmachine(m);
        if (c == null) {
            return "include / ";
        }
        if (Model.getFacade().getName(c) == null) {
            return "include / ";
        }
        if (Model.getFacade().getName(c).length() == 0) {
            return "include / ";
        }
        return ("include / " + Model.getFacade().getName(c));
    }

    private String generateObjectFlowState(Object m) {
        Object c = Model.getFacade().getType(m);
        if (c == null) {
            return "";
        }
        return Model.getFacade().getName(c);
    }


    /**
     * Generates the operation of a class
     * @param op The operation object
     * @param documented True to generate constraint enriched comment
     * @return Returns a string with the operation generated
     */
    private String generateOperation(Object op, boolean documented) {

	String s = "";
	boolean isDestructor = false;

	Object cls = Model.getFacade().getOwner(op);

	String nameStr = Model.getFacade().getName(op);
	String clsName = Model.getFacade().getName(
            Model.getFacade().getOwner(op));

	String tagStr = "";
	String tag = Model.getFacade().getTaggedValueValue(op, "override");
        if ("true".equals(tag)) {
            tagStr = " override ";
        }
	// Check if this is a destructor
        if (Model.getExtensionMechanismsHelper().hasStereotype(op, "destroy")) {
            nameStr = "~" + nameStr;
            isDestructor = true;            
        }

        // pick out return type
        List rps = Model.getCoreHelper().getReturnParameters(op);
        if (rps.size() > 1) {
            // TODO: Add better support or error reporting for multiple return
            // parameters
            throw new RuntimeException(
                    "Multiple return parameters not supported");
        }
        Object rp = null;
        if (rps.size() == 1) {
            rp = rps.get(0);
        }
	// name and params
	List params = new ArrayList(Model.getFacade().getParameters(op));
	
	String returnDoc = generateConstraintEnrichedDocComment(rp,"/// <returns>","/// ","/// </returns>") + "\n" + INDENT;
	params.remove (rp);
	
	if (documented) {
	    s += generateConstraintEnrichedDocComment(op,"/// <summary>","/// ","/// </summary>") + "\n" + INDENT;
		for (Object p : params) {
		    s +=  generateConstraintEnrichedDocComment(p,"/// <param name=\"" + Model.getFacade().getName(p) + "\">" ,"/// ","/// </param>") + "\n" + INDENT;
		}
		s += returnDoc;
	}

	//    s += "function ";

	if (!(Model.getFacade().isAInterface(cls)) && !isDestructor) {
	    s += generateAbstractness (op);
	    s += generateScope (op);
	    s += generateChangeability (op);
	    s += generateVisibility (op);
	}
	
	s += tagStr;
	if (rp != null) {
	    Object returnType = Model.getFacade().getType(rp);
	    if (returnType == null && !nameStr.equals(clsName)) {
		s += " void ";
	    } else if (returnType != null) {
		s += " " + generateClassifierRef (returnType) + " ";
	    }
	}


	s += nameStr + "(";

	boolean first = true;
	for (Object p : params) {
	    if (!first) {
	        s += ", ";
	    }
	    s += generateParameter(p);
	    first = false;
	}

	s += ")";

	return s;
    }

/**
 * Generates attributes
 * @param attr Attribute to generate code for
 * @param documented True to generate constraint enriched comment
 * @return The code for this attribute
 */
    private String generateAttribute(Object attr, boolean documented) {
	String s = "";

	String makeGet = Model.getFacade().getTaggedValueValue(attr, "get");
	String makeSet = Model.getFacade().getTaggedValueValue(attr, "set");

	boolean genAccessor = false;

	genAccessor =
	    (((makeGet != null) && (makeGet.equals("true")))
	        || ((makeSet != null) && (makeSet.equals("true"))));

	if (documented) {
	    s += generateConstraintEnrichedDocComment (attr) + "\n" + INDENT;
	}
	if (genAccessor) {
	    s += " private ";
	} else {
	    s += generateVisibility(attr);
	}

	s += generateScope(attr);
	s += generateChangability(attr);
	if (isCollection(attr)) {
	    String temp =
	        generateMultiplicity(Model.getFacade().getMultiplicity(attr));
	    if (temp.length() > 0) {
		s += temp + " ";
	    }
	}

	Object type = Model.getFacade().getType(attr);
	if (type != null) {
	    s += generateClassifierRef(type) + " ";
	}

	String slash = "";
	//    if (attr.containsStereotype(MStereotype.DERIVED)) slash = "/";
	String attrName = Model.getFacade().getName(attr);
	Object vis = Model.getFacade().getVisibility(attr);

	// if (Model.getFacade().PUBLIC_VISIBILITYKIND.equals(vis)) {
	    // use original attribute name, no change
	// }
	if (Model.getVisibilityKind().getPrivate().equals(vis)) {
	    attrName =
	        Model.getFacade().getName(Model.getFacade().getOwner(attr))
	        + "_" + attrName;
	}
	// if (Model.getFacade().PROTECTED_VISIBILITYKIND.equals(vis)) {
	    // use orignial name for the moment
	// }

	if (genAccessor) {
	    attrName = "m_" + attrName;
	}

	s += slash + attrName;
	Object init = Model.getFacade().getInitialValue(attr);
	if (init != null) {
	    String initStr = generateExpression(init).trim();
	    if (initStr.length() > 0) {
	        s += " = " + initStr;
	    }
	}

	s += ";\n";
	// s += generateConstraints(attr);  Removed 2001-09-26 STEFFEN ZSCHALER

	// look if get and set methods are needed (Marian Heddesheimer)

	//  if get and set are set, visibility is set to same as orig attribute
	//  and a variable with m_ prefix is created.


	if (genAccessor) {

	    s += "\n";

	    s += INDENT + generateVisibility(attr) + " ";
	    s += generateClassifierRef(type) + " " 
	        + Model.getFacade().getName(attr);
	    s += " {\n";

	    if ((makeSet != null) && (makeSet.equals("true"))) {
	        s += INDENT + INDENT 
	            + "set { m_" + Model.getFacade().getName(attr);
	        s += " = value; } \n";
	    }

	    if ((makeGet != null) && (makeGet.equals("true"))) {
	        s += INDENT + INDENT;
	        s += "get { return m_" 
	            + Model.getFacade().getName(attr) + "; } \n";
	    }

	    s += INDENT + "}\n";
	}

	return s;
    }

    /**
     * @param element ModelElement which has the Multiplicity
     * @return true if multiplicity is non-null and upper bound is greater
     * than 1
     */
    private boolean isCollection(Object element) {
    	Object multiplicity = Model.getFacade().getMultiplicity(element);
    	// TODO: How should we handle a null multiplicity for code generation?
        if (multiplicity != null) {
            int upper = Model.getFacade().getUpper(multiplicity);
            // -1 is UML's special 'unlimited integer'
            if (upper > 1 || upper == -1) {
                return true;
            }
        }
        return false;
    }

/**
 * Generates a parameter
 * @param param The parametere to generate code for
 * @return The generated code
 */
    private String generateParameter(Object param) {
        String s = "";
        String temp = "";
	// TODO: qualifiers (e.g., const)
	// TODO: stereotypes...
	s +=  generateClassifierRef(Model.getFacade().getType(param)) + " ";
	org.argouml.model.Facade fac = Model.getFacade();
	Object kind = fac.getKind(param);
	org.argouml.model.DirectionKind dirkind = Model.getDirectionKind();
	Object inoutParam = dirkind.getInOutParameter();
	
	//if ((Model.getFacade().getKind(param).equals(
	//        Model.getDirectionKind().getInOutParameter())))
	if (kind != null) {
	    if (kind.equals(inoutParam)) {
	        // if  INOUT, then pass by Reference
	        temp = "ref " + s;
	        s = temp;
	    }
	
	    if (Model.getFacade().getKind(param).equals(
                    Model.getDirectionKind().getOutParameter())) {
                // if OUT
                temp = "out " + s;
                s = temp;
            }
        }
	s += Model.getFacade().getName(param);
    

	// TODO: initial value

	//    MExpression default_val = param.getDefaultValue();
	//    if ( (default_val != null) && (default_val.getBody() != null) ) {
	//        s += " = " + default_val.getBody();
	//    }

	return s;
    }

/**
 * Generates a classifier
 * @param cls The classifier to generate code for
 * @return The generated code
 */
    private String generateClassifier(Object cls) {
	String generatedName = Model.getFacade().getName(cls);
	String classifierKeyword;
	LOG.debug("generateClassifier: " + generatedName);
	if (Model.getFacade().isAClass(cls)) {
	    classifierKeyword = "class";
	} else if (Model.getFacade().isAInterface(cls)) {
	    classifierKeyword = "interface";
	} else {
	    return ""; // actors and use cases
	}

	StringBuffer sb = new StringBuffer();

	// Add the comments for this classifier first.
	sb.append(DocumentationManager.getComments(cls,"/// <summary>","/// "," /// </summary>"));
	sb.append(generateConstraintEnrichedDocComment (cls,"/// <summary>","/// "," /// </summary>")).append ("\n");
	
	sb.append(generateVisibility(Model.getFacade().getVisibility(cls)));
	if (Model.getFacade().isAbstract(cls)
                && !(Model.getFacade().isAInterface(cls))) {
            sb.append(" abstract ");
        }
        if (Model.getFacade().isLeaf(cls)) {
            sb.append(" final ");
        }
	sb.append(classifierKeyword).append(" ").append(generatedName);
	String baseClass =
	    generateGeneralization(Model.getFacade().getGeneralizations(cls));
	String tv = null;
	if (!baseClass.equals("")) {
	    sb.append(' ').append(": ").append(baseClass);
	}

	// nsuml: realizations!
	if (Model.getFacade().isAClass(cls)) {
	    String interfaces = generateSpecification(cls);
	    if (!interfaces.equals("")) {
		if (baseClass.equals("")) {
		    sb.append(": ");
		} else {
		    sb.append(", ");
		}
		sb.append(interfaces);
	    }
	}
	sb.append("\n{");

	tv = generateTaggedValues(cls);
	if (tv != null && tv.length() > 0) {
	    sb.append(INDENT).append(tv);
	}

	// Removed 2001-09-26 STEFFEN ZSCHALER:
	// sb.append(generateConstraints(cls));


	// generate constructor (Marian Heddesheimer)
	String makeConstructor =
	    Model.getFacade().getTaggedValueValue(cls, "constructor");
	if ((makeConstructor != null) && (makeConstructor.equals("true"))) {
	    sb.append(Model.getFacade().getName(cls)).append("() {\n");
	    sb.append(generateSection(cls));
	    sb.append(INDENT).append("}\n");
	}

	List strs = Model.getFacade().getAttributes(cls);
	if (strs.size() > 0) {
	    sb.append('\n');

	    if (Model.getFacade().isAClass(cls)) {
	        sb.append(INDENT).append("// Attributes\n");
	    }
	    for (Object sf : strs) {
		sb.append('\n').append(INDENT).append(
                        generateAttribute(sf, false));
		tv = generateTaggedValues(sf);
		if (tv != null && tv.length() > 0) {
		    sb.append(INDENT).append(tv).append('\n');
		}
	    }
	}

	Collection ends = Model.getFacade().getAssociationEnds(cls);
	if (ends.size() > 0) {
	    sb.append('\n');
	    if (Model.getFacade().isAClass(cls)) {
	        sb.append(INDENT).append("// Associations\n");
	    }
	    for (Object ae : ends) {
		Object a = Model.getFacade().getAssociation(ae);
		sb.append('\n');
		sb.append(INDENT).append(generateAssociationFrom(a, ae));
		tv = generateTaggedValues(a);
		if (tv != null && tv.length() > 0) {
		    sb.append(INDENT).append(tv);
		}

		// TODO: Why was this not in generateAssociationFrom ?
		// sb.append(generateConstraints(a));
	    }
	}

	// TODO: constructors

	List behs = Model.getFacade().getOperations(cls);

	// Generate operations for all interfaces the class realizes
	for (Object bf :  Model.getCoreHelper().getRealizedInterfaces(cls)) {
	        behs.addAll(Model.getFacade().getOperations(bf));
	}
	
	if (behs.size() > 0) {
	    StringBuffer sbtemp;
	    sb.append ('\n');
	    sb.append (INDENT).append ("// Operations\n");

	    for (Object bf : behs) {
		sbtemp = new StringBuffer();
		
		sbtemp.append('\n').append(INDENT);
		Object parent = Model.getFacade().getOwner(bf);
		//Generate public modifier for interface implementations
		if (Model.getFacade().isAInterface(parent)) {
                    sbtemp.append("public ");
                }
		sbtemp.append(generateOperation(bf, true));

		tv = generateTaggedValues(bf);

		if ((Model.getFacade().isAClass(cls))
		        && (Model.getFacade().isAOperation(bf))
		        && (!(Model.getFacade().isAbstract(bf)))) {
		    sbtemp.append('\n').append(INDENT).append("{\n");

		    if (tv.length() > 0) {
		        sbtemp.append (INDENT).append (tv);
		    }

		    sbtemp.append(generateMethodBody(bf));
		    sbtemp.append('\n');
		    sbtemp.append(INDENT).append ("}\n");
		} else {
		    sbtemp.append(";\n");
		    if (tv.length() > 0) {
		        sbtemp.append(INDENT).append(tv).append('\n');
		    }
		}
		sb.append(sbtemp);
	    }
	}
	sb.append("} /* end ").append(classifierKeyword).append(' ');
	sb.append(generatedName).append(" */\n");

	return sb.toString();
    }

    /**
     * Generate the body of a method associated with the given operation. This
     * assumes there's at most one method associated!
     *
     * If no method is associated with the operation, a default method body will
     * be generated.
     */
    private String generateMethodBody(Object op) {
	if (op != null) {
	    // Collection methods = op.getMethods();
	    // Iterator i = methods.iterator();
	    // MMethod m = null;

	    // System.out.print(", op!=null, size="+methods.size());
	    return generateSection(op);
	    // return INDENT + INDENT + "/* method body for "
	    //     + op.getName() + " */";
	    /*
	      while (i != null && i.hasNext()) {
	      //System.out.print(", i!= null");
	      m = (MMethod) i.next();

	      if (m != null) {
	      //LOG.debug(", BODY of "+m.getName());
	      //LOG.debug("|"+m.getBody().getBody()+"|");
	      if (m.getBody() != null)
	      return m.getBody().getBody();
	      else
	      return "";
	      }
	      }

	      // pick out return type
	      MParameter rp = MMUtil.SINGLETON.getReturnParameter (op);
	      if (rp != null) {
	      MClassifier returnType = rp.getType();
	      return generateDefaultReturnStatement (returnType);
	      }
	    */
	}

	return generateDefaultReturnStatement (null);
    }

    /**
     * Generate default return statement
     * @param cls The object to generetae the code for
     * @return The generated code
     */
    private String generateDefaultReturnStatement(Object cls) {
	if (cls == null) {
	    return "";
	}

	String clsName = Model.getFacade().getName(cls);
	if (clsName.equals("void")) {
	    return "";
	} else if (clsName.equals("char")) {
	    return INDENT + "return 'x';\n";
	} else if (clsName.equals("int")) {
	    return INDENT + "return 0;\n";
	} else if (clsName.equals("boolean")) {
	    return INDENT + "return false;\n";
	} else if (clsName.equals("byte")) {
	    return INDENT + "return 0;\n";
	} else if (clsName.equals("long")) {
	    return INDENT + "return 0;\n";
	} else if (clsName.equals("float")) {
	    return INDENT + "return 0.0;\n";
	} else if (clsName.equals("double")) {
	    return INDENT + "return 0.0;\n";
	} else {
	    return INDENT + "return null;\n";
	}
    }

    /**
     * Generate code for tagged values
     * @param e The tagged value to generate code for
     * @return The generated code
     */
    private String generateTaggedValues(Object e) {
	Iterator iter = Model.getFacade().getTaggedValues(e);
	if (!iter.hasNext()) {
	    return "";
	}
	boolean first = true;
	StringBuffer buf = new StringBuffer();
	String s = null;
	while (iter.hasNext()) {
	    s = generateTaggedValue(iter.next());
	    if (s != null && s.length() > 0) {
	        if(!s.contains("documentation"))
	        {
        		if (first) {
        		    /*
        		     * Corrected 2001-09-26 STEFFEN ZSCHALER
        		     *
        		     * Was:
        		     buf.append("// {");
        		     *
        		     * which caused problems with new lines characters
        		     * in tagged values (e.g. comments...).  The new
        		     * version still has some problems with tagged values
        		     * containing "*"+"/" as this closes the comment
        		     * prematurely, but comments should be taken out of
        		     * the tagged values list anyway...
        		     */
        		    buf.append("/* {");
        		    first = false;
        		} else {
        		    buf.append(", ");
        		}
		buf.append(s);
	        }
	    }
	}

	if (!first) {
	    buf.append ("}*/\n");
	}

	return buf.toString();
    }


    /**
     * Generate code for a tagged value
     * @param tv The tagged value
     * @return The generated code
     */
    private String generateTaggedValue(Object tv) {
	if (tv == null) {
	    return "";
	}
	String s = generateUninterpreted(Model.getFacade().getValueOfTag(tv));
	if (s == null || s.length() == 0 || s.equals("/** */")) {
	    return "";
	}
	return Model.getFacade().getTagOfTag(tv) + "=" + s;
    }

    /**
     * Enhance/Create the doccomment for the given model element, including
     * tags for any OCL constraints connected to the model element. The tags
     * generated are suitable for use with the ocl injector which is part of
     * the Dresden OCL Toolkit and are in detail:
     *
     * &nbsp;@invariant for each invariant specified
     * &nbsp;@precondition for each precondition specified
     * &nbsp;@postcondition for each postcondition specified
     * &nbsp;@key-type specifying the class of the keys of a mapped association
     * &nbsp; Currently mapped associations are not supported yet...
     * &nbsp;@element-type specifying the class referenced in an association
     *
     * @since 2001-09-26 ArgoUML 0.9.3
     * @author Steffen Zschaler
     *
     * @param me the model element for which the documentation comment is needed
     * @param ae the association end which is represented by the model element
     * @return the documentation comment for the specified model element, either
     * enhanced or completely generated
     */
    private String generateConstraintEnrichedDocComment(Object me,
						       Object ae) {
	String sDocComment = generateConstraintEnrichedDocComment(me);

	if (isCollection(ae)) {
	    // Multiplicity greater 1, that means we will generate some sort of
	    // collection, so we need to specify the element type tag

	    // Prepare doccomment
	    if (sDocComment != null) {
		// Just remove closing */
		sDocComment =
		    sDocComment.substring(0, sDocComment.indexOf("*/") + 1);
	    } else {
		if (VERBOSE) {
		    sDocComment =
		        INDENT + "/**\n" + INDENT + " * \n" + INDENT + " *";
		} else {
		    sDocComment = "";
		}
	    }

	    // Build doccomment
	    sDocComment += " @element-type ";
	    sDocComment += 
	        Model.getFacade().getName(Model.getFacade().getType(ae));

	    sDocComment += "\n" + INDENT + " */";

	    return sDocComment;
	} else {
	    if (sDocComment != null) {
	        return sDocComment;
	    } else {
	        return "";
	    }
	}
    }
    /**
     * Enhance/Create the doccomment for the given model element, including
     * tags for any OCL constraints connected to the model element. The tags
     * generated are suitable for use with the ocl injector which is part
     * of the Dresden OCL Toolkit and are in detail:
     *
     * &nbsp;@invariant for each invariant specified
     * &nbsp;@precondition for each precondition specified
     * &nbsp;@postcondition for each postcondition specified
     *
     * @since 2001-09-26 ArgoUML 0.9.3
     * @author Steffen Zschaler
     *
     * @param me the model element for which the documentation comment is needed
     * @return the documentation comment for the specified model element, either
     * enhanced or completely generated
     */
    private String generateConstraintEnrichedDocComment(Object me) {
	return generateConstraintEnrichedDocComment(me,"/// <summary> ", "/// ", "/// </summary>");
    }
    private String generateConstraintEnrichedDocComment(Object me,String header, String prefix, String footer) {
	// Retrieve any existing doccomment
	String sDocComment =
	    DocumentationManager.getDocs(me, "",header, prefix, footer);

	LOG.debug("sDocComment: " + sDocComment);
	if (sDocComment != null) {
	    // Fix Bug in documentation manager.defaultFor -->
	    // look for current INDENT and use it
	    for (int i = sDocComment.indexOf ('\n');
		 i >= 0 && i < sDocComment.length();
		 i = sDocComment.indexOf ('\n', i + 1)) {
		sDocComment =
		    sDocComment.substring(0, i + 1)
		    + INDENT + sDocComment.substring (i + 1);
	    }
	}

	// Extract constraints
	Collection cConstraints = Model.getFacade().getConstraints(me);

	if (cConstraints.size() == 0) {
	    if (sDocComment != null) {
	        return sDocComment;
	    } else {
	        return "";
	    }
	}

	// Prepare doccomment
	if (sDocComment != null) {
	    // Just remove closing */
	    sDocComment =
	        sDocComment.substring(0, sDocComment.indexOf(footer) + 1);
	} else {
	    if (VERBOSE) {
		sDocComment =
		    INDENT + footer + "\n" + INDENT + " /// \n" + INDENT + " ///";
	    } else {
		sDocComment = "";
	    }
	}

	// Add each constraint

	class TagExtractor
		extends tudresden.ocl.parser.analysis.DepthFirstAdapter {
	    private LinkedList<String> llsTags = new LinkedList<String>();
	    private String constraintName;
	    private int constraintID = 0;

	    /**
	     * Constructor.
	     *
	     * @param sConstraintName
	     */
	    public TagExtractor(String sConstraintName) {
		super();

		constraintName = sConstraintName;
	    }

	    public Iterator<String> getTags() {
		return llsTags.iterator();
	    }

	    public void caseAConstraintBody(AConstraintBody node) {
		// We don't care for anything below this node,
	        // so we do not use apply anymore.
		String sKind = null;
		if (node.getStereotype() != null) {
		    sKind = node.getStereotype().toString();
		}

		String sExpression = null;
		if (node.getExpression() != null) {
		    sExpression = node.getExpression().toString();
		}

		String sName;
		if (node.getName() != null) {
		    sName = node.getName().getText();
		} else {
		    sName = constraintName + "_" + (constraintID++);
		}

		if ((sKind == null)
		        || (sExpression == null)) {
		    return;
		}

		String sTag;
		if (sKind.equals ("inv ")) {
		    sTag = "@invariant ";
		} else if (sKind.equals ("post ")) {
		    sTag = "@post-condition ";
		} else if (sKind.equals ("pre ")) {
		    sTag = "@pre-condition ";
		} else {
		    return;
		}

		sTag += sName + ": " + sExpression;
		llsTags.addLast (sTag);
	    }
	}

	tudresden.ocl.check.types.ModelFacade mf =
	    new org.argouml.ocl.ArgoFacade (me);
	for (Object constraint : cConstraints) {

	    try {
		tudresden.ocl.OclTree otParsed =
		    tudresden.ocl.OclTree.createTree(
		            (String) Model.getFacade().getBody(
		                    Model.getFacade().getBody(constraint)),
						     mf);

		TagExtractor te =
		    new TagExtractor(Model.getFacade().getName(constraint));
		otParsed.apply (te);

		for (Iterator j = te.getTags(); j.hasNext();) {
		    sDocComment += " " + j.next() + "\n" + INDENT + " ///";
		}
	    } catch (IOException ioe) {
		// Nothing to be done, should not happen anyway ;-)
	    }
	}

	sDocComment += "\n" + footer;

	return sDocComment;
    }

    /**
     * Generate code for AssociationFrom
     * @param association The association
     * @param associationEnd The end of the association
     * @return The generated code
     */
    private String generateAssociationFrom(Object association,
            				  Object associationEnd) {
	// TODO: does not handle n-ary associations
	String s = "";

	/*
	 * Moved into while loop 2001-09-26 STEFFEN ZSCHALER
	 *
	 * Was:
	 *
	 s += DocumentationManager.getDocs(a) + "\n" + INDENT;
	*/

	for (Object associationEnd2 
	        : Model.getFacade().getConnections(association)) {
	    if (associationEnd2 != associationEnd) {
		/**
		 * Added generation of doccomment 2001-09-26 STEFFEN ZSCHALER
		 *
		 */
		s += generateConstraintEnrichedDocComment(
		        association,
		        associationEnd2);
		s += "\n";

		s += generateAssociationEnd(associationEnd2);
	    }
	}

	return s;
    }


    private String generateAssociation(Object a) {
	String s = "";
	//     String generatedName = generateName(a.getName());
	//     s += "MAssociation " + generatedName + " {\n";

	//     Iterator endEnum = a.getConnection().iterator();
	//     while (endEnum.hasNext()) {
	//       MAssociationEnd ae = (MAssociationEnd)endEnum.next();
	//       s += generateAssociationEnd(ae);
	//       s += ";\n";
	//     }
	//     s += "}\n";
	return s;
    }


    /*
     * @see org.argouml.application.api.NotationProvider2#generateAssociationEnd(java.lang.Object)
     */
    private String generateAssociationEnd(Object associationEnd) {
        if (!Model.getFacade().isNavigable(associationEnd)) {
            return "";
        }
	
	String s = INDENT;
	String tempS = "";

        s = INDENT;
        s += generateVisibility(
                Model.getFacade().getVisibility(associationEnd));

        if (Model.getExtensionMechanismsHelper().hasStereotype(associationEnd, 
                "event")) {
            s += "event ";            
        }
	
        // must be public or generate public navigation method!

	if (Model.getFacade().isStatic(associationEnd)) {
	    if (VERBOSE) {
	        tempS += "static ";
	    }
	}
	//     String n = ae.getName();
	//     if (n != null && !String.UNSPEC.equals(n)) {
	//         s += generateName(n) + " ";
	//     }
	//     if (ae.isNavigable()) s += "navigable ";
	//     if (ae.getIsOrdered()) s += "ordered ";
	// Object m = Model.getFacade().getMultiplicity(associationEnd);
	// if (Model.getFacade().M1_1_MULTIPLICITY.equals(m)
	//         || Model.getFacade().M0_1_MULTIPLICITY.equals(m)) {
	// }
	if (VERBOSE) {
	    tempS += "/*" + generateClassifierRef(Model.getFacade()
                .getType(associationEnd)) + "*/";
	} else {
	    if (VERBOSE) {
	        tempS += "/* Vector */ "; //generateMultiplicity(m) + " ";
	    }
	}

	if (tempS.length() > 0) {
	    s += tempS + " ";
	}

	String name = Model.getFacade().getName(associationEnd);
	Object association = Model.getFacade().getAssociation(associationEnd);
        if (isCollection(associationEnd)) {
            s += generateClassifierRef(Model.getFacade()
                    .getType(associationEnd))
                + " ";
        } else {
            s += "ArrayList ";
        }
	String associationName = Model.getFacade().getName(association);
	if (name != null
	        && name != null && name.length() > 0) {
	    s += " " + name;
	} else if (associationName != null
	        && associationName != null && associationName.length() > 0) {
	    s += " " + associationName;
	} else {
	    s += " my";
	    s += generateClassifierRef(Model.getFacade()
                .getType(associationEnd));
	}

	return s + ";\n";
    }

    //   private String generateConstraints(MModelElement me) {
    //     List constr = me.getConstraint();
    //     if (constr == null || constr.size() == 0) return "";
    //     String s = "{";
    //     Iterator conEnum = constr.iterator();
    //     while (conEnum.hasNext()) {
    //       s += generateConstraint((MConstraint)conEnum.next());
    //       if (conEnum.hasNext()) s += "; ";
    //     }
    //     s += "}";
    //     return s;
    //   }


    //   private String generateConstraint(MConstraint c) {
    //     return generateExpression(c);
    //   }

    ////////////////////////////////////////////////////////////////
    // internal methods?

/**
 * @param generalizations The generalization we are generating
 * @return The generated code
 */
    private String generateGeneralization(Collection generalizations) {
	if (generalizations == null || generalizations.size() == 0) {
	    return "";
	}
	Collection classes = new ArrayList();
	for (Object generalization : generalizations) {
	    Object generalizableElement = 
	        Model.getFacade().getGeneral(generalization);
	    // assert ge != null
	    if (generalizableElement != null) {
	        classes.add(generalizableElement);
	    }
	}
	return generateClassList(classes);
    }

    /**
     * @param cls The classifier that we generate the specification for.
     * @return The specification, as a String.
     */
    private String generateSpecification(Object cls) {
	String s = "";
	//s += cls.getName();

	Collection realizations =
	    Model.getCoreHelper().getRealizedInterfaces(cls);
	if (realizations.size() == 0) {
	    return "";
	}
	Iterator clsEnum = realizations.iterator();
	while (clsEnum.hasNext()) {
	    Object i = clsEnum.next();
	    s += generateClassifierRef(i);
	    if (clsEnum.hasNext()) {
	        s += ", ";
	    }
	}

	return s;
    }
/**
 * Generates , separated list of classes
 * @param classifiers A collection of classifiers
 * @return The generated list as a String
 */
    private String generateClassList(Collection classifiers) {
	String s = "";
	if (classifiers == null) {
	    return "";
	}
	Iterator clsEnum = classifiers.iterator();
	while (clsEnum.hasNext()) {
	    s += generateClassifierRef(clsEnum.next());
	    if (clsEnum.hasNext()) {
	        s += ", ";
	    }
	}
	return s;
    }

    /**
     * Generates visibility: public, private, protected
     * 
     * This can be called with either a feature or a visibility.
     * 
     * @param handle The object to generate visibility for
     * 
     * @return A string with the visibility
     */
    private String generateVisibility(Object handle) {
        Object visibility;
	if (Model.getFacade().isAFeature(handle)) {
	    visibility = Model.getFacade().getVisibility(handle);
	} else {
	    visibility = handle;
	}

	//if (vis == null) return "";
	if (Model.getVisibilityKind().getPublic().equals(visibility)) {
	    return "public ";
	} else if (Model.getVisibilityKind().getPrivate().equals(visibility)) {
	    return  "private ";
	} else if (Model.getVisibilityKind().getProtected()
            .equals(visibility)) {
	    return  "protected ";
	} else {
	    return "";
	}
    }
    /**
     * Generate scope.
     *
     * @param feature The candidate.
     * @return Return the scope.
     */
    private String generateScope(Object feature) {
	if (Model.getFacade().isStatic(feature)) {
	    if (VERBOSE) {
	        return "/* static */ ";
	    } else {
	        return "static ";
	    }
	}
	return "";
    }

    /**
     * Generate "abstract" keyword for abstract operations and the virtual
     * keyword for root operations.
     * 
     * @param op
     *            The candidate.
     * @return Return the abstractness.
     */
    private String generateAbstractness(Object op) {
	if (Model.getFacade().isAbstract(op)) {
	    return "abstract ";
	}
	if (Model.getFacade().isRoot(op)) {
	    return "virtual ";
	}
	
	return "";
    }

    /**
     * Generate "final" keyword for final operations.
     *
     * @param op The candidate.
     * @return The generated changeability.
     */
    private String generateChangeability(Object op) {
	if (Model.getFacade().isLeaf(op)) {
	    return " sealed ";
	} else {
	    return "";
	}
    }
/**
 * Generate changeability
 * @param sf The object to generate for
 * @return The generated code
 */
    private String generateChangability(Object sf) {
	Object ck = Model.getFacade().getChangeability(sf);
	//if (ck == null) return "";
	if (Model.getChangeableKind().getFrozen().equals(ck)) {
	    return " sealed ";
	}
	//if (MChangeableKind.ADDONLY.equals(ck)) return "final ";
	return "";
    }

    /*
     * @see org.argouml.application.api.NotationProvider2#generateMultiplicity(java.lang.Object)
     */
    private String generateMultiplicity(Object multiplicity) {
	if (multiplicity == null) {
	    return "";
	}
        return Model.getFacade().toString(multiplicity);
    }


    private String generateState(Object m) {
	return Model.getFacade().getName(m);
    }


    private String generateStateBody(Object state) {
	LOG.debug("GeneratorCSharp: generating state body");
	String s = "";
	Object entry = Model.getFacade().getEntry(state);
	Object exit = Model.getFacade().getExit(state);
	if (entry != null) {
	    String entryStr = generateAction(entry);
	    if (entryStr.length() > 0) {
	        s += "entry / " + entryStr;
	    }
	}
	if (exit != null) {
	    String exitStr = generateAction(exit);
	    if (s.length() > 0) {
	        s += "\n";
	    }
	    if (exitStr.length() > 0) {
	        s += "exit / " + exitStr;
	    }
	}
	Collection trans = Model.getFacade().getInternalTransitions(state);
	for (Object tran : trans) {
	    if (s.length() > 0) {
	        s += "\n";
	    }
	    s += generateTransition(tran);
	}

	/*   if (trans != null) {
	     int size = trans.size();
	     MTransition[] transarray = (MTransition[])trans.toArray();
	     for (int i = 0; i < size; i++) {
	     if (s.length() > 0) s += "\n";
	     s += Generate(transarray[i]);
	     }
	     }*/
	return s;
    }


    private String generateTransition(Object state) {
	String s = Model.getFacade().getName(state);
	String t = generateEvent(Model.getFacade().getTrigger(state));
	String g = generateGuard(Model.getFacade().getGuard(state));
	String e = generateAction(Model.getFacade().getEffect(state));
	if (s.length() > 0) {
	    s += ": ";
	}
	s += t;
	if (g.length() > 0) {
	    s += " [" + g + "]";
	}
	if (e.length() > 0) {
	    s += " / " + e;
	}
	return s;

	/*  String s = m.getName();
	    String t = generate(m.getTrigger());
	    String g = generate(m.getGuard());
	    String e = generate(m.getEffect());
	    if(s == null) s = "";
	    if(t == null) t = "";
	    if (s.length() > 0 &&
	    (t.length() > 0 ||
	    (g != null && g.length() > 0) ||
	    (e != null && e.length() > 0)))
	    s += ": ";
	    s += t;
	    if (g != null && g.length() > 0) s += " [" + g + "]";
	    if (e != null && e.length() > 0) s += " / " + e;
	    return s;*/
    }


    private String generateAction(Object m) {
	// return m.getName();
	Object script = Model.getFacade().getScript(m);
	if ((script != null) && (Model.getFacade().getBody(script) != null)) {
	    return Model.getFacade().getBody(script).toString();
	}
	return "";
    }


    private String generateGuard(Object guard) {
	//return generateExpression(m.getExpression());
	if (Model.getFacade().getExpression(guard) != null) {
	    return generateExpression(Model.getFacade().getExpression(guard));
	}
	return "";
    }


    private String generateMessage(Object message) {
	if (message == null) {
	    return "";
	}
	return Model.getFacade().getName(message) + "::"
		+ generateAction(Model.getFacade().getAction(message));
    }

    /**
     * Generates the String representation for an Event.
     *
     * @param modelElement Model element to generate notation for.
     *
     * @return Generated notation for model element.
     */
    private String generateEvent(Object modelElement) {
        if (!Model.getFacade().isAEvent(modelElement)) {
            throw new ClassCastException(modelElement.getClass()
                    + " has wrong object type, Event required");
        }

        return "";
    }

    /**
     * Generates a section to guard manual modifications to code
     * @param cls The object
     * @return The generated section
     */
    private String generateSection(Object cls) {
        String id = UUIDHelper.getUUID(cls);
        assert id != null;
	// String s = "";
	// s += INDENT + "// section " + id + " begin\n";
	// s += INDENT + "// section " + id + " end\n";
	return Section.generate(id, INDENT);
    }


    /*
     * @see org.argouml.moduleloader.ModuleInterface#getName()
     */
    public String getName() {
        return "GeneratorCSharp";
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getInfo(int)
     */
    public String getInfo(int type) {
        switch (type) {
        case DESCRIPTION:
            return "CSharp Notation and Code Generator";
        case AUTHOR:
            return "Mike Lipki";
        case VERSION:
            return "0.1.0 - $Id: GeneratorCSharp.java 280 2010-01-11 21:16:34Z linus $";
        default:
            return null;
        }
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#enable()
     */
    public boolean enable() {
        GeneratorManager.getInstance().addGenerator(myLang, this);
        return true;
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#disable()
     */
    public boolean disable() {
        GeneratorManager.getInstance().removeGenerator(myLang);
        return true;
    }
    

    private String generateActionState(Object actionState) {
        String ret = "";
        Object action = Model.getFacade().getEntry(actionState);
        if (action != null) {
            Object expression = Model.getFacade().getScript(action);
            if (expression != null) {
                ret = generateExpression(expression);
            }
        }
        return ret;
    }

    /**
     * Generates the import statements of a source file
     * @param cls The file
     * @param packagePath the path to the package
     * @return The generated import statement
     */
    private String generateImports(Object cls, String packagePath) {
        // TODO: check also generalizations
        StringBuffer sb = new StringBuffer(80);
        Set<String> importSet = new HashSet<String>();

        for (Object mFeature : Model.getFacade().getFeatures(cls)) {
            if (Model.getFacade().isAAttribute(mFeature)) {
                addImportType(importSet, mFeature, packagePath);
            } else if (Model.getFacade().isAOperation(mFeature)) {
                // check the parameter types
                for (Object parameter 
                        : Model.getFacade().getParameters(mFeature)) {
                    addImportType(importSet, parameter, packagePath);
                }

                // check the return parameter types
                for (Object parameter 
                        : Model.getCoreHelper().getReturnParameters(mFeature)) {
                    addImportType(importSet, parameter, packagePath);
                }

                // check raised signals
                for (Object signal 
                        : Model.getFacade().getRaisedSignals(mFeature)) {
                    if (!Model.getFacade().isAException(signal)) {
                        continue;
                    }
                    addImport(importSet, signal, packagePath);
                }
            }
        }

        // now check packages of all generalized types
        for (Object gen : Model.getFacade().getGeneralizations(cls)) {
            Object parent = Model.getFacade().getGeneral(gen);
            if (parent == cls) {
                continue;
            }
            addImport(importSet, parent, packagePath);
        }

        // now check packages of the interfaces
        for (Object iface : Model.getFacade().getSpecifications(cls)) {
            addImport(importSet, iface, packagePath);
        }

        // check association end types
        for (Object associationEnd 
                : Model.getFacade().getAssociationEnds(cls)) {
            Object association = 
                Model.getFacade().getAssociation(associationEnd);
            for (Object associationEnd2 
                    : Model.getFacade().getConnections(association)) {
                if (associationEnd2 != associationEnd
                        && Model.getFacade().isNavigable(associationEnd2)
                        && !Model.getFacade().isAbstract(
                                Model.getFacade().getAssociation(
                                        associationEnd2))) {
                    // association end found
                    if (isCollection(associationEnd2)) {
                        importSet.add("System.Collections");
                    } else {
                        addImportType(importSet, associationEnd2, packagePath);
                    }
                }
            }
        }
        for (String imp : importSet) {
            sb.append("using ").append(imp).append(";");
	    sb.append(LINE_SEPARATOR);
        }
        if (!importSet.isEmpty()) {
            sb.append(LINE_SEPARATOR);
        }
        // Generate user section for using statements
        sb.append("// In this section you can add your own using directives");
        sb.append(LINE_SEPARATOR);
        sb.append(generateSection(cls));
        return sb.toString();
    }

    /**
     * Generate a single import.
     * 
     * @param element
     *                the model element to generate the import for
     * @param exclude
     *                a package name to exclude. If the computed package name
     *                equals this string, null will be returned instead.
     * @return The generated type
     */
    private String generateImport(Object element, String exclude) {
        String ret = null;
        if (element != null 
                && Model.getFacade().getNamespace(element) != null) {
            String p = getPackageName(Model.getFacade().getNamespace(element));
            if (!p.equals(exclude)) {
                ret = p;
		if (p.length() > 0) {
		    ret = p;
		} else {
		    ret = null;
		}
	    }
        }
        return ret;
    }
    
    /**
     * Add an import (if needed) for a single element (typically a classifier).
     */
    private void addImport(Set<String> imports, Object element, 
            String exclude) {
        String importString = generateImport(element, exclude);
        if (importString != null) {
            imports.add(importString);
        }
    }
    
    /**
     * Add an import for a typed element such as a StructuralFeature or 
     * Parameter.
     */
    private void addImportType(Set<String> imports, Object feature, 
            String exclude) {
        Object type = Model.getFacade().getType(feature);
        addImport(imports, type, exclude);
    }
    
    /**
     * Gets the .NET package name for a given namespace, ignoring the root
     * namespace (which is the model).
     * 
     * @param namespace
     *                the namespace
     * @return the Java package name
     */
    public String getPackageName(Object namespace) {
        if (namespace == null
	    || !Model.getFacade().isANamespace(namespace)
	    || Model.getFacade().getNamespace(namespace) == null) {
            return "";
        }
        String packagePath = Model.getFacade().getName(namespace);
        while ((namespace = Model.getFacade().getNamespace(namespace)) 
                != null) {
            // omit root package name; it's the model's root
            if (Model.getFacade().getNamespace(namespace) != null) {
                packagePath =
		    Model.getFacade().getName(namespace) + '.' + packagePath;
            }
        }
        return packagePath;
    }
    /**
     * 
     * @param elements
     * @param deps
     * @return N/A
     * @see org.argouml.uml.generator.CodeGenerator#generate(java.util.Collection, boolean)
     */
    public Collection<SourceUnit> generate(Collection elements, boolean deps) {
        LOG.debug("generate() called");
        File tmpdir = null;
        try {
            tmpdir = TempFileUtils.createTempDir();
            if (tmpdir != null) {
                generateFiles(elements, tmpdir.getPath(), deps);
                return TempFileUtils.readAllFiles(tmpdir);
            }
            return Collections.emptyList();
        } finally {
            if (tmpdir != null) {
                TempFileUtils.deleteDir(tmpdir);
            }
            LOG.debug("generate() terminated");
        }
    }
    
/**
 * 
 * @param elements
 * @param path
 * @param deps
 * @return THe generated files?
 * @see org.argouml.uml.generator.CodeGenerator#generateFiles(java.util.Collection, java.lang.String, boolean)
 */
    public Collection<String> generateFiles(Collection elements, String path,
            boolean deps) {
        LOG.debug("generateFiles() called");
        // TODO: 'deps' is ignored here
        for (Object element : elements) {
            generateFile(element, path);
        }
        return TempFileUtils.readFileNames(new File(path));
    }
    
    /*
     * @see org.argouml.uml.generator.CodeGenerator#generateFileList(java.util.Collection, boolean)
     */
    public Collection<String> generateFileList(Collection elements, 
            boolean deps) {
        LOG.debug("generateFileList() called");
        // TODO: 'deps' is ignored here
        File tmpdir = null;
        try {
            tmpdir = TempFileUtils.createTempDir();
            for (Object element : elements) {
                generateFile(element, tmpdir.getName());
            }
            return TempFileUtils.readFileNames(tmpdir);
        } finally {
            if (tmpdir != null) {
                TempFileUtils.deleteDir(tmpdir);
            }
        }
    }

    /**
     * 
     * @param expr
     * @return The generated expression
     */
    private static String generateExpression(Object expr) {
        if (Model.getFacade().isAExpression(expr)) {
            return generateUninterpreted(
                    (String) Model.getFacade().getBody(expr));
        }
        else if (Model.getFacade().isAConstraint(expr)) {
            return generateExpression(Model.getFacade().getBody(expr));
        }
        return "";
    }
    
    /**
     * 
     * @param un
     * @return un or an empty String
     */
    private static String generateUninterpreted(String un) {
        if (un == null) {
            return "";
        }
        return un;
    }

    /**
     * 
     * @param cls
     * @return Empty string or name of cls
     */
    private static String generateClassifierRef(Object cls) {
        if (cls == null) {
            return "";
        }
        return Model.getFacade().getName(cls);
    }

}
