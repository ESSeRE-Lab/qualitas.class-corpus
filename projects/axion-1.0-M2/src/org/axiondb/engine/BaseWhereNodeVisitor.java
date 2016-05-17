/*
 * $Id: BaseWhereNodeVisitor.java,v 1.5 2003/03/27 19:14:03 rwald Exp $
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

import org.axiondb.BinaryBranchWhereNode;
import org.axiondb.BranchWhereNode;
import org.axiondb.InWhereNode;
import org.axiondb.LeafWhereNode;
import org.axiondb.NotWhereNode;
import org.axiondb.WhereNode;
import org.axiondb.WhereNodeVisitor;

/**
 * A {@link WhereNodeVisitor} that delegates
 * incoming {@link WhereNode WhereNodes} to the appropriate
 * method.
 *
 * @version $Revision: 1.5 $ $Date: 2003/03/27 19:14:03 $
 * @author Morgan Delagrange
 * @author Chuck Burdick
 */
public class BaseWhereNodeVisitor implements WhereNodeVisitor {
    public void visit(WhereNode node) {
        if(node instanceof BranchWhereNode) {
            visitBranchWhereNode((BranchWhereNode)node);
        } else if(node instanceof LeafWhereNode) {
            visitLeafWhereNode((LeafWhereNode)node);
        } else if(node instanceof InWhereNode) {
            visitInWhereNode((InWhereNode)node);
        }
    }

    protected void visitLeafWhereNode(LeafWhereNode node) {
    }

    protected void visitInWhereNode(InWhereNode node) {
    }

    protected void visitBranchWhereNode(BranchWhereNode node) {
        if(node instanceof BinaryBranchWhereNode) {
            visitBinaryBranchWhereNode((BinaryBranchWhereNode)node);
        } else if(node instanceof NotWhereNode) {
            visitNotWhereNode((NotWhereNode)node);
        }
    }

    protected void visitBinaryBranchWhereNode(BinaryBranchWhereNode node) {
        transverseWhereNode(node);
    }

    protected void visitNotWhereNode(NotWhereNode node) {
        transverseWhereNode(node);
    }

    protected void transverseWhereNode(WhereNode node) {
        if(node instanceof BinaryBranchWhereNode) {
            BinaryBranchWhereNode bnode = (BinaryBranchWhereNode)node;
            visit(bnode.getLeft());
            visit(bnode.getRight());
        } else if(node instanceof NotWhereNode) {
            NotWhereNode nnode = (NotWhereNode)node;
            visit(nnode.getChild());
        }
    }
}
