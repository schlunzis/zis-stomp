package org.schlunzis.zis.stomp.broker;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.broker.internal.StompBrokerFactory;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketServerFactory;

public class StompBrokerBuilder {

    @Nullable
    private WebsocketServerFactory websocketServerFactory;

    /// Creates a new STOMP broker builder.
    ///
    /// @see StompBroker#builder()
    /// @since 1.0.0
    StompBrokerBuilder() {
    }

    /// Sets the websocket server factory. This parameter is required.
    ///
    /// @param websocketServerFactory the websocket server factory
    /// @return the builder instance
    /// @since 1.0.0
    public StompBrokerBuilder websocketServerFactory(WebsocketServerFactory websocketServerFactory) {
        this.websocketServerFactory = websocketServerFactory;
        return this;
    }

    /// Returns the configured websocket server factory.
    ///
    /// @return the websocket server, or null if not set
    /// @since 1.0.0
    public @Nullable WebsocketServerFactory websocketServerFactory() {
        return websocketServerFactory;
    }

    /// Builds the [StompBroker] instance.
    ///
    /// You may call this method multiple times to create multiple brokers with the same configuration.
    ///
    /// @return the STOMP broker
    /// @throws IllegalStateException if the websocket server is not set
    /// @since 1.0.0
    public StompBroker build() throws IllegalStateException {
        StompBrokerFactory factory = new StompBrokerFactory();
        return factory.create(this);
    }

}
