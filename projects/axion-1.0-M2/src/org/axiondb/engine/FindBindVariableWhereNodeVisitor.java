/*
 * $Id: FindBindVariableWhereNodeVisitor.java,v 1.4 2003/01/06 19:30:56 rwald Exp $
 * =======================================================================
 * Copyright (c) 2002-2003 Axion Development Team.  All rights reserved.
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

import org.axiondb.BindVariable;
import org.axiondb.LeafWhereNode;
import org.axiondb.WhereNode;

/**
 * A {@link WhereNodeVisitor} that finds an indexed {@link BindVariable}
 * within the visited tree.
 * 
 * @version $Revision: 1.4 $ $Date: 2003/01/06 19:30:56 $
 * @author Rod Waldhoff
 * @deprecated Apparently no longer used (replaced by CollectBindVariableWhereNodeVisitor)
 */
public class FindBindVariableWhereNodeVisitor extends BaseWhereNodeVisitor {
    /**
     * @param index the 1-based index of the {@link BindVariable} to find
     */
    public FindBindVariableWhereNodeVisitor(int index) {
        _index = index;
    }
    
    public BindVariable getBindVariable() {
        return _bindVar;
    }

    public void visit(WhereNode node) {
        if(null != _bindVar) {
            return;
        } else {
            super.visit(node);
        }
    }

    public void visitLeafWhereNode(LeafWhereNode node) {
        if(node.getLeft() instanceof BindVariable) {
            if(_index == 1) {
                _bindVar = (BindVariable)(node.getLeft());
                return;
            } else {
                _index--;
            }
        }
        if(node.getRight() instanceof BindVariable) {
            if(_index == 1) {
                _bindVar = (BindVariable)(node.getRight());
                return;
            } else {
                _index--;
            }
        }
    }

    private int _index;
    private BindVariable _bindVar = null;
}
