/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2004 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: c-jdbc@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Charlie Hubbard, Prashant Dhokte.
 * Contributor(s): ______________________________________.
 */

package org.objectweb.cjdbc.scenario.tools.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * a.k.a. The "ObjectMolester"
 * 
 * <p>
 * This class is used to access a method or field of an object no matter what
 * the access modifier of the method or field. The syntax for accessing fields
 * and methods is out of the ordinary because this class uses reflection to
 * peel away protection.
 * 
 * <p>
 * Here is an example of using this to access a private member. <code>resolveName</code>
 * is a private method of <code>Class</code>.
 * 
 * <pre>
 *  Class c = Class.class; System.out.println( PrivilegedAccessor.invokeMethod(c, "resolveName", "/net/iss/common/PrivilegeAccessor"));
 * </pre>
 * 
 * @author <a href="mailto:chubbard@iss.net">Charlie Hubbard</a>
 * @author <a href="mailto:pdhokte@iss.net">Prashant Dhokte</a>
 */
public class PrivilegedAccessor
{
  /**
   * Gets the value of the named field and returns it as an object.
   * 
   * @param instance the object instance
   * @param fieldName the name of the field
   * @return an object representing the value of the field
   * @throws IllegalAccessException if the access to the class is refused
   * @throws NoSuchFieldException if the field name does not exist
   */
  public static Object getValue(Object instance, String fieldName)
    throws IllegalAccessException, NoSuchFieldException
  {
    Field field = getField(instance.getClass(), fieldName);
    field.setAccessible(true);
    return field.get(instance);
  }

  /**
   * Calls a method on the given object instance with the given argument.
   * 
   * @param instance the object instance
   * @param methodName the name of the method to invoke
   * @param arg the argument to pass to the method
   * @return an <code>Object</code> instance
   * @throws NoSuchMethodException when no such method exists
   * @throws IllegalAccessException if the access to the class is refused
   * @throws InvocationTargetException if the invocation failed
   * @see PrivilegedAccessor#invokeMethod(Object,String,Object[])
   */
  public static Object invokeMethod(
    Object instance,
    String methodName,
    Object arg)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
  {
    Object[] args = new Object[1];
    args[0] = arg;
    return invokeMethod(instance, methodName, args);
  }

  /**
   * Calls a method on the given object instance with the given arguments.
   * 
   * @param instance the object instance
   * @param methodName the name of the method to invoke
   * @param args an array of objects to pass as arguments
   * @return an <code>Object</code> instance
   * @throws NoSuchMethodException when no such method exists
   * @throws IllegalAccessException if the access to the class is refused
   * @throws InvocationTargetException if the invocation failed
   * @see PrivilegedAccessor#invokeMethod(Object,String,Object)
   */
  public static Object invokeMethod(
    Object instance,
    String methodName,
    Object[] args)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
  {
    Class[] classTypes = null;
    if (args != null)
    {
      classTypes = new Class[args.length];
      for (int i = 0; i < args.length; i++)
      {
        if (args[i] != null)
          classTypes[i] = args[i].getClass();
      }
    }
    return getMethod(instance, methodName, classTypes).invoke(instance, args);
  }

  /**
   * @param instance the object instance
   * @param methodName the method name
   * @param classTypes class types
   * @return a <code>Method</code> instance
   * @throws NoSuchMethodException when no such method exists
   */
  public static Method getMethod(
    Object instance,
    String methodName,
    Class[] classTypes)
    throws NoSuchMethodException
  {
    Method accessMethod =
      getMethod(instance.getClass(), methodName, classTypes);
    accessMethod.setAccessible(true);
    return accessMethod;
  }

  /**
   * Returns the named field from the given class.
   * 
   * @param thisClass class
   * @param fieldName field name
   * @return a <code>Field</code> instance
   * @throws NoSuchFieldException when no such field exists
   */
  private static Field getField(Class thisClass, String fieldName)
    throws NoSuchFieldException
  {
    if (thisClass == null)
      throw new NoSuchFieldException("Invalid field : " + fieldName);
    try
    {
      return thisClass.getDeclaredField(fieldName);
    }
    catch (NoSuchFieldException e)
    {
      return getField(thisClass.getSuperclass(), fieldName);
    }
  }

  /**
   * Returns the named method with a method signature matching classTypes from
   * the given class.
   * 
   * @param thisClass class
   * @param methodName method name
   * @param classTypes class types
   * @return a <code>Method</code> instance
   * @throws NoSuchMethodException when no such method exists
   */
  private static Method getMethod(
    Class thisClass,
    String methodName,
    Class[] classTypes)
    throws NoSuchMethodException
  {
    if (thisClass == null)
      throw new NoSuchMethodException("Invalid method : " + methodName);
    try
    {
      return thisClass.getDeclaredMethod(methodName, classTypes);
    }
    catch (NoSuchMethodException e)
    {
      return getMethod(thisClass.getSuperclass(), methodName, classTypes);
    }
  }
}
