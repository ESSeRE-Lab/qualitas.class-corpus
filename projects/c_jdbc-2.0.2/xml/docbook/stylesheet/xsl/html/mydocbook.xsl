<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:doc="http://nwalsh.com/xsl/documentation/1.0"
                exclude-result-prefixes="doc"
                version='1.0'>

  <xsl:import href="docbook.xsl"/>  
  
  <!-- Customization -->
  <xsl:param name="make.valid.html" select="1"/>  

  <xsl:param name="navig.graphics" select="1"/>
  <xsl:param name="navig.graphics.extension" select="'.gif'"/>
  <xsl:param name="navig.graphics.path" select="'../../images/'"/>

  <xsl:param name="admon.graphics" select="1"/>
  <xsl:param name="admon.graphics.extension" select="'.gif'"/>
  <xsl:param name="admon.graphics.path" select="'../../images/'"/>

  <xsl:param name="callouts.graphics" select="1"/>
  <xsl:param name="callouts.graphics.extension" select="'.gif'"/>
  <xsl:param name="callouts.graphics.path" select="'../../images/'"/>

  <xsl:param name="section.autolabel" select="1"/>

  <xsl:param name="toc.section.depth">3</xsl:param>
</xsl:stylesheet>
