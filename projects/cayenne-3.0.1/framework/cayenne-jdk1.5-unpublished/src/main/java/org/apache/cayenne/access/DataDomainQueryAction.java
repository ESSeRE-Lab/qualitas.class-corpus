/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cayenne.CayenneException;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.QueryResponse;
import org.apache.cayenne.cache.QueryCache;
import org.apache.cayenne.cache.QueryCacheEntryFactory;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.LifecycleEvent;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.EntityResultSegment;
import org.apache.cayenne.query.ObjectIdQuery;
import org.apache.cayenne.query.PrefetchSelectQuery;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.Query;
import org.apache.cayenne.query.QueryCacheStrategy;
import org.apache.cayenne.query.QueryMetadata;
import org.apache.cayenne.query.QueryRouter;
import org.apache.cayenne.query.RefreshQuery;
import org.apache.cayenne.query.RelationshipQuery;
import org.apache.cayenne.reflect.ClassDescriptor;
import org.apache.cayenne.reflect.LifecycleCallbackRegistry;
import org.apache.cayenne.util.GenericResponse;
import org.apache.cayenne.util.ListResponse;
import org.apache.cayenne.util.Util;
import org.apache.commons.collections.Transformer;

/**
 * Performs query routing and execution. During execution phase intercepts callbacks to
 * the OperationObserver, remapping results to the original pre-routed queries.
 * 
 * @since 1.2
 */
class DataDomainQueryAction implements QueryRouter, OperationObserver {

    static final boolean DONE = true;

    DataContext context;
    DataDomain domain;
    DataRowStore cache;
    Query query;
    QueryMetadata metadata;

    QueryResponse response;
    GenericResponse fullResponse;
    Map prefetchResultsByPath;
    Map<QueryEngine, Collection<Query>> queriesByNode;
    Map<Query, Query> queriesByExecutedQueries;
    boolean noObjectConversion;

    /*
     * A constructor for the "new" way of performing a query via 'execute' with
     * QueryResponse created internally.
     */
    DataDomainQueryAction(ObjectContext context, DataDomain domain, Query query) {
        if (context != null && !(context instanceof DataContext)) {
            throw new IllegalArgumentException(
                    "DataDomain can only work with DataContext. "
                            + "Unsupported context type: "
                            + context);
        }

        this.domain = domain;
        this.query = query;
        this.metadata = query.getMetaData(domain.getEntityResolver());
        this.context = (DataContext) context;

        // cache may be shared or unique for the ObjectContext
        if (context != null) {
            this.cache = this.context.getObjectStore().getDataRowCache();
        }

        if (this.cache == null) {
            this.cache = domain.getSharedSnapshotCache();
        }
    }

    QueryResponse execute() {

        // run chain...
        if (interceptOIDQuery() != DONE) {
            if (interceptRelationshipQuery() != DONE) {
                if (interceptRefreshQuery() != DONE) {
                    if (interceptSharedCache() != DONE) {
                        if (interceptDataDomainQuery() != DONE) {
                            runQueryInTransaction();
                        }
                    }
                }
            }
        }

        if (!noObjectConversion) {
            interceptObjectConversion();
        }

        return response;
    }

    private boolean interceptDataDomainQuery() {
        if (query instanceof DataDomainQuery) {
            response = new ListResponse(domain);
            return DONE;
        }

        return !DONE;
    }

    private boolean interceptOIDQuery() {
        if (query instanceof ObjectIdQuery) {

            ObjectIdQuery oidQuery = (ObjectIdQuery) query;
            DataRow row = null;

            if (cache != null && !oidQuery.isFetchMandatory()) {
                row = cache.getCachedSnapshot(oidQuery.getObjectId());
            }

            // refresh is forced or not found in cache
            if (row == null) {

                if (oidQuery.isFetchAllowed()) {

                    runQueryInTransaction();
                }
                else {
                    response = new ListResponse();
                }
            }
            else {
                response = new ListResponse(row);
            }

            return DONE;
        }

        return !DONE;
    }

    private boolean interceptRelationshipQuery() {

        if (query instanceof RelationshipQuery) {

            RelationshipQuery relationshipQuery = (RelationshipQuery) query;
            if (relationshipQuery.isRefreshing()) {
                return !DONE;
            }

            ObjRelationship relationship = relationshipQuery.getRelationship(domain
                    .getEntityResolver());

            // check if we can derive target PK from FK... this implies that the
            // relationship is to-one
            if (relationship.isSourceIndependentFromTargetChange()) {
                return !DONE;
            }

            if (cache == null) {
                return !DONE;
            }

            DataRow sourceRow = cache.getCachedSnapshot(relationshipQuery.getObjectId());
            if (sourceRow == null) {
                return !DONE;
            }

            // we can assume that there is one and only one DbRelationship as
            // we previously checked that "!isSourceIndependentFromTargetChange"
            DbRelationship dbRelationship = relationship.getDbRelationships().get(0);

            ObjectId targetId = sourceRow.createTargetObjectId(relationship
                    .getTargetEntityName(), dbRelationship);

            // null id means that FK is null...
            if (targetId == null) {
                this.response = new GenericResponse(Collections.EMPTY_LIST);
                return DONE;
            }

            DataRow targetRow = cache.getCachedSnapshot(targetId);

            if (targetRow != null) {
                this.response = new GenericResponse(Collections.singletonList(targetRow));
                return DONE;
            }

            // check whether a non-null FK is enough to assume non-null target, and if so,
            // create a fault
            if (context != null
                    && relationship.isSourceDefiningTargetPrecenseAndType(domain
                            .getEntityResolver())) {

                // prevent passing partial snapshots to ObjectResolver per CAY-724.
                // Create
                // a hollow object right here and skip object conversion downstream
                this.noObjectConversion = true;
                Object object = context.localObject(targetId, null);

                this.response = new GenericResponse(Collections.singletonList(object));
                return DONE;
            }
        }

        return !DONE;
    }

    /**
     * @since 3.0
     */
    private boolean interceptRefreshQuery() {

        if (query instanceof RefreshQuery) {
            RefreshQuery refreshQuery = (RefreshQuery) query;

            if (refreshQuery.isRefreshAll()) {

                // not sending any events - peer contexts will not get refreshed
                if (domain.getSharedSnapshotCache() != null) {
                    domain.getSharedSnapshotCache().clear();
                }
                context.getQueryCache().clear();

                GenericResponse response = new GenericResponse();
                response.addUpdateCount(1);
                this.response = response;
                return DONE;
            }

            Collection<Persistent> objects = (Collection<Persistent>) refreshQuery
                    .getObjects();
            if (objects != null && !objects.isEmpty()) {

                Collection<ObjectId> ids = new ArrayList<ObjectId>(objects.size());
                for (final Persistent object : objects) {
                    ids.add(object.getObjectId());
                }

                if (domain.getSharedSnapshotCache() != null) {
                    // send an event for removed snapshots
                    domain.getSharedSnapshotCache().processSnapshotChanges(
                            context.getObjectStore(),
                            Collections.EMPTY_MAP,
                            Collections.EMPTY_LIST,
                            ids,
                            Collections.EMPTY_LIST);
                }

                GenericResponse response = new GenericResponse();
                response.addUpdateCount(1);
                this.response = response;
                return DONE;
            }

            // 3. refresh query - this shouldn't normally happen as child datacontext
            // usually does a cascading refresh
            if (refreshQuery.getQuery() != null) {
                Query cachedQuery = refreshQuery.getQuery();

                String cacheKey = cachedQuery
                        .getMetaData(context.getEntityResolver())
                        .getCacheKey();
                context.getQueryCache().remove(cacheKey);

                this.response = domain.onQuery(context, cachedQuery);
                return DONE;
            }

            // 4. refresh groups...
            if (refreshQuery.getGroupKeys() != null
                    && refreshQuery.getGroupKeys().length > 0) {

                String[] groups = refreshQuery.getGroupKeys();
                for (String group : groups) {
                    domain.getQueryCache().removeGroup(group);
                }

                GenericResponse response = new GenericResponse();
                response.addUpdateCount(1);
                this.response = response;
                return DONE;
            }
        }

        return !DONE;
    }

    /*
     * Wraps execution in shared cache checks
     */
    private final boolean interceptSharedCache() {

        if (metadata.getCacheKey() == null) {
            return !DONE;
        }

        boolean cache = QueryCacheStrategy.SHARED_CACHE == metadata.getCacheStrategy();
        boolean cacheOrCacheRefresh = cache
                || QueryCacheStrategy.SHARED_CACHE_REFRESH == metadata.getCacheStrategy();

        if (!cacheOrCacheRefresh) {
            return !DONE;
        }

        QueryCache queryCache = domain.getQueryCache();
        QueryCacheEntryFactory factory = getCacheObjectFactory();

        if (cache) {
            List cachedResults = queryCache.get(metadata, factory);

            // response may already be initialized by the factory above ... it is null if
            // there was a preexisting cache entry
            if (response == null) {
                response = new ListResponse(cachedResults);
            }

            if (cachedResults instanceof ListWithPrefetches) {
                this.prefetchResultsByPath = ((ListWithPrefetches) cachedResults)
                        .getPrefetchResultsByPath();
            }
        }
        else {
            // on cache-refresh request, fetch without blocking and fill the cache
            queryCache.put(metadata, (List) factory.createObject());
        }

        return DONE;
    }

    private QueryCacheEntryFactory getCacheObjectFactory() {
        return new QueryCacheEntryFactory() {

            public Object createObject() {
                runQueryInTransaction();

                List list = response.firstList();
                if (list != null) {

                    // make an immutable list to make sure callers don't mess it up
                    list = Collections.unmodifiableList(list);

                    // include prefetches in the cached result
                    if (prefetchResultsByPath != null) {
                        list = new ListWithPrefetches(list, prefetchResultsByPath);
                    }
                }

                return list;
            }
        };
    }

    /*
     * Gets response from the underlying DataNodes.
     */
    void runQueryInTransaction() {

        domain.runInTransaction(new Transformer() {

            public Object transform(Object input) {
                runQuery();
                return null;
            }
        });
    }

    private void runQuery() {
        // reset
        this.fullResponse = new GenericResponse();
        this.response = this.fullResponse;
        this.queriesByNode = null;
        this.queriesByExecutedQueries = null;

        // whether this is null or not will driver further decisions on how to process
        // prefetched rows
        this.prefetchResultsByPath = metadata.getPrefetchTree() != null
                && !metadata.isFetchingDataRows() ? new HashMap() : null;

        // categorize queries by node and by "executable" query...
        query.route(this, domain.getEntityResolver(), null);

        // run categorized queries
        if (queriesByNode != null) {
            for (Map.Entry<QueryEngine, Collection<Query>> entry : queriesByNode
                    .entrySet()) {
                QueryEngine nextNode = entry.getKey();
                Collection<Query> nodeQueries = entry.getValue();
                nextNode.performQueries(nodeQueries, this);
            }
        }
    }

    private void interceptObjectConversion() {

        if (context != null && !metadata.isFetchingDataRows()) {

            List<Object> mainRows = response.firstList();
            if (mainRows != null && !mainRows.isEmpty()) {

                ObjectConversionStrategy converter;

                List<Object> rsMapping = metadata.getResultSetMapping();
                if (rsMapping == null) {
                    converter = new SingleObjectConversionStrategy();
                }
                else {

                    if (rsMapping.size() == 1) {
                        if (rsMapping.get(0) instanceof EntityResultSegment) {
                            converter = new SingleObjectConversionStrategy();
                        }
                        else {
                            converter = new SingleScalarConversionStrategy();
                        }
                    }
                    else {
                        converter = new MixedConversionStrategy();
                    }
                }

                converter.convert(mainRows);
            }
        }
    }

    public void route(QueryEngine engine, Query query, Query substitutedQuery) {

        Collection<Query> queries = null;
        if (queriesByNode == null) {
            queriesByNode = new HashMap<QueryEngine, Collection<Query>>();
        }
        else {
            queries = queriesByNode.get(engine);
        }

        if (queries == null) {
            queries = new ArrayList<Query>(5);
            queriesByNode.put(engine, queries);
        }

        queries.add(query);

        // handle case when routing resulted in an "executable" query different from the
        // original query.
        if (substitutedQuery != null && substitutedQuery != query) {

            if (queriesByExecutedQueries == null) {
                queriesByExecutedQueries = new HashMap<Query, Query>();
            }

            queriesByExecutedQueries.put(query, substitutedQuery);
        }
    }

    public QueryEngine engineForDataMap(DataMap map) {
        if (map == null) {
            throw new NullPointerException("Null DataMap, can't determine DataNode.");
        }

        QueryEngine node = domain.lookupDataNode(map);

        if (node == null) {
            throw new CayenneRuntimeException("No DataNode exists for DataMap " + map);
        }

        return node;
    }

    public void nextCount(Query query, int resultCount) {
        fullResponse.addUpdateCount(resultCount);
    }

    public void nextBatchCount(Query query, int[] resultCount) {
        fullResponse.addBatchUpdateCount(resultCount);
    }

    public void nextRows(Query query, List<?> dataRows) {

        // exclude prefetched rows in the main result
        if (prefetchResultsByPath != null && query instanceof PrefetchSelectQuery) {
            PrefetchSelectQuery prefetchQuery = (PrefetchSelectQuery) query;
            prefetchResultsByPath.put(prefetchQuery.getPrefetchPath(), dataRows);
        }
        else {
            fullResponse.addResultList(dataRows);
        }
    }

    public void nextRows(Query q, ResultIterator it) {
        throw new CayenneRuntimeException("Invalid attempt to fetch a cursor.");
    }

    public void nextGeneratedRows(Query query, ResultIterator keysIterator) {
        if (keysIterator != null) {
            try {
                nextRows(query, keysIterator.allRows());
            }
            catch (CayenneException ex) {
                // don't throw here....
                nextQueryException(query, ex);
            }
            finally {
                try {
                    keysIterator.close();
                }
                catch (CayenneException e) {
                    // don't throw here....
                    nextQueryException(query, e);
                }
            }
        }
    }

    public void nextQueryException(Query query, Exception ex) {
        throw new CayenneRuntimeException("Query exception.", Util.unwindException(ex));
    }

    public void nextGlobalException(Exception e) {
        throw new CayenneRuntimeException("Global exception.", Util.unwindException(e));
    }

    public boolean isIteratedResult() {
        return false;
    }

    abstract class ObjectConversionStrategy<T> {

        abstract void convert(List<T> mainRows);

        protected List<Persistent> toObjects(
                ClassDescriptor descriptor,
                PrefetchTreeNode prefetchTree,
                List<DataRow> normalizedRows) {
            List<Persistent> objects;

            // take a shortcut when no prefetches exist...
            if (prefetchTree == null) {
                objects = new ObjectResolver(context, descriptor, metadata
                        .isRefreshingObjects())
                        .synchronizedObjectsFromDataRows(normalizedRows);
            }
            else {
                HierarchicalObjectResolver resolver = new HierarchicalObjectResolver(context, metadata);
                objects = resolver.synchronizedObjectsFromDataRows(
                        prefetchTree,
                        normalizedRows,
                        prefetchResultsByPath);
            }
            return objects;
        }

        protected void updateResponse(List sourceObjects, List targetObjects) {
            if (response instanceof GenericResponse) {
                ((GenericResponse) response).replaceResult(sourceObjects, targetObjects);
            }
            else if (response instanceof ListResponse) {
                response = new ListResponse(targetObjects);
            }
            else {
                throw new IllegalStateException("Unknown response object: " + response);
            }
        }
    }

    class SingleObjectConversionStrategy extends ObjectConversionStrategy<DataRow> {

        @Override
        void convert(List<DataRow> mainRows) {

            ClassDescriptor descriptor = metadata.getClassDescriptor();
            PrefetchTreeNode prefetchTree = metadata.getPrefetchTree();

            List<Persistent> objects = toObjects(descriptor, prefetchTree, mainRows);
            updateResponse(mainRows, objects);

            // apply POST_LOAD callback
            LifecycleCallbackRegistry callbackRegistry = context
                    .getEntityResolver()
                    .getCallbackRegistry();

            if (!callbackRegistry.isEmpty(LifecycleEvent.POST_LOAD)) {
                callbackRegistry.performCallbacks(LifecycleEvent.POST_LOAD, objects);
            }
        }
    }

    class SingleScalarConversionStrategy extends ObjectConversionStrategy<Object> {

        @Override
        void convert(List<Object> mainRows) {
            // noop... scalars require no further processing
        }
    }

    class MixedConversionStrategy extends ObjectConversionStrategy<Object[]> {

        protected List<Persistent> toObjects(
                ClassDescriptor descriptor,
                PrefetchTreeNode prefetchTree,
                List<Object[]> rows,
                int position) {

            int len = rows.size();
            List<DataRow> rowsColumn = new ArrayList<DataRow>(len);
            for (int i = 0; i < len; i++) {
                rowsColumn.add((DataRow) rows.get(i)[position]);
            }
            List<Persistent> objects;
            if (prefetchTree != null) {
                PrefetchTreeNode prefetchTreeNode = null;
                for (PrefetchTreeNode prefetch : prefetchTree.getChildren()) {
                    if (descriptor.getEntity().getName().equals(prefetch.getEntityName())) {
                        if (prefetchTreeNode == null) {
                            prefetchTreeNode = new PrefetchTreeNode();
                        }
                        PrefetchTreeNode addPath = prefetchTreeNode.addPath(prefetch
                                .getPath());
                        addPath.setSemantics(PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS);
                        addPath.setPhantom(false);
                    }
                }
                prefetchTree = prefetchTreeNode;
            }
            if (prefetchTree == null) {
                objects = new ObjectResolver(context, descriptor, metadata
                        .isRefreshingObjects())
                        .synchronizedObjectsFromDataRows(rowsColumn);
            }
            else {
                HierarchicalObjectResolver resolver = new HierarchicalObjectResolver(
                        context,
                        metadata,
                        descriptor, true);
                objects = resolver.synchronizedObjectsFromDataRows(
                        prefetchTree,
                        rowsColumn,
                        prefetchResultsByPath);
            }
            return objects;
        }

        @Override
        void convert(List<Object[]> mainRows) {

            int rowsLen = mainRows.size();

            List<Object> rsMapping = metadata.getResultSetMapping();
            int width = rsMapping.size();
            // no conversions needed for scalar positions; reuse Object[]'s to fill them
            // with resolved objects
            List<List<?>> resultLists = new ArrayList<List<?>>(width);
            for (int i = 0; i < width; i++) {

                if (rsMapping.get(i) instanceof EntityResultSegment) {
                    EntityResultSegment entitySegment = (EntityResultSegment) rsMapping
                            .get(i);
                    List<Persistent> nextResult = toObjects(entitySegment
                            .getClassDescriptor(), metadata.getPrefetchTree() , mainRows, i);
                    
                    resultLists.add(nextResult);
                    
                    for (int j = 0; j < rowsLen; j++) {
                        Object[] row = mainRows.get(j);
                        row[i] = nextResult.get(j);
                    }
                }
            }
            Set<List<?>> seen = new HashSet(mainRows.size());
            Iterator<Object[]> it = mainRows.iterator();
            while (it.hasNext()) {
                
                if (!seen.add(Arrays.asList(it.next()))) {
                    it.remove();
                    
                }
                
            }
            
            // invoke callbacks now that all objects are resolved...
            LifecycleCallbackRegistry callbackRegistry = context
                    .getEntityResolver()
                    .getCallbackRegistry();

            if (!callbackRegistry.isEmpty(LifecycleEvent.POST_LOAD)) {
                for (List<?> list : resultLists) {
                    callbackRegistry.performCallbacks(LifecycleEvent.POST_LOAD, list);
                }
            }
        }
    }
}
