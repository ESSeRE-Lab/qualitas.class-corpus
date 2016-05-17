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

package org.objectweb.cjdbc.console.wizard;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTabbedPane;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Node;
import org.objectweb.cjdbc.common.i18n.WizardTranslate;
import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.console.wizard.listeners.WizardListener;
import org.objectweb.cjdbc.console.wizard.objects.Backend;
import org.objectweb.cjdbc.console.wizard.objects.ConnectionInfo;
import org.objectweb.cjdbc.console.wizard.objects.ConnectionTypeInfo;
import org.objectweb.cjdbc.console.wizard.objects.User;
import org.objectweb.cjdbc.console.wizard.tab.AuthenticationTab;
import org.objectweb.cjdbc.console.wizard.tab.BackendTab;
import org.objectweb.cjdbc.console.wizard.tab.CachingTab;
import org.objectweb.cjdbc.console.wizard.tab.DistributionTab;
import org.objectweb.cjdbc.console.wizard.tab.RecoveryTab;
import org.objectweb.cjdbc.console.wizard.tab.RequestManagerTab;
import org.objectweb.cjdbc.console.wizard.tab.VirtualDatabaseTab;

/**
 * This is a <code>JTabbedPane</code> used to contain all the tabs define in
 * the <code>org.objectweb.cjdbc.console.wizard.tab</code> package.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public class WizardTabs extends JTabbedPane implements WizardListener
{

  private VirtualDatabaseTab virtualDatabaseTab;
  private DistributionTab    distributionTab;
  private AuthenticationTab  authenticationTab;
  private BackendTab         backendsTab;
  private RequestManagerTab  requestManagerTab;
  private ArrayList          tabs;
  private CachingTab         cachingTab;
  private RecoveryTab        recoveryTab;

  /**
   * Creates a new <code>WizardTabs</code> object
   */
  public WizardTabs()
  {
    this.setVisible(true);
    tabs = new ArrayList();

    virtualDatabaseTab = (VirtualDatabaseTab) this
        .addTab(new VirtualDatabaseTab(this));
    authenticationTab = (AuthenticationTab) this.addTab(new AuthenticationTab(
        this));
    backendsTab = (BackendTab) this.addTab(new BackendTab(this));
    requestManagerTab = (RequestManagerTab) this.addTab(new RequestManagerTab(
        this));

    distributionTab = new DistributionTab(this);
    cachingTab = new CachingTab(this);
    recoveryTab = new RecoveryTab(this);
    tabs.add(distributionTab);
  }

  /**
   * Add a tab
   * 
   * @param tab the tab to add
   * @return the updated tab
   */
  public WizardTab addTab(WizardTab tab)
  {
    this.addTab(tab.getName(), tab);
    tabs.add(tab);
    return tab;
  }

  /**
   * Set Tab Enabling.
   * 
   * @param tab the tab
   * @param enabled true if it myst be enabled
   */
  public void setTabEnabled(String tab, boolean enabled)
  {
    String translatedTab = WizardTranslate.get(tab);
    int index = 0;
    WizardTab componentTab;
    if (tab.equalsIgnoreCase(WizardConstants.TAB_DISTRIBUTION))
    {
      index = 1;
      componentTab = distributionTab;
    }
    else if (tab.equalsIgnoreCase(WizardConstants.TAB_CACHING))
    {
      index = this.getTabCount();
      componentTab = cachingTab;
    }
    else if (tab.equalsIgnoreCase(WizardConstants.TAB_RECOVERY))
    {
      index = this.getTabCount();
      componentTab = recoveryTab;
    }
    else
      return;

    if (enabled)
      this.insertTab(translatedTab, null, componentTab, translatedTab, index);
    else
      this.remove(componentTab);
  }

  /**
   * Return the users.
   * 
   * @return the users
   */
  public ArrayList getUsers()
  {
    return authenticationTab.getUsers();
  }

  /**
   * Return the backends.
   * 
   * @return the backends.
   */
  public ArrayList getBackends()
  {
    return backendsTab.getBackends();
  }

  /**
   * Return true if the database is distributed.
   * 
   * @return true if distributed database
   */
  public boolean isDistributedDatabase()
  {
    return virtualDatabaseTab.isDistributedDB();
  }

  /**
   * @see org.objectweb.cjdbc.console.wizard.listeners.WizardListener#backendListChanged()
   */
  public void backendListChanged()
  {
    for (int i = 0; i < tabs.size(); i++)
      ((WizardListener) tabs.get(i)).backendListChanged();
  }

  /**
   * @see org.objectweb.cjdbc.console.wizard.listeners.WizardListener#distributionChanged()
   */
  public void distributionChanged()
  {
    setTabEnabled(WizardConstants.TAB_DISTRIBUTION, isDistributedDatabase());

    for (int i = 0; i < tabs.size(); i++)
      ((WizardListener) tabs.get(i)).distributionChanged();
  }

  /**
   * @see org.objectweb.cjdbc.console.wizard.listeners.WizardListener#usersChanged()
   */
  public void usersChanged()
  {
    for (int i = 0; i < tabs.size(); i++)
      ((WizardListener) tabs.get(i)).usersChanged();
  }

  /**
   * Import a <code>Document</code> and set the gui so that all the fields are
   * filled with information from that document. This will reset all the values
   * already filled in.
   * 
   * @param doc a document parsed with the dom4j api.
   */
  public void importDocumentFromXml(Document doc)
  {
    // /////////////////////////////////////////////////////////////////////////
    // Virtual Database attributes
    // /////////////////////////////////////////////////////////////////////////
    Node virtualDabase = doc.selectSingleNode("//" + DatabasesXmlTags.ELT_CJDBC
        + "/" + DatabasesXmlTags.ELT_VirtualDatabase);
    virtualDatabaseTab.vdbName.setText(virtualDabase.valueOf("@"
        + DatabasesXmlTags.ATT_name));
    virtualDatabaseTab.maxNbOfConnections.setValue(Integer
        .parseInt(virtualDabase.valueOf("@"
            + DatabasesXmlTags.ATT_maxNbOfConnections)));

    String pool = virtualDabase.valueOf("@" + DatabasesXmlTags.ATT_poolThreads);
    if (!Boolean.valueOf(pool).booleanValue())
      virtualDatabaseTab.pool.doClick();
    virtualDatabaseTab.minNbOfThreads.setValue(Integer.parseInt(virtualDabase
        .valueOf("@" + DatabasesXmlTags.ATT_minNbOfThreads)));
    virtualDatabaseTab.maxNbOfThreads.setValue(Integer.parseInt(virtualDabase
        .valueOf("@" + DatabasesXmlTags.ATT_maxNbOfThreads)));
    virtualDatabaseTab.maxThreadIdleTime.setValue(Integer
        .parseInt(virtualDabase.valueOf("@"
            + DatabasesXmlTags.ATT_maxThreadIdleTime)));
    virtualDatabaseTab.sqlDumpLength.setValue(Integer.parseInt(virtualDabase
        .valueOf("@" + DatabasesXmlTags.ATT_sqlDumpLength)));
    virtualDatabaseTab.blob.setSelectedItem((virtualDabase.valueOf("@"
        + DatabasesXmlTags.ATT_blobEncodingMethod)));

    // /////////////////////////////////////////////////////////////////////////
    // Virtual Database distribution
    // /////////////////////////////////////////////////////////////////////////
    Node distribution = virtualDabase
        .selectSingleNode(DatabasesXmlTags.ELT_Distribution);
    if (distribution != null)
    {
      virtualDatabaseTab.distributed.doClick();
      distributionTab.groupName.setText(distribution.valueOf("@"
          + DatabasesXmlTags.ATT_groupName));
      distributionTab.macroClock.setSelectedItem(distribution.valueOf("@"
          + DatabasesXmlTags.ATT_macroClock));
      distributionTab.castTimeout.setValue(Integer.parseInt(distribution
          .valueOf("@" + DatabasesXmlTags.ATT_castTimeout)));
    }

    // /////////////////////////////////////////////////////////////////////////
    // authentication
    // /////////////////////////////////////////////////////////////////////////
    Node authentication = virtualDabase
        .selectSingleNode(DatabasesXmlTags.ELT_AuthenticationManager);
    Node userNode;
    User user;
    Iterator iter;

    List admins = authentication.selectNodes(DatabasesXmlTags.ELT_Admin + "/"
        + DatabasesXmlTags.ELT_User);
    iter = admins.iterator();
    while (iter.hasNext())
    {
      userNode = (Node) iter.next();
      user = new User();
      user.setUsername(userNode.valueOf("@" + DatabasesXmlTags.ATT_username));
      user.setPassword(userNode.valueOf("@" + DatabasesXmlTags.ATT_password));
      authenticationTab.admin.getUsersCombo().addItem(user);
    }

    List users = authentication.selectNodes(DatabasesXmlTags.ELT_VirtualUsers
        + "/" + DatabasesXmlTags.ELT_VirtualLogin);
    iter = users.iterator();
    while (iter.hasNext())
    {
      userNode = (Node) iter.next();
      user = new User();
      user.setUsername(userNode.valueOf("@" + DatabasesXmlTags.ATT_vLogin));
      user.setPassword(userNode.valueOf("@" + DatabasesXmlTags.ATT_vPassword));
      authenticationTab.users.getUsersCombo().addItem(user);
    }
    this.usersChanged();

    // /////////////////////////////////////////////////////////////////////////
    // backends
    // /////////////////////////////////////////////////////////////////////////
    List backends = virtualDabase
        .selectNodes(DatabasesXmlTags.ELT_DatabaseBackend);
    iter = backends.iterator();
    JComboBox backendsCombo = backendsTab.backendsCombo;
    Node backendNode;
    Node schemaNode;
    while (iter.hasNext())
    {
      backendNode = (Node) iter.next();
      schemaNode = backendNode
          .selectSingleNode(DatabasesXmlTags.ELT_DatabaseSchema);
      Backend backend = new Backend();
      backend.setName(backendNode.valueOf("@" + DatabasesXmlTags.ATT_name));
      backend.setDriver(backendNode.valueOf("@" + DatabasesXmlTags.ATT_driver));
      backend.setDriverPath(backendNode.valueOf("@"
          + DatabasesXmlTags.ATT_driverPath));
      backend.setUrl(backendNode.valueOf("@" + DatabasesXmlTags.ATT_url));
      backend.setConnectionTestStatement(backendNode.valueOf("@"
          + DatabasesXmlTags.ATT_connectionTestStatement));
      if (schemaNode != null)
      {
        backend.setGatherSystemTables(schemaNode.valueOf("@"
            + DatabasesXmlTags.ATT_gatherSystemTables));
        backend.setDynamicPrecision(schemaNode.valueOf("@"
            + DatabasesXmlTags.ATT_dynamicPrecision));
      }
      // /////////////////////////////////////////////////////////////////////////
      // Connection managers
      // /////////////////////////////////////////////////////////////////////////
      Hashtable managers = new Hashtable();
      backend.setConnectionManagers(managers);
      List connectionManagers = backendNode
          .selectNodes(DatabasesXmlTags.ELT_ConnectionManager);
      Iterator iterator = connectionManagers.iterator();
      while (iterator.hasNext())
      {
        Node connect = (Node) iterator.next();
        User user2 = getUser(connect.valueOf("@" + DatabasesXmlTags.ATT_vLogin));
        if (user2 != null)
        {
          ConnectionInfo info = new ConnectionInfo();
          info.setRLogin(connect.valueOf("@" + DatabasesXmlTags.ATT_rLogin));
          info.setRPassword(connect.valueOf("@"
              + DatabasesXmlTags.ATT_rPassword));
          info.setUrlParameters(connect.valueOf("@"
              + DatabasesXmlTags.ATT_urlParameters));
          ConnectionTypeInfo cti = new ConnectionTypeInfo();
          ArrayList values = new ArrayList();

          Node simple = connect
              .selectSingleNode(DatabasesXmlTags.ELT_SimpleConnectionManager);
          if (simple != null)
          {
            cti.setType(WizardConstants.CONNECTION_MANAGERS[0]);
          }
          Node failfast = connect
              .selectSingleNode(DatabasesXmlTags.ELT_FailFastPoolConnectionManager);
          if (failfast != null)
          {
            cti.setType(WizardConstants.CONNECTION_MANAGERS[1]);
            values.add(failfast.valueOf("@" + DatabasesXmlTags.ATT_poolSize));
          }
          Node random = connect
              .selectSingleNode(DatabasesXmlTags.ELT_RandomWaitPoolConnectionManager);
          if (random != null)
          {
            cti.setType(WizardConstants.CONNECTION_MANAGERS[2]);
            values.add(random.valueOf("@" + DatabasesXmlTags.ATT_poolSize));
            values.add(random.valueOf("@" + DatabasesXmlTags.ATT_timeout));
          }
          Node variable = connect
              .selectSingleNode(DatabasesXmlTags.ELT_VariablePoolConnectionManager);
          if (variable != null)
          {
            cti.setType(WizardConstants.CONNECTION_MANAGERS[3]);
            values.add(variable
                .valueOf("@" + DatabasesXmlTags.ATT_initPoolSize));
            values
                .add(variable.valueOf("@" + DatabasesXmlTags.ATT_minPoolSize));
            values
                .add(variable.valueOf("@" + DatabasesXmlTags.ATT_maxPoolSize));
            values
                .add(variable.valueOf("@" + DatabasesXmlTags.ATT_idleTimeout));
            values
                .add(variable.valueOf("@" + DatabasesXmlTags.ATT_waitTimeout));
          }

          cti.setValues(values);
          info.setConnectionTypeInfo(cti);
          managers.put(user2, info);
        }
      }
      backendsCombo.addItem(backend);
    }
    backendsCombo.setSelectedIndex(0);

    // /////////////////////////////////////////////////////////////////////////
    // Request manager
    // /////////////////////////////////////////////////////////////////////////
    Node requestManager = virtualDabase
        .selectSingleNode(DatabasesXmlTags.ELT_RequestManager);

    if (requestManager.valueOf("@" + DatabasesXmlTags.ATT_caseSensitiveParsing)
        .equals("true"))
      requestManagerTab.caseSensitiveParsing.doClick();
    requestManagerTab.beginTimeout.setValue(Integer.parseInt(requestManager
        .valueOf("@" + DatabasesXmlTags.ATT_beginTimeout)));
    requestManagerTab.commitTimeout.setValue(Integer.parseInt(requestManager
        .valueOf("@" + DatabasesXmlTags.ATT_commitTimeout)));
    requestManagerTab.rollbackTimeout.setValue(Integer.parseInt(requestManager
        .valueOf("@" + DatabasesXmlTags.ATT_rollbackTimeout)));

    // /////////////////////////////////////////////////////////////////////////
    // Scheduler
    // /////////////////////////////////////////////////////////////////////////
    Element scheduler = (Element) requestManager
        .selectSingleNode(DatabasesXmlTags.ELT_RequestScheduler);
    Element schedulerElement = (Element) scheduler.elements().iterator().next();
    requestManagerTab.scheduler.setSelectedItem(schedulerElement.getName());
    requestManagerTab.schedulerLevel.setSelectedItem(schedulerElement
        .valueOf("@" + DatabasesXmlTags.ATT_level));

    // /////////////////////////////////////////////////////////////////////////
    // Loadbalancer
    // /////////////////////////////////////////////////////////////////////////
    Element loadbalancer = (Element) requestManager
        .selectSingleNode(DatabasesXmlTags.ELT_LoadBalancer);
    Element loadElement = (Element) loadbalancer.elements().iterator().next();
    String raidbLoad = loadElement.getName();
    if (raidbLoad.startsWith(DatabasesXmlTags.ELT_RAIDb_1)
        || raidbLoad.startsWith(DatabasesXmlTags.ELT_RAIDb_2))
    {
      Element wait = (Element) loadElement
          .selectSingleNode(DatabasesXmlTags.ELT_WaitForCompletion);
      if (wait != null)
        requestManagerTab.wait4completion.setSelectedItem(wait.valueOf("@"
            + DatabasesXmlTags.ATT_policy));

      Iterator raidb = loadElement.elements().iterator();
      while (raidb.hasNext())
      {
        Element raid = (Element) raidb.next();
        String name = raid.getName();
        if (name.startsWith(DatabasesXmlTags.ELT_RAIDb_1)
            || name.startsWith(DatabasesXmlTags.ELT_RAIDb_2))
        {
          requestManagerTab.loadbalancer.setSelectedItem(name);
        }
      }
    }
    else
      requestManagerTab.loadbalancer.setSelectedItem(loadElement.getName());

    // /////////////////////////////////////////////////////////////////////////
    // Cache
    // /////////////////////////////////////////////////////////////////////////
    Element cache = (Element) requestManager
        .selectSingleNode(DatabasesXmlTags.ELT_RequestCache);
    if (cache != null)
    {
      requestManagerTab.usecaching.doClick();

      List cacheElements = cache.elements();
      iter = cacheElements.iterator();
      while (iter.hasNext())
      {
        Element celem = (Element) iter.next();
        if (celem.getName()
            .equalsIgnoreCase(DatabasesXmlTags.ELT_MetadataCache))
        {
          cachingTab.metadataenable.doClick();
          cachingTab.maxNbOfMetadata.setValue(Integer.parseInt(celem
              .valueOf("@" + DatabasesXmlTags.ATT_maxNbOfMetadata)));
          cachingTab.maxNbOfField.setValue(Integer.parseInt(celem.valueOf("@"
              + DatabasesXmlTags.ATT_maxNbOfField)));
        }
        else if (celem.getName().equalsIgnoreCase(
            DatabasesXmlTags.ELT_ParsingCache))
        {
          cachingTab.parsingenable.doClick();
          boolean background = Boolean.valueOf(
              celem.valueOf("@" + DatabasesXmlTags.ATT_backgroundParsing))
              .booleanValue();
          if (background)
            cachingTab.backgroundParsing.doClick();
          cachingTab.maxNbOfEntries.setValue(Integer.parseInt(celem.valueOf("@"
              + DatabasesXmlTags.ATT_maxNbOfEntries)));
        }
        else if (celem.getName().equalsIgnoreCase(
            DatabasesXmlTags.ELT_ResultCache))
        {
          cachingTab.resultenable.doClick();
          cachingTab.granularity.setSelectedItem(celem.valueOf("@"
              + DatabasesXmlTags.ATT_granularity));
          cachingTab.resultMaxNbOfEntries.setValue(Integer.parseInt(celem
              .valueOf("@" + DatabasesXmlTags.ATT_maxNbOfEntries)));
          cachingTab.pendingTimeout.setValue(Integer.parseInt(celem.valueOf("@"
              + DatabasesXmlTags.ATT_pendingTimeout)));
        }
      }
    }

    // /////////////////////////////////////////////////////////////////////////
    // Recovery
    // /////////////////////////////////////////////////////////////////////////
    Node recovery = requestManager
        .selectSingleNode(DatabasesXmlTags.ELT_RecoveryLog);
    if (recovery != null)
    {
      Node jdbcRecovery = recovery
          .selectSingleNode(DatabasesXmlTags.ELT_RecoveryLog);
      requestManagerTab.userecoverylog.doClick();
      recoveryTab.driver.setText(jdbcRecovery.valueOf("@"
          + DatabasesXmlTags.ATT_driver));
      recoveryTab.driverPath.setText(jdbcRecovery.valueOf("@"
          + DatabasesXmlTags.ATT_driverPath));
      recoveryTab.url.setText(jdbcRecovery.valueOf("@"
          + DatabasesXmlTags.ATT_url));
      recoveryTab.login.setText(jdbcRecovery.valueOf("@"
          + DatabasesXmlTags.ATT_login));
      recoveryTab.password.setText(jdbcRecovery.valueOf("@"
          + DatabasesXmlTags.ATT_password));
      recoveryTab.requestTimeout.setValue(Integer.parseInt(jdbcRecovery
          .valueOf("@" + DatabasesXmlTags.ATT_requestTimeout)));
    }

    this.validate();
    this.repaint();
  }

  private User getUser(String userName)
  {
    JComboBox box = authenticationTab.users.getUsersCombo();
    int count = box.getItemCount();
    for (int i = 0; i < count; i++)
    {
      User user = (User) box.getItemAt(i);
      if (user.getUsername().equals(userName))
        return user;
    }
    return null;
  }

  /**
   * This will export the content of all the wizard fields to a dom4j
   * <code>Document</code>
   * 
   * @return a dom4j Document
   */
  public Document exportDocumentToXml()
  {

    // DocumentType doctype = DocumentFactory.getInstance()
    // .createDocType(
    // DatabasesXmlTags.ELT_CJDBC,
    // "-//ObjectWeb//DTD C-JDBC " + Constants.VERSION + "//EN",
    // "http://c-jdbc.objectweb.org/dtds/c-jdbc-" + Constants.VERSION
    // + ".dtd");
    DocumentType doctype = DocumentFactory.getInstance().createDocType(
        DatabasesXmlTags.ELT_CJDBC, "-//ObjectWeb//DTD C-JDBC 1.1//EN",
        "http://c-jdbc.objectweb.org/dtds/c-jdbc-1.1.dtd");
    Document document = DocumentFactory.getInstance().createDocument();
    document.setDocType(doctype);
    Element root = document.addElement(DatabasesXmlTags.ELT_CJDBC);

    // /////////////////////////////////////////////////////////////////////////
    // Virtual Database attributes
    // /////////////////////////////////////////////////////////////////////////

    Element virtualDatabase = root
        .addElement(DatabasesXmlTags.ELT_VirtualDatabase);
    virtualDatabase.addAttribute(DatabasesXmlTags.ATT_name,
        virtualDatabaseTab.vdbName.getText());
    virtualDatabase.addAttribute(DatabasesXmlTags.ATT_maxNbOfConnections, ""
        + virtualDatabaseTab.maxNbOfConnections.getValue());
    virtualDatabase.addAttribute(DatabasesXmlTags.ATT_poolThreads, ""
        + (virtualDatabaseTab.pool.getSelectedObjects() != null));
    virtualDatabase.addAttribute(DatabasesXmlTags.ATT_minNbOfThreads, ""
        + virtualDatabaseTab.minNbOfThreads.getValue());
    virtualDatabase.addAttribute(DatabasesXmlTags.ATT_maxNbOfThreads, ""
        + virtualDatabaseTab.maxNbOfThreads.getValue());
    virtualDatabase.addAttribute(DatabasesXmlTags.ATT_maxThreadIdleTime, ""
        + virtualDatabaseTab.maxThreadIdleTime.getValue());
    virtualDatabase.addAttribute(DatabasesXmlTags.ATT_sqlDumpLength, ""
        + virtualDatabaseTab.sqlDumpLength.getValue());
    virtualDatabase.addAttribute(DatabasesXmlTags.ATT_blobEncodingMethod, ""
        + virtualDatabaseTab.blob.getSelectedItem());

    // /////////////////////////////////////////////////////////////////////////
    // Virtual Database distribution
    // /////////////////////////////////////////////////////////////////////////
    if (isDistributedDatabase())
    {
      Element distribution = virtualDatabase
          .addElement(DatabasesXmlTags.ELT_Distribution);
      distribution.addAttribute(DatabasesXmlTags.ATT_groupName,
          distributionTab.groupName.getText());
      distribution.addAttribute(DatabasesXmlTags.ATT_macroClock,
          distributionTab.macroClock.getSelectedItem().toString());
      distribution.addAttribute(DatabasesXmlTags.ATT_castTimeout, ""
          + distributionTab.castTimeout.getValue());
    }

    // /////////////////////////////////////////////////////////////////////////
    // authentication
    // /////////////////////////////////////////////////////////////////////////
    Element authentication = virtualDatabase
        .addElement(DatabasesXmlTags.ELT_AuthenticationManager);
    Element admin = authentication.addElement(DatabasesXmlTags.ELT_Admin);
    JComboBox adminBox = authenticationTab.admin.getUsersCombo();
    int count = adminBox.getItemCount();
    for (int i = 0; i < count; i++)
    {
      User user = (User) adminBox.getItemAt(i);
      admin.addElement(DatabasesXmlTags.ELT_User).addAttribute(
          DatabasesXmlTags.ATT_username, user.getUsername()).addAttribute(
          DatabasesXmlTags.ATT_password, user.getPassword());
    }

    Element users = authentication
        .addElement(DatabasesXmlTags.ELT_VirtualUsers);
    JComboBox virtualBox = authenticationTab.users.getUsersCombo();
    count = virtualBox.getItemCount();
    for (int i = 0; i < count; i++)
    {
      User user = (User) virtualBox.getItemAt(i);
      users.addElement(DatabasesXmlTags.ELT_VirtualLogin).addAttribute(
          DatabasesXmlTags.ATT_vLogin, user.getUsername()).addAttribute(
          DatabasesXmlTags.ATT_vPassword, user.getPassword());
    }

    // /////////////////////////////////////////////////////////////////////////
    // backends
    // /////////////////////////////////////////////////////////////////////////
    JComboBox backends = backendsTab.backendsCombo;
    int backcount = backends.getItemCount();
    for (int i = 0; i < backcount; i++)
    {
      Backend backend = (Backend) backends.getItemAt(i);
      Element backende = virtualDatabase
          .addElement(DatabasesXmlTags.ELT_DatabaseBackend);
      backende.addAttribute(DatabasesXmlTags.ATT_name, backend.getName());
      backende.addAttribute(DatabasesXmlTags.ATT_driver, backend.getDriver());
      backende.addAttribute(DatabasesXmlTags.ATT_driverPath, backend
          .getDriverPath());
      backende.addAttribute(DatabasesXmlTags.ATT_url, backend.getUrl());
      backende.addAttribute(DatabasesXmlTags.ATT_connectionTestStatement,
          backend.getConnectionTestStatement());

      Element schema = backende.addElement(DatabasesXmlTags.ELT_DatabaseSchema);
      schema.addAttribute(DatabasesXmlTags.ATT_dynamicPrecision, backend
          .getDynamicPrecision());
      if (Boolean.valueOf(backend.getGatherSystemTables()).booleanValue())
        schema.addAttribute(DatabasesXmlTags.ATT_gatherSystemTables, backend
            .getGatherSystemTables());

      // /////////////////////////////////////////////////////////////////////////
      // Connection managers
      // /////////////////////////////////////////////////////////////////////////
      Hashtable managers = backend.getConnectionManagers();
      Enumeration keys = managers.keys();
      while (keys.hasMoreElements())
      {
        User user = (User) keys.nextElement();
        ConnectionInfo info = (ConnectionInfo) managers.get(user);
        Element connection = backende
            .addElement(DatabasesXmlTags.ELT_ConnectionManager);
        connection
            .addAttribute(DatabasesXmlTags.ATT_vLogin, user.getUsername());
        connection.addAttribute(DatabasesXmlTags.ATT_rLogin, info.getRLogin());
        connection.addAttribute(DatabasesXmlTags.ATT_rPassword, info
            .getRPassword());
        connection.addAttribute(DatabasesXmlTags.ATT_urlParameters, info
            .getUrlParameters());
        ConnectionTypeInfo cti = info.getConnectionTypeInfo();

        String type = cti.getType();
        if (type == WizardConstants.CONNECTION_MANAGERS[0])
          connection.addElement(DatabasesXmlTags.ELT_SimpleConnectionManager);
        else if (type == WizardConstants.CONNECTION_MANAGERS[1])
        {
          Element manager = connection
              .addElement(DatabasesXmlTags.ELT_FailFastPoolConnectionManager);
          manager.addAttribute(DatabasesXmlTags.ATT_poolSize, ""
              + cti.getValue(0));
        }
        else if (type == WizardConstants.CONNECTION_MANAGERS[2])
        {
          Element manager = connection
              .addElement(DatabasesXmlTags.ELT_RandomWaitPoolConnectionManager);
          manager.addAttribute(DatabasesXmlTags.ATT_poolSize, ""
              + cti.getValue(0));
          manager.addAttribute(DatabasesXmlTags.ATT_timeout, ""
              + cti.getValue(1));
        }
        else if (type == WizardConstants.CONNECTION_MANAGERS[3])
        {
          Element manager = connection
              .addElement(DatabasesXmlTags.ELT_VariablePoolConnectionManager);
          manager.addAttribute(DatabasesXmlTags.ATT_initPoolSize, ""
              + cti.getValue(0));
          manager.addAttribute(DatabasesXmlTags.ATT_minPoolSize, ""
              + cti.getValue(1));
          manager.addAttribute(DatabasesXmlTags.ATT_maxPoolSize, ""
              + cti.getValue(2));
          manager.addAttribute(DatabasesXmlTags.ATT_idleTimeout, ""
              + cti.getValue(3));
          manager.addAttribute(DatabasesXmlTags.ATT_waitTimeout, ""
              + cti.getValue(4));
        }
      }
    }

    // /////////////////////////////////////////////////////////////////////////
    // Request manager
    // /////////////////////////////////////////////////////////////////////////
    Element requestManager = virtualDatabase
        .addElement(DatabasesXmlTags.ELT_RequestManager);

    requestManager.addAttribute(DatabasesXmlTags.ATT_caseSensitiveParsing,
        Boolean.toString(requestManagerTab.caseSensitiveParsing
            .getSelectedObjects() != null));
    requestManager.addAttribute(DatabasesXmlTags.ATT_beginTimeout,
        requestManagerTab.beginTimeout.getValue() + "");
    requestManager.addAttribute(DatabasesXmlTags.ATT_commitTimeout,
        requestManagerTab.commitTimeout.getValue() + "");
    requestManager.addAttribute(DatabasesXmlTags.ATT_rollbackTimeout,
        requestManagerTab.rollbackTimeout.getValue() + "");

    // /////////////////////////////////////////////////////////////////////////
    // Scheduler
    // /////////////////////////////////////////////////////////////////////////
    Element scheduler = requestManager
        .addElement(DatabasesXmlTags.ELT_RequestScheduler);
    int ischeduler = requestManagerTab.scheduler.getSelectedIndex();
    String level = (String) requestManagerTab.schedulerLevel.getSelectedItem();
    switch (ischeduler)
    {
      case 0 :
        scheduler.addElement(DatabasesXmlTags.ELT_SingleDBScheduler)
            .addAttribute(DatabasesXmlTags.ATT_level, level);
        break;
      case 1 :
        scheduler.addElement(DatabasesXmlTags.ELT_RAIDb0Scheduler)
            .addAttribute(DatabasesXmlTags.ATT_level, level);
        break;
      case 2 :
        scheduler.addElement(DatabasesXmlTags.ELT_RAIDb1Scheduler)
            .addAttribute(DatabasesXmlTags.ATT_level, level);
        break;
      case 3 :
        scheduler.addElement(DatabasesXmlTags.ELT_RAIDb2Scheduler)
            .addAttribute(DatabasesXmlTags.ATT_level, level);
        break;
      default :
        break;
    }

    // /////////////////////////////////////////////////////////////////////////
    // Cache
    // /////////////////////////////////////////////////////////////////////////
    if (requestManagerTab.usecaching.getSelectedObjects() != null)
    {
      Element cache = requestManager
          .addElement(DatabasesXmlTags.ELT_RequestCache);
      if (cachingTab.metadataenable.getSelectedObjects() != null)
      {
        Element metadata = cache.addElement(DatabasesXmlTags.ELT_MetadataCache);
        metadata.addAttribute(DatabasesXmlTags.ATT_maxNbOfMetadata, ""
            + cachingTab.maxNbOfMetadata.getValue());
        metadata.addAttribute(DatabasesXmlTags.ATT_maxNbOfField, ""
            + cachingTab.maxNbOfField.getValue());
      }
      if (cachingTab.parsingenable.getSelectedObjects() != null)
      {
        Element parsing = cache.addElement(DatabasesXmlTags.ELT_ParsingCache);
        parsing
            .addAttribute(DatabasesXmlTags.ATT_backgroundParsing,
                Boolean.toString(cachingTab.backgroundParsing
                    .getSelectedObjects() != null));
        parsing.addAttribute(DatabasesXmlTags.ATT_maxNbOfEntries, ""
            + cachingTab.maxNbOfEntries.getValue());
      }
      if (cachingTab.resultenable.getSelectedObjects() != null)
      {
        Element result = cache.addElement(DatabasesXmlTags.ELT_ResultCache);
        result.addAttribute(DatabasesXmlTags.ATT_granularity, ""
            + cachingTab.granularity.getSelectedItem());
        result.addAttribute(DatabasesXmlTags.ATT_maxNbOfEntries, ""
            + cachingTab.resultMaxNbOfEntries.getValue());
        result.addAttribute(DatabasesXmlTags.ATT_pendingTimeout, ""
            + cachingTab.pendingTimeout.getValue());
        result.addComment(WizardTranslate.get("not.supported.caching.rules"));
      }
    }

    // /////////////////////////////////////////////////////////////////////////
    // Loadbalancer
    // /////////////////////////////////////////////////////////////////////////
    Element loadbalancer = requestManager
        .addElement(DatabasesXmlTags.ELT_LoadBalancer);
    String loadElement = (String) requestManagerTab.loadbalancer
        .getSelectedItem();

    Element currentLoadbalancer = loadbalancer;

    if (loadElement.startsWith(DatabasesXmlTags.ELT_RAIDb_0))
      currentLoadbalancer = loadbalancer
          .addElement(DatabasesXmlTags.ELT_RAIDb_0);
    if (loadElement.startsWith(DatabasesXmlTags.ELT_RAIDb_1))
    {
      currentLoadbalancer = loadbalancer
          .addElement(DatabasesXmlTags.ELT_RAIDb_1);
      currentLoadbalancer.addElement(DatabasesXmlTags.ELT_WaitForCompletion)
          .addAttribute(DatabasesXmlTags.ATT_policy,
              (String) requestManagerTab.wait4completion.getSelectedItem());
    }
    if (loadElement.startsWith(DatabasesXmlTags.ELT_RAIDb_2))
    {
      currentLoadbalancer = loadbalancer
          .addElement(DatabasesXmlTags.ELT_RAIDb_2);
      currentLoadbalancer.addElement(DatabasesXmlTags.ELT_WaitForCompletion)
          .addAttribute(DatabasesXmlTags.ATT_policy,
              (String) requestManagerTab.wait4completion.getSelectedItem());
    }
    if (loadElement.startsWith(DatabasesXmlTags.ELT_ParallelDB))
      currentLoadbalancer = loadbalancer
          .addElement(DatabasesXmlTags.ELT_ParallelDB);

    currentLoadbalancer.addElement(loadElement);
    currentLoadbalancer.addComment(WizardTranslate
        .get("not.supported.macro.handling"));
    currentLoadbalancer.addComment(WizardTranslate
        .get("not.supported.create.policy"));

    // /////////////////////////////////////////////////////////////////////////
    // Recovery
    // /////////////////////////////////////////////////////////////////////////
    if (requestManagerTab.userecoverylog.getSelectedObjects() != null)
    {
      Element recovery = requestManager
          .addElement(DatabasesXmlTags.ELT_RecoveryLog);
      Element jdbc = recovery.addElement(DatabasesXmlTags.ELT_RecoveryLog);
      jdbc.addAttribute(DatabasesXmlTags.ATT_driver, recoveryTab.driver
          .getText());
      jdbc.addAttribute(DatabasesXmlTags.ATT_driverPath, recoveryTab.driverPath
          .getText());
      jdbc.addAttribute(DatabasesXmlTags.ATT_url, recoveryTab.url.getText());
      jdbc
          .addAttribute(DatabasesXmlTags.ATT_login, recoveryTab.login.getText());
      jdbc.addAttribute(DatabasesXmlTags.ATT_password, recoveryTab.password
          .getText());
      jdbc.addAttribute(DatabasesXmlTags.ATT_requestTimeout, ""
          + recoveryTab.requestTimeout.getValue());

      jdbc.addElement(DatabasesXmlTags.ELT_RecoveryLogTable);
      jdbc.addElement(DatabasesXmlTags.ELT_CheckpointTable);
      jdbc.addElement(DatabasesXmlTags.ELT_BackendTable);
    }

    return document;
  }
}