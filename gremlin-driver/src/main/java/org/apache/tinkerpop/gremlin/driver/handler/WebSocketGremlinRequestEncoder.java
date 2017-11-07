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
package org.apache.tinkerpop.gremlin.driver.handler;

import org.apache.tinkerpop.gremlin.driver.MessageSerializer;
import org.apache.tinkerpop.gremlin.driver.message.RequestMessage;
import org.apache.tinkerpop.gremlin.driver.ser.MessageTextSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
@ChannelHandler.Sharable
public final class WebSocketGremlinRequestEncoder extends MessageToMessageEncoder<RequestMessage> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketGremlinRequestEncoder.class);
    private final boolean binaryEncoding;

    private final MessageSerializer serializer;

    public WebSocketGremlinRequestEncoder(final boolean binaryEncoding, final MessageSerializer serializer) {
        this.binaryEncoding = binaryEncoding;
        this.serializer = serializer;
    }

    @Override
    protected void encode(final ChannelHandlerContext channelHandlerContext, final RequestMessage requestMessage, final List<Object> objects) throws Exception {
        try {
            if (binaryEncoding) {
                final ByteBuf encodedMessage = serializer.serializeRequestAsBinary(requestMessage, channelHandlerContext.alloc());
                objects.add(new BinaryWebSocketFrame(encodedMessage));
            } else {
                final MessageTextSerializer textSerializer = (MessageTextSerializer) serializer;
                objects.add(new TextWebSocketFrame(textSerializer.serializeRequestAsString(requestMessage)));
            }
        } catch (Exception ex) {
            logger.warn(String.format("An error occurred during serialization of this request [%s] - it could not be sent to the server.", requestMessage), ex);
        }
    }
}
