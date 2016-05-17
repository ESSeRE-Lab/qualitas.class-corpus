<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!-- book.xsl 
 | Merge DITA topics with "validation" of topic property
 *-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
<!-- Include error message template -->
<xsl:include href="common/output-message.xsl"/>

<xsl:param name="root-path"></xsl:param>

<!-- Set the prefix for error message numbers -->
<xsl:variable name="msgprefix">IDXS</xsl:variable>

<xsl:variable name="xml-path"></xsl:variable>

<xsl:output method="xml"
            encoding="utf-8"
/>

<xsl:template match="/*">
   <xsl:element name="{name()}">
     <xsl:apply-templates select="@*" mode="copy-element"/>
     <xsl:apply-templates select="*"/>
   </xsl:element>
</xsl:template>

<xsl:template match="/*/*[contains(@class,' map/topicmeta ')]" priority="1">
  <xsl:apply-templates select="." mode="copy-element"/>
</xsl:template>
<xsl:template match="*[contains(@class,' map/topicmeta ')]"/>
<xsl:template match="*[contains(@class,' map/navref ')]"/>
<xsl:template match="*[contains(@class,' map/reltable ')]"/>
<xsl:template match="*[contains(@class,' map/anchor ')]"/>

<xsl:template match="*[contains(@class,' map/topicref ')][@href][not(@href='')][not(@print='no')]">
  <xsl:variable name="topicrefClass"><xsl:value-of select="@class"/></xsl:variable>
  <xsl:comment>Start of imbed for <xsl:value-of select="@href"/></xsl:comment>
  <xsl:choose>
    <xsl:when test="@format and not(@format='dita')">
       <!-- Topicref to non-dita files will be ingored in PDF transformation -->
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">Topicref to non-dita files will be ignored in PDF transformation</xsl:with-param>
        <xsl:with-param name="msgnum">039</xsl:with-param>
        <xsl:with-param name="msgsev">I</xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="contains(@href,'#')">
      <xsl:variable name="sourcefile"><xsl:value-of select="substring-before(@href,'#')"/></xsl:variable>
      <xsl:variable name="sourcetopic"><xsl:value-of select="substring-after(@href,'#')"/></xsl:variable>
      <xsl:variable name="targetName"><xsl:value-of select="name(document($sourcefile,/)//*[@id=$sourcetopic][contains(@class,' topic/topic ')][1])"/></xsl:variable>
      <xsl:element name="{$targetName}">
        <xsl:apply-templates select="document($sourcefile,/)//*[@id=$sourcetopic][contains(@class,' topic/topic ')][1]/@*" mode="copy-element"/>
        <xsl:attribute name="refclass"><xsl:value-of select="$topicrefClass"/></xsl:attribute>
        <xsl:apply-templates select="document($sourcefile,/)//*[@id=$sourcetopic][contains(@class,' topic/topic ')][1]/*" mode="copy-element">
          <xsl:with-param name="src-file"><xsl:value-of select="$sourcefile"/></xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:when>
    <!-- If the target is a topic, as opposed to a ditabase mixed file -->
    <xsl:when test="document(@href,/)/*[contains(@class,' topic/topic ')]">
      <xsl:variable name="targetName"><xsl:value-of select="name(document(@href,/)/*)"/></xsl:variable>
      <xsl:element name="{$targetName}">
        <xsl:apply-templates select="document(@href,/)/*/@*" mode="copy-element"/>
        <xsl:attribute name="refclass"><xsl:value-of select="$topicrefClass"/></xsl:attribute>
        <!-- If the root element of the topic does not contain an id attribute, then generate one.
             Later, we will use these id attributes as anchors for PDF bookmarks. -->
        <xsl:if test="not(document(@href,/)/*/@id)">
          <xsl:attribute name="id"><xsl:value-of select="generate-id()"/></xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="document(@href,/)/*/*" mode="copy-element">
          <xsl:with-param name="src-file"><xsl:value-of select="@href"/></xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:when>
    <!-- Otherwise: pointing to ditabase container; output each topic in the ditabase file.
         The refclass value is copied to each of the main topics.
         If this topicref has children, they will be treated as children of the <dita> wrapper.
         This is the same as saving them as peers of the topics in the ditabase file. -->
    <xsl:otherwise>
      <xsl:for-each select="document(@href,/)/*/*">
        <xsl:element name="{name()}">
          <xsl:apply-templates select="@*" mode="copy-element"/>
          <xsl:attribute name="refclass"><xsl:value-of select="$topicrefClass"/></xsl:attribute>
          <xsl:apply-templates select="*" mode="copy-element"/>
        </xsl:element>
      </xsl:for-each>
      <xsl:apply-templates/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ')][not(@href)]">
  <xsl:element name="{name()}">
    <xsl:apply-templates select="@*" mode="copy-element"/>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template match="*|@*|comment()|processing-instruction()|text()" mode="copy-element">
<xsl:param name="src-file"></xsl:param>
  <xsl:copy>
    <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="copy-element">
      <xsl:with-param name="src-file"><xsl:value-of select="$src-file"/></xsl:with-param>
    </xsl:apply-templates>
  </xsl:copy>
</xsl:template>

<xsl:template match="@href" mode="copy-element" priority="1">
  <xsl:param name="src-file"></xsl:param>

  <xsl:variable name="file-path">  
    <xsl:call-template name="get-file-path">
      <xsl:with-param name="src-file">
        <xsl:value-of select="$src-file"/>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:value-of select="."/>
  </xsl:variable>

  <xsl:variable name="file-path-new">
    <xsl:call-template name="normalize-path">
      <xsl:with-param name="file-path">
        <xsl:value-of select="$file-path"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>  

  <xsl:choose>
    <xsl:when test="contains(.,'://') or starts-with(.,'#')">
      <xsl:copy/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:attribute name="href">
        <xsl:value-of select="$file-path-new"/>
      </xsl:attribute>
    </xsl:otherwise>
  </xsl:choose>  
</xsl:template>

<xsl:template name="get-file-path">
  <xsl:param name="src-file"/>  
  <xsl:if test="contains($src-file,'/')">
    <xsl:value-of select="substring-before($src-file,'/')"/>
    <xsl:text>/</xsl:text>
    <xsl:call-template name="get-file-path">
      <xsl:with-param name="src-file">
        <xsl:value-of select="substring-after($src-file,'/')"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:if>  
</xsl:template>

<xsl:template name="normalize-path">
  <xsl:param name="file-path"></xsl:param>  

  <xsl:choose>
    <xsl:when test="contains($file-path, '/')">
      <xsl:variable name="dirname" select="substring-before($file-path,'/')"/>
      <xsl:variable name="file-path-new" select="substring-after($file-path,'/')"/>
      <xsl:variable name="dirname2" select="substring-before($file-path-new,'/')"/>
      <xsl:variable name="file-path-new2" select="substring-after($file-path-new,'/')"/>

      <xsl:choose>
        <xsl:when test="$dirname2='..'">          
          <xsl:choose>
            <xsl:when test="$dirname='..'">
              <xsl:text>../../</xsl:text>
              <xsl:call-template name="normalize-path">
                <xsl:with-param name="file-path">
                  <xsl:value-of select="$file-path-new2"/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="normalize-path">
                <xsl:with-param name="file-path">
                  <xsl:value-of select="$file-path-new2"/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$dirname"/><xsl:text>/</xsl:text>
          <xsl:call-template name="normalize-path">
            <xsl:with-param name="file-path">
              <xsl:value-of select="$file-path-new"/>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:when test="$file-path">
      <xsl:value-of select="$file-path"/>
    </xsl:when>
    <xsl:otherwise></xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
