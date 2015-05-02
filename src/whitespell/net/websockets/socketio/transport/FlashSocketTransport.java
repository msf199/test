/**
 * Copyright 2012 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package whitespell.net.websockets.socketio.transport;

import whitespell.net.websockets.socketio.DisconnectableHub;
import whitespell.net.websockets.socketio.HeartbeatHandler;
import whitespell.net.websockets.socketio.SocketIOChannelInitializer;
import whitespell.net.websockets.socketio.Transport;
import whitespell.net.websockets.socketio.ack.AckManager;
import whitespell.net.websockets.socketio.handler.AuthorizeHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelPipeline;

@Sharable
public class FlashSocketTransport extends WebSocketTransport {

    public static final String NAME = "flashsocket";

    public FlashSocketTransport(String connectPath, boolean isSsl, AckManager ackManager,
            DisconnectableHub disconnectable, AuthorizeHandler authorizeHandler,
            HeartbeatHandler heartbeatHandler) {
        super(connectPath, isSsl, ackManager, disconnectable, authorizeHandler, heartbeatHandler);
        path = connectPath + NAME;
    }

    @Override
    protected Transport getTransport() {
        return Transport.FLASHSOCKET;
    }

    @Override
    protected void removeHandler(ChannelPipeline pipeline) {
        pipeline.remove(SocketIOChannelInitializer.WEB_SOCKET_TRANSPORT);
    }

}
