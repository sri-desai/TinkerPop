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

import org.apache.tinkerpop.gremlin.process.traversal.Operator;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.SackFunctions;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalOptionParent;
import org.apache.tinkerpop.gremlin.process.traversal.util.Metrics;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalMetrics;
import org.apache.tinkerpop.gremlin.structure.Column;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.util.function.HashMapSupplier;
import org.apache.tinkerpop.gremlin.util.function.Lambda;
import org.apache.tinkerpop.shaded.jackson.annotation.JsonTypeInfo;
import org.apache.tinkerpop.shaded.jackson.core.JsonGenerator;
import org.apache.tinkerpop.shaded.jackson.databind.BeanProperty;
import org.apache.tinkerpop.shaded.jackson.databind.jsontype.TypeIdResolver;
import org.apache.tinkerpop.shaded.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Extension of the Jackson's default TypeSerializer. An instance of this object will be passed to the serializers
 * on which they can safely call the utility methods to serialize types and making it compatible with the version
 * 2.0 of GraphSON.
 *
 * @author Kevin Gallardo (https://kgdo.me)
 */
public class GraphSONTypeSerializer extends TypeSerializer {

    private final TypeIdResolver idRes;
    private final String propertyName;
    private final TypeInfo typeInfo;
    private final String valuePropertyName;
    private final Map<Class, Class> classMap = new HashMap<>();

    GraphSONTypeSerializer(final TypeIdResolver idRes, final String propertyName, final TypeInfo typeInfo,
                           final String valuePropertyName) {
        this.idRes = idRes;
        this.propertyName = propertyName;
        this.typeInfo = typeInfo;
        this.valuePropertyName = valuePropertyName;
    }

    @Override
    public TypeSerializer forProperty(final BeanProperty beanProperty) {
        return this;
    }

    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return null;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public TypeIdResolver getTypeIdResolver() {
        return idRes;
    }

    @Override
    public void writeTypePrefixForScalar(final Object o, final JsonGenerator jsonGenerator) throws IOException {
        if (canWriteTypeId()) {
            writeTypePrefix(jsonGenerator, getTypeIdResolver().idFromValueAndType(o, getClassFromObject(o)));
        }
    }

    @Override
    public void writeTypePrefixForObject(final Object o, final JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        // TODO: FULL_TYPES should be implemented here as : if (fullTypesModeEnabled()) writeTypePrefix(Map);
    }

    @Override
    public void writeTypePrefixForArray(final Object o, final JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartArray();
        // TODO: FULL_TYPES should be implemented here as : if (fullTypesModeEnabled()) writeTypePrefix(List);
    }

    @Override
    public void writeTypeSuffixForScalar(final Object o, final JsonGenerator jsonGenerator) throws IOException {
        if (canWriteTypeId()) {
            writeTypeSuffix(jsonGenerator);
        }
    }

    @Override
    public void writeTypeSuffixForObject(final Object o, final JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeEndObject();
        // TODO: FULL_TYPES should be implemented here as : if (fullTypesModeEnabled()) writeTypeSuffix(Map);
    }

    @Override
    public void writeTypeSuffixForArray(final Object o, final JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeEndArray();
        // TODO: FULL_TYPES should be implemented here as : if (fullTypesModeEnabled()) writeTypeSuffix(List);
    }

    @Override
    public void writeCustomTypePrefixForScalar(final Object o, final JsonGenerator jsonGenerator, final String s) throws IOException {
        if (canWriteTypeId()) {
            writeTypePrefix(jsonGenerator, s);
        }
    }

    @Override
    public void writeCustomTypePrefixForObject(final Object o, final JsonGenerator jsonGenerator, final String s) throws IOException {
        jsonGenerator.writeStartObject();
        // TODO: FULL_TYPES should be implemented here as : if (fullTypesModeEnabled()) writeTypePrefix(s);
    }

    @Override
    public void writeCustomTypePrefixForArray(final Object o, final JsonGenerator jsonGenerator, final String s) throws IOException {
        jsonGenerator.writeStartArray();
        // TODO: FULL_TYPES should be implemented here as : if (fullTypesModeEnabled()) writeTypePrefix(s);
    }

    @Override
    public void writeCustomTypeSuffixForScalar(final Object o, final JsonGenerator jsonGenerator, final String s) throws IOException {
        if (canWriteTypeId()) {
            writeTypeSuffix(jsonGenerator);
        }
    }

    @Override
    public void writeCustomTypeSuffixForObject(final Object o, final JsonGenerator jsonGenerator, final String s) throws IOException {
        jsonGenerator.writeEndObject();
        // TODO: FULL_TYPES should be implemented here as : if (fullTypesModeEnabled()) writeTypeSuffix(s);
    }

    @Override
    public void writeCustomTypeSuffixForArray(final Object o, final JsonGenerator jsonGenerator,final  String s) throws IOException {
        jsonGenerator.writeEndArray();
        // TODO: FULL_TYPES should be implemented here as : if (fullTypesModeEnabled()) writeTypeSuffix(s);
    }

    private boolean canWriteTypeId() {
        return typeInfo != null
                && typeInfo == TypeInfo.PARTIAL_TYPES;
    }

    private void writeTypePrefix(final JsonGenerator jsonGenerator, final String s) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(this.getPropertyName(), s);
        jsonGenerator.writeFieldName(this.valuePropertyName);
    }

    private void writeTypeSuffix(final JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeEndObject();
    }

    /**
     *  We force only **one** translation of a Java object to a domain specific object. i.e. users register typeIDs
     *  and serializers/deserializers for the predefined types we have in the spec. Graph, Vertex, Edge,
     *  VertexProperty, etc... And **not** their implementations (TinkerGraph, DetachedVertex, TinkerEdge, etc..)
    */
    private Class getClassFromObject(final Object o) {
        final Class c = o.getClass();
        if (classMap.containsKey(c))
            return classMap.get(c);

        final Class mapped;
        if (Vertex.class.isAssignableFrom(c))
            mapped = Vertex.class;
        else if (Edge.class.isAssignableFrom(c))
            mapped = Edge.class;
        else if (Path.class.isAssignableFrom(c))
            mapped = Path.class;
        else if (VertexProperty.class.isAssignableFrom(c))
            mapped = VertexProperty.class;
        else if (Metrics.class.isAssignableFrom(c))
            mapped = Metrics.class;
        else if (TraversalMetrics.class.isAssignableFrom(c))
            mapped = TraversalMetrics.class;
        else if (Property.class.isAssignableFrom(c))
            mapped = Property.class;
        else if (ByteBuffer.class.isAssignableFrom(c))
            mapped = ByteBuffer.class;
        else if (InetAddress.class.isAssignableFrom(c))
            mapped = InetAddress.class;
        else if (Traverser.class.isAssignableFrom(c))
            mapped = Traverser.class;
        else if (Lambda.class.isAssignableFrom(c))
            mapped = Lambda.class;
        else if (VertexProperty.Cardinality.class.isAssignableFrom(c))
            mapped = VertexProperty.Cardinality.class;
        else if (Column.class.isAssignableFrom(c))
            mapped = Column.class;
        else if (Direction.class.isAssignableFrom(c))
            mapped = Direction.class;
        else if (Operator.class.isAssignableFrom(c))
            mapped = Operator.class;
        else if (Order.class.isAssignableFrom(c))
            mapped = Order.class;
        else if (Pop.class.isAssignableFrom(c))
            mapped = Pop.class;
        else if (SackFunctions.Barrier.class.isAssignableFrom(c))
            mapped = SackFunctions.Barrier.class;
        else if (TraversalOptionParent.Pick.class.isAssignableFrom(c))
            mapped = TraversalOptionParent.Pick.class;
        else if (Scope.class.isAssignableFrom(c))
            mapped = Scope.class;
        else if (T.class.isAssignableFrom(c))
            mapped = T.class;
        else
            mapped = c;

        classMap.put(c, mapped);
        return mapped;
    }
}
