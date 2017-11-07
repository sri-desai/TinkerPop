/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.traversal.step.map;

import org.apache.tinkerpop.gremlin.process.traversal.Parameterizing;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.FromToModulating;
import org.apache.tinkerpop.gremlin.process.traversal.step.Mutating;
import org.apache.tinkerpop.gremlin.process.traversal.step.Scoping;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Parameters;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.CallbackRegistry;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.Event;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.ListCallbackRegistry;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public final class AddEdgeStep<S> extends MapStep<S, Edge>
        implements Mutating<Event.EdgeAddedEvent>, TraversalParent, Parameterizing, Scoping, FromToModulating {

    private static final String FROM = Graph.Hidden.hide("from");
    private static final String TO = Graph.Hidden.hide("to");

    private Parameters parameters = new Parameters();
    private CallbackRegistry<Event.EdgeAddedEvent> callbackRegistry;

    public AddEdgeStep(final Traversal.Admin traversal, final String edgeLabel) {
        super(traversal);
        this.parameters.set(this, T.label, edgeLabel);
    }

    @Override
    public <S, E> List<Traversal.Admin<S, E>> getLocalChildren() {
        return this.parameters.getTraversals();
    }

    @Override
    public Parameters getParameters() {
        return this.parameters;
    }

    @Override
    public Set<String> getScopeKeys() {
        return this.parameters.getReferencedLabels();
    }

    @Override
    public void addPropertyMutations(final Object... keyValues) {
        this.parameters.set(this, keyValues);
    }

    @Override
    public void addTo(final Traversal.Admin<?,?> toObject) {
        this.parameters.set(this, TO, toObject);
    }

    @Override
    public void addFrom(final Traversal.Admin<?,?> fromObject) {
        this.parameters.set(this, FROM, fromObject);
    }

    @Override
    protected Edge map(final Traverser.Admin<S> traverser) {
        final Vertex toVertex = this.parameters.get(traverser, TO, () -> (Vertex) traverser.get()).get(0);
        final Vertex fromVertex = this.parameters.get(traverser, FROM, () -> (Vertex) traverser.get()).get(0);
        final String edgeLabel = this.parameters.get(traverser, T.label, () -> Edge.DEFAULT_LABEL).get(0);

        final Edge edge = fromVertex.addEdge(edgeLabel, toVertex, this.parameters.getKeyValues(traverser, TO, FROM, T.label));
        if (callbackRegistry != null) {
            final Event.EdgeAddedEvent vae = new Event.EdgeAddedEvent(DetachedFactory.detach(edge, true));
            callbackRegistry.getCallbacks().forEach(c -> c.accept(vae));
        }
        return edge;
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements(TraverserRequirement.OBJECT);
    }

    @Override
    public CallbackRegistry<Event.EdgeAddedEvent> getMutatingCallbackRegistry() {
        if (null == this.callbackRegistry) this.callbackRegistry = new ListCallbackRegistry<>();
        return this.callbackRegistry;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.parameters.hashCode();
    }

    @Override
    public String toString() {
        return StringFactory.stepString(this, this.parameters.toString());
    }

    @Override
    public void setTraversal(final Traversal.Admin<?, ?> parentTraversal) {
        super.setTraversal(parentTraversal);
        this.parameters.getTraversals().forEach(this::integrateChild);
    }

    @Override
    public AddEdgeStep<S> clone() {
        final AddEdgeStep<S> clone = (AddEdgeStep<S>) super.clone();
        clone.parameters = this.parameters.clone();
        return clone;
    }

}
