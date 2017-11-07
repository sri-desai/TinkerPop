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
package org.apache.tinkerpop.gremlin.process.computer.util;

import org.apache.tinkerpop.gremlin.process.computer.Memory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class MemoryHelper {

    public MemoryHelper() {
    }

    public static void validateValue(final Object value) throws IllegalArgumentException {
        if (null == value)
            throw Memory.Exceptions.memoryValueCanNotBeNull();
    }

    public static void validateKey(final String key) throws IllegalArgumentException {
        if (null == key)
            throw Memory.Exceptions.memoryKeyCanNotBeNull();
        if (key.isEmpty())
            throw Memory.Exceptions.memoryKeyCanNotBeEmpty();
    }
}
