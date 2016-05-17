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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

 This is the XSL file for pretty printing of virtual database XML dumps.

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" indent="no"/>

<!--                    -->
<!-- Virtual Database   -->
<!--                    -->

<xsl:template match="VirtualDatabase">

### <i18n>xsl.header.virtualdatabase</i18n> ###

<i18n>xsl.name</i18n>: <xsl:value-of select="@name"/>
<i18n>xsl.max.connections</i18n>: <xsl:value-of select="@maxNbOfConnections"/>
<i18n>xsl.min.number.threads</i18n>: <xsl:value-of select="@minNbOfThreads"/>
<i18n>xsl.max.number.threads</i18n>: <xsl:value-of select="@maxNbOfThreads"/>
<i18n>xsl.max.thread.idle.time</i18n>: <xsl:value-of select="@maxThreadIdleTime"/>
<i18n>xsl.thread.pooling</i18n>: <xsl:value-of select="@poolThreads"/>
<xsl:apply-templates/>
</xsl:template>

<!--                -->
<!-- Distribution   -->
<!--                -->

<xsl:template match="Distribution">

### <i18n>xsl.header.distribution</i18n> ###
<i18n>xsl.group.name</i18n>: <xsl:value-of select="@groupName"/>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="DistributedBackendPolicy">
<i18n>xsl.policy.for.backend</i18n> <xsl:value-of select="@backendName"/>: <i18n>xsl.controller.is.writer</i18n>=<xsl:value-of select="@controllerIsWriter"/> <i18n>xsl.recover.on.failure</i18n>=<xsl:value-of select="@recoverOnFailure"/>
<xsl:apply-templates/>
</xsl:template>

<!--            -->
<!-- Monitoring	-->
<!--            -->

<xsl:template match="SQLMonitoring">

### <i18n>xsl.header.monitoring</i18n> ###
<i18n>xsl.default.monitoring</i18n>:<xsl:choose><xsl:when test="@defaultMonitoring = 'true'">on</xsl:when><xsl:when test="@defaultMonitoring = 'false'">off</xsl:when></xsl:choose>
<i18n>xsl.rule.list</i18n>:
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="SQLMonitoringRule">
<i18n>xsl.query.pattern</i18n>: <xsl:value-of select="@queryPattern"/>
<i18n>xsl.is.case.sensitive</i18n>: <xsl:value-of select="@isCaseSentive"/>
<i18n>xsl.apply.to.skeleton</i18n>: <xsl:value-of select="@applyToSkeleton"/>
<i18n>xsl.monitoring</i18n>: <xsl:value-of select="@monitoring"/>
<xsl:apply-templates/>
</xsl:template>

<!--                -->
<!-- Authentication -->
<!--                -->

<xsl:template match="AuthenticationManager">

### <i18n>xsl.header.authentication.manager</i18n> ###<xsl:apply-templates/>
</xsl:template>

<xsl:template match="User">
<i18n>xsl.admin.login</i18n>: <xsl:value-of select="@username"/> <i18n>xsl.password</i18n>: <xsl:value-of select="@password"/><xsl:apply-templates/>
</xsl:template>

<xsl:template match="VirtualLogin">
<i18n>xsl.virtual.login</i18n>: <xsl:value-of select="@vLogin"/> <i18n>xsl.password</i18n>: <xsl:value-of select="@vPassword"/><xsl:apply-templates/>
</xsl:template>

<!--                    -->
<!-- Connection Manager -->
<!--                    -->

<xsl:template match="ConnectionManager">

### <i18n>xsl.header.connection.manager</i18n> (<i18n>xsl.login</i18n>:<xsl:value-of select="@vLogin"/>,<i18n>xsl.login</i18n>: <xsl:value-of select="@rLogin"/> <i18n>xsl.password</i18n>: <xsl:value-of select="@rPassword"/>) ###<xsl:apply-templates/>
</xsl:template>

<xsl:template match="SimpleConnectionManager">
<i18n>xsl.simple.connection.manager</i18n>
</xsl:template>

<xsl:template match="RandomWaitPoolConnectionManager">
<i18n>xsl.random.wait.pool.connection.manager</i18n>
<i18n>xsl.pool.size</i18n>: <xsl:value-of select="@poolSize"/>
<i18n>xsl.time.out</i18n>: <xsl:value-of select="@timeOut"/>
</xsl:template>

<xsl:template match="FailFastPoolConnectionManager">
<i18n>xsl.fail.fast.pool.connection.manager</i18n>
<i18n>xsl.pool.size</i18n>: <xsl:value-of select="@poolSize"/>
</xsl:template>

<xsl:template match="VariablePoolConnectionManager">
<i18n>xsl.variable.pool.connection.manager</i18n>
<i18n>xsl.init.pool.size</i18n>: <xsl:value-of select="@initPoolSize"/>
<i18n>xsl.min.pool.size</i18n>: <xsl:value-of select="@minPoolSize"/>
<i18n>xsl.max.pool.size</i18n>: <xsl:value-of select="@maxPoolSize"/>
<i18n>xsl.idle.timeout</i18n>: <xsl:value-of select="@idleTimeout"/>
<i18n>xsl.wait.timeout</i18n>: <xsl:value-of select="@waitTimeout"/>
</xsl:template>

<!--                  -->
<!-- Database Backend -->
<!--                  -->

<xsl:template match="DatabaseBackend">

### <i18n>xsl.header.backend.information</i18n> ###
<i18n>xsl.name</i18n>: <xsl:value-of select="@name"/>
<i18n>xsl.driver.class</i18n>: <xsl:value-of select="@driver"/>
<i18n>xsl.url</i18n>:  <xsl:value-of select="@url"/>
<xsl:apply-templates/>
</xsl:template>

<!--                 -->
<!-- Database Schema -->
<!--                 -->

<xsl:template match="DatabaseSchema">
### <i18n>xsl.header.schema</i18n> ###<xsl:apply-templates/>
<i18n>xsl.dynamic.precision</i18n>: <xsl:value-of select="@dynamicPrecision"/>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="DatabaseStaticSchema">
# <i18n>xsl.header.schema.static</i18n> #<xsl:apply-templates/>
</xsl:template>

<xsl:template match="DatabaseProcedure">
^<i18n>xsl.procedure</i18n>: <xsl:value-of select="@name"/>
</xsl:template>

<xsl:template match="DatabaseTable">
*** <i18n>xsl.table</i18n>: <xsl:value-of select="@tableName"/> <i18n>xsl.number.columns</i18n>: <xsl:value-of select="@nbOfColumns"/><xsl:apply-templates/>
</xsl:template>

<xsl:template match="DatabaseColumn">
* <xsl:value-of select="@columnName"/> (<i18n>xsl.is.unique</i18n>:<xsl:value-of select="@isUnique"/>)<xsl:apply-templates/>
</xsl:template>

<!--                 -->
<!-- Request Manager -->
<!--                 -->

<xsl:template match="RequestManager">

### <i18n>xsl.header.request.manager</i18n> ###
<i18n>xsl.begin.timeout</i18n>:<xsl:value-of select="@beginTimeout"/>
<i18n>xsl.commit.timeout</i18n>:<xsl:value-of select="@commitTimeout"/>
<i18n>xsl.rollback.timeout</i18n>:<xsl:value-of select="@rollbackTimeout"/>
<i18n>xsl.background.parsing</i18n>: <xsl:value-of select="@backgroundParsing"/>
<i18n>xsl.case.sensitive.parsing</i18n>: <xsl:value-of select="@caseSensitiveParsing"/><xsl:apply-templates/>
</xsl:template>

<!--            -->
<!-- Schedulers -->
<!--            -->

<xsl:template match="RequestScheduler">

### <i18n>xsl.header.scheduler</i18n> ###<xsl:apply-templates/>
</xsl:template>


<xsl:template match="RAIDb0Scheduler">
<i18n>xsl.raidb0</i18n>:
<xsl:choose>
<xsl:when test="@level='pessimisticTransaction'"><i18n>xsl.with.pessimistic.transaction</i18n></xsl:when>
<xsl:when test="@level='query'"><i18n>xsl.with.query.level.transaction</i18n></xsl:when>
</xsl:choose>
</xsl:template>

<xsl:template match="RAIDb-1Scheduler">
<i18n>xsl.raidb1</i18n>:
<xsl:choose>
<xsl:when test="@level='pessimisticTransaction'"><i18n>xsl.with.pessimistic.transaction</i18n></xsl:when>
<xsl:when test="@level='query'"><i18n>xsl.with.query.level.transaction</i18n></xsl:when>
<xsl:when test="@level='optimisticQuery'"><i18n>xsl.with.optimistic.query.level</i18n></xsl:when>
<xsl:when test="@level = pessimisticTransaction"><i18n>xsl.with.pessimistic.transaction.level</i18n></xsl:when>
</xsl:choose>
</xsl:template>

<xsl:template match="RAIDb-2Scheduler">
<i18n>xsl.raidb2</i18n>:
<xsl:choose>
<xsl:when test="@level='pessimisticTransaction'"><i18n>xsl.with.pessimistic.transaction</i18n></xsl:when>
<xsl:when test="@level='query'"><i18n>xsl.with.query.level.transaction</i18n></xsl:when>
</xsl:choose>
</xsl:template>

<xsl:template match="SingleDBScheduler">
<i18n>xsl.singledb</i18n>:
<xsl:choose>
<xsl:when test="@level='pessimisticTransaction'"><i18n>xsl.with.pessimistic.transaction</i18n></xsl:when>
<xsl:when test="@level='query'"><i18n>xsl.with.query.level.transaction</i18n></xsl:when>
</xsl:choose>
</xsl:template>

<!--       -->
<!-- Cache -->
<!--       -->

<xsl:template match="RequestCache"><xsl:apply-templates/></xsl:template>

<xsl:template match="QueryCache">

### <i18n>xsl.header.querycache</i18n> ###
<i18n>xsl.pending.timeout</i18n>: <xsl:value-of select="@pendingTimeout"/>
<i18n>xsl.max.number.entries</i18n>: <xsl:value-of select="@maxNbOfEntries"/>
<i18n>xsl.granularity</i18n>: <xsl:value-of select="@granularity"/><xsl:apply-templates/>
</xsl:template>

<xsl:template match="CacheRule">
-><i18n>xsl.additional.cache.rule</i18n>
</xsl:template>

<xsl:template match="DefaultCacheRule">
-><i18n>xsl.default.rule</i18n> (<i18n>xsl.timestamp.resolution</i18n>: <xsl:value-of select="@timestampResolution"/>) <xsl:apply-templates/>
</xsl:template>

<!-- Cache Rules -->
<xsl:template match="RelaxedCaching">
*<i18n>xsl.relaxed.caching</i18n>: <i18n>xsl.timeout</i18n>: <xsl:value-of select="@timeout"/> <i18n>xsl.keep.if.not.dirty</i18n>: <xsl:value-of select="@keepIfNotDirty"/>
</xsl:template>
<xsl:template match="NoCaching">
*<i18n>xsl.no.caching</i18n>
</xsl:template>
<xsl:template match="EagerCaching">
*<i18n>xsl.eager.caching</i18n>
</xsl:template>

<!--                -->
<!-- Load balancers	-->
<!--                -->

<xsl:template match="LoadBalancer">

### <i18n>xsl.header.load.balancer</i18n> ### <xsl:apply-templates/>
</xsl:template>

	<!-- RAIDb level -->
	
	<!-- Single DB -->
<xsl:template match="SingleDB">
<i18n>xsl.singledb</i18n>
</xsl:template>
	<!-- RAIDb 0  -->
<xsl:template match="RAIDb-0">
<i18n>xsl.raidb0</i18n><xsl:apply-templates/>
</xsl:template>

	<!-- RAIDb 1 -->
<xsl:template match="RAIDb-1">
<i18n>xsl.raidb1</i18n>,<i18n>xsl.timestamp.resolution</i18n>:<xsl:value-of select="@timestampResolution"/><xsl:apply-templates/>
</xsl:template>

<xsl:template match="RAIDb-1-RoundRobin">
<i18n>xsl.load.balancing</i18n>:<i18n>xsl.round.robin</i18n>
</xsl:template>

<xsl:template match="RAIDb-1-WeightedRoundRobin">
<i18n>xsl.load.balancing</i18n>: <i18n>xsl.weighted.round.robin</i18n>
</xsl:template>

<xsl:template match="RAIDb-1-LeastPendingRequestsFirst">
<i18n>xsl.load.balancing</i18n>: <i18n>xsl.least.pending.request.first</i18n>
</xsl:template>

<xsl:template match="RAIDb-1ec">
<i18n>xsl.raidb1</i18n>,<i18n>xsl.error.checking</i18n>
</xsl:template>

<xsl:template match="RAIDb-1ec-RoundRobin">
<i18n>xsl.load.balancing</i18n>: <i18n>xsl.round.robin</i18n>
</xsl:template>

<xsl:template match="RAIDb-1ec-WeightedRoundRobin">
<i18n>xsl.load.balancing</i18n>: <i18n>xsl.weighted.round.robin</i18n>
</xsl:template>

	<!-- RAIDb 2 -->
<xsl:template match="RAIDb-2">
<i18n>xsl.raidb2</i18n>,<i18n>xsl.timestamp.resolution</i18n>:<xsl:value-of select="@timestampResolution"/>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="RAIDb-2-RoundRobin">
<i18n>xsl.load.balancing</i18n>: Round Robin
</xsl:template>

<xsl:template match="RAIDb-2-WeightedRoundRobin">
<i18n>xsl.load.balancing</i18n>: Weighted Round Robin
</xsl:template>

<xsl:template match="RAIDb-2-LeastPendingRequestsFirst">
<i18n>xsl.load.balancing</i18n>: Least Pending Requests First
</xsl:template>

<xsl:template match="RAIDb-2ec">
<i18n>xsl.raidb2</i18n>,<i18n>xsl.error.checking</i18n>
</xsl:template>

<xsl:template match="RAIDb-2ec-RoundRobin">
<i18n>xsl.load.balancing</i18n>: <i18n>xsl.round.robin</i18n>
</xsl:template>

<xsl:template match="RAIDb-2ec-WeightedRoundRobin">
<i18n>xsl.load.balancing</i18n>: <i18n>xsl.weighted.round.robin</i18n>
</xsl:template>

<!--          -->
<!-- Policies -->
<!--          -->

<xsl:template match="WaitForCompletion">
<i18n>xsl.wait.for.completion.policy</i18n>: <xsl:choose>
<xsl:when test="@policy='first'"><i18n>xsl.first</i18n></xsl:when>
<xsl:when test="@policy='majority'"><i18n>xsl.majority</i18n></xsl:when>
<xsl:when test="@policy='all'"><i18n>xsl.all</i18n></xsl:when>
</xsl:choose>
</xsl:template>

<xsl:template match="ErrorChecking">
<i18n>xsl.error.checking.policy</i18n>: <xsl:value-of select="@numberOfNodes"/> <xsl:choose>
<xsl:when test="@policy='random'"><i18n>xsl.random</i18n></xsl:when>
<xsl:when test="@policy='roundRobin'"><i18n>xsl.round.robin</i18n></xsl:when>
<xsl:when test="@policy='all'"><i18n>xsl.all</i18n></xsl:when>
</xsl:choose>
</xsl:template>

<xsl:template match="CreateTable">
<i18n>xsl.create.table.policy</i18n> (<i18n>xsl.table</i18n><xsl:value-of select="@tableName"/>): <xsl:value-of select="@numberOfNodes"/> <xsl:choose>
<xsl:when test="@policy='random'"><i18n>xsl.random</i18n></xsl:when>
<xsl:when test="@policy='roundRobin'"><i18n>xsl.round.robin</i18n></xsl:when>
<xsl:when test="@policy='all'"><i18n>xsl.all</i18n></xsl:when>
</xsl:choose>
</xsl:template>

<!--              -->
<!-- Recovery Log -->
<!--              -->
<xsl:template match="RecoveryLog">
### <i18n>xsl.header.recovery.log</i18n> ### <xsl:apply-templates/>
</xsl:template>

<xsl:template match="JDBCRecoveryLog">
<i18n>xsl.jdbc.recovery.with.options</i18n>:
<i18n>xsl.driver.class</i18n> <xsl:value-of select="@driver"/>
<i18n>xsl.url</i18n>: <xsl:value-of select="@url"/>
<i18n>xsl.login</i18n>: <xsl:value-of select="@login"/>
<i18n>xsl.password</i18n>: <xsl:value-of select="@password"/>
<i18n>xsl.timeout</i18n>: <xsl:value-of select="@requestTimeout"/>
</xsl:template>

</xsl:stylesheet>
