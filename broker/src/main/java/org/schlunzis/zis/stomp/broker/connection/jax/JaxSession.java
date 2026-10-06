package org.schlunzis.zis.stomp.broker.connection.jax;

import io.avaje.jex.websocket.WsContext;
import org.schlunzis.zis.stomp.broker.connection.StompSession;

import java.util.Objects;

/// Simple wrapper implementation for [WsContext] as a [StompSession].
///
/// @param context the Jax websocket context
/// @since 1.0.0
public record JaxSession(WsContext context) implements StompSession {

    @Override
    public void send(String message) {
        context.send(message);
    }

    @Override
    public void close() {
        context.closeSession();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JaxSession(WsContext c))) return false;
        // Using the underlying websocket object to have the same behavior across instances.
        return Objects.equals(context.ws(), c.ws());
    }

    @Override
    public int hashCode() {
        // Using the underlying websocket object to have the same behavior across instances.
        return Objects.hashCode(context.ws());
    }

}
