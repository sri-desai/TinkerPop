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
package org.apache.tinkerpop.gremlin.driver.ser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import org.apache.tinkerpop.gremlin.driver.message.RequestMessage;
import org.apache.tinkerpop.gremlin.driver.message.ResponseMessage;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.gryo.GryoIo;
import org.apache.tinkerpop.gremlin.structure.io.gryo.GryoMapper;
import org.apache.tinkerpop.shaded.kryo.ClassResolver;
import org.apache.tinkerpop.shaded.kryo.Kryo;
import org.apache.tinkerpop.shaded.kryo.Serializer;
import org.apache.tinkerpop.shaded.kryo.io.Input;
import org.apache.tinkerpop.shaded.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public abstract class AbstractGryoMessageSerializerV3d0 extends AbstractMessageSerializer {
    private GryoMapper gryoMapper;
    private ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return gryoMapper.createMapper();
        }
    };

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String TOKEN_CUSTOM = "custom";
    public static final String TOKEN_SERIALIZE_RESULT_TO_STRING = "serializeResultToString";
    public static final String TOKEN_USE_MAPPER_FROM_GRAPH = "useMapperFromGraph";
    public static final String TOKEN_BUFFER_SIZE = "bufferSize";
    public static final String TOKEN_CLASS_RESOLVER_SUPPLIER = "classResolverSupplier";

    protected boolean serializeToString = false;
    private int bufferSize = 4096;

    /**
     * Creates an instance with a provided mapper configured {@link GryoMapper} instance. Note that this instance
     * will be overridden by {@link #configure} is called.
     */
    public AbstractGryoMessageSerializerV3d0(final GryoMapper kryo) {
        this.gryoMapper = kryo;
    }

    /**
     * Called from the {@link #configure(Map, Map)} method right before the call to create the builder. Sub-classes
     * can choose to alter the builder or completely replace it.
     */
    GryoMapper.Builder configureBuilder(final GryoMapper.Builder builder, final Map<String, Object> config,
                                        final Map<String, Graph> graphs) {
        return builder;
    }

    @Override
    public final void configure(final Map<String, Object> config, final Map<String, Graph> graphs) {
        final GryoMapper.Builder builder;
        final Object graphToUseForMapper = config.get(TOKEN_USE_MAPPER_FROM_GRAPH);
        if (graphToUseForMapper != null) {
            if (null == graphs) throw new IllegalStateException(String.format(
                    "No graphs have been provided to the serializer and therefore %s is not a valid configuration", TOKEN_USE_MAPPER_FROM_GRAPH));

            final Graph g = graphs.get(graphToUseForMapper.toString());
            if (null == g) throw new IllegalStateException(String.format(
                    "There is no graph named [%s] configured to be used in the %s setting",
                    graphToUseForMapper, TOKEN_USE_MAPPER_FROM_GRAPH));

            // a graph was found so use the mapper it constructs.  this allows gryo to be auto-configured with any
            // custom classes that the implementation allows for
            builder = g.io(GryoIo.build()).mapper();
        } else {
            // no graph was supplied so just use the default - this will likely be the case when using a graph
            // with no custom classes or a situation where the user needs complete control like when using two
            // distinct implementations each with their own custom classes.
            builder = GryoMapper.build();
        }

        addIoRegistries(config, builder);
        addClassResolverSupplier(config, builder);
        addCustomClasses(config, builder);

        this.serializeToString = Boolean.parseBoolean(config.getOrDefault(TOKEN_SERIALIZE_RESULT_TO_STRING, "false").toString());
        this.bufferSize = Integer.parseInt(config.getOrDefault(TOKEN_BUFFER_SIZE, "4096").toString());

        this.gryoMapper = configureBuilder(builder, config, graphs).create();
    }

    private void addClassResolverSupplier(final Map<String, Object> config, final GryoMapper.Builder builder) {
        final String className = (String) config.getOrDefault(TOKEN_CLASS_RESOLVER_SUPPLIER, null);
        if (className != null && !className.isEmpty()) {
            try {
                final Class<?> clazz = Class.forName(className);
                try {
                    final Method instanceMethod = tryInstanceMethod(clazz);
                    builder.classResolver((Supplier<ClassResolver>) instanceMethod.invoke(null));
                } catch (Exception methodex) {
                    // tried instance() and that failed so try newInstance() no-arg constructor
                    builder.classResolver((Supplier<ClassResolver>) clazz.newInstance());
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private void addCustomClasses(final Map<String, Object> config, final GryoMapper.Builder builder) {
        final List<String> classNameList = getListStringFromConfig(TOKEN_CUSTOM, config);

        classNameList.stream().forEach(serializerDefinition -> {
            String className;
            Optional<String> serializerName;
            if (serializerDefinition.contains(";")) {
                final String[] split = serializerDefinition.split(";");
                if (split.length != 2)
                    throw new IllegalStateException(String.format("Invalid format for serializer definition [%s] - expected <class>;<serializer-class>", serializerDefinition));

                className = split[0];
                serializerName = Optional.of(split[1]);
            } else {
                serializerName = Optional.empty();
                className = serializerDefinition;
            }

            try {
                final Class clazz = Class.forName(className);
                final Serializer serializer;
                if (serializerName.isPresent()) {
                    final Class serializerClazz = Class.forName(serializerName.get());
                    serializer = (Serializer) serializerClazz.newInstance();
                    builder.addCustom(clazz, kryo -> serializer);
                } else
                    builder.addCustom(clazz);
            } catch (Exception ex) {
                throw new IllegalStateException("Class could not be found", ex);
            }
        });
    }

    @Override
    public ResponseMessage deserializeResponse(final ByteBuf msg) throws SerializationException {
        try {
            final Kryo kryo = kryoThreadLocal.get();
            final byte[] payload = new byte[msg.capacity()];
            msg.readBytes(payload);
            try (final Input input = new Input(payload)) {
                return kryo.readObject(input, ResponseMessage.class);
            }
        } catch (Exception ex) {
            logger.warn("Response [{}] could not be deserialized by {}.", msg, GryoMessageSerializerV1d0.class.getName());
            throw new SerializationException(ex);
        }
    }

    @Override
    public ByteBuf serializeResponseAsBinary(final ResponseMessage responseMessage, final ByteBufAllocator allocator) throws SerializationException {
        ByteBuf encodedMessage = null;
        try {
            final Kryo kryo = kryoThreadLocal.get();
            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                final Output output = new Output(baos, bufferSize);
                final ResponseMessage msgToWrite = !serializeToString ? responseMessage :
                        ResponseMessage.build(responseMessage.getRequestId())
                            .code(responseMessage.getStatus().getCode())
                            .statusAttributes(responseMessage.getStatus().getAttributes())
                            .responseMetaData(responseMessage.getResult().getMeta())
                            .result(serializeResultToString(responseMessage))
                            .statusMessage(responseMessage.getStatus().getMessage()).create();
                kryo.writeObject(output, msgToWrite);

                final long size = output.total();
                if (size > Integer.MAX_VALUE)
                    throw new SerializationException(String.format("Message size of %s exceeds allocatable space", size));

                output.flush();
                encodedMessage = allocator.buffer((int) size);
                encodedMessage.writeBytes(baos.toByteArray());
            }

            return encodedMessage;
        } catch (Exception ex) {
            if (encodedMessage != null) ReferenceCountUtil.release(encodedMessage);

            logger.warn("Response [{}] could not be serialized by {}.", responseMessage.toString(), GryoMessageSerializerV1d0.class.getName());
            throw new SerializationException(ex);
        }
    }

    @Override
    public RequestMessage deserializeRequest(final ByteBuf msg) throws SerializationException {
        try {
            final Kryo kryo = kryoThreadLocal.get();
            final byte[] payload = new byte[msg.readableBytes()];
            msg.readBytes(payload);
            try (final Input input = new Input(payload)) {
                // by the time the message gets here, the mime length/type have been already read, so this part just
                // needs to process the payload.
                return kryo.readObject(input, RequestMessage.class);
            }
        } catch (Exception ex) {
            logger.warn("Request [{}] could not be deserialized by {}.", msg, GryoMessageSerializerV1d0.class.getName());
            throw new SerializationException(ex);
        }
    }

    @Override
    public ByteBuf serializeRequestAsBinary(final RequestMessage requestMessage, final ByteBufAllocator allocator) throws SerializationException {
        ByteBuf encodedMessage = null;
        try {
            final Kryo kryo = kryoThreadLocal.get();
            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                final Output output = new Output(baos, bufferSize);
                final String mimeType = mimeTypesSupported()[0];
                output.writeByte(mimeType.length());
                output.write(mimeType.getBytes(UTF8));

                kryo.writeObject(output, requestMessage);

                final long size = output.total();
                if (size > Integer.MAX_VALUE)
                    throw new SerializationException(String.format("Message size of %s exceeds allocatable space", size));

                output.flush();
                encodedMessage = allocator.buffer((int) size);
                encodedMessage.writeBytes(baos.toByteArray());
            }

            return encodedMessage;
        } catch (Exception ex) {
            if (encodedMessage != null) ReferenceCountUtil.release(encodedMessage);

            logger.warn("Request [{}] could not be serialized by {}.", requestMessage.toString(), GryoMessageSerializerV1d0.class.getName());
            throw new SerializationException(ex);
        }
    }

    private Object serializeResultToString(final ResponseMessage msg) {
        if (msg.getResult() == null) return "null";
        if (msg.getResult().getData() == null) return "null";

        // the IteratorHandler should return a collection so keep it as such
        final Object o = msg.getResult().getData();
        if (o instanceof Collection) {
            return ((Collection) o).stream().map(d -> null == d ? "null" : d.toString()).collect(Collectors.toList());
        } else {
            return o.toString();
        }
    }
}