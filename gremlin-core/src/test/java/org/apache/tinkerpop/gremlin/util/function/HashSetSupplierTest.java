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
package org.apache.tinkerpop.gremlin.util.function;

import org.junit.Test;

import java.util.HashSet;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class HashSetSupplierTest {
    @Test
    public void shouldSupplyHashSet() {
        assertEquals(0, HashSetSupplier.instance().get().size());
    }

    @Test
    public void shouldSupplyHashSetInstance() {
        assertEquals(0, HashSetSupplier.instance().get().size());
        assertThat(HashSetSupplier.instance().get(), instanceOf(HashSet.class));
    }

    @Test
    public void shouldSupplyNewHashSetOnEachInvocation() {
        final HashSet<Object> l1 = HashSetSupplier.instance().get();
        final HashSet<Object> l2 = HashSetSupplier.instance().get();
        final HashSet<Object> l3 = HashSetSupplier.instance().get();

        assertNotSame(l1, l2);
        assertNotSame(l1, l3);
        assertNotSame(l2, l3);
    }
}
