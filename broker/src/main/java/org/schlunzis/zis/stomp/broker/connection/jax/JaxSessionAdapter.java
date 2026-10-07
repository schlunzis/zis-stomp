package org.schlunzis.zis.stomp.broker.connection.jax;

import io.avaje.jex.websocket.WsContext;
import org.schlunzis.zis.stomp.broker.connection.StompSessionAdapter;

import java.util.Objects;

/// Simple adapter implementation for [WsContext].
///
/// @since 1.0.0
public class JaxSessionAdapter implements StompSessionAdapter<WsContext> {

    /// Creates a new adapter for Jax websocket sessions.
    ///
    /// @since 1.0.0
    public JaxSessionAdapter() {
    }

    @Override
    public void send(WsContext context, String message) {
        context.send(message);
    }

    @Override
    public void close(WsContext context) {
        context.closeSession();
    }

    @Override
    public boolean equals(WsContext a, WsContext b) {
        // Using the underlying websocket object to have the same behavior across instances.
        return Objects.equals(a.ws(), b.ws());
    }

    @Override
    public int hashCode(WsContext context) {
        // Using the underlying websocket object to have the same behavior across instances.
        return Objects.hashCode(context.ws());
    }

}
