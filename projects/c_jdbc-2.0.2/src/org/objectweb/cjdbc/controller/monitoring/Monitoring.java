/*
 * Created on Dec 18, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.objectweb.cjdbc.controller.monitoring;

import org.objectweb.cjdbc.common.xml.DatabasesXmlTags;
import org.objectweb.cjdbc.common.xml.XmlComponent;

/**
 * @author niko
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class Monitoring implements XmlComponent
{
  boolean active;
  
  /**
   * Return all stats information in the form of a String
   * 
   * @return stats information
   */
  public abstract String[][] getAllStatsInformation();
  /**
   * Dump all stats using the current logger (INFO level).
   */
  public abstract void dumpAllStatsInformation();
  
  /**
   * Clean the content of statistics, to avoid memory problems.
   */
  public abstract void cleanStats();

  /**
   * @see org.objectweb.cjdbc.common.xml.XmlComponent#getXml()
   */
  public String getXml()
  {
    StringBuffer info = new StringBuffer();
    info.append("<"+DatabasesXmlTags.ELT_Monitoring+">");
    info.append(getXmlImpl());
    info.append("</"+DatabasesXmlTags.ELT_Monitoring+">");
    return info.toString();
  }
  
  /** Get implementation information */
  protected abstract String getXmlImpl();
  
  /**
   * Returns the active value.
   * 
   * @return Returns the active.
   */
  public boolean isActive()
  {
    return active;
  }
  /**
   * Sets the active value.
   * 
   * @param active The active to set.
   */
  public void setActive(boolean active)
  {
    this.active = active;
  }
}