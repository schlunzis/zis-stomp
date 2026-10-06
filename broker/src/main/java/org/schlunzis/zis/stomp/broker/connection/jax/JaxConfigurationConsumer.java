package org.schlunzis.zis.stomp.broker.connection.jax;

import io.avaje.jex.websocket.WebSocketListener;
import org.schlunzis.zis.stomp.broker.StompBroker;

import java.io.StringReader;
import java.util.function.Consumer;

/// Configuration consumer for [io.avaje.jex.websocket.WebSocketPlugin].
///
/// @since 1.0.0
public class JaxConfigurationConsumer implements Consumer<WebSocketListener.Builder> {

    private final StompBroker<JaxSession> broker;

    /// Creates a new consumer that can be passed to a [io.avaje.jex.websocket.WebSocketPlugin] to configure message
    /// passing to the broker.
    ///
    /// @param broker the stomp broker to use for configuration
    /// @since 1.0.0
    public JaxConfigurationConsumer(StompBroker<JaxSession> broker) {
        this.broker = broker;
    }

    @Override
    public void accept(WebSocketListener.Builder ws) {
        ws
                .onOpen(ctx -> broker.onOpen(new JaxSession(ctx)))
                .onMessage(ctx -> broker.onMessage(new JaxSession(ctx), new StringReader(ctx.message())))
                .onClose(ctx -> broker.onClose(new JaxSession(ctx)))
                .onError(ctx -> broker.onError(new JaxSession(ctx), ctx.error()));
    }

}
