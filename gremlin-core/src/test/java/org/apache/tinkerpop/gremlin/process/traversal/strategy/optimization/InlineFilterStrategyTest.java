/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.apache.tinkerpop.gremlin.process.traversal.P.eq;
import static org.apache.tinkerpop.gremlin.process.traversal.P.gt;
import static org.apache.tinkerpop.gremlin.process.traversal.P.lt;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.and;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.as;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.dedup;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.drop;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.filter;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.hasLabel;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.limit;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.match;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.or;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.tail;
import static org.junit.Assert.assertEquals;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */


@RunWith(Parameterized.class)
public class InlineFilterStrategyTest {

    @Parameterized.Parameter(value = 0)
    public Traversal original;

    @Parameterized.Parameter(value = 1)
    public Traversal optimized;

    @Test
    public void doTest() {
        final TraversalStrategies strategies = new DefaultTraversalStrategies();
        strategies.addStrategies(InlineFilterStrategy.instance());
        this.original.asAdmin().setStrategies(strategies);
        this.original.asAdmin().applyStrategies();
        assertEquals(this.optimized, this.original);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> generateTestParameters() {

        return Arrays.asList(new Traversal[][]{
                {has("age", 10).as("a").has("name", "marko").as("b").coin(0.5).as("c"), addHas(__.start(), "age", eq(10), "name", eq("marko")).as("a", "b").coin(0.5).as("c")},
                //
                {filter(out("knows")), filter(out("knows"))},
                {filter(has("age", gt(10))).as("a"), has("age", gt(10)).as("a")},
                {filter(has("age", gt(10)).as("b")).as("a"), has("age", gt(10)).as("b", "a")},
                {filter(has("age", gt(10)).as("a")), has("age", gt(10)).as("a")},
                {filter(and(has("age", gt(10)).as("a"), has("name", "marko"))), addHas(__.start(), "age", gt(10), "name", eq("marko")).as("a")},
                //
                {or(has("name", "marko"), has("age", 32)), or(has("name", "marko"), has("age", 32))},
                {or(has("name", "marko"), has("name", "bob")), has("name", eq("marko").or(eq("bob")))},
                {or(has("name", "marko"), has("name")), or(has("name", "marko"), has("name"))},
                {or(has("age", 10), and(has("age", gt(20)), has("age", lt(100)))), has("age", eq(10).or(gt(20).and(lt(100))))},
                {or(has("name", "marko"), filter(has("name", "bob"))), has("name", eq("marko").or(eq("bob")))},
                {or(has("name", "marko"), filter(or(filter(has("name", "bob")), has("name", "stephen")))), has("name", eq("marko").or(eq("bob").or(eq("stephen"))))},
                {or(has("name", "marko").as("a"), filter(or(filter(has("name", "bob")).as("b"), has("name", "stephen").as("c")))), has("name", eq("marko").or(eq("bob").or(eq("stephen")))).as("a", "b", "c")},
                //
                {and(has("age", gt(10)), filter(has("age", 22))), addHas(__.start(), "age", gt(10), "age", eq(22))},
                {and(has("age", gt(10)).as("a"), filter(has("age", 22).as("b")).as("c")).as("d"), addHas(__.start(), "age", gt(10), "age", eq(22)).as("a", "b", "c", "d")},
                {and(has("age", gt(10)).as("a"), and(filter(has("age", 22).as("b")).as("c"), has("name", "marko").as("d"))), addHas(__.start(), "age", gt(10), "age", eq(22), "name", eq("marko")).as("a", "b", "c", "d")},
                {and(has("age", gt(10)).as("a"), and(has("name", "stephen").as("b"), has("name", "marko").as("c")).as("d")).as("e"), addHas(__.start(), "age", gt(10), "name", eq("stephen"), "name", eq("marko")).as("a", "b", "c", "d", "e")},
                {and(has("age", gt(10)), and(out("knows"), has("name", "marko"))), has("age", gt(10)).and(out("knows"), has("name", "marko"))},
                {and(has("age", gt(20)), or(has("age", lt(10)), has("age", gt(100)))), addHas(__.start(), "age", gt(20), "age", lt(10).or(gt(100)))},
                {and(has("age", gt(20)).as("a"), or(has("age", lt(10)), has("age", gt(100)).as("b"))), addHas(__.start(), "age", gt(20), "age", lt(10).or(gt(100))).as("a", "b")},
                {and(has("age", gt(20)).as("a"), or(has("age", lt(10)).as("c"), has("age", gt(100)).as("b"))), addHas(__.start(), "age", gt(20), "age", lt(10).or(gt(100))).as("a", "b", "c")},
                //
                {V().match(as("a").has("age", 10), as("a").filter(has("name")).as("b")), V().has("age", 10).as("a").match(as("a").has("name").as("b"))},
                {match(as("a").has("age", 10), as("a").filter(has("name")).as("b")), match(as("a").has("age", 10), as("a").has("name").as("b"))},
                {match(as("a").has("age", 10).both().as("b"), as("b").out().as("c")), match(as("a").has("age", 10).both().as("b"), as("b").out().as("c"))},
                {__.map(match(as("a").has("age", 10), as("a").filter(has("name")).as("b"))), __.map(match(as("a").has("age", 10), as("a").has("name").as("b")))},
                {V().match(as("a").has("age", 10)), V().has("age", 10).as("a")},
                {V().match(as("a").has("age", 10).has("name", "marko").as("b")), V().has("age", 10).has("name", "marko").as("a", "b")},
                {V().match(as("a").has("age", 10).has("name", "marko").as("b"), as("a").out("knows").as("c")), V().has("age", 10).has("name", "marko").as("a", "b").match(as("a").out("knows").as("c"))},
                {V().match(as("a").out("knows").as("c"), as("a").has("age", 10).has("name", "marko").as("b")), V().has("age", 10).has("name", "marko").as("a", "b").match(as("a").out("knows").as("c"))},
                {V().match(as("a").out("knows").as("c"), as("a").has("age", 10).has("name", "marko").as("b"), as("a").has("name", "bob")), V().has("age", 10).has("name", "marko").has("name", "bob").as("a", "b").match(as("a").out("knows").as("c"))},
                {V().match(as("a").has("age", 10).as("b"), as("a").filter(has("name")).as("b")), V().has("age", 10).as("a", "b").match(as("a").has("name").as("b"))},
                //
                {filter(dedup()), filter(dedup())},
                {filter(filter(drop())), filter(drop())},
                {and(has("name"), limit(10).has("age")), and(has("name"), limit(10).has("age"))},
                {filter(tail(10).as("a")), filter(tail(10).as("a"))},
                //
                {outE().hasLabel("knows").inV(), outE("knows").inV()},
                {outE().hasLabel("knows").hasLabel("created").inV(), outE("knows").hasLabel("created").inV()},
                {outE().or(hasLabel("knows"), hasLabel("created")).inV(), outE("knows", "created").inV()},
                {outE().or(hasLabel("knows").as("a"), hasLabel("created").as("b")).as("c").inV(), outE("knows", "created").as("a", "b", "c").inV()},
                {outE().hasLabel(P.eq("knows").or(P.gt("created"))).has("weight", gt(1.0)).inV(), addHas(outE(), T.label.getAccessor(), P.eq("knows").or(P.gt("created")), "weight", gt(1.0)).inV()},
                {outE().hasLabel(P.eq("knows").or(P.eq("created"))).has("weight", gt(1.0)).inV(), outE("knows", "created").has("weight", gt(1.0)).inV()},
                // {outE().or(has(T.label,P.within("knows","likes")).hasLabel("created")).inV(), outE("knows", "likes", "created").inV()},
                {outE().hasLabel(P.within("knows", "created")).inV(), outE("knows", "created").inV()},
        });
    }

    private static GraphTraversal.Admin<?, ?> addHas(final GraphTraversal<?, ?> traversal, final Object... hasKeyValues) {
        final HasStep<?> hasStep = new HasStep<>((Traversal.Admin) traversal);
        for (int i = 0; i < hasKeyValues.length; i = i + 2) {
            hasStep.addHasContainer(new HasContainer((String) hasKeyValues[i], (P) hasKeyValues[i + 1]));
        }
        return traversal.asAdmin().addStep(hasStep);
    }
}
