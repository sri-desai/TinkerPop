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
package org.apache.tinkerpop.gremlin.structure.io.graphson;

import org.apache.commons.io.IOUtils;
import org.apache.tinkerpop.gremlin.structure.io.Compatibility;

import java.io.File;
import java.io.IOException;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public enum GraphSONCompatibility implements Compatibility {
    V1D0_3_2_3("3.2.3", "1.0", "v1d0"),
    V2D0_PARTIAL_3_2_3("3.2.3", "2.0", "v2d0-partial"),
    V2D0_NO_TYPE_3_2_3("3.2.3", "2.0", "v2d0-no-types"),
    V1D0_3_2_4("3.2.4", "1.0", "v1d0"),
    V2D0_PARTIAL_3_2_4("3.2.4", "2.0", "v2d0-partial"),
    V2D0_NO_TYPE_3_2_4("3.2.4", "2.0", "v2d0-no-types"),
    V1D0_3_2_5("3.2.5", "1.0", "v1d0"),
    V2D0_PARTIAL_3_2_5("3.2.5", "2.0", "v2d0-partial"),
    V2D0_NO_TYPE_3_2_5("3.2.5", "2.0", "v2d0-no-types"),
    V1D0_3_3_0("3.3.0", "2.0", "v1d0"),
    V2D0_PARTIAL_3_3_0("3.3.0", "2.0", "v2d0-partial"),
    V2D0_NO_TYPE_3_3_0("3.3.0", "2.0", "v2d0-no-types"),
    V3D0_PARTIAL_3_3_0("3.3.0", "3.0", "v3d0");

    private static final String SEP = File.separator;

    private final String graphSONVersion;
    private final String tinkerpopVersion;
    private final String configuration;

    GraphSONCompatibility(final String tinkerpopVersion, final String graphSONVersion, final String configuration) {
        this.tinkerpopVersion = tinkerpopVersion;
        this.configuration = configuration;
        this.graphSONVersion = graphSONVersion;
    }

    public byte[] readFromResource(final String resource) throws IOException {
        final String testResource = "_" + tinkerpopVersion.replace(".", "_") + SEP + resource + "-" + configuration + ".json";
        return IOUtils.toByteArray(getClass().getResourceAsStream(testResource));
    }

    @Override
    public String getReleaseVersion() {
        return tinkerpopVersion;
    }

    @Override
    public String getVersion() {
        return graphSONVersion;
    }

    @Override
    public String getConfiguration() {
        return configuration;
    }

    @Override
    public String toString() {
        return tinkerpopVersion + "-" + configuration;
    }
}
