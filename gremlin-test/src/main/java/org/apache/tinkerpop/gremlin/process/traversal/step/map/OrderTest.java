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

import org.apache.tinkerpop.gremlin.LoadGraphWith;
import org.apache.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import org.apache.tinkerpop.gremlin.process.GremlinProcessRunner;
import org.apache.tinkerpop.gremlin.process.IgnoreEngine;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalEngine;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Column;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.GRATEFUL;
import static org.apache.tinkerpop.gremlin.LoadGraphWith.GraphData.MODERN;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
@RunWith(GremlinProcessRunner.class)
public abstract class OrderTest extends AbstractGremlinProcessTest {

    public abstract Traversal<Vertex, String> get_g_V_name_order();

    public abstract Traversal<Vertex, String> get_g_V_name_order_byXa1_b1X_byXb2_a2X();

    public abstract Traversal<Vertex, String> get_g_V_order_byXname_incrX_name();

    public abstract Traversal<Vertex, String> get_g_V_order_byXnameX_name();

    public abstract Traversal<Vertex, Double> get_g_V_outE_order_byXweight_decrX_weight();

    public abstract Traversal<Vertex, String> get_g_V_order_byXname_a1_b1X_byXname_b2_a2X_name();

    public abstract Traversal<Vertex, Map<String, Vertex>> get_g_V_asXaX_outXcreatedX_asXbX_order_byXshuffleX_selectXa_bX();

    public abstract Traversal<Vertex, Map<Integer, Integer>> get_g_VX1X_hasXlabel_personX_mapXmapXint_ageXX_orderXlocalX_byXvalues_decrX_byXkeys_incrX(final Object v1Id);

    public abstract Traversal<Vertex, Vertex> get_g_V_order_byXoutE_count__decrX();

    public abstract Traversal<Vertex, Map<String, List<Vertex>>> get_g_V_group_byXlabelX_byXname_order_byXdecrX_foldX();

    public abstract Traversal<Vertex, List<Double>> get_g_V_localXbothE_weight_foldX_order_byXsumXlocalX_decrX();

    public abstract Traversal<Vertex, Map<String, Object>> get_g_V_asXvX_mapXbothE_weight_foldX_sumXlocalX_asXsX_selectXv_sX_order_byXselectXsX_decrX();

    public abstract Traversal<Vertex, Vertex> get_g_V_hasLabelXpersonX_order_byXageX();

    public abstract Traversal<Vertex, List<Vertex>> get_g_V_hasLabelXpersonX_fold_orderXlocalX_byXageX();

    public abstract Traversal<Vertex, String> get_g_V_hasLabelXpersonX_order_byXvalueXageX__decrX_name();

    public abstract Traversal<Vertex, String> get_g_V_properties_order_byXkey_decrX_key();

    public abstract Traversal<Vertex, Vertex> get_g_V_hasXsong_name_OHBOYX_outXfollowedByX_outXfollowedByX_order_byXperformancesX_byXsongType_incrX();

    public abstract Traversal<Vertex, String> get_g_V_both_hasLabelXpersonX_order_byXage_decrX_limitX5X_name();

    public abstract Traversal<Vertex, String> get_g_V_both_hasLabelXpersonX_order_byXage_decrX_name();

    public abstract Traversal<Vertex, String> get_g_V_hasLabelXsongX_order_byXperfomances_decrX_byXnameX_rangeX110_120X_name();

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_name_order() {
        final Traversal<Vertex, String> traversal = get_g_V_name_order();
        printTraversalForm(traversal);
        checkOrderedResults(Arrays.asList("josh", "lop", "marko", "peter", "ripple", "vadas"), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_name_order_byXa1_b1X_byXb2_a2X() {
        final Traversal<Vertex, String> traversal = get_g_V_name_order_byXa1_b1X_byXb2_a2X();
        printTraversalForm(traversal);
        checkOrderedResults(Arrays.asList("marko", "vadas", "peter", "ripple", "josh", "lop"), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_order_byXname_incrX_name() {
        final Traversal<Vertex, String> traversal = get_g_V_order_byXname_incrX_name();
        printTraversalForm(traversal);
        checkOrderedResults(Arrays.asList("josh", "lop", "marko", "peter", "ripple", "vadas"), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_order_byXnameX_name() {
        final Traversal<Vertex, String> traversal = get_g_V_order_byXnameX_name();
        printTraversalForm(traversal);
        checkOrderedResults(Arrays.asList("josh", "lop", "marko", "peter", "ripple", "vadas"), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_outE_order_byXweight_decrX_weight() {
        final Traversal<Vertex, Double> traversal = get_g_V_outE_order_byXweight_decrX_weight();
        printTraversalForm(traversal);
        checkOrderedResults(Arrays.asList(1.0d, 1.0d, 0.5d, 0.4d, 0.4d, 0.2d), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_order_byXname_a1_b1X_byXname_b2_a2X_name() {
        final Traversal<Vertex, String> traversal = get_g_V_order_byXname_a1_b1X_byXname_b2_a2X_name();
        printTraversalForm(traversal);
        checkOrderedResults(Arrays.asList("marko", "vadas", "peter", "ripple", "josh", "lop"), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_asXaX_outXcreatedX_asXbX_order_byXshuffleX_selectXa_bX() {
        final Traversal<Vertex, Map<String, Vertex>> traversal = get_g_V_asXaX_outXcreatedX_asXbX_order_byXshuffleX_selectXa_bX();
        printTraversalForm(traversal);
        int counter = 0;
        int markoCounter = 0;
        int joshCounter = 0;
        int peterCounter = 0;
        while (traversal.hasNext()) {
            counter++;
            Map<String, Vertex> bindings = traversal.next();
            assertEquals(2, bindings.size());
            if (bindings.get("a").id().equals(convertToVertexId("marko"))) {
                assertEquals(convertToVertexId("lop"), bindings.get("b").id());
                markoCounter++;
            } else if (bindings.get("a").id().equals(convertToVertexId("josh"))) {
                assertTrue((bindings.get("b")).id().equals(convertToVertexId("lop")) || bindings.get("b").id().equals(convertToVertexId("ripple")));
                joshCounter++;
            } else if (bindings.get("a").id().equals(convertToVertexId("peter"))) {
                assertEquals(convertToVertexId("lop"), bindings.get("b").id());
                peterCounter++;
            } else {
                fail("This state should not have been reachable");
            }
        }
        assertEquals(4, markoCounter + joshCounter + peterCounter);
        assertEquals(1, markoCounter);
        assertEquals(1, peterCounter);
        assertEquals(2, joshCounter);
        assertEquals(4, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_VX1X_hasXlabel_personX_mapXmapXint_ageXX_orderXlocalX_byXvalues_decrX_byXkeys_incrX() {
        final Traversal<Vertex, Map<Integer, Integer>> traversal = get_g_VX1X_hasXlabel_personX_mapXmapXint_ageXX_orderXlocalX_byXvalues_decrX_byXkeys_incrX(convertToVertexId("marko"));
        printTraversalForm(traversal);
        final Map<Integer, Integer> map = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(4, map.size());
        final Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();
        Map.Entry<Integer, Integer> entry = iterator.next();
        assertEquals(3, entry.getKey().intValue());
        assertEquals(87, entry.getValue().intValue());
        entry = iterator.next();
        assertEquals(2, entry.getKey().intValue());
        assertEquals(58, entry.getValue().intValue());
        entry = iterator.next();
        assertEquals(1, entry.getKey().intValue());
        assertEquals(29, entry.getValue().intValue());
        entry = iterator.next();
        assertEquals(4, entry.getKey().intValue());
        assertEquals(29, entry.getValue().intValue());
        assertFalse(iterator.hasNext());
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_order_byXoutE_count__decrX() {
        Arrays.asList(get_g_V_order_byXoutE_count__decrX()).forEach(traversal -> {
            printTraversalForm(traversal);
            final List<Vertex> vertices = traversal.toList();
            assertEquals(vertices.size(), 6);
            assertEquals("marko", vertices.get(0).value("name"));
            assertEquals("josh", vertices.get(1).value("name"));
            assertEquals("peter", vertices.get(2).value("name"));
            assertTrue(vertices.get(3).value("name").equals("vadas") || vertices.get(3).value("name").equals("ripple") || vertices.get(3).value("name").equals("lop"));
            assertTrue(vertices.get(4).value("name").equals("vadas") || vertices.get(4).value("name").equals("ripple") || vertices.get(4).value("name").equals("lop"));
            assertTrue(vertices.get(5).value("name").equals("vadas") || vertices.get(5).value("name").equals("ripple") || vertices.get(5).value("name").equals("lop"));
        });
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_group_byXlabelX_byXname_order_byXdecrX_foldX() {
        final Traversal<Vertex, Map<String, List<Vertex>>> traversal = get_g_V_group_byXlabelX_byXname_order_byXdecrX_foldX();
        printTraversalForm(traversal);
        final Map<String, List<Vertex>> map = traversal.next();
        assertFalse(traversal.hasNext());
        assertEquals(2, map.size());
        List list = map.get("software");
        assertEquals(2, list.size());
        assertEquals("lop", list.get(1));
        assertEquals("ripple", list.get(0));
        list = map.get("person");
        assertEquals(4, list.size());
        assertEquals("josh", list.get(3));
        assertEquals("marko", list.get(2));
        assertEquals("peter", list.get(1));
        assertEquals("vadas", list.get(0));
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_localXbothE_weight_foldX_order_byXsumXlocalX_decrX() {
        final Traversal<Vertex, List<Double>> traversal = get_g_V_localXbothE_weight_foldX_order_byXsumXlocalX_decrX();
        final List<List<Double>> list = traversal.toList();
        assertEquals(list.get(0).size(), 3);
        assertEquals(list.get(1).size(), 3);
        //assertEquals(list.get(2).size(),3);  // they both have value 1.0 and thus can't guarantee a tie order
        //assertEquals(list.get(3).size(),1);
        assertEquals(list.get(4).size(), 1);
        assertEquals(list.get(5).size(), 1);
        ///
        assertEquals(2.4d, list.get(0).stream().reduce(0.0d, (a, b) -> a + b), 0.01d);
        assertEquals(1.9d, list.get(1).stream().reduce(0.0d, (a, b) -> a + b), 0.01d);
        assertEquals(1.0d, list.get(2).stream().reduce(0.0d, (a, b) -> a + b), 0.01d);
        assertEquals(1.0d, list.get(3).stream().reduce(0.0d, (a, b) -> a + b), 0.01d);
        assertEquals(0.5d, list.get(4).stream().reduce(0.0d, (a, b) -> a + b), 0.01d);
        assertEquals(0.2d, list.get(5).stream().reduce(0.0d, (a, b) -> a + b), 0.01d);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_asXvX_mapXbothE_weight_foldX_sumXlocalX_asXsX_selectXv_sX_order_byXselectXsX_decrX() {
        final Traversal<Vertex, Map<String, Object>> traversal = get_g_V_asXvX_mapXbothE_weight_foldX_sumXlocalX_asXsX_selectXv_sX_order_byXselectXsX_decrX();
        final List<Map<String, Object>> list = traversal.toList();
        assertEquals(convertToVertex(graph, "josh"), list.get(0).get("v"));
        assertEquals(2.4d, (Double) list.get(0).get("s"), 0.1d);
        ///
        assertEquals(convertToVertex(graph, "marko"), list.get(1).get("v"));
        assertEquals(1.9d, (Double) list.get(1).get("s"), 0.1d);
        //
        assertEquals(1.0d, (Double) list.get(2).get("s"), 0.1d);   // they both have 1.0 so you can't test the "v" as().
        assertEquals(1.0d, (Double) list.get(3).get("s"), 0.1d);
        ///
        assertEquals(convertToVertex(graph, "vadas"), list.get(4).get("v"));
        assertEquals(0.5d, (Double) list.get(4).get("s"), 0.1d);
        ///
        assertEquals(convertToVertex(graph, "peter"), list.get(5).get("v"));
        assertEquals(0.2d, (Double) list.get(5).get("s"), 0.1d);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_hasLabelXpersonX_order_byXageX() {
        final Traversal<Vertex, Vertex> traversal = get_g_V_hasLabelXpersonX_order_byXageX();
        printTraversalForm(traversal);
        checkResults(Arrays.asList(convertToVertex(graph, "vadas"), convertToVertex(graph, "marko"), convertToVertex(graph, "josh"), convertToVertex(graph, "peter")), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_hasLabelXpersonX_fold_orderXlocalX_byXageX() {
        final Traversal<Vertex, List<Vertex>> traversal = get_g_V_hasLabelXpersonX_fold_orderXlocalX_byXageX();
        printTraversalForm(traversal);
        final List<Vertex> list = traversal.next();
        assertEquals(convertToVertex(graph, "vadas"), list.get(0));
        assertEquals(convertToVertex(graph, "marko"), list.get(1));
        assertEquals(convertToVertex(graph, "josh"), list.get(2));
        assertEquals(convertToVertex(graph, "peter"), list.get(3));
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_hasLabelXpersonX_order_byXvalueXageX__decrX_name() {
        final Traversal<Vertex, String> traversal = get_g_V_hasLabelXpersonX_order_byXvalueXageX__decrX_name();
        printTraversalForm(traversal);
        checkOrderedResults(Arrays.asList("peter", "josh", "marko", "vadas"), traversal);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_properties_order_byXkey_decrX_key() {
        final Traversal<Vertex, String> traversal = get_g_V_properties_order_byXkey_decrX_key();
        printTraversalForm(traversal);
        checkOrderedResults(Arrays.asList(
                "name", "name", "name", "name", "name", "name",
                "lang", "lang",
                "age", "age", "age", "age"), traversal);
    }

    @Test
    @LoadGraphWith(GRATEFUL)
    public void g_V_hasXsong_name_OHBOYX_outXfollowedByX_outXfollowedByX_order_byXperformancesX_byXsongType_incrX() {
        final Traversal<Vertex, Vertex> traversal = get_g_V_hasXsong_name_OHBOYX_outXfollowedByX_outXfollowedByX_order_byXperformancesX_byXsongType_incrX();
        printTraversalForm(traversal);
        int counter = 0;
        String lastSongType = "a";
        int lastPerformances = Integer.MIN_VALUE;
        while (traversal.hasNext()) {
            final Vertex vertex = traversal.next();
            final String currentSongType = vertex.value("songType");
            final int currentPerformances = vertex.value("performances");
            assertTrue(currentPerformances == lastPerformances || currentPerformances > lastPerformances);
            if (currentPerformances == lastPerformances)
                assertTrue(currentSongType.equals(lastSongType) || currentSongType.compareTo(lastSongType) < 0);
            lastSongType = currentSongType;
            lastPerformances = currentPerformances;
            counter++;
        }
        assertEquals(144, counter);
    }

    @Test
    @LoadGraphWith(MODERN)
    public void g_V_both_hasLabelXpersonX_order_byXage_decrX_limitX5X_name() {
        final Traversal<Vertex, String> traversal = get_g_V_both_hasLabelXpersonX_order_byXage_decrX_limitX5X_name();
        printTraversalForm(traversal);
        checkOrderedResults(Arrays.asList("peter", "josh", "josh", "josh", "marko"), traversal);
    }

    @Test
    @IgnoreEngine(TraversalEngine.Type.STANDARD) // validating the internal sort/limit works in GraphComputer
    @LoadGraphWith(MODERN)
    public void g_V_both_hasLabelXpersonX_order_byXage_decrX_name() {
        final Traversal<Vertex, String> traversal = get_g_V_both_hasLabelXpersonX_order_byXage_decrX_name();
        traversal.asAdmin().applyStrategies();
        if (!TraversalHelper.getFirstStepOfAssignableClass(OrderGlobalStep.class, traversal.asAdmin()).isPresent())
            return; // total hack to avoid providers that don't compile to OrderGlobalStep
        TraversalHelper.getFirstStepOfAssignableClass(OrderGlobalStep.class, traversal.asAdmin()).get().setLimit(1);
        printTraversalForm(traversal);
        final List<String> results = traversal.toList();
        assertTrue(results.size() < 8);
        assertFalse(traversal.hasNext());
    }

    @Test
    @LoadGraphWith(GRATEFUL)
    public void g_V_hasLabelXsongX_order_byXperfomances_decrX_byXnameX_rangeX110_120X_name() {
        final Traversal<Vertex, String> traversal = get_g_V_hasLabelXsongX_order_byXperfomances_decrX_byXnameX_rangeX110_120X_name();
        printTraversalForm(traversal);
        checkOrderedResults(Arrays.asList(
                "WANG DANG DOODLE", "THE ELEVEN", "WAY TO GO HOME", "FOOLISH HEART",
                "GIMME SOME LOVING", "DUPREES DIAMOND BLUES", "CORRINA", "PICASSO MOON",
                "KNOCKING ON HEAVENS DOOR", "MEMPHIS BLUES"), traversal);
    }

    public static class Traversals extends OrderTest {

        @Override
        public Traversal<Vertex, String> get_g_V_name_order() {
            return g.V().<String>values("name").order();
        }

        @Override
        public Traversal<Vertex, String> get_g_V_name_order_byXa1_b1X_byXb2_a2X() {
            return g.V().<String>values("name").order().by((a, b) -> a.substring(1, 2).compareTo(b.substring(1, 2))).by((a, b) -> b.substring(2, 3).compareTo(a.substring(2, 3)));
        }

        @Override
        public Traversal<Vertex, String> get_g_V_order_byXname_incrX_name() {
            return g.V().order().by("name", Order.incr).values("name");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_order_byXnameX_name() {
            return g.V().order().by("name").values("name");
        }

        @Override
        public Traversal<Vertex, Double> get_g_V_outE_order_byXweight_decrX_weight() {
            return g.V().outE().order().by("weight", Order.decr).values("weight");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_order_byXname_a1_b1X_byXname_b2_a2X_name() {
            return g.V().order().
                    <String>by("name", (a, b) -> a.substring(1, 2).compareTo(b.substring(1, 2))).
                    <String>by("name", (a, b) -> b.substring(2, 3).compareTo(a.substring(2, 3))).values("name");
        }

        @Override
        public Traversal<Vertex, Map<String, Vertex>> get_g_V_asXaX_outXcreatedX_asXbX_order_byXshuffleX_selectXa_bX() {
            return g.V().as("a").out("created").as("b").order().by(Order.shuffle).select("a", "b");
        }

        @Override
        public Traversal<Vertex, Map<Integer, Integer>> get_g_VX1X_hasXlabel_personX_mapXmapXint_ageXX_orderXlocalX_byXvalues_decrX_byXkeys_incrX(final Object v1Id) {
            return g.V(v1Id).hasLabel("person").map(v -> {
                final Map<Integer, Integer> map = new HashMap<>();
                map.put(1, (int) v.get().value("age"));
                map.put(2, (int) v.get().value("age") * 2);
                map.put(3, (int) v.get().value("age") * 3);
                map.put(4, (int) v.get().value("age"));
                return map;
            }).order(Scope.local).by(Column.values, Order.decr).by(Column.keys, Order.incr);
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_order_byXoutE_count__decrX() {
            return g.V().order().by(outE().count(), Order.decr);
        }

        @Override
        public Traversal<Vertex, Map<String, List<Vertex>>> get_g_V_group_byXlabelX_byXname_order_byXdecrX_foldX() {
            return g.V().<String, List<Vertex>>group().by(T.label).by(__.values("name").order().by(Order.decr).fold());
        }

        @Override
        public Traversal<Vertex, List<Double>> get_g_V_localXbothE_weight_foldX_order_byXsumXlocalX_decrX() {
            return g.V().local(__.bothE().<Double>values("weight").fold()).order().by(__.sum(Scope.local), Order.decr);
        }

        @Override
        public Traversal<Vertex, Map<String, Object>> get_g_V_asXvX_mapXbothE_weight_foldX_sumXlocalX_asXsX_selectXv_sX_order_byXselectXsX_decrX() {
            return g.V().as("v").map(__.bothE().<Double>values("weight").fold()).sum(Scope.local).as("s").select("v", "s").order().by(__.select("s"), Order.decr);
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_hasLabelXpersonX_order_byXageX() {
            return g.V().hasLabel("person").order().by("age");
        }

        @Override
        public Traversal<Vertex, List<Vertex>> get_g_V_hasLabelXpersonX_fold_orderXlocalX_byXageX() {
            return g.V().hasLabel("person").fold().order(Scope.local).by("age");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_hasLabelXpersonX_order_byXvalueXageX__decrX_name() {
            return g.V().hasLabel("person").order().<Vertex>by(v -> v.value("age"), Order.decr).values("name");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_properties_order_byXkey_decrX_key() {
            return g.V().properties().order().by(T.key, Order.decr).key();
        }

        @Override
        public Traversal<Vertex, Vertex> get_g_V_hasXsong_name_OHBOYX_outXfollowedByX_outXfollowedByX_order_byXperformancesX_byXsongType_incrX() {
            return g.V().has("song", "name", "OH BOY").out("followedBy").out("followedBy").order().by("performances").by("songType", Order.decr);
        }

        @Override
        public Traversal<Vertex, String> get_g_V_both_hasLabelXpersonX_order_byXage_decrX_limitX5X_name() {
            return g.V().both().hasLabel("person").order().by("age", Order.decr).limit(5).values("name");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_both_hasLabelXpersonX_order_byXage_decrX_name() {
            return g.V().both().hasLabel("person").order().by("age", Order.decr).values("name");
        }

        @Override
        public Traversal<Vertex, String> get_g_V_hasLabelXsongX_order_byXperfomances_decrX_byXnameX_rangeX110_120X_name() {
            return g.V().hasLabel("song").order().by("performances", Order.decr).by("name").range(110, 120).values("name");
        }
    }
}
