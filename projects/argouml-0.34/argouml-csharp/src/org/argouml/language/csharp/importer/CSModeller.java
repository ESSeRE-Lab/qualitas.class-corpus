/* $Id: CSModeller.java 282 2010-01-12 18:11:03Z linus $
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

package org.argouml.language.csharp.importer;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.argouml.i18n.Translator;
import org.argouml.kernel.Project;
import org.argouml.language.csharp.importer.bridge.ModifierMap;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.members.FieldNode;
import org.argouml.language.csharp.importer.csparser.members.InterfaceMethodNode;
import org.argouml.language.csharp.importer.csparser.members.MethodNode;
import org.argouml.language.csharp.importer.csparser.members.ParamDeclNode;
import org.argouml.language.csharp.importer.csparser.members.PropertyNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;
import org.argouml.language.csharp.importer.csparser.structural.CompilationUnitNode;
import org.argouml.language.csharp.importer.csparser.structural.NamespaceNode;
import org.argouml.language.csharp.importer.csparser.structural.UsingDirectiveNode;
import org.argouml.language.csharp.importer.csparser.types.ClassNode;
import org.argouml.language.csharp.importer.csparser.types.InterfaceNode;
import org.argouml.model.Model;
import org.argouml.taskmgmt.ProgressMonitor;
import org.argouml.uml.reveng.ImportSettings;


/**
 * This is the modeller for C# reverse engineering.
 *
 * @author Thilina Hasantha <thilina.hasantha@gmail.com>
 */
public class CSModeller {
    private Project project;
    private ImportSettings settings;
    private CompilationUnitNode cu = null;
    private int phase = 0;

    /**
     * Following tags will be used to find the type of a uml element stored into
     * ele Hashtable while modeling
     */

    private static String TAG_CLASS = "cls#";     //Tag for classes
    private static String TAG_INTERFACE = "int#"; //Tag for interfaces
    private static String TAG_NS = "ns#";         //Tag for name spaces
    private static String TAG_GEN = "gen#";
    private static String TAG_EXTEND = "ext#";
    private static String TAG_OP = "opr#";
    private static String TAG_STEREOTYPE = "str#";


    private Object model;
    
    /**
     * Hashtable of elements, keyed by tag (see above) and names.
     */
    private Hashtable<String, Object> ele = new Hashtable<String, Object>();

    private boolean arraysAsDatatype;

    /**
     * @param p        currently opened project
     * @param settings import settings specified by the user
     */
    public CSModeller(Project p, ImportSettings settings) {
        this.project = p;
        // TODO: There may be multiple top level packages in a project
        model = p.getModel();
        this.settings = settings;
        // TODO: This was a private Java importer setting which was mistakenly
        // included in the API (and then deprecated immediately).  If it's 
        // needed the C# importer needs to define its own setting to control it.
//        arraysAsDatatype = settings.isDatatypeSelected();
        arraysAsDatatype = false;
    }

    /**
     * @param cNodes     list of CompilationUnitNodes (ASTs) grnrated by parser
     * @param monitor    Progress monitor dialog displayed while parsing files
     * @param startCount starting file number
     */
    public void model(List cNodes, ProgressMonitor monitor, int startCount) {

        int count = startCount;
        phase = 0;
        for (Object obj : cNodes) {
            if (monitor.isCanceled()) {
                monitor.updateSubTask(
                        Translator.localize("dialog.import.cancelled"));
                return;
            }
            cu = (CompilationUnitNode) obj;
            addNamespace(cu.DefaultNamespace);
            addNamespaceNodes(cu.Namespaces);
            monitor.updateProgress(count++);
        }
        phase++;
        for (Object obj : cNodes) {
            if (monitor.isCanceled()) {
                monitor.updateSubTask(
                        Translator.localize("dialog.import.cancelled"));
                return;
            }
            cu = (CompilationUnitNode) obj;
            addNamespace(cu.DefaultNamespace);
            addNamespaceNodes(cu.Namespaces);
            monitor.updateProgress(count++);
        }
    }

    /**
     * Add name space nodes to model
     *
     * @param nss list of name space nodes
     */
    public void addNamespaceNodes(NodeCollection<NamespaceNode> nss) {
        for (NamespaceNode ns : nss) {
            if (phase == 0) {
                addNamespace(ns);
            }
            addNamespaceClasses(ns);

        }
        for (NamespaceNode ns : nss) {
            addNamespaceNodes(ns.Namespaces);
        }
    }

    /**
     * Add all classes in a name space node
     *
     * @param ns namespace node
     */
    private void addNamespaceClasses(NamespaceNode ns) {
        String parent = buildToParent(ns.Name.Identifier, 
                ns.Name.Identifier.length);
        for (ClassNode cn : ns.Classes) {
            if (phase == 0) {
                addClass(cn.Modifiers, 
                        cn.Name.Identifier[cn.Name.Identifier.length - 1], 
                        parent);
            } else if (phase == 1) {
                addAttributes(cn, parent);
                addProperties(cn, parent);
                addMethods(cn, parent);
                buildGeneralization(cn, ns);
            }
        }
        for (InterfaceNode cn : ns.Interfaces) {
            if (phase == 0) {
                addInterface(cn.Modifiers, 
                        cn.Name.Identifier[cn.Name.Identifier.length - 1], 
                        parent);
            } else if (phase == 1) {
                addMethods(cn, parent);
            }
        }
    }

    /**
     * Add all mothds in a class to model
     *
     * @param cn
     * @param namespace
     */
    private void addMethods(ClassNode cn, String namespace) {
        for (MethodNode mn : cn.Methods) {
            addOperation(cn.Name.Identifier[0], mn, namespace);
        }
    }

    private void addMethods(InterfaceNode cn, String namespace) {
        for (InterfaceMethodNode mn : cn.Methods) {
            addOperation(cn.Name.Identifier[0], mn, namespace);
        }
    }

    /**
     * Add top level name spaces to model
     *
     * @param name
     */
    private void addRootNamesapce(String name) {
        if (ele.get(TAG_NS + name) != null) {
            return;
        }
        Object pk = Model.getModelManagementFactory().buildPackage(name);
        Model.getCoreHelper().setRoot(pk, true);
        Model.getCoreHelper().setNamespace(pk, model);
        Model.getCoreHelper().addOwnedElement(model, pk);
        ele.put(TAG_NS + name, pk);
    }

    private void addSubNamesapce(String name, String parent) {
        if (ele.get(TAG_NS + parent + "." + name) != null) {
            return;
        }
        Object pk = Model.getModelManagementFactory().buildPackage(name);
        Model.getCoreHelper().setRoot(pk, true);
        Model.getCoreHelper().setNamespace(pk, ele.get(TAG_NS + parent));
        ele.put(TAG_NS + parent + "." + name, pk);
    }

    public void addNamespace(NamespaceNode ns) {
        for (int i = 0; i < ns.Name.Identifier.length; i++) {
            if (i == 0) {
                addRootNamesapce(ns.Name.Identifier[i]);
            } else {
                addSubNamesapce(ns.Name.Identifier[i], 
                        buildToParent(ns.Name.Identifier, i));
            }
        }
        addFixedStereotypes();
    }


    private String buildToParent(String[] sa, int k) {
        String p = "";
        for (int i = 0; i < k; i++) {
            if (i < k - 1) {
                p += sa[i] + ".";
            } else {
                p += sa[i];
            }
        }
        return p;
    }

    public Object addClass(long modifiers, String name, String parent) {
        if (ele.get(TAG_CLASS + parent + "." + name) != null) {
            return ele.get(TAG_CLASS + parent + "." + name);
        }
        short cmod = ModifierMap.getUmlModifierForVisibility(modifiers);
        //concatModifires(modifiers);
        Object mClass = Model.getCoreFactory().createClass();
        Model.getCoreHelper().setName(mClass, name);
        Model.getCoreHelper().setNamespace(mClass, ele.get(TAG_NS + parent));
        setVisibility(mClass, cmod);
        Model.getCoreHelper().setAbstract(mClass,
                (cmod & CSharpConstants.ACC_ABSTRACT) > 0);
        Model.getCoreHelper().setLeaf(mClass,
                (cmod & CSharpConstants.ACC_FINAL) > 0);
        Model.getCoreHelper().setRoot(mClass, false);
        ele.put(TAG_CLASS + parent + "." + name, mClass);
        System.out.println("Add class " + TAG_CLASS + parent + "." + name);
        return mClass;
    }

    public Object addInterface(long modifiers, String name, String parent) {
        if (ele.get(TAG_INTERFACE + parent + "." + name) != null) {
            return ele.get(TAG_INTERFACE + parent + "." + name);
        }
        short cmod = ModifierMap.getUmlModifierForVisibility(modifiers);
        //concatModifires(modifiers);
        Object mInterface = Model.getCoreFactory().createInterface();
        Model.getCoreHelper().setName(mInterface, name);
        Model.getCoreHelper().setNamespace(mInterface, ele.get(TAG_NS + parent));
        setVisibility(mInterface, cmod);

        Model.getCoreHelper().setRoot(mInterface, false);
        ele.put(TAG_INTERFACE + parent + "." + name, mInterface);
        return mInterface;
    }

    public void addOperation(String parent, MethodNode mn, String cPackage) {

        String name = mn.names.get(0).Identifier[0];
        String className = cPackage + "." + parent;
        String id = TAG_OP + className + "." + name + getParameterTypeString(mn);

        if (ele.get(id) != null) {
            return;
        }

        short cmod = ModifierMap.getUmlModifierForVisibility(mn.modifiers);
        Object cls = ele.get(TAG_CLASS + className);

        //return

        Object classifier = null;
        //check in classes
        String temp = buildToParent(mn.type.Identifier.Identifier, 
                mn.type.Identifier.Identifier.length);
        classifier = getStoredDataType(temp, cPackage);

        Object mOperation = Model.getCoreFactory().buildOperation2(cls, classifier, name);
        setVisibility(mOperation, cmod);
        Model.getCoreHelper().setAbstract(mOperation,
                (cmod & CSharpConstants.ACC_ABSTRACT) > 0);
        Model.getCoreHelper().setLeaf(mOperation,
                (cmod & CSharpConstants.ACC_FINAL) > 0);
        Model.getCoreHelper().setRoot(mOperation, false);
        Model.getCoreHelper().setStatic(mOperation, 
                (cmod & CSharpConstants.ACC_STATIC) > 0);


        Object parameter = null;
        if (mn.params != null) {
            for (ParamDeclNode p : mn.params) {

                classifier = null;

                classifier = getStoredDataType(buildToParent(
                        p.type.Identifier.Identifier,
                        p.type.Identifier.Identifier.length), cPackage);
                parameter =
                        Model.getCoreFactory().buildParameter(mOperation, classifier);
                Model.getCoreHelper().setName(parameter, p.name);

            }
        }

        ele.put(id, mOperation);


    }


    public void addOperation(String parent, InterfaceMethodNode mn,
            String cPackage) {

        String name = mn.names.get(0).Identifier[0];
        String className = cPackage + "." + parent;
        String id = TAG_OP + className + "." + name + getParameterTypeString(mn);

        if (ele.get(id) != null) {
            return;
        }

        short cmod = ModifierMap.getUmlModifierForVisibility(mn.modifiers);
        Object cls = ele.get(TAG_CLASS + className);
        if (cls == null) {
            cls = ele.get(TAG_INTERFACE + className);
        }
        if (cls == null) {
            return;
        }
        //return

        Object classifier = null;
        //check in classes
        String temp = buildToParent(mn.type.Identifier.Identifier, 
                mn.type.Identifier.Identifier.length);
        classifier = getStoredDataType(temp, cPackage);

        Object mOperation = Model.getCoreFactory().buildOperation2(cls, classifier, name);
        setVisibility(mOperation, cmod);
        Model.getCoreHelper().setAbstract(mOperation,
                (cmod & CSharpConstants.ACC_ABSTRACT) > 0);
        Model.getCoreHelper().setLeaf(mOperation,
                (cmod & CSharpConstants.ACC_FINAL) > 0);
        Model.getCoreHelper().setRoot(mOperation, false);
        Model.getCoreHelper().setStatic(mOperation, 
                (cmod & CSharpConstants.ACC_STATIC) > 0);


        Object parameter = null;
        if (mn.params != null) {
            for (ParamDeclNode p : mn.params) {

                classifier = null;

                classifier = getStoredDataType(buildToParent(
                        p.type.Identifier.Identifier,
                        p.type.Identifier.Identifier.length), cPackage);
                parameter =
                        Model.getCoreFactory().buildParameter(mOperation, classifier);
                Model.getCoreHelper().setName(parameter, p.name);

            }
        }

        ele.put(id, mOperation);


    }


    void addAttributes(ClassNode cn, String cPackage) {
        if (cn.Fields != null) {
            for (FieldNode f : cn.Fields) {
                addAttribute(cn, f, cPackage);
            }
        }
    }

    void addProperties(ClassNode cn, String cPackage) {
        if (cn.Properties != null) {
            for (PropertyNode f : cn.Properties) {
                addProperty(cn, f, cPackage);
            }
        }
    }


    void addAttribute(ClassNode cn, FieldNode fn, String cPackage) {
//        System.out.println("Add attribute");
        short modifiers = ModifierMap.getUmlModifierForVisibility(fn.modifiers);
        String typeSpec = buildToParent(fn.type.Identifier.Identifier, 
                fn.type.Identifier.Identifier.length);
//        System.out.println("Complete class "+typeSpec);
        String name = buildToParent(fn.names.get(0).Identifier, 
                fn.names.get(0).Identifier.length);
//        System.out.println("Attrib name "+name);
        String initializer = null;
        String docs = "";
        boolean forceIt = false;


        String multiplicity = "1_1";
        Object mClassifier = null;
        String className = cPackage + "." + cn.Name.Identifier[0];
//        System.out.println("Class name "+className);
        Object cls = ele.get(TAG_CLASS + className);
//        Object cls = getClasesByName(cn.Name.Identifier[0], "");
//        if (cls == null) {
//            cls = ele.get(TAG_CLASS + className);
//        }

        if (typeSpec != null) {
            if (!arraysAsDatatype && typeSpec.indexOf('[') != -1) {
                typeSpec = typeSpec.substring(0, typeSpec.indexOf('['));
                multiplicity = "1_N";
            }
            mClassifier = getStoredDataType(typeSpec, cPackage);
        }

        Object mAttribute = buildAttribute(cls, mClassifier, name);
        setOwnerScope(mAttribute, modifiers);
        setVisibility(mAttribute, modifiers);
        Model.getCoreHelper().setMultiplicity(mAttribute, multiplicity);

        if (initializer != null) {

            // we must remove line endings and tabs from the intializer
            // strings, otherwise the classes will display horribly.
            initializer = initializer.replace('\n', ' ');
            initializer = initializer.replace('\t', ' ');

            Object newInitialValue =
                    Model.getDataTypesFactory()
                            .createExpression("CSharp",
                                    initializer);
            Model.getCoreHelper().setInitialValue(
                    mAttribute,
                    newInitialValue);
        }

        if ((modifiers & CSharpConstants.ACC_FINAL) > 0) {
            Model.getCoreHelper().setReadOnly(mAttribute, true);
        } else if (Model.getFacade().isReadOnly(mAttribute)) {
            Model.getCoreHelper().setReadOnly(mAttribute, true);
        }

    }


    private Object buildReturnParameter(Object operation, Object classifier) {
        Object parameter = buildParameter(operation, classifier, "return");
        Model.getCoreHelper().setKind(parameter, 
                Model.getDirectionKind().getReturnParameter());
        return parameter;
    }

    private Object buildParameter(Object operation, Object classifier,
                                  String name) {
        Object parameter =
                Model.getCoreFactory().buildParameter(operation, classifier);
        Model.getCoreHelper().setName(parameter, name);
        return parameter;
    }

    private void buildGeneralization(ClassNode cn, NamespaceNode ns) {
        if (cn.BaseClasses != null) {
            for (TypeNode tn : cn.BaseClasses) {
                String parent = buildToParent(tn.Identifier.Identifier, 
                        tn.Identifier.Identifier.length);
                String child = buildToParent(cn.Name.Identifier, 
                        cn.Name.Identifier.length);
                String pkg = buildToParent(ns.Name.Identifier, 
                        ns.Name.Identifier.length);

                //System.out.println("AD GEN");
                //System.out.println("Child : "+child);
                //System.out.println("Parent : "+parent);
                //System.out.println("pkg : "+pkg);

                Object c = getStoredDataType(child, pkg);
                Object p = getStoredDataType(parent, pkg);
                Object n = getNameSpace(pkg);
                Object g = null;
                if (Model.getFacade().isAInterface(p)) {
                    g = buildRealization(c, p, n);
                } else {
                    g = buildGeneralizations(c, p);
                }
                if (g != null)
                    Model.getCoreHelper().setName(g, child + " -> " + parent);

            }
        }
    }


    private Object buildGeneralizations(Object child, Object parnt) {
        Object gen = Model.getCoreFactory().buildGeneralization(child, parnt);
        return gen;
    }

    private Object buildRealization(Object child, Object parnt, Object namespace) {
        Object rel = Model.getCoreFactory().buildRealization(child, parnt, namespace);
        return rel;
    }


    Object getClasesByName(String paramType, String cPackage) {
//        System.out.println("<getClasesByName> "+paramType);
        Object kx = null;
        if (paramType.contains(".")) {
            return ele.get(TAG_CLASS + paramType);
        } else {
            kx = ele.get(TAG_CLASS + cPackage + "."
                    + paramType);
            System.out.println(kx);
            if (kx != null) {
                return kx;
            }
            for (UsingDirectiveNode u : cu.UsingDirectives) {
                String temp = buildToParent(u.Target.Identifier, 
                        u.Target.Identifier.length);
//                System.out.println(temp);
                kx = ele.get(TAG_CLASS + temp + "."
                        + paramType);
                System.out.println(kx);
                if (kx != null) {
                    return kx;
                }
            }
            return null;
        }
    }

    Object getInterfaceByName(String paramType, String cPackage) {
        Object kx = null;
        if (paramType.contains(".")) {
            return ele.get(TAG_INTERFACE + paramType);
        } else {
            kx = ele.get(TAG_INTERFACE + cPackage + "."
                    + paramType);
            if (kx != null) {
                return kx;
            }
            for (UsingDirectiveNode u : cu.UsingDirectives) {
                kx = ele.get(TAG_INTERFACE
                        + buildToParent(u.Target.Identifier,
                                u.Target.Identifier.length) + "." + paramType);
                if (kx != null) {
                    return kx;
                }
            }
            return null;
        }
    }

    /**
     * Set the visibility for a model element.
     *
     * @param element   The model element.
     * @param modifiers A sequence of modifiers which may contain
     *                  'private', 'protected' or 'public'.
     */
    private void setVisibility(Object element, short modifiers) {
        if ((modifiers & CSharpConstants.ACC_PRIVATE) > 0) {
            Model.getCoreHelper().setVisibility(
                    element,
                    Model.getVisibilityKind().getPrivate());
        } else if ((modifiers & CSharpConstants.ACC_PROTECTED) > 0) {
            Model.getCoreHelper().setVisibility(
                    element,
                    Model.getVisibilityKind().getProtected());
        } else if ((modifiers & CSharpConstants.ACC_PUBLIC) > 0) {
            Model.getCoreHelper().setVisibility(
                    element,
                    Model.getVisibilityKind().getPublic());
        } else {
            Model.getCoreHelper().setVisibility(
                    element,
                    Model.getVisibilityKind().getPackage());
        }
    }


    private void setOwnerScope(Object feature, short modifiers) {
        Model.getCoreHelper().setStatic(
                feature, (modifiers & CSharpConstants.ACC_STATIC) > 0);
    }

    private String getParameterTypeString(MethodNode mn) {
        String k = "";
        if (mn.params == null) {
            return k;
        }
        for (ParamDeclNode p : mn.params) {
            k += "|" + buildToParent(p.type.Identifier.Identifier,
                    p.type.Identifier.Identifier.length);
        }
        return k.toLowerCase();
    }

    private String getParameterTypeString(InterfaceMethodNode mn) {
        String k = "";
        if (mn.params == null) {
            return k;
        }
        for (ParamDeclNode p : mn.params) {
            k += "|" + buildToParent(p.type.Identifier.Identifier,
                    p.type.Identifier.Identifier.length);
        }
        return k.toLowerCase();
    }


    private short concatModifires(short[] modifiers) {
        short mod = 0;
        for (int i = 0; i < modifiers.length; i++) {
            mod = (short) (mod + modifiers[i]);
        }
        return mod;
    }

    public Collection getNewElements() {
        return ele.values();
    }

    public Object getStoredDataType(String name, String cPackage) {

        Object classifier = getClasesByName(name, cPackage);
        if (classifier == null) {
            classifier = getInterfaceByName(name, cPackage);
        }
        if (classifier == null) {
            classifier = addClass(0, name, /*cPackage*/"DefaultNamespace");
            ele.put(TAG_CLASS + "DefaultNamespace" + "." + name, classifier);
        }
        return classifier;
    }

    public Object getNameSpace(String pkg) {
        return ele.get(TAG_NS + pkg);
    }

    private Object buildAttribute(Object classifier, Object type, String name) {
        Object mAttribute =
                Model.getCoreFactory().buildAttribute2(classifier, type);
        Model.getCoreHelper().setName(mAttribute, name);
        return mAttribute;
    }

    void addProperty(ClassNode cn, PropertyNode fn, String cPackage) {

        short modifiers = ModifierMap.getUmlModifierForVisibility(fn.modifiers);
        String typeSpec = buildToParent(fn.type.Identifier.Identifier, fn.type.Identifier.Identifier.length);
        String name = buildToParent(fn.names.get(0).Identifier, fn.names.get(0).Identifier.length);
        String initializer = null;
        String docs = "";
        boolean forceIt = false;


        String multiplicity = "1_1";
        Object mClassifier = null;
        String className = cPackage + "." + cn.Name.Identifier[0];
        Object cls = ele.get(TAG_CLASS + className);

        if (typeSpec != null) {
            if (!arraysAsDatatype && typeSpec.indexOf('[') != -1) {
                typeSpec = typeSpec.substring(0, typeSpec.indexOf('['));
                multiplicity = "1_N";
            }
            mClassifier = getStoredDataType(typeSpec, cPackage);
        }

        // if we want to create a UML attribute:
        //if (noAssociations) {
        Object mAttribute = buildAttribute(cls, mClassifier, name);
        setOwnerScope(mAttribute, modifiers);
        setVisibility(mAttribute, modifiers);
        Model.getCoreHelper().setMultiplicity(mAttribute, multiplicity);
        //=======

        if (fn.getter != null && fn.setter != null) {
            applyReadWriteStereotype(mAttribute);
        } else if (fn.getter != null) {
            //System.out.println("Only gatter "+name);
            applyWriteOnlyStereotype(mAttribute);
        } else if (fn.setter != null) {
            //System.out.println("Only setter "+name);
            applyReadOnlyStereotype(mAttribute);
        }
        // Set the initial value for the attribute.
        if (initializer != null) {

            // we must remove line endings and tabs from the intializer
            // strings, otherwise the classes will display horribly.
            initializer = initializer.replace('\n', ' ');
            initializer = initializer.replace('\t', ' ');

            Object newInitialValue =
                    Model.getDataTypesFactory()
                            .createExpression("CSharp",
                                    initializer);
            Model.getCoreHelper().setInitialValue(
                    mAttribute,
                    newInitialValue);
        }

        if ((modifiers & CSharpConstants.ACC_FINAL) > 0) {
            Model.getCoreHelper().setReadOnly(mAttribute, true);
        } else if (Model.getFacade().isReadOnly(mAttribute)) {
            Model.getCoreHelper().setReadOnly(mAttribute, true);
        }
    }


    private void applyReadWriteStereotype(Object property) {
        Object mSt = ele.get(TAG_STEREOTYPE + "DefaultNamespace" + "."
                + "CSharp_Property_rw");
        if (mSt != null) {
            Model.getCoreHelper().addStereotype(property, mSt);
        }
    }

    private void applyWriteOnlyStereotype(Object property) {
        Object mSt = ele.get(TAG_STEREOTYPE + "DefaultNamespace" + "."
                + "CSharp_Property_ro");
        if (mSt != null) {
            Model.getCoreHelper().addStereotype(property, mSt);
        }
    }

    private void applyReadOnlyStereotype(Object property) {
        Object mSt = ele.get(TAG_STEREOTYPE + "DefaultNamespace" + "."
                + "CSharp_Property_ro");
        if (mSt != null) {
            Model.getCoreHelper().addStereotype(property, mSt);
        }
    }

    /**
     * Add read-write, read-only and write-only stereotypes to model
     */
    private void addFixedStereotypes() {
        Object mSt = ele.get(TAG_STEREOTYPE + "DefaultNamespace" 
                + "." + "CSharp_Property_rw");
        if (mSt == null) {
            //adding stereotype CSharp Property to default namespace
            Object strCP = Model.getExtensionMechanismsFactory()
                    .buildStereotype("CSharp Property", model);
            Object tv = Model.getExtensionMechanismsFactory().buildTaggedValue(
                    "accessors", "read-only");
            Model.getExtensionMechanismsHelper().addTaggedValue(strCP, tv);
            ele.put(TAG_STEREOTYPE + "DefaultNamespace" + "." 
                    + "CSharp_Property_ro", strCP);

            //adding stereotype CSharp Property to default namespace
            strCP = Model.getExtensionMechanismsFactory()
                    .buildStereotype("CSharp Property", model);
            tv = Model.getExtensionMechanismsFactory().buildTaggedValue(
                    "accessors", "write-only");
            Model.getExtensionMechanismsHelper().addTaggedValue(strCP, tv);
            ele.put(TAG_STEREOTYPE + "DefaultNamespace" + "." 
                    + "CSharp_Property_wo", strCP);

            //adding stereotype CSharp Property to default namespace
            strCP = Model.getExtensionMechanismsFactory()
                    .buildStereotype("CSharp Property", model);
            tv = Model.getExtensionMechanismsFactory().buildTaggedValue(
                    "accessors", "read-and-write");
            Model.getExtensionMechanismsHelper().addTaggedValue(strCP, tv);
            ele.put(TAG_STEREOTYPE + "DefaultNamespace" + "." 
                    + "CSharp_Property_rw", strCP);
        }
    }

}
