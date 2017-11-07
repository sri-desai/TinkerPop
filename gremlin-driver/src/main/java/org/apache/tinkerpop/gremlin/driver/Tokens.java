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
package org.apache.tinkerpop.gremlin.driver;

import java.util.Arrays;
import java.util.List;

/**
 * String constants used in gremlin-driver and gremlin-server.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public final class Tokens {
    private Tokens() {}

    public static final String OPS_AUTHENTICATION = "authentication";
    public static final String OPS_BYTECODE = "bytecode";
    public static final String OPS_EVAL = "eval";
    public static final String OPS_INVALID = "invalid";
    public static final String OPS_GATHER = "gather";
    public static final String OPS_KEYS = "keys";
    public static final String OPS_CLOSE = "close";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String OPS_SHOW = "show";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String OPS_IMPORT = "import";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String OPS_RESET = "reset";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String OPS_USE = "use";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String OPS_VERSION = "version";

    public static final String ARGS_BATCH_SIZE = "batchSize";
    public static final String ARGS_BINDINGS = "bindings";
    public static final String ARGS_ALIASES = "aliases";
    public static final String ARGS_FORCE = "force";
    public static final String ARGS_GREMLIN = "gremlin";
    public static final String ARGS_LANGUAGE = "language";
    public static final String ARGS_SCRIPT_EVAL_TIMEOUT = "scriptEvaluationTimeout";
    public static final String ARGS_HOST = "host";
    public static final String ARGS_SESSION = "session";
    public static final String ARGS_MANAGE_TRANSACTION = "manageTransaction";
    public static final String ARGS_SASL = "sasl";
    public static final String ARGS_SASL_MECHANISM = "saslMechanism";
    public static final String ARGS_SIDE_EFFECT = "sideEffect";
    public static final String ARGS_AGGREGATE_TO = "aggregateTo";
    public static final String ARGS_SIDE_EFFECT_KEY = "sideEffectKey";

    /**
     * @deprecated As of release 3.1.0-incubating, replaced by {@link #ARGS_ALIASES}.
     */
    @Deprecated
    public static final String ARGS_REBINDINGS = "rebindings";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    public static final String ARGS_COORDINATES = "coordinates";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String ARGS_COORDINATES_GROUP = "group";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String ARGS_COORDINATES_ARTIFACT = "artifact";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String ARGS_COORDINATES_VERSION = "version";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String ARGS_IMPORTS = "imports";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String ARGS_INFO_TYPE_DEPENDENCIES = "dependencies";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    public static final String ARGS_INFO_TYPE = "infoType";

    /**
     * @deprecated As of release 3.1.1-incubating, replaced by {@link #ARGS_INFO_TYPE_DEPENDENCIES}
     */
    @Deprecated
    public static final String ARGS_INFO_TYPE_DEPDENENCIES = "dependencies";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final String ARGS_INFO_TYPE_IMPORTS = "imports";

    /**
     * @deprecated As for release 3.2.2, not replaced as this feature was never really published as official.
     */
    @Deprecated
    public static final List<String> INFO_TYPES = Arrays.asList(ARGS_INFO_TYPE_DEPENDENCIES,
            ARGS_INFO_TYPE_IMPORTS);

    public static final String VAL_AGGREGATE_TO_BULKSET = "bulkset";
    public static final String VAL_AGGREGATE_TO_LIST = "list";
    public static final String VAL_AGGREGATE_TO_MAP = "map";
    public static final String VAL_AGGREGATE_TO_NONE = "none";
    public static final String VAL_AGGREGATE_TO_SET = "set";

    public static final String VAL_TRAVERSAL_SOURCE_ALIAS = "g";

    public static final String STATUS_ATTRIBUTE_EXCEPTIONS = "exceptions";
    public static final String STATUS_ATTRIBUTE_STACK_TRACE = "stackTrace";
}
