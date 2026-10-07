package org.schlunzis.zis.stomp.broker;

import org.jspecify.annotations.Nullable;
import org.schlunzis.zis.stomp.broker.connection.StompSessionAdapter;
import org.schlunzis.zis.stomp.broker.internal.StompBrokerFactory;

import java.util.Objects;

/// A stomp broker builder.
///
/// @param <SESSION> the type of session handled by the broker
/// @since 1.0.0
public class StompBrokerBuilder<SESSION> {

    private String[] hosts = new String[0];
    private @Nullable Authenticator authenticator = null;
    private @Nullable StompSessionAdapter<SESSION> sessionAdapter;

    /// Creates a new STOMP broker builder.
    ///
    /// @see StompBroker#builder()
    /// @since 1.0.0
    StompBrokerBuilder() {
    }

    /// Sets the host names this stomp broker can be reached by.
    ///
    /// Glob pattern are not supported.
    ///
    /// @param hosts the hosts to accept
    /// @return this builder
    /// @throws NullPointerException if hosts is `null`
    /// @since 1.0.0
    public StompBrokerBuilder<SESSION> hosts(String... hosts) {
        this.hosts = Objects.requireNonNull(hosts);
        return this;
    }

    /// Returns the hosts set in this builder.
    ///
    /// @return the hosts
    /// @since 1.0.0
    public String[] hosts() {
        return this.hosts;
    }

    /// Sets the authenticator to be used by the stomp broker.
    /// This is only used for STOMP CONNECT frame authentication.
    ///
    /// Depending on the server implementation, the initial HTTP connection may authenticate clients
    /// (via e.g. HTTP Basic).
    /// In that case no authenticator has to be set.
    ///
    /// If none is set, a [NoopAuthenticator] is used, which always returns `true`.
    ///
    /// @param authenticator the new authenticator
    /// @return this builder
    /// @throws NullPointerException if the authenticator is `null`
    /// @see Authenticator#authenticate(String, String)
    /// @since 1.0.0
    public StompBrokerBuilder<SESSION> authenticator(Authenticator authenticator) {
        this.authenticator = Objects.requireNonNull(authenticator);
        return this;
    }

    /// Returns the authenticator set in this builder.
    ///
    /// @return the authenticator
    /// @since 1.0.0
    public @Nullable Authenticator authenticator() {
        return this.authenticator;
    }

    /// Sets the session adapter to be used by the stomp broker.
    ///
    /// @param sessionAdapter the new session adapter
    /// @return this builder
    /// @throws NullPointerException if the session adapter is `null`
    /// @since 1.0.0
    public StompBrokerBuilder<SESSION> sessionAdapter(StompSessionAdapter<SESSION> sessionAdapter) {
        this.sessionAdapter = Objects.requireNonNull(sessionAdapter);
        return this;
    }

    /// Returns the session adapter set in this builder.
    ///
    /// @return the session adapter
    /// @since 1.0.0
    public @Nullable StompSessionAdapter<SESSION> sessionAdapter() {
        return this.sessionAdapter;
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
