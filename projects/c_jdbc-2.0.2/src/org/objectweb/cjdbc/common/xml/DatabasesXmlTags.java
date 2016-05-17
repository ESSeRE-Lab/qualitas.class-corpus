/**
 * C-JDBC: Clustered JDBC.
 * Copyright (C) 2002-2005 French National Institute For Research In Computer
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
 * Initial developer(s): Emmanuel Cecchet. 
 * Contributor(s): Mathieu Peltier, Nicolas Modrzyk.
 */

package org.objectweb.cjdbc.common.xml;

/**
 * List of the xml tags recognized to read and write the databasases
 * configuration with
 * 
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @author <a href="mailto:Mathieu.Peltier@inrialpes.fr">Mathieu Peltier </a>
 * @author <a href="mailto:Nicolas.Modrzyk@inrialpes.fr">Nicolas Modrzyk </a>
 * @version 1.0
 */
public final class DatabasesXmlTags
{
  /*
   * All purpose variables
   */

  /** Value of "true" */
  public static final String VAL_true                                 = "true";
  /** Value of "false" */
  public static final String VAL_false                                = "false";
  /** Value of "true" */
  public static final String VAL_on                                   = "on";
  /** Value of "false" */
  public static final String VAL_off                                  = "off";

  /*
   * Virtual Database
   */

  /** Name of a <code>VirtualDatabase</code> object element. */
  public static final String ELT_CJDBC                                = "C-JDBC";

  /** Name of a <code>VirtualDatabase</code> object element. */
  public static final String ELT_VirtualDatabase                      = "VirtualDatabase";

  /**
   * <code>name</code> attribute in <code>VirtualDatabase</code>,
   * <code>ControllerName</code>,<code>BackendWeight</code>,
   * <code>BackendName</code> and <code>DatabaseBackend</code>.
   */
  public static final String ATT_name                                 = "name";

  /** <code>maxNbOfConnections</code> attribute in <code>VirtualDatabase</code>. */
  public static final String ATT_maxNbOfConnections                   = "maxNbOfConnections";

  /** <code>poolThreads</code> attribute in <code>VirtualDatabase</code>. */
  public static final String ATT_poolThreads                          = "poolThreads";

  /** <code>minNbOfThreads</code> attribute in <code>VirtualDatabase</code>. */
  public static final String ATT_minNbOfThreads                       = "minNbOfThreads";

  /** <code>maxNbOfThreads</code> attribute in <code>VirtualDatabase</code>. */
  public static final String ATT_maxNbOfThreads                       = "maxNbOfThreads";

  /** <code>maxThreadIdleTime</code> attribute in <code>VirtualDatabase</code>. */
  public static final String ATT_maxThreadIdleTime                    = "maxThreadIdleTime";

  /** <code>sqlDumpLength</code> attribute in <code>VirtualDatabase</code>. */
  public static final String ATT_sqlDumpLength                        = "sqlDumpLength";

  /** <code>blobEncodingMethod</code> attribute in <code>VirtualDatabase</code>. */
  public static final String ATT_blobEncodingMethod                   = "blobEncodingMethod";

  /**
   * <code>none</code> value of the <code>blobEncodingMethod</code>
   * attribute.
   */
  public static final String VAL_none                                 = "none";

  /**
   * <code>hexa</code> value of the <code>blobEncodingMethod</code>
   * attribute.
   */
  public static final String VAL_hexa                                 = "hexa";

  /**
   * <code>base64</code> value of the <code>blobEncodingMethod</code>
   * attribute.
   */
  public static final String VAL_base64                               = "base64";

  /**
   * <code>escaped</code> value of the <code>blobEncodingMethod</code>
   * attribute.
   */
  public static final String VAL_escaped                              = "escaped";

  /*
   * Distribution
   */

  /** Name of a <code>Distribution</code> object element. */
  public static final String ELT_Distribution                         = "Distribution";

  /** <code>groupName</code> attribute in <code>Distribution</code>. */
  public static final String ATT_groupName                            = "groupName";

  /** <code>macroClock</code> attribute in <code>Distribution</code>. */
  public static final String ATT_macroClock                           = "macroClock";

  /** <code>castTimeout</code> attribute in <code>Distribution</code>. */
  public static final String ATT_castTimeout                          = "castTimeout";

  /*
   * Monitoring
   */

  /** Name of a <code>Monitoring</code> object element. */
  public static final String ELT_Monitoring                           = "Monitoring";

  /** Name of a <code>SQLMonitoring</code> object element. */
  public static final String ELT_SQLMonitoring                        = "SQLMonitoring";

  /** <code>defaultMonitoring</code> attribute in <code>SQLMonitoring</code>. */
  public static final String ATT_defaultMonitoring                    = "defaultMonitoring";

  /** Name of a <code>SQLMonitoringRule</code> object element. */
  public static final String ELT_SQLMonitoringRule                    = "SQLMonitoringRule";

  /**
   * <code>queryPattern</code> attribute in <code>ResultCacheRule</code>,
   * <code>RewritingRule</code> or <code>SQLMonitoringRule</code> elements
   */
  public static final String ATT_queryPattern                         = "queryPattern";

  /**
   * <code>caseSensitive</code> attribute in <code>ResultCacheRule</code>,
   * <code>RewritingRule</code> or <code>SQLMonitoringRule</code> elements
   */
  public static final String ATT_caseSensitive                        = "caseSensitive";

  /** <code>applyToSkeleton</code> attribute in <code>SQLMonitoringRule</code>. */
  public static final String ATT_applyToSkeleton                      = "applyToSkeleton";

  /** <code>monitoring</code> attribute in <code>SQLMonitoringRule</code>. */
  public static final String ATT_monitoring                           = "monitoring";

  /*
   * Backup
   */

  /** Name of a <code>Backup</code> object element. */
  public static final String ELT_Backup                               = "Backup";

  /** Name of a <code>Backuper</code> object element. */
  public static final String ELT_Backuper                             = "Backuper";

  /** <code>backuperName</code> attribute in <code>Backuper</code>. */
  public static final String ATT_backuperName                         = "backuperName";

  /** <code>className</code> attribute in <code>Backuper</code>. */
  public static final String ATT_className                            = "className";

  /** <code>options</code> attribute in <code>Backuper</code>. */
  public static final String ATT_options                              = "options";

  /*
   * Database Backend
   */

  /** Name of a <code>DatabaseBackend</code> object element. */
  public static final String ELT_DatabaseBackend                      = "DatabaseBackend";

  /**
   * <code>driverPath</code> attribute in <code>DatabaseBackend</code> and
   * <code>RecoveryLog</code>.
   */
  public static final String ATT_driverPath                           = "driverPath";

  /**
   * <code>driver</code> attribute in <code>DatabaseBackend</code> and
   * <code>RecoveryLog</code>.
   */
  public static final String ATT_driver                               = "driver";

  /**
   * <code>url</code> attribute in <code>DatabaseBackend</code> and
   * <code>RecoveryLog</code>.
   */
  public static final String ATT_url                                  = "url";

  /**
   * <code>connectionTestStatement</code> attribute in
   * <code>DatabaseBackend</code>
   */
  public static final String ATT_connectionTestStatement              = "connectionTestStatement";

  /** <code>writeEnabled</code> attribute in <code>DatabaseBackend</code> */
  public static final String ATT_writeEnabled                         = "writeEnabled";

  /*
   * Rewriting Rule
   */

  /** Name of a <code>RewritingRule</code> object element. */
  public static final String ELT_RewritingRule                        = "RewritingRule";

  /** <code>rewrite</code> attribute in <code>RewritingRule</code>. */
  public static final String ATT_rewrite                              = "rewrite";

  /** <code>matchingType</code> attribute in <code>RewritingRule</code>. */
  public static final String ATT_matchingType                         = "matchingType";

  /**
   * Value of "simple" for <code>matchingType</code> in a
   * <code>RewritingRule</code>.
   */
  public static final String VAL_simple                               = "simple";

  /**
   * Value of "pattern" for <code>matchingType</code> in a
   * <code>RewritingRule</code>.
   */
  public static final String VAL_pattern                              = "pattern";

  /**
   * Value of "replaceAll" for <code>matchingType</code> in a
   * <code>RewritingRule</code>.
   */
  public static final String VAL_replaceAll                           = "replaceAll";

  /** <code>stopOnMatch</code> attribute in <code>RewritingRule</code>. */
  public static final String ATT_stopOnMatch                          = "stopOnMatch";

  /*
   * Authentication
   */

  /** Name of a <code>AuthenticationManager</code> object element. */
  public static final String ELT_AuthenticationManager                = "AuthenticationManager";

  /** Name of a <code>Admin</code> object element. */
  public static final String ELT_Admin                                = "Admin";

  /** Name of a <code>User</code> object element. */
  public static final String ELT_User                                 = "User";

  /** <code>username</code> attribute in <code>User</code>. */
  public static final String ATT_username                             = "username";

  /**
   * <code>password</code> attribute in <code>User</code>. defined
   * somewhere else
   */

  /** <code>VirtualUsers</code> object element */
  public static final String ELT_VirtualUsers                         = "VirtualUsers";

  /** Name of a <code>VirtualLogin</code> object element. */
  public static final String ELT_VirtualLogin                         = "VirtualLogin";

  /**
   * <code>vLogin</code> attribute in <code>VirtualLogin</code> and
   * <code>ConnectionManager</code>.
   */
  public static final String ATT_vLogin                               = "vLogin";

  /** <code>vPassword</code> attribute in <code>VirtualLogin</code>. */
  public static final String ATT_vPassword                            = "vPassword";

  /*
   * Database Schema
   */

  /** Name of a <code>DatabaseSchema</code> object element. */
  public static final String ELT_DatabaseSchema                       = "DatabaseSchema";
  /**
   * Dynamic schema precision in <code>DatabaseSchema</code> value can be
   * static,table,column,procedures,all
   */
  public static final String ATT_dynamicPrecision                     = "dynamicPrecision";
  /** Static level for dynamic schema means no dynamicity is used at all */
  public static final String VAL_static                               = "static";
  // public static final String VAL_table = "table"; // already defined
  // public static final String VAL_column = "column"; // already defined
  /** Procedures level means procedures names are retrieved and checked */
  public static final String VAL_procedures                           = "procedures";
  // public static final String VAL_all = "all"; // already defined

  /** Static schema definition */
  public static final String ELT_DatabaseStaticSchema                 = "DatabaseStaticSchema";

  /** Name of a <code>DatabaseTable</code> object element. */
  public static final String ELT_DatabaseTable                        = "DatabaseTable";

  /** <code>gatherSystemTables</code> attribute in <code>DatabaseSchema</code> */
  public static final String ATT_gatherSystemTables                   = "gatherSystemTables";
  /** <code>schemaName</code> attribute in <code>DatabaseSchema</code> */
  public static final String ATT_schemaName                           = "schemaName";

  /**
   * <code>tableName</code> attribute in <code>DatabaseTable</code>,
   * <code>CreateTable</code>,<code>RecoveryLogTable</code> and
   * <code>CheckpointTable</code>
   */
  public static final String ATT_tableName                            = "tableName";

  /** <code>nbOfColumns</code> attribute in <code>DatabaseTable</code>. */
  public static final String ATT_nbOfColumns                          = "nbOfColumns";

  /** Name of a <code>DatabaseColumn</code> object element. */
  public static final String ELT_DatabaseColumn                       = "DatabaseColumn";

  /** <code>columnName</code> attribute in <code>DatabaseColumn</code>. */
  public static final String ATT_columnName                           = "columnName";

  /** <code>isUnique</code> attribute in <code>DatabaseColumn</code>. */
  public static final String ATT_isUnique                             = "isUnique";

  /** <code>DatabaseProcedure</code> object element */
  public static final String ELT_DatabaseProcedure                    = "DatabaseProcedure";

  /** <code>DatabaseProcedure</code> returns a result */
  public static final String VAL_returnsResult                        = "returnsResult";
  /** <code>DatabaseProcedure</code> does not say whether it returns a result */
  public static final String VAL_resultUnknown                        = "resultUnknown";
  /** <code>DatabaseProcedure</code> returns no result */
  public static final String VAL_noResult                             = "noResult";

  /** <code>DatabaseProcedureColumn</code> object element */
  public static final String ELT_DatabaseProcedureColumn              = "DatabaseProcedureColumn";

  /** Specify the return type of a procedure given the java specification */
  public static final String ATT_returnType                           = "returnType";

  /** Allow the procedure parameter to be null */
  public static final String ATT_nullable                             = "nullable";
  /** parameter cannot be null */
  public static final String VAL_noNulls                              = "noNulls";
  /** parameter can be null */
  public static final String VAL_nullable                             = "nullable";
  /** parameter with null value unknown */
  public static final String VAL_nullableUnknown                      = "nullableUnknown";

  /** Param type as in IN|OUT|RETURN ... */
  public static final String ATT_paramType                            = "paramType";
  /** column type in */
  public static final String VAL_in                                   = "in";
  /** column type out */
  public static final String VAL_out                                  = "out";
  /** column type in out */
  public static final String VAL_inout                                = "inout";
  /** column type return */
  public static final String VAL_return                               = "return";
  /** column type result */
  public static final String VAL_result                               = "result";
  /** column type unknown */
  public static final String VAL_unknown                              = "unknown";

  /*
   * Request Manager
   */

  /** Name of a <code>RequestManager</code> object element. */
  public static final String ELT_RequestManager                       = "RequestManager";

  /**
   * <code>caseSensitiveParsing</code> attribute in
   * <code>RequestManager</code>.
   */
  public static final String ATT_caseSensitiveParsing                 = "caseSensitiveParsing";

  /** <code>beginTimeout</code> attribute in <code>RequestManager</code>. */
  public static final String ATT_beginTimeout                         = "beginTimeout";

  /** <code>commitTimeout</code> attribute in <code>RequestManager</code>. */
  public static final String ATT_commitTimeout                        = "commitTimeout";

  /** <code>rollbackTimeout</code> attribute in <code>RequestManager</code>. */
  public static final String ATT_rollbackTimeout                      = "rollbackTimeout";

  /*
   * Macro Handling
   */
  /** Name of a <code>MacroHandling</code> object element. */
  public static final String ELT_MacroHandling                        = "MacroHandling";

  /** Rand macro */
  public static final String ATT_rand                                 = "rand";
  /** Now Macro */
  public static final String ATT_now                                  = "now";
  /** currentDate macro */
  public static final String ATT_currentDate                          = "currentDate";
  /** currentTime macro */
  public static final String ATT_currentTime                          = "currentTime";
  /** timeOfDay macro */
  public static final String ATT_timeOfDay                            = "timeOfDay";
  /** currentTimestamp macro */
  public static final String ATT_currentTimestamp                     = "currentTimestamp";
  /** Timer precision to use when rewriting a query */
  public static final String ATT_timeResolution                       = "timeResolution";

  /** Value for the <code>MacrosHandler</code> */
  public static final String VAL_int                                  = "int";
  /** Value for the <code>MacrosHandler</code> */
  public static final String VAL_long                                 = "long";
  /** Value for the <code>MacrosHandler</code> */
  public static final String VAL_double                               = "double";
  /** Value for the <code>MacrosHandler</code> */
  public static final String VAL_float                                = "float";
  /** Value for the <code>MacrosHandler</code> */
  public static final String VAL_time                                 = "time";
  /** Value for the <code>MacrosHandler</code> */
  public static final String VAL_date                                 = "date";
  /** Value for the <code>MacrosHandler</code> */
  public static final String VAL_timestamp                            = "timestamp";

  /*
   * Request Scheduler
   */

  /** Name of a <code>RequestScheduler</code> object element. */
  public static final String ELT_RequestScheduler                     = "RequestScheduler";

  /** Name of a <code>SingleDBScheduler</code> object element. */
  public static final String ELT_SingleDBScheduler                    = "SingleDBScheduler";

  /** Name of a <code>RAIDb-0Scheduler</code> object element. */
  public static final String ELT_RAIDb0Scheduler                      = "RAIDb-0Scheduler";

  /** Name of a <code>RAIDb-1Scheduler</code> object element. */
  public static final String ELT_RAIDb1Scheduler                      = "RAIDb-1Scheduler";

  /** Name of a <code>RAIDb-2Scheduler</code> object element. */
  public static final String ELT_RAIDb2Scheduler                      = "RAIDb-2Scheduler";

  /** <code>level</code> attribute in request schedulers. */
  public static final String ATT_level                                = "level";

  /** Value of a Request Scheduler <code>level</code> attribute. */
  public static final String VAL_passThrough                          = "passThrough";

  /** Value of a Request Scheduler <code>level</code> attribute. */
  public static final String VAL_query                                = "query";

  /** Value of a Request Scheduler <code>level</code> attribute. */
  public static final String VAL_optimisticQuery                      = "optimisticQuery";

  /** Value of a Request Scheduler <code>level</code> attribute. */
  public static final String VAL_optimisticTransaction                = "optimisticTransaction";

  /** Value of a Request Scheduler <code>level</code> attribute. */
  public static final String VAL_pessimisticTransaction               = "pessimisticTransaction";

  /*
   * Request Cache
   */

  /** Name of a <code>RequestCache</code> object element. */
  public static final String ELT_RequestCache                         = "RequestCache";

  /** Name of a <code>MetadataCache</code> object element. */
  public static final String ELT_MetadataCache                        = "MetadataCache";

  /** <code>maxNbOfMetadata</code> attribute in <code>MetadataCache</code>. */
  public static final String ATT_maxNbOfMetadata                      = "maxNbOfMetadata";

  /** <code>maxNbOfField</code> attribute in <code>MetadataCache</code>. */
  public static final String ATT_maxNbOfField                         = "maxNbOfField";

  /** Name of a <code>ParsingCache</code> object element. */
  public static final String ELT_ParsingCache                         = "ParsingCache";

  /** <code>backgroundParsing</code> attribute in <code>ParsingCache</code>. */
  public static final String ATT_backgroundParsing                    = "backgroundParsing";

  /**
   * <code>ATT_maxNbOfEntries</code> attribute in <code>ParsingCache</code>
   * and <code>ResultCache</code>
   */
  public static final String ATT_maxNbOfEntries                       = "maxNbOfEntries";

  /** Name of a <code>ResultCache</code> object element. */
  public static final String ELT_ResultCache                          = "ResultCache";

  /**
   * <code>granularity</code> attribute in <code>ResultCache</code> or
   * <code>ResultCacheDB</code>.
   */
  public static final String ATT_granularity                          = "granularity";

  /** Value of a <code>ResultCache/granularity</code> attribute. */
  public static final String VAL_noInvalidation                       = "noInvalidation";

  /** Value of a <code>ResultCache/granularity</code> attribute. */
  public static final String VAL_database                             = "database";

  /** Value of a <code>ResultCache/granularity</code> attribute. */
  public static final String VAL_table                                = "table";

  /** Value of a <code>ResultCache/granularity</code> attribute. */
  public static final String VAL_column                               = "column";

  /** Value of a <code>ResultCache/granularity</code> attribute. */
  public static final String VAL_columnUnique                         = "columnUnique";

  /** <code>pendingTimeout</code> attribute in <code>ResultCache</code> */
  public static final String ATT_pendingTimeout                       = "pendingTimeout";

  /*
   * Cache Patterns and actionss
   */

  /** <code>DefaultResultCacheRule</code> element */
  public static final String ELT_DefaultResultCacheRule               = "DefaultResultCacheRule";

  /** <code>ResultCacheRule</code> element */
  public static final String ELT_ResultCacheRule                      = "ResultCacheRule";

  /**
   * <code>timestampResolution</code> attribute in <code>ResultCache</cache>,
   * <code>RAIDb-1</code>, <code>RAIDb-1ec</code>, 
   * <code>RAIDb-2</code> or <code>RAIDb-2ec</code>
   */
  public static final String ATT_timestampResolution                  = "timestampResolution";

  /** <code>NoCaching</code> action for a caching rule */
  public static final String ELT_NoCaching                            = "NoCaching";

  /** <code>EagerCaching</code> action for a caching rule */
  public static final String ELT_EagerCaching                         = "EagerCaching";

  /** <code>RelaxedCaching</code> action for a caching rule */
  public static final String ELT_RelaxedCaching                       = "RelaxedCaching";

  /** parameter for the relaxed caching action */
  public static final String ATT_keepIfNotDirty                       = "keepIfNotDirty";

  /*
   * Load Balancers
   */

  /** Name of a <code>RequestLoadBalancer</code> object element. */
  public static final String ELT_LoadBalancer                         = "LoadBalancer";

  /** Name of a <code>SingleDB</code> object element. */
  public static final String ELT_SingleDB                             = "SingleDB";

  /** Name of a <code>ParallelDB</code> object element. */
  public static final String ELT_ParallelDB                           = "ParallelDB";

  /** Name of a <code>ParallelDB-RoundRobin</code> object element. */
  public static final String ELT_ParallelDB_RoundRobin                = "ParallelDB-RoundRobin";

  /**
   * Name of a <code>ParallelDB-LeastPendingRequestsFirst</code> object
   * element.
   */
  public static final String ELT_ParallelDB_LeastPendingRequestsFirst = "ParallelDB-LeastPendingRequestsFirst";

  /** Name of a <code>RAIDb-0</code> object element. */
  public static final String ELT_RAIDb_0                              = "RAIDb-0";

  /** Name of a <code>RAIDb-1</code> object element. */
  public static final String ELT_RAIDb_1                              = "RAIDb-1";

  /** Name of a <code>RAIDb-1-RoundRobin</code> object element. */
  public static final String ELT_RAIDb_1_RoundRobin                   = "RAIDb-1-RoundRobin";

  /** Name of a <code>RAIDb-1-WeightedRoundRobin</code> object element. */
  public static final String ELT_RAIDb_1_WeightedRoundRobin           = "RAIDb-1-WeightedRoundRobin";

  /** Name of a <code>RAIDb-1-LeastPendingRequestsFirst</code> object element. */
  public static final String ELT_RAIDb_1_LeastPendingRequestsFirst    = "RAIDb-1-LeastPendingRequestsFirst";

  /** Name of a <code>RAIDb-1ec</code> object element. */
  public static final String ELT_RAIDb_1ec                            = "RAIDb-1ec";

  /**
   * <code>nbOfConcurrentReads</code> attribute in <code>RAIDb-1ec</code>
   * and <code>RAIDb-2ec</code>
   */
  public static final String ATT_nbOfConcurrentReads                  = "nbOfConcurrentReads";

  /** Name of a <code>RAIDb-1ec-RoundRobin</code> object element. */
  public static final String ELT_RAIDb_1ec_RoundRobin                 = "RAIDb-1ec-RoundRobin";

  /** Name of a <code>RAIDb-1ec-WeightedRoundRobin</code> object element. */
  public static final String ELT_RAIDb_1ec_WeightedRoundRobin         = "RAIDb-1ec-WeightedRoundRobin";

  /** Name of a <code>RAIDb-2</code> object element. */
  public static final String ELT_RAIDb_2                              = "RAIDb-2";

  /** Name of a <code>RAIDb-2-RoundRobin</code> object element. */
  public static final String ELT_RAIDb_2_RoundRobin                   = "RAIDb-2-RoundRobin";

  /** Name of a <code>RAIDb-2-WeightedRoundRobin</code> object element. */
  public static final String ELT_RAIDb_2_WeightedRoundRobin           = "RAIDb-2-WeightedRoundRobin";

  /** Name of a <code>RAIDb-2-LeastPendingRequestsFirst</code> object element. */
  public static final String ELT_RAIDb_2_LeastPendingRequestsFirst    = "RAIDb-2-LeastPendingRequestsFirst";

  /** Name of a <code>RAIDb-2ec</code> object element. */
  public static final String ELT_RAIDb_2ec                            = "RAIDb-2ec";

  /** Name of a <code>RAIDb-2ec-RoundRobin</code> object element. */
  public static final String ELT_RAIDb_2ec_RoundRobin                 = "RAIDb-2ec-RoundRobin";

  /** Name of a <code>RAIDb-2ec-WeightedRoundRobin</code> object element. */
  public static final String ELT_RAIDb_2ec_WeightedRoundRobin         = "RAIDb-2ec-WeightedRoundRobin";

  // WaitForCompletion

  /** Name of a <code>WaitForCompletion</code> object element */
  public static final String ELT_WaitForCompletion                    = "WaitForCompletion";

  /**
   * <code>policy</code> attribute in <code>WaitForCompletion</code> and
   * <code>CreateTable</code>
   */
  public static final String ATT_policy                               = "policy";

  /** Value of a <code>WaitForCompletion/policy</code> attribute */
  public static final String VAL_first                                = "first";

  /** Value of a <code>WaitForCompletion/policy</code> attribute */
  public static final String VAL_majority                             = "majority";

  /**
   * Value of a <code>WaitForCompletion/policy</code>,
   * <code>ErrorChecking</code> or <code>CreateTable/policy</code> attribute
   */
  public static final String VAL_all                                  = "all";

  // ErrorChecking

  /** Name of a <code>ErrorChecking</code> object element */
  public static final String ELT_ErrorChecking                        = "ErrorChecking";

  // CreateTable

  /** Name of a <code>CreateTable</code> object element */
  public static final String ELT_CreateTable                          = "CreateTable";

  /**
   * Value of a <code>CreateTable/policy</code> or
   * <code>ErrorChecking/policy</code> attribute
   */
  public static final String VAL_random                               = "random";

  /**
   * Value of a <code>CreateTable/policy</code> or
   * <code>ErrorChecking/policy</code> attribute
   */
  public static final String VAL_roundRobin                           = "roundRobin";

  /**
   * <code>numberOfNodes</code> attribute in <code>CreateTable</code> or
   * <code>ErrorChecking</code>
   */
  public static final String ATT_numberOfNodes                        = "numberOfNodes";

  // BackendWeight

  /** Name of a <code>BackendWeight</code> object element */
  public static final String ELT_BackendWeight                        = "BackendWeight";

  /** <code>weight</code> attribute in <code>BackendWeight</code>. */
  public static final String ATT_weight                               = "weight";

  /** Name of a <code>BackendName</code> object element */
  public static final String ELT_BackendName                          = "BackendName";

  /*
   * Connection Manager
   */

  /** Name of a <code>ConnectionManager</code> object element. */
  public static final String ELT_ConnectionManager                    = "ConnectionManager";

  /** Name of a <code>SimpleConnectionManager</code> object element. */
  public static final String ELT_SimpleConnectionManager              = "SimpleConnectionManager";

  /** Name of a <code>FailFastPoolConnectionManager</code> object element. */
  public static final String ELT_FailFastPoolConnectionManager        = "FailFastPoolConnectionManager";

  /** Name of a <code>RandomWaitPoolConnectionManager</code> object element. */
  public static final String ELT_RandomWaitPoolConnectionManager      = "RandomWaitPoolConnectionManager";

  /** Name of a <code>VariablePoolConnectionManager</code> object element. */
  public static final String ELT_VariablePoolConnectionManager        = "VariablePoolConnectionManager";

  /**
   * Real Login <code>rLogin</code> to use with this
   * <code>ConnectionManager</code>.
   */
  public static final String ATT_rLogin                               = "rLogin";
  /**
   * Real backend password <code>rPassword</code> to use with this
   * <code>ConnectionManager</code>.
   */
  public static final String ATT_rPassword                            = "rPassword";
  /**
   * Additional <code>urlParameters</code> to use with this
   * <code>ConnectionManager</code>.
   */
  public static final String ATT_urlParameters                        = "urlParameters";

  /**
   * <code>poolSize</code> attribute in
   * <code>FailFastPoolConnectionManager</code> or
   * <code>RandomWaitPoolConnectionManager</code>.
   */
  public static final String ATT_poolSize                             = "poolSize";

  /**
   * <code>timeout</code> attribute in
   * <code>RandomWaitPoolConnectionManager</code>.
   */
  public static final String ATT_timeout                              = "timeout";

  /**
   * <code>initPoolSize</code> attribute in
   * <code>VariablePoolConnectionManager</code>.
   */
  public static final String ATT_initPoolSize                         = "initPoolSize";

  /**
   * <code>minPoolSize</code> attribute in
   * <code>VariablePoolConnectionManager</code>.
   */
  public static final String ATT_minPoolSize                          = "minPoolSize";

  /**
   * <code>maxPoolSize</code> attribute in
   * <code>VariablePoolConnectionManager</code>.
   */
  public static final String ATT_maxPoolSize                          = "maxPoolSize";

  /**
   * <code>idleTimeout</code> attribute in
   * <code>VariablePoolConnectionManager</code>.
   */
  public static final String ATT_idleTimeout                          = "idleTimeout";

  /**
   * <code>waitTimeout</code> attribute in
   * <code>VariablePoolConnectionManager</code>.
   */
  public static final String ATT_waitTimeout                          = "waitTimeout";

  /*
   * Recovery Log
   */

  /** Name of a <code>RecoveryLog</code> object element. */
  public static final String ELT_RecoveryLog                          = "RecoveryLog";

  /** <code>login</code> attribute in <code>RecoveryLog</code>. */
  public static final String ATT_login                                = "login";

  /** <code>password</code> attribute in <code>RecoveryLog</code>. */
  public static final String ATT_password                             = "password";

  /** <code>requestTimeout</code> attribute in <code>RecoveryLog</code>. */
  public static final String ATT_requestTimeout                       = "requestTimeout";

  /** <code>recoveryBatchSize</code> attribute in <code>RecoveryLog</code>. */
  public static final String ATT_recoveryBatchSize                    = "recoveryBatchSize";

  /** Name of a <code>RecoveryLogTable</code> object element. */
  public static final String ELT_RecoveryLogTable                     = "RecoveryLogTable";

  /**
   * <code>idColumnType</code> attribute in <code>RecoveryLogTable</code>,
   * <code>CheckpointTable</code> and <code>BackendTable</code>.
   */
  public static final String ATT_createTable                          = "createTable";

  /** <code>idColumnType</code> attribute in <code>RecoveryLogTable</code>. */
  public static final String ATT_idColumnType                         = "idColumnType";

  /** <code>vloginColumnType</code> attribute in <code>RecoveryLogTable</code>. */
  public static final String ATT_vloginColumnType                     = "vloginColumnType";

  /** <code>sqlColumnName</code> attribute in <code>RecoveryLogTable</code>. */
  public static final String ATT_sqlColumnName                        = "sqlColumnName";

  /** <code>sqlColumnType</code> attribute in <code>RecoveryLogTable</code>. */
  public static final String ATT_sqlColumnType                        = "sqlColumnType";

  /**
   * <code>transactionIdColumnType</code> attribute in
   * <code>RecoveryLogTable</code>.
   */
  public static final String ATT_transactionIdColumnType              = "transactionIdColumnType";

  /**
   * <code>extraStatementDefinition</code> attribute in
   * <code>RecoveryLogTable</code> and <code>CheckpointTable</code>.
   */
  public static final String ATT_extraStatementDefinition             = "extraStatementDefinition";

  /** Name of a <code>CheckpointTable</code> object element. */
  public static final String ELT_CheckpointTable                      = "CheckpointTable";

  /**
   * <code>checkpointNameColumnType</code> attribute in
   * <code>CheckpointTable</code>.
   */
  public static final String ATT_checkpointNameColumnType             = "checkpointNameColumnType";

  /**
   * <code>requestIdColumnType</code> attribute in
   * <code>CheckpointTable</code>.
   */
  public static final String ATT_requestIdColumnType                  = "requestIdColumnType";

  /** Name of a <code>BackendTable</code> object element. */
  public static final String ELT_BackendTable                         = "BackendTable";

  /**
   * <code>databaseNameColumnType</code> attribute in
   * <code>BackendTable</code>.
   */
  public static final String ATT_databaseNameColumnType               = "databaseNameColumnType";

  /**
   * <code>backendNameColumnType</code> attribute in <code>BackendTable</code>
   * and <code>DumpTable</code>.
   */
  public static final String ATT_backendNameColumnType                = "backendNameColumnType";

  /**
   * <code>backendStateColumnType</code> attribute in
   * <code>BackendTable</code>.
   */
  public static final String ATT_backendStateColumnType               = "backendStateColumnType";

  /** Name of a <code>DumpTable</code> object element. */
  public static final String ELT_DumpTable                            = "DumpTable";
  /**
   * <code>dumpNameColumnType</code> attribute in <code>DumpTable</code>.
   */
  public static final String ATT_dumpNameColumnType                   = "dumpNameColumnType";
  /**
   * <code>dumpDateColumnType</code> attribute in <code>DumpTable</code>.
   */
  public static final String ATT_dumpDateColumnType                   = "dumpDateColumnType";
  /**
   * <code>dumpPathColumnType</code> attribute in <code>DumpTable</code>.
   */
  public static final String ATT_dumpPathColumnType                   = "dumpPathColumnType";
  /**
   * <code>dumpTypeColumnType</code> attribute in <code>DumpTable</code>.
   */
  public static final String ATT_dumpFormatColumnType                 = "dumpFormatColumnType";
  /**
   * <code>tablesColumnName</code> attribute in <code>DumpTable</code>.
   */
  public static final String ATT_tablesColumnName                     = "tablesColumnName";
  /**
   * <code>tablesColumnType</code> attribute in <code>DumpTable</code>.
   */
  public static final String ATT_tablesColumnType                     = "tablesColumnType";

}