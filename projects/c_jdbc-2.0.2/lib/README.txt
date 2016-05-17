This directory contains all external tools used by C-JDBC. Note also
that Ant 1.5 or greater (http://jakarta.apache.og/ant/) is required to
build C-JDBC.
Licenses of 3rd party software (hsqldb, iSQL, ...) can be found in the
doc/licenses/ directory.

C-JDBC core:

  - File(s): commons-cli.jar
    Project name: commons CLI
    Version: 1.0
    CVS tag:
    Web site: http://jakarta.apache.org/commons/cli/
    License: APL 2.0 (http://www.apache.org/licenses/LICENSE-2.0.txt)
    Description:  
      The CLI library provides a simple and easy to use API for working with 
      the command line arguments and options.
    Notes:

  - File(s): jgroups-core.jar
    Project name: JGroups
    Version: 2.2.8
    CVS tag:
    License: LGPL (http://www.jgroups.org/javagroupsnew/docs/license.html)
    Web site: http://www.jgroups.org/
    Description: 
      JGroups is a toolkit for reliable multicast communication.
    Notes: 
      
  - File(s): log4j.jar
    Project name: Log4j 
    Version: 1.2.8
    CVS tag:
    License: APL 2.0 (http://www.apache.org/licenses/LICENSE-2.0.txt)
    Web site: http://logging.apache.org/log4j
    Description: 
      Low overhead extensible logging framework with dynamic reconfiguration
      capabilities.
    Notes: 
      
  - File(s): jmx/mx4j.jar, jmx/mx4j-impl.jar, jmx/mx4j-jmx.jar, 
     jmx/mx4j-remote.jar, jmx/mx4j-rimpl.jar, jmx/mx4j-rjmx.jar, 
     jmx/mx4j-tools.jar, jmx/mx4j-tools.jar, jmx/xml-apis.jar, jmx/xsl/
    Project name: MX4J
    Version: 2.0.1
    CVS tag:
    License: MX4J license (http://mx4j.sourceforge.net/docs/ch01s06.html)
    Web site: http://mx4j.sourceforge.net/
    Description:
      MX4J is a project to build an Open Source implementation of the Java(TM)
      Management Extensions (JMX) and of the JMX Remote API (JSR 160)
      specifications, and to build tools relating to JMX.
    Notes: 
      MX4J license is an Apache-like license.
     
  - File(s): jmx/xalan.jar
    Project name: Xalan-Java
    CVS tag:
    Version: 2.4.1
    License: APL 2.0 (http://www.apache.org/licenses/LICENSE-2.0.txt)
    Web site: http://xml.apache.org/xalan-j/
    Description: 
      Xalan-Java is an XSLT processor for transforming XML documents into HTML,
      text, or other XML document types. 
    Notes: 

  - File(s): jdbc2_0-stdext.jar
    Project name: JDBC 2.0 Standard Extension
    Version: 
    CVS tag:
    License: Sun Microsystems, Inc. Binary Code License Agreement
    Web site: http://java.sun.com/products/jdbc/
    Description:
      javax.sql package from Sun. In general, you might want to download the 
      javax.sql package in addition to the Java 2 SDK, Standard Edition, if you
      do not write server-side code but want to use one of the following:
       * A DataSource object to make a connection
       * A RowSet object
      If your driver vendor bundles the javax.sql package with its product, as
      many are expected to do, you do not need to download anything beyond the
      Java 2 SDK, Standard Edition. 
      The javax.sql package is also called the JDBC 2.0 Optional Package API 
      (formerly known as the JDBC 2.0 Standard Extension API). 
    Notes: 
      This is needed for JDK 1.3 only. More information at 
      http://java.sun.com/products/jdbc/download.html

  - File(s): jakarta-regexp-1.4-dev.jar
    Project name: Jakarta Regexp
    Version: 1.4-dev
    CVS tag:
    License: APL 2.0 (http://www.apache.org/licenses/LICENSE-2.0.txt)
    Web site: http://jakarta.apache.org/regexp
    Description: 
      Jakarta Regexp is a 100% Pure Java Regular Expression package.
    Notes: 
      Uses 1.4-dev instead of 1.3 because it fixes platform dependencies but
      there are still some bug/inconsistencies remaining.

  - File(s): tribe.jar
    Project name: Tribe
    Version: 0.3
    CVS tag: tribe-0_3
    License: LGPL (http://tribe.objectweb.org/license.html)
    Web site: http://tribe.objectweb.org
    Description: 
      Tribe is a Java-based group communication library. It is based on 
      reliable point-to-point FIFO communication channels (basically TCP) and
      targets high performance cluster environments.
    Notes: 
      This version still requires JGroups to work with C-JDBC.


C-JDBC console:

  - File(s): jcommon-0.9.0.jar 
    Project name: JCommon
    Version: 0.9.0
    CVS tag:
    License: LGPL (http://www.jfree.org/lgpl.html)
    Web site: http://www.jfree.org/jcommon/
    Description: 
       JCommon is a collection of useful classes used by JFreeChart, 
       JFreeReport and other projects. 
    Notes: 
      
  - File(s): jfreechart.jar
    Project name: JFreeChart
    Version: 0.9.15
    CVS tag:
    License: LGPL (http://www.object-refinery.com/lgpl.html)
    Web site: http://www.jfree.org/jfreechart/
    Description: 
      JFreeChart is a free Java class library for generating charts, including:
       * pie charts (2D and 3D);
       * bar charts (regular and stacked, with an optional 3D effect);
       * line and area charts;
       * scatter plots and bubble charts;
       * time series, high/low/open/close charts and candle stick charts;
       * combination charts;
       * Pareto charts;
       * Gantt charts;
       * wind plots, meter charts and symbol charts;
       * wafer map charts.
    Notes: 
      
  - File(s): metouia.jar
    Project name: Metouia
    Version: 1.0 beta
    CVS tag:
    License: LGPL (http://mlf.sourceforge.net/index.php?license)
    Web site: http://mlf.sourceforge.net/index.php
    Description: 
      The metouia Look&Feel is a Java swing LAF
    Notes: 

   
   - File(s): jline.jar
    Project name: JLine
    Version: 0.9.1
    CVS tag:
    License: BSD (http://www.opensource.org/licenses/bsd-license.php)
    Web site: http://jline.sourceforge.net/
    Description: 
       JLine is a Java library for handling console input. It is similar in 
       functionality to BSD editline and GNU readline.It also supports 
       navigation keys.
    Notes: 
             
          
C-JDBC documentation:

   - File(s): docbook/docbook-dsssl-1.78.tar.gz
    Project name: DSSSL-DocBook stylesheet 
    Version: 1.78
    CVS tag:
    License: MIT/X Consortium (http://www.x.org/terms.htm)
    Web site: http://wiki.docbook.org/topic/DocBookDssslStylesheets
    Description: 
      The DocBook DSSSL stylesheets are a set of stylesheets for use with the 
      Jade/Openjade DSSSL engine for transforming DocBook SGML and XML 
      documents into other formats, such as HTML and PDF.
    Notes: 
      
   - File(s): docbook/docbook-xsl-1.65.1.tar.gz
    Project name: XSL-Docbook Stylesheet
    Version: 1.65.1
    CVS tag:
    License: MIT/X Consortium (http://www.x.org/terms.htm)
    Web site: http://wiki.docbook.org/topic/DocBookXslStylesheets
    Description: 
      The DocBook XSL stylesheets are a set of stylesheets for use with an XSLT
      engine (such as xsltproc or Saxon) for transforming DocBook XML documents
      into other DocBookOutputFormats, such as HTML, PDF, Microsoft HTML Help,
      and man pages.
    Notes: 
      

C-JDBC tests:

  - test/junit.jar
      JUnit 3.8.1
      http://www.junit.org/

  - test/mockobjects-core.jar
    test/mockobjects-jdk1.3.jar
    test/mockobjects-jdk1.4.jar
    test/mockobjects-jdk1.5.jar (this is just a copy of 1.4 as it does not exist yet)
      Mockobjects framework 0.09
      http://www.mockobjects.com/

  - test/mmmockobjects.jar
      MockMaker 1.11.0
      http://www.mockmaker.org/
      
  - test/dtdparser121.jar
      DTD Parser 1.2.1
      http://www.wutka.com/dtdparser.html

Octopus:

   - octopus/Octopus.jar
   Version 2.8
   The Octopus component for backup and restore of databases
   
   - octopus/OctopusGenerator.jar
   Version 2.8
   The Generator of configuration for Octopus
   
   - octopus/csvjdbc.jar
   Version 2.8
   CSV-JDBC driver
   
   - octopus/xmlutil.jar
   Version 2.8
   Xml Util package   
   
   - octopus/xercesImpl.jar
    Version 2.8
   Xml Parser
   
src: A set of sources libraries that do not contains binaries and class files

   - src/jfree-src.jar
    JFreeChart 0.9.15
    Display chart and graphs
    http://www.jfree.org/jfreechart/
    
   - src/jgroups-src.jar
    JGroups 2.2.8
    http://jgroups.org
   
   - src/mx4j-src.jar
    MX4J 2.0.1
    http://mx4j.sourceforge.net/

   - src/octopus-src.jar
    Version 2.8
    Contains both The Generator of configuration for Octopus and the octopus loader.
   
Others:

  - other/doccheck.jar
      Doc Check Utilities 1.2 Beta 1
      http://java.sun.com/j2se/javadoc/doccheck/

  - other/IzPack.3.7.0.tar.gz
      IzPack 3.7.0
      http://www.izforge.com/izpack/
      
   - other/ant_doxygen.jar
      Doxygen Ant Task v 1.4
    Source Code Documentation generator library used with ant.
    
   - other/jdepend.jar
      JDepend 2.6
    Provides code robustness information in different format.
    
        
