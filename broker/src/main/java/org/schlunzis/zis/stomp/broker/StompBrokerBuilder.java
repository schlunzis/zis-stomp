package org.schlunzis.zis.stomp.broker;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.broker.internal.StompBrokerFactory;
import org.schlunzis.zis.stomp.broker.websocket.WebsocketSession;

/// A stomp broker builder
///
/// @param <SESSION> the type of session handled by the broker
public class StompBrokerBuilder<SESSION extends WebsocketSession> {

    private @Nullable Authenticator authenticator = null;

    /// Creates a new STOMP broker builder.
    ///
    /// @see StompBroker#builder()
    /// @since 1.0.0
    StompBrokerBuilder() {
    }

    /// Sets the authenticator to be used by the stomp broker.
    /// This is only used for STOMP CONNECT frame authentication. Depending on the server implementation, the initial
    /// HTTP connection may also authenticate clients.
    ///
    /// If none is set, a [NoopAuthenticator] is used, which always returns `true`.
    ///
    /// @param authenticator the new authenticator
    /// @return this builder
    /// @since 1.0.0
    public StompBrokerBuilder<SESSION> authenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    /// Returns the authenticator set in this builder.
    ///
    /// @return the authenticator
    /// @since 1.0.0
    public @Nullable Authenticator authenticator() {
        return this.authenticator;
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
