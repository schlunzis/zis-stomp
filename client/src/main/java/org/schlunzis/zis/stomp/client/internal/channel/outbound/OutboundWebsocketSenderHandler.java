package org.schlunzis.zis.stomp.client.internal.channel.outbound;

import org.schlunzis.zis.stomp.client.internal.interaction.InteractionContext;
import org.schlunzis.zis.stomp.client.protocol.FrameBuilder;
import org.schlunzis.zis.stomp.client.websocket.WebSocketClient;

/// Outbound channel handler for sending STOMP frames over a WebSocket connection.
///
/// This handler uses a [WebSocketClient] to send the built STOMP frames.
///
/// @see WebSocketClient
public class OutboundWebsocketSenderHandler extends AbstractOutboundChannelHandler {

    private final WebSocketClient webSocketClient;

    public OutboundWebsocketSenderHandler(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void handle(FrameBuilder frameBuilder, InteractionContext<?> context) {
        webSocketClient.send(frameBuilder.build());
        super.handle(frameBuilder, context);
    }

}
