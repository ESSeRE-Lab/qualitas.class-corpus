<!--
/*
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
 * Initial developer(s): Nicolas Modrzyk
 * Contributor(s): 
 */
-->

<!-- 
  Format and indent xml, we could use indent=true 
  but it does not work with out parser.
-->
<xsl:stylesheet version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" />
  <xsl:param name="indent-increment" select="'   '" />
  
  <xsl:template match="*">
     <xsl:param name="indent" select="'&#xA;'"/>
  
     <xsl:value-of select="$indent"/>
     <xsl:copy>
       <xsl:copy-of select="@*" />
       <xsl:apply-templates>
         <xsl:with-param name="indent"
              select="concat($indent, $indent-increment)"/>
       </xsl:apply-templates>
       <xsl:if test="*">
       	<xsl:value-of select="$indent"/>
       </xsl:if>
     </xsl:copy>
  </xsl:template>
  
  <xsl:template match="comment()|processing-instruction()">
     <xsl:copy />
  </xsl:template>
  
  <!-- WARNING: this is dangerous. Handle with care -->
  <xsl:template match="text()[normalize-space(.)='']"/>
  
</xsl:stylesheet>

