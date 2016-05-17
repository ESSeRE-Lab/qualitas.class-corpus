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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.query.PrefetchProcessor;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.apache.cayenne.query.QueryMetadata;
import org.apache.cayenne.reflect.ClassDescriptor;

/**
 * Processes a number of DataRow sets corresponding to a given prefetch tree, resolving
 * DataRows to an object tree. Can process any combination of joint and disjoint sets, per
 * prefetch tree.
 */
class HierarchicalObjectResolver {

    DataContext context;
    QueryMetadata queryMetadata;
    DataRowStore cache;
    ClassDescriptor descriptor;
    boolean needToSaveDuplicates;

    HierarchicalObjectResolver(DataContext context, QueryMetadata queryMetadata) {
        this.queryMetadata = queryMetadata;
        this.context = context;
        this.cache = context.getObjectStore().getDataRowCache();
    }

    HierarchicalObjectResolver(DataContext context, QueryMetadata metadata,
            ClassDescriptor descriptor, boolean needToSaveDuplicates) {
        this(context, metadata);
        this.descriptor = descriptor;
        this.needToSaveDuplicates = needToSaveDuplicates;
    }

    /**
     * Properly synchronized version of 'resolveObjectTree'.
     */
    List synchronizedObjectsFromDataRows(
            PrefetchTreeNode tree,
            List mainResultRows,
            Map extraResultsByPath) {

        synchronized (context.getObjectStore()) {
            synchronized (cache) {
                return resolveObjectTree(tree, mainResultRows, extraResultsByPath);
            }
        }
    }

    private List resolveObjectTree(
            PrefetchTreeNode tree,
            List mainResultRows,
            Map extraResultsByPath) {

        // create a copy of the tree using DecoratedPrefetchNodes and then traverse it
        // resolving objects...
        PrefetchProcessorNode decoratedTree = new PrefetchProcessorTreeBuilder(
                this,
                mainResultRows,
                extraResultsByPath).buildTree(tree);

        // do a single path for disjoint prefetches, joint subtrees will be processed at
        // each disjoint node that is a parent of joint prefetches.
        decoratedTree.traverse(new DisjointProcessor());

        // connect related objects
        decoratedTree.traverse(new PostProcessor());

        return decoratedTree.getObjects() != null
                ? decoratedTree.getObjects()
                : new ArrayList(1);
    }

    final class DisjointProcessor implements PrefetchProcessor {

        public boolean startDisjointPrefetch(PrefetchTreeNode node) {

            PrefetchProcessorNode processorNode = (PrefetchProcessorNode) node;

            // this means something bad happened during fetch
            if (processorNode.getDataRows() == null) {
                return false;
            }

            // ... continue with processing even if the objects list is empty to handle
            // multi-step prefetches.
            if (processorNode.getDataRows().isEmpty()) {
                return true;
            }

            List objects = processorNode.getResolver().objectsFromDataRows(
                    processorNode.getDataRows());
            processorNode.setObjects(objects);

            return true;
        }

        public boolean startJointPrefetch(PrefetchTreeNode node) {

            // delegate processing of the top level joint prefetch to a joint processor,
            // skip non-top joint nodes

            if (node.getParent() != null && !node.getParent().isJointPrefetch()) {

                PrefetchProcessorJointNode processorNode = (PrefetchProcessorJointNode) node;

                JointProcessor subprocessor = new JointProcessor(processorNode);

                PrefetchProcessorNode parent = (PrefetchProcessorNode) processorNode
                        .getParent();

                while (parent != null && parent.isPhantom()) {
                    parent = (PrefetchProcessorNode) parent.getParent();
                }

                if (parent == null) {
                    return false;
                }

                List parentRows = parent.getDataRows();

                // phantom node?
                if (parentRows == null || parentRows.size() == 0) {
                    return false;
                }

                List parentObjects = parent.getObjects();
                int size = parentRows.size();

                for (int i = 0; i < size; i++) {
                    subprocessor.setCurrentFlatRow((DataRow) parentRows.get(i));
                    parent.setLastResolved((Persistent) parentObjects.get(i));
                    processorNode.traverse(subprocessor);
                }

                List objects = processorNode.getObjects();

                cache.snapshotsUpdatedForObjects(
                        objects,
                        processorNode.getResolvedRows(),
                        queryMetadata.isRefreshingObjects());

            }
            return true;
        }

        public boolean startPhantomPrefetch(PrefetchTreeNode node) {
            return true;
        }

        public boolean startUnknownPrefetch(PrefetchTreeNode node) {
            throw new CayenneRuntimeException("Unknown prefetch node: " + node);
        }

        public void finishPrefetch(PrefetchTreeNode node) {
            // now that all the children are processed, we can clear the dupes

            // TODO: see TODO in ObjectResolver.relatedObjectsFromDataRows

            if (node.isDisjointPrefetch() && !needToSaveDuplicates) {
                PrefetchProcessorNode processorNode = (PrefetchProcessorNode) node;
                if (processorNode.isJointChildren()) {
                    List<Persistent> objects = processorNode.getObjects();

                    if (objects != null && objects.size() > 1) {

                        Set<Persistent> seen = new HashSet<Persistent>(objects.size());
                        Iterator<Persistent> it = objects.iterator();
                        while (it.hasNext()) {
                            if (!seen.add(it.next())) {
                                it.remove();
                            }
                        }
                    }
                }
            }
        }
    }

    // a processor of a single joint result set that walks a subtree of prefetch nodes
    // that use this result set.
    final class JointProcessor implements PrefetchProcessor {

        DataRow currentFlatRow;
        PrefetchProcessorNode rootNode;

        JointProcessor(PrefetchProcessorJointNode rootNode) {
            this.rootNode = rootNode;
        }

        void setCurrentFlatRow(DataRow currentFlatRow) {
            this.currentFlatRow = currentFlatRow;
        }

        public boolean startDisjointPrefetch(PrefetchTreeNode node) {
            // disjoint prefetch that is not the root terminates the walk...
            // don't process the root node itself..
            return node == rootNode;
        }

        public boolean startJointPrefetch(PrefetchTreeNode node) {
            PrefetchProcessorJointNode processorNode = (PrefetchProcessorJointNode) node;

            Persistent object = null;

            // find existing object, if found skip further processing
            Map id = processorNode.idFromFlatRow(currentFlatRow);
            object = processorNode.getResolved(id);
            DataRow row = null;
            if (object == null) {

                row = processorNode.rowFromFlatRow(currentFlatRow);
                object = processorNode.getResolver().objectFromDataRow(row);

                // LEFT OUTER JOIN produced no matches...
                if (object == null) {
                    return false;
                }

                processorNode.putResolved(id, object);
                processorNode.addObject(object, row);
            }

            // linking by parent needed even if an object is already there
            // (many-to-many case)

            processorNode.getParentAttachmentStrategy().linkToParent(row, object);

            processorNode.setLastResolved(object);
            return processorNode.isJointChildren();
        }

        public boolean startPhantomPrefetch(PrefetchTreeNode node) {
            return ((PrefetchProcessorNode) node).isJointChildren();
        }

        public boolean startUnknownPrefetch(PrefetchTreeNode node) {
            throw new CayenneRuntimeException("Unknown prefetch node: " + node);
        }

        public void finishPrefetch(PrefetchTreeNode node) {
            // noop
        }
    }

    // processor that converts temporary associations between DataObjects to Cayenne
    // relationships and also fires snapshot update events
    final class PostProcessor implements PrefetchProcessor {

        public void finishPrefetch(PrefetchTreeNode node) {
        }

        public boolean startDisjointPrefetch(PrefetchTreeNode node) {
            ((PrefetchProcessorNode) node).connectToParents();
            return true;
        }

        public boolean startJointPrefetch(PrefetchTreeNode node) {
            PrefetchProcessorJointNode processorNode = (PrefetchProcessorJointNode) node;

            if (!processorNode.getObjects().isEmpty()) {
                cache.snapshotsUpdatedForObjects(
                        processorNode.getObjects(),
                        processorNode.getResolvedRows(),
                        queryMetadata.isRefreshingObjects());
            }

            // run 'connectToParents' even if the object list is empty. This is needed to
            // refresh stale relationships e.g. when some related objects got deleted.
            processorNode.connectToParents();
            return true;
        }

        public boolean startPhantomPrefetch(PrefetchTreeNode node) {
            return true;
        }

        public boolean startUnknownPrefetch(PrefetchTreeNode node) {
            throw new CayenneRuntimeException("Unknown prefetch node: " + node);
        }
    }
}
