/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.painless.ir;

import org.elasticsearch.painless.Location;
import org.elasticsearch.painless.MethodWriter;
import org.elasticsearch.painless.phase.IRTreeVisitor;
import org.elasticsearch.painless.symbol.WriteScope;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class IfElseNode extends ConditionNode {

    /* ---- begin tree structure ---- */

    private BlockNode elseBlockNode;

    public void setElseBlockNode(BlockNode elseBlockNode) {
        this.elseBlockNode = elseBlockNode;
    }

    public BlockNode getElseBlockNode() {
        return elseBlockNode;
    }

    /* ---- end tree structure, begin visitor ---- */

    @Override
    public <Scope> void visit(IRTreeVisitor<Scope> irTreeVisitor, Scope scope) {
        irTreeVisitor.visitIfElse(this, scope);
    }

    @Override
    public <Scope> void visitChildren(IRTreeVisitor<Scope> irTreeVisitor, Scope scope) {
        getConditionNode().visit(irTreeVisitor, scope);
        getBlockNode().visit(irTreeVisitor, scope);
        getElseBlockNode().visit(irTreeVisitor, scope);
    }

    /* ---- end visitor ---- */

    public IfElseNode(Location location) {
        super(location);
    }

    @Override
    protected void write(WriteScope writeScope) {
        MethodWriter methodWriter = writeScope.getMethodWriter();
        methodWriter.writeStatementOffset(getLocation());

        Label fals = new Label();
        Label end = new Label();

        getConditionNode().write(writeScope);
        methodWriter.ifZCmp(Opcodes.IFEQ, fals);
        getBlockNode().write(writeScope.newBlockScope());

        if (getBlockNode().doAllEscape() == false) {
            methodWriter.goTo(end);
        }

        methodWriter.mark(fals);
        elseBlockNode.write(writeScope.newBlockScope());
        methodWriter.mark(end);
    }
}
