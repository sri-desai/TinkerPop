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
package org.apache.tinkerpop.gremlin.process.traversal.strategy.verification;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.Column;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@RunWith(Parameterized.class)
public class LambdaRestrictionStrategyTest {

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"filter(x->true)", __.filter(x -> true), false},
                {"map(Traverser::get)", __.map(Traverser::get), false},
                {"sideEffect(x -> {int i = 1+1;})", __.sideEffect(x -> {
                    int i = 1 + 1;
                }), false},
                {"select('a','b').by(Object::toString)", __.select("a", "b").by(Object::toString), false},
                {"order().by((a,b)->a.compareTo(b))", __.order().by((a, b) -> ((Integer) a).compareTo((Integer) b)), false},
                {"order(local).by((a,b)->a.compareTo(b))", __.order(Scope.local).by((a, b) -> ((Integer) a).compareTo((Integer) b)), false},
                {"__.choose(v->v.toString().equals(\"marko\"),__.out(),__.in())", __.choose(v -> v.toString().equals("marko"), __.out(), __.in()), false},
                {"order(local).by(values,decr)", __.order(Scope.local).by(Column.values, (a, b) -> ((Double) a).compareTo((Double) b)), false},
                //
                {"order(local).by(values,decr)", __.order(Scope.local).by(Column.values, Order.decr), true},
                {"order().by(label,decr)", __.order().by(T.label, Order.decr), true},
                {"groupCount().by(label)", __.groupCount().by(T.label), true},
        });
    }

    @Parameterized.Parameter(value = 0)
    public String name;

    @Parameterized.Parameter(value = 1)
    public Traversal traversal;

    @Parameterized.Parameter(value = 2)
    public boolean allow;

    @Test
    public void shouldBeVerifiedIllegal() {
        final TraversalStrategies strategies = new DefaultTraversalStrategies();
        strategies.addStrategies(LambdaRestrictionStrategy.instance());
        traversal.asAdmin().setStrategies(strategies);
        if (allow) {
            traversal.asAdmin().applyStrategies();
        } else {
            try {
                traversal.asAdmin().applyStrategies();
                fail("The strategy should not allow lambdas: " + this.traversal);
            } catch (VerificationException ise) {
                assertTrue(ise.getMessage().contains("lambda"));
            }
        }
    }
}
