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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): ______________________.
 */

package org.objectweb.cjdbc.scenario.tools.testlet.xml;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.objectweb.cjdbc.scenario.templates.Template;
import org.objectweb.cjdbc.scenario.tools.components.ComponentInterface;
import org.objectweb.cjdbc.scenario.tools.components.backend.DatabaseManager;
import org.objectweb.cjdbc.scenario.tools.components.controller.ControllerManager;
import org.objectweb.cjdbc.scenario.tools.testlet.AbstractConnectionTestLet;

/**
 * This class defines a XmlBasedRegressionTesting
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class XmlBasedRegressionTesting extends TestCase
{

  private Document          doc;
  private DatabaseManager   backendManager;
  private ControllerManager controllerManager;
  private String            scenarioFileName;

  /**
   * 
   * Creates a new <code>XmlBasedRegressionTesting</code> object
   * 
   *
   */
  public XmlBasedRegressionTesting()
  {
    URL url = Class.class.getResource("/xml/scenario.xml");
    if(url==null)
      throw new RuntimeException("Cannot find xml file");
    scenarioFileName = url.getFile();
  }

  /**
   * 
   * @see junit.framework.TestCase#setUp()
   */
  public void setUp()
  {
    try
    {
      SAXReader xmlReader = new SAXReader();
      this.doc = xmlReader.read(scenarioFileName);

      backendManager = new DatabaseManager();
      controllerManager = new ControllerManager();
      List database = doc.selectNodes("//scenario/configuration/database");
      for (Iterator i = database.iterator(); i.hasNext();)
      {
        Element data = (Element) i.next();
        System.out.println("Starting Backend:" + data.valueOf("@port") + ":"
            + data.valueOf("@data"));
        ComponentInterface hsqldb = backendManager.instanciateProcess(data
            .valueOf("@port"));
        System.out.println("Echo port:" + hsqldb.getPort() + " is started:"
            + backendManager.isStarted(data.valueOf("@port")));
        hsqldb.loadDatabase(data.valueOf("@data"));
      }

      List controllers = doc.selectNodes("//scenario/configuration/controller");
      for (Iterator i = controllers.iterator(); i.hasNext();)
      {
        // Starting controller
        Element data = (Element) i.next();
        System.out.println("Starting Controller:" + data.valueOf("@port"));
        ComponentInterface controller = controllerManager
            .instanciateProcess(data.valueOf("@port"));

        // Loading virtual databases
        Iterator iter = data.elementIterator();
        while (iter.hasNext())
        {
          controller.loadDatabase(((Node) iter.next()).valueOf("@data"));
        }
      }
    }
    catch (Exception e)
    {
      fail(e.getMessage());
    }

  }

  /**
   * 
   * Run main test
   * 
   * @throws Exception if fails
   */
  public void testMe() throws Exception
  {
    List lets = doc.selectNodes("//scenario/testlet");
    for (Iterator i = lets.iterator(); i.hasNext();)
    {
      Element let = (Element) i.next();
      String test = "org.objectweb.cjdbc.scenario.tools.testlet."
          + let.valueOf("@class");
      System.out.println("Starting test:" + test);
      Constructor[] constructors = Class.forName(test).getConstructors();
      Constructor constructor = null;
      System.out.println(constructors.length);
      for (int j = 0; j < constructors.length; j++)
      {
        constructor = constructors[j];
        Class[] klasses = constructor.getParameterTypes();
        if (klasses.length == 0)
          break;
      }
      System.out.println("Constructor is:" + constructor.getName());
      AbstractConnectionTestLet testLet = (AbstractConnectionTestLet) constructor
          .newInstance(new Object[]{Template.getCJDBCConnection()});
      testLet.execute();
      TestResult result = new TestResult();
      testLet.run(result);
      System.out.println("Failures" + result.failureCount());
      Enumeration enumaration = result.errors();
      while (enumaration.hasMoreElements())
      {
        System.out.println(enumaration.nextElement());
      }
      enumaration = result.failures();
      while (enumaration.hasMoreElements())
      {
        TestFailure elem = (TestFailure) enumaration.nextElement();
        elem.thrownException().printStackTrace();
        System.out.println(elem.trace());
        //System.out.println();
        System.out.println(elem);
      }
      System.out.println("Test was successful:" + result.wasSuccessful());
    }
  }

  /**
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  public void tearDown()
  {
    controllerManager.stopAll();
    backendManager.stopAll();
  }
}