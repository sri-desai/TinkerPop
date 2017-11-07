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

import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.javatuples.Pair;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GremlinGroovyScriptEngineTest {
    private static final Logger logger = LoggerFactory.getLogger(GremlinGroovyScriptEngineTest.class);

    private static final Object[] EMPTY_ARGS = new Object[0];

    @Test
    public void shouldCompileScriptWithoutRequiringVariableBindings() throws Exception {
        // compile() should cache the script to avoid future compilation
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();

        final String script = "g.V(x).out()";
        assertFalse(engine.isCached(script));
        assertNotNull(engine.compile(script));
        assertTrue(engine.isCached(script));

        engine.reset();

        assertFalse(engine.isCached(script));
    }

    @Test
    public void shouldEvalWithNoBindings() throws Exception {
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
        engine.eval("def addItUp(x,y){x+y}");
        assertEquals(3, engine.eval("1+2"));
        assertEquals(3, engine.eval("addItUp(1,2)"));
    }

    @Test
    public void shouldPromoteDefinedVarsInInterpreterModeWithNoBindings() throws Exception {
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine(new InterpreterModeGroovyCustomizer());
        engine.eval("def addItUp = { x, y -> x + y }");
        assertEquals(3, engine.eval("int xxx = 1 + 2"));
        assertEquals(4, engine.eval("yyy = xxx + 1"));
        assertEquals(7, engine.eval("def zzz = yyy + xxx"));
        assertEquals(4, engine.eval("zzz - xxx"));
        assertEquals("accessible-globally", engine.eval("if (yyy > 0) { def inner = 'should-stay-local'; outer = 'accessible-globally' }\n outer"));
        assertEquals("accessible-globally", engine.eval("outer"));

        try {
            engine.eval("inner");
            fail("Should not have been able to access 'inner'");
        } catch (Exception ex) {
            final Throwable root = ExceptionUtils.getRootCause(ex);
            assertThat(root, instanceOf(MissingPropertyException.class));
        }

        assertEquals(10, engine.eval("addItUp(zzz,xxx)"));
    }

    @Test
    public void shouldPromoteDefinedVarsInInterpreterModeWithBindings() throws Exception {
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine(new InterpreterModeGroovyCustomizer());
        final Bindings b = new SimpleBindings();
        b.put("x", 2);
        engine.eval("def addItUp = { x, y -> x + y }", b);
        assertEquals(3, engine.eval("int xxx = 1 + x", b));
        assertEquals(4, engine.eval("yyy = xxx + 1", b));
        assertEquals(7, engine.eval("def zzz = yyy + xxx", b));
        assertEquals(4, engine.eval("zzz - xxx", b));
        assertEquals("accessible-globally", engine.eval("if (yyy > 0) { def inner = 'should-stay-local'; outer = 'accessible-globally' }\n outer", b));
        assertEquals("accessible-globally", engine.eval("outer", b));

        try {
            engine.eval("inner", b);
            fail("Should not have been able to access 'inner'");
        } catch (Exception ex) {
            final Throwable root = ExceptionUtils.getRootCause(ex);
            assertThat(root, instanceOf(MissingPropertyException.class));
        }

        assertEquals(10, engine.eval("addItUp(zzz,xxx)", b));
    }

    @Test
    public void shouldEvalWithBindings() throws Exception {
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
        final Bindings b = new SimpleBindings();
        b.put("x", 2);
        assertEquals(3, engine.eval("1+x", b));
    }

    @Test
    public void shouldEvalWithNullInBindings() throws Exception {
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
        final Bindings b = new SimpleBindings();
        b.put("x", null);
        assertNull(engine.eval("x", b));
    }

    @Test
    public void shouldEvalSuccessfulAssert() throws Exception {
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
        assertNull(engine.eval("assert 1==1"));
    }

    @Test(expected = AssertionError.class)
    public void shouldEvalFailingAssert() throws Exception {
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
        engine.eval("assert 1==0");
    }


    @Test
    public void shouldClearEngineScopeOnReset() throws Exception {
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
        engine.eval("x = { y -> y + 1}");
        Bindings b = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        assertTrue(b.containsKey("x"));
        assertEquals(2, ((Closure) b.get("x")).call(1));

        // should clear the bindings
        engine.reset();
        try {
            engine.eval("x(1)");
            fail("Bindings should have been cleared.");
        } catch (Exception ex) {

        }

        b = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        assertFalse(b.containsKey("x"));

        // redefine x
        engine.eval("x = { y -> y + 2}");
        assertEquals(3, engine.eval("x(1)"));
        b = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        assertTrue(b.containsKey("x"));
        assertEquals(3, ((Closure) b.get("x")).call(1));
    }

    @Test
    public void shouldResetClassLoader() throws Exception {
        final GremlinGroovyScriptEngine scriptEngine = new GremlinGroovyScriptEngine();
        try {
            scriptEngine.eval("addOne(1)");
            fail("Should have tossed ScriptException since addOne is not yet defined.");
        } catch (ScriptException se) {
        }

        // validate that the addOne function works
        scriptEngine.eval("addOne = { y-> y + 1}");
        assertEquals(2, scriptEngine.eval("addOne(1)"));

        // reset the script engine which should blow out the addOne function that's there.
        scriptEngine.reset();

        try {
            scriptEngine.eval("addOne(1)");
            fail("Should have tossed ScriptException since addOne is no longer defined after reset.");
        } catch (ScriptException se) {
        }
    }

    @Test
    public void shouldProcessScriptWithUTF8Characters() throws Exception {
        final ScriptEngine engine = new GremlinGroovyScriptEngine();
        assertEquals("轉注", engine.eval("'轉注'"));
    }

    @Test
    public void shouldAllowVariableReuseAcrossThreads() throws Exception {
        final BasicThreadFactory testingThreadFactory = new BasicThreadFactory.Builder().namingPattern("test-gremlin-scriptengine-%d").build();
        final ExecutorService service = Executors.newFixedThreadPool(8, testingThreadFactory);
        final GremlinGroovyScriptEngine scriptEngine = new GremlinGroovyScriptEngine();

        final AtomicBoolean failed = new AtomicBoolean(false);
        final int max = 512;
        final List<Pair<Integer, List<Integer>>> futures = Collections.synchronizedList(new ArrayList<>(max));
        IntStream.range(0, max).forEach(i -> {
            final int yValue = i * 2;
            final int zValue = i * -1;
            final Bindings b = new SimpleBindings();
            b.put("x", i);
            b.put("y", yValue);

            final String script = "z=" + zValue + ";[x,y,z]";
            try {
                service.submit(() -> {
                    try {
                        final List<Integer> result = (List<Integer>) scriptEngine.eval(script, b);
                        futures.add(Pair.with(i, result));
                    } catch (Exception ex) {
                        failed.set(true);
                    }
                });
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        service.shutdown();
        assertThat(service.awaitTermination(120000, TimeUnit.MILLISECONDS), is(true));

        // likely a concurrency exception if it occurs - and if it does then we've messed up because that's what this
        // test is partially designed to protected against.
        assertThat(failed.get(), is(false));
        assertEquals(max, futures.size());
        futures.forEach(t -> {
            assertEquals(t.getValue0(), t.getValue1().get(0));
            assertEquals(t.getValue0() * 2, t.getValue1().get(1).intValue());
            assertEquals(t.getValue0() * -1, t.getValue1().get(2).intValue());
        });
    }

    @Test
    public void shouldInvokeFunctionRedirectsOutputToContextWriter() throws Exception {
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
        StringWriter writer = new StringWriter();
        engine.getContext().setWriter(writer);

        final String code = "def myFunction() { print \"Hello World!\" }";
        engine.eval(code);
        engine.invokeFunction("myFunction", EMPTY_ARGS);
        assertEquals("Hello World!", writer.toString());

        writer = new StringWriter();
        final StringWriter writer2 = new StringWriter();
        engine.getContext().setWriter(writer2);
        engine.invokeFunction("myFunction", EMPTY_ARGS);
        assertEquals("", writer.toString());
        assertEquals("Hello World!", writer2.toString());
    }

    @Test
    public void shouldInvokeFunctionRedirectsOutputToContextOut() throws Exception {
        final GremlinGroovyScriptEngine  engine = new GremlinGroovyScriptEngine();
        StringWriter writer = new StringWriter();
        final StringWriter unusedWriter = new StringWriter();
        engine.getContext().setWriter(unusedWriter);
        engine.put("out", writer);

        final String code = "def myFunction() { print \"Hello World!\" }";
        engine.eval(code);
        engine.invokeFunction("myFunction", EMPTY_ARGS);
        assertEquals("", unusedWriter.toString());
        assertEquals("Hello World!", writer.toString());

        writer = new StringWriter();
        final StringWriter writer2 = new StringWriter();
        engine.put("out", writer2);
        engine.invokeFunction("myFunction", EMPTY_ARGS);
        assertEquals("", unusedWriter.toString());
        assertEquals("", writer.toString());
        assertEquals("Hello World!", writer2.toString());
    }

    @Test
    public void shouldEnableEngineContextAccessibleToScript() throws Exception {
        final GremlinGroovyScriptEngine  engine = new GremlinGroovyScriptEngine();
        final ScriptContext engineContext = engine.getContext();
        engine.put("theEngineContext", engineContext);
        final String code = "[answer: theEngineContext.is(context)]";
        assertThat(((Map) engine.eval(code)).get("answer"), is(true));
    }

    @Test
    public void shouldEnableContextBindingOverridesEngineContext() throws Exception {
        final GremlinGroovyScriptEngine  engine = new GremlinGroovyScriptEngine();
        final ScriptContext engineContext = engine.getContext();
        final Map<String,Object> otherContext = new HashMap<>();
        otherContext.put("foo", "bar");
        engine.put("context", otherContext);
        engine.put("theEngineContext", engineContext);
        final String code = "[answer: context.is(theEngineContext) ? \"wrong\" : context.foo]";
        assertEquals("bar", ((Map) engine.eval(code)).get("answer"));
    }

    @Test
    public void shouldGetClassMapCacheBasicStats() throws Exception {
        final GremlinGroovyScriptEngine  engine = new GremlinGroovyScriptEngine();
        assertEquals(0, engine.getClassCacheEstimatedSize());
        assertEquals(0, engine.getClassCacheHitCount());
        assertEquals(0, engine.getClassCacheLoadCount());
        assertEquals(0, engine.getClassCacheLoadFailureCount());
        assertEquals(0, engine.getClassCacheLoadSuccessCount());

        engine.eval("1+1");

        assertEquals(1, engine.getClassCacheEstimatedSize());
        assertEquals(0, engine.getClassCacheHitCount());
        assertEquals(1, engine.getClassCacheLoadCount());
        assertEquals(0, engine.getClassCacheLoadFailureCount());
        assertEquals(1, engine.getClassCacheLoadSuccessCount());

        for (int ix = 0; ix < 100; ix++) {
            engine.eval("1+1");
        }

        assertEquals(1, engine.getClassCacheEstimatedSize());
        assertEquals(100, engine.getClassCacheHitCount());
        assertEquals(1, engine.getClassCacheLoadCount());
        assertEquals(0, engine.getClassCacheLoadFailureCount());
        assertEquals(1, engine.getClassCacheLoadSuccessCount());

        for (int ix = 0; ix < 100; ix++) {
            engine.eval("1+" + ix);
        }

        assertEquals(100, engine.getClassCacheEstimatedSize());
        assertEquals(101, engine.getClassCacheHitCount());
        assertEquals(100, engine.getClassCacheLoadCount());
        assertEquals(0, engine.getClassCacheLoadFailureCount());
        assertEquals(100, engine.getClassCacheLoadSuccessCount());

        try {
            engine.eval("(me broken");
            fail("Should have tanked with compilation error");
        } catch (Exception ex) {
            assertThat(ex, instanceOf(ScriptException.class));
        }

        assertEquals(101, engine.getClassCacheEstimatedSize());
        assertEquals(101, engine.getClassCacheHitCount());
        assertEquals(101, engine.getClassCacheLoadCount());
        assertEquals(1, engine.getClassCacheLoadFailureCount());
        assertEquals(100, engine.getClassCacheLoadSuccessCount());

        try {
            engine.eval("(me broken");
            fail("Should have tanked with compilation error");
        } catch (Exception ex) {
            assertThat(ex, instanceOf(ScriptException.class));
        }

        assertEquals(101, engine.getClassCacheEstimatedSize());
        assertEquals(102, engine.getClassCacheHitCount());
        assertEquals(101, engine.getClassCacheLoadCount());
        assertEquals(1, engine.getClassCacheLoadFailureCount());
        assertEquals(100, engine.getClassCacheLoadSuccessCount());
    }
}