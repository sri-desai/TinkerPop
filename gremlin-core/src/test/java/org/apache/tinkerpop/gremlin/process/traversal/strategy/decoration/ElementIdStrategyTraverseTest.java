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
package org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddEdgeStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PropertiesStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@RunWith(Parameterized.class)
public class ElementIdStrategyTraverseTest {
    private static Traversal traversalWithAddV;

    static {
        final Graph mockedGraph = mock(Graph.class);
        final DefaultGraphTraversal t = new DefaultGraphTraversal<>(mockedGraph);
        t.asAdmin().addStep(new GraphStep<>(t.asAdmin(), Vertex.class, true));
        traversalWithAddV = t.addV();
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"addV()", traversalWithAddV, 1},
                {"addE(test).from(x)", __.addE("test").from("x"), 0},
                {"addE(test).to(x)", __.addE("test").to("x"), 0},
                {"addE(test).from(x).property(key,value)", __.addE("test").from("x").property("key", "value"), 0},
                {"addE(test).to(x).property(key,value)", __.addE("test").to("x").property("key", "value"), 0},
                {"out().id()", __.out().id(), 1},
                {"in().id()", __.in().id(), 1},
                {"outE().id()", __.outE().id(), 1},
                {"inE().id()", __.inE().id(), 1},
                {"bothE().id()", __.bothE().id(), 1},
                {"bothE().otherV().id()", __.bothE().otherV().id(), 2},
                {"in().out().addE(test).from(x)", __.in().out().addE("test").from("x"), 2},
                {"in().out().addE(test).to(x)", __.in().out().addE("test").to("x"), 2},
        });
    }

    @Parameterized.Parameter(value = 0)
    public String name;

    @Parameterized.Parameter(value = 1)
    public Traversal traversal;

    @Parameterized.Parameter(value = 2)
    public int expectedInsertedSteps;

    @Test
    public void shouldAlterTraversalToIncludeIdWhereNecessary() {
        final ElementIdStrategy strategy = ElementIdStrategy.build().create();
        strategy.apply(traversal.asAdmin());

        final Step step = (Step) traversal.asAdmin().getSteps().get(expectedInsertedSteps);
        if (step instanceof AddVertexStep)
            assertTrue(((AddVertexStep) step).getParameters().contains(strategy.getIdPropertyKey()));
        else if (step instanceof AddVertexStartStep)
            assertTrue(((AddVertexStartStep) step).getParameters().contains(strategy.getIdPropertyKey()));
        else if (step instanceof AddEdgeStep)
            assertTrue(((AddEdgeStep) step).getParameters().contains(strategy.getIdPropertyKey()));
        else if (step instanceof PropertiesStep)
            assertEquals(strategy.getIdPropertyKey(), ((PropertiesStep) step).getPropertyKeys()[0]);
        else
            fail("Check test definition - the expectedInsertedSteps should be the index of the step to trigger the ID substitution");
    }
}
