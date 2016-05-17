/*
 * $Id: FlattenWhereNodeVisitor.java,v 1.4 2003/03/27 19:14:03 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002 Axion Development Team.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The names "Tigris", "Axion", nor the names of its contributors may
 *    not be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * 4. Products derived from this software may not be called "Axion", nor
 *    may "Tigris" or "Axion" appear in their names without specific prior
 *    written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =======================================================================
 */

package org.axiondb.engine;

import java.util.HashSet;

import org.axiondb.BinaryBranchWhereNode;
import org.axiondb.InWhereNode;
import org.axiondb.LeafWhereNode;
import org.axiondb.NotWhereNode;
import org.axiondb.WhereNode;

/**
 * Decomposes a {@link WhereNode} tree into a {@link HashSet}
 * of {@link WhereNode}s that were originally ANDed together
 * in the source tree.
 *
 * @author Morgan Delagrange
 * @author Chuck Burdick
 */
public class FlattenWhereNodeVisitor extends BaseWhereNodeVisitor {

    protected HashSet _nodes = null;

    public HashSet getNodes(WhereNode node) {
        _nodes = new HashSet();
        visit(node);
        return _nodes;
    }

    public void visitBinaryBranchWhereNode(BinaryBranchWhereNode node) {
        if(node.isAnd()) {
            transverseWhereNode(node);
        } else {
            _nodes.add(node);
        }
    }

    public void visitLeafWhereNode(LeafWhereNode node) {
        _nodes.add(node);
    }

    public void visitInWhereNode(InWhereNode node) {
        _nodes.add(node);
    }

    public void visitNotWhereNode(NotWhereNode node) {
        _nodes.add(node);
    }

}
