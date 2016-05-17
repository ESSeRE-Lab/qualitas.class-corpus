/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl.newjvm;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

import java.rmi.*;

// NOTE: Do NOT import/use the config framework in this class!
//  (This class runs in a different JVM, and will not share the config object)


import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.util.InputStreamRedirector;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.OptionVisitor;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.text.TextUtil;

import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.junit.JUnitModelCallback;
import edu.rice.cs.drjava.model.junit.JUnitTestManager;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.repl.InteractionsPaneOptions;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.*;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;

// For Windows focus fix
import javax.swing.JDialog;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

/** This is the main class for the interpreter JVM.  All public methods except those involving remote calls (callbacks) 
  * synchronized (unless synchronization has no effect).  This class is loaded in the Interpreter JVM, not the Main JVM. 
  * (Do not use DrJava's config framework here.)
  * <p>
  * Note that this class is specific to DynamicJava. It must be refactored to accommodate other interpreters.
  * @version $Id: InterpreterJVM.java 5340 2010-07-29 02:30:24Z mgricken $
  */
public class InterpreterJVM extends AbstractSlaveJVM implements InterpreterJVMRemoteI, JUnitModelCallback {
  
  /** Singleton instance of this class. */
  public static final InterpreterJVM ONLY = new InterpreterJVM();
  
  // As RMI can lead to parallel threads, all fields must be thread-safe.  Collections are wrapped
  // in synchronized versions.
  
  private final InteractionsPaneOptions _interpreterOptions;
  private volatile Pair<String, Interpreter> _activeInterpreter;
  private final Interpreter _defaultInterpreter;
  private final Map<String, Interpreter> _interpreters;
  private final Set<Interpreter> _busyInterpreters;
  private final Map<String, Pair<TypeContext, RuntimeBindings>> _environments;
  
  private final ClassPathManager _classPathManager;
  private final ClassLoader _interpreterLoader;
  
  /** Responsible for running JUnit tests in this JVM. */
  private final JUnitTestManager _junitTestManager;
  
  /** Remote reference to the MainJVM class in DrJava's primary JVM.  Assigned ONLY once. */
  private volatile MainJVMRemoteI _mainJVM;
  
  /** Private constructor; use the singleton ONLY instance. */
  private InterpreterJVM() {
    super("Reset Interactions Thread", "Poll DrJava Thread");
    
    _classPathManager = new ClassPathManager(ReflectUtil.SYSTEM_CLASS_PATH);
    _interpreterLoader = _classPathManager.makeClassLoader(null);
    _junitTestManager = new JUnitTestManager(this, _classPathManager);

    // set the thread context class loader, this way NextGen and Mint can use the interpreter's class loader
    Thread.currentThread().setContextClassLoader(_interpreterLoader);
    
    // _interpreterOptions = Options.DEFAULT;
    _interpreterOptions = new InteractionsPaneOptions();
    _defaultInterpreter = new Interpreter(_interpreterOptions, _interpreterLoader);
    _interpreters = new HashMap<String,Interpreter>();
    _busyInterpreters = new HashSet<Interpreter>();
    _environments = new HashMap<String, Pair<TypeContext, RuntimeBindings>>();
    _activeInterpreter = Pair.make("", _defaultInterpreter);
  }
  
  /** Actions to perform when this JVM is started (through its superclass, AbstractSlaveJVM). */
  protected void handleStart(MasterRemote mainJVM) {
    //_dialog("handleStart");
    _mainJVM = (MainJVMRemoteI) mainJVM;
    
    // redirect stdin
    System.setIn(new InputStreamRedirector() {
      protected String _getInput() {
        try { return _mainJVM.getConsoleInput(); }
        catch(RemoteException re) {
          error.log(re);
          throw new UnexpectedException("Main JVM can't be reached for input.\n" + re);
        }
      }
    });
    
    // redirect stdout
    System.setOut(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) {
        try { _mainJVM.systemOutPrint(s); }
        catch (RemoteException re) {
          error.log(re);
          throw new UnexpectedException("Main JVM can't be reached for output.\n" + re);
        }
      }
    }));
    
    // redirect stderr
    System.setErr(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) {
        try { _mainJVM.systemErrPrint(s); }
        catch (RemoteException re) {
          error.log(re);
          throw new UnexpectedException("Main JVM can't be reached for output.\n" + re);
        }
      }
    }));
    
    /* On Windows, any frame or dialog opened from Interactions pane will appear *behind* DrJava's frame, unless a 
     * previous frame or dialog is shown here.  Not sure what the difference is, but this hack seems to work.  (I'd
     * be happy to find a better solution, though.)  Only necessary on Windows, since frames and dialogs on other 
     * platforms appear correctly in front of DrJava. */
    if (PlatformFactory.ONLY.isWindowsPlatform()) {
      JDialog d = new JDialog();
      d.setSize(0,0);
      d.setVisible(true);
      d.setVisible(false);
    }
    //_dialog("interpreter JVM started");
  }
  
  /** Interprets the given string of source code in the active interpreter. The result is returned to MainJVM via 
    * the interpretResult method.
    * @param s Source code to interpret.
    */
  public InterpretResult interpret(String s) { return interpret(s, _activeInterpreter.second()); }
  
  /** Interprets the given string of source code with the given interpreter. The result is returned to
    * MainJVM via the interpretResult method.
    * @param s Source code to interpret.
    * @param interpreterName Name of the interpreter to use
    * @throws IllegalArgumentException if the named interpreter does not exist
    */
  public InterpretResult interpret(String s, String interpreterName) {
    Interpreter i = _interpreters.get(interpreterName);
    if (i == null) {
      throw new IllegalArgumentException("Interpreter '" + interpreterName + "' does not exist.");
    }
    return interpret(s, i);
  }
  
  private InterpretResult interpret(String input, Interpreter interpreter) {
    debug.logStart("Interpret " + input);
    
    boolean available = _busyInterpreters.add(interpreter);
    if (!available) { debug.logEnd(); return InterpretResult.busy(); }
    
    // set the thread context class loader, this way NextGen and Mint can use the interpreter's class loader
    Thread.currentThread().setContextClassLoader(_interpreterLoader);

    Option<Object> result = null;
    try { result = interpreter.interpret(input); }
    catch (InterpreterException e) { debug.logEnd(); return InterpretResult.exception(e); }
    catch (Throwable e) { debug.logEnd(); return InterpretResult.unexpectedException(e); }
    finally { _busyInterpreters.remove(interpreter); }
    
    return result.apply(new OptionVisitor<Object, InterpretResult>() {
      public InterpretResult forNone() { return InterpretResult.noValue(); }
      public InterpretResult forSome(Object obj) {
        if (obj instanceof String) { debug.logEnd(); return InterpretResult.stringValue((String) obj); }
        else if (obj instanceof Character) { debug.logEnd(); return InterpretResult.charValue((Character) obj); }
        else if (obj instanceof Number) { debug.logEnd(); return InterpretResult.numberValue((Number) obj); }
        else if (obj instanceof Boolean) { debug.logEnd(); return InterpretResult.booleanValue((Boolean) obj); }
        else {
          try {
            String resultString = TextUtil.toString(obj);
            String resultTypeStr = null;
            if (obj!=null) {
                Class<?> c = obj.getClass();
                resultTypeStr = getClassName(c);
            }
            debug.logEnd();
            return InterpretResult.objectValue(resultString,resultTypeStr);
          }
          catch (Throwable t) {
            // an exception occurred during toString
            debug.logEnd(); 
            return InterpretResult.exception(new EvaluatorException(t));
          }
        }
      }
    });
  }
  
  /** Gets the value of the variable with the given name in the current interpreter.
    * Invoked reflectively by the debugger.  To simplify the inter-process exchange,
    * an array here is used as the return type rather than an {@code Option<Object>} --
    * an empty array corresponds to "none," and a singleton array corresponds to a "some."
    * @param var name of the variable to look up
    * @return empty array for "none", singleton array for "some" value
    * @see edu.rice.cs.drjava.model.debug.jpda.JPDADebugger#GET_VARIABLE_VALUE_SIG
    * @see edu.rice.cs.drjava.model.debug.jpda.JPDADebugger#_copyVariablesFromInterpreter()
    */
  public Object[] getVariableValue(String var) {
    Pair<Object,String>[] arr = getVariable(var);
    if (arr.length==0) return new Object[0];
    else return new Object[] { arr[0].first() };
  }
  
  /** Gets the value and type string of the variable with the given name in the current interpreter.
    * Invoked reflectively by the debugger.  To simplify the inter-process exchange,
    * an array here is used as the return type rather than an {@code Option<Object>} --
    * an empty array corresponds to "none," and a singleton array corresponds to a "some."
    */
  @SuppressWarnings("unchecked")
  public Pair<Object,String>[] getVariable(String var) {
    InterpretResult ir = interpret(var);
    return ir.apply(new InterpretResult.Visitor<Pair<Object,String>[]>() {
        public Pair<Object,String>[] fail() { return new Pair[0]; }
        public Pair<Object,String>[] value(Object val) {
          return new Pair[] { new Pair<Object,String>(val, getClassName(val.getClass())) };
        }
        public Pair<Object,String>[] forNoValue() { return fail(); }
        public Pair<Object,String>[] forStringValue(String val) { return value(val); }
        public Pair<Object,String>[] forCharValue(Character val) { return value(val); }
        public Pair<Object,String>[] forNumberValue(Number val) { return value(val); }
        public Pair<Object,String>[] forBooleanValue(Boolean val) { return value(val); }
        public Pair<Object,String>[] forObjectValue(String valString, String objTypeString) {
            return new Pair[] { new Pair<Object,String>(valString, objTypeString) }; }
        public Pair<Object,String>[] forException(String message) { return fail(); }
        public Pair<Object,String>[] forEvalException(String message, StackTraceElement[] stackTrace) { return fail(); }
        public Pair<Object,String>[] forUnexpectedException(Throwable t) { return fail(); }
        public Pair<Object,String>[] forBusy() { return fail(); }
    });
  }

  /** Gets the string representation of the value of a variable in the current interpreter.
    * @param var the name of the variable
    * @return null if the variable is not defined; the first part of the pair is "null" if the value is null,
    * otherwise its string representation; the second part is the string representation of the variable's type
    */
  public Pair<String,String> getVariableToString(String var) {
//    if (!isValidFieldName(var)) { return "<error in watch name>"; }
    Pair<Object,String>[] val = getVariable(var);
    if (val.length == 0) { return new Pair<String,String>(null,null); }
    else {
      Object o = val[0].first();
      try { return new Pair<String,String>(TextUtil.toString(o),val[0].second()); }
      catch (Throwable t) { return new Pair<String,String>("<error in toString()>",""); }
    }
  }

  /** @return the name of the class, with the right number of array suffixes "[]" and while being ambiguous
    * about boxed and primitive types. */
  public static String getClassName(Class<?> c) {
    StringBuilder sb = new StringBuilder();
    boolean isArray = c.isArray();
    while(c.isArray()) {
      sb.append("[]");
      c = c.getComponentType();
    }
    
    if (!isArray) {
      // we can't distinguish primitive types from their boxed types right now
      if (c.equals(Byte.class))      { return "byte"+sb.toString()+" or "+c.getSimpleName()+sb.toString(); } 
      if (c.equals(Short.class))     { return "short"+sb.toString()+" or "+c.getSimpleName()+sb.toString(); }
      if (c.equals(Integer.class))   { return "int"+sb.toString()+" or "+c.getSimpleName()+sb.toString(); }
      if (c.equals(Long.class))      { return "long"+sb.toString()+" or "+c.getSimpleName()+sb.toString(); }
      if (c.equals(Float.class))     { return "float"+sb.toString()+" or "+c.getSimpleName()+sb.toString(); }
      if (c.equals(Double.class))    { return "double"+sb.toString()+" or "+c.getSimpleName()+sb.toString(); }
      if (c.equals(Boolean.class))   { return "boolean"+sb.toString()+" or "+c.getSimpleName()+sb.toString(); }
      if (c.equals(Character.class)) { return "char"+sb.toString()+" or "+c.getSimpleName()+sb.toString(); }
      else return c.getName()+sb.toString();
    }
    else {
      // if it's an array, we can distinguish boxed types and primitive types
      return c.getName()+sb.toString();
    }
  }
  
  /** Adds a named Interpreter to the list.
    * @param name the unique name for the interpreter
    * @throws IllegalArgumentException if the name is not unique
    */
  public void addInterpreter(String name) {
    if (_interpreters.containsKey(name)) {
      throw new IllegalArgumentException("'" + name + "' is not a unique interpreter name");
    }
    Interpreter i = new Interpreter(_interpreterOptions, _interpreterLoader);
    _interpreters.put(name, i);
  }
  
  /** Adds a named Interpreter in the given environment to the list.  Invoked reflectively by
    * the debugger.
    * @param name  The unique name for the interpreter
    * @param thisVal  The value of {@code this} (may be null, implying this is a static context)
    * @param thisClass  The class in whose context the interpreter is to be created
    * @param localVars  Values of local variables
    * @param localVarNames  Names of the local variables
    * @param localVarClasses  Classes of the local variables.  To simplify the work callers must
    *                         do, a value with a primitive type may have a {@code null} entry here.
    * @throws IllegalArgumentException if the name is not unique, or if the local var arrays
    *                                  are not all of the same length
    * @see edu.rice.cs.drjava.model.debug.jpda.JPDADebugger#ADD_INTERPRETER_SIG
    * @see edu.rice.cs.drjava.model.debug.jpda.JPDADebugger#_dumpVariablesIntoInterpreterAndSwitch
    */
  public void addInterpreter(String name, Object thisVal, Class<?> thisClass, Object[] localVars,
                             String[] localVarNames, Class<?>[] localVarClasses) {
    debug.logValues(new String[]{ "name", "thisVal", "thisClass", "localVars", "localVarNames",
      "localVarClasses" }, name, thisVal, thisClass, localVars, localVarNames, localVarClasses);
    if (_interpreters.containsKey(name)) {
      throw new IllegalArgumentException("'" + name + "' is not a unique interpreter name");
    }
    if (localVars.length != localVarNames.length || localVars.length != localVarClasses.length) {
      throw new IllegalArgumentException("Local variable arrays are inconsistent");
    }
    
    // TODO: handle inner classes
    // TODO: enforce final vars?
    Package pkg = thisClass.getPackage();
    DJClass c = SymbolUtil.wrapClass(thisClass);
    List<LocalVariable> vars = new LinkedList<LocalVariable>();
    for (int i = 0; i < localVars.length; i++) {
      if (localVarClasses[i] == null) {
        try { localVarClasses[i] = (Class<?>) localVars[i].getClass().getField("TYPE").get(null); }
        catch (IllegalAccessException e) { throw new IllegalArgumentException(e); }
        catch (NoSuchFieldException e) { throw new IllegalArgumentException(e); }
      }
      Type varT = SymbolUtil.typeOfGeneralClass(localVarClasses[i], _interpreterOptions.typeSystem());
      vars.add(new LocalVariable(localVarNames[i], varT, false));
    }
    
    TypeContext ctx = new ImportContext(_interpreterLoader, _interpreterOptions);
    if (pkg != null) { ctx = ctx.setPackage(pkg.getName()); }
    ctx = new ClassSignatureContext(ctx, c, _interpreterLoader);
    ctx = new ClassContext(ctx, c);
    ctx = new DebugMethodContext(ctx, thisVal == null);
    ctx = new LocalContext(ctx, vars);
    
    RuntimeBindings bindings = RuntimeBindings.EMPTY;
    if (thisVal != null) { bindings = new RuntimeBindings(bindings, c, thisVal); }
    bindings = new RuntimeBindings(bindings, vars, IterUtil.asIterable(localVars));
    
    Interpreter i = new Interpreter(_interpreterOptions, ctx, bindings);
    _environments.put(name, Pair.make(ctx, bindings));
    _interpreters.put(name, i);
  }
  
  /** A custom context for interpreting within the body of a defined method. */
  private static class DebugMethodContext extends DelegatingContext {
    private final boolean _isStatic;
    public DebugMethodContext(TypeContext next, boolean isStatic) { super(next); _isStatic = isStatic; }
    protected TypeContext duplicate(TypeContext next) { return new DebugMethodContext(next, _isStatic); }
    @Override public DJClass getThis() { return _isStatic ? null : super.getThis(); }
    @Override public DJClass getThis(String className) { return _isStatic ? null : super.getThis(className); }
    @Override public Type getReturnType() { return null; }
    @Override public Iterable<Type> getDeclaredThrownTypes() { return IterUtil.empty(); }
  }
  
  
  /** Removes the interpreter with the given name, if it exists. */
  public void removeInterpreter(String name) {
    _interpreters.remove(name);
    _environments.remove(name);
  }
  
  
  /** Sets the current interpreter to be the one specified by the given name
    * @param name the unique name of the interpreter to set active
    * @return Status flags: whether the current interpreter changed, and whether it is busy
    */
  public synchronized Pair<Boolean, Boolean> setActiveInterpreter(String name) {
    Interpreter i = _interpreters.get(name);
    if (i == null) { throw new IllegalArgumentException("Interpreter '" + name + "' does not exist."); }
    boolean changed = (i != _activeInterpreter.second());
    _activeInterpreter = Pair.make(name, i);
    return Pair.make(changed, _busyInterpreters.contains(i));
  }
  
  /** Sets the default interpreter to be active.
    * @return Status flags: whether the current interpreter changed, and whether it is busy
    */
  public synchronized Pair<Boolean, Boolean> setToDefaultInterpreter() {
    boolean changed = (_defaultInterpreter != _activeInterpreter.second());
    _activeInterpreter = Pair.make("", _defaultInterpreter);
    return Pair.make(changed, _busyInterpreters.contains(_defaultInterpreter));
  }
  
  /** Check that all access of class members is permitted by accessibility controls. */
  public synchronized void setEnforceAllAccess(boolean enforce) {
    _interpreterOptions.setEnforceAllAccess(enforce);
  }
  
  /** Check that access of private class members is permitted (irrelevant if setEnforceAllAccess() is set to true). */
  public synchronized void setEnforcePrivateAccess(boolean enforce) {
    _interpreterOptions.setEnforcePrivateAccess(enforce);
  }

  /** Require a semicolon at the end of statements. */
  public synchronized void setRequireSemicolon(boolean require) {
    _interpreterOptions.setRequireSemicolon(require);
  }
  
  /** Require variable declarations to include an explicit type. */
  public synchronized void setRequireVariableType(boolean require) {
    _interpreterOptions.setRequireVariableType(require);
  }
  
  // ---------- JUnit methods ----------
  /** Sets up a JUnit test suite in the Interpreter JVM and finds which classes are really TestCases classes (by 
    * loading them).  Unsynchronized because it contains a remote call and does not involve mutable local state.
    * @param classNames the class names to run in a test
    * @param files the associated file
    * @return the class names that are actually test cases
    */
  public List<String> findTestClasses(List<String> classNames, List<File> files) throws RemoteException {
    return _junitTestManager.findTestClasses(classNames, files);
  }
  
  /** Runs JUnit test suite already cached in the Interpreter JVM.  Unsynchronized because it contains a remote call
    * and does not involve mutable local state.
    * @return false if no test suite is cached; true otherwise
    */
  public boolean runTestSuite() throws RemoteException { return _junitTestManager.runTestSuite(); }
  
  /** Notifies Main JVM that JUnit has been invoked on a non TestCase class.  Unsynchronized because it contains a 
    * remote call and does not involve mutable local state.
    * @param isTestAll whether or not it was a use of the test all button
    * @param didCompileFail whether or not a compile before this JUnit attempt failed
    */
  public void nonTestCase(boolean isTestAll, boolean didCompileFail) {
    try { _mainJVM.nonTestCase(isTestAll, didCompileFail); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Notifies the main JVM that JUnitTestManager has encountered an illegal class file.  Unsynchronized because it 
    * contains a remote call and does not involve mutable local state.
    * @param e the ClassFileError object describing the error on loading the file
    */
  public void classFileError(ClassFileError e) {
    try { _mainJVM.classFileError(e); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Notifies that a suite of tests has started running.  Unsynchronized because it contains a remote call and does
    * not involve mutable local state.
    * @param numTests The number of tests in the suite to be run.
    */
  public void testSuiteStarted(int numTests) {
    try { _mainJVM.testSuiteStarted(numTests); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Notifies that a particular test has started.  Unsynchronized because it contains a remote call and does not
    * involve mutable local state.
    * @param testName The name of the test being started.
    */
  public void testStarted(String testName) {
    try { _mainJVM.testStarted(testName); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Notifies that a particular test has ended.  Unsynchronized because it contains a remote call.
    * @param testName The name of the test that has ended.
    * @param wasSuccessful Whether the test passed or not.
    * @param causedError If not successful, whether the test caused an error or simply failed.
    */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError) {
    try { _mainJVM.testEnded(testName, wasSuccessful, causedError); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Notifies that a full suite of tests has finished running.  Unsynchronized because it contains a remote call
    * and does not involve mutable local state.
    * @param errors The array of errors from all failed tests in the suite.
    */
  public void testSuiteEnded(JUnitError[] errors) {
    try { _mainJVM.testSuiteEnded(errors); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Called when the JUnitTestManager wants to open a file that is not currently open.  Unsynchronized because it 
    * contains a remote call and does not involve mutable local state.
    * @param className the name of the class for which we want to find the file
    * @return the file associated with the given class
    */
  public File getFileForClassName(String className) {
    try { return _mainJVM.getFileForClassName(className); }
    catch (RemoteException re) { error.log(re); return null; }
  }
  
  public void junitJVMReady() { }
  
  // --------- Class path methods ----------
  public void addExtraClassPath(File f) { _classPathManager.addExtraCP(f); }
  public void addProjectClassPath(File f) { _classPathManager.addProjectCP(f); }
  public void addBuildDirectoryClassPath(File f) { _classPathManager.addBuildDirectoryCP(f); }
  public void addProjectFilesClassPath(File f) { _classPathManager.addProjectFilesCP(f); }
  public void addExternalFilesClassPath(File f) { _classPathManager.addExternalFilesCP(f); }
  public Iterable<File> getClassPath() {
    // need to make a serializable snapshot
    return IterUtil.snapshot(_classPathManager.getClassPath());
  }
  
}
