package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboundWebsocketSenderHandler extends AbstractOutboundChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(OutboundWebsocketSenderHandler.class);
    private final WebSocketClient webSocketClient;

    public OutboundWebsocketSenderHandler(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void handle(FrameBuilder frameBuilder, InteractionContext<?> context) {
        log.trace("Sending outbound frame via WebSocket {}", frameBuilder);
        webSocketClient.send(frameBuilder.build());
        super.handle(frameBuilder, context);
    }

}
