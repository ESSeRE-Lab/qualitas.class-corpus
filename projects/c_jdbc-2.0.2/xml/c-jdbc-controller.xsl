<?xml version="1.0"?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" indent="no"/>
  
<xsl:template match="C-JDBC">
### <i18n>xsl.header.cjdbc.controller</i18n> (<i18n>xsl.version</i18n> <xsl:value-of select="@version"/>) ###
<xsl:apply-templates/>
</xsl:template>

<!--			-->
<!-- Controller         -->
<!--			-->

<xsl:template match="Controller">
<i18n>xsl.controller.name</i18n>: <xsl:value-of select="@name"/>

*** <i18n>xsl.network.settings</i18n> ***
<i18n>xsl.ip.address</i18n>: <xsl:value-of select="@ipAddress"/>
<i18n>xsl.port</i18n>: <xsl:value-of select="@port"/>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="Internationalization">
*** <i18n>xsl.locale</i18n> ***
<i18n>xsl.language</i18n>: <xsl:value-of select="@language"/><xsl:apply-templates/>
</xsl:template>

<xsl:template match="JmxSettings">
*** <i18n>xsl.jmx.settings</i18n> ***
<i18n>xsl.jmx.enabled</i18n>: <i18n>xsl.true</i18n><xsl:apply-templates/>
</xsl:template>

<!--			-->
<!-- Security Settings  -->
<!--			-->

<xsl:template match="SecuritySettings">
*** <i18n>xsl.security.settings</i18n> ***
<i18n>xsl.security.enabled</i18n>: <i18n>xsl.true</i18n>
<i18n>xsl.default.connect</i18n>:<xsl:value-of select="@defaultConnect"/><xsl:apply-templates/>
</xsl:template>

<xsl:template match="Jar">
<i18n>xsl.allow.more.drivers</i18n>: <xsl:value-of select="@allowAdditionalDriver"/><xsl:apply-templates/>
</xsl:template>

<xsl:template match="Shutdown">
*** <i18n>xsl.shutdown.options</i18n> ***<xsl:apply-templates/>
</xsl:template>

<xsl:template match="Console">
<i18n>xsl.allow.console.shutdown</i18n>: <xsl:value-of select="@allow"/>
<i18n>xsl.local.console.only</i18n>: <xsl:value-of select="@onlyLocalhost"/>
</xsl:template>

<xsl:template match="Client">
<i18n>xsl.allow.client.shutdown</i18n>: <xsl:value-of select="@allow"/>
<i18n>xsl.local.client.only</i18n>: <xsl:value-of select="@onlyLocalhost"/>
</xsl:template>

<xsl:template match="Accept">
 *** <i18n>xsl.accepted.hosts</i18n> ***
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="Block">
 *** <i18n>xsl.blocked.hosts</i18n> ***
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="Iprange"><i18n>xsl.ip.range</i18n>: <xsl:value-of select="@value"/></xsl:template>
<xsl:template match="Hostname"><i18n>xsl.hostname</i18n>: <xsl:value-of select="@value"/></xsl:template>
<xsl:template match="Ipaddress"><i18n>xsl.ip.address</i18n>: <xsl:value-of select="@value"/></xsl:template>

</xsl:stylesheet>
