package org.schlunzis.zis.stomp.broker;

import org.schlunzis.zis.stomp.broker.internal.StompBrokerFactory;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketSession;

/// A stomp broker builder
///
/// @param <SESSION> the type of session handled by the broker
public class StompBrokerBuilder<SESSION extends WebsocketSession> {

    /// Creates a new STOMP broker builder.
    ///
    /// @see StompBroker#builder()
    /// @since 1.0.0
    StompBrokerBuilder() {
    }

    /// Builds the [StompBroker] instance.
    ///
    /// You may call this method multiple times to create multiple brokers with the same configuration.
    ///
    /// @return the STOMP broker
    /// @throws IllegalStateException if the websocket server is not set
    /// @since 1.0.0
    public StompBroker<SESSION> build() throws IllegalStateException {
        StompBrokerFactory<SESSION> factory = new StompBrokerFactory<>();
        return factory.create(this);
    }

}
