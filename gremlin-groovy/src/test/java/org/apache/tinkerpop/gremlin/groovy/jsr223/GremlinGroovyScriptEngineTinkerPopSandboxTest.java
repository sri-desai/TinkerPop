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
package org.apache.tinkerpop.gremlin.groovy.jsr223;

import org.apache.tinkerpop.gremlin.groovy.jsr223.customizer.TinkerPopSandboxExtension;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.junit.Test;

import javax.script.Bindings;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GremlinGroovyScriptEngineTinkerPopSandboxTest {
    @Test
    public void shouldNotEvalAsTheMethodIsNotWhiteListed() throws Exception {
        final CompileStaticGroovyCustomizer standardSandbox = new CompileStaticGroovyCustomizer(TinkerPopSandboxExtension.class.getName());
        try (GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine(standardSandbox)) {
            engine.eval("java.lang.Math.abs(123)");
            fail("Should have a compile error because class/method is not white listed");
        } catch (Exception ex) {
            assertEquals(MultipleCompilationErrorsException.class, ex.getCause().getClass());
            assertThat(ex.getCause().getMessage(), containsString("Not authorized to call this method"));
        }
    }

    @Test
    public void shouldEvalOnGAsTheMethodIsWhiteListed() throws Exception {
        final Graph graph = TinkerFactory.createModern();
        final GraphTraversalSource g = graph.traversal();
        final CompileStaticGroovyCustomizer standardSandbox = new CompileStaticGroovyCustomizer(TinkerPopSandboxExtension.class.getName());
        try (GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine(standardSandbox)) {
            final Bindings bindings = engine.createBindings();
            bindings.put("g", g);
            bindings.put("marko", convertToVertexId(graph, "marko"));
            assertEquals(g.V(convertToVertexId(graph, "marko")).next(), engine.eval("g.V(marko).next()", bindings));
            assertEquals(g.V(convertToVertexId(graph, "marko")).out("created").count().next(), engine.eval("g.V(marko).out(\"created\").count().next()", bindings));
        }
    }

    private Object convertToVertexId(final Graph graph, final String vertexName) {
        return convertToVertex(graph, vertexName).id();
    }

    private Vertex convertToVertex(final Graph graph, final String vertexName) {
        // all test graphs have "name" as a unique id which makes it easy to hardcode this...works for now
        return graph.traversal().V().has("name", vertexName).next();
    }
}
