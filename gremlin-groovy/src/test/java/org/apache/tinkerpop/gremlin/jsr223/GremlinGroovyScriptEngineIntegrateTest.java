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
package org.apache.tinkerpop.gremlin.jsr223;

import org.apache.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import org.javatuples.Pair;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GremlinGroovyScriptEngineIntegrateTest {
    @Test
    @Ignore("This is not a test that needs to run on build - it's more for profiling the GremlinGroovyScriptEngine")
    public void shouldTest() throws Exception {
        final Random r = new Random();
        final List<Pair<String, Integer>> scripts = new ArrayList<>();
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
        for (int ix = 0; ix < 1000000; ix++) {
            final String script = "1 + " + ix;
            final int output = (int) engine.eval(script);
            assertEquals(1 + ix, output);

            if (ix % 1000 == 0) scripts.add(Pair.with(script, output));

            if (ix % 25 == 0) {
                final Pair<String,Integer> p = scripts.get(r.nextInt(scripts.size()));
                assertEquals(p.getValue1().intValue(), (int) engine.eval(p.getValue0()));
            }
        }
    }
}
