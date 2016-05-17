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
 * Initial developer(s): Nicolas Modrzyk.
 * Contributor(s): Emmanuel Cecchet.
 */

package org.objectweb.cjdbc.controller.virtualdatabase;

import org.objectweb.cjdbc.common.log.Trace;
import org.objectweb.cjdbc.common.sql.metadata.MetadataContainer;
import org.objectweb.cjdbc.controller.backend.DatabaseBackend;

/**
 * Class gathering the static metadata related to the database. We collect
 * information from the underlying driver and keep this object for further
 * usage.
 * 
 * @author <a href="mailto:Nicolas.Modrzyk@inria.fr">Nicolas Modrzyk </a>
 * @author <a href="mailto:Emmanuel.Cecchet@inria.fr">Emmanuel Cecchet </a>
 * @version 1.0
 */
public class VirtualDatabaseStaticMetaData
{
  private String            vdbName;
  private Trace             logger;
  private MetadataContainer metadataContainer = null;

  /**
   * Reference the database for this metadata. Do not fetch any data at this
   * time
   * 
   * @param database to link this metadata to
   */
  public VirtualDatabaseStaticMetaData(VirtualDatabase database)
  {
    this.vdbName = database.getVirtualDatabaseName();
    this.logger = Trace
        .getLogger("org.objectweb.cjdbc.controller.virtualdatabase.VirtualDatabaseWorkerThread."
            + vdbName + ".metadata");
  }

  /**
   * Save the driver metadata of the backend if this is the first one to be
   * collected. If not display a warning for each incompatible value.
   * 
   * @param backend the new backend to get metadata from
   */
  public void gatherStaticMetadata(DatabaseBackend backend)
  {
    MetadataContainer newContainer = backend.getDatabaseStaticMetadata();
    if (logger.isDebugEnabled())
      logger.debug("fetching static metadata for backend:" + backend.getName());
    if (metadataContainer == null)
      metadataContainer = newContainer;
    else
    {
      boolean isCompatible = metadataContainer.isCompatible(newContainer,
          logger);
      if (logger.isDebugEnabled())
        logger.debug("Backend static metadata is compatible with current ones:"
            + isCompatible);
    }
  }

  /**
   * Returns the ("getXXX(Y,Z,...)", value) hash table holding metadata queries.
   * 
   * @return Returns the metadataContainer.
   */
  public MetadataContainer getMetadataContainer()
  {
    return metadataContainer;
  }
}